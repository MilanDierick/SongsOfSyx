package view.sett.ui.minimap;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.BOOLEAN.BOOLEANImp;
import util.data.BOOLEANO;
import util.data.GETTER_TRANS;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.table.GScrollRows;
import view.interrupter.IPopCurrent;
import view.interrupter.InterManager;
import view.main.VIEW;
import view.subview.GameWindow;

final class ViewMiniMapUI extends GuiSection {
	

	public static final COLOR other = COLOR.WHITE85;
	public static final COLOR enemy = new ColorShifting(new ColorImp(30, 5, 5), new ColorImp(128, 0, 0)).setSpeed(0.4);
	public final static COLOR good = new ColorImp(0, 0, 100);
	private final static COLOR bad = new ColorImp(100, 0, 0);
	private static final ColorImp imp = new ColorImp();
	

	public ViewMiniMapUI(InterManager i, GameWindow window, int maxZoomout, ViewMiniMap m){
		
		D.gInit(this);

		{
			CLICKABLE c;
			
			c = new GButt.Panel(SPRITES.icons().m.plus) {
				@Override
				protected void clickA() {
					window.zoomInc(-1);
					if (window.zoomout() < 3) {
						VIEW.s().getWindow().centerAt(window.pixels().cX(), window.pixels().cY());
						m.hide();
					}
				}
			};
			addRight(0, c);
			
			c = new GButt.Panel(SPRITES.icons().m.minus) {
				@Override
				protected void clickA() {
					window.zoomInc(1);
				}
				
				@Override
				protected void renAction() {
					activeSet(window.zoomout() < maxZoomout);
				}
				
			};
			addRight(0, c);
			
			c = new GButt.Panel(SPRITES.icons().m.wildlife) {
				@Override
				protected void clickA() {
					showAnimals.toggle();
				}
				
				@Override
				protected void renAction() {
					selectedSet(showAnimals.is());
				}
				
			}.hoverInfoSet(DicMisc. ¤¤Animals);
			addRight(0, c);
			
			c = new GButt.Panel(SPRITES.icons().m.clear_food) {
				@Override
				protected void clickA() {
					showGrowable.toggle();
				}
				
				@Override
				protected void renAction() {
					selectedSet(showGrowable.is());
				}
				
			}.hoverInfoSet(DicMisc.¤¤Growth);
			addRight(0, c);
			
			c = new GButt.Panel(SPRITES.icons().m.pickaxe) {
				@Override
				protected void clickA() {
					showMinerals.toggle();
				}
				
				@Override
				protected void renAction() {
					selectedSet(showMinerals.is());
				}
				
			}.hoverInfoSet(DicMisc.¤¤Minerals);
			addRight(0, c);
		}
		
		
		{
			
			LinkedList<RENDEROBJ> bbs = new LinkedList<>();
			
			bbs.add(new BSearchable(UI.icons().m.ok, DicMisc.¤¤All) {
				
				@Override
				protected void clickA() {
					bitsRooms.setAll(true);
				}
				
			});
			
			bbs.add(new BSearchable(UI.icons().m.cancel, DicMisc.¤¤None) {
				
				@Override
				protected void clickA() {
					bitsRooms.setAll(false);
				}
				
			});
			
			LinkedList<BSearchable> all = new LinkedList<>();
			
			for (RoomBlueprint b : SETT.ROOMS().all()) {
				if (b instanceof RoomBlueprintIns<?>) {
					RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
					all.add(new BSearchable(bb.icon.big, bb.info.names) {
						
						@Override
						protected void clickA() {
							bitsRooms.toggle(bb.index());
						}
						
						@Override
						protected void renAction() {
							selectedSet(bitsRooms.get(bb.index()));
						}
						
					});
					
				}
			}
			
			IPopCurrent ii = bSearchList(bbs, all);
			
			CLICKABLE c = new GButt.Panel(UI.icons().m.building) {
				@Override
				protected void clickA() {
					ii.show(this);
				}
			}.hoverTitleSet(DicMisc.¤¤Buildings);
			
			int x1 = body().x2();
			
			
			
			
			c.body().centerY(body());
			c.body().moveX1(x1);
			add(c);
			
		}
		
		{
			
			LinkedList<RENDEROBJ> bbs = new LinkedList<>();
			
			bbs.add(new BSearchable(UI.icons().m.ok, DicMisc.¤¤All) {
				
				@Override
				protected void clickA() {
					bitsHType.setAll(true);
				}
				
			});
			
			bbs.add(new BSearchable(UI.icons().m.cancel, DicMisc.¤¤None) {
				
				@Override
				protected void clickA() {
					bitsHType.setAll(false);
				}
				
			});
			
			LinkedList<BSearchable> all = new LinkedList<>();
			
			for (HTYPE t : HTYPE.ALL()) {
				all.add(new BSearchable(UI.icons().s.human, t.names) {
					
					@Override
					protected void clickA() {
						bitsHType.toggle(t.index());
					}
					
					@Override
					protected void renAction() {
						selectedSet(bitsHType.get(t.index()));
					}
					
				});
			}
			
			IPopCurrent ii = bSearchList(bbs, all);
			
			CLICKABLE c = new GButt.Panel(UI.icons().m.citizen) {
				@Override
				protected void clickA() {
					ii.show(this);
				}
			}.hoverTitleSet(DicMisc.¤¤Population);
			
			int x1 = body().x2();
			
			
			
			
			c.body().centerY(body());
			c.body().moveX1(x1);
			add(c);
			
		}
		
		{
			
			LinkedList<RENDEROBJ> bbs = new LinkedList<>();
			
			bbs.add(new BSearchable(UI.icons().m.ok, DicMisc.¤¤All) {
				
				@Override
				protected void clickA() {
					bitsEmployed.setAll(true);
				}
				
			});
			
			bbs.add(new BSearchable(UI.icons().m.cancel, DicMisc.¤¤None) {
				
				@Override
				protected void clickA() {
					bitsEmployed.setAll(false);
				}
				
			});
			
			LinkedList<BSearchable> all = new LinkedList<>();
			
			for (RoomBlueprint b : SETT.ROOMS().all()) {
				if (b.employment() != null && b instanceof RoomBlueprintIns<?>) {
					RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
					all.add(new BSearchable(bb.icon.big, bb.employment().title) {
						
						@Override
						protected void clickA() {
							bitsEmployed.toggle(bb.index());
						}
						
						@Override
						protected void renAction() {
							selectedSet(bitsEmployed.get(bb.index()));
						}
						
					});
					
				}
			}
			
			IPopCurrent ii = bSearchList(bbs, all);
			
			CLICKABLE c = new GButt.Panel(UI.icons().m.workshop) {
				@Override
				protected void clickA() {
					ii.show(this);
				}
			}.hoverTitleSet(DicMisc.¤¤Employment);
			
			int x1 = body().x2();
			
			
			
			
			c.body().centerY(body());
			c.body().moveX1(x1);
			add(c);
			
		}
		
		{
			LinkedList<RENDEROBJ> bbs = new LinkedList<>();
			
			
			bbs.add(new BSearchable(UI.icons().m.cancel, DicMisc.¤¤Clear) {
				
				@Override
				protected void clickA() {
					statC = null;
				}
				
			});
			
			LinkedList<BSearchable> all = new LinkedList<>();
			
			for (STAT p : STATS.createMatterList(true, false, null)) {
				all.add(new BSearchable(UI.icons().m.heart, p.info().name) {
					
					@Override
					protected void clickA() {
						if (statC == p)
							statC = null;
						else
							statC = p;
					}
					
					@Override
					protected void renAction() {
						selectedSet(statC == p);
					}
					
				});
			}
			
			IPopCurrent ii = bSearchList(bbs, all);
			
			CLICKABLE c = new GButt.Panel(UI.icons().m.heart) {
				@Override
				protected void clickA() {
					ii.show(this);
				}
			}.hoverTitleSet(DicMisc.¤¤Happiness);
			
			int x1 = body().x2();
			
			
			
			
			c.body().centerY(body());
			c.body().moveX1(x1);
			add(c);
		}
		
//		int x1 = body().x2();
//		
//		CLICKABLE c;
//		
//		GDropDown<CLICKABLE> select = new GDropDown<CLICKABLE>(SPRITES.icons().s.human);
//		EntFunk d = new EntFunk();
//		CharSequence none = DicMisc.¤¤None;
//		c = new GButt.Glow(none){
//			
//			@Override
//			protected void clickA() {
//				currentE = d;
//			};
//		};
//		select.add(c);
//		select.setSelected(c);
//		
//		for (STAT p : STATS.createMatterList(true, false, null)) {
//			EntFunkImp f = new EntFunkImp(p);
//			c = new GButt.Glow(p.info().name){
//				
//				@Override
//				protected void clickA() {
//					currentE = f;
//				};
//			}.hoverInfoSet(p.info().desc);
//			select.add(c);
//		}
//		select.init();
//		
//		select.hoverInfoSet(D.g("entities", "Color entities based on status"));
//		
//		
//		select.body().centerY(body());
//		select.body().moveX1(x1);
//		add(select);
		

		
		body().moveY1(30);
		body().moveX2(C.WIDTH()-50);
		
	}
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		UI.PANEL().butt.render(r, body(), 0);
		super.render(r, ds);
	}
	
	private static class BSearchable extends GButt.ButtPanel {

		public final String search;
		
		public BSearchable(SPRITE icon, CharSequence label) {
			super(label);
			search = ""+label;
			icon(icon);
			body.setWidth(300);
		}
		
		
	}
	
	private static IPopCurrent bSearchList(LIST<RENDEROBJ> pre, LIST<BSearchable> sss) {
		

		StringInputSprite input = new StringInputSprite(10, UI.FONT().M);
		input.placeHolder(DicMisc.¤¤Search);
		
		GInput in = new GInput(input);
		
		IPopCurrent pop = new IPopCurrent() {
			@Override
			public void show(CLICKABLE trigger) {
				super.show(trigger);
				in.focus();
			}
		};
		
		GuiSection s = pop.expansion;
		
		for (RENDEROBJ r : pre)
			s.addDown(0, r);
		
		s.addDown(8, in);;
		
		GScrollRows rr = new GScrollRows(sss, sss.get(0).body.height()*10) {
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (input.text().length() == 0)
					return true;
				BSearchable s = (BSearchable) o;
				return Str.containsText(s.search, input.text());
			}
		};
		
		s.addDown(4, rr.view());
		
		return pop;
	}


	
	public final BOOLEANImp showAnimals = new BOOLEANImp(true);
	public final BOOLEANImp showGrowable = new BOOLEANImp(true);
	public final BOOLEANImp showMinerals = new BOOLEANImp(true);
	
	private final Bitmap1D bitsHType = new Bitmap1D(HTYPE.ALL().size(), false);
	private final Bitmap1D bitsRooms = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
	private final Bitmap1D bitsEmployed = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
	{
		bitsHType.setAll(true);
		bitsRooms.setAll(true);
		bitsEmployed.setAll(true);
	}
	private boolean bitUnemployed = true;
	private STAT statC = null;
	
	public final BOOLEANO<Humanoid> showHuman = new BOOLEANO<Humanoid>() {

		@Override
		public boolean is(Humanoid t) {
			if (bitsHType.get(t.indu().hType().index())) {
				RoomInstance ins = STATS.WORK().EMPLOYED.get(t);
				if (ins == null)
					return bitUnemployed;
				return bitsEmployed.get(ins.blueprint().index());
			}
			return false;
		}
	
	};
	
	public final BOOLEANO<RoomInstance> showRoom = new BOOLEANO<RoomInstance>() {

		@Override
		public boolean is(RoomInstance t) {
			return bitsRooms.get(t.blueprintI().index());
		}
	
	};
	
	public final GETTER_TRANS<Humanoid, COLOR> colorCode = new GETTER_TRANS<Humanoid, COLOR>() {
		
		@Override
		public COLOR get(Humanoid h) {
			if (h.indu().hostile()) {
				return enemy;
			}
			if (statC != null) {
				double d = statC.indu().getD(h.indu());
//				if (stat.standing().definition(h.race()).inverted)
//					d = 1.0-d;
				
				imp.interpolate(bad, good, d);
				return imp;
			}else {
				return good;
			}
		}
	};


}
