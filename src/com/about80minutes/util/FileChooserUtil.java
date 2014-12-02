package com.about80minutes.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * Utility for displaying a customised JFileChooser
 */
public class FileChooserUtil {
	
	private static final JFileChooser FILE_CHOOSER = new JFileChooser();
	
	/**
	 * Utility method to show a {@link javax.swing.JFileChooser}
	 *
	 * @param parent a {@link java.awt.Component} which is the parent of the chooser
	 * @param dialogTitle a {@link java.lang.String} containing the title of the dialog
	 * @param approveButtonText a {@link java.lang.String} containing the approval button text
	 * @param approveButtonToolTip a {@link java.lang.String} containing the tooltip text
	 * @param approveButtonMnemonic a {@link char} containing the approval button mnemonic
	 * @param file a {@link java.io.File} to use for initialising the display, if this is
	 * null the users home will be displayed by default.
	 *
	 * @return a link to a selected {@link java.io.File}
	 */
	public static File showDialog(Component parent, String dialogTitle,
							String approveButtonText,
							String approveButtonToolTip,
							char approveButtonMnemonic,
							File file) {
		FILE_CHOOSER.setDialogTitle(dialogTitle);
		FILE_CHOOSER.setFileFilter(new CSVFileFilter());
		FILE_CHOOSER.setApproveButtonText(approveButtonText);
		FILE_CHOOSER.setApproveButtonToolTipText(approveButtonToolTip);
		FILE_CHOOSER.setApproveButtonMnemonic(approveButtonMnemonic);
		FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FILE_CHOOSER.rescanCurrentDirectory();
		FILE_CHOOSER.setSelectedFile(file);

		int result = FILE_CHOOSER.showDialog(parent,null);
		return (result == JFileChooser.APPROVE_OPTION) ? FILE_CHOOSER.getSelectedFile() : null;
	}
}
