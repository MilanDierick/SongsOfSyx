package settlement.overlay;

import init.RES;
import settlement.main.SETT;
import settlement.path.components.SCompFinder.SCompPath;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.LISTE;
import util.colors.GCOLORS_MAP;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

final class ServiceRadius extends Addable{

	private ROOM_SERVICE_ACCESS_HASER ser;
	private RoomInstance ins;
	private SCompPath check;
	
	ServiceRadius(LISTE<Addable> all){
		super(all, null, null,null, true, false);
		exclusive = true;
	}

	public void add(ROOM_SERVICE_ACCESS_HASER ser, RoomInstance ins) {
		super.add();
		this.ser = ser;
		this.ins = ins;
	}
	
	@Override
	public void initBelow(RenderData data) {
		
		RES.coos().set(0);
		
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && ser.service().service(c.x(), c.y()) != null) {
				RES.coos().get().set(c.x(), c.y());
				RES.coos().inc();
			}
		}
		
		check = SETT.PATH().comps.pather.fill(RES.coos(), ser.service().radius);
		
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		
		
		COLOR c = COLOR.WHITE10;
		if (check.is(it.tile())) {
			c = COLOR.WHITE50;
			if (SETT.ENV().service.has(ser.service().finder, it.tx(), it.ty())) {
				c = GCOLORS_MAP.bestOverlay;
				if (!SETT.ENV().service.is(ser.service().finder, it.tx(), it.ty()))
					c = GCOLORS_MAP.worstOverlay;
				
			}
		}
		
		
		renderUnder(c, r, it);
		
	}
	
	@Override
	public void finishBelow() {
		
	}
	
}
