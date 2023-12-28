package world.battle;

import java.util.Arrays;

import game.faction.trade.ITYPE;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;

final class AD_Shipper {

	private final double[] needs = new double[AD.supplies().all.size()];
	
	public void ship(Side toSide, Side fromSide, int[] slaves, int[] loot) {
		
		if (toSide.isPlayer && slaves != null) {
			for (Race r : RACES.all()) {
				if (slaves[r.index] > 0) {
					SETT.ENTRY().add(r,  HTYPE.PRISONER, slaves[r.index]);
				}
			}
		}
		
		if (loot == null) {
			stop(toSide, fromSide);
			return;
		}
		
		Arrays.fill(needs, 0);
		
		for (SideUnit u : toSide.us) {
			if (u.a() != null && AD.men(null).get(u.a()) > 0) {
				WArmy a = u.a();
				for (ADSupply s : AD.supplies().all) {
					needs[s.index()] += s.needed(a);
				}
				
			}
		}

		for (SideUnit u : toSide.us) {
			if (u.a() != null && AD.men(null).get(u.a()) > 0) {
				WArmy a = u.a();
				for (ADSupply s : AD.supplies().all) {
					double n = s.needed(a);
					if (n == 0)
						continue;
					int am = (int) Math.ceil((n*loot[s.res.index()]/needs[s.index()]));
					am = CLAMP.i(am, 0, loot[s.res.index()]);
					am = CLAMP.i(am, 0, (int)n);
					s.current().inc(a, am);
					loot[s.res.index()] -= am;
				}
				
			}
		}
		
		stop(toSide, fromSide);

		if (toSide.faction == null)
			return;
		
		Shipment s = null;
		
		
		for (RESOURCE res : RESOURCES.ALL()) {

			if (loot[res.index()] > 0) {
				if (s == null) {
					
					s = WORLD.ENTITIES().caravans.create(fromSide.coo.x(), fromSide.coo.y(), toSide.faction.capitolRegion(), ITYPE.spoils);
				}
				if (s != null) {
					s.loadAndReserve(res, loot[res.index()]);
				}
				
			}
		}
		
		
	}
	
	private void stop(Side toSide, Side fromSide) {
		for (SideUnit u : toSide.us) {
			if (u.a() != null && u.a().besieging() == null) {
				u.a().stop();
			}
		}
		
		for (SideUnit u : fromSide.us) {
			if (u.a() != null) {
				u.a().stop();
			}
		}
	}

}
