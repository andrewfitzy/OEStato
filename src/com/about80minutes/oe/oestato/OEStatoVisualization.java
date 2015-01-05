package com.about80minutes.oe.oestato;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.about80minutes.oe.oestato.horizon.LinkCountView;
import com.about80minutes.oe.oestato.horizon.LinkCountViewResult;
import com.about80minutes.oe.oestato.horizon.ObjectSummary;
import com.about80minutes.util.FileChooserUtil;
import com.google.common.collect.Lists;
import com.palantir.api.horizon.v1.HorizonConnection;
import com.palantir.api.horizon.v1.common.Describables;
import com.palantir.api.horizon.v1.operation.OperationDescriptions;
import com.palantir.api.horizon.v1.view.View;
import com.palantir.api.objectexplorer.v1.UserInterfaceManager;
import com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel;
import com.palantir.api.objectexplorer.v1.vis.Visualization;
import com.palantir.api.objectexplorer.v1.vis.VisualizationArgument.VoidVisualizationArgument;
import com.palantir.api.objectexplorer.v1.vis.VisualizationFactory;
import com.palantir.api.workspace.ApplicationInterface;
import com.palantir.api.workspace.PalantirWorkspaceContext;
import com.palantir.api.workspace.applications.GraphApplicationInterface;
import com.palantir.api.workspace.applications.MapApplicationInterface;
import com.palantir.api.workspace.dataitem.DataItemGroup;
import com.palantir.api.workspace.dataitem.DataItemGroups;
import com.palantir.services.Locator;
import com.palantir.util.Locatables;

/**
 * Visualization class, this displays the results table for a Stato operation.
 */
public class OEStatoVisualization implements Visualization<VoidVisualizationArgument> {

	private static final Logger LOGGER = LogManager.getLogger(OEStatoVisualization.class);
	
	private final AnalysisTreeSetModel setModel;
	private final UserInterfaceManager uiManager;
	private PalantirWorkspaceContext context = null; 
	private final JPanel panel = new JPanel();
	private JTable table = null;
	private final OEStatoTableModel tableModel = new OEStatoTableModel();
	private final VisualizationFactory<VoidVisualizationArgument> factory;
	private LinkCountViewResult result;
	private final ValueComparator comparator = new ValueComparator();
	private ExportAction exportAllAction = null;
	private ExportSelectedAction exportSelectedAction = null;
	private AddToApplicationAction addToGraphAction = null;
	private AddToApplicationAction addToMapAction = null;
	private DrilldownAction drilldownAction = null;
	private JPopupMenu popup = null;

	/**
	 * Constructor to initialise this class
	 * 
	 * @param setModel an {@link com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel}
	 * to use in this visualization
	 * @param uiManager an {@link com.palantir.api.objectexplorer.v1.UserInterfaceManager}
	 * to use in this visualization
	 * @param factory a {@link com.palantir.api.objectexplorer.v1.vis.VisualizationFactory}
	 * which initialises this visualization
	 */
	public OEStatoVisualization(final AnalysisTreeSetModel setModel,
								   final UserInterfaceManager uiManager,
								   final VisualizationFactory<VoidVisualizationArgument> factory) {

		this.setModel = setModel;
		this.uiManager = uiManager;
		this.factory = factory;
		context = this.uiManager.getPalantirContext();

		this.initUI();
    }
	
	/**
	 * utility method for initialising the UI element.
	 */
	private void initUI() {
		panel.setLayout(new BorderLayout());
		
		exportAllAction = new ExportAction("Export All");
		exportSelectedAction = new ExportSelectedAction("Export Selected");
		
		addToGraphAction = new AddToApplicationAction("Add to Graph", GraphApplicationInterface.APPLICATION_URI);
		addToMapAction = new AddToApplicationAction("Add to Map", MapApplicationInterface.APPLICATION_URI);
		
		drilldownAction = new DrilldownAction("Drilldown");
		
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.getSelectionModel().addListSelectionListener(new RowListener());
		
		popup = new JPopupMenu();
		JMenuItem item = popup.add(drilldownAction);
		this.setMenuItemIcon(item, "/ObjectExplorer/filter.png");
		popup.addSeparator();
		item = popup.add(addToGraphAction);
		this.setMenuItemIcon(item, "/Application/Icons/AppBarIcons/graphHover.png");
		item = popup.add(addToMapAction);
		this.setMenuItemIcon(item, "/Application/Icons/AppBarIcons/mapHover.png");
		popup.addSeparator();
		item = popup.add(exportAllAction);
		this.setMenuItemIcon(item, null);
		item = popup.add(exportSelectedAction);
		this.setMenuItemIcon(item, null);
		table.setComponentPopupMenu(popup);
		
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
	}

	/**
	 * Utility method to use for setting icons on menu item options
	 * 
	 * @param item a {@link javax.swing.JMenuItem} to add an icon to
	 * @param iconURI a {@link java.lang.String} containing the icon URI
	 */
	private void setMenuItemIcon(JMenuItem item, String iconURI) {
		if(iconURI != null) { //ignore if null
			try {
				item.setIcon(new ImageIcon(ImageIO.read(OEStatoVisualization.class.getResource(iconURI)).getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
			} catch (IOException e) {
				//icon not loaded, ignore adding an icon
				LOGGER.warn("Icon not loaded: " + iconURI, e);
			}
		}
	}
	
	/**
	 * sets the initial display
	 */
	public void setInitialDisplay() {
		// Do nothing when it first displays
	}

	/**
	 * Method for producing the data for the ui
	 */
	public void generate() {
		final HorizonConnection server = setModel.getAnalysisModel().getHorizonConnection();
		View<LinkCountViewResult> view = new LinkCountView();
	    result = server.computeView(setModel.getOperation().getId(), view, false).getResult();
	}

	/**
	 * Sets the display for this visualisation.
	 */
	public void setVisualizationDisplay() {
		List<Entry<Long, ObjectSummary>> list = Lists.newArrayList(result.objectSummaries.entrySet());
		Collections.sort(list, comparator);
		tableModel.setTableData(list);
		exportSelectedAction.setEnabled(Boolean.FALSE);
	}

	/**
	 * Called when this visualisation is disposed, detroys long lived resources.
	 */
	public void dispose() {
	    //take no action here
	}

	/**
	 * Gets the display component for this visualisation
	 * 
	 * @return a {@link javax.swing.JComponent} containing the ui components
	 */
	public JComponent getDisplayComponent() {
        return panel;
    }

	/**
	 * Gets the toolbar component for this visualisation
	 * 
	 * @return a {@link javax.swing.JComponent} representing a toolbar component
	 */
	public JComponent getToolbarComponent() {
		return null;
	}

	/**
	 * Returns the factory that created this visualisation
	 * 
	 * @return a {@link com.palantir.api.objectexplorer.v1.vis.VisualizationFactory}
	 */
	public VisualizationFactory<VoidVisualizationArgument> getFactory() {
        return factory;
    }

	/**
	 * Returns the state of the visualisation. This visualisation has no state
	 * therefore returns null
	 * 
	 * @return a {@link com.palantir.api.objectexplorer.v1.vis.VisualizationArgument.VoidVisualizationArgument}
	 */
	public VoidVisualizationArgument getState() {
        return null;
    }

	/**
	 * Gets the description for the visualisation
	 * 
	 * @return a {@link java.lang.String} containing the description
	 */
	public String getDescription() {
        return "Visualization of link counts for each object";
    }

	/**
	 * Gets the name of this visualisation
	 * 
	 * @return a {@link java.lang.String} containing the visualisation name
	 */
	public String getName() {
        return "OEStato";
    }
	
	/**
	 * Utility method for processing the export action. 
	 * 
	 * @param selection an int array containing the current selection.
	 */
	private void processExport(int[] selection) {
		int selectionLength = selection.length;
		int[] convertedIndexes = new int[selectionLength];
		for(int i = 0;i < selectionLength;i++) {
			convertedIndexes[i] = table.convertRowIndexToModel(selection[i]);
		}
		
		File file = FileChooserUtil.showDialog(getDisplayComponent(), "Select File",
				"Select",
				"Select a file",
				's',
				null);
		if(file != null) {
			OutputStream output = null;
			try {
				output = new FileOutputStream(file);
				tableModel.toCSV(selection, output);
			} catch (FileNotFoundException e) {
				LOGGER.error(String.format("file: %s not found",file.getName()), e);
			} finally {
				if(output != null) {
					IOUtils.closeQuietly(output);
				}
			}
		}
	}
	
	/**
	 * Utility class used for sorting the values in a collection. This class is
	 * used to sort Map entries which map a Long key to an ObjectSummary value
	 */
	private class ValueComparator implements Comparator<Entry<Long, ObjectSummary>> {
		
		/**
		 * Implementation of the compare method. This returns the results in
		 * descending order based on link count of ObjectSummary.
		 * 
		 * @param val01 an {@link java.util.Map.Entry} which maps a {@link java.lang.Long}
		 * key to an {@link com.about80minutes.oe.oestato.horizon.ObjectSummary} value
		 * @param val02 an {@link java.util.Map.Entry} which maps a {@link java.lang.Long}
		 * key to an {@link com.about80minutes.oe.oestato.horizon.ObjectSummary} value
		 * 
		 * @return an int containing the result of the comparison - this ensures
		 * descending order
		 */
		public int compare(Entry<Long, ObjectSummary> val01, Entry<Long, ObjectSummary> val02) {
			int result = 0;
			//note the - (minus) in front of the result, little trick to order descending
			result = -val01.getValue().linkCount.compareTo(val02.getValue().linkCount);
			if(result == 0) {
				result = val01.getValue().objectName.compareToIgnoreCase(val02.getValue().objectName);
			}
			return result;
		}
	}
	
	/**
	 * Action for exporting some data from the helper
	 */
	@SuppressWarnings("serial")
	private class ExportAction extends AbstractAction {
		
		/**
		 * Constructor for this action
		 * 
		 * @param title a {@link java.lang.String} to use as the action title
		 */
		public ExportAction(String title) {
			super(title);
		}

		/**
		 * Completes the actions required by this action
		 * 
		 * @param an {@link java.awt.event.ActionEvent} to react to 
		 */
		public void actionPerformed(ActionEvent event) {
			int tableSize = OEStatoVisualization.this.table.getRowCount();
			int[] rows = new int[tableSize];
			for(int i = 0;i < tableSize;i++) {
				rows[i] = i;
			}
			OEStatoVisualization.this.processExport(rows);
		}
	}
	
	/**
	 * Action for exporting some selected data from the helper
	 */
	@SuppressWarnings("serial")
	private class ExportSelectedAction extends AbstractAction {
		
		/**
		 * Constructor for this action
		 * 
		 * @param title a {@link java.lang.String} to use as the action title
		 */
		public ExportSelectedAction(String title) {
			super(title);
		}

		/**
		 * Completes the actions required by this action
		 * 
		 * @param an {@link java.awt.event.ActionEvent} to react to 
		 */
		public void actionPerformed(ActionEvent event) {
			int[] rows = OEStatoVisualization.this.table.getSelectedRows();
			OEStatoVisualization.this.processExport(rows);
		}
	}

	/**
	 * Action for drilling down on some data in the helper
	 */
	@SuppressWarnings("serial")
	private class DrilldownAction extends AbstractAction {
		
		/**
		 * Constructor for this action
		 * 
		 * @param title a {@link java.lang.String} to use as the action title
		 */
		public DrilldownAction(String title) {
			super(title);
		}

		/**
		 * Completes the actions required by this action
		 * 
		 * @param an {@link java.awt.event.ActionEvent} to react to 
		 */
		public void actionPerformed(ActionEvent event) {
			int[] rows = OEStatoVisualization.this.table.getSelectedRows();
			int selectedrowsLength = rows.length;
			List<Long> objects = Lists.newArrayList();
			if(selectedrowsLength > 0) {
				for(int i = 0;i < selectedrowsLength; i++) {
					Long objId = Long.parseLong((String)OEStatoVisualization.this.tableModel.getValueAt(rows[i], 0));
					objects.add(objId);
				}
			}
			
			OEStatoVisualization.this.uiManager.newSetCreator()
											    .setParent(OEStatoVisualization.this.setModel)
											    .setOperationDescription(OperationDescriptions.newObjectIdsOperationDescription(Describables.create("OEStato Drilldown"), objects))
											    .setSourceVisualization(OEStatoVisualization.this)
											    .create();
		}
	}
	
	/**
	 * Action for adding some items to another Palantir application
	 */
	@SuppressWarnings("serial")
	private class AddToApplicationAction extends AbstractAction {
		
		private final String application;
		
		/**
		 * Constructor for this action
		 * 
		 * @param title a {@link java.lang.String} to use as the action title
		 */
		public AddToApplicationAction(String title, String application) {
			super(title);
			this.application = application;
		}

		/**
		 * Completes the actions required by this action
		 * 
		 * @param an {@link java.awt.event.ActionEvent} to react to 
		 */
		public void actionPerformed(ActionEvent event) {
			int[] rows = OEStatoVisualization.this.table.getSelectedRows();
			int selectedrowsLength = rows.length;
			if(selectedrowsLength > 0) {
				List<Long> objects = Lists.newArrayList();
				for(int i = 0;i < selectedrowsLength; i++) {
					Long objId = Long.parseLong((String)OEStatoVisualization.this.tableModel.getValueAt(rows[i], 0));
					objects.add(objId);
				}
				context.getMonitoredExecutorService().execute(new EntityAcquisitionWorker(this.application, objects));
			}
		}
	}
	
	/**
	 * Swing worker to load the objects to an application.
	 */
	private class EntityAcquisitionWorker extends SwingWorker<Void, Void> {

		private final Collection<Long> items;
		private final String appUri;

		/**
		 * Constructor for this class
		 *
		 * @param items a {@link java.util.List} of {@link com.palantir.services.Locator}
		 * objects
		 */
		public EntityAcquisitionWorker(String appUri, List<Long> items) {
			this.items = items;
			this.appUri = appUri;
		}

		/**
		 * Process work in background
		 *
		 * @return a {@link java.lang.Void} object (null)
		 */
		protected Void doInBackground() {
			Set<Locator> locators = Locatables.getLocatorSetFromIds(this.items, context.getInvestigationManager().getInvestigation().getRealm().getRealmId());
			DataItemGroup dataItemGroup = DataItemGroups.builder().addObjectLocators(locators).build();
			ApplicationInterface app = context.getApplication(this.appUri);
			app.getInteractionHandler().addGroup(dataItemGroup);
			context.signalApplicationIcon(app);
			return null;
		}

		/**
		 * Handles the completion of this job
		 */
		protected void done() {
			try {
				this.get();
			} catch (InterruptedException e) {
				LOGGER.error("Error loading objects: Job thread interrupted", e);
			} catch (ExecutionException e) {
				LOGGER.error("Error loading objects: Execution Exception", e);
			}
		}
	}
	
	/**
	 * Listens for row selections, if the number of items selected is > 0 then
	 * allow the user to explore selected, else hide that option
	 */
	private class RowListener implements ListSelectionListener {
        
		/**
		 * React to changes of value in the source table
		 * 
		 * @param event a {@link javax.swing.event.ListSelectionEvent} to react
		 * to 
		 */
		public void valueChanged(ListSelectionEvent event) {
            if(!event.getValueIsAdjusting()) {
            	int[] selectedRows = OEStatoVisualization.this.table.getSelectedRows();
        		if(selectedRows.length <= 0) {
        			OEStatoVisualization.this.exportSelectedAction.setEnabled(Boolean.FALSE);
        		} else {
        			OEStatoVisualization.this.exportSelectedAction.setEnabled(Boolean.TRUE);
        		}
            }
        }
    }
}
