package init.sprite.UI;

import java.io.IOException;

import util.colors.GCOLOR;

public class UI {

	private static UIDecor decor;
	private static UIPanels panels;
	private static UIFonts fonts;
	private static Icons icons;

	public static void init() throws IOException {
		GCOLOR.read();
		fonts = new UIFonts();
		panels = new UIPanels();
		decor = new UIDecor();
		icons = new Icons();
	}
	
	public static UIFonts FONT() {
		return fonts;
	}
	
	public static UIPanels PANEL() {
		return panels;
	}
	
	public static UIDecor decor() {
		return decor;
	}
	
	public static Icons icons() {
		return icons;
	}

}
