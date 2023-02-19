package init.sprite.UI;

import java.io.IOException;

import init.race.Race;
import util.colors.GCOLOR;

public class UI {

	private static UIDecor decor;
	private static UIPanels panels;
	private static UIFonts fonts;

	public static void init(Race race) throws IOException {
		GCOLOR.read();
		fonts = new UIFonts(race);
		panels = new UIPanels(race);
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
