package settlement.army.ai.general;

import init.C;
import init.config.Config;
import settlement.army.Army;
import settlement.army.Div;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.army.ai.util.DivTDataStatus;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.*;

final class GroupsIniterTargets {

	private final ArrayList<Target> active = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final LIST<Target> all;
	
	private final ArrayList<TargetDiv> tmpEnemy = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final ArrayList<TargetDiv> tmpEnemy2 = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final LIST<TargetDiv> allEnemy;
	
	private final Context c;
	private final Army a;
	private final IntChecker check = new IntChecker(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final DivTDataStatus status = new DivTDataStatus();
	
	private int MAX_DIST = 64;
	
	GroupsIniterTargets(Context c){
		
		this.a = c.army.enemy();
		this.c = c;
		
		ArrayList<Target> all = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		while(all.hasRoom())
			all.add(new Target());
		this.all = all;
		
		
		ArrayList<TargetDiv> allE = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		while(allE.hasRoom())
			allE.add(new TargetDiv());
		this.allEnemy = allE;

	}
	
	public static class Target {
		
		private Target() {
			
		}
		
		
		public final Coo coo = new Coo();
		public final Coo centre = new Coo();
		public int men;
		public ArrayCooShort coos = new ArrayCooShort(Config.BATTLE.DIVISIONS_PER_ARMY);
		public double power;
		private double minPowerAllocated;
		private double maxPowerAllocated;
		
		public double powerMin() {
			return minPowerAllocated;
		}
		
		public double powerMax() {
			return maxPowerAllocated;
		}
		
		private final ArrayList<GDiv> divs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		
		public void register(GDiv div) {
			divs.add(div);
			power += div.div().settings.power;
		}
	}
	
	public static class TargetDiv {
		
		private Div div;
		public int tx,ty;
	}
	
	
	public LIST<Target> get(){
		
		active.clearSloppy();
		tmpEnemy.clearSloppy();
		
		for (int di = 0; di < Config.BATTLE.DIVISIONS_PER_ARMY; di++) {
			Div d = a.divisions().get(di);
			if (d.order().active()) {
				d.order().status.get(status);
				int tx = status.currentPixelCX()>>C.T_SCROLL;
				int ty = status.currentPixelCY()>>C.T_SCROLL;
				if (SETT.IN_BOUNDS(tx, ty)) {
					TargetDiv e = allEnemy.get(di);
					e.tx = tx;
					e.ty = ty;
					e.div = d;
					tmpEnemy.add(e);
				}
				
			}
		}
		
		
		
		AbsMap m = c.absMap;
		m.clear();
		for (TargetDiv e : tmpEnemy) {
			m.set(e.tx>> AbsMap.scroll, e.ty>> AbsMap.scroll, 1);
		}
		
		
		Flooder f = c.flooder.getFlooder();
		f.init(this);
		
		check.init();
		
		for (TargetDiv e : tmpEnemy) {
			check.isSetAndSet(e.div.indexArmy());
		}
		while (!tmpEnemy.isEmpty()) {
			TargetDiv e = tmpEnemy.removeLast();	
			if (!check.isSet(e.div.indexArmy()))
				continue;
			
			Target t = findTarget(e);
			if (t != null) {
				active.add(t);
			}
			
		}
		
		f.done();
		
		setPower();
		
		return active;
		
	}
	
	private void setPower() {
		
//		//LIST<Target> ee = enemies.get();
//		if (active.size() == 0)
//			return;
//		
//		double EnemyPower = 0;
//		for (Target e : active) {
//			EnemyPower += e.power;
//		}
//		
//		double power = 0;
//		for (GDiv d : c.divs) {
//			if (d.active)
//				power += d.div().settings.power;
//		}
//		
//		double dPower = power/EnemyPower;
//		
//		dPower = CLAMP.d(dPower, 0, 2);
//		
//		for (Target e : active) {
//			double p = e.power;
//			e.minPowerAllocated = p*dPower;
//			e.maxPowerAllocated = p*3;
//			e.power = 0;
//		}
//		
		
	}
	
	private Target findTarget(TargetDiv e) {
		
		Flooder f = c.flooder.getFlooder();
		tmpEnemy2.clearSloppy();
		tmpEnemy2.add(e);
		check.unset(e.div.indexArmy());
		f.pushSloppy(e.tx>> AbsMap.scroll, e.ty>> AbsMap.scroll, 0);
		AbsMap m = c.absMap;
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			double v = t.getValue();
			
			if (m.is(t, 1)) {
				for (COORDINATE coo : AbsMap.tiles(t.x(), t.y())) {
					c.tmpList.clearSloppy();
					ArmyAIUtil.map().getAlly(c.tmpList, coo.x(), coo.y(), a);
					for (Div dd : c.tmpList) {
						if (check.isSet(dd.indexArmy())) {
							tmpEnemy2.add(allEnemy.get(dd.indexArmy()));
							check.unset(dd.indexArmy());
							v = 0;
						}
							
						
						
					}
				}
			}

			if (v >= MAX_DIST)
				continue;
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (AbsMap.bounds.holdsPoint(dx, dy)) {
					
					
					f.pushSmaller(dx,  dy, v+c.pmap.CostNoPath.abs().get(dx, dy));
				}
			}
			
		}
		
		if (tmpEnemy2.size() > 0) {
//			LOG.ln(e.tx + " " + e.ty);
//			for (Enemy ee : tmpEnemy2)
//				LOG.ln(" -> " + ee.tx + " " + ee.ty);
			return makeTarget(tmpEnemy2);
		}
		return null;
	}
	
	private Target makeTarget(ArrayList<TargetDiv> enemies) {
		
		Target t = all.get(enemies.get(0).div.indexArmy());
		double power = 0;
		t.power = 0;
		t.men = 0;
		int xx = 0;
		int yy = 0;
		
		t.coos.set(0);
		
		for (TargetDiv e : enemies) {
			xx += e.tx;
			yy += e.ty;
			power += e.div.menNrOf();
			t.power += e.div.settings.power;
			t.coos.get().set(e.tx, e.ty);
			t.coos.inc();
		}
		
		xx /= enemies.size();
		yy /= enemies.size();
		
		t.centre.set(xx, yy);
		
		TargetDiv b = enemies.get(0);
		double best = Double.MAX_VALUE;
		
		for (TargetDiv e : enemies) {
			
			double dx = xx-e.tx;
			double dy = yy-e.ty;
			double dist = dx*dx+dy*dy;
			if (dist < best) {
				best = dist;
				b = e;
			}
		}
		
		
		t.coo.set(b.tx, b.ty);
		t.men = (int) power;
		
		return t;
		
		
	}
	
}
