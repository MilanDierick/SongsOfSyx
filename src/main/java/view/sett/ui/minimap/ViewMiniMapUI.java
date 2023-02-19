package view.sett.ui.minimap;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STAT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Pendulum;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import util.gui.misc.GDropDown;
import view.interrupter.InterManager;
import view.main.VIEW;
import view.subview.GameWindow;

final class ViewMiniMapUI extends GuiSection {
	
	private final Pendulum shift = new Pendulum();
	private static  double shade;
	
	static final COLOR other = COLOR.WHITE85;
	static final COLOR enemy = new ColorShifting(new ColorImp(30, 5, 5), new ColorImp(128, 0, 0)).setSpeed(0.4);
	
	final static COLOR good = new ColorImp(0, 0, 100);
	final static COLOR bad = new ColorImp(100, 0, 0);
	
	static final ColorImp imp = new ColorImp();
	
	static final EntFunk eNone = new EntFunk();
	EntFunk currentE = eNone;
	
	static boolean showAnimal = true;
	static boolean showGrowables = true;
	static boolean showMinables = true;
	static RoomBlueprintIns<?> showRooms;
	static boolean showRoomsAll = true;
	
	public ViewMiniMapUI(InterManager i, GameWindow window, int maxZoomout, ViewMiniMap m){
		
		D.gInit(this);
		shift.setFactor(0.5);
		
		LIST<SPRITE> s = new ArrayList<>(
				UI.PANEL().panelL.get(DIR.E),
				UI.PANEL().panelL.get(DIR.W, DIR.E),
				UI.PANEL().panelL.get(DIR.W));
		addRight(0, s.get(0));
		
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
		addRight(0, s.get(1));
		addOnTop(c);
		
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
		addRight(0, s.get(1));
		addOnTop(c);
		
		c = new GButt.Panel(SPRITES.icons().m.wildlife) {
			@Override
			protected void clickA() {
				showAnimal = !showAnimal;
			}
			
			@Override
			protected void renAction() {
				selectedSet(showAnimal);
			}
			
		}.hoverInfoSet("show wildlife");
		addRight(0, s.get(1));
		addOnTop(c);
		
		c = new GButt.Panel(SPRITES.icons().m.clear_food) {
			@Override
			protected void clickA() {
				showGrowables = !showGrowables;
			}
			
			@Override
			protected void renAction() {
				selectedSet(showGrowables);
			}
			
		}.hoverInfoSet("show wild edibles");
		addRight(0, s.get(1));
		addOnTop(c);
		
		c = new GButt.Panel(SPRITES.icons().m.pickaxe) {
			@Override
			protected void clickA() {
				showMinables = !showMinables;
			}
			
			@Override
			protected void renAction() {
				selectedSet(showMinables);
			}
			
		}.hoverInfoSet("show minable resources");
		addRight(0, s.get(1));
		addOnTop(c);
		
		{
			int x1 = body().x2();
			
			GDropDown<CLICKABLE> select = new GDropDown<CLICKABLE>(SPRITES.icons().s.house);
			CharSequence all = DicMisc.¤¤All;
			c = new GButt.Glow(all){
				
				@Override
				protected void clickA() {
					showRooms = null;
					showRoomsAll = true;
				};
			};
			select.add(c);
			select.setSelected(c);
			
			c = new GButt.Glow(DicMisc.¤¤None){
				
				@Override
				protected void clickA() {
					showRooms = null;
					showRoomsAll = false;
				};
			};
			select.add(c);
			
			for (RoomBlueprint p : SETT.ROOMS().all()) {
				if (p instanceof RoomBlueprintIns<?>) {
					RoomBlueprintIns<?> pp = (RoomBlueprintIns<?>) p;
					c = new GButt.Glow(pp.info.names){
						
						@Override
						protected void clickA() {
							showRooms = pp;
						};
					}.hoverInfoSet(pp.info.desc);
					select.add(c);
				}
				
			}
			select.init();
			
			int x2 = x1 + select.body().width();
			while(body().x2() <= x2)
				addRight(0, s.get(1));
			select.body().centerY(body());
			select.body().moveX1(x1);
			add(select);
			
		}
		
		int x1 = body().x2();
		
		GDropDown<CLICKABLE> select = new GDropDown<CLICKABLE>(SPRITES.icons().s.human);
		EntFunk d = new EntFunk();
		CharSequence none = DicMisc.¤¤None;
		c = new GButt.Glow(none){
			
			@Override
			protected void clickA() {
				currentE = d;
			};
		};
		select.add(c);
		select.setSelected(c);
		
		for (STAT p : STATS.createMatterList(true, false, null)) {
			EntFunkImp f = new EntFunkImp(p);
			c = new GButt.Glow(p.info().name){
				
				@Override
				protected void clickA() {
					currentE = f;
				};
			}.hoverInfoSet(p.info().desc);
			select.add(c);
		}
		select.init();
		
		select.hoverInfoSet(D.g("entities", "Color entities based on status"));
		
		int x2 = x1 + select.body().width();
		add(s.get(1), body().x2(), body().y1());
		while(body().x2() < x2)
			addRight(0, s.get(1));
		addRightC(0, s.get(2));
		select.body().centerY(body());
		select.body().moveX1(x1);
		add(select);
		

		
		body().moveY1(30);
		body().moveX2(C.WIDTH()-50);
		
	}
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		shift.update(ds*4);
		shade = 0.2 + 0.8*(shift.get()+1)*0.5;
		super.render(r, ds);
	}
	

	
	public static class EntFunk {
		
		
		
		public COLOR get(ENTITY e) {
		
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hostile()) {
					return enemy;
				}
				imp.set(good);
				imp.setRed((int) ((imp.red()&0x0FF)*shade)).setGreen((int) ((imp.green()&0x0FF)*shade)).setBlue((int) ((imp.blue()&0x0FF)*shade));
			}else {
				return other;
			}
			return imp;
			
		}
		
	}
	
	public static class EntFunkMini {
		
		
		
		public COLOR get(ENTITY e) {
		
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hostile()) {
					return enemy;
				}
				return good;
			}else {
				return other;
			}
			
		}
		
	}
	
	private final class EntFunkImp extends EntFunk{
		
		private final STAT stat;
		
		EntFunkImp(STAT stat){
			this.stat = stat;
		}
		
		@Override
		public COLOR get(ENTITY e) {
		
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hostile()) {
					return enemy;
				}
				double d = stat.indu().getD(h.indu());
//				if (stat.standing().definition(h.race()).inverted)
//					d = 1.0-d;
				
				imp.interpolate(bad, good, d);
				return imp;
			}else {
				return other;
			}
			
		}
		
	}

	

}
