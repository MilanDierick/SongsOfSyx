package game.faction.diplomacy;

import java.util.Arrays;

import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.trade.ITYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.data.INT;
import util.data.INT_O.INT_OE;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.log.WLogger;
import world.regions.Region;
import world.regions.data.RD;

public final class DealParty {
	
	public final INT.IntImp credits = new INT.IntImp() {
		
		@Override
		public int min() {
			return 0;
		};
		@Override
		public int max() {
			Faction fa = f.get();
			int cr = 0;
			if (fa instanceof FactionNPC) {
				cr = (int) ((FactionNPC) fa).stockpile.credit();
			}else
				cr = (int) f.get().credits().credits();
			if (cr < 0)
				return 0;
			return cr;
		};
		
	};
	public final ArrayList<Region> regs = new ArrayList<Region>(128);
	private final int[] res = new int[RESOURCES.ALL().size()];
	
	public final INT_OE<RESOURCE> resources = new INT_OE<RESOURCE>() {

		@Override
		public int get(RESOURCE t) {
			return res[t.index()];
		}

		@Override
		public int min(RESOURCE t) {
			return 0;
		}

		@Override
		public int max(RESOURCE t) {
			return Math.max(f.get().res().get(t)-1, 0);
		}

		@Override
		public void set(RESOURCE t, int i) {
			res[t.index()] = i;
		}
		
	};
	public final GETTER<? extends Faction> f;
	private final Deal deal;
	
	public DealParty(Deal deal, GETTER<? extends Faction> f){
		this.f = f;
		this.deal = deal;
	}
	
	void clear() {
		credits.set(0);
		Arrays.fill(res, 0);
		regs.clearSloppy();
	}

	void execute(Faction target) {
		
		target.credits().inc(credits.get(), CTYPE.DIPLOMACY);
		f.get().credits().inc(-credits.get(), CTYPE.DIPLOMACY);
		
		
		for (Region reg : regs) {
			WLogger.newOwner(f.get(), target, reg);
			RD.setFaction(reg, target);
		}
		
		boolean rr = false;
		for (RESOURCE r : RESOURCES.ALL()) {
			if (res[r.index()] > 0) {
				rr = true;
				break;
			}
		}
		
		if (!rr)
			return;
		
		
		
		Shipment s = WORLD.ENTITIES().caravans.create(f.get().capitolRegion().cx(), f.get().capitolRegion().cy(), target.capitolRegion(), ITYPE.diplomacy);
		
		if (s != null) {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = res[r.index()];
				if (a > 0) {
					s.loadAndReserve(r, a);
				}
			}
		}else {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = res[r.index()];
				if (a > 0) {
					target.buyer().reserve(r, a, ITYPE.diplomacy);
					target.buyer().deliverAndUnreserve(r, a, ITYPE.diplomacy);
				}
			}
		}
		
		for (RESOURCE r : RESOURCES.ALL()) {
			int a = res[r.index()];
			if (a > 0) {
				f.get().seller().remove(r, a, ITYPE.diplomacy);
			}
		}
		
	}
	
	public double value(Faction target, FactionNPC faction) {
		
		double value = 0;
		
		value += credits.get();
		
		for (RESOURCE r : RESOURCES.ALL()) {
			
			if (res[r.index()] > 0)
				value += DealValues.valueResource(r, target, faction, res[r.index()]);
				
		}
		
		for (Region reg : regs) {
			value += DealRegions.valueRegion(reg, deal);
		}
		return value;
		
		
	}
	
	public boolean hasValue() {
		
		if (credits.get() != 0)
			return true;
		
		for (RESOURCE r : RESOURCES.ALL()) {
			
			if (res[r.index()] > 0)
				return true;
				
		}
		
		return regs.size() > 0;
		
		
	}
	

	
}
