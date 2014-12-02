package com.about80minutes.oe.oestato;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import jxl.common.Logger;

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

	private static final Logger LOGGER = Logger.getLogger(OEStatoMenuItem.class);

	/**
	 * Constructor, initialises this class. Simply calls through to super.
	 */
    public OEStatoMenuItem() {
		super("OEStato", "OEStato", "Shows some stats about the current set",
			  "com.about80minutes.oe.oestato.OEStatoMenuItem",
			  "com.about80minutes.oe.oestato.images.up",
			  "com.about80minutes.oe.oestato.images.active",
			  "com.about80minutes.oe.oestato.images.pressed",
			  "com.about80minutes.oe.oestato.images.disabled");
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
		ImageRegistry reg = context.getUserInterfaceManager().getImageRegistry();
		reg.register("com.about80minutes.oe.oestato.images.up", getImage("/statoUp.png"));
		reg.register("com.about80minutes.oe.oestato.images.active", getImage("/statoHoverActive.png"));
		reg.register("com.about80minutes.oe.oestato.images.pressed", getImage("/statoPressed.png"));
		reg.register("com.about80minutes.oe.oestato.images.disabled", getImage("/statoDisabled.png"));
		return OperationsMenuItemConfigurations.create(getDescription());
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
