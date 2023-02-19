package settlement.path.finder;

import static settlement.main.SETT.*;

import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.FindableDataSingle;
import settlement.path.components.SComponent;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;

public final class SFinderHumanoid {

	private Humanoid asker;
	private FindableDataSingle ti;
	private Humanoid res;
	

	
	private final SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			int am = ti.get(c);
			for (DIR d: DIR.ALLC)
				if (SETT.PATH().comps.zero.get(asker.tc(), d) == c) {
					am --;
					break;
				}
			return am > 0;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
				if (e instanceof Humanoid && e != asker) {
					res = (Humanoid) e;
					return true;
				}
			}
			return false;
		}
	};
	
	{
//		new TestPath("other people", fin) {
//			
//			@Override
//			protected void place(int sx, int sy, SPath p) {
//				asker = null;
//				ti = PATH().comps.data.people(true);
//				super.place(sx, sy, p);
//			}
//			
//		};
	}
	
	public Humanoid find(Humanoid client, int radius) {
		
		asker = client;
		ti = PATH().comps.data.people(!client.indu().hostile());
		
		if (STATS.POP().POP.data(null).get(null, 0) < 2)
			return null;
		
		if (SETT.PATH().finders.finder().find(client.tc().x(), client.tc().y(), fin, radius) != null) {
			return res;
		}
		
		return null;
		
	}
	
	public SComponent findComp(Humanoid client, int radius) {
		asker = client;
		ti = PATH().comps.data.people(!client.indu().hostile());
		return SETT.PATH().comps.pather.get(client.ssx(), client.ssy(), fin, radius);
	}
	
}