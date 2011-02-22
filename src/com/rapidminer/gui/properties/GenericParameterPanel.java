/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.gui.properties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This panel might be used, where ever ParameterTypes should be editable without presence of an operator
 * or special circumstances. 
 * 
 * @author Sebastian Land
 *
 */
public class GenericParameterPanel extends PropertyPanel {

	private static final long serialVersionUID = -8633435565053835262L;

	private Parameters parameters = null;
//	private Map<String, String> keyValueMap = new LinkedHashMap<String, String>();
//	private Map<String, ParameterType> keyTypeMap = new LinkedHashMap<String, ParameterType>();
	
	public GenericParameterPanel() {
		
	}
	
	public GenericParameterPanel(Parameters parameters) {
		super();
		setParameters(parameters);
	}
	
	@Override
	protected Operator getOperator() {
		return null;
	}

	@Override
	protected Collection<ParameterType> getProperties() {
		List<ParameterType> visible = new LinkedList<ParameterType>();
		if (parameters != null) {
			for (ParameterType type: parameters.getParameterTypes()) {
				if (!type.isHidden())
					visible.add(type);
			}
		}
		return visible;
	}

	@Override
	protected String getValue(ParameterType type) {
		try {
			return parameters.getParameter(type.getKey());
		} catch (UndefinedParameterError e) {
			return type.getDefaultValueAsString();
		}
		//return keyValueMap.get(type.getKey());
	}

	@Override
	/**
	 * This implementation ignores the operator, since it is null anyway.
	 */
	protected void setValue(Operator operator, ParameterType type, String value) {
		parameters.setParameter(type.getKey(), value);
		//keyValueMap.put(type.getKey(), value);
		setupComponents();
	}
	
	public void setValue(String key, String value) {
		parameters.setParameter(key, value);
		//keyValueMap.put(key, value);
		setupComponents();
	}
	
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
//		keyValueMap.clear();
//		keyTypeMap.clear();
//		for (ParameterType type: parameters) {
//			keyValueMap.put(type.getKey(), type.getDefaultValueAsString());
//			keyTypeMap.put(type.getKey(), type);
//		}
//		
		// calling super method for rebuilding panel
		setupComponents();
	}
	
	public void clearProperties() {
		this.parameters = null;
		
		setupComponents();
	}

	public Parameters getParameters() {
		return parameters;
	}
	
//	public Collection<String> getPropertyKeys() {
//		return keyValueMap.keySet();
//	}
//	
//	public String getValue(String key) {
//		return keyValueMap.get(key);
//	}
}
