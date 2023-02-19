package settlement.room.food.farm;

import game.faction.FACTIONS;
import game.time.TIME;
import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import settlement.tilemap.Fertility;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<FarmInstance, ROOM_FARM> {

	private static CharSequence ¤¤estimated = "¤Estimated Harvest (year)";
	private static CharSequence ¤¤daysToHarvest = "¤Days until harvest";
	private static CharSequence ¤¤baseValue = "¤Base Value";
	private static CharSequence ¤¤workValue = "¤Work Value";
	private static CharSequence ¤¤workValueD = "¤Each day of the year a farm needs tending to. Workers failing to tend to all the tiles will lead to low yields.";
	
	private static CharSequence ¤¤Event = "¤Event";
	private static CharSequence ¤¤EventD = "¤Events such as blights or blessings.";
	
	private static CharSequence ¤¤HarvestYear = "¤This Year";
	private static CharSequence ¤¤HarvestPrev = "¤Last Year";
	
	private static CharSequence ¤¤skill = "¤Skill";
	private static CharSequence ¤¤skillD = "¤Skill is boosts from workers that have been put into the the farm during the growth cycle.";
	
	private static CharSequence ¤¤cycle = "¤Cycle";
	private static CharSequence ¤¤reseed = "¤reseed";
	private static CharSequence ¤¤reseedD = "¤Reseed the farm with another crop. New farm must still be constructed.";
	
	Gui(ROOM_FARM s) {
		super(s);
		D.t(this);
	}
	
	private GuiSection rebuilds = new GuiSection();
	
	@Override
	public void hover(GBox box, FarmInstance i) {
		super.hover(box, i);
		box.NL();
		if (!i.blueprintI().constructor.isIndoors) {
			box.text(blueprint.constructor.fertility.name());
			box.add(GFORMAT.perc(box.text(), i.tData.fertility()));
			
			box.NL();
		}
		box.text(¤¤estimated);
		box.add(GFORMAT.i(box.text(), Util.prospect(i)));
		
		box.space();
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid text, GuiSection sExtra) {
		
		IndustryResource res = blueprint.industries().get(0).outs().get(0);
		
		text.add(new GHeader(DicMisc.¤¤Production));
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤estimated);
				
			};
			
		};
		
		s.add(res.resource.icon(), 0, 0);
		
		s.addRightC(4, new GStat() {
			
			@Override
			public void update(GText text) {
				int am = 0;
				for (int i = 0; i < blueprint.instancesSize(); i++) {
					FarmInstance ins = blueprint.getInstance(i);
					am += Util.prospect(ins);
				}
				GFORMAT.i(text, am);
			}
		});
		
		s.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				int prev = 0;
				int am = 0;
				for (int i = 0; i < blueprint.instancesSize(); i++) {
					FarmInstance ins = blueprint.getInstance(i);
					prev += Util.prevHarvest(ins);
					am += Util.prospect(ins);
				}
				GFORMAT.iIncr(text, am-prev);
			}
		});
		
		text.add(s);
		
		GStaples st = new GStaples(res.history().historyRecords()) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int i = res.history().historyRecords()-1-stapleI;
				int am = res.history().get(i);
				GText t = box.text();
				DicTime.setYearsAgo(t, i);
				box.add(t);
				box.NL(2);
				box.add(GFORMAT.i(box.text(), am));
			}
			
			@Override
			protected double getValue(int stapleI) {
				return res.history().get(res.history().historyRecords()-1-stapleI);
			}
		};
		

		st.body().setWidth(180).setHeight(64);
		text.add(st);
		
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<FarmInstance> getter, int x1, int y1) {
		
		ACTION rebuild = new ACTION() {
			@Override
			public void exe() {
				rebuilds = new GuiSection();
				int i = 0;
				for (ROOM_FARM f : SETT.ROOMS().FARMS) {
					if (f.isAvailable(SETT.ENV().climate())) {
						CLICKABLE ss = new GButt.ButtPanel(f.iconBig()) {
							
							@Override
							protected void clickA() {
								if (FACTIONS.player().locks.unlockText(f) == null && f != getter.get().blueprintI()) {
									VIEW.inters().popup.close();
									getter.get().changeTo(f);
									
								}
								
							};
							
							@Override
							protected void renAction() {
								activeSet(FACTIONS.player().locks.unlockText(f) == null && f != getter.get().blueprintI() && f.constructor.isIndoors == getter.get().blueprintI().constructor.isIndoors);
							};
							
						}.hoverSet(f.info);
						rebuilds.add(ss, (i%6)*ss.body().width(), (i/6)*ss.body().height());
						i++;
					}
				}
				
			}
		};
		{
			rebuild.exe();
			SETT.addGeneratorHook(rebuild);
		}

		CLICKABLE c = new GButt.ButtPanel(¤¤reseed) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(rebuilds, this);
				super.clickA();
			}
		}.pad(8, 4).hoverInfoSet(¤¤reseedD);
		
		section.addRelBody(16, DIR.S, c);
		
		{
			GuiSection s = new GuiSection();
			
			s.add(prod(getter));
			
			RENDEROBJ o = new GStat() {
				@Override
				public void update(GText text) {
					DicTime.setDays(text, blueprint.time.daysToHarvest());
				}
			}.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤daysToHarvest);
			s.addRightC(32, o);
			s.body().incrW(64);
			
			s.addRelBody(4, DIR.N, new GHeader(DicMisc.¤¤Production));
			
			section.addRelBody(8, DIR.S, s);
			
			s = new GuiSection();
			
			if (!blueprint.constructor.isIndoors)
				s.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.perc(text, getter.get().tData.fertility());
					}
				}.hv(Fertility.¤¤name, blueprint.constructor.fertility.desc()));
			
			s.addRightC(32, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, getter.get().tData.work());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(¤¤workValue);
					b.text( ¤¤workValueD);
					b.NL();
					b.textLL(DicTime.¤¤Today);
					b.tab(4);
					b.add(GFORMAT.perc(b.text(), getter.get().tData.workday()));
				};
				
			}.hv(¤¤workValue));
			
			s.addRightC(32, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, getter.get().tData.skill());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(¤¤skill);
					b.text( ¤¤skillD);
					b.NL();
					b.textLL(DicTime.¤¤Today);
					b.tab(4);
					b.add(GFORMAT.perc(b.text(), getter.get().tData.skillToday()));
					
					b.NL(8);
					
					Industry ii = getter.get().blueprintI().industries().get(0);
					IndustryUtil.hoverBoosts(b, 1.0, null, ii.bonus(), getter.get());
				};
				
			}.hv(¤¤skill));
			
			section.addRelBody(16, DIR.S, s);
			s = new GuiSection();
			
			s.addRightC(32, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, blueprint.moisture);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(SETT.WEATHER().moisture.info.name);
					b.text(SETT.WEATHER().moisture.info.desc);
					b.add(GFORMAT.perc(b.text(), blueprint.moisture));
				};
				
			}.hv(SETT.WEATHER().moisture.info.name));
			
			s.addRightC(32, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, blueprint.event);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(¤¤Event);
					b.text(¤¤EventD);
					b.add(GFORMAT.perc(b.text(), blueprint.event));
				};
				
			}.hv(¤¤Event));
			
			
			s.addRightC(32, new GStat() {
				
				@Override
				public void update(GText text) {
					text.add(getter.get().tData.cName());
				}
			}.hv(¤¤cycle));
			section.addRelBody(4, DIR.S, s);
			
			
		}
		
		
	
		
		
	}
	
	private RENDEROBJ prod(GETTER<FarmInstance> getter) {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				FarmInstance ins = getter.get();

				b.textL(¤¤baseValue);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), Util.base(ins)));
				b.NL();
				
				if (!blueprint.constructor.isIndoors) {
					b.textL(Fertility.¤¤name);
					b.tab(6);
					b.add(GFORMAT.f1(b.text(), ins.tData.fertility()));
					b.NL();
				}
				
				b.textL(¤¤workValue);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.tData.work()));
				b.NL();
				
				b.textL(¤¤skill);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.tData.skill()));
				b.NL();
				
				b.textL(SETT.WEATHER().moisture.info.name);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.blueprintI().moisture));
				b.NL();
				
				b.textL(¤¤Event);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), blueprint.event));
				b.NL();
				
				
				b.NL(4);
				
				b.textLL(¤¤estimated);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), Util.prospect(ins)));
				b.NL();
				
				b.NL(16);
				b.textL(¤¤HarvestYear);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ins.blueprintI().indus.get(0).outs().get(0).year.get(ins)));
				b.NL();
				
				b.NL(2);
				b.textL(¤¤HarvestPrev);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ins.blueprintI().indus.get(0).outs().get(0).yearPrev.get(ins)));
				b.NL();
				
			}
			
		};
		
		s.add(blueprint.crop.resource.icon(), 0, 0);
		GStat stat = new GStat() {
			
			@Override
			public void update(GText text) {
				double am = Util.prospect(getter.get())/TIME.years().bitConversion(TIME.days());
				GFORMAT.f(text, am);
			}
		};
		
		s.addRightC(6, stat);
		
		s.body().incrW(64);
		return s;
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
	}


}
