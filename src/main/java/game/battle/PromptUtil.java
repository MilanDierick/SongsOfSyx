package game.battle;

import java.util.Arrays;

import game.battle.Conflict.Side;
import game.battle.Resolver.SideResult;
import init.RES;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.colors.GCOLORS_MAP;
import util.data.DOUBLE;
import util.data.DOUBLE_O;
import util.data.INT.INTE;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import view.main.VIEW;
import view.world.ui.army.DivCard;
import world.army.WARMYD;
import world.entity.army.WArmy;
import world.map.regions.REGIOND;

public final class PromptUtil {

	private final CardA[] cardas = new CardA[RES.config().BATTLE.DIVISIONS_PER_ARMY];
	private final GText name = new GText(UI.FONT().H2, 32);
	private final GText soldiers = new GText(UI.FONT().M, 32);
	
	PromptUtil(){
		for (int i = 0; i < cardas.length; i++) {
			cardas[i] = new CardA(i);
		}
	}
	
	private static class CardA implements SPRITE{

		private final int i;
		private WArmy a;
		
		CardA(int i){
			this.i = i;
		}
		
		@Override
		public int width() {
			return DivCard.WIDTH;
		}

		@Override
		public int height() {
			return DivCard.HEIGHT;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			VIEW.world().UI.armies.divCard.render(r, X1, Y1, a.divs().get(i), true, false, false);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}
	
	private class BattleEntry extends HoverableAbs {
		
		private final int i;
		private final boolean player;
		private final Conflict conflict;
		
		public BattleEntry(int i, Conflict conflict, boolean player) {
			super(264, 18);
			this.i = i;
			this.conflict = conflict;
			this.player = player;
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			Side side = player ? conflict.sideA : conflict.sideB;
			if (i < side.size()) {
				name.clear();
				name.add(side.get(i).name);
				name.color(GCOLOR.T().faction(side.get(i).faction()));
				name.renderCY(r, body().x1(), body().cY());
				
				soldiers.clear();
				GFORMAT.i(soldiers, WARMYD.men(null).get(side.get(i)));
				soldiers.renderCY(r, body().x1()+200, body().cY());
				
				
			}else if (i == side.size() && side.garrison() != null) {
				name.clear();
				name.add(DicArmy.¤¤Garrison);
				name.color(GCOLOR.T().faction(side.garrison().faction()));
				name.renderCY(r, body().x1(), body().cY());
				soldiers.clear();
				GFORMAT.i(soldiers, REGIOND.MILITARY().soldiers.get(side.garrison()));
				soldiers.renderCY(r, body().x1()+150, body().cY());
			}
			
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			Side side = player ? conflict.sideA : conflict.sideB;
			if (i < side.size()) {
				
				WArmy a = side.get(i);
				for (int i = 0; i < a.divs().size() && i < cardas.length; i++) {
					cardas[i].a = a;
					b.add(cardas[i]);
					if (i % 10 == 9)
						b.NL();
					
				}
			}
		}
		
	}
	
	public GuiSection makeBattle(Conflict conflict) {
		GuiSection ss = new GuiSection();
		
		
		for (int i = 0; i < cardas.length; i++) {
			cardas[i] = new CardA(i);
		}
		
		for (int i = 0; i < 8; i++) {
			ss.add(new BattleEntry(i, conflict, true), 0, i*18);
			ss.add(new BattleEntry(i, conflict, false), ss.getLastX2()+20, i*18);
		}
		
		DOUBLE dub = new DOUBLE() {
			
			@Override
			public double getD() {
				return 0.5*conflict.sideA.power/conflict.sideB.power;
			}
		};
		
		
		ss.addRelBody(8, DIR.N, new HOVERABLE.Sprite(new GMeter.GMeterSprite(GMeter.C_REDGREEN, dub, 150, 16)).hoverInfoSet(DicArmy.¤¤Balance));
		ss.addRelBody(4, DIR.N, SPRITES.icons().m.sword);
		
		return ss;
	}

	public GuiSection result(Conflict conflict, SideResult result, SideResult enemy) {
		
		GuiSection s = new GuiSection();
		
		s.body().setWidth(264*2 + 20);
		
		
		int i = 0;
		if (conflict.sideA.garrison() != null) {
			
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -result.garrisonLost);
				}
			}.hh((SPRITE) new GText(UI.FONT().H2, DicArmy.¤¤Garrison).color(GCOLORS_MAP.get(conflict.sideA.garrison().faction())), 200), 0, i*20);
			i++;
		}
		int ai = 0;
		for (; ai < conflict.sideA.size() && i < 8; i++) {
			int d = result.deaths[ai];
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -d);
				}
			}.hh((SPRITE) new GText(UI.FONT().H2, conflict.sideA.get(ai).name).color(GCOLORS_MAP.get(conflict.sideA.get(ai).faction())), 200), 0, i*20);
			ai++;
		}
		
		ai = 0;
		i = 0;
		if (conflict.sideB.garrison() != null) {
			
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -enemy.garrisonLost);
				}
			}.hh((SPRITE) new GText(UI.FONT().H2, DicArmy.¤¤Garrison).color(GCOLORS_MAP.get(conflict.sideB.garrison().faction())), 200), 264+20, i*20);
			i++;
		}
		
		for (; ai < conflict.sideB.size() && i < 8; i++) {
			int d = enemy.deaths[ai];
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, -d);
				}
			}.hh((SPRITE) new GText(UI.FONT().H2, conflict.sideB.get(ai).name).color(GCOLORS_MAP.get(conflict.sideB.get(ai).faction())), 200), 264+20, i*20);
			ai++;
		}

		s.addRelBody(4, DIR.N, new GHeader(DicArmy.¤¤Casualties));
		return s;
		
		
		
	}
	
	GuiSection spoils(DOUBLE_O<RESOURCE> getter) {
		
		GuiSection s = new GuiSection();
		
		s.body().setWidth(400);
		
		for (RESOURCE res : RESOURCES.ALL()) {
			GStat stat = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, (int) getter.getD(res));
				}
			};
			s.add(stat.hh(res.icon()), (res.index()%4)*100, (res.index()/4)*24);
		}

		s.addRelBody(4, DIR.N, new GHeader(DicArmy.¤¤Spoils));
		return s;
		
	}
	
	GuiSection slaves(int[] enslave, DOUBLE_O<Race> getter) {
		
		GuiSection s = new GuiSection();
		
		s.body().setWidth(400);
		
		Arrays.fill(enslave, 0);
		
		for (Race r : RACES.all()) {
	
			INTE t = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return (int) getter.getD(r);
				}
				
				@Override
				public int get() {
					return CLAMP.i(enslave[r.index], 0, (int)getter.getD(r));
				}
				
				@Override
				public void set(int t) {
					enslave[r.index] = t;
				}
			};
			
			GGaugeMutable g = new GGaugeMutable(t, 150) {
				@Override
				protected int setInfo(DOUBLE d, GText text) {
					GFORMAT.iofkInv(text, enslave[r.index], (int) getter.getD(r));
					return 110;
				}
			};
			
			CLICKABLE.Pair ren = new CLICKABLE.Pair(new RENDEROBJ.Sprite(r.appearance().icon), g, DIR.E, 8);
			ren.hoverInfoSet(r.info.names);

			s.add(ren, (r.index%2)*260, (r.index/2)*24);
		}

		s.addRelBody(4, DIR.N, new GHeader(DicArmy.¤¤Captives));
		return s;
		
	}
	
}
