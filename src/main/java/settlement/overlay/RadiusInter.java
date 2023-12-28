package settlement.overlay;

import init.C;
import init.RES;
import init.sprite.SPRITES;
import settlement.environment.SEService;
import settlement.main.SETT;
import settlement.path.components.*;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.RoomBlueprintIns;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

final class RadiusInter extends Addable{

	private RoomBlueprintIns<? extends RADIUS_INTER> blue;
	private SFinderFindable fin;
	private final int half = SEService.quad/2;
	private final int m = SEService.quad-1;
	private double er;
	private int ex, ey;
	
	RadiusInter(LISTE<Addable> all){
		super(all, null, null,null, true, true);
		exclusive = true;
	}

	public void add(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin, int ex, int ey, double er) {
		super.add();
		this.er = er;
		this.ex = ex;
		this.ey = ey;
		this.blue = blue;
		this.fin = fin;
		
	}
	
	public void add(RoomBlueprintIns<? extends RADIUS_INTER> blue, SFinderFindable fin) {
		add(blue, fin, -1, -1, -1);

	}
	

	
	private double value(int tx, int ty) {
		
		SComponent c = SETT.PATH().comps.zero.get(tx, ty);
		if (c == null || !RES.flooder().hasBeenPushed(c.centreX(), c.centreY()))
			return 0;
		
		double v = RES.flooder().getValue(c.centreX(), c.centreY());
		int i = (int) RES.flooder().getValue2(c.centreX(), c.centreY());
		
		if (i < 0 || i >= blue.instancesSize())
			return (v/er);
		RADIUS_INTER r = blue.getInstance(i);
		return v/r.radius();
	}
	
	@Override
	public void initBelow(RenderData data) {
		RES.flooder().init(this);
		addPoint(ex, ey, er, -1);
		for (int i = 0; i < blue.instancesSize(); i++) {
			RADIUS_INTER r = blue.getInstance(i);
			addPoint(r.rx(), r.ry(), r.radius(), i);
		}
		
		SComp0Level comps = SETT.PATH().comps.zero;
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollGreatest();
			SComponent c = comps.get(t);
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				double v = t.getValue()-e.cost2();
				if (v >= 0) {
					if (RES.flooder().pushGreater(e.to().centreX(), e.to().centreY(), v) != null) {
						RES.flooder().setValue2(e.to().centreX(), e.to().centreY(), t.getValue2());
					}
				}
				e = e.next();
			}
		}
	}
	
	private void addPoint(int tx, int ty, double radius, int value2) {
		if (radius <= 1)
			return;
		SComp0Level comps = SETT.PATH().comps.zero;
		SComponent c = comps.get(tx, ty);
		if (c == null) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				c = comps.get(tx, ty, d);
				if (c != null)
					break;
			}
		}
		if (c != null) {
			if (RES.flooder().pushSloppy(c.centreX(), c.centreY(), radius) != null)
				RES.flooder().setValue2(c.centreX(), c.centreY(), value2);
		}
			
	}

	@Override
	public boolean render(Renderer r, RenderIterator it) {
		
		if ((it.tx() & m) != half || (it.ty() & m) != half) {
			return false;
		}
		
		if (SETT.ENV().service.has(fin, it.tx(), it.ty())) {
			SPRITE s = SETT.ENV().service.is(fin, it.tx(), it.ty()) ?
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
		double v = value(it.tx(), it.ty());
		renderUnder(v, r, it, false);
		
	}
	
	@Override
	public void finishBelow() {
		RES.flooder().done();
	}
	
}
