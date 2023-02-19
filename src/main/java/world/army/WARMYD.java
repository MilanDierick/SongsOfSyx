package world.army;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import util.data.DataO;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.*;
import util.info.INFO;
import world.World;
import world.entity.army.WArmy;

public final class WARMYD {

	static DataO<WArmy> dataA;
	static DataO<Faction> dataT;
	
	static ArrayListResize<Imp> imps;
	static WARMYD self;
	
	{
		self = this;
		dataA = new DataO<WArmy>() {
			@Override
			protected int[] data(WArmy t) {
				return t.divs().data;
			}		
		};
		dataT = new DataO<Faction>() {
			@Override
			protected int[] data(Faction t) {
				return t.kingdom().armies().data;
			}
		};
		
		imps = new ArrayListResize<>(200, 10000);
		new WINDU();
	}
	
	public static GETTER_TRANSE<WArmy, Faction> faction(){
		return self.faction;
	}
	final GETTER_TRANSE<WArmy, Faction> faction = new GETTER_TRANSE<WArmy, Faction>() {
		
		private final INT_OE<WArmy> data = dataA.new DataShort();
		
		@Override
		public Faction get(WArmy f) {
			if (data.get(f) == 0)
				return null;
			return FACTIONS.all().get(data.get(f)-1);
		}
		
		@Override
		public void set(WArmy f, Faction t) {
			if (get(f) != null) {
				for (Imp imp : imps) {
					imp.t.inc(get(f), -imp.get(f));
				}
				get(f).kingdom().armies().armies.removeShort(f.armyIndex());
			}else {
				if (World.ARMIES().rebels().armies.contains(f.armyIndex()))
					World.ARMIES().rebels().armies.removeShort(f.armyIndex());
			}
			
			int fi = t == null ? 0 : t.index() + 1;
			
			data.set(f, fi);
			
			if (get(f) != null) {
				get(f).kingdom().armies().armies.add(f.armyIndex());
				for (Imp imp : imps) {
					imp.t.inc(get(f), imp.get(f));
				}
			}else{
				World.ARMIES().rebels().armies.add(f.armyIndex());
			}
			
		}
	};
	
	public static void addOnlyToBeCalledFromAnArmy(WArmy a) {
		World.ARMIES().rebels().armies.add(a.armyIndex());
	}
	
	public static void removeOnlyTobeCalledFromAnArmy(WArmy a) {
		faction().set(a, null);
		if (World.ARMIES().rebels().armies.contains(a.armyIndex()))
			World.ARMIES().rebels().armies.removeShort(a.armyIndex());
		
	}
	
	public static WArmyDataTarget men(Race race){
		if (race == null)
			return self.menTotal;
		return self.men[race.index];
	}
	final WArmyDataTargetImp[] men = new WArmyDataTargetImp[RACES.all().size()];
	final WArmyDataTarget menTotal;
	{
		for (Race r : RACES.all()) {
			men[r.index] = new WArmyDataTargetImp(DicArmy.¤¤Soldiers, DicArmy.¤¤Soldiers);
		}
		menTotal = new WArmyDataTargetAll(men);
		
	}
	
	static void register(WDIV div, boolean supplies, int conscripts, int credits, int d) {
		if (div.army() == null)
			return;
		self.men[div.race().index].inc(div.army(), d*div.men(), d*div.menTarget());
		if (supplies) {
			for(ArmySupply s : RESOURCES.SUP().ALL()) {
				supplies().get(s).needed.inc(div.army(), d*s.minimum*div.men());
				supplies().get(s).target.inc(div.army(), d*s.minimum*div.menTarget());
			}
			for (EQUIPPABLE_MILITARY s : STATS.EQUIP().military_all()) {
				supplies().get(s).needed.inc(div.army(), d*div.men()*div.equipTarget(s));
				supplies().get(s).target.inc(div.army(), d*div.menTarget()*div.equipTarget(s));
			}
		}
		
		WARMYD.supplies().creditsNeeded.inc(div.army(), d*credits*div.men(), d*credits*div.menTarget());
		
		if (div.faction() != null)
			WARMYD.self.conscriptsInService.get(div.race().index).inc(div.faction(), d*conscripts);
		
		self.powerChanges.set(div.army(), 1);
		if (div.army().faction() != null)
			self.powerChangesFaction.set(div.army().faction(), 1);
	}
	
	
	public static INT_OE<Faction> conscriptable(Race race) {
		if (race == null)
			return self.conscriptsTotal;
		return self.conscripts.get(race.index);
	}
	final ArrayList<INT_OE<Faction>> conscripts = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	final INT_OE<Faction> conscriptsTotal = new INT_OE<Faction>() {

		@Override
		public int get(Faction t) {
			int am = 0;
			for (INT_OE<Faction> f : conscripts)
				am += f.get(t);
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

		@Override
		public void set(Faction t, int i) {
			
		}
		
	};
	
	{
		for (Race r : RACES.all()) {
			conscripts.add(dataT. new DataInt(DicArmy.¤¤Conscriptable, DicArmy.¤¤ConscriptsD) {
				
				@Override
				public int get(Faction t) {
					if (r.population().rarity <= 0) {
						if (t == FACTIONS.player()) {
							return 0;
						}else {
							return World.camps().factions.max(t, r);
						}
					}
					return super.get(t);
				}
				
			});
		}
	}
	
	public static INT_O<Faction> conscriptableInService(Race race) {
		if (race == null)
			return self.conscriptsInServiceTotal;
		return self.conscriptsInService.get(race.index);
	}
	
	
	final ArrayList<INT_OE<Faction>> conscriptsInService = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	final INT_O<Faction> conscriptsInServiceTotal = new INT_O<Faction>() {

		@Override
		public int get(Faction t) {
			int am = 0;
			for (INT_OE<Faction> f : conscriptsInService)
				am += f.get(t);
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
	{
		for (@SuppressWarnings("unused") Race r : RACES.all()) {
			conscriptsInService.add(dataT. new DataInt(DicArmy.¤¤Conscriptable, DicArmy.¤¤ConscriptsD));
		}
	}
	
	private final Imp powerChanges = new Imp(dataA.new DataBit(), DicMisc.¤¤Power, "");
	private final INT_OE<Faction> powerChangesFaction = dataT.new DataBit();
	private final INT_O<WArmy> powerArmy = new Imp(dataA.new DataInt(), DicMisc.¤¤Power, "") {
		@Override
		public int get(WArmy t) {
			if (powerChanges.get(t) == 1) {
				int p = 0;
				for (int di = 0; di < t.divs().size(); di++)
					p += t.divs().get(di).provess();
				powerChanges.set(t, 0);
				super.set(t, (int) (p*self.supplies.morale(t)));
			}
			
			return super.get(t);
		};
	};
	
	private final INT_O<Faction> powerFactionArmy = dataT.new DataInt() {
		
		@Override
		public int get(Faction t) {
			if (powerChangesFaction.get(t) == 1) {
				int p = 0;
				for (WArmy a : t.kingdom().armies().all())
					p+= self.powerArmy.get(a);
				powerChangesFaction.set(t, 0);
				super.set(t, p);
			}
			return super.get(t);
		};
		
	};
			
	public static INT_O<WArmy> quality() {
		return self.powerArmy;
	}
	public static INT_O<Faction> qualityF() {
		return self.powerFactionArmy;
	}
	
	
	public static WArmySupplies supplies() {
		return self.supplies;
	}
	final WArmySupplies supplies = new WArmySupplies(); 

	private final WDivBoosts boosts = new WDivBoosts();
	public static WDivBoosts boosts() {
		return self.boosts;
	}

	
	
	public static double penalty(Faction f, Race race) {
		if (f != GAME.player())
			return 1;
		double t = conscriptableInService(race).get(f);
		if (t == 0)
			return 1;
		double p = WARMYD.conscriptable(race).get(f)/t;
		p = CLAMP.d(p, 0, 1);
		
		return p;
	}
	
	public WARMYD(){
		
		imps.trim();
		
	}
	
	public interface WArmyData extends INT_O<WArmy> {
		
		public INT_O<Faction> total();
		
	}
	
	public interface WArmyDataE extends WArmyData, INT_OE<WArmy> {
		
	}
	
	public interface WArmyDataTarget extends WArmyData{
		
		public WArmyData target();
		
	}
	
	public final class WArmySupplies {
		
		final WArmyDataTargetImp creditsNeeded = new WArmyDataTargetImp(DicRes.¤¤Currs, "");
		public final LIST<WArmySupply> all;
		final INT_OE<WArmy> morale;
		final INT_OE<WArmy> health;
		
		private WArmySupplies(){

			ArrayList<WArmySupply> all = new ArrayList<>(RESOURCES.SUP().ALL().size() + STATS.EQUIP().military().size() + STATS.EQUIP().ammo().size());
			
			for (ArmySupply a : RESOURCES.SUP().ALL()) {
				all.add(new WArmySupply(all.size(), a.resource, a.consumption_day, a.minimum, a.morale, a.health));
			}
			
			for (EQUIPPABLE_MILITARY a : STATS.EQUIP().military_all()) {
				all.add(new WArmySupply(all.size(), a.resource(), a.resource().degradeSpeed()*TIME.days().bitConversion(TIME.years()), 0, 0, 0));
			}
			
			morale = dataA.new DataBit();
			health = dataA.new DataBit();
			
			this.all = all;
			
		}
		
		public WArmySupply get(ArmySupply a) {
			return all.get(a.index());
		}
		
		public WArmySupply get(EQUIPPABLE_MILITARY a) {
			return all.get(RESOURCES.SUP().ALL().size() + a.indexMilitary());
		}
		
		public void fillAll(WArmy a) {
			for (WArmySupply s : all) {
				s.current.set(a, s.max(a));
			}
		}
		
		public void update(WArmy a) {
			for (WArmySupply s : all) {
				double am = s.usedPerDay*s.needed.get(a);
				int tot = (int) am;
				if (am-tot > RND.rFloat())
					tot++;
				s.current.inc(a, -tot);
			}
			
		}
		
		public WArmyDataTarget credits() {
			return creditsNeeded;
		}

		public double morale(WArmy a) { 
			double m = 1;
			for (WArmySupply s : all) {
				if (s.current.get(a) < s.used().get(a)) {
					m *=  1 - s.morale * (s.used().get(a) - s.current().get(a))/s.used().get(a);
				}
			}
			return m;
		}
		
		public double health(WArmy a) {
			double m = 1;
			for (WArmySupply s : all) {
				if (s.current.get(a) < s.used().get(a)) {
					m *= 1 - s.health * (s.used().get(a) - s.current().get(a))/s.used().get(a);
				}
			}
			return m;
		}
		
		public void transfer(WDIV div, WArmy old, WArmy current) {
			if (old == null || current == null)
				return;
			for(ArmySupply s : RESOURCES.SUP().ALL()) {
				double tot = get(s).max(old) + get(s).max(div.menTarget()*s.minimum);
				if (tot > 0) {
					double d = CLAMP.d(get(s).current().get(old)/tot, 0, 1);
					int am = get(s).max((int)(div.menTarget()*d*s.minimum));
					get(s).current().inc(old, -am);
					get(s).current().inc(current, am);
				}
			}
			for (EQUIPPABLE_MILITARY s : STATS.EQUIP().military_all()) {
				double tot = get(s).max(old) +  get(s).max(div.menTarget()*div.equipTarget(s)) ;
				if (tot > 0) {
					double d = CLAMP.d(get(s).current().get(old)/tot, 0, 1);
					int am = get(s).max((int)(div.menTarget()*d*div.equipTarget(s)));
					get(s).current().inc(old, -am);
					get(s).current().inc(current, am);
				}
			}
		}
		
	}
	
	public static class WArmySupply implements INDEXED {

		public final RESOURCE res;
		private final Imp current;
		private final Imp needed;
		private final Imp target;
		
		public final int minimumPerMan;
		public final double morale;
		public final double health;
		public final double usedPerDay;
		private final int index;

		WArmySupply(int index, RESOURCE res, double consumption, int equipPerman, double morale, double health) {
			this.usedPerDay = consumption;
			this.index = index;
			current = new Imp(dataA.new DataInt(), res.names, res.names) {
				@Override
				public void set(WArmy t, int i) {
					self.powerChanges.set(t, 1);
					super.set(t, i);
				}
			};
			needed = new Imp(dataA.new DataInt(), res.names, res.names);
			target = new Imp(dataA.new DataInt(), res.names, res.names);
			
			this.res = res;
			this.minimumPerMan = equipPerman;
			this.morale = morale;
			this.health = health;
		}
		
		public int max(WArmy a) {
			return max(target.get(a));
		}
		
		int max(int amount) {
			return (int) (amount + 24.0*amount*usedPerDay);
		}
		
		public int needed(WArmy a) {
			return CLAMP.i(max(a) - current.get(a), 0, Integer.MAX_VALUE);
		}
		
		public int needed(Faction faction) {
			int am = 0;
			for (WArmy a : faction.kingdom().armies().all())
				am += needed(a);
			return am;
		}

		@Override
		public int index() {
			return index;
		}
		
		public WArmyDataE current() {
			return current;
		}
		
		public WArmyData used() {
			return needed;
		}
		
		public WArmyData target() {
			return target;
		}
		
		public double morale(WArmy a) {
			return CLAMP.d(1 - morale * (used().get(a) - current().get(a))/used().get(a), 0, 1);
		}
		
		public double health(WArmy a) {
			return CLAMP.d(1 - health * (used().get(a) - current().get(a))/used().get(a), 0, 1);
		}

		public double getD(WArmy a) {
			double t = target.get(a);
			if (t <= 0)
				return 0;
			return CLAMP.d(current.get(a)/t, 0, 1);
		}

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
		
		Imp(INT_OE<WArmy> d, CharSequence name, CharSequence desc){
			this.d = d;
			info = new INFO(name, desc);
			t = dataT.new DataInt(info);
			imps.add(this);
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
		
		WArmyDataTargetImp(CharSequence name, CharSequence desc){
			super(dataA.new DataInt(), name, desc);
			ptarget = new Imp(dataA.new DataInt(name, desc), name, desc);
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
