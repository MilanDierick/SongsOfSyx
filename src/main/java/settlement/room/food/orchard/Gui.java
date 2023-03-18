package settlement.room.food.orchard;

import game.GAME;
import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import settlement.tilemap.Fertility;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<Instance, ROOM_ORCHARD> {

	private static CharSequence ¤¤Trees = "¤Trees";
	private static CharSequence ¤¤TreesD = "¤Amount of fully grown trees. Only when trees are fully grown will they start producing. Neglected trees will die.";
	private static CharSequence ¤¤TreeNext = "¤Next tree will be grown in:";
	
	
	private static CharSequence ¤¤estimated = "¤Estimated Harvest (year)";
	private static CharSequence ¤¤daysToHarvest = "¤Days until harvest";
	private static CharSequence ¤¤baseValue = "¤Base Value";
	
	private static CharSequence ¤¤HarvestYear = "¤This Year";
	private static CharSequence ¤¤HarvestPrev = "¤Last Year";
	
	private static CharSequence ¤¤skillNext = "¤Skill Next";
	private static CharSequence ¤¤skillNextD = "¤The skill and effort of work from previous year. This skill is what bears the fruit of the current year.";
	
	private static CharSequence ¤¤skillPrev = "¤Skill Current";
	private static CharSequence ¤¤skillPrevD = "¤Skill is boosts from workers that have been put into the the orchard during the growth cycle. Skill is counted when the trees are grown. High skill also makes trees grow quicker.";
	private static CharSequence ¤¤skillCurrent = "¤Skill currently put in";
	
	private static CharSequence ¤¤chop = "¤Chop";
	private static CharSequence ¤¤chopD = "¤Reset all progress by chopping down the trees and instantly get {0} {1}.";
	
	Gui(ROOM_ORCHARD s) {
		super(s);
		D.t(this);
	}
	
	private final Cache cache = new Cache();
	
	@Override
	public void hover(GBox box, Instance i) {
		super.hover(box, i);
		box.NL();
		if (!i.blueprintI().constructor.isIndoors) {
			box.text(blueprint.constructor.fertility.name());
			box.add(GFORMAT.perc(box.text(), i.fertility()));
			
			box.NL();
		}
		box.text(¤¤estimated);
		box.add(GFORMAT.i(box.text(), (int)cache.output(i)));
		
		box.space();
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid text, GuiSection sExtra) {
		
		IndustryResource res = blueprint.industries().get(0).outs().get(0);
		
		text.add(new GHeader(DicMisc.¤¤Production));
		
		GuiSection s = new GuiSection();
		
		s.add(res.resource.icon(), 0, 0);
		
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
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<Instance> getter, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		
		s.add(prod(getter));
		
		RENDEROBJ o = new GStat() {
			@Override
			public void update(GText text) {
				DicTime.setDays(text, blueprint.time.daysTillHarvest());
			}
		}.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤daysToHarvest);
		s.addRightC(32, o);
		s.body().incrW(64);
		
		s.addRelBody(4, DIR.N, new GHeader(DicMisc.¤¤Production));
		section.addRelBody(8, DIR.S, s);
		
		
		s = new GuiSection();
		
		int tab = 180;
		
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				Instance ins = getter.get();
				GFORMAT.iofkInv(text, cache.trees(ins), cache.treesTotal(ins));
				
				if (cache.daysTillNextTree(ins) < Integer.MAX_VALUE) {
					text.s().add('(').s();
					DicTime.setDays(text, cache.daysTillNextTree(ins));
					text.s().add(')');
				}
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤Trees);
				b.text(¤¤TreesD);
				b.NL(8);
				
				if (cache.daysTillNextTree(getter.get()) < Integer.MAX_VALUE) {
					b.textL(¤¤TreeNext);
					GText t = b.text();
					DicTime.setDays(t, cache.daysTillNextTree(getter.get()));
					b.add(t);
				}
			};
			
		}.hh(¤¤Trees, tab).increaseWidth(100));
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, getter.get().fertility());
				
				if (cache.fertilityNext(getter.get()) != getter.get().fertility()) {
					text.s().add('-').add('>').s();
					GFORMAT.perc(text, cache.fertilityNext(getter.get()));
				}
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤Fertility);
				b.text(DicMisc.¤¤FertilityD);
			};
			
		}.hh(DicMisc.¤¤Fertility, tab).increaseWidth(100));
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, SETT.WEATHER().moisture.growthValue());
			}
			
		}.hh(SETT.WEATHER().moisture.info.name, SETT.WEATHER().moisture.info.desc, tab).increaseWidth(100));
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f1(text, getter.get().skillPrev());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤skillNext);
				b.text(¤¤skillNextD);
				b.NL(8);
				b.textLL(¤¤skillCurrent);
				b.NL(8);
				Industry ii = getter.get().blueprintI().industries().get(0);
				IndustryUtil.hoverBoosts(b, 1.0, null, ii.bonus(), getter.get());
				
			};
			
		}.hh(¤¤skillNext, tab).increaseWidth(100));
		
		s.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f1(text, getter.get().skill());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤skillPrev);
				b.text(¤¤skillPrevD);
				b.NL(8);
				b.textLL(¤¤skillCurrent);
				b.NL(8);
				Industry ii = getter.get().blueprintI().industries().get(0);
				IndustryUtil.hoverBoosts(b, 1.0, null, ii.bonus(), getter.get());
				
			};
			
		}.hh(¤¤skillPrev, tab).increaseWidth(100));
		
		section.add(s, section.body().x1(), section.body().y2()+16);
		
		
		CLICKABLE c = new GButt.ButtPanel(¤¤chop) {
			
			@Override
			protected void renAction() {
				activeSet(cache.wood > 0);
			};
			
			@Override
			protected void clickA() {
				
				Instance ins = getter.get();
				
				for (COORDINATE c : ins.body()) {
					if (!ins.is(c))
						continue;
					OTile t = blueprint.tile.getM(c.x(), c.y());
					if (t != null) {
						t.chop();
					}
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				GText t = b.text();
				t.add(¤¤chopD);
				t.insert(0, cache.wood);
				t.insert(1, blueprint.auxRes.resource().names);
				b.add(t);
			};
			
		}.pad(8, 4);
		
		section.addRelBody(16, DIR.S, c);
		
	}
	
	private RENDEROBJ prod(GETTER<Instance> getter) {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				Instance ins = getter.get();
				
				b.textL(¤¤baseValue);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), blueprint.productionData.outs().get(0).rate*blueprint.time.days));
				b.NL();
				
				b.textL(DicMisc.¤¤Area);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), ins.base));
				b.NL();
				
				b.textL(Fertility.¤¤name);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.fertility()));
				b.NL();
				
				b.textL(SETT.WEATHER().moisture.info.name);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), blueprint.moisture));
				b.NL();
				
				b.textL(¤¤skillNext);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.skillPrev()));
				b.NL();
				
				b.textL(¤¤Trees);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), (double)cache.trees/cache.treesTotal));
				b.NL();
				
				b.NL(4);
				
				b.textLL(¤¤estimated);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), cache.output(ins)));
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
		
		s.add(blueprint.productionData.outs().get(0).resource.icon(), 0, 0);
		GStat stat = new GStat() {
			
			@Override
			public void update(GText text) {
				double am = cache.output(getter.get());
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
	
	private class Cache {
		
		private int upI = -1;
		private int treesTotal;
		private int trees;
		private int daysTillNextTree;
		private int wood;
		private double fertilityNext;
		private double output;
		
		private Instance ins;
		
		public int treesTotal(Instance ins) {
			up(ins);
			return treesTotal;
		}
		
		public int trees(Instance ins) {
			up(ins);
			return trees;
		}
		
		public int daysTillNextTree(Instance ins) {
			up(ins);
			return daysTillNextTree;
		}
		
		public double fertilityNext(Instance ins) {
			up(ins);
			return fertilityNext;
		}
		
		public double output(Instance ins) {
			up(ins);
			return output;
		}
		
		private void up(Instance ins) {

			
			
			if (upI == GAME.updateI() && this.ins == ins)
				return;
			this.ins = ins;
			upI = GAME.updateI();
			
			wood = 0;
			fertilityNext = 0;
			treesTotal = 0;
			trees = 0;
			daysTillNextTree = Integer.MAX_VALUE;
			
			for (COORDINATE c : ins.body()) {
				if (!ins.is(c))
					continue;
				
				OTile t = blueprint.tile.getM(c.x(), c.y());
				if (t != null) {
					treesTotal ++;
					if (t.state() == t.IBIG)
						trees ++;
					else {
						int d = t.state().daysTillGrown();
						if (d < daysTillNextTree) {
							
							daysTillNextTree = d;
						}
					}
					if (t.state() == t.ISMALL )
						wood+= blueprint.auxRes.amount()/2;
					else if (t.state() == t.IBIG || t.state() == t.IDEAD)
						wood+= blueprint.auxRes.amount();
				}
			
				fertilityNext += (int) (SETT.FERTILITY().target.get(c.x(), c.y())*OTile.BFER.mask);
			}
			
			fertilityNext /= (ins.area()*OTile.BFER.mask);
			
			output = trees*blueprint.time.days*ins.skillPrev()*blueprint.moisture*ins.fertility()*ins.base*blueprint.productionData.outs().get(0).rate/treesTotal;
		}
		
	}


}
