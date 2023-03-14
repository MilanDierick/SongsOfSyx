package init.sprite.UI;

import java.io.IOException;

import util.colors.GCOLOR;

public class UI {

	private static UIDecor decor;
	private static UIPanels panels;
	private static UIFonts fonts;

	public static void init() throws IOException {
		GCOLOR.read();
		fonts = new UIFonts();
		panels = new UIPanels();
		decor = new UIDecor();
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

}
