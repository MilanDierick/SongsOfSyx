package view.sett.ui.minimap;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import init.C;
import init.D;
import init.boostable.BOOSTABLES;
import init.resources.*;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.industry.module.*;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.infra.stockpile.StockpileInstance;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LinkedList;
import util.data.INT.INTE;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.panel.GFrame;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HistoryResource;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;

final class UIMiniResources extends Expansion{

	private static CharSequence ¤¤desc = "¤Click to open resource details, right click to go to warehouse.";
	private static CharSequence ¤¤Exists = "¤Resources exist scattered on the map that are not yet stored and counted.";
	private static CharSequence ¤¤Food = "¤Days of food stored for all subjects.";
	private static CharSequence ¤¤Production = "¤Produced per day:";
	static {
		D.ts(UIMiniResources.class);
	}
	
	private GuiSection mini;
	private GuiSection full;
	
	public UIMiniResources(int index, int y1){
		super(index);
		
		full = new Full(y1);
		mini = new Mini(y1);
		
		add(full);
		
		CLICKABLE c = new GButt.Glow(SPRITES.icons().s.arrow_left) {
			@Override
			protected void clickA() {
				int y1 = body().y1();
				clear();
				add(full);
				body().moveY1(y1);
			}
		};
		mini.add(c, mini.body().x2()-c.body().width()-4, mini.body().y1()+4+16);
		c = new GButt.Glow(SPRITES.icons().s.arrow_right) {
			@Override
			protected void clickA() {
				int y1 = body().y1();
				clear();
				add(mini);
				body().moveY1(y1);
			}
		};
		full.add(c, full.body().x2()-c.body().width()-4, full.body().y1()+4+16);
		

		
	}
	
	private static RENDEROBJ foodDays(int width) {
		
		
		
		return new HOVERABLE.HoverableAbs(width-4, 18) {
			
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					int fd = (int) (STATS.FOOD().FOOD_DAYS.data(null).getD(null, 0)* STATS.FOOD().FOOD_DAYS.dataDivider());
					GFORMAT.i(text, fd);
				}
			}.decrease();
			
			GStaples staples = new GStaples(STATS.DAYS_SAVED) {
				
				@Override
				protected void hover(GBox box, int stapleI) {
					
				}
				
				@Override
				protected double getValue(int stapleI) {
					return STATS.FOOD().FOOD_DAYS.data(null).getD(null, STATS.DAYS_SAVED-stapleI -1);
					
				}
			};
			
			{
				staples.body().setWidth(200).setHeight(64);
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				SPRITES.icons().s.plate.renderCY(r, body.x1(), body().cY());
				
				int x1 = body().x1()+20;
				
				GMeter.renderDelta(r, STATS.FOOD().FOOD_DAYS.data().getPeriodD(null, 8, 0), STATS.FOOD().FOOD_DAYS.data().getD(null), x1, body().x2(), body().y1(), body().y2());
				s.adjust();
				
				OPACITY.O50.bind();
				
				x1 += 8;
				COLOR.BLACK.render(r, x1-2, x1+s.width()+2, body().y1()+1, body().y2()-1);
				OPACITY.unbind();
				s.renderCY(r, x1, body().cY());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(DicRes.¤¤Food);
				
				b.textLL(STATS.FOOD().FOOD_DAYS.info().name);
				b.tab(5);
				int fd = (int) (STATS.FOOD().FOOD_DAYS.data(null).getD(null, 0)* STATS.FOOD().FOOD_DAYS.dataDivider());
				b.add(GFORMAT.iBig(b.text(), fd));
				b.NL(8);
				
				b.text(¤¤Food);
				b.NL();
				b.add(staples);
				b.NL(8);
				
				int t1 = 5;
				int t2 = 2;
				int years = 4;
				
				b.textLL(TIME.years().cycleName());
				for (int i = 0; i < years; i++) {
					b.tab(t1+i*t2);
					b.add(b.text().lablifySub().add(-years+i));
				}
				b.tab(t1+years*t2);
				b.add(b.text().lablifySub().add(0));
				
				b.NL();
				b.textLL(DicRes.¤¤Stored);
				for (int i = 0; i < years; i++) {
					int back = (years - i)*4+TIME.seasons().bitCurrent();
					b.tab(t1+i*t2);
					b.add(GFORMAT.i(b.text(), get(SETT.ROOMS().STOCKPILE.tally().amountsSeason(), back)));
				}
				b.tab(t1+years*t2);
				b.add(GFORMAT.i(b.text(), get(SETT.ROOMS().STOCKPILE.tally().amountsSeason(), 0)));
				
				b.NL();
				b.textLL(FACTIONS.player().res().in.info().name);
				for (int i = 0; i < years; i++) {
					int back = (years - i)*4+TIME.seasons().bitCurrent();
					b.tab(t1+i*t2);
					b.add(GFORMAT.iIncr(b.text(), getA(FACTIONS.player().res().in, back)));
				}
				b.tab(t1+years*t2);
				b.add(GFORMAT.iIncr(b.text(), getA(FACTIONS.player().res().in, 0)));
				
				b.NL();
				b.textLL(FACTIONS.player().res().out.info().name);
				for (int i = 0; i < years; i++) {
					int back = (years - i)*4+TIME.seasons().bitCurrent();
					b.tab(t1+i*t2);
					b.add(GFORMAT.iIncr(b.text(), -getA(FACTIONS.player().res().out, back)));
				}
				b.tab(t1+years*t2);
				b.add(GFORMAT.iIncr(b.text(), -getA(FACTIONS.player().res().out, 0)));
				
				b.NL();
				b.textLL(STATS.POP().POP.info().name);
				for (int i = 0; i < years; i++) {
					int back = (years - i);
					b.tab(t1+i*t2);
					b.add(b.text().add(STATS.POP().popYearly().get(back)));
				}
				b.tab(t1+years*t2);
				b.add(b.text().add(STATS.POP().popYearly().get(0)));
				b.NL();
				
				
				super.hoverInfoGet(text);
			}
			
			private int get(HistoryResource h, int back) {
				int am = 0;
				for (Edible e : RESOURCES.EDI().all())
					am += h.get(e.resource.index()).get(back);
				return am;
			}
			
			private int getA(HistoryResource h, int back) {
				int am = 0;
				for (Edible e : RESOURCES.EDI().all())
					for (int i = 0; i < 4; i++) {
						if (back-i >= 0)
							am += h.get(e.resource.index()).get(back-i);
					}
				return am;
			}
			
		};
	
	}
	
	private static class Mini extends GuiSection {
		
		private final INTE t;
		
		Mini(int y1){
			RENDEROBJ row = mini(RESOURCES.ALL().get(0));
			int width = row.body().width();
			int cats = 0;
			for (RESOURCE r : RESOURCES.ALL())
				if (r.category > cats) {
					cats = r.category;
				}
			LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
			{
				int cat = RESOURCES.ALL().get(0).category;
				
				for (RESOURCE r : RESOURCES.ALL()) {
					if (r.category != cat) {
						rows.add(new RENDEROBJ.RenderImp(width, 16) {
							@Override
							public void render(SPRITE_RENDERER r, float ds) {
								GFrame.renderHorizontal(r, body().x1()+4, body().x2()-4, body().y1()+8);
							}
						});
						cat = r.category;
					}
					
					rows.add(mini(r));
					
				}
//				
//				for (int c = 0; c <= cats; c++) {
//					for (RESOURCE r : RESOURCES.ALL())
//						if (r.category == c) {
//							rows.add(res(r));
//						}
//					if (c != cats)
//						rows.add(new RENDEROBJ.RenderImp(row.body().width(), 16) {
//						
//						@Override
//						public void render(SPRITE_RENDERER r, float ds) {
//							GFrame.renderHorizontal(r, body().x1()+4, body().x2()-4, body().y1()+8);
//						}
//					});
//				}
			}
			
			

			
			GFrame f = new GFrame();
			f.body().setWidth(width);
			f.body().setHeight(C.HEIGHT()-y1);
			f.body().moveX2(C.WIDTH());
			f.body().moveY1(y1);
			add(f);
			
			
			RENDEROBJ c = foodDays(width-4);
			c.body().moveX1(body().x1()+2).moveY1(y1+4);
			add(c);
			y1 = c.body().y2()+4;
			
			c = new GButt.Glow(UI.decor().up) {
				@Override
				protected void renAction() {
					activeSet(t.get() > 0);
				}
				@Override
				protected void clickA() {
					t.inc(-1);
				}
			};
			
			c.body().centerX(this);
			c.body().moveY1(getLastY2()+4);
			add(c);
			
			
			GScrollRows sc = new GScrollRows(rows, C.HEIGHT()-getLastY2()-c.body().height()-8, 0, false);
			addDownC(0, sc.view());
			
			t = sc.target;
			
			c = new GButt.Glow(UI.decor().down) {
				@Override
				protected void renAction() {
					activeSet(t.get() != t.max());
				}
				@Override
				protected void clickA() {
					t.inc(1);
				}
			};
			addDownC(4, c);
		}
		
		
	}
	
	private static class Full extends GuiSection {
		
		private final INTE t;
		
		Full(int y1){
			RENDEROBJ row = big(RESOURCES.ALL().get(0));
			int width = row.body().width()*2;
			int cats = 0;
			for (RESOURCE r : RESOURCES.ALL())
				if (r.category > cats) {
					cats = r.category;
				}
			LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
			{
				GuiSection s = null;
				int cat = RESOURCES.ALL().get(0).category;
				
				for (RESOURCE r : RESOURCES.ALL()) {
					if (r.category != cat) {
						rows.add(new RENDEROBJ.RenderImp(width, 16) {
							@Override
							public void render(SPRITE_RENDERER r, float ds) {
								GFrame.renderHorizontal(r, body().x1()+4, body().x2()-4, body().y1()+8);
							}
						});
						s = new GuiSection();
						rows.add(s);
						cat = r.category;
					}
					
					if (s == null || s.elements().size() >= 2) {
						s = new GuiSection();
						rows.add(s);
					}
					
					s.addRightC(0, big(r));
					
				}
//				
//				for (int c = 0; c <= cats; c++) {
//					for (RESOURCE r : RESOURCES.ALL())
//						if (r.category == c) {
//							rows.add(res(r));
//						}
//					if (c != cats)
//						rows.add(new RENDEROBJ.RenderImp(row.body().width(), 16) {
//						
//						@Override
//						public void render(SPRITE_RENDERER r, float ds) {
//							GFrame.renderHorizontal(r, body().x1()+4, body().x2()-4, body().y1()+8);
//						}
//					});
//				}
			}
			
			

			
			GFrame f = new GFrame();
			f.body().setWidth(width);
			f.body().setHeight(C.HEIGHT()-y1);
			f.body().moveX2(C.WIDTH());
			f.body().moveY1(y1);
			add(f);
			
			
			RENDEROBJ c = foodDays(width-8);
			c.body().moveX1(body().x1()+4).moveY1(y1+4);
			add(c);
			y1 = c.body().y2()+4;
			
			c = new GButt.Glow(UI.decor().up) {
				@Override
				protected void renAction() {
					activeSet(t.get() > 0);
				}
				@Override
				protected void clickA() {
					t.inc(-1);
				}
			};
			
			c.body().centerX(this);
			c.body().moveY1(getLastY2()+4);
			add(c);
			
			
			GScrollRows sc = new GScrollRows(rows, C.HEIGHT()-getLastY2()-c.body().height()-8, 0, false);
			addDownC(0, sc.view());
			
			t = sc.target;
			
			c = new GButt.Glow(UI.decor().down) {
				@Override
				protected void renAction() {
					activeSet(t.get() != t.max());
				}
				@Override
				protected void clickA() {
					t.inc(1);
				}
			};
			addDownC(4, c);
		}
		
		
	}
	
	private static GuiSection resBody(RESOURCE res) {
		return new GuiSection() {
			
			int wI = 0;
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				int a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
				int c = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
				GBox b = (GBox) text;
				b.title(res.names);
				b.text(res.desc);
				b.NL();
				b.textL(DicRes.¤¤SpoilRate);
				b.add(GFORMAT.perc(b.text(), res.degradeSpeed()*CLAMP.d(1.0/BOOSTABLES.CIVICS().SPOILAGE.get(null, null), 0, 10)));
				b.add(b.text().add('/').add(TIME.years().cycleName()));
				b.NL();
				if (RESOURCES.EDI().is(res)) {
					b.textL(DicRes.¤¤Edible);

				}
				
				b.NL(4);
				b.textLL(¤¤Production);
				b.NL();
				{
					for (Industry in : SETT.ROOMS().INDUSTRIES) {
						for (IndustryResource r : in.outs()){
							if (r.resource == res && in.blue instanceof RoomBlueprintIns<?>) {
								double am = 0;
								RoomBlueprintIns<?> ins = (RoomBlueprintIns<?>) in.blue;
								for (int i = 0; i < ins.instancesSize(); i++) {
									RoomInstance ii = ins.getInstance(i);
									if (!(ii instanceof ROOM_PRODUCER) || ((ROOM_PRODUCER)ii).industry() == in)
										am += IndustryUtil.calcProductionRate(r.rate, in, ii)*ii.employees().employed();
								}
								b.textL(ins.info.name);
								b.tab(5);
								b.add(GFORMAT.i(b.text(),(int) am));
								b.NL();
							}
						}
					}
				}
				
				
				b.NL(8);
				b.add(b.text().lablifySub().add(SETT.ROOMS().STOCKPILE.info.names));
				b.add(GFORMAT.iofk(b.text(), a, c));
				b.NL();
				b.add(b.text().lablifySub().add(SETT.ROOMS().IMPORT.info.name));
				b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().IMPORT.tally.amount.get(res), SETT.ROOMS().IMPORT.tally.capacity.get(res)));
				b.NL();
				b.add(b.text().lablifySub().add(SETT.ROOMS().EXPORT.info.name));
				b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().EXPORT.tally.amount.get(res), SETT.ROOMS().EXPORT.tally.capacity.get(res)));
				b.NL();
				if (SETT.PATH().finders.resource.scattered.has(res)) {
					b.text(¤¤Exists);
				}
				b.NL(4);
				b.text(¤¤desc);
				
				super.hoverInfoGet(text);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				double a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
				double c = SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
				double d = 0;
				if (c > 0)
					d = a/c;
				if (d > 0.9)
					GMeter.render(r, GMeter.C_REDPURPLE, d, body());
				else if (c > 0)
					GMeter.render(r, GMeter.C_REDGREEN, d, body());
				else
					GMeter.render(r, GMeter.C_INACTIVE, d, body());
				
				if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0) {
					d = SETT.ROOMS().IMPORT.tally.importWhenBelow.getD(res);
					if (d > 0) {
						int x1 = (int) (body().x1() + d*(body().width()-2));
						COLOR.WHITE85.render(r, x1, x1+1, body().y1(), body().y2());
					}
				}
				
				
				
				if (!hoveredIs()) {
					OPACITY.O25.bind();
					COLOR.BLACK.render(r, body(), -1);
					OPACITY.unbind();
				}
				
				//COLOR.WHITE30.render(r, body());
				if (hoveredIs()) {
					if (MButt.RIGHT.consumeClick()) {
						
						for (int i = 0 ; i < SETT.ROOMS().STOCKPILE.instancesSize(); i++) {
							wI++;
							if (wI >= SETT.ROOMS().STOCKPILE.instancesSize())
								wI = 0;
							
							StockpileInstance ins = SETT.ROOMS().STOCKPILE.getInstance(wI);
							
							if (ins.storageGet(res) > 0) {
								VIEW.s().getWindow().centererTile.set(ins.body().cX(), ins.body().cY());
								break;
							}
							
						}
					}
				}
				
				super.render(r, ds);
				
			}
			
			@Override
			public boolean click() {
				VIEW.s().panels.add(VIEW.UI().goods.detail(res, GAME.player()), true);
				return super.click();
				
			}
		};
	}
	
	private static RENDEROBJ stat(RESOURCE res) {
		return new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().S);
				int a = SETT.ROOMS().STOCKPILE.tally().amountTotal(res);
				GFORMAT.i(text, a);
				
				if (a == 0) 
					if (SETT.PATH().finders.resource.scattered.has(res))
						text.normalify();
					else
						text.errorify();
			}
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				OPACITY.O018.bind();
				COLOR.BLACK.render(r, X1-1, X2+1, Y1-1, Y2+1);
				OPACITY.unbind();
				super.render(r, X1, X2, Y1, Y2);
				
			};
		}.r(DIR.NW);
	}

	
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (visableIs()) {
			COLOR.WHITE15.render(r, body());
			super.render(r, ds);
		}
	}
	

	
	private static RENDEROBJ mini(RESOURCE res) {
		
		GuiSection s = resBody(res);
		
		s.add(res.icon().small, 0, 0);
		RENDEROBJ r = stat(res);
		
	 
		s.addRightC(3, r);
		s.body().incrW(40);
		
		
		
		s.pad(2, 4);
		return s;
		
		
	}
	
	private static RENDEROBJ big(RESOURCE res) {
		
		GuiSection s = resBody(res);
		
		s.add(res.icon(), 0, 0);
		RENDEROBJ r = stat(res);
		
	 
		s.addRightC(1, r);
		s.body().incrW(42);
		
		
		
		s.pad(2, 4);
		return s;
		
		
	}

	@Override
	public void save(FilePutter file) {
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		
	}
	

}
