package world.regions.data.updating;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import game.time.TIME;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RDOutput.RDResource;

final class Shipper {


	public Shipper() {
		
	}


	
	public void ship(Region r, double seconds) {
		
		
		
		Faction f = r.faction();
		
		if (f == null)
			return;
		
		if (r.besieged())
			return;
		
		
		
		if (f.capitolRegion() == null)
			return;
		
		double days = seconds*TIME.secondsPerDayI;
		int am = 0;
		
		if (f == FACTIONS.player()) {
			if (r.capitol())
				return;
		}
		
		f.credits().inc(RD.TAX().boost.get(r)*days, CTYPE.TAX);
		
		for (RDResource res : RD.OUTPUT().all) {
			int a = (int) Math.ceil(res.boost.get(r)*days);
			am += a;
		}
		
		if (am <= 0)
			return;
		
		Shipment c = WORLD.ENTITIES().caravans.create(r, f.capitolRegion(), ITYPE.tax);
		if (c != null) {
			for (RDResource res : RD.OUTPUT().all) {
				int a = (int) Math.ceil(res.boost.get(r)*days);
				if (a > 0) {
					c.loadAndReserve(res.res, a);
				}
			}
		}

	}
	
	public void shipAll(Faction f, double days) {
		
		for (int ri = 0; ri < f.realm().regions(); ri++) {
			Region reg = f.realm().region(ri);
			for (RDResource res : RD.OUTPUT().all) {
				int a = (int) Math.ceil(res.boost.get(reg)*days);
				if (a > 0) {
					f.buyer().reserve(res.res, a, ITYPE.tax);
					f.buyer().deliverAndUnreserve(res.res, a, ITYPE.tax);
				}
			}
			
		}
		
	}


}
