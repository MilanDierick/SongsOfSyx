package settlement.overlay;

import init.C;
import init.RES;
import init.sprite.SPRITES;
import settlement.environment.SEService;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.components.SCompFinder.SCompPath;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;

final class ServiceRadius extends Addable{

	private ROOM_SERVICE_ACCESS_HASER ser;
	private RoomInstance ins;
	private SCompPath check;
	private final int half = SEService.quad/2;
	private final int m = SEService.quad-1;
	
	ServiceRadius(LISTE<Addable> all){
		super(all, null, null,null, true, true);
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
	public boolean render(Renderer r, RenderIterator it) {
		
		if ((it.tx() & m) != half || (it.ty() & m) != half) {
			return false;
		}
		
		if (SETT.ENV().service.has(ser.service().finder, it.tx(), it.ty())) {
			SPRITE s = SETT.ENV().service.is(ser.service().finder, it.tx(), it.ty()) ?
					SPRITES.icons().s.alert : SPRITES.icons().s.allRight;
			
			int X1 = it.x()-8;
			int Y1 = it.y()-8;
			int X2 = it.x()+C.TILE_SIZE+16;
			int Y2 = it.y()+C.TILE_SIZE+16;
			
			COLOR.BLACK.bind();
			
			s.render(r, X1+8, X2+8, Y1+8, Y2+8);
			
			COLOR.unbind();
			
			s.render(r, X1, X2, Y1, Y2);
		}
		
		return false;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		
		double v = check.is(it.tile()) ? 1 : 0;
		renderUnder(v, r, it, false);
		
	}
	
	@Override
	public void finishBelow() {
		
	}
	
}
