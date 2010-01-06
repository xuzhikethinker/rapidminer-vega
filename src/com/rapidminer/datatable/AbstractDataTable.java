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
package com.rapidminer.datatable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.Range;

import com.rapidminer.gui.plotter.charts.AbstractChartPanel.Selection;
import com.rapidminer.report.Tableable;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.container.Pair;

/**
 * This abstract data table implementation provides some default implementations for data
 * tables like listener handling. The method {@link #fireEvent()} can be used to promote
 * changes to all listeners.
 * 
 * In addition, IO methods are also provided by this abstract implementation.
 * 
 * @author Ingo Mierswa
 */
public abstract class AbstractDataTable implements DataTable, Tableable {
	
	/** The list of data table listeners. */
	private List<DataTableListener> listeners = new LinkedList<DataTableListener>();

	/** The name of the table. */
    private String name;
    
    private HashSet<String> deselectionSet = new HashSet<String>();
    
	private int deselectionCount;
    
    public AbstractDataTable(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
	public String[] getColumnNames() {
		String[] result = new String[getNumberOfColumns()];
		for (int i = 0; i < result.length; i++)
			result[i] = getColumnName(i);
		return result;
	}

	public void addDataTableListener(DataTableListener dataTableListener) {
		this.listeners.add(dataTableListener);
	}

	public void removeDataTableListener(DataTableListener dataTableListener) {
		this.listeners.remove(dataTableListener);
	}
	
	protected void fireEvent() {
		// copy to avoid ConcurrentModification
		Iterator i = new LinkedList<DataTableListener>(listeners).iterator();
		while (i.hasNext()) {
			((DataTableListener) i.next()).dataTableUpdated(this);
		}
	}

	public String getValueAsString(DataTableRow row, int column) {
		if (isDate(column)) {
			return Tools.formatDate(new Date((long)row.getValue(column)));
		} else if (isDateTime(column)) {
			return Tools.formatDateTime(new Date((long)row.getValue(column)));
		} else if (isTime(column)) {
			return Tools.formatTime(new Date((long)row.getValue(column)));
		} else if (isNominal(column)) {
			return mapIndex(column, (int)row.getValue(column));
		} else {
			return row.getValue(column) + "";
		}
	}
	
	public void write(PrintWriter out) throws IOException {
		out.println("# Generated by " + getName() + "[" + getClass().getName() + "]");
		for (int j = 0; j < getNumberOfColumns(); j++) {
			out.print((j != 0 ? "\t" : "# ") + getColumnName(j));
		}
		out.println();

		Iterator i = iterator();
		while (i.hasNext()) {
			DataTableRow row = (DataTableRow) i.next();
			for (int j = 0; j < getNumberOfColumns(); j++) {
				out.print((j != 0 ? "\t" : "") + getValueAsString(row, j));
			}
			out.println();
		}
		out.flush();
	}
	
	public boolean containsMissingValues() {
		Iterator<DataTableRow> i = iterator();
		while (i.hasNext()) {
			DataTableRow row = i.next();
			for (int j = 0; j < getNumberOfColumns(); j++) {
				if (Double.isNaN(row.getValue(j)))
					return true;
			}
		}
		return false;
	}
	
	public int getRowNumber() {
		return getNumberOfRows();
	}
	
	public int getColumnNumber() {
		return getNumberOfColumns();
	}
	
	public String getCell(int row, int column) {
		double value = getRow(row).getValue(column);
		if (isDate(column)) {
			return Tools.formatDate(new Date((long)value));
		} else if (isDateTime(column)) {
			return Tools.formatDateTime(new Date((long)value));
		} else if (isTime(column)) {
			return Tools.formatTime(new Date((long)value));
		} else if (isNominal(column)) {
			return mapIndex(column, (int)value);
		} else {
			return Tools.formatIntegerIfPossible(value);
		}
	}
	
	public void prepareReporting() {}
	public void finishReporting() {}
	
	public boolean isFirstLineHeader() { return false; }
	
	public boolean isFirstColumnHeader() { return false; }

	@Override
	public boolean isDeselected(String id) {
		return deselectionSet.contains(id);
	}
	
	@Override
	public void setSelection(Selection selection) {
		deselectionCount = 0;
		Collection<Pair<String, Range>> delimiters = selection.getDelimiters();
		Iterator<DataTableRow> i = iterator();
		deselectionSet.clear();
		while (i.hasNext()) {
			DataTableRow row = i.next();
			boolean rowSelected = true;
			for (Pair<String, Range> delimiter : delimiters) {
				int col = getColumnIndex(delimiter.getFirst());
				if (col >= 0 && col < this.getNumberOfColumns()) {
					double value = row.getValue(col);
					if (Double.isNaN(value)) {
						continue;
					}
					rowSelected &= delimiter.getSecond().contains(value);
				}
			}
			if (!rowSelected) {
				String id = row.getId();
				if (id != null) {
					deselectionSet.add(id);
					deselectionCount++;
				}
			}
		}
		fireEvent();
	}
	
	@Override
	public int getSelectionCount() {
		return getNumberOfRows() - deselectionCount;
	}
}
