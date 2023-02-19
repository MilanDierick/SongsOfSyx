package view.sett.ui.home;

import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.main.*;
import settlement.main.RenderData.RenderIterator;
import settlement.room.home.HOME_TYPE;
import settlement.room.home.house.HomeHouse;
import snake2d.Renderer;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GButt;
import util.rendering.ShadowBatch;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class UIHomeAssign extends PlacableMulti{

	private static CharSequence ¤¤name = "assign";
	private static CharSequence ¤¤desc = "lets you assign homes to specific criteria.";
	private static CharSequence ¤¤prob = "Must be placed on a house.";
	static {
		D.ts(UIHomeAssign.class);
	}
	
	private Race race;
	private HCLASS clas;
	private LIST<CLICKABLE> sec;
	
	public UIHomeAssign() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.citizen);
		
		
		
	}
	
	private class Butt extends GButt.ButtPanel{
		
		final Race tt;
		
		Butt(Race race){
			super(race.appearance().icon);
			tt = race;
			hoverTitleSet(race.info.name);
			hoverInfoSet(race.info.desc);
		}
		
		@Override
		protected void clickA() {
			if (activeIs())
				race = tt;
		}
		
		@Override
		protected void renAction() {
			activeSet(clas != null);
			selectedSet(clas != null && race == tt);
		}
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (SETT.ROOMS().HOMES.HOME.is(tx, ty))
			return null;
		return ¤¤prob;
	}

	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (SETT.ROOMS().HOMES.HOME.is(tx, ty)) {
			HOME_TYPE t = HOME_TYPE.EVERYONE();
			if (clas == HCLASS.CITIZEN) {
				t = HOME_TYPE.CITIZEN(race);
			}else if (clas == HCLASS.SLAVE){
				t = HOME_TYPE.SLAVE(race);
			}
			SETT.ROOMS().HOMES.HOME.house(tx, ty, this).settingSet(t).done();
		}
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return SETT.ROOMS().HOMES.HOME.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}

	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		
		ren.add();
		if (sec != null)
			return sec;
		GuiSection sec = new GuiSection();
		
		sec.addRightC(0, new GButt.ButtPanel(HOME_TYPE.EVERYONE().icon()) {
			
			@Override
			protected void clickA() {
				clas = null;
				race = null;
			}
			
			@Override
			protected void renAction() {
				selectedSet(clas == null);
			}
			
		}.hoverSet(HOME_TYPE.EVERYONE()));
		
		sec.addRightC(8, new GButt.ButtPanel(HCLASS.CITIZEN.icon()) {
			
			@Override
			protected void clickA() {
				clas = HCLASS.CITIZEN;
			}
			
			@Override
			protected void renAction() {
				selectedSet(clas == HCLASS.CITIZEN);
			}
			
		}.hoverSet(HCLASS.CITIZEN));
		
		sec.addRightC(0, new GButt.ButtPanel(HCLASS.SLAVE.icon()) {
			
			@Override
			protected void clickA() {
				clas = HCLASS.SLAVE;
			}
			
			@Override
			protected void renAction() {
				selectedSet(clas == HCLASS.SLAVE);
			}
			
		}.hoverSet(HCLASS.SLAVE));
		
		int y1 = sec.body().y2();
		int i = 0;
		
		sec.add(new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			
			@Override
			protected void clickA() {
				race = null;
			}
			
			@Override
			protected void renAction() {
				activeSet(clas != null);
				selectedSet(clas != null && race == null);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (clas == HCLASS.CITIZEN) {
					text.title(HOME_TYPE.CITIZEN(null).name);
					text.text(HOME_TYPE.CITIZEN(null).desc);
				}else if (clas == HCLASS.SLAVE) {
					text.title(HOME_TYPE.SLAVE(null).name);
					text.text(HOME_TYPE.SLAVE(null).desc);
				}
			}
			
		}, (i%8)*32, y1+(i/8)*32);
		i++;
		

		for (Race r : RACES.all()) {
			sec.add(new Butt(r), (i%8)*32, y1+(i/8)*32);
			i++;
		}
		this.sec = new ArrayList<CLICKABLE>(
			sec
		);
		return this.sec;
	}
	
	
	private final ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
		
		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			RenderIterator it = data.onScreenTiles();
			while(it.has()) {
				if ((it.tx() % 3) == 0 && (it.ty() % 3) == 0) {
					HomeHouse h = SETT.ROOMS().HOMES.HOME.house(it.tx(), it.ty(), this);
					if (h != null) {
						int dx = (h.body().cX()-it.tx())*C.TILE_SIZE;
						int dy = (h.body().cY()-it.ty())*C.TILE_SIZE;
						
						HOME_TYPE t = h.setting();
						int w = t.icon().width()*C.SCALE;
						int hi = t.icon().height()*C.SCALE;
						int x1 = it.x()+dx-(w-C.TILE_SIZE)/2;
						int y1 = it.y()+dy-(hi-C.TILE_SIZE)/2;
						
						t.icon().render(r, x1, x1+w, y1, y1+hi);
						h.done();
					}
				}
				it.next();
			}
			remove();
		}
	};
	
}
