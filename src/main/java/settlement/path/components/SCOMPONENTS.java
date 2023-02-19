package settlement.path.components;

import static settlement.main.SETT.*;

import init.RES;
import settlement.main.SETT;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class SCOMPONENTS {

	public final FindableDatas data = new FindableDatas();
	public final SComp0Level zero = new SComp0Level();
	public final LIST<SCompNLevel> levels;
	public final SCompNLevel last;
	public final LIST<SComponentLevel> all;
	public final SCompFinder pather;
	
	
	
	public SCOMPONENTS(){
		
		final int increase = 8;
		
		int ls = 0;
		int ss = SComp0Level.SIZE*increase;
		while(ss <= SETT.TWIDTH) {
			ss *= increase;
			ls++;
		}
		ls++;
		ArrayList<SComponentLevel> all = new ArrayList<>(ls+1);
		all.add(zero);
		ArrayList<SCompNLevel> levels = new ArrayList<>(ls);
		ss = SComp0Level.SIZE*increase;
		ls = 1;
		while(levels.hasRoom()) {
			SCompNLevel l = new SCompNLevel(all.get(all.size()-1), ls, ss);
			levels.add(l);
			all.add(l);
			ss *= increase;
			if (ss >  SETT.TWIDTH)
				ss = SETT.TWIDTH;
			ls++;
		}
		this.levels = levels;
		last = levels.get(levels.size()-1);
		this.all = all;

		
		pather = new SCompFinder(this, RES.pathTools());
		
		new SCompTests(this);
		new SCompUI(this);
		
		
	}
	
	public void clear() {
		for (SComponentLevel l : all)
			l.init();
	}
	
	public void init() {
		for (SComponentLevel l : all)
			l.init();
		update();
		
//		check();
	}
	
	public void update() {
	
		for (SComponentLevel l : all)
			l.update();
	}
	
//	public void check() {
//		SComponentChecker check = new SComponentChecker(zero);
//		check.init();
//		for (COORDINATE c : SETT.TILE_BOUNDS) {
//			SComponent cc = zero.get(c);
//			if (cc != null && !check.isSetAndSet(cc)) {
//				while(cc != null) {
//					if (cc.level().get(cc.centreX(), cc.centreY()) != cc) {
//						System.err.println(cc.centreX() + " " + cc.centreY() + " " + cc.level().level());
//					}
//					cc = cc.superComp();
//				}
//				
//				
//			}
//			
//			
//		}
//		
//	}
	
	public void updateAvailability(int tx, int ty) {
		zero.update(tx, ty);
	}
	
	public void updateService(int tx, int ty) {
		zero.changeSerives(tx, ty);
	}
	
	public final MAP_OBJECT<SComponent> superComp = new MAP_OBJECT<SComponent>() {

		@Override
		public SComponent get(int tile) {
			SComponent s = zero.get(tile);
			while(s != null && s.superComp() != null)
				s = s.superComp();
			return s;
		}

		@Override
		public SComponent get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return null;
		}
	
	};
	
}
