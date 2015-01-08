package com.about80minutes.palantir.oe.oestato;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import jxl.common.Logger;

import com.palantir.api.histogram.HistogramBucket;
import com.palantir.api.histogram.HistogramModel;
import com.palantir.api.horizon.v1.object.HComponentBaseType;
import com.palantir.api.horizon.v1.object.HComponentType;
import com.palantir.api.objectexplorer.v1.UserInterfaceManager;
import com.palantir.api.objectexplorer.v1.image.ImageRegistry;
import com.palantir.api.objectexplorer.v1.menu.AbstractOperationsMenuItem;
import com.palantir.api.objectexplorer.v1.menu.MenuActionEvent;
import com.palantir.api.objectexplorer.v1.menu.MenuContext;
import com.palantir.api.objectexplorer.v1.menu.OperationsMenuItemConfiguration;
import com.palantir.api.objectexplorer.v1.menu.OperationsMenuItemConfigurations;
import com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel;

/**
 * Menu item for display on the Object Explorer dashboard
 */
public class OEStatoMenuItem extends AbstractOperationsMenuItem {

	private static final String OESTATO_DISABLED = "com.about80minutes.palantir.oe.oestato.images.disabled";
	private static final String OESTATO_PRESSED = "com.about80minutes.palantir.oe.oestato.images.pressed";
	private static final String OESTATO_ACTIVE = "com.about80minutes.palantir.oe.oestato.images.active";
	private static final String OESTATO_UP = "com.about80minutes.palantir.oe.oestato.images.up";

	private static final Logger LOGGER = Logger.getLogger(OEStatoMenuItem.class);
	
	private Boolean firstRun = Boolean.TRUE;

	/**
	 * Constructor, initialises this class. Simply calls through to super.
	 */
    public OEStatoMenuItem() {
		super("OEStato", "OEStato", "Shows some stats about the current set",
			  "com.about80minutes.palantir.oe.oestato.OEStatoMenuItem.OEStatoMenuItem()",
			  OESTATO_UP, OESTATO_ACTIVE, OESTATO_PRESSED, OESTATO_DISABLED);
	}

	/**
	 * Gets the configuration for this menu item.
	 * 
	 * @param context a {@link com.palantir.api.objectexplorer.v1.menu.MenuContext}
	 * to use during configuration
	 * 
	 * @return a {@link com.palantir.api.objectexplorer.v1.menu.OperationsMenuItemConfiguration}
	 * for this menu item.
	 */
	public OperationsMenuItemConfiguration getConfigurationFor(MenuContext context) {
		if(firstRun) {
			ImageRegistry reg = context.getUserInterfaceManager().getImageRegistry();
			reg.register(OESTATO_UP, getImage("/statoUp.png"));
			reg.register(OESTATO_ACTIVE, getImage("/statoHoverActive.png"));
			reg.register(OESTATO_PRESSED, getImage("/statoPressed.png"));
			reg.register(OESTATO_DISABLED, getImage("/statoDisabled.png"));
			firstRun = Boolean.FALSE;
		}
		AnalysisTreeSetModel setModel = context.getSelectedSet();
	    return OperationsMenuItemConfigurations.create(containsLinkedObjects(setModel), true, false, getDescription(), null);
	}

	/**
	 * Utility method for checking if the selected set has at least 1 link in it
	 * 
	 * @param setModel a {@link com.palantir.api.objectexplorer.v1.model.AnalysisTreeSetModel}
	 * to process
	 * 
	 * @return a boolean indicating the presence of at least 1 link
	 */
	private boolean containsLinkedObjects(AnalysisTreeSetModel setModel) {
		Boolean containsLinks = Boolean.FALSE;
		
		HistogramModel<HComponentType<?>> propertyHistogramModel = setModel.getPropertyHistogramModel();
		for(HistogramBucket<HComponentType<?>> bucket : propertyHistogramModel.getBuckets()) {
			HComponentType<?> propertyType = bucket.getFeature();
			HComponentBaseType bt = propertyType.getBaseType();
			if(bt.compareTo(HComponentBaseType.LINK) == 0) {
				containsLinks = Boolean.TRUE;
				break;
			}
		}
	    return containsLinks;
	}

	/**
	 * Reacts to the Operations Menu Item being pressed
	 * 
	 * @param event a {@link com.palantir.api.objectexplorer.v1.menu.MenuActionEvent}
	 * to react to.
	 */
	public void actionPerformed(MenuActionEvent event) {
		UserInterfaceManager uiManager = event.getMenuContext().getUserInterfaceManager();
		AnalysisTreeSetModel setModel = event.getMenuContext().getSelectedSet();
		uiManager.showVisualization(setModel, new OEStatoVisualizationFactory(), null);
	}
	
	/**
	 * Utility method for returning an Image from a path. If an exception is
	 * thrown during read of the image it will be caught and null returned.
	 * 
	 * @param imagePath a {@link java.lang.String} which contains the location
	 * of the image file
	 * 
	 * @return an {@link java.awt.Image} containing the image data.
	 */
	private Image getImage(String imagePath) {
		Image image = null;
		try {
			image = ImageIO.read(OEStatoMenuItem.class.getResource(imagePath));
		} catch (IOException e) {
			LOGGER.warn(String.format("Image not found %s", imagePath), e);
		}
		return image;
	}
}
