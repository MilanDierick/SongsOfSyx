package view.ui;

import game.faction.FACTIONS;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.*;
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
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import view.main.VIEW;

final class UINotifications extends GuiSection {
	
	private final LinkedList<Butt> butts = new LinkedList<>();
	private static final int max = 8;
	
	
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
		
		butts.add(new Butt(SPRITES.icons().s.death, new ColorImp(127, 100, 0), STATS.NEEDS().disease.infected().info().name, STATS.NEEDS().disease.infected().info().desc) {
			int k = 0;
			
			@Override
			public int get() {
				return STATS.NEEDS().disease.infected().data(null).get(null);
			}
			
			@Override
			protected void clickA() {
				k = showNextH(k, STATS.NEEDS().disease.infected());
				super.clickA();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				UINotifications.this.hover(text, STATS.NEEDS().disease.infected());
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
		
		Butt(ICON.SMALL icon, COLOR color){
			this.icon = icon;
			this.color = color;
			body.setDim(32, 40);
		}
		
		Butt(ICON.SMALL icon, COLOR color, CharSequence hover){
			this.icon = icon;
			this.color = color;
			hoverInfoSet(hover);
			body.setDim(32, 40);
		}
		
		Butt(ICON.SMALL icon, COLOR color, CharSequence name, CharSequence desc){
			this.icon = icon;
			this.color = color;
			hoverTitleSet(name);
			hoverInfoSet(desc);
			body.setDim(32, 40);
		}
		
		Butt(ICON.SMALL icon, COLOR color, INFO info){
			this.icon = icon;
			this.color = color;
			hoverTitleSet(info.name);
			hoverInfoSet(info.desc);
			body.setDim(32, 40);
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
			icon.renderC(r, body().cX(), body().cY()-10);
			COLOR.unbind();
			
			text.clear();
			GFORMAT.i(text, CLAMP.i(am, 0, 99));
			text.adjustWidth();
			text.renderC(r, body().cX(), body().cY()+10);
			text.color(COLOR.WHITE100);

			OPACITY.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				if (MButt.RIGHT.consumeClick()) {
					supressed = !supressed;
				}
				return true;
			}
			return false;
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
