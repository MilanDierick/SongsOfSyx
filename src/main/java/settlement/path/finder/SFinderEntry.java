package settlement.path.finder;

import settlement.main.SETT;
import settlement.path.components.SComponent;

public final class SFinderEntry implements SFINDER{

	SFinderEntry(){
		
	}
	
	public boolean find(int sx, int sy, SPath path, int max) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s != null && s.edgeMask() != 0) {
			return path.request(sx, sy, this, max);
		}
		return false;
	}
	
	@Override
	public boolean isInComponent(SComponent c, double distance) {
		return c.edgeMask() != 0;
	}

	@Override
	public boolean isTile(int tx, int ty, int tile) {
		return tx == 0 || tx == SETT.TWIDTH-1 || ty == 0 || ty == SETT.THEIGHT-1;
	}

}
