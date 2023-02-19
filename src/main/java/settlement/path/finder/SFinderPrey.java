package settlement.path.finder;

import static settlement.main.SETT.*;

import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import snake2d.util.datatypes.COORDINATE;

public final class SFinderPrey {

	private Animal a;
	
	SFinderPrey() {
		new TestPath("Animal", fin);
	}
	
	private final SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return PATH().comps.data.reservableAnimals.get(c) > 0;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
				if (e instanceof Animal) {
					a = (Animal) e;
					if (a.reservable()) {
						return true;
					}
				}
			}
			return false;
		}
	};
	
	public Animal findAndReserve(COORDINATE start, SPath p, int radius) {
		
		if (p.request(start.x(), start.y(), fin, radius)) {
			a.reserve();
			return a;
		}
		
		return null;
		
	}
	
}