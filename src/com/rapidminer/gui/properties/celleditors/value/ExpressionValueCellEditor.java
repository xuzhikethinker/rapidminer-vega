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
package com.rapidminer.gui.properties.celleditors.value;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.rapidminer.gui.properties.ExpressionPropertyDialog;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeExpression;

public class ExpressionValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {
	
	private static final long serialVersionUID = 2355429695124754211L;

	private static final String CALCULATOR_NAME = "calculator.png";
	
	private static Icon CALCULATOR_ICON = null;
	
	static {
		CALCULATOR_ICON = SwingTools.createIcon("16/" + CALCULATOR_NAME);
	}
	
	private final JPanel panel = new JPanel();

	private final JTextField textField = new JTextField(12);

	private final ParameterTypeExpression type;

	private final GridBagLayout gridBagLayout = new GridBagLayout();
	
	public ExpressionValueCellEditor(ParameterTypeExpression type) {
		this.type = type;
		panel.setLayout(gridBagLayout);
		panel.setToolTipText(type.getDescription());
		textField.setToolTipText(type.getDescription());
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}			
		});
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				fireEditingStopped();
			}			
			@Override public void focusGained(FocusEvent e) { }
		});

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.weightx = 1;
		gridBagLayout.setConstraints(textField, c);
		panel.add(textField);
		
		JButton button = new JButton(CALCULATOR_ICON);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonPressed();
			}
		});
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0;
		gridBagLayout.setConstraints(button, c);
		panel.add(button);
	}

	private void buttonPressed() {
		Object value = getCellEditorValue();
		String initial = value == null ? null : value.toString();
		ExpressionPropertyDialog dialog = new ExpressionPropertyDialog(type, initial);
		dialog.setVisible(true);
		if (dialog.isOk())
			setText(dialog.getExpression());
		fireEditingStopped();
	}

	protected void setText(String text) {
		if (text == null)
			textField.setText("");
		else
			textField.setText(text);
	}
	
    /** Does nothing. */
    public void setOperator(Operator operator) {}
    
	@Override
	public boolean rendersLabel() {
		return false;
	}
	
	public Object getCellEditorValue() {
		return (textField.getText().trim().length() == 0) ? null : textField.getText().trim();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
		textField.setText((value == null) ? "" : value.toString());
		return panel;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

	public boolean useEditorAsRenderer() {
		return true;
	}
}
