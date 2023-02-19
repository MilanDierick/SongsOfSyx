package settlement.army.ai.general;

import init.C;
import settlement.army.formation.DivFormation;
import snake2d.PathTile;
import snake2d.util.datatypes.VectorImp;

final class PathingUtil {

	private final VectorImp vec = new VectorImp();
	private final Context c;
	
	public PathingUtil(Context c) {
		this.c = c;
	}
	
	public PathTile getLastStraighPathTile (int sx, int sy, PathTile t) {
		
		if (t.getParent() == null)
			return t;
		
		int dist = 10;
		PathTile safe = t;
		PathTile current = t;

		for (int i = 0; i < dist; i++) {
			if (current.getParent() == null) {
				return safe;
			}
			current = current.getParent();
		}
		
		vec.set(safe.x(), safe.y(), current.x(), current.y());
		double nx = vec.nX();
		double ny = vec.nY();
		
		while(current.getParent() != null) {
			PathTile start = current;
			for (int i = 0; i < dist; i++) {
				if (current.getParent() == null) {
					return safe;
				}
				current = current.getParent();
			}
			vec.set(start, current);
			if (nx*vec.nX() + ny*vec.nY() <= 0.8) {
				safe = start;
				nx = vec.nX();
				ny = vec.nY();
			}
		}
		
		return safe;

	}
	
	public DivFormation getDest(int sx, int sy, GDiv d, PathTile dest) {
		
		if (dest.getParent() == null) {
			vec.set(sx, sy, dest);
			vec.rotate90();
		}else {
			vec.set(dest.getParent(), dest);
			vec.rotate90();
		}
		
		
		int rm = (int) (1 + Math.sqrt(d.div().menNrOf()/2.0))*d.div().settings.formation.size;
		return c.deployer.deployArroundCentre(d.div().menNrOf(), d.div().settings.formation, (dest.x()<<C.T_SCROLL) + C.TILE_SIZEH, (dest.y()<<C.T_SCROLL) + C.TILE_SIZEH, vec.nX(), vec.nY(), rm, c.army);
		
	}
	
	public PathTile reverse(PathTile abs) {
		if (abs.getParent() != null){
			PathTile p = abs.getParent();
			abs.parentSet(null);
			abs =reverse(abs, p);
		}
		return abs;
	}
	
	private PathTile reverse(PathTile newParent, PathTile t) {
		if (t.getParent() == null) {
			t.parentSet(newParent);
			return t;
		}
		PathTile res = reverse(t, t.getParent());
		t.parentSet(newParent);
		return res;
		
	}
	
	
}
