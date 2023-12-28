package view.sett.ui.bottom;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.category.RoomCategorySub;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEAN;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.table.GScrollRows;
import util.info.INFO;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.tool.PLACABLE;

final class BuildMain {

	private final Inter inter;
	private static final int BUTTH = 44;
	private static final int BUTTM = 8;
	private static final int HEIGHT = BUTTH*BUTTM;
	private final UIRoomPlacer placer;
	private final KeyMap<BParenter> map = new KeyMap<>();
	
	
	private static CharSequence ¤¤Build = "¤Build:";
	private static CharSequence ¤¤Fences = "¤Fences";
	private static CharSequence ¤¤Roads = "¤Roads";
	private static CharSequence ¤¤move = "¤Move";
	private static CharSequence ¤¤Construct = "¤Construct";
	private static CharSequence ¤¤terraform = "Terraform";

	static {
		D.ts(BuildMain.class);
	}
	
	
	BuildMain(Inter inter, UIRoomPlacer placer){
		this.inter = inter;
		this.placer = placer;
		IDebugPanelSett.add("Test bottom highligt", new ACTION() {
			
			@Override
			public void exe() {
				hilight("ACTION_MOVE_THRONE", new BOOLEAN() {
					double n = VIEW.renderSecond();
					@Override
					public boolean is() {
						return VIEW.renderSecond()-n < 10;
					}
				});
			}
		});
		
	}
	
	public void hilight(String key, BOOLEAN condition) {
		if (map.get(key) != null)
			map.get(key).higlight(condition);
		else
			LOG.ln(map.keysString());
	}
	
	public GuiSection create() {
		GuiSection s = new GuiSection();
		
		
		
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_AGRIULTURE,
				SETT.ROOMS().CATS.MAIN_INDUSTRY
			};
			BPanel p = create(s, UI.icons().l.work, SETT.ROOMS().CATS.MAIN_INDUSTRY.name);
			append(p, cats);
		}
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_INFRA,
			};
			BPanel p = create(s, UI.icons().l.infra, SETT.ROOMS().CATS.MAIN_INFRA.name);
			append(p, cats, SETT.ROOMS().CATS.MILITARY);
			
			
			{
				BPanel m = append(p, SETT.ROOMS().CATS.MILITARY);
				INFO i = new INFO(DicMisc.¤¤Fortifications, ""+ ¤¤Build + " " + DicMisc.¤¤Fortifications);
				new BAction(m, "FORTIFICATION", SPRITES.icons().m.fortification, i.name, i.desc, Jobs.normal(SETT.JOBS().build_fort, i));
				new BAction(m, "STAIRS", SETT.JOBS().build_stairs);
			}
			
//			{
//				BPanel exp = new BPanel();
//				exp.daddy = new BExp(p, SPRITES.icons().m.repair, ¤¤Construct, exp);
//				INFO i;
//				
//				{
//					ACTION a = new ACTION() {
//						@Override
//						public void exe() {
//							VIEW.inters().popup.close();
//							VIEW.s().uiManager.disturb();
//							VIEW.s().tools.place(ROOMS().THRONE.placer);
//						}
//					};
//					String name = ¤¤move + " " + ROOMS().THRONE.info.name;
//					
//					new BAction(exp, "MOVE_THRONE", ROOMS().THRONE.icon(), name, name, a) {
//						
//						@Override
//						public void hoverInfoGet(GUI_BOX text) {
//							ROOMS().THRONE.placer.hoverDesc((GBox) text);
//							super.hoverInfoGet(text);
//						}
//					};
//				}
//				
//				i = new INFO(¤¤Fences, ""+ ¤¤Build + " " + ¤¤Fences);
//				new BAction(exp, "FENCES", SETT.JOBS().fences.get(0).placer().getIcon(), i.name, i.desc, Jobs.normal(SETT.JOBS().fences, i));
//				
//				i = new INFO(¤¤Roads, ""+ ¤¤Build + " " + ¤¤Roads);
//				new BAction(exp, "ROADS", SETT.JOBS().roads.get(0).placer().getIcon(), i.name, i.desc, Jobs.normal(SETT.JOBS().roads, i));
//				
//				i = new INFO(DicMisc.¤¤Structures, ""+ ¤¤Build + " " + DicMisc.¤¤Structures);
//				new BAction(exp, "STRUCTURES", SETT.JOBS().build_structure.get(0).combo.getIcon(), i.name, i.desc, Jobs.struct(i));
//				
//				RoomCategorySub cat = SETT.ROOMS().CATS.DECOR;
//				ACTION ac = new ACTION() {
//					
//					@Override
//					public void exe() {
//						for (RoomBlueprintImp b : cat.rooms())
//							if (b.reqs.passes(FACTIONS.player())) {
//								VIEW.inters().popup.close();
//								VIEW.s().uiManager.disturb();
//								placer.init(b, cat);
//								return;
//							}
//					}
//				};
//				new BAction(exp, "DECOR", cat.icon(), cat.name(), ""+ ¤¤Build + " " + cat.name(), ac);
//				
//				for (RoomBlueprintImp r : SETT.ROOMS().CATS.MAIN_MISC.misc.rooms())
//					new BRoom(exp, r);
//			}
			
			
			
		}
		{
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				SETT.ROOMS().CATS.MAIN_SERVICE,
			};
			BPanel p = create(s, UI.icons().l.service, SETT.ROOMS().CATS.MAIN_SERVICE.name);
			append(p, cats);
		}
		{

			
			
			BPanel p = create(s, UI.icons().l.jobs, DicMisc.¤¤Misc);
			
			
			
			{
				ACTION a = new ACTION() {
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().uiManager.disturb();
						VIEW.s().tools.place(ROOMS().THRONE.placer);
					}
				};
				String name = ¤¤move + " " + ROOMS().THRONE.info.name;
				
				new BAction(p, "MOVE_THRONE", ROOMS().THRONE.icon(), name, name, a) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						ROOMS().THRONE.placer.hoverDesc((GBox) text);
						super.hoverInfoGet(text);
					}
				};
			}
			
			for (RoomBlueprintImp r : SETT.ROOMS().CATS.MAIN_MISC.misc.rooms())
				new BRoom(p, r);
			
			{
			
				BPanel exp = new BPanel();
				exp.daddy = new BExp(p, SPRITES.icons().m.repair, ¤¤Construct, exp);
				
				INFO i = new INFO(¤¤Fences, ""+ ¤¤Build + " " + ¤¤Fences);
				new BAction(exp, "FENCES", SETT.JOBS().fences.get(0).placer().getIcon(), i.name, i.desc, Jobs.normal(SETT.JOBS().fences, i));
				
				i = new INFO(¤¤Roads, ""+ ¤¤Build + " " + ¤¤Roads);
				new BAction(exp, "ROADS", SETT.JOBS().roads.get(0).placer().getIcon(), i.name, i.desc, Jobs.normal(SETT.JOBS().roads, i));
				
				i = new INFO(DicMisc.¤¤Structures, ""+ ¤¤Build + " " + DicMisc.¤¤Structures);
				new BAction(exp, "STRUCTURES", SETT.JOBS().build_structure.get(0).combo.getIcon(), i.name, i.desc, Jobs.struct(i));
				
				RoomCategorySub cat = SETT.ROOMS().CATS.DECOR;
				ACTION ac = new ACTION() {
					
					@Override
					public void exe() {
						for (RoomBlueprintImp b : cat.rooms())
							if (b.reqs.passes(FACTIONS.player())) {
								VIEW.inters().popup.close();
								VIEW.s().uiManager.disturb();
								placer.init(b, cat);
								return;
							}
					}
				};
				new BAction(exp, "DECOR", cat.icon(), cat.name(), ""+ ¤¤Build + " " + cat.name(), ac);
			}
			{
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().tools.place(SETT.JOBS().clearss.food.placer());
					}
				};
				new BAction(p, "JOB_FORRAGE", SETT.JOBS().clearss.food.placer().getIcon(),SETT.JOBS().clearss.food.placer().name(), SETT.JOBS().clearss.food.placer().desc(), a);
			}
			
			{
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.inters().popup.close();
						VIEW.s().tools.place(SETT.JOBS().clearss.hunt);
					}
				};
				new BAction(p, "JOB_HUNT", SETT.JOBS().clearss.hunt.getIcon(),SETT.JOBS().clearss.hunt.name(), SETT.JOBS().clearss.hunt.desc(), a);
			}
			
			{
				
				BPanel exp = new BPanel();
				exp.daddy = new BExp(p, SETT.JOBS().clearss.woodAndRock.getIcon(), ¤¤terraform, exp);
				
				int id = 0;
				
				for (PLACABLE pp : SETT.JOBS().clearss.placers) {
					new BAction(exp, "JOB_CLEAR_" + id, pp, "");
					id++;
				}
				
//				ACTION a = new ACTION() {
//					
//					@Override
//					public void exe() {
//						VIEW.inters().popup.close();
//						VIEW.s().tools.place(SETT.JOBS().clearss.lastActivated);
//					}
//				};
//				new BAction(p, "JOB_TERRAFORM", SETT.JOBS().clearss.woodAndRock.getIcon(), ¤¤terraform, ¤¤terraformD, a);
			}
			RoomCategoryMain[] cats = new RoomCategoryMain[] {
				
			};
			append(p, cats);
		}
		
//		{
//			
//			
//			BPanel p = create(s, UI.icons().m.cancel, "");
//			
//			B c = new B(p, UI.icons().m.time, ¤¤planning) {
//				
//				@Override
//				protected void clickA() {
//					SETT.JOBS().planMode.toggle();
//				};
//				
//				@Override
//				protected void renAction() {
//					selectedSet(SETT.JOBS().planMode.is());
//				};
//				
//				
//			};
//			c.hoverInfoSet(¤¤planningD);
//			
//			new BAction(p, "ACTIVATE", JOBS().tool_activate, JOBS().tool_activate.desc());
//			new BAction(p, "DORMANT", JOBS().tool_dormant, JOBS().tool_dormant.desc());
//			
//			new BAction(p, "REPAIR", JOBS().tool_repair, JOBS().tool_repair.desc());
//			new BAction(p, "REMOVE_SMART", JOBS().tool_remove_smartl, JOBS().tool_remove_smartl.desc());
//			new BAction(p, "REMOVE_ALL", JOBS().tool_remove_all, JOBS().tool_remove_all.desc());
//			new BAction(p, "REMOVE_JOB", JOBS().tool_clear, JOBS().tool_clear.desc());
//			
//			RoomCategoryMain[] cats = new RoomCategoryMain[] {
//				
//			};
//			append(p, cats);
//		}
		
		return s;
		
	}
	
	private BPanel create(GuiSection mainS, SPRITE icon, CharSequence label) {
		BPanel panel = new BPanel();
		BMain main = new BMain(panel, icon, label);
		panel.daddy = main;
		mainS.addRightC(0, main);
		return panel;
	}
	
	private void append(BPanel panel, RoomCategoryMain[] cats, RoomCategorySub... ignore) {

		
		LinkedList<RoomBlueprintImp> misc = new LinkedList<>();
		
		for (RoomCategoryMain m : cats) {
			ouer:
			for (RoomCategorySub ss : m.subs) {
				for (RoomCategorySub so : ignore)
					if (so == ss)
						continue ouer;
				
				append(panel, ss);
			}
			for (RoomBlueprintImp b : m.misc.rooms()) {
				misc.add(b);
			}
		}
		
		if (misc.size() > 0) {
			BPanel exp = new BPanel();
			exp.daddy = new BExp(panel, UI.icons().m.questionmark, cats[0].misc.name(), exp);
			for (RoomBlueprintImp b : misc) {
				new BRoom(exp, b);
			}
		}
	}
	
	private BPanel append(BPanel panel, RoomCategorySub ss) {

		BPanel exp = new BPanel();
		exp.daddy = new BExp(panel, ss.icon(), ss.name(), exp);
		
		for (RoomBlueprintImp b : ss.rooms()) {
			new BRoom(exp, b);
		}
		return exp;
	}
	
	interface BParenter {
		
		public BParenter parent();
		public CLICKABLE cl();
		public void higlight(BOOLEAN condition);
	}
	
	private class BPanel implements BParenter{
		
		private BParenter daddy;
		private GuiSection section;
		final ArrayListGrower<B> butts = new ArrayListGrower<>();
		
		
		
		@Override
		public CLICKABLE cl() {
			if (section == null) {
				section = new SPanel();
				section.body().setHeight(HEIGHT);
				section.body().setWidth(1);
				CLICKABLE bbs;
				
				if (butts.size() > BUTTM) {
					LinkedList<CLICKABLE> rows = new LinkedList<>();
					for (B b : butts)
						rows.add(b.cl());
					bbs = new GScrollRows(rows, HEIGHT).view();
				}else {
					GuiSection bb = new GuiSection();
					for (B b : butts) {
						bb.addDown(0, b.cl());
					}
					bbs = bb;
				}
				
				
				
				int dy = HEIGHT;
				if (daddy instanceof BExp) {
					BPanel pp = ((B) daddy).daddy;
					dy = daddy.cl().body().y1()-pp.section.body().y1();
				}
				
				bbs.body().moveCY(dy);
				
				if (bbs.body().y2() > section.body().y2())
					bbs.body().moveY2(section.body().y2());
				
				if (bbs.body().y1() < section.body().y1())
					bbs.body().moveY1(section.body().y1());
				
				section.add(bbs);
				section.pad(3, 8);
				
			}
			
			return section;
		}

		@Override
		public BParenter parent() {
			return daddy;
		}

		@Override
		public void higlight(BOOLEAN condition) {
			daddy.higlight(condition);
		}
	}
	
	private class B extends BButt implements BParenter {

		private final BPanel daddy;
		private BOOLEAN con;
		
		public B(BPanel panel, SPRITE icon, CharSequence label) {
			super(icon, label);
			this.daddy = panel;
			daddy.butts.add(this);
		}

		@Override
		public BParenter parent() {
			return daddy;
		}

		@Override
		public CLICKABLE cl() {
			return this;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
			if (con != null) {
				if (con.is()) {
					COLOR.RED2RED.renderFrame(r, body, 0, 3);
				}else
					con = null;
			}
		}

		@Override
		public void higlight(BOOLEAN condition) {
			con = condition;
			daddy.higlight(condition);
		}
		
		
	}
	
	private class BMain extends GButt.ButtPanel implements BParenter {

		private final BPanel pop;
		private BOOLEAN con;
		
		public BMain(BPanel panel, SPRITE icon, CharSequence label) {
			super(icon);
			hoverTitleSet(label);
			this.pop = panel;
		}

		@Override
		public BParenter parent() {
			return null;
		}

		@Override
		public CLICKABLE cl() {
			return this;
		}
		
		@Override
		protected void clickA() {
			inter.set(this, pop.cl());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			super.render(r, ds, isActive, isSelected, isHovered);
			if (con != null) {
				if (con.is()) {
					COLOR.RED2RED.renderFrame(r, body, 0, 3);
				}else
					con = null;
			}
		}

		@Override
		public void higlight(BOOLEAN condition) {
			con = condition;
		}
		
	}
	
	private final class BExp extends B implements BParenter {

		private final BPanel exp;
		
		public BExp(BPanel panel, SPRITE icon, CharSequence label, BPanel exp) {
			super(panel, icon, label);
			this.exp = exp;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			isSelected |= inter.exp == exp;
			super.render(r, ds, isActive, isSelected, isHovered);
			UI.icons().m.arrow_right.renderCY(r, body().x2()-32, body.cY());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				inter.exp(this, exp.cl());
			}
			return super.hover(mCoo);
		}
		
	}
	
	private class BRoom extends  B {

		private final RoomBlueprintImp room;
		private final CLICKABLE wrap;
		
		public BRoom(BPanel panel, RoomBlueprintImp room) {
			super(panel, room.icon, room.info.name);
			this.room = room;
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					inter.hide();
					placer.init(room, -1,-1);
				}
			};
			String key = "BUILD_" + room.key;
			wrap = KeyButt.wrap(a, this, KEYS.SETT(), key, room.info.name, ¤¤Build + " " + room.info.name);
			map.put(key, this);
			clickActionSet(a);
			SearchToolPanel.add(wrap, room.info.name);
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			UIRoomBuild.hoverRoomBuild(room, text);
		}
		
		@Override
		protected void renAction() {
			activeSet(room.reqs.passes(FACTIONS.player()));
		}
		
		@Override
		public CLICKABLE cl() {
			return wrap;
		}
		
		@Override
		protected void clickA() {
			
		}
		
	}
	
	private class BAction extends  B {

		private final CLICKABLE wrap;
		
		public BAction(BPanel panel, String key, SPRITE icon, CharSequence name, CharSequence desc, ACTION action) {
			super(panel, icon, name);
			key = "ACTION_" + key;
			wrap = KeyButt.wrap(action, this, KEYS.SETT(), key, name, desc);
			map.put(key, this);
			clickActionSet(action);
			SearchToolPanel.add(wrap, name);
		}
		
		public BAction(BPanel panel, String key, Job job) {
			this(panel, key, job.placer(), job.placer().desc);
			
		}
		
		public BAction(BPanel panel, String key, PLACABLE place, CharSequence desc) {
			super(panel, place.getIcon(), place.name());
			key = "ACTION_" + key;
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().uiManager.disturb();
					VIEW.s().tools.place(place);
				}
			};
			wrap = KeyButt.wrap(a, this, KEYS.SETT(), key, place.name(), desc);
			map.put(key, this);
			clickActionSet(a);
			SearchToolPanel.add(wrap, place.name());
		}
		
		@Override
		public CLICKABLE cl() {
			return wrap;
		}
		
		@Override
		protected void clickA() {
			
		}
		
	}
	
}
