package view.sett.ui.food;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import game.faction.FResources;
import game.faction.FResources.RTYPE;
import init.D;
import init.need.NEEDS;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.home.HOMET;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomProduction;
import settlement.room.main.RoomProduction.Source;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import util.dic.*;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;

public class UIFood extends ISidePanel{

	private static CharSequence ¤¤expl = "How many days we can feed the population. Note that this is a rough estimate. Many factors, such as trade, and production can affect this amount.";
	
	static {
		D.ts(UIFood.class);
	}
	
	public UIFood() {
		
		titleSet(DicRes.¤¤Food);
		
		
	
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				double am = 0;
				for (ResG rr : RESOURCES.EDI().all()) {
					am += ROOMS().PROD.produced(rr.resource);
				}
				
				GFORMAT.f0(text, am);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				double am = 0;
				for (ResG rr : RESOURCES.EDI().all()) {
					b.add(rr.resource.icon());
					b.textL(rr.resource.name);
					b.tab(7);
					double a = ROOMS().PROD.produced(rr.resource);
					
					b.add(GFORMAT.f0(b.text(), a));
					b.NL();
					am += a;
				}
				
				b.NL(8);
				
				b.textLL(DicMisc.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.f0(b.text(), am));
				
			};
			
		}.hv(DicMisc.¤¤ProductionRate));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			private final Bitmap1D check = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
			
			@Override
			public void update(GText text) {
				
				double needed = 0;
				
				for (ResG res : RESOURCES.EDI().all())
					needed += SETT.ROOMS().PROD.consumed(res.resource);
				
				for (int ci = 0; ci < HCLASS.ALL().size(); ci++) {
					HCLASS c = HCLASS.ALL.get(ci);
					if (c.player) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							double n = NEEDS.TYPES().HUNGER.rate.get(c.get(r))*STATS.POP().POP.data(c).get(r, 0)*STATS.FOOD().RATIONS.decree().get(c).get(r);
							needed += n;
							
						}
					}
				}
				
				GFORMAT.f0(text, -needed);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				double needed = 0;
				check.clear();
				for (int ci = 0; ci < HCLASS.ALL().size(); ci++) {
					HCLASS c = HCLASS.ALL.get(ci);
					if (c.player) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							double n = NEEDS.TYPES().HUNGER.rate.get(c.get(r))*STATS.POP().POP.data(c).get(r, 0)*STATS.FOOD().RATIONS.decree().get(c).get(r);
							if (n > 0) {
								HOMET t = HOMET.get(c, r);
								if (t == null) {
									b.add(r.appearance().icon);
									b.textL(c.names);
								}else {
									b.add(t.icon);
									b.textL(t.name);
								}
								
								b.tab(7);
								b.add(GFORMAT.f0(b.text(), -n));
								b.NL();
							}
							needed += n;
							
						}
					}
				}
				
				b.sep();
				
				for (ResG rr : RESOURCES.EDI().all()) {
					for (Source ii : SETT.ROOMS().PROD.consumers(rr.resource)) {
						if (ii.am() >= 0) {
							b.add(ii.icon());
							b.textL(ii.name());
							b.tab(7);
							b.add(GFORMAT.f0(b.text(), -ii.am()));
							b.NL();
						}
					}
				}
				
				b.sep();
				
				b.textLL(DicMisc.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.f0(b.text(), -needed));
				
			};
			
		}.hv(DicMisc.¤¤ConsumptionRate));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				int a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					ResG r = RESOURCES.EDI().all().get(ei);
					a += ROOMS().STOCKPILE.tally().amountTotal(r.resource);
				}
				
				for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
					ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
					a += e.totalFood();
				}
				
				for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
					ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
					a += e.totalFood();
				}
				
				GFORMAT.f0(text, a);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				
				int a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					ResG r = RESOURCES.EDI().all().get(ei);
					a += ROOMS().STOCKPILE.tally().amountTotal(r.resource);
				}
				
				b.add(ROOMS().STOCKPILE.icon.small);
				b.textL(ROOMS().STOCKPILE.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), a));
				b.NL();
				
				for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
					ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
					int am = (int) e.totalFood();
					a += am;
					

					b.add(e.icon.small);
					b.textL(e.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), am));
					b.NL();
				}
				
				for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
					ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
					int am = (int) e.totalFood();
					a += am;
					

					b.add(e.icon.small);
					b.textL(e.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), am));
					b.NL();
				}

				b.NL(8);
				
				b.textLL(DicMisc.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.i(b.text(), a));
				
			};
			
		}.hv(DicRes.¤¤Stored));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				GFORMAT.f(text, STATS.FOOD().FOOD_DAYS.data().getD(null)*STATS.FOOD().FOOD_DAYS.dataDivider());
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				b.text(¤¤expl);
				
			};
			
		}.hv(STATS.FOOD().FOOD_DAYS.info().name));
		
		GStaples st = new GStaples(STATS.DAYS_SAVED/4) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int ii = (STATS.DAYS_SAVED/4-stapleI -1);
				box.title(STATS.FOOD().FOOD_DAYS.info().name);
				
				{
					GText tt = box.text();
					DicTime.setDaysAgo(tt, ii*4);
					box.textLL(tt);
					box.NL(4);
				}
				
				{
					box.textLL(STATS.FOOD().FOOD_DAYS.info().name);
					box.tab(7);
					box.add(GFORMAT.f(box.text(), STATS.FOOD().FOOD_DAYS.data(null).getD(null, ii*4)*STATS.FOOD().FOOD_DAYS.dataDivider()));
					box.NL();
				}

				{
					box.textLL(DicMisc.¤¤Population);
					box.tab(7);
					box.add(GFORMAT.i(box.text(), STATS.POP().POP.data().get(null)));
					box.NL();
				}
				
				{
					box.textLL(DicRes.¤¤Stored);
					box.tab(7);
					int st = 0;
					for (ResG rr : RESOURCES.EDI().all()) {
						st += SETT.ROOMS().STOCKPILE.tally().amountsSeason().history(rr.resource).get(ii);
					}
					box.add(GFORMAT.i(box.text(), st));
					box.NL();
				}
				
				box.sep();
				
				{
					int net = 0;
					
					for (RTYPE t : FResources.RTYPE.all) {
						box.text(t.name);
						box.tab(7);
						int in = 0;
						int out = 0;
						for (ResG rr : RESOURCES.EDI().all()) {
							in += FACTIONS.player().res().in(t).history(rr.resource).get(ii);
							out += FACTIONS.player().res().out(t).history(rr.resource).get(ii);
						}
						net += in;
						net -= out;
						box.add(GFORMAT.iIncr(box.text(), in));
						box.tab(9);
						box.add(GFORMAT.iIncr(box.text(), -out));
						box.NL();
					}
					
					box.textL(DicRes.¤¤Net);
					box.tab(7);
					box.add(GFORMAT.iIncr(box.text(), net));
					box.NL();
				}
				
				
				
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				return STATS.FOOD().FOOD_DAYS.data(null).getD(null, (STATS.DAYS_SAVED-stapleI -1)/4);
			}
		};
		
		st.body().setWidth(400).setHeight(80);
		
		section.addRelBody(4, DIR.S, st);
		
		
		Queue<ResG> all = new Queue<>(RESOURCES.EDI().all().size());
		
		for (ResG res : RESOURCES.EDI().all())
			all.push(res);
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		while(all.hasNext()) {
			
			GuiSection s = new GuiSection();
			
			for (int i = 0; i < 2 && all.hasNext(); i++) {
				
				s.addRightC(8, new RR(all.poll()));

				
			}
			
			rows.add(s);
		}
		
		section.addRelBody(16, DIR.S, new GScrollRows(rows, HEIGHT-section.body().height()-32).view());
		
		
	}
	
	
	private static class RR extends HOVERABLE.HoverableAbs{
		
		private final ResG res;
		private final GText t = new GText(UI.FONT().S, 8);
		
		RR(ResG res){
			super(Icon.M*2+200, Icon.M*2+12);
			this.res = res;
		}

		@Override
		protected void render(SPRITE_RENDERER ren, float ds, boolean isHovered) {
			GButt.ButtPanel.renderBG(ren, true, false, isHovered, body);
			res.resource.icon().renderScaled(ren, body.x1()+8, body.y1()+6, 2);
			t.clear();
			
			int am = totStored();
			
			GFORMAT.i(t, am);
			t.adjustWidth();
			t.renderCY(ren, body.x1()+120-t.width(), body.cY());
			
			t.clear();
			
			GFORMAT.f0(t, SETT.ROOMS().PROD.produced(res.resource)-SETT.ROOMS().PROD.consumed(res.resource));
			t.adjustWidth();
			t.renderCY(ren, body.x2()-8-t.width(), body.cY());
			
			GButt.ButtPanel.renderFrame(ren, body);
		}
		
		private int totStored() {
			int am = 0;
			am += ROOMS().STOCKPILE.tally().amountTotal(res.resource);
			
			for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
				ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
				am += e.amount(res);
			}
			
			for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
				ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
				am += e.amount(res);
			}
			return am;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(res.resource.names);
			
			b.textLL(DicRes.¤¤Stored);
			b.NL();
			b.add(ROOMS().STOCKPILE.icon.small);
			b.textL(ROOMS().STOCKPILE.info.names);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), ROOMS().STOCKPILE.tally().amountTotal(res.resource)));
			b.NL();
			
			for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
				ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
				b.NL();
				b.add(e.icon.small);
				b.textL(e.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(),  e.amount(res)));
				b.NL();
			}
			
			for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
				ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
				b.NL();
				b.add(e.icon.small);
				b.textL(e.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(),  e.amount(res)));
				b.NL();
			}
			
			b.NL(4);
			
			b.textL(DicMisc.¤¤Total);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), totStored()));
			
			b.sep();
			
			b.textLL(STATS.FOOD().FOOD_PREFFERENCE.info().name);
			b.NL();
			
			for (Race r : RACES.all()) {
				if (r.pref().foodMask.has(res.resource)) {
					b.add(r.appearance().icon);
					b.textL(r.info.names);
					b.NL();
				}
			}
			
			b.sep();
			
			b.textLL(DicMisc.¤¤Production);
			b.NL();
			
			for (RoomProduction.Source ii : SETT.ROOMS().PROD.producers(res.resource)) {
				b.add(ii.icon());
				b.textLL(ii.name());
				if (ii.thereAreMultipleIns() != null) {
					for (IndustryResource iii : ii.thereAreMultipleIns().ins()) {
						b.add(iii.resource.icon().small);
					}
				}
				
				b.tab(7);
				b.add(GFORMAT.f0(b.text(), ii.am()));
				b.NL();
			}
			
			b.NL(8);
			b.textLL(DicMisc.¤¤Consumed);
			b.NL();
			
			for (RoomProduction.Source ii : SETT.ROOMS().PROD.consumers(res.resource)) {
				b.add(ii.icon());
				b.textLL(ii.name());
				if (ii.thereAreMultipleIns() != null) {
					for (IndustryResource iii : ii.thereAreMultipleIns().ins()) {
						b.add(iii.resource.icon().small);
					}
				}
				
				b.tab(7);
				b.add(GFORMAT.f0(b.text(), -ii.am()));
				b.NL();
			}
		}
		
	}
	
}
