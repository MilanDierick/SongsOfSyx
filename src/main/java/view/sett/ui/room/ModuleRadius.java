package view.sett.ui.room;

import init.D;
import init.sprite.SPRITES;
import settlement.main.*;
import settlement.path.components.SCompFinder.SCompPath;
import settlement.room.main.*;
import settlement.room.main.job.ROOM_RADIUS;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.colors.GCOLORS_MAP;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.slider.GGaugeMutable;
import util.rendering.ShadowBatch;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleRadius implements ModuleMaker{

	private final CharSequence ¤¤NAME = "¤Radius";
	private final CharSequence ¤¤PROBLEM = "¤No work is within the radius!";
	private final CharSequence ¤¤DESC = "¤Set the work radius of this room. Subjects will look for work within the radius. A big radius can be ineffective and workers will have a hard time getting back to services when work is done.";
	
	private RoomInstance i;
	
	private final ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
		
		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			if (i == null)
				return;
			ROOM_RADIUS_INSTANCE ra = (ROOM_RADIUS_INSTANCE) i;
			
			SCompPath check = SETT.PATH().comps.pather.fill(i.mX(), i.mY(), ra.radius());
			RenderData.RenderIterator it = data.onScreenTiles();
			GCOLORS_MAP.map_ok.bind();
			while(it.has()) {
				if (check.is(it.tx(), it.ty()) && !i.is(it.tile())) {
					SPRITES.cons().BIG.dashedThick.render(r, 0x0F, it.x(), it.y());
				}
				it.next();
			}
			COLOR.unbind();
			remove();
		}
	};
	
	public ModuleRadius(Init init) {
		
		D.t(this);
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		
		if (p instanceof ROOM_RADIUS) {
			l.add(new I(p));
		}
		
	}
	
	
	private class I extends UIRoomModule {
		
		private final RoomBlueprint p;
		
		I(RoomBlueprint b){
			this.p = b;
		}
		
		
		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			
			
			
			if (p instanceof ROOM_RADIUSE) {
				
				GHeader h = new GHeader(¤¤NAME) {
					@Override
					protected void render(snake2d.SPRITE_RENDERER r, float ds, boolean isHovered) {
						super.render(r, ds, isHovered);
						i = get.get();
						ren.add();
					}; 
				};
				h.hoverInfoSet(¤¤DESC);
				section.addRelBody(8, DIR.S, h);
				
				ROOM_RADIUSE r = (ROOM_RADIUSE) p;
				INTE i = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return 100;
					}
					
					@Override
					public int get() {
						return r.radiusRaw(get.get());
					}
					
					@Override
					public void set(int t) {
						r.radiusRawSet(get.get(), (byte) t);
					}
				};
				GGaugeMutable m = new GGaugeMutable(i, 260);
				section.addRelBody(2, DIR.S, m);
				
			}else if (p instanceof ROOM_RADIUS) {
				section.add(new RENDEROBJ.RenderImp() {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						i = get.get();
						ren.add();
					}
				});
				
			}
			
			
			
			
		}
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			ModuleRadius.this.i = (RoomInstance) i;
			//ren.add();
		}
		
		@Override
		public void problem(GBox box, Room room, int rx, int ry) {
			ROOM_RADIUS_INSTANCE i = (ROOM_RADIUS_INSTANCE) room;
			if (!i.searching()) {
				box.error(¤¤PROBLEM);
				box.NL(2);
			}
		}
	}
	
}
