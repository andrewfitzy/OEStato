package com.about80minutes.palantir.oe.oestato;

import com.palantir.api.objectexplorer.v1.UserInterfaceManager;
import com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel;
import com.palantir.api.objectexplorer.v1.vis.Visualization;
import com.palantir.api.objectexplorer.v1.vis.VisualizationFactory;
import com.palantir.api.objectexplorer.v1.vis.VisualizationArgument.VoidVisualizationArgument;

/**
 * Factory for this visualisation
 */
public class OEStatoVisualizationFactory implements VisualizationFactory<VoidVisualizationArgument> {

    /**
     * Get the visualisation element of this OE plugin.
     * 
     * @param setModel an {@link com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel}
     * for use by this plugin
     * @param uiManager a {@link com.palantir.api.objectexplorer.v1.UserInterfaceManager}
     * for use by this plugin
     * @param args a {@link com.palantir.api.objectexplorer.v1.vis.VisualizationArgument.VoidVisualizationArgument}
     * for use by this plugin
     */
	public Visualization<VoidVisualizationArgument> get(AnalysisTreeSetModel setModel, UserInterfaceManager uiManager, VoidVisualizationArgument args) {
		return new OEStatoVisualization(setModel, uiManager, this);
	}

	/**
	 * Method to return the URI of this plugin
	 * 
	 * @return a {@link java.lang.String} containing the URI for this plugin
	 */
	public String getUri() {
		return "com.about80minutes.palantir.oe.oestato.OEStatoVisualizationFactory";
	}

	/**
	 * No arguments for this plugin therefore return a Void argument
	 * 
	 * @return a {@link java.lang.Class} object for void vizualization argument
	 * {@link com.palantir.api.objectexplorer.v1.vis.VisualizationArgument.VoidVisualizationArgument}
	 */
	public Class<VoidVisualizationArgument> getArgumentClass() {
		return VoidVisualizationArgument.class;
	}
}