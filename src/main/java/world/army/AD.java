package world.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import util.info.INFO;
import world.WORLD;
import world.army.util.ADUtil;
import world.entity.army.WArmy;

public final class AD {	
	static AD self;
	{
		self = this;
	}
	private final ADInit init = new ADInit();
	
	private final ADConscripts conscripts = new ADConscripts(init);
	
	private final ADUtil util = new ADUtil(this);
	
	final GETTER_TRANSE<WArmy, Faction> faction = new GETTER_TRANSE<WArmy, Faction>() {
		
		private final INT_OE<WArmy> data = init.dataA.new DataShort();
		
		@Override
		public Faction get(WArmy f) {
			if (data.get(f) == 0)
				return null;
			return FACTIONS.getByIndex(data.get(f)-1);
		}
		
		@Override
		public void set(WArmy f, Faction t) {
			if (get(f) != null) {
				for (Imp imp : imps) {
					imp.t.inc(get(f), -imp.get(f));
				}
				get(f).armies().armies.removeShort(f.armyIndex());
			}else {
				if (WORLD.ARMIES().rebels().armies.contains(f.armyIndex()))
					WORLD.ARMIES().rebels().armies.removeShort(f.armyIndex());
			}
			
			int fi = t == null ? 0 : t.index() + 1;
			
			data.set(f, fi);
			
			if (get(f) != null) {
				get(f).armies().armies.add(f.armyIndex());
				for (Imp imp : imps) {
					imp.t.inc(get(f), imp.get(f));
				}
			}else{
				WORLD.ARMIES().rebels().armies.add(f.armyIndex());
			}
			
		}
	};
	
	final WArmyDataTargetImp[] men = new WArmyDataTargetImp[RACES.all().size()];
	final WArmyDataTarget menTotal;
	{
		for (Race r : RACES.all()) {
			men[r.index] = new WArmyDataTargetImp(init, DicArmy.¤¤Soldiers, DicArmy.¤¤Soldiers);
		}
		menTotal = new WArmyDataTargetAll(men);
	}
	
	private final ADPower power = new ADPower(init);
	
	final ADSupplies supplies = new ADSupplies(init); 
	
	final LIST<Imp> imps;
	
	final int dataFactionCount;
	final int dataArmyCount;
	
	public AD(WARMIES ww){
		imps = new ArrayList<AD.Imp>(init.imps);
		dataFactionCount = init.dataT.longCount();
		dataArmyCount = init.dataA.longCount();
	}
	
	public void update(Faction f, double seconds) {
		conscripts.update(f, seconds);
	}
	
	public void init(Faction f) {
		conscripts.init(f);
	}
	
	
	public static ADConscripts conscripts(){
		return self.conscripts;
	}
	
	public static GETTER_TRANSE<WArmy, Faction> faction(){
		return self.faction;
	}
	
	public static void addOnlyToBeCalledFromAnArmy(WArmy a) {
		WORLD.ARMIES().rebels().armies.add(a.armyIndex());
	}
	
	public static void removeOnlyTobeCalledFromAnArmy(WArmy a) {
		faction().set(a, null);
		if (WORLD.ARMIES().rebels().armies.contains(a.armyIndex()))
			WORLD.ARMIES().rebels().armies.removeShort(a.armyIndex());
		
	}
	
	public static WArmyDataTarget men(Race race){
		if (race == null)
			return self.menTotal;
		return self.men[race.index];
	}
	
	public static ADUtil UTIL() {
		return self.util;
	}
	
	
	static void register(ADDiv div, boolean supplies, boolean conscripts, int credits, int d) {
		if (div.army() == null)
			return;
		self.men[div.race().index].inc(div.army(), d*div.men(), d*div.menTarget());
		if (supplies) {
			for(int i = 0; i < RESOURCES.SUP().ALL().size(); i++) {
				ArmySupply s = RESOURCES.SUP().ALL().get(i);
				supplies().get(s).needed.inc(div.army(), d*s.minimum*div.men());
				supplies().get(s).target.inc(div.army(), d*s.minimum*div.menTarget());
			}
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++ ) {
				EquipBattle s = STATS.EQUIP().BATTLE_ALL().get(i);
				supplies().get(s).needed.inc(div.army(), d*div.men()*div.equipTarget(s));
				supplies().get(s).target.inc(div.army(), d*div.menTarget()*div.equipTarget(s));
			}
		}
		
		AD.supplies().creditsNeeded.inc(div.army(), d*credits*div.men(), d*credits*div.menTarget());
		
		self.conscripts.count(div, supplies, conscripts, credits, d);

		self.power.clearCache(div.army());
	}


	

			
	public static ADPower power() {
		return self.power;
	}
	
	
	public static ADSupplies supplies() {
		return self.supplies;
	}
	
	public interface WArmyData extends INT_O<WArmy> {
		
		public INT_O<Faction> total();
		
	}
	
	public interface WArmyDataE extends WArmyData, INT_OE<WArmy> {
		
	}
	
	public interface WArmyDataTarget extends WArmyData{
		
		public WArmyData target();
		
	}
	
	final class WArmyDataTargetAll implements WArmyDataTarget{

		private final WArmyData pcurrent;
		private final WArmyData ptarget;
		
		WArmyDataTargetAll(WArmyDataTargetImp[] all){
			WArmyData[] curr = new WArmyData[all.length];
			WArmyData[] tar = new WArmyData[all.length];
			for (int i = 0; i < all.length; i++) {
				curr[i] = all[i];
				tar[i] = all[i].target();
			}
			pcurrent = new WarmyDataAll(curr);
			ptarget = new WarmyDataAll(tar);
		}


		@Override
		public WArmyData target() {
			return ptarget;
		}

		@Override
		public INT_O<Faction> total() {
			return pcurrent.total();
		}

		@Override
		public int get(WArmy t) {
			return pcurrent.get(t);
		}

		@Override
		public int min(WArmy t) {
			return pcurrent.min(t);
		}

		@Override
		public int max(WArmy t) {
			return pcurrent.max(t);
		}
		
	}
	

	private class WarmyDataAll implements WArmyData {

		private final WArmyData[] all;
		private final INT_O<Faction> f = new INT_O<Faction>(){

			@Override
			public int get(Faction t) {
				int am = 0;
				for (WArmyData tt : all) {
					am += tt.total().get(t);
				}
				return am;
			}

			@Override
			public int min(Faction t) {
				return 0;
			}

			@Override
			public int max(Faction t) {
				return Integer.MAX_VALUE;
			}
			
		};
		
		WarmyDataAll(WArmyData[] all){
			this.all = all;
		}
		
		@Override
		public int min(WArmy t) {
			return 0;
		}
		
		@Override
		public int max(WArmy t) {
			return Integer.MAX_VALUE;
		}
		
		@Override
		public int get(WArmy t) {
			int am = 0;
			for (WArmyData tt : all)
				am += tt.get(t);
			return am;
		}

		@Override
		public INT_O<Faction> total() {
			return f;
		}
		
		
	}
	

	
	static class Imp implements WArmyDataE{

		private final INT_OE<WArmy> d;
		private final INT_OE<Faction> t;
		private final INFO info;
		
		Imp(ADInit init, INT_OE<WArmy> d, CharSequence name, CharSequence desc){
			this.d = d;
			info = new INFO(name, desc);
			t = init.dataT.new DataInt(info);
			init.imps.add(this);
		}
		
		@Override
		public INT_O<Faction> total() {
			return t;
		}

		@Override
		public int get(WArmy t) {
			return d.get(t);
		}

		@Override
		public int min(WArmy t) {
			return d.min(t);
		}

		@Override
		public int max(WArmy t) {
			return d.max(t);
		}

		@Override
		public void set(WArmy t, int i) {
			if (t.faction() != null)
				this.t.inc(t.faction(), -d.get(t));
			d.set(t, i);
			if (t.faction() != null)
				this.t.inc(t.faction(), d.get(t));
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	static class WArmyDataTargetImp extends Imp implements WArmyDataTarget{

		private final Imp ptarget;
		
		WArmyDataTargetImp(ADInit init, CharSequence name, CharSequence desc){
			super(init, init.dataA.new DataInt(), name, desc);
			ptarget = new Imp(init, init.dataA.new DataInt(name, desc), name, desc);
		}

		void inc(WArmy t, int current, int target) {
			inc(t, current);
			this.ptarget.inc(t, target);
		}
		
		@Override
		public WArmyData target() {
			return ptarget;
		}
		
	}
	
}
