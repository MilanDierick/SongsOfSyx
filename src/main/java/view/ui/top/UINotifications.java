package view.ui.top;

import game.GAME;
import game.faction.FACTIONS;
import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import settlement.stats.util.CAUSE_LEAVE;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicArmy;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import view.main.VIEW;
import world.WORLD;
import world.entity.army.WArmy;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.RD;
import world.regions.data.building.RDBuilding;

final class UINotifications extends GuiSection {
	
	private final LinkedList<Butt> butts = new LinkedList<>();
	private final static int BW = 46;
	private static final int max = 6 + (C.WIDTH()-C.MIN_WIDTH)/(2*BW);
	
	private static CharSequence ¤¤constructed = "Regions that can be upgraded";
	
	static {
		D.ts(UINotifications.class);
	}
	
	UINotifications(){
		
		D.spush(UINotifications.class);
		
		butts.add(new Butt(SPRITES.icons().s.plate, new ColorImp(127, 100, 0), STATS.FOOD().STARVATION.info()) {
			
			int k = 0;
			@Override
			public int get() {
				return STATS.FOOD().STARVATION.data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.FOOD().STARVATION);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				UINotifications.this.hover(text, STATS.FOOD().STARVATION);
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.alert, new ColorImp(127, 100, 0), STATS.POP().TRAPPED.info()) {
			int k = 0;
			
			@Override
			public int get() {
				return STATS.POP().TRAPPED.data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.POP().TRAPPED);
				super.clickA();
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.drop, new ColorImp(127, 20, 0), STATS.NEEDS().INJURIES.DANGER.info()) {
			int k = 0;
			
			@Override
			public int get() {
				return STATS.NEEDS().INJURIES.DANGER.data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.NEEDS().INJURIES.DANGER);
				super.clickA();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				UINotifications.this.hover(text, STATS.NEEDS().INJURIES.DANGER);
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.human, new ColorImp(0, 110, 127), STATS.NEEDS().EXPOSURE.DANGER.info()) {
			int k = 0;
			
			@Override
			public int get() {
				return STATS.NEEDS().EXPOSURE.DANGER.data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.NEEDS().EXPOSURE.DANGER);
				super.clickA();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				UINotifications.this.hover(text, STATS.NEEDS().EXPOSURE.DANGER);
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.arrow_left, new ColorImp(127, 20, 0), STATS.POP().EMMIGRATING.info()) {
			int k = 0;
			
			@Override
			public int get() {
				return STATS.POP().EMMIGRATING.data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.POP().EMMIGRATING);
				super.clickA();
			}
			
		});
		
		
		
		butts.add(new Butt(SPRITES.icons().s.death, new ColorImp(127, 20, 0)) {
			
			LinkedList<CAUSE_LEAVE> wrongful = new LinkedList<>();
			
			{
				for (CAUSE_LEAVE l : CAUSE_LEAVE.ALL()) {
					if (l.defaultStanding() > 0)
						wrongful.add(l);
				}
			}
			
			CharSequence wrong = D.g("wrongful", "Wrongful Deaths");
			private int ci = 0;
			private Corpse corpse;
			
			@Override
			public int get() {
				int i = 0;
				for (CAUSE_LEAVE c : wrongful) {
					i+= SETT.THINGS().corpses.amount(c);
				}
				return i;
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(wrong);
				for (CAUSE_LEAVE c : wrongful) {
					b.textL(c.name);
					b.tab(4);
					b.add(GFORMAT.i(b.text(), SETT.THINGS().corpses.amount(c)));
					b.NL();
				}
				super.hoverInfoGet(text);
			}
			
			@Override
			protected void clickA() {
				if (corpse == null || corpse.isRemoved() || corpse.cause() != wrongful.get(ci)) {
					corpse = getCorpse();
				}
				if (corpse != null) {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(corpse.ctx(), corpse.cty());
					corpse = SETT.THINGS().corpses.getNext(corpse);
				}
			}
			
			private Corpse getCorpse() {
				for (int i = 0; i < wrongful.size(); i++) {
					ci ++;
					ci %= wrongful.size();
					Corpse c = SETT.THINGS().corpses.getFirst(wrongful.get(ci));
					if (c != null) {
						return c;
					}
				}
				return null;
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.hammer, new ColorImp(20, 127, 20), D.g("Room", "Constructed Rooms")) {
			@Override
			public int get() {
				return SETT.ROOMS().stats.finished().amount();
			}
			
			@Override
			protected void clickA() {
				COORDINATE r = SETT.ROOMS().stats.finished().poll();
				if (r != null) {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(r);
					if (SETT.ROOMS().map.instance.get(r) != null)
						VIEW.s().ui.rooms.open(SETT.ROOMS().map.instance.get(r));
				}
				super.clickA();
			}
			
			@Override
			protected void supress() {
				SETT.ROOMS().stats.finished().clear();
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.hammer, new ColorImp(127, 20, 20), D.g("RoomBroken", "Broken Rooms")) {
			@Override
			public int get() {
				return SETT.ROOMS().stats.broken().amount();
			}
			
			@Override
			protected void clickA() {
				COORDINATE r = SETT.ROOMS().stats.broken().poll();
				if (r != null) {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(r);
				}
				super.clickA();
			}
			
			@Override
			protected void supress() {
				SETT.ROOMS().stats.finished().clear();
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.crazy, new ColorImp(127, 100, 20), HTYPE.DERANGED.names, HTYPE.DERANGED.desc) {
			int k = 0;
			@Override
			public int get() {
				int am = STATS.POP().pop(HTYPE.DERANGED);
				am -= SETT.ROOMS().ASYLUM.prisoners();
				am = CLAMP.i(am, 0, Integer.MAX_VALUE);
				return am;
			}
			
			@Override
			protected void clickA() {
				ENTITY[] es = SETT.ENTITIES().getAllEnts();
				for (int q = 0; q < es.length; q++) {
					if (k >= es.length)
						k = 0;
					ENTITY e = es[k];
					k++;
					if (e instanceof Humanoid && (((Humanoid) e).indu().hType()) == HTYPE.DERANGED){
						VIEW.s().activate();
						VIEW.s().getWindow().centererTile.set(e.tc());
						VIEW.s().ui.subjects.show(((Humanoid) e));
						return;
					}
					
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				GBox b = (GBox) text;
				b.NL();
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = FACTIONS.player().races.get(ri);
					b.add(r.appearance().icon);
					b.textL(r.info.names);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), STATS.POP().pop(r, HTYPE.DERANGED)));
					b.NL();
				}
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.sword, new ColorImp(127, 20, 20), DicArmy.¤¤Trespassing, DicArmy.¤¤TrespassingD) {
			int am = 0;
			private final GAME.Cache cache = new GAME.Cache(60);
			@Override
			public int get() {
				if (cache.shouldAndReset()) {
					am = 0;
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						Region reg = FACTIONS.player().realm().region(ri);
						for (WArmy a : WORLD.ENTITIES().armies.fill(reg))
							if (FACTIONS.DIP().war.is(FACTIONS.player(), a.faction()))
								am++;
					}
				}
				return am;
			}
			
			@Override
			protected void clickA() {
				
				cache.shouldAndReset();
				int k = get();
				if (k == 0)
					return;
				
				for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
					Region reg = FACTIONS.player().realm().region(ri);
					for (WArmy a : WORLD.ENTITIES().armies.fill(reg))
						if (FACTIONS.DIP().war.is(FACTIONS.player(), a.faction())) {
							k--;
							if (k == 0) {
								VIEW.world().activate();
								VIEW.world().window.centererTile.set(a.ctx(), a.cty());
								return;
							}
						}
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
			}
			
		});
		
		butts.add(new Butt(SPRITES.icons().s.world, new ColorImp(20, 127, 20), null, ¤¤constructed) {
			
			ArrayListGrower<RDBuilding> notis = new ArrayListGrower<>();
			final Bitmap1D has = new Bitmap1D(WREGIONS.MAX, false);
			
			{
				for (RDBuilding b : RD.BUILDINGS().all) {
					if (b.notify)
						notis.add(b);
				}
			}
			
			
			int am = 0;
			int ri = 0;
			int k = 0;
			private final GAME.Cache cache = new GAME.Cache(4);
			@Override
			public int get() {
				
				if (!cache.shouldAndReset())
					return am;
				Region reg = WORLD.REGIONS().getByIndex(ri);
				if (has.get(ri))
					am --;
				if (reg != null && reg.active() && !reg.capitol() && reg.faction() == FACTIONS.player()) {
					
					for (RDBuilding bu: notis) {
						
						if (!bu.level.isMax(reg) && bu.canAfford(reg, bu.level.get(reg)+1)) {
							am++;
							has.set(ri, true);
						}
							
					}
				}
				ri++;
				ri %= WREGIONS.MAX;
				return am;
			}
			
			@Override
			protected void clickA() {
				
				for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
					k %= FACTIONS.player().realm().regions();
					Region reg = FACTIONS.player().realm().region(k);
					k++;
					if (reg.capitol())
						continue;
					for (RDBuilding bu: notis) {
						if (!bu.level.isMax(reg) && bu.canAfford(reg, bu.level.get(reg)+1)) {
							VIEW.world().activate();
							VIEW.world().UI.regions.open(reg);
							VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
							return;
						}
							
					}
				}
				
				am = 0;
				has.setAll(false);

			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
			}
			
		});
		
		body().setDim(max*butts.get(0).body.width(), butts.get(0).body().height());
		
		D.spop();
		
		
	}
	
	private int showNextH(int k, STAT s) {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		for (int q = 0; q < es.length; q++) {
			if (k >= es.length)
				k = 0;
			ENTITY e = es[k];
			k++;
			if (e instanceof Humanoid && s.indu().isMax(((Humanoid) e).indu())){
				Humanoid h = (Humanoid) e;
				if (h.indu().clas().player && s.indu().isMax(h.indu())) {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(e.tc());
					VIEW.s().ui.subjects.show(((Humanoid) e));
				}
				return k;
			}
			
		}
		return k;
	}

	void add(RENDEROBJ b, int i) {
		b.body().moveX1Y1(b.body().width()*(i/2), b.body().height()*(i%2));
		addRightC(0, b);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		int x1 = body().x1();
		int y1 = body().y1();
		clear();
		int am = 0;
		for (Butt b : butts) {
			if (b.get() > 0) {
				addRight(0, b);
				am++;
				if (am > max)
					return;
			}
		}
		body().moveX1Y1(x1, y1);
		super.render(r, ds);
	}
	
	
	private static abstract class Butt extends CLICKABLE.ClickableAbs {

		private final static GText text = new GText(UI.FONT().S, 10);
		private final static COLOR flashBg = new ColorShifting(GCOLOR.UI().bg(), GCOLOR.UI().bg().shade(4.0)).setSpeed(1.5);
		private final SPRITE icon;
		private final COLOR color;
		private boolean supressed = true;
		
		Butt(Icon icon, COLOR color){
			this.icon = icon;
			this.color = color;
			body.setDim(BW, 22);
		}
		
		Butt(Icon icon, COLOR color, CharSequence hover){
			this.icon = icon;
			this.color = color;
			hoverInfoSet(hover);
			body.setDim(BW, 22);
		}
		
		Butt(Icon icon, COLOR color, CharSequence name, CharSequence desc){
			this.icon = icon;
			this.color = color;
			hoverTitleSet(name);
			hoverInfoSet(desc);
			body.setDim(BW, 22);
		}
		
		Butt(Icon icon, COLOR color, INFO info){
			this.icon = icon;
			this.color = color;
			hoverTitleSet(info.name);
			hoverInfoSet(info.desc);
			body.setDim(BW, 22);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body);
			int am = get();
			if (am > 0 && ! supressed) {
				flashBg.render(r, body, -1);
			}else {
				GCOLOR.UI().bg().render(r, body, -1);
			}
			
			ColorImp.TMP.set(color);
			
			if (am == 0) {
				OPACITY.O50.bind();
			}else if (!supressed) {
				OPACITY.O75TO100.bind();
			}else {
				
			}
			
			ColorImp.TMP.set(color).bind();
			icon.renderCY(r, body().x1()+2, body().cY());
			COLOR.unbind();
			
			text.clear();
			GFORMAT.i(text, CLAMP.i(am, 0, 99));
			text.adjustWidth();
			text.renderCY(r, body().x1()+20, body().cY()+2);
			text.color(COLOR.WHITE100);

			OPACITY.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				if (MButt.RIGHT.consumeClick()) {
					supress();
				}
				return true;
			}
			return false;
		}
		
		protected void supress() {
			supressed = !supressed;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
		}
		
		public abstract int get();
		
	}
	
	private void hover(GUI_BOX text, STAT s) {
		
		GBox b = (GBox) text;
		b.NL();
		b.tab(6);
		b.textLL(HCLASS.CITIZEN.names);
		b.tab(9);
		b.textLL(HCLASS.SLAVE.names);
		b.tab(12);
		b.textLL(HCLASS.NOBLE.names);
		b.NL();
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = FACTIONS.player().races.get(ri);
			b.add(r.appearance().icon);
			b.textL(r.info.names);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), s.data(HCLASS.CITIZEN).get(r)));
			b.tab(9);
			b.add(GFORMAT.i(b.text(), s.data(HCLASS.SLAVE).get(r)));
			b.tab(12);
			b.add(GFORMAT.i(b.text(), s.data(HCLASS.NOBLE).get(r)));
			b.NL();
		}
		
		b.NL(4);
		
		b.textL(HCLASS.CHILD.names);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), s.data(HCLASS.CHILD).get(null)));
		
	}
	

}
