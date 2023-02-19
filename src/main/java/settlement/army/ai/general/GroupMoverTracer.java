package settlement.army.ai.general;

import settlement.army.Div;
import settlement.army.ai.general.Groups.GroupLine;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class GroupMoverTracer {

	private final Context c;
	private final VectorImp vec = new VectorImp();
	
	GroupMoverTracer(Context c){
		this.c = c;
	}


	public GDiv trace(int sx, int sy, GroupLine group) {
		
		vec.set(group.v);
		vec.rotate90();
		
		boolean up = true;
		boolean down = true;
		for (int i = 0; i < 64 && (up || down); i++) {
			if (up) {
				
				
				int tx = (int) (sx + vec.nX()*i);
				int ty = (int) (sy + vec.nY()*i);
				
				if (SETT.IN_BOUNDS(tx, ty) && !SETT.PATH().availability.get(tx, ty).isSolid(c.army)){
					GDiv div = get(tx, ty, group);
					if (div != null)
						return div;
				}else {
					up = false;
				}
				
			}
			if (down) {
				int tx = (int) (sx + vec.nX()*-i);
				int ty = (int) (sy + vec.nY()*-i);
				if (SETT.IN_BOUNDS(tx, ty) && !SETT.PATH().availability.get(tx, ty).isSolid(c.army)){
					GDiv div = get(tx, ty, group);
					if (div != null)
						return div;
				}else {
					down = false;
				}
			}
		}
		return null;
	}
	
	private GDiv get(int tx, int ty, GroupLine group) {
		
		ArrayList<Div> res = c.tmpList;
		res.clearSloppy();
		ArmyAIUtil.map().getAlly(res, tx, ty, c.army);
		Div result = null;
		for (Div d : res) {
			if (group.divsTmp.get(d.indexArmy())) {
				if (result == null || result.index() < d.index())
					result = d;
			}
				
		}
		if (result != null)
			return c.divs.get(result.indexArmy());
		return null;
		
	}
	
}
