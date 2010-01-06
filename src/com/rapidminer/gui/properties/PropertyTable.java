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
package com.rapidminer.gui.properties;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.rapidminer.gui.properties.celleditors.key.DefaultPropertyKeyRenderer;
import com.rapidminer.gui.properties.celleditors.key.DelegationKeyCellEditor;
import com.rapidminer.gui.properties.celleditors.key.ParameterValueKeyCellEditor;
import com.rapidminer.gui.properties.celleditors.key.PropertyKeyCellEditor;
import com.rapidminer.gui.properties.celleditors.value.AttributeFileValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.AttributeValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.AttributesValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.ColorValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.ConfigurationWizardValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.DatabaseConnectionValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.DatabaseTableValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.DefaultPropertyValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.EnumerationValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.InnerOperatorValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.ListValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.MatrixValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.OperatorValueValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.ParameterTupelCellEditor;
import com.rapidminer.gui.properties.celleditors.value.PreviewValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.PropertyValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.RegexpValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.RepositoryLocationValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.SQLQueryValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.SimpleFileValueCellEditor;
import com.rapidminer.gui.properties.celleditors.value.TextValueCellEditor;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeAttributeFile;
import com.rapidminer.parameter.ParameterTypeAttributes;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeChar;
import com.rapidminer.parameter.ParameterTypeColor;
import com.rapidminer.parameter.ParameterTypeConfiguration;
import com.rapidminer.parameter.ParameterTypeDatabaseConnection;
import com.rapidminer.parameter.ParameterTypeDatabaseTable;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeEnumeration;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInnerOperator;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeMatrix;
import com.rapidminer.parameter.ParameterTypeParameterValue;
import com.rapidminer.parameter.ParameterTypePassword;
import com.rapidminer.parameter.ParameterTypePreview;
import com.rapidminer.parameter.ParameterTypeRegexp;
import com.rapidminer.parameter.ParameterTypeRepositoryLocation;
import com.rapidminer.parameter.ParameterTypeSQLQuery;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.ParameterTypeText;
import com.rapidminer.parameter.ParameterTypeTupel;
import com.rapidminer.parameter.ParameterTypeValue;
import com.rapidminer.tools.LogService;

/**
 * A property table is a table for editing parameters of operators or other properties (like program settings). Hence,
 * it has two columns, one for the key and one for the value. This class does not do very much, but as we want such
 * tables to appear at several places, we use this superclass for formatting and as a factory for CellEditors.
 * 
 * @author Ingo Mierswa, Simon Fischer
 */
public abstract class PropertyTable extends ExtendedJTable {

	private static final long serialVersionUID = -8510884721529372231L;

	private static Map<Class<? extends ParameterType>, Class<? extends PropertyValueCellEditor>> knownValueEditors = new HashMap<Class<? extends ParameterType>, Class<? extends PropertyValueCellEditor>>();

	private static Map<Class<? extends ParameterType>, Class<? extends PropertyKeyCellEditor>> knownKeyEditors = new HashMap<Class<? extends ParameterType>, Class<? extends PropertyKeyCellEditor>>();

	// do not register ParameterTypeString because no constructor for this type exists
	// --> simply use Default editor for non-registered types (including String)
	// the type ParameterTypeDirectory will also be handled by the simple file case
	static {
		// register known value editors
		registerPropertyValueCellEditor(ParameterTypePassword.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeConfiguration.class, ConfigurationWizardValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypePreview.class, PreviewValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeColor.class, ColorValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeCategory.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeStringCategory.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeBoolean.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeChar.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeInt.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeDouble.class, DefaultPropertyValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeAttributeFile.class, AttributeFileValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeFile.class, SimpleFileValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeRepositoryLocation.class, RepositoryLocationValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeValue.class, OperatorValueValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeInnerOperator.class, InnerOperatorValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeList.class, ListValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeMatrix.class, MatrixValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeText.class, TextValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeAttribute.class, AttributeValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeTupel.class, ParameterTupelCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeRegexp.class, RegexpValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeAttributes.class, AttributesValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeEnumeration.class, EnumerationValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeDatabaseConnection.class, DatabaseConnectionValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeSQLQuery.class, SQLQueryValueCellEditor.class);
		registerPropertyValueCellEditor(ParameterTypeDatabaseTable.class, DatabaseTableValueCellEditor.class);

		// register known key editors
		registerPropertyKeyCellEditor(ParameterTypeParameterValue.class, ParameterValueKeyCellEditor.class);
		registerPropertyKeyCellEditor(ParameterTypeList.class, DelegationKeyCellEditor.class);
		registerPropertyKeyCellEditor(ParameterTypeEnumeration.class, DelegationKeyCellEditor.class);
	}

	private DefaultTableModel model;

	private final List<PropertyValueCellEditor> valueEditors = new ArrayList<PropertyValueCellEditor>();

	private final List<PropertyKeyCellEditor> keyEditors = new ArrayList<PropertyKeyCellEditor>();

	private final List<String> toolTips = new ArrayList<String>();

	private String[] columnNames = new String[] { "Key", "Value" };

	private final PropertyTableParameterChangeListener changeListener = new PropertyTableParameterChangeListener(this);

	public PropertyTable() {
		this(new String[] { "Key", "Value" });
	}

	public PropertyTable(String[] columnNames) {
		super(null, false, false);

		this.columnNames = columnNames;
		setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().setReorderingAllowed(false);

		// allow only row selections
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);

		// // use bigger rows since some of the GUI elements need more space
		// setRowHeight(getRowHeight() + SwingTools.TABLE_WITH_COMPONENTS_ROW_EXTRA_HEIGHT);

		// hard coded row height as in property panel
		setRowHeight(PropertyPanel.VALUE_CELL_EDITOR_HEIGHT);

		setShowPopupMenu(false);
	}

	public abstract ParameterType getParameterType(int row);

	public ParameterType getKeyParameterType(int row) {
		return getParameterType(row);
	}

	public abstract Operator getOperator(int row);

	public DefaultTableModel getDefaultModel() {
		return model;
	}

	protected static void updateEditorsAndRenderers(PropertyTable propertyTable) {
		propertyTable.valueEditors.clear();
		propertyTable.keyEditors.clear();
		propertyTable.toolTips.clear();
		int numberOfRows = propertyTable.getModel().getRowCount();
		for (int i = 0; i < numberOfRows; i++) {
			ParameterType type = propertyTable.getParameterType(i);
			propertyTable.valueEditors.add(createPropertyValueCellEditor(type, propertyTable.getOperator(i)));
			ParameterType keyType = propertyTable.getKeyParameterType(i);
			PropertyKeyCellEditor keyEditor = createPropertyKeyCellEditor(propertyTable, keyType, propertyTable.getOperator(i), propertyTable.changeListener);
			propertyTable.keyEditors.add(keyEditor);

			StringBuffer toolTip = new StringBuffer(type.getDescription());
			if ((!(type instanceof ParameterTypeCategory)) && (!(type instanceof ParameterTypeStringCategory))) {
				String range = type.getRange();
				if ((range != null) && (range.trim().length() > 0)) {
					toolTip.append(" (");
					toolTip.append(type.getRange());
					toolTip.append(")");
				}
			}
			propertyTable.toolTips.add(SwingTools.transformToolTipText(toolTip.toString()));
		}
	}

	public int getNumberOfKeyEditors() {
		return this.keyEditors.size();
	}

	protected PropertyKeyCellEditor getKeyEditor(int index) {
		if (keyEditors.size() == 0) {
			return null;
		} else {
			return keyEditors.get(index);
		}
	}

	protected void updateTableData(int rows) {
		setModel(new DefaultTableModel(columnNames, rows));
	}

	public void setModel(DefaultTableModel model) {
		this.model = model;
		super.setModel(model);
	}

	public int getNumberOfValueEditors() {
		return valueEditors.size();
	}

	public PropertyValueCellEditor getValueEditor(int index) {
		return valueEditors.get(index);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		if (column == 1) {
			return valueEditors.get(row);
		} else {
			TableCellRenderer renderer = keyEditors.get(row);
			if (renderer instanceof TableCellEditor) {
				return (TableCellEditor) renderer;
			} else {
				return super.getCellEditor(row, column);
			}
		}
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 1) {
			PropertyValueCellEditor editor = valueEditors.get(row);
			if (!editor.useEditorAsRenderer()) {
				return super.getCellRenderer(row, column);
			} else {
				return editor;
			}
		} else {
			if (keyEditors.size() == 0) {
				return null;
			} else {
				return keyEditors.get(row);
			}
		}
	}

	/**
	 * Programmatically starts editing the cell at <code>row</code> and <code>column</code>, if those indices are in the
	 * valid range, and the cell at those indices is editable. To prevent the <code>JTable</code> from editing a
	 * particular table, column or cell value, return false from the <code>isCellEditable</code> method in the
	 * <code>TableModel</code> interface.
	 * 
	 * @param row
	 *            the row to be edited
	 * @param column
	 *            the column to be edited
	 * @param e
	 *            event to pass into <code>shouldSelectCell</code>; note that as of Java 2 platform v1.2, the call to
	 *            <code>shouldSelectCell</code> is no longer made
	 * @return false if for any reason the cell cannot be edited, or if the indices are invalid
	 */
	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		if (cellEditor != null && !cellEditor.stopCellEditing()) {
			return false;
		}

		if (row < 0 || row >= getRowCount() || column < 0 || column >= getColumnCount()) {
			return false;
		}

		if (!isCellEditable(row, column))
			return false;

		TableCellEditor editor = getCellEditor(row, column);
		if (editor != null && editor.isCellEditable(e)) {
			editorComp = prepareEditor(editor, row, column);
			if (editorComp == null) {
				removeEditor();
				return false;
			}
			editorComp.setBounds(getCellRect(row, column, false));
			add(editorComp);
			editorComp.validate();
			editorComp.repaint();

			setCellEditor(editor);
			setEditingRow(row);
			setEditingColumn(column);
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	private String getToolTipText(int row) {
		if ((row >= 0) && (row < toolTips.size()))
			return toolTips.get(row);
		else
			return null;
	}

	/** This method ensures that the correct tool tip for the current table cell is delivered. */
	@Override
	public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();
		int row = rowAtPoint(p);
		return getToolTipText(row);
	}

	/** This method ensures that the correct tool tip for the current column is delivered. */
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {

			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				int realColumnIndex = convertColumnIndexToModel(index);
				if (realColumnIndex == 0)
					return "The names of the parameters.";
				else
					return "The values of the parameters.";
			}
		};
	}

	public static void registerPropertyValueCellEditor(Class<? extends ParameterType> typeClass, Class<? extends PropertyValueCellEditor> editor) {
		knownValueEditors.put(typeClass, editor);
	}

	public static void registerPropertyKeyCellEditor(Class<? extends ParameterType> typeClass, Class<? extends PropertyKeyCellEditor> editor) {
		knownKeyEditors.put(typeClass, editor);
	}

	public static PropertyValueCellEditor createPropertyValueCellEditor(ParameterType type, Operator operator) {
		Class<? extends PropertyValueCellEditor> clazz = knownValueEditors.get(type.getClass());
		Class usedClass = type.getClass();
		if (clazz == null) {
			while (clazz == null) {
				usedClass = usedClass.getSuperclass();
				if (!(ParameterType.class.isAssignableFrom(usedClass)))
					break;
				clazz = knownValueEditors.get(usedClass);
			}
		}

		if (clazz != null) {
			try {
				Constructor<? extends PropertyValueCellEditor> constructor = clazz.getConstructor(new Class[] { usedClass });
				PropertyValueCellEditor editor = constructor.newInstance(new Object[] { type });
				editor.setOperator(operator);
				return editor;
			} catch (InstantiationException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (IllegalAccessException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (SecurityException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (NoSuchMethodException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (IllegalArgumentException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (InvocationTargetException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			}
			// no proper editor found --> return default editor
			PropertyValueCellEditor editor = new DefaultPropertyValueCellEditor(type);
			editor.setOperator(operator);
			return editor;
		} else {
			// no proper editor found --> return default editor
			PropertyValueCellEditor editor = new DefaultPropertyValueCellEditor(type);
			editor.setOperator(operator);
			return editor;
		}
	}

	public static PropertyKeyCellEditor createPropertyKeyCellEditor(PropertyTable table, ParameterType type, Operator operator, PropertyTableParameterChangeListener changeListener) {
		Class<? extends PropertyKeyCellEditor> clazz = knownKeyEditors.get(type.getClass());
		Class usedClass = type.getClass();
		if (clazz == null) {
			while (clazz == null) {
				usedClass = usedClass.getSuperclass();
				if (!(ParameterType.class.isAssignableFrom(usedClass)))
					break;
				clazz = knownKeyEditors.get(usedClass);
			}
		}

		if (clazz != null) {
			try {
				Constructor<? extends PropertyKeyCellEditor> constructor = clazz.getConstructor(new Class[] { usedClass });
				PropertyKeyCellEditor editor = constructor.newInstance(new Object[] { type });
				if (editor instanceof ParameterValueKeyCellEditor && changeListener != null) {
					((ParameterValueKeyCellEditor) editor).setParameterChangeListener(changeListener);
				}
				editor.setOperator(operator, table);
				return editor;
			} catch (InstantiationException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (IllegalAccessException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (SecurityException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (NoSuchMethodException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (IllegalArgumentException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			} catch (InvocationTargetException e) {
				LogService.getGlobal().log("Cannot construct property editor: " + e, LogService.ERROR);
			}
			// no proper editor found --> return default editor
			PropertyKeyCellEditor editor = new DefaultPropertyKeyRenderer(type);
			editor.setOperator(operator, table);
			return editor;
		} else {
			// no proper editor found --> return default editor
			PropertyKeyCellEditor editor = new DefaultPropertyKeyRenderer(type);
			editor.setOperator(operator, table);
			return editor;
		}
	}
}
