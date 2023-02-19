package settlement.maintenance;

import static settlement.main.SETT.*;

import init.boostable.BOOSTABLES;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class MFloor {
	
	public int rate = 0x01F;
	private final Data data;
	
	
	MFloor(Data data){
		this.data = data;
	}
	
	public void update(int tx, int ty, int i) {
		if (validate(tx, ty)) {
			if (!RND.oneIn(rate))
				return;
			
			double c = CLAMP.d(1.0/BOOSTABLES.CIVICS().MAINTENANCE.get(null, null), 0, 1000);
			if ((1.0-FLOOR().getter.get(tx, ty).durability) <= RND.rFloat()*c)
				return;
			FLOOR().degradeInc(i, 1);
			if (RND.oneIn(2))
				SETT.GRASS().grow(tx, ty);
			data.setter.set(tx, ty);
			if (data.resourceSetter.get(i) == 0 && 0.5*SETT.FLOOR().degrade.get(tx, ty)*SETT.FLOOR().getter.get(tx, ty).resAmount > RND.rFloat()*5)
				data.resourceSetter.set(i, 1);
		}
	}
	
	public boolean validate(int tx, int ty) {
		if (FLOOR().getter.is(tx, ty)) {
//			if (FLOOR().degrade(tx, ty) == 0) {
//				data.setter.clear(tx, ty);
//			}
			return true;
		}
		data.setter.clear(tx, ty);
		return false;
	}
	
	void maintain(int tx, int ty) {
		
		if (FLOOR().getter.is(tx, ty)) {
			int i = tx+ty*TWIDTH;
			FLOOR().degradeInc(i, -4);
			if (FLOOR().degrade(tx, ty) == 0)
				data.setter.clear(i);
			GRASS().current.set(tx,  ty, 0);
		}
	}

	public void vandalise(short tx, short ty) {
		if (FLOOR().getter.is(tx, ty)) {
			FLOOR().degradeInc(tx+ty*SETT.TWIDTH, 8);
			data.setter.set(tx, ty);
		}
		
	}

	
}