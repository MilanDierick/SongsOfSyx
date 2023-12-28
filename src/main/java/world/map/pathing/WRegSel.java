package world.map.pathing;

import game.faction.FACTIONS;
import game.faction.Faction;
import util.data.BOOLEANO;
import world.regions.Region;

public abstract class WRegSel implements BOOLEANO<Region>{
	
	public WRegSel() {
		
		
	}
	
	private static Region home;
	private static Faction faction;
	
	private final static WRegSel DUMDUM = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return true;
		}
	};
	
	private final static WRegSel DUMMY = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t != home;
		}
	};
	
	private final static WRegSel CAPITOLS = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t.capitol();
		}
	};
	
	private final static WRegSel SINGLE = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t == home;
		}
	};
	
	private final static WRegSel FACTION = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == faction;
		}
	};
	
	private final static WRegSel ENEMYFACTION = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() != null && FACTIONS.DIP().war.is(t.faction(), faction);
		}
	};
	
	private final static WRegSel ENEMY = new WRegSel() {
		
		@Override
		public boolean is(Region t) {
			return FACTIONS.DIP().war.is(t.faction(), faction);
		}
	};
	
	public static WRegSel DUMMY() {
		return DUMDUM;
	}
	
	public static WRegSel DUMMY(Region home) {
		WRegSel.home = home;
		return DUMMY;
	}
	
	public static WRegSel CAPITOLS(Faction home) {
		WRegSel.faction = home;
		return CAPITOLS;
	}
	
	public static WRegSel SINGLE(Region home) {
		WRegSel.home = home;
		return SINGLE;
	}
	
	public static WRegSel FACTION(Faction home) {
		WRegSel.faction = home;
		return FACTION;
	}
	
	public static WRegSel ENEMYFACTION(Faction home) {
		WRegSel.faction = home;
		return ENEMYFACTION;
	}
	
	public static WRegSel ENEMY(Faction home) {
		WRegSel.faction = home;
		return ENEMY;
	}
	
	
}