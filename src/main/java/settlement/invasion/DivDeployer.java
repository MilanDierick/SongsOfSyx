package settlement.invasion;

import static settlement.main.SETT.*;

import init.C;
import init.config.Config;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.invasion.SpotMaker.InvasionSpot;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;

final class DivDeployer {

	public static Div deploy(ArrayList<InvadorDiv> divs, InvasionSpot spot) {
		if (divs.size() == 0)
			return null;
		
		Div d = getDiv();
		if (d == null)
			return null;
		
		
		
		int sx = spot.body.x1();
		int sy = spot.body.y1();
		DIR dir = spot.dir.next(-2);
		
		InvadorDiv id = divs.get(0);
		
		for (COORDINATE c : spot.body) {
			if (DIR.get(sx, sy, c.x(), c.y()) == dir) {
				sx = c.x();
				sy = c.y();
			}
		}
		
		sx += spot.dir.x()*6;
		sy += spot.dir.y()*6;
		
		dir = spot.dir.next(2);
		int w = (int) Math.ceil(id.men/5.0);
		if (w >= spot.size)
			w = spot.size-1;
		for (int i = 0; i < spot.size-w; i++) {
			
			if (can(sx, sy)) {
				boolean can= true;
				for (int k = 1; k <= w; k++) {
					int dx = sx + k*dir.x();
					int dy = sy + k*dir.y();
					if (!can(dx, dy)) {
						can = false;
						break;
					}
				}
				if (can) {
					place(sx, sy, id, d, spot.dir);
					divs.remove(0);
					return d;
				}
			}
			
			sx += dir.x();
			sy += dir.y();
		}
		return null;
		
		
		
	}
	
	private static ArrayList<Humanoid> tmp = new ArrayList<>(Config.BATTLE.MEN_PER_DIVISION);
	
	private static boolean place(int sx, int sy, InvadorDiv d, Div div, DIR spotdir) {
		int amount = d.men;
		int w = (int) Math.ceil(d.men/5.0);
		
		div.position().clear();
		div.settings.musteringSet(true);
		
		DIR right = spotdir.next(2);
		DIR down = right.next(2);
		tmp.clear();
		for (int y = 0; y < w; y++) {
			for (int x = 0; x < 5; x++) {
				if (amount-- > 0) {
					int cx = (sx + y*right.x() + x*down.x())*C.TILE_SIZE+C.TILE_SIZEH;
					int cy = (sy + y*right.y() + x*down.y())*C.TILE_SIZE+C.TILE_SIZEH;
					Humanoid h = new Humanoid(cx, cy, d.race, HTYPE.ENEMY, null);
					if (h != null && !h.isRemoved()) {
						h.setDivision(div);
						tmp.add(h);
						for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
							m.set(h.indu(), d.equipment[m.indexMilitary()]);
						}
						STATS.BATTLE().TRAINING_MELEE.indu().setD(h.indu(), d.trainingM);
						STATS.BATTLE().TRAINING_ARCHERY.indu().setD(h.indu(), d.trainingR);
						STATS.BATTLE().COMBAT_EXPERIENCE.indu().setD(h.indu(), d.experience);
					}
					
				}
			}
		}
		

		
		int x1 = sx*C.TILE_SIZE+C.TILE_SIZEH;
		int y1 = sy*C.TILE_SIZE+C.TILE_SIZEH;
		DIR dir = spotdir.next(2);
		int x2 = x1 + dir.x()*w*C.TILE_SIZE;
		int y2 = y1 + dir.y()*w*C.TILE_SIZE;
		
		ARMIES().placer.deploy(div, x1, x2, y1, y2);
		div.initPosition();
		for (Humanoid h : tmp)
			h.teleportAndInitInDiv();
		
		return true;
	}
	
	private static boolean can(int sx, int sy) {
		
		
		if (!IN_BOUNDS(sx, sy)) {
			return false;
		}
		
		if (SETT.PATH().availability.get(sx, sy).isSolid(SETT.ARMIES().enemy())) {
			SETT.ARMIES().map.breakIt(sx, sy);
			return false;
		}
		
		
		for (ENTITY e : SETT.ENTITIES().getAtTile(sx, sy)) {
			if (e instanceof Humanoid && ((Humanoid) e).indu().army() == SETT.ARMIES().enemy())
				return false;
		}
		return true;
	}
	
	public static Div getDiv() {
		
		for (Div d : SETT.ARMIES().enemy().divisions()) {
			if (d.menNrOf() == 0) {
				d.morale.init();
				return d;
			}
		}
		return null;
	}
	
}
