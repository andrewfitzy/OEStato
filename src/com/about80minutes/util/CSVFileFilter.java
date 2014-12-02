package com.about80minutes.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Implementation of FileFilter, this only allows .csv files to be selected.
 */
public class CSVFileFilter extends FileFilter {

	/**
     * Whether the given file is accepted by this filter.
     *
     * @param file a {@link java.io.File} which is to be tested
     *
     * @return true if accepted false if rejected.
     */
	@Override
	public boolean accept(File file) {
		boolean accepted = false;
		if(file.isDirectory() || file.getAbsolutePath().endsWith(".csv")
							  || file.getAbsolutePath().endsWith(".CSV")) {
			accepted = true;
		}
		return accepted;
	}

	/**
     * The description of this filter.
     *
	 * @return a {@link java.lang.String} description of this filter
     */
	@Override
	public String getDescription() {
		return "(.csv) Comma Delimited Value Files";
	}
}
