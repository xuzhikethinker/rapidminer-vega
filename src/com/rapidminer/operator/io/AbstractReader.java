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
package com.rapidminer.operator.io;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.Process;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataError;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.Observable;
import com.rapidminer.tools.Observer;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.io.Encoding;

/** 
 * Superclass of all operators that have no input and generate a single output.
 * This class is mainly a tribute to the e-LICO DMO.
 * 
 * @author Simon Fischer 
 */
public abstract class AbstractReader<T extends IOObject> extends Operator {

	private final OutputPort outputPort = getOutputPorts().createPort("output");
	private final Class<? extends IOObject> generatedClass;

	private boolean cacheDirty = true;
	private MetaData cachedMetaData;
	private MetaDataError cachedError;

	public AbstractReader(OperatorDescription description, Class<? extends IOObject> generatedClass) {
		super(description);
		this.generatedClass = generatedClass;
		getTransformer().addRule(new MDTransformationRule() {
			@Override
			public void transformMD() {
				if (cacheDirty || !isMetaDataCacheable()) {										
					try {
						// TODO add extra thread for meta data generation?
						cachedMetaData = AbstractReader.this.getGeneratedMetaData();
						cachedError = null;
					} catch (OperatorException e) {
						cachedMetaData = new MetaData(AbstractReader.this.generatedClass);
						String msg = e.getMessage();
						if ((msg == null) || (msg.length() == 0)) {
							msg = e.toString();
						}
						// will be added below
						cachedError = new SimpleMetaDataError(Severity.WARNING, outputPort, "cannot_create_exampleset_metadata", new Object[] { msg });
					}
					if (cachedMetaData != null) {
						cachedMetaData.addToHistory(outputPort);
					}
					cacheDirty = false;
				}				
				outputPort.deliverMD(cachedMetaData);
				if (cachedError != null) {
					outputPort.addError(cachedError);
				}
			}
		});
		observeParameters();
	}

	private void observeParameters() {
		// we add this as the first observer. otherwise, this change is not seen
		// by the resulting meta data transformation
		getParameters().addObserverAsFirst(new Observer<String>() {
			@Override
			public void update(Observable<String> observable, String arg) {
				cacheDirty = true;
			}			
		}, false);
	}

	public MetaData getGeneratedMetaData() throws OperatorException {
		return new MetaData(generatedClass);
	}

	protected boolean isMetaDataCacheable() {
		return false;
	}
	
	/** Creates (or reads) the ExampleSet that will be returned by {@link #apply()}. */
	public abstract T read() throws OperatorException;

	@Override
	public void doWork() throws OperatorException {
		outputPort.deliver(read());
	}

	/** Describes an operator that can read certain file types. */
	public static class ReaderDescription {
		private final String fileExtension;
		private final Class<? extends AbstractReader> readerClass;		
		/** This parameter must be set to the file name. */
		private final String fileParameterKey;

		public ReaderDescription(String fileExtension,
				Class<? extends AbstractReader> readerClass,
				String fileParameterKey) {
			super();
			this.fileExtension = fileExtension;
			this.readerClass = readerClass;
			this.fileParameterKey = fileParameterKey;
		}		
	}

	private static final Map<String,ReaderDescription> READER_DESCRIPTIONS = new HashMap<String,ReaderDescription>();

	/** Registers an operator that can read files with a given extension. */
	protected static void registerReaderDescription(ReaderDescription rd) {		
		READER_DESCRIPTIONS.put(rd.fileExtension.toLowerCase(), rd);
	}

	public static AbstractReader createReader(URL url) throws OperatorCreationException {		
		String file = url.getFile();
		int dot = file.lastIndexOf('.');
		if (dot == -1) {
			return null;
		} else {
			String extension = file.substring(dot+1).toLowerCase();
			ReaderDescription rd = READER_DESCRIPTIONS.get(extension);
			if (rd == null) {
				return null;
			}
			AbstractReader reader = OperatorService.createOperator(rd.readerClass);
			reader.setParameter(rd.fileParameterKey, url.toString());
			return reader;
		}
	}

	public static boolean canMakeReaderFor(URL url) {
		String file = url.getFile();
		int dot = file.lastIndexOf('.');
		if (dot == -1) {
			return false;
		} else {
			String extension = file.substring(dot+1).toLowerCase();
			return READER_DESCRIPTIONS.containsKey(extension);
		}
	}

	/** Returns the key of the parameter that specifies the file to be read. */
	public static String getFileParameterForOperator(Operator operator) {
		for (ReaderDescription rd : READER_DESCRIPTIONS.values()) {
			if (rd.readerClass.equals(operator.getClass())) {
				return rd.fileParameterKey;	
			}
		}
		return null;		
	}

	@Override
	protected void registerOperator(Process process) {
		super.registerOperator(process);
		process.addObserver(new Observer<Process>() {
			@Override
			public void update(Observable<Process> observable, Process arg) {
				cacheDirty = true;
				AbstractReader.this.fireUpdate(AbstractReader.this);
			}			
		}, false);
		cacheDirty = true;
	}


	protected boolean supportsEncoding() {
		return false;
	}

//	@Override
//	public Operator cloneOperator(String name) {
//		Operator operator = super.cloneOperator(name);
//		((AbstractReader)operator).observeParameters();
//		return operator;
//	}
		
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		if (supportsEncoding())
			types.addAll(Encoding.getParameterTypes(this));
		return types;
	}
}
