package settlement.army.ai.divs;

import static settlement.army.ai.divs.Plans.Plan.*;

import settlement.army.Div;
import settlement.army.formation.DivPosition;
import settlement.army.formation.DivPositionAbs;
import snake2d.util.datatypes.*;
import snake2d.util.sets.ArrayList;

final class ToolsColl {

	private final VectorImp vec = new VectorImp();
	private final VectorImp vec2 = new VectorImp();
	private final Coo coo = new Coo();
	private final ArrayList<Div> friendlyColls = new ArrayList<>(16);
	private final DivPosition otherpos = new DivPosition();
	private final PathDiv opath = new PathDiv();
	
	
	ToolsColl(Tools t){

	}
	

	
	public int isCollidingWithFriendly() {
		
		if (path.isDest())
			return 0;
		path.currentIInc(1);
		int sx = path.x();
		int sy = path.y();
		path.currentIInc(-1);
		vec.set(path.x(), path.y(), sx, sy);
		
		
		int res = 0;
		friendlyColls.clearSloppy();
		for (Div o : status.friendlyCollisions(friendlyColls)) {
			o.order().current.get(otherpos);
			COORDINATE c = getCentrePixel(otherpos);
			if (c != null) {
				vec2.set(next.centrePixel(), c);
				if (vec.nX()*vec2.nX() + vec2.nY()*vec.nY() > 0) {
					res = 1;
					if (isSuperColliding(vec, o))
						return 2;
				}
			}
			
		}
		
		return res;
		
	}
	
	private boolean isSuperColliding(VectorImp vec, Div div) {
		AIManager mo = ARMY_AI_DIVS.self.getOther(div);
		if (mo.plan() instanceof PlanWalkAbs) {
			PlanWalkAbs p = (PlanWalkAbs) mo.plan();
			if (p.colTimer.get(mo) > 0) {
				div.order().path.get(opath);
				if (!opath.isDest()) {
					opath.currentIInc(1);
					int sx = opath.x();
					int sy = opath.y();
					opath.currentIInc(-1);
					vec2.set(opath.x(), opath.y(), sx, sy);
					if (vec.nX()*vec2.nX() + vec2.nY()*vec.nY() > 0) {
						return div.index() > m.order.index;
						
					}
				}
			}
		}
		return false;
	}
	
	COORDINATE getCentrePixel(DivPositionAbs pos) {
		if (pos.deployed() == 0)
			return null;
		int cx = 0;
		int cy = 0;
		for (int i = 0; i < pos.deployed(); i++) {
			cx+= pos.pixel(i).x();
			cy+= pos.pixel(i).y();
		}
		cx /= pos.deployed();
		cy /= pos.deployed();
		coo.set(cx, cy);
		return coo;
	}

	

	
}
