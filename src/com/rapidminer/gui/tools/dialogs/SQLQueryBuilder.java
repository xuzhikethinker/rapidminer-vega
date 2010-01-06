/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.tools.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ProgressMonitor;
import com.rapidminer.gui.tools.ProgressUtils;
import com.rapidminer.gui.tools.SQLEditor;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.syntax.DefaultInputHandler;
import com.rapidminer.gui.tools.syntax.InputHandler;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.jdbc.ColumnIdentifier;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.connection.ConnectionEntry;
import com.rapidminer.tools.jdbc.connection.DatabaseConnectionService;


/**
 * 
 * @author Tobias Malbrecht
 */
public class SQLQueryBuilder extends ButtonDialog {

	private static final long serialVersionUID = 1779762368364719191L;
	
    /** The list with all tables. */
    private final JList tableList = new JList();
    
    /** The list with all attribute names. */
    private final JList attributeList = new JList();
    
    /** The text area with the where clause. */
    private final JTextArea whereTextArea = new JTextArea(4, 15);

    /** The text area with the where clause. */
    private final SQLEditor sqlQueryTextArea = new SQLEditor();
    {
    	InputHandler inputHandler = new DefaultInputHandler() {
    		@Override
			public void keyReleased(KeyEvent e) {
    			fireStateChanged();
    			super.keyReleased(e);
    		}
    	};
    	inputHandler.addDefaultKeyBindings();
    	sqlQueryTextArea.setInputHandler(inputHandler);
    }
    
    /** All attribute names for the available tables. */
    private final Map<String, List<ColumnIdentifier>> attributeNameMap = new LinkedHashMap<String, List<ColumnIdentifier>>();
    
    private ConnectionEntry entry = null;

	public SQLQueryBuilder(String key, boolean modal, Object ... arguments) {
		super(key, modal, arguments);
	}
	
	public void setConnectionEntry(ConnectionEntry entry) {
		this.entry = entry;
		try {
			retrieveTableAndAttributeNames();
		} catch (SQLException e) {
			SwingTools.showSimpleErrorMessage("db_connection_failed_url", e, entry.getURL());
		}
	}
	
	public JPanel makeQueryBuilderPanel() {
		return makeQueryBuilderPanel(false);
	}
	
	public JPanel makeQueryBuilderPanel(boolean editOnly) {
		JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
		
		if (!editOnly) {
	        JPanel gridPanel = new JPanel(createGridLayout(1, 3));
	        // table and attribute lists, where clause text area
	        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        tableList.addListSelectionListener(new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent e) {
	                updateAttributeNames();
	                updateSQLQuery();
	            }
	        });
	        JScrollPane tablePane = new ExtendedJScrollPane(tableList);
	        tablePane.setBorder(createTitledBorder("Tables"));
	        gridPanel.add(tablePane);
	
	        attributeList.addListSelectionListener(new ListSelectionListener() {
	            public void valueChanged(ListSelectionEvent e) {
	                updateSQLQuery();
	            }
	        });
	        JScrollPane attributePane = new ExtendedJScrollPane(attributeList);
	        attributePane.setBorder(createTitledBorder("Attributes"));
	        gridPanel.add(attributePane);
	        
	        whereTextArea.addKeyListener(new KeyListener() {
	            public void keyTyped(KeyEvent e) {}
	            public void keyPressed(KeyEvent e) {}
	            public void keyReleased(KeyEvent e) {
	                updateSQLQuery();
	            }
	        });
	        JScrollPane whereTextPane = new ExtendedJScrollPane(whereTextArea);
	        whereTextPane.setBorder(createTitledBorder("Where Clause"));
	        gridPanel.add(whereTextPane);
	        
	        c.weighty = 0.5;
	        c.gridwidth = GridBagConstraints.REMAINDER;
	        panel.add(gridPanel, c);
		}
        
        // SQL statement field
        c.weighty = 1.0d;
        sqlQueryTextArea.setBorder(createTitledBorder("SQL Query"));
        panel.add(sqlQueryTextArea, c);

        return panel;
	}
	
    private void updateAttributeNames() {
        List<ColumnIdentifier> allColumnIdentifiers = new LinkedList<ColumnIdentifier>();
        Object[] selection = tableList.getSelectedValues();
        for (Object o : selection) {
            String tableName = (String)o;
            List<ColumnIdentifier> attributeNames = this.attributeNameMap.get(tableName);
            if (attributeNames != null) {
                Iterator<ColumnIdentifier> i = attributeNames.iterator();
                while (i.hasNext()) {
                    ColumnIdentifier currentIdentifier = i.next();
                    allColumnIdentifiers.add(currentIdentifier);
                }
            }
        }
        attributeList.removeAll();
        ColumnIdentifier[] identifierArray = new ColumnIdentifier[allColumnIdentifiers.size()];
        allColumnIdentifiers.toArray(identifierArray);
        attributeList.setListData(identifierArray);
    }
    
    private void appendAttributeName(StringBuffer result, ColumnIdentifier identifier, boolean first, boolean singleTable) {
        if (!first) {
            result.append(", ");
        }
        if (singleTable) {
            result.append(identifier.getFullName(entry.getProperties(), singleTable));
        } else {
            result.append(identifier.getFullName(entry.getProperties(), singleTable) + " AS " + identifier.getAliasName(entry.getProperties(), singleTable));
        }        
    }
    
    private void updateSQLQuery() {
        fireStateChanged();
        
        Object[] tableSelection = tableList.getSelectedValues();
        if (tableSelection.length == 0) {
            sqlQueryTextArea.setText("");
            return;
        }
        
        boolean singleTable = tableSelection.length == 1;
        
        // SELECT
        StringBuffer result = new StringBuffer("SELECT ");
        Object[] attributeSelection = attributeList.getSelectedValues();
        if (singleTable && (((attributeSelection.length == 0) || (attributeSelection.length == attributeList.getModel().getSize())))) {
            result.append("*");
        } else {
            if ((attributeSelection.length == 0) || (attributeSelection.length == attributeList.getModel().getSize())) {
                boolean first = true;
                for (int i = 0; i < attributeList.getModel().getSize(); i++) {
                    ColumnIdentifier identifier = (ColumnIdentifier) attributeList.getModel().getElementAt(i);
                    appendAttributeName(result, identifier, first, singleTable);
                    first = false;
                }                
            } else {
                boolean first = true;
                for (Object o : attributeSelection) {
                	ColumnIdentifier identifier = (ColumnIdentifier)o;
                    appendAttributeName(result, identifier, first, singleTable);
                    first = false;
                }
            }
        }
        
        // FROM
        result.append(Tools.getLineSeparator() + "FROM ");
        boolean first = true;
        for (Object o : tableSelection) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            String tableName = (String)o;
            result.append(entry.getProperties().getIdentifierQuoteOpen() + tableName + entry.getProperties().getIdentifierQuoteClose());
        }
        
        // WHERE
        String whereText = whereTextArea.getText().trim();
        if (whereText.length() > 0) {
            result.append(Tools.getLineSeparator() + "WHERE " + whereText);
        }
        sqlQueryTextArea.setText(result.toString());
    }

    private void retrieveTableAndAttributeNames() throws SQLException {
    	if (entry != null) {
	    	final DatabaseHandler handler = DatabaseConnectionService.connect(entry);
	        Thread retrieveTablesThread = new Thread() { 
	            @Override
				public void run() { 
	                ProgressMonitor monitor = ProgressUtils.createProgressMonitor(SQLQueryBuilder.this, 100, true, 50, true); 
	                monitor.start("Fetching tables and attributes from database..."); 
	                try {
	                	// retrieve data
	                    attributeNameMap.clear();
	                    if (handler != null) {
	                        Map<String, List<ColumnIdentifier>> newAttributeMap;
							try {
								newAttributeMap = handler.getAllTableMetaData();
		                        attributeNameMap.putAll(newAttributeMap);
							} catch (SQLException e) {
								// TODO: appropriate error message
								SwingTools.showVerySimpleErrorMessage("Retrieval of table and attribute names failed");
							}
	                    }
	                    
	                    // set table name list data
	                    String[] allNames = new String[attributeNameMap.size()];
	                    attributeNameMap.keySet().toArray(allNames);
	                    tableList.removeAll();
	                    tableList.setListData(allNames);
	                } finally { 
	                    // to ensure that progress monitor is closed in case of any exception 
	                    if (monitor.getCurrent() != monitor.getTotal()) { 
	                        monitor.setCurrent(null, monitor.getTotal());
	                    }
	                    
	                    // disconnect
	                    try {
							handler.disconnect();
						} catch (SQLException e) {
							// TODO: appropriate error message
							SwingTools.showVerySimpleErrorMessage("Disconnecting from the database failed");
						}
	                }
	            } 
	        };
	        retrieveTablesThread.start();
    	}
    }
    
    public void setQuery(String query) {
    	sqlQueryTextArea.setText(query);
    }
    
    public String getQuery() {
    	return sqlQueryTextArea.getText();
    }
}
