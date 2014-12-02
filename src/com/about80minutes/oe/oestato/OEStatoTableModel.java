package com.about80minutes.oe.oestato;

import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.csv.CSVPrinter;
import org.apache.hadoop.thirdparty.guava.common.collect.Lists;

import com.about80minutes.oe.oestato.horizon.ObjectSummary;

/**
 * Customised model used by the results JTable
 */
@SuppressWarnings("serial")
public class OEStatoTableModel extends AbstractTableModel {
	
	private static final String[] COLUMN_NAMES = new String[]{"ID", "Title", "URI", "Link Count"};
	private List<Entry<Long, ObjectSummary>> valueList = Lists.newArrayList();
	
	/**
	 * Sets the table data value
	 * 
	 * @param data a {@link java.util.List} of {@link java.util.Map.Entry} items
	 * mapping {@link java.lang.Long} to {@link com.about80minutes.oe.oestato.horizon.ObjectSummary}
	 */
	public void setTableData(List<Entry<Long, ObjectSummary>> data) {
		this.valueList = data;
		this.fireTableDataChanged();
	}
	
	/**
	 * Returns the name of the given column
	 * 
	 * @param colNum an int containing the column number
	 */
	public String getColumnName(int colNum) {
	    return COLUMN_NAMES[colNum];
	}

	/**
	 * Gets a count of the number of columns
	 * 
	 * @return an int containing the number of columns
	 */
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	/**
	 * Gets a count of the number of rows
	 * 
	 * @return an int containing the number of rows
	 */
	public int getRowCount() {
		return this.valueList.size();
	}

	/**
	 * Gets the cell value at a given co-ordinate
	 * 
	 * @param row an int containing the row number
	 * @param column an int containing the column number 
	 * 
	 * @return a {@link java.lang.Object} containing the cell value 
	 */
	public Object getValueAt(int row, int column) {
		String value = "";
		Entry<Long, ObjectSummary> tmpEntry = valueList.get(row);
		switch(column) {
			case 0: //"ID"
				value = tmpEntry.getKey().toString();
				break;
			case 1: //"Title"
				value = tmpEntry.getValue().objectName;
				break;
			case 2: //"URI"
				value = tmpEntry.getValue().objectURI;
				break;
			case 3://"Count"
				value = tmpEntry.getValue().linkCount.toString();
				break;
			default:
				break;
		}
		return value;
	}

	/**
	 * Prints the table data to the given output stream. This method does not
	 * close the stream after processing
	 * 
	 * @param rows an int array containing the idexes of the selected rows 
	 * @param stream a {@link java.io.OutputStream} to write the data to
	 */
	public void toCSV(int[] rows, OutputStream stream) {
		CSVPrinter printer = new CSVPrinter(stream);
		
		//print header first
		printer.println(COLUMN_NAMES);

		//then print row data
		String[] tmpRow = null;
		
		for(int i = 0;i < rows.length;i++) {
			tmpRow = new String[COLUMN_NAMES.length];
			for(int j = 0;j < COLUMN_NAMES.length;j++) {
				String tmpCellVal = (String) this.getValueAt(rows[i], j);
				if(tmpCellVal == null) {
					tmpCellVal = "";
				}
				tmpRow[j] = tmpCellVal;
			}
			printer.println(tmpRow);
		}
	}
}
