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
package com.rapidminer.operator.nio;

import java.io.IOException;

import javax.swing.table.AbstractTableModel;

import jxl.Sheet;
import jxl.read.biff.BiffException;

import com.rapidminer.operator.nio.model.ExcelResultSetConfiguration;
import com.rapidminer.tools.Tools;

/** Returns values backed by an operned excel workbook.
 * 
 *  Note: This model used to be created in {@link ExcelResultSetConfiguration#makePreviewTableModel(com.rapidminer.tools.ProgressListener),
 *  but this lead to problems because effects like empty columns or rows (e.g. {@link ExcelResultSet#emptyColumns) were not respected.}
 * @author Simon Fischer
 *
 */
public class ExcelSheetTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private Sheet sheet;

	private ExcelResultSetConfiguration config;
	
	public ExcelSheetTableModel(Sheet sheet) {
		this.sheet = sheet;
	}

	public ExcelSheetTableModel(ExcelResultSetConfiguration excelResultSetConfiguration) throws IndexOutOfBoundsException, BiffException, IOException {
		this.config = excelResultSetConfiguration;
		this.sheet = config.getWorkbook().getSheet(config.getSheet());
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (config != null) {
			return sheet.getCell(columnIndex + config.getColumnOffset(), rowIndex + config.getRowOffset()).getContents();
		} else {
			return sheet.getCell(columnIndex, rowIndex).getContents();
		}
	}

	@Override
	public int getRowCount() {
		if (config != null) {
			return config.getRowLast() - config.getRowOffset() + 1;
		} else {
			return sheet.getRows();
		}
	}

	@Override
	public int getColumnCount() {
		if (config != null) {
			return config.getColumnLast() - config.getColumnOffset() + 1;
		} else {
			return sheet.getColumns();
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (config != null) {
			return Tools.getExcelColumnName(columnIndex + config.getColumnOffset());
		} else {
			return Tools.getExcelColumnName(columnIndex);
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
