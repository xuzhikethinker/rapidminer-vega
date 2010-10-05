/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2010 by Rapid-I and the contributors
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

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.dialogs.wizards.WizardStep;
import com.rapidminer.operator.nio.model.ExcelResultSetConfiguration;

/**
 * This is a pane, showing the contents of a complete excel workbook. There's one tab per sheet.
 * 
 * @author Tobias Malbrecht, Sebastian Land
 */
public class ExcelWorkbookPane extends JPanel {

	public static class ExcelWorkbookSelection {
	
	private int sheetIndex;
		private int columnIndexStart;
		private int rowIndexStart;
		private int columnIndexEnd;
		private int rowIndexEnd;

		public ExcelWorkbookSelection(int sheetIndex, int columnIndexStart, int rowIndexStart, int columnIndexEnd, int rowIndexEnd) {
			this.sheetIndex = sheetIndex;
			this.columnIndexStart = columnIndexStart;
			this.rowIndexStart = rowIndexStart;
			this.columnIndexEnd = columnIndexEnd;
			this.rowIndexEnd = rowIndexEnd;
		}

		@Override
		public String toString() {
			return sheetIndex + ": " + columnIndexStart + ":" + rowIndexStart + " - " + columnIndexEnd + ":" + rowIndexEnd;
		}

		public int getSheetIndex() {
			return sheetIndex;
		}

		public int getColumnIndexEnd() {
			return columnIndexEnd;
		}

		public int getColumnIndexStart() {
			return columnIndexStart;
		}

		public int getRowIndexEnd() {
			return rowIndexEnd;
		}

		public int getRowIndexStart() {
			return rowIndexStart;
		}

		public int getSelectionWidth() {
			return columnIndexEnd - columnIndexStart + 1;
		}

		public int getSelectionHeight() {
			return rowIndexEnd - rowIndexStart + 1;
		}
	}

	private static final long serialVersionUID = 9179757216097316344L;

	private ExcelResultSetConfiguration configuration;

	private ExtendedJTabbedPane sheetsPane;
	private ExtendedJTable[] tables;
	private ExcelWorkbookSelection selectedView;

	public ExcelWorkbookPane(WizardStep wizardStep, ExcelResultSetConfiguration configuration) {
		super();
		this.configuration = configuration;

		// creating gui
		sheetsPane = new ExtendedJTabbedPane();
		sheetsPane.setBorder(null);
		this.setLayout(new BorderLayout());
		this.add(sheetsPane);
	}

	public void loadWorkbook() { 
		// add dummy
		sheetsPane.removeAll();
		JPanel dummy = new JPanel();
		dummy.add(new ResourceLabel("loading_excel_sheets"));
		sheetsPane.addTab("Pending...", dummy);

		new ProgressThread("load_workbook", false) {
			@Override
			public void run() {
				// initializing progress
				getProgressListener().setTotal(100);
				getProgressListener().setCompleted(10);

				// loading workbook if necessary
				try {
					final Workbook finalWorkbook = configuration.getWorkbook();

					// now add everything to gui 
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							tables = new ExtendedJTable[finalWorkbook.getNumberOfSheets()];

							String[] sheetNames = finalWorkbook.getSheetNames();
							for (int sheetIndex = 0; sheetIndex < finalWorkbook.getNumberOfSheets(); sheetIndex++) {
								ExcelSheetTableModel sheetModel = new ExcelSheetTableModel(finalWorkbook.getSheet(sheetIndex));
								tables[sheetIndex] = new ExtendedJTable(sheetModel, false, false);
								tables[sheetIndex].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
								tables[sheetIndex].setBorder(null);

								// momentary disable selection in tables
								tables[sheetIndex].setRowSelectionAllowed(false);
								tables[sheetIndex].setColumnSelectionAllowed(false);
								tables[sheetIndex].setCellSelectionEnabled(true);

								// add table to gui
								ExtendedJScrollPane pane = new ExtendedJScrollPane(tables[sheetIndex]);
								pane.setBorder(null);
								if (sheetIndex == 0) {
									sheetsPane.removeAll();
								}
								sheetsPane.addTab(sheetNames[sheetIndex], pane);
								
								ExcelWorkbookSelection selection = new ExcelWorkbookSelection(configuration.getSheet(), 
										configuration.getColumnOffset(), configuration.getRowOffset(), 
										configuration.getColumnLast(), configuration.getRowLast());
								
								if ((configuration.getColumnOffset() > 0) || ((configuration.getRowOffset() > 0))) {
									setSelection(selection);
								}
							}
						}
					});
				} catch (BiffException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					getProgressListener().complete();
				}
			}
		}.start();
	}

	public void setSelection(ExcelWorkbookSelection selection) {
		final int sheetIndex = selection.getSheetIndex();
		sheetsPane.setSelectedIndex(sheetIndex);
		tables[sheetIndex].clearSelection();
		tables[sheetIndex].setColumnSelectionInterval(selection.getColumnIndexStart(), selection.getColumnIndexEnd());
		tables[sheetIndex].setRowSelectionInterval(selection.getRowIndexStart(), selection.getRowIndexEnd());		
	}
	
	public ExcelWorkbookSelection getSelection() {
		if (selectedView == null) {
			int sheetIndex = sheetsPane.getSelectedIndex();
			int columnIndexStart = tables[sheetIndex].getSelectedColumn();
			int rowIndexStart = tables[sheetIndex].getSelectedRow();
			int columnIndexEnd = columnIndexStart + tables[sheetIndex].getSelectedColumnCount() - 1;
			int rowIndexEnd = rowIndexStart + tables[sheetIndex].getSelectedRowCount() - 1;
			if (columnIndexStart == -1) {
				// then use complete selected table
				return new ExcelWorkbookSelection(sheetIndex, 0, 0, tables[sheetIndex].getColumnCount() - 1, tables[sheetIndex].getRowCount() - 1);
			} else {
				return new ExcelWorkbookSelection(sheetIndex, columnIndexStart, rowIndexStart, columnIndexEnd, rowIndexEnd);
			}
		} else {
			int sheetIndex = selectedView.getSheetIndex();
			int columnIndexStart = tables[0].getSelectedColumn() + selectedView.getColumnIndexStart();
			int rowIndexStart = tables[0].getSelectedRow() + selectedView.getRowIndexStart();
			int columnIndexEnd = columnIndexStart + tables[0].getSelectedColumnCount() - 1 + selectedView.getColumnIndexStart();
			int rowIndexEnd = rowIndexStart + tables[0].getSelectedRowCount() - 1 + selectedView.getRowIndexStart();
			if (columnIndexStart == -1) {
				// then use complete selected table
				return new ExcelWorkbookSelection(sheetIndex, selectedView.getColumnIndexStart(), selectedView.getRowIndexStart(), selectedView.getColumnIndexEnd(), selectedView.getRowIndexEnd());
			} else {
				return new ExcelWorkbookSelection(sheetIndex, columnIndexStart, rowIndexStart, columnIndexEnd, rowIndexEnd);
			}
		}
	}
}
