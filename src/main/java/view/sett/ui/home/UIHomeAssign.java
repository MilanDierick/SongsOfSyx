package view.sett.ui.home;

import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.room.home.HOMET;
import settlement.room.home.HomeSettings.HomeSetting;
import settlement.room.home.HomeSettings.HomeSettingTmp;
import settlement.room.home.house.HomeHouse;
import snake2d.Renderer;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class UIHomeAssign extends PlacableMulti{

	private static CharSequence ¤¤name = "assign";
	private static CharSequence ¤¤desc = "lets you assign homes to specific criteria.";
	private static CharSequence ¤¤prob = "Must be placed on a house.";
	
	private static CharSequence ¤¤everyone = "Set permission for everyone";
	private static CharSequence ¤¤none = "Set permission for none";
	private static CharSequence ¤¤permission = "Set permission for:";
	private static CharSequence ¤¤permissionAll = "Set permission for All:";
	
	
	static {
		D.ts(UIHomeAssign.class);
	}
	
	private final HomeSettingTmp tmp = new HomeSettingTmp();

	private final LIST<CLICKABLE> butts;
	
	public UIHomeAssign() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.citizen);
		
		GuiSection sec = new GuiSection();
		sec.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			
			@Override
			protected void clickA() {
				tmp.setEveryone();
			}
			
		}.hoverInfoSet(¤¤everyone));
		sec.addRightC(0, new GButt.ButtPanel(HCLASS.CITIZEN.icon()) {
			
			@Override
			protected void clickA() {
				tmp.clear();
				for (Race r : RACES.all())
					tmp.set(HOMET.get(HCLASS.CITIZEN, r));
			}
			
		}.hoverInfoSet(¤¤permissionAll + " " + HCLASS.CITIZEN.names));
		sec.addRightC(0, new GButt.ButtPanel(HCLASS.SLAVE.icon()) {
			
			@Override
			protected void clickA() {
				tmp.clear();
				for (Race r : RACES.all())
					tmp.set(HOMET.get(HCLASS.SLAVE, r));
			}
			
		}.hoverInfoSet(¤¤permissionAll + " " + HCLASS.SLAVE.names));
		sec.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			
			@Override
			protected void clickA() {
				tmp.clear();
			}
			
		}.hoverInfoSet(¤¤none));
		
		GuiSection s = new GuiSection();
		for (HOMET t : HOMET.ALL()) {
			if (t.race == null)
				continue;
			CLICKABLE bb = new GButt.ButtPanel(t.icon) {
				
				@Override
				protected void clickA() {
					if (selectedIs())
						tmp.clear(t);
					else
						tmp.set(t);
				}
				
				@Override
				protected void renAction() {
					selectedSet(tmp.is(t));
				}
			}.hoverInfoSet(¤¤permission + " " + t.name);
			
			if (s.getLastX2() > 500) {
				bb.body().moveX1(0).moveY1(s.body().y2());
				s.add(bb);
			}else
				s.addRightC(0, bb);
			
		}
		
		sec.addRelBody(8, DIR.S, s);
		
		butts = new ArrayList<CLICKABLE>(sec);
		
		tmp.setEveryone();
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (SETT.ROOMS().HOMES.HOME.is(tx, ty))
			return null;
		return ¤¤prob;
	}

	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (SETT.ROOMS().HOMES.HOME.isService(tx + ty*SETT.TWIDTH)) {
			SETT.ROOMS().HOMES.settings.set(tx, ty, tmp);
		}
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return SETT.ROOMS().HOMES.HOME.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}

	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		
		ren.add();		
		return butts;
	}
	
	
	private final ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
		
		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			
			RenderIterator it = data.onScreenTiles();
			while(it.has()) {
				if (SETT.ROOMS().HOMES.HOME.isService(it.tile())) {
					HomeHouse h = SETT.ROOMS().HOMES.HOME.house(it.tx(), it.ty(), this);
					render(r, h, it);
					h.done();
				}
				it.next();
			}
			remove();
		}
		
		private final ArrayList<HOMET> rens = new ArrayList<>(HOMET.ALL().size());
		
		private void render(Renderer r, HomeHouse h, RenderIterator it) {
			
			double dx = h.body().x1()+h.body().width()*0.5;
			dx = dx -h.service().x();
			int cx = (int) (it.x() + dx*C.TILE_SIZE);
			
			double dy = h.body().y1()+h.body().height()*0.5;
			dy = dy -h.service().y();
			int cy = (int) (it.y() + dy*C.TILE_SIZE);
			
			HomeSetting t = h.setting();
			int am = 0;
			HOMET single = null;
			for (HOMET hh : HOMET.ALL()) {
				if (t.is(hh)) {
					single = hh;
					am++;
				}
			}
			
			if (am == HOMET.ALL().size()-1) {
				renderSingle(r, cx, cy, UI.icons().m.questionmark);
			}else if (am == 0) {
				renderSingle(r, cx, cy, UI.icons().m.cancel);
			}else if (am == 1) {
				renderSingle(r, cx, cy, single.icon);
			}else if (am < HOMET.ALL().size()/2) {
				rens.clearSloppy();
				for (HOMET hh : HOMET.ALL()) {
					if (t.is(hh)) {
						rens.add(hh);
					}
				}
				renderMany(r, cx, cy, h, false);
			}else {
				rens.clearSloppy();
				for (HOMET hh : HOMET.ALL()) {
					if (!t.is(hh)) {
						rens.add(hh);
					}
				}
				renderMany(r, cx, cy, h, true);
			}
		}
		
		private void renderSingle(Renderer r, int cx, int cy, SPRITE icon) {
			int w = icon.width()*C.SCALE;
			int h = icon.height()*C.SCALE;
			int x1 = cx-w/2;
			int y1 = cy-h/2;
			icon.render(r, x1, x1+w, y1, y1+h);
		}
		
		private void renderMany(Renderer r, int cx, int cy, HomeHouse house, boolean anti) {
			
			int width = house.body().width()*C.TILE_SIZE;
			int w = 24*C.SCALE/2;
			int h = rens.get(0).icon.height()*C.SCALE/2;
			
			int dx = (width/rens.size());
			dx = CLAMP.i(dx, 1, w);
			
			int x1 = cx - dx*rens.size()/2;
			int y1 = cy - h;
			
			for (HOMET t : rens) {
				t.icon.render(r, x1, x1+w, y1, y1+h);
				if (anti)
					UI.icons().m.anti.render(r, x1, x1+w, y1, y1+h);
				x1 += dx;
			}
		}
	};
	
}
