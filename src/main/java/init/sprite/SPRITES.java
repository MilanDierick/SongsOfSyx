package init.sprite;

import java.io.IOException;

import init.RES;
import init.sprite.UI.*;
import init.sprite.game.GameSheets;
import snake2d.CORE;
import snake2d.util.color.ColorImp;

public class SPRITES {

	private static SPRITES self;
	private final Settlement settlement;
	private final UIConses panelsOverlays;
	private final Textures textures;

	private final LoadScreen loadScreen;
	private final UISpecials specials;
	private final UIArmyCard armyCard;
	private final GameSheets game;
	
	public static SPRITES make(RES.Data data) throws IOException {
		return new SPRITES(data);
	}
	
	public SPRITES(RES.Data data) throws IOException{
		

		
		self = this;
		settlement = new Settlement();
		CORE.checkIn();

		CORE.checkIn();
		
		panelsOverlays = new UIConses();
		loadScreen = new LoadScreen();
		CORE.checkIn();
		specials = new UISpecials();
		CORE.checkIn();
		armyCard = new UIArmyCard();
		CORE.checkIn();
		textures = new Textures();
		game = new GameSheets();
		
	}
	
	public final static class COLOR_REMOVE{
		
//		public final static COLOR RED100 = new ColorImp(127, 29, 29);
//		public final static COLOR RED75 = RED100.shade(0.75);
//		public final static COLOR RED50 = RED100.shade(0.5);
//		
//		public final static COLOR BLUE100 = new ColorImp(29, 29, 127);
//		public final static COLOR BLUE75 = BLUE100.shade(0.75);
//		public final static COLOR BLUE50 = BLUE100.shade(0.5);
		

		

		

		
		public static void bad2Good(ColorImp c, double d) {
			if (d < 0)
				d = 0;
			if (d > 1)
				d = 1;
			double r = (d > 0.5) ? (1.0-(d-0.5)*2) : 1;
			double g = (d < 0.5) ? d*2 : 1;
			c.set(30+(int)(70*r), 30+(int)(70*g), 30);
		}
	}
	
	
	public static Icons icons(){
		return UI.icons();
	}
	
	public static UIConses cons(){
		return self.panelsOverlays;
	}
	
	public static Settlement sett() {
		return self.settlement;
	}
	
	public static LoadScreen loadScreen() {
		return self.loadScreen;
	}
	
	public static UISpecials specials() {
		return self.specials;
	}
	
	public static UIArmyCard armyCard() {
		return self.armyCard;
	}
	
	public static Textures textures() {
		return self.textures;
	}
	
	public static GameSheets GAME() {
		return self.game;
	}
	
	public class Settlement{

		public final SettlementMap map = new SettlementMap();

		private Settlement() throws IOException {

		}

	}
	
}
