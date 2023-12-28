package world.map.pathing;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.regions.Region;

public abstract class WTREATY {
	
	private static final ArrayListGrower<WTREATY> all = new ArrayListGrower<>();
	public static final LIST<WTREATY> ALL = all;
	
	public WTREATY() {
		
		
	}
	
	
	
	private static Region home;
	private static Faction fhome;

	
	private final static WTREATY DUMMY = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			return true;
		}
	};
	
	private final static WTREATY NEIGHBOUR = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			Region to = WORLD.REGIONS().map.get(tx, ty);
			
			if (from == null || from.faction() == fhome)
				return true;
			
			if (from != to)
				return false;
			return true;
		}
	};
	
	private final static WTREATY NEIGHBOURS = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			Region to = WORLD.REGIONS().map.get(tx, ty);
			if (from == home)
				return true;
			if (from == null)
				return true;
			if (from == to)
				return true;
			return from == to;
		}
	};
	
	private final static WTREATY SAME = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			Region to = WORLD.REGIONS().map.get(tx, ty);
			if (from == null || to == null)
				return true;
			return from.faction() == to.faction() && from.faction() == fhome;
		}
	};
	
	private final static WTREATY AJACENT_FACTION = new WTREATY() {
		
		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region from = WORLD.REGIONS().map.get(fx, fy);
			Region to = WORLD.REGIONS().map.get(tx, ty);
			
			if (from == null)
				return true;
			if (to == null)
				return from == null || from == home;
			return from.faction() == fhome || from.faction() == to.faction();
		}
	};
	
	private final static WTREATY TRADE = new WTREATY() {

		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			Region r = WORLD.REGIONS().map.get(tx, ty);
			if (r == null)
				return true;
			if (r.faction() == null)
				return true;
			if (r.faction() == fhome)
				return true;
			if (FACTIONS.DIP().trades(fhome, r.faction()))
				return true;
			return false;
		}
		
		
	};
	
	private final static WTREATY TRADEP = new WTREATY() {

		@Override
		public boolean can(int fx, int fy, int tx, int ty, double dist) {
			if (TRADE.can(fx, fy, tx, ty, dist))
				return true;
			Region r = WORLD.REGIONS().map.get(fx, fy);
			if (r == null)
				return true;
			if (FACTIONS.DIP().trades(fhome, r.faction()) || fhome == r.faction())
				return true;
			Region r2 = WORLD.REGIONS().map.get(tx, ty);
			if (r == r2 || r.faction() == r2.faction())
				return true;
			return false;
		}
		
		
	};
	
	
	public static WTREATY DUMMY() {
		return DUMMY;
	}
	
	public static WTREATY NEIGHBOURS(Region home) {
		WTREATY.home = home;
		return NEIGHBOURS;
	}
	
	public static WTREATY NEIGHBOURSF(Faction f) {
		WTREATY.fhome = f;
		return NEIGHBOUR;
	}
	
	public static WTREATY FACTIONS(Faction home) {
		WTREATY.fhome = home;
		return AJACENT_FACTION;
	}
	
	public static WTREATY TRADEPARTNERS(Faction home) {
		WTREATY.fhome = home;
		return TRADE;
	}
	
	public static WTREATY TRADEPOTENTIAL(Faction home) {
		WTREATY.fhome = home;
		return TRADEP;
	}
	
	public static WTREATY SAME(Faction home) {
		WTREATY.fhome = home;
		return SAME;
	}


	public abstract boolean can(int fx, int fy, int tx, int ty, double dist);

	
}