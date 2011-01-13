package com.rapidminer.operator.io.test;

import static junit.framework.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.DatabaseDataReader;
import com.rapidminer.operator.io.DatabaseExampleSetWriter;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.IOObjectEntry;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.test.TestUtils;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.jdbc.DatabaseHandler;

/**
 * 
 * @author Simon Fischer
 *
 */
public class DatabaseWriteTest {

	private static final String TABLE_NAME = "unit_test_table";
	private static final String TEST_DATA_LOCATION = "//Samples/data/Labor-Negotiations";
	private static final String TEST_REPOS_DATE_LOCATION = "//Samples/data/";
	
	
	private ExampleSet exampleSet;

	private static class DatabaseRef {
		private final String url, user, password;
		private String driverClass;
		private DatabaseRef(String url, String user, String password, String driverClass) {
			super();
			this.url = url;
			this.user = user;
			this.password = password;
			this.setDriverClass(driverClass);
		}
		public String getUrl() {
			return url;
		}
		public String getPassword() {
			return password;
		}
		public String getUser() {
			return user;
		}
		public void setDriverClass(String driverClass) {
			this.driverClass = driverClass;
		}
		/** May be null for bundled drivers. */
		public String getDriverClass() {
			return driverClass;
		}
	}
	
	private static final DatabaseRef DB_SQL_SERVER = new DatabaseRef("jdbc:jtds:sqlserver://192.168.1.8:1433/rapidanalytics", "rapidrepository", "rapidrepository", null);// "net.sourceforge.jtds.jdbc.Driver");
	private static final DatabaseRef DB_MY_SQL = new DatabaseRef("jdbc:mysql://192.168.1.7:3306/test", "rapidi", "rapidi", null); // "com.mysql.jdbc.Driver");
	private static final DatabaseRef DB_ORACLE = new DatabaseRef("jdbc:oracle:thin:@192.168.1.8:1521", "rapidi", "rapidi", "oracle.jdbc.driver.OracleDriver");
	private static final DatabaseRef DB_INGRES = new DatabaseRef("jdbc:ingres://192.168.1.7:28047/demodb", "ingres", "vw2010", null);
	
	@Before
	public void setUp() throws Exception {
		TestUtils.initRapidMiner(); // for read database operator
		final Entry entry = new RepositoryLocation(TEST_DATA_LOCATION).locateEntry();
		this.exampleSet = (ExampleSet) ((IOObjectEntry)entry).retrieveData(null);
	}
	
	@Test
	public void testCreateTableMicrosoftSQLServer() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_SQL_SERVER);
	}

	@Test
	public void testCreateTableMySQL() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_MY_SQL);
	}

	@Test
	public void testCreateTableOracle() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_ORACLE);
	}

	@Test
	public void testCreateTableIngres() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_INGRES);
	}
	
	@Test
	public void testWriteOperator() throws OperatorCreationException, OperatorException {
		DatabaseExampleSetWriter writer = OperatorService.createOperator(DatabaseExampleSetWriter.class);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		writer.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		writer.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		writer.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		writer.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, "LaborNegotiationOp");
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_OVERWRITE_MODE, DatabaseHandler.OVERWRITE_MODES[DatabaseHandler.OVERWRITE_MODE_OVERWRITE]);
		writer.write(exampleSet);
	}
	
	@Test
	public void testWriteOperatorGetGeneratedKeys() throws OperatorCreationException, OperatorException {
		String testTableName = "test_get_gen_keys_back";
		// Delete exiosting entries
		DatabaseHandler handler;
		try {
			handler = DatabaseHandler.getConnectedDatabaseHandler(DB_MY_SQL.getUrl(), DB_MY_SQL.getUser(), DB_MY_SQL.getPassword());
			handler.emptyTable(testTableName);
		} catch (SQLException e1) {
			throw new OperatorException("can not delete table", e1);
		}
		// Use Iris with out the id attribute
		Entry entry =null;
		ExampleSet eSet =null;
		try {
			entry = new RepositoryLocation(TEST_REPOS_DATE_LOCATION+"Iris").locateEntry();
			 eSet = (ExampleSet) ((IOObjectEntry)entry).retrieveData(null);
		} catch (RepositoryException e) {
			throw new OperatorException("can not access repository", e);
		}
		Attribute idAttribute = eSet.getAttributes().get("id");
		// remove id attribute
		eSet.getAttributes().remove(idAttribute);
		
		DatabaseExampleSetWriter writer = OperatorService.createOperator(DatabaseExampleSetWriter.class);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		writer.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		writer.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		writer.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		writer.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, testTableName);
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_OVERWRITE_MODE, DatabaseHandler.OVERWRITE_MODES[DatabaseHandler.OVERWRITE_MODE_APPEND]);
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_GET_GENERATED_PRIMARY_KEYS, Boolean.TRUE.toString());
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_GENERATED_KEYS_ATTRIBUTE_NAME, "id");
		ExampleSet result = writer.write(eSet);
		

		DatabaseDataReader reader = OperatorService.createOperator(DatabaseDataReader.class);		
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		reader.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		reader.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		reader.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, testTableName);
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		ExampleSet dbSet = reader.read();
		
		
		assertEquals(result.size(), dbSet.size());
		
		Attribute resultAtt = result.getAttributes().get("id");
		Attribute dbAtt = dbSet.getAttributes().get("id");
		
		// compare results
		Iterator<Example> resultIt = result.iterator();
		Iterator<Example> dbSetIt = dbSet.iterator();
		while (resultIt.hasNext()){
			Example resultEx = resultIt.next();
			Example dbEx = dbSetIt.next();
			assertEquals(resultEx.getValue(resultAtt), dbEx.getValue(dbAtt));
		}
	}

	@Test
	public void testReadOperator() throws OperatorCreationException, OperatorException {
		DatabaseDataReader reader = OperatorService.createOperator(DatabaseDataReader.class);		
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		reader.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		reader.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		reader.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, "LaborNegotiationOp");
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		ExampleSet exampleSet = reader.read();
		assertEquals(40, exampleSet.size());
		assertEquals(17, exampleSet.getAttributes().size());		
	}

	private void testCreateTable(DatabaseRef connection) throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		final String driverClass = connection.getDriverClass();
		if (driverClass != null) {
			Class.forName(driverClass);
		}
		DatabaseHandler handler = DatabaseHandler.getConnectedDatabaseHandler(connection.getUrl(), connection.getUser(), connection.getPassword());
		handler.createTable(exampleSet, TABLE_NAME, DatabaseHandler.OVERWRITE_MODE_OVERWRITE, true, -1);
		
		DatabaseDataReader readOp = OperatorService.createOperator(DatabaseDataReader.class);
		readOp.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		readOp.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, connection.getUrl());
		readOp.setParameter(DatabaseHandler.PARAMETER_USERNAME, connection.getUser());
		readOp.setParameter(DatabaseHandler.PARAMETER_PASSWORD, connection.getPassword());
		readOp.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		readOp.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, TABLE_NAME);
		ExampleSet result = readOp.read();
		
		TestUtils.assertEquals("example set", exampleSet, result, -1);
	}	
}
