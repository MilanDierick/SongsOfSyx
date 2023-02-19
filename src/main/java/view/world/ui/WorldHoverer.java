package view.world.ui;

import game.GAME;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.dic.*;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.main.VIEW;
import view.world.ui.army.DivCard;
import world.army.WARMYD;
import world.army.WDIV;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.regions.RegionTaxes.RegionIndustry;
import world.map.regions.RegionTaxes.RegionResource;

public class WorldHoverer {

	private static final DivCardsR cards = new DivCardsR();
	private static final DivCardsA cardsA = new DivCardsA();
	
	private WorldHoverer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void hover(GUI_BOX box, Region r) {
		GBox b = (GBox) box;
		b.title(r.name());
		
		if (r.faction() == FACTIONS.player()) {
			
			pop(b, r);
			
			b.textLL(REGIOND.OWNER().adminPoints().info().name);
			b.add(GFORMAT.i(b.text(), REGIOND.OWNER().adminPoints().get(r)));
			b.NL();
			
			b.textLL(REGIOND.MILITARY().soldiers.info().name);
			b.add(GFORMAT.i(b.text(), REGIOND.MILITARY().soldiers.get(r)));
			b.NL();
			b.add(cards.get(r));
			
			
			b.NL();
			
			b.textLL(REGIOND.OWNER().loyalty_current.info().name);
			b.add(GFORMAT.perc(b.text(), REGIOND.OWNER().loyalty_current.getD(r)));
			b.add(SPRITES.icons().s.arrow_right);
			b.add(GFORMAT.perc(b.text(), REGIOND.OWNER().loyalty_target.getD(r)));
			
			b.NL();
			b.textLL(REGIOND.CIVIC().health.info().name);
			b.add(GFORMAT.perc(b.text(), REGIOND.CIVIC().health.getD(r)));
			b.add(SPRITES.icons().s.arrow_right);
			b.add(GFORMAT.perc(b.text(), REGIOND.CIVIC().health_target.getD(r)));
			
			b.NL();
			b.textLL(REGIOND.CIVIC().knowledge.info().name);
			b.add(GFORMAT.i(b.text(), (int)REGIOND.CIVIC().knowledge.getD(r)));
			b.add(SPRITES.icons().s.arrow_right);
			b.add(GFORMAT.i(b.text(), (int)REGIOND.CIVIC().knowledge.getD(r)));
			
			b.NL(8);
			b.textLL(DicRes.¤¤Taxes);
			b.NL();
			{
				int t = 0;
				for (RegionResource re : REGIOND.RES().res) {
					if (re.output_target.getD(r) > 0) {
						b.tab(t*3);
						b.add(re.resource.icon());
						b.add(GFORMAT.i(b.text(), (int)re.output_target.getD(r)));
						t++;
						if (t >= 6) {
							t = 0;
							b.NL();
						}
					}
					
				}
			}
			
		}else {
			
			if (r.faction() != null) {
				b.add(r.faction().banner().HUGE);
				b.add(b.text().color(r.faction().banner().colorFG()).add(r.faction().appearence().name()));
			}else {
				b.text(DicGeo.¤¤NoRuler);
			}
			b.NL();
			pop(b, r);
			
			b.textLL(REGIOND.MILITARY().soldiers.info().name);
			b.add(GFORMAT.i(b.text(), REGIOND.MILITARY().soldiers.get(r)));
			b.NL();
			b.add(cards.get(r));
			
			
		}
		
		b.NL(8);
		
		{
			int t = 0;
			for (RegionIndustry re : REGIOND.RES().all) {
				if (re.prospect.getD(r) > 0.3) {
					b.tab(t*3);
					b.add(re.industry.blue.iconBig());
					b.add(GFORMAT.perc(b.text(), re.prospect.getD(r)));
					t++;
					if (t >= 6) {
						t = 0;
						b.NL();
					}
				}
				
			}
		}
	}
	
	private static class DivCardsR implements SPRITE {

		private Region region;
		
		public SPRITE get(Region region) {
			this.region = region;
			return this;
		}
		
		@Override
		public int width() {
			return DivCard.WIDTH*REGIOND.MILITARY().divisions(region).size();
		}

		@Override
		public int height() {
			return DivCard.HEIGHT;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int i = 0;
			for (WDIV d : REGIOND.MILITARY().divisions(region)) {
				
				VIEW.world().UI.armies.divCard.render(r, X1+i*DivCard.WIDTH, Y1, d, true, false, false);
				i++;
			}
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private static class DivCardsA implements SPRITE {

		private WArmy army;
		
		public SPRITE get(WArmy a) {
			this.army = a;
			return this;
		}
		
		@Override
		public int width() {
			return DivCard.WIDTH*12;
		}

		@Override
		public int height() {
			return CLAMP.i((int) Math.ceil(army.divs().size()/12.0), 0, 8)*DivCard.HEIGHT;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			for (int i = 0; i < army.divs().size() && i < 12*8; i++) {
				WDIV d = army.divs().get(i);
				VIEW.world().UI.armies.divCard.render(r, X1+(i%12)*DivCard.WIDTH, Y1+(DivCard.HEIGHT*(i/12)), d, true, false, false);
			}
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	private static void pop(GBox b, Region r) {
		b.textLL(REGIOND.POP().total().info().name);
		b.add(GFORMAT.i(b.text(), REGIOND.POP().total().get(r)));
		b.NL();
		{
			int t = 0;
			for (Race ra : RACES.all()) {
				b.tab(t*3);
				b.add(ra.appearance().icon);
				b.add(GFORMAT.i(b.text(), REGIOND.RACE(ra).population.get(r)));
				t++;
				if (t >= 6) {
					t = 0;
					b.NL();
				}
			}
		}
		b.NL(8);
	}
	
	public static void hover(GUI_BOX box, WEntity e) {
		
		if (e instanceof WArmy)
			hover(box, (WArmy) e);
		else if (e instanceof Shipment)
			hover(box, (Shipment) e);
			
		
		
	}
	
	private static void hover(GUI_BOX box, Shipment e) {
		GBox b = (GBox) box;
		b.title(DicGeo.¤¤Caravan);
		b.textL(e.type().name);
		b.NL(8);
		Region c = e.destination();
		if (c == null)
			return;
		
		GText t = b.text();
		t.color(c.faction().banner().colorBG());
		t.add(DicGeo.¤¤BoundFor).insert(0, c.name());
		box.add(t);
		box.NL(4);
		
		for (RESOURCE r :  RESOURCES.ALL()) {
			if (e.loadGet(r) > 0) {
				box.add(r.icon());
				box.add(GFORMAT.i(b.text(), e.loadGet(r)));
			}
			
		}
	}
	
	private static void hover(GUI_BOX box, WArmy a) {
		GBox b = (GBox) box;
		b.title(a.name);
		
		if (a.faction() != null) {
			b.add(a.faction().banner().HUGE);
			b.add(b.text().color(a.faction().banner().colorFG()).add(a.faction().appearence().name()));
		}else {
			b.text(DicArmy.¤¤Rebels);
		}
		b.NL();
		if (a.faction() != null) {
			if (FACTIONS.rel().war.get(FACTIONS.player(), a.faction()) == 1)
				box.text(DicArmy.¤¤Enemy);
			else if (FACTIONS.rel().overlord.get(FACTIONS.player(), a.faction()) == 1)
				box.text(DicArmy.¤¤Puppet);
			else if (FACTIONS.rel().overlord.get(a.faction(), FACTIONS.player()) == 1)
				box.text(DicArmy.¤¤Puppet);
			else
				box.text(DicArmy.¤¤Neutral);
			b.NL();
		}
		
		
		b.textL(DicArmy.¤¤Soldiers);
		b.add(GFORMAT.iofkInv(b.text(), WARMYD.men(null).get(a), WARMYD.men(null).target().get(a)));
		b.NL();
		b.add(cardsA.get(a));
		
		
		if (a.region() != null) {
			b.add(b.text().color(a.region().color()).add(a.region().name()));
			b.NL(8);
		}
		if (a.faction() == GAME.player() || S.get().developer) {
			b.add(a.state().info(a, b.text()));
		}else {
			
			
			
		}
	}
	
}
