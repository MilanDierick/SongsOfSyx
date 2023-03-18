package view.sett.ui.subject;

import game.GAME;
import game.nobility.Nobility;
import init.C;
import init.D;
import init.boostable.BBoost;
import init.resources.RES_AMOUNT;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.StatsMultipliers.StatMultiplier;
import settlement.stats.StatsMultipliers.StatMultiplierAction;
import settlement.stats.standing.STANDINGS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.LinkedList;
import util.dic.*;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;

final class SInfoActions extends GuiSection{
	
	private final UISubject a;
	final GuiSection section = new GuiSection();
	
	private static CharSequence ¤¤rename = "¤Rename subject.";

	private static CharSequence ¤¤Elevate = "¤Elevate.";
	private static CharSequence ¤¤NobleOk = "Click to elevate this subject into a position of power";
	private static CharSequence ¤¤NobleNo = "Your status determines how many nobles you can elevate. To elevate more, you must increase your status level.";
	private static CharSequence ¤¤NobleAlready = "This subject is already a noble one.";
	private static CharSequence ¤¤workplace = "¤Go to workplace";

	
	private static CharSequence ¤¤ActionNotFor = "¤Action not available for:";
	private static CharSequence ¤¤ActionMarked = "¤The subject is marked for this action, but the action has not yet been consummated. Make sure the requirements of the action are fulfilled, and give it some time.";
	private static CharSequence ¤¤ActionConsumed = "¤This action has been consumed. Its effect will last for a few days while tampering off.";
	private static CharSequence ¤¤ActionCantBe = "¤Action can currently not be performed.";
	static {
		D.ts(SInfoActions.class);
	}

	SInfoActions(UISubject a) {
		this.a = a;
		
		
		int i = 0;
		final int rrr = 4;
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.admin) {
			
			STRING_RECIEVER r = new STRING_RECIEVER() {
				
				@Override
				public void acceptString(CharSequence string) {
					if (string != null && string.length() > 0)
						STATS.APPEARANCE().customName(a.a.indu()).clear().add(string);
				}
			};
			
			@Override
			protected void clickA() {
				VIEW.inters().input.requestInput(r, ¤¤rename, STATS.APPEARANCE().name(a.a.indu()));

			}
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤rename);
				super.hoverInfoGet(text);
			}
			
		}.setDim(40, 40), i++, rrr, 2, 2);
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.noble) {
			RENDEROBJ popup = nPopup();
			@Override
			protected void renAction() {				
				
			}
			
			@Override
			protected void clickA() {
				if (GAME.NOBLE().active() < GAME.player().level().current().noblesAllowed() && a.a.office() == null) {
					VIEW.inters().popup.show(popup, this);
				}
			}
			
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤Elevate);
				if (a.a.office() != null)
					text.text(¤¤NobleAlready);
				if (GAME.NOBLE().active() < GAME.player().level().current().noblesAllowed())
					text.text(¤¤NobleOk);
				else
					((GBox)text).error(¤¤NobleNo);
			}
			
			
		}.setDim(40, 40), i++, rrr, 2, 2);
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.workshop) {
			@Override
			protected void clickA() {
				RoomInstance i = STATS.WORK().EMPLOYED.get(a.a.indu());
				VIEW.s().ui.rooms.open(i);
			};
			@Override
			protected void renAction() {
				activeSet(STATS.WORK().EMPLOYED.get(a.a.indu()) != null);
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤workplace);
				text.NL(4);
				if (STATS.WORK().EMPLOYED.get(a.a.indu()) != null)
					text.text(STATS.WORK().EMPLOYED.get(a.a.indu()).name());
				super.hoverInfoGet(text);
			}
			
		}.setDim(40, 40), i++, rrr, 2, 2);
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				COORDINATE c = a.a.ai().getDestination();
				if (c != null) {
					VIEW.s().getWindow().centerAtTile(c.x(), c.y());
					a.follow = 0;
					VIEW.s().clearAllInterrupters();
				}
			}
			
			@Override
			protected void renAction() {
				COORDINATE c = a.a.ai().getDestination();
				activeSet(c != null);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox box = (GBox) text;
				box.text(DicGeo.¤¤Destination);
				box.NL();
				COORDINATE c = a.a.ai().getDestination();
				if (c != null) {
					box.add(box.text().add('(').add(c.x()).add(':').add(c.y()).add(')'));
				}
			}
		}.setDim(40, 40), i++, rrr, 2, 2);
		

		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.sword) {

			@Override
			protected void renAction() {
				activeSet(a.a.division() != null || STATS.BATTLE().RECRUIT.get(a.a) != null);
			}
			
			@Override
			protected void clickA() {
				VIEW.s().battle.activate();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(DicArmy.¤¤Division);
				
				if (a.a.division() != null)
					text.text(a.a.division().info.name());
				if (STATS.BATTLE().RECRUIT.get(a.a) != null)
					text.text(STATS.BATTLE().RECRUIT.get(a.a).info.name());
				else
					text.text(DicMisc.¤¤None);
			};
			
			
		}.setDim(40, 40), i++, rrr, 2, 2);
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.building) {
			
			@Override
			protected void renAction() {
				HOME h = STATS.HOME().GETTER.get(a.a, this);
				activeSet(h != null);
				if (h != null)
					h.done();
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(SETT.ROOMS().HOMES.HOME.info.name);
				int ri = 0;
				HOME h = STATS.HOME().GETTER.get(a.a, this);
				if (h != null) {
					for (RES_AMOUNT ra : a.a.race().home().clas(a.a.indu().clas()).resources()) {
						b.add(ra.resource().icon());
						b.add(GFORMAT.iofkInv(b.text(), STATS.HOME().current(a.a, ri++), ra.amount()));
						b.NL();
					}
					h.done();
				}
				
				text.NL();
				
				CharSequence s = a.a.race().bio().houseProblem(a.a);
				if (s != null)
					b.add(b.text().warnify().add(s));
			}
			
			@Override
			protected void clickA() {
				HOME h = STATS.HOME().GETTER.get(a.a, this);
				if (h != null) {
					VIEW.s().getWindow().centerAtTile(h.body().cX(), h.body().cY());
					a.follow = 0;
					h.done();
				}else {
					VIEW.s().panels.add(VIEW.s().ui.home, true);
				}
			}
			
		}.setDim(40, 40), i++, rrr, 2, 2);
		
		if (S.get().developer) {
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.exit) {

				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
				}
				
				@Override
				protected void clickA() {
					a.a.interrupt();
				}
				
			}.setDim(40, 40), i++, rrr, 2, 2);
		}
		
		for (StatMultiplier m : STATS.MULTIPLIERS().all()) {
			if (m instanceof StatMultiplierAction) {
				addGrid(mulAction(a, (StatMultiplierAction) m), i++, rrr, 2, 2);
			}
		}
		
	}
	
	private static RENDEROBJ mulAction(UISubject a, StatMultiplierAction m) {
		return new GButt.ButtPanel(m.icon) {
			
			@Override
			protected void renAction() {
				activeSet(m.available(a.a.indu().clas()) && !m.consumeIs(a.a) && (m.markIs(a.a) || m.canBeMarked(a.a.indu())));
				selectedSet(m.markIs(a.a) || m.consumeIs(a.a));
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(m.verb);
				b.text(m.desc);
				b.NL(8);
				
				if (!m.available(a.a.indu().clas())){
					b.add(b.text().errorify().add(¤¤ActionNotFor).s().add(a.a.indu().clas().names));
				}else if (m.consumeIs(a.a)){
					b.textL(¤¤ActionConsumed);
					b.NL(4);
					b.textLL(STANDINGS.get(a.a.indu().clas()).info().name);
					b.tab(5);
					b.add(GFORMAT.percInc(b.text(), m.multiplier(a.a)-1));
				}else if(m.markIs(a.a)) {
					b.textL(¤¤ActionMarked);
				}else if(!m.canBeMarked(a.a.indu())) {
					b.error(¤¤ActionCantBe);
					b.NL(4);
					m.info(b, 1);
				}else {
					b.NL(4);
					b.textLL(STANDINGS.get(a.a.indu().clas()).info().name);
					b.tab(5);
					b.add(GFORMAT.percInc(b.text(), m.max(a.a.indu().clas(), a.a.indu().race())-1));
					b.NL(8);
					m.info(b, 1);
				}
				
				
				
			}
			
			@Override
			protected void clickA() {
				if (activeIs())
					m.mark(a.a, !m.markIs(a.a));
			}
			
		}.setDim(40, 40);
	}
	
	private final RENDEROBJ nPopup() {
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (Nobility n : GAME.NOBLE().ALL()) {
			
			
			GuiSection b = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(n.info().name);
					text.text(n.info().desc);
					text.NL(8);
					
					n.BOOSTER.hover(text);
				}
				
				@Override
				protected void renAction() {
					activeSet(n.subject() == null);
				}
				
				@Override
				protected void clickA() {
					if (n.subject() == null) {
						a.a.officeSet(n);
						VIEW.inters().popup.close();
					}
				}
				
			};
			b.body().setWidth(350);
			GuiSection boo = new GuiSection();
			
			int am = 0;
			for (BBoost bo : n.BOOSTER.boosts()) {
				if (am++ > 14)
					break;
				boo.addRightC(8, bo.boostable.icon());
			}
			
			
			b.addRelBody(1, DIR.S, boo);
			b.addRelBody(1, DIR.N, new GHeader(n.info().name));
			b.pad(0, 4);
			rows.add(b);
		}
		return new GScrollRows(rows, C.HEIGHT()/2).view();
	}

	
}
