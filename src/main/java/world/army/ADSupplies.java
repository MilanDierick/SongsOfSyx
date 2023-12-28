package world.army;

import game.time.TIME;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.dic.DicRes;
import world.army.AD.WArmyDataTarget;
import world.army.AD.WArmyDataTargetImp;
import world.entity.army.WArmy;

public final class ADSupplies {
	
	final WArmyDataTargetImp creditsNeeded;
	public final LIST<ADSupply> all;
	final INT_OE<WArmy> morale;
	final INT_OE<WArmy> health;
	
	ADSupplies(ADInit init){
		creditsNeeded = new WArmyDataTargetImp(init, DicRes.¤¤Currs, "");
		ArrayList<ADSupply> all = new ArrayList<>(RESOURCES.SUP().ALL().size() + STATS.EQUIP().BATTLE().size() + STATS.EQUIP().RANGED().size());
		
		for (ArmySupply a : RESOURCES.SUP().ALL()) {
			all.add(new ADSupply(all.size(), init, a.resource, a.consumption_day, a.minimum, a.morale, a.health));
		}
		
		for (EquipBattle a : STATS.EQUIP().BATTLE_ALL()) {
			all.add(new ADSupply(all.size(), init, a.resource(), a.resource().degradeSpeed()*TIME.days().bitConversion(TIME.years()), 0, 0, 0));
		}
		
		morale = init.dataA.new DataBit();
		health = init.dataA.new DataBit();
		
		this.all = all;
		
	}
	
	public ADSupply get(ArmySupply a) {
		return all.get(a.index());
	}
	
	public ADSupply get(EquipBattle a) {
		return all.get(RESOURCES.SUP().ALL().size() + a.indexMilitary());
	}
	
	public void fillAll(WArmy a) {
		for (ADSupply s : all) {
			s.current.set(a, s.max(a));
		}
	}
	
	public void update(WArmy a) {
		for (ADSupply s : all) {
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
		for (ADSupply s : all) {
			if (s.current.get(a) < s.used().get(a)) {
				m *=  1 - s.morale * (s.used().get(a) - s.current().get(a))/s.used().get(a);
			}
		}
		return m;
	}
	
	public double health(WArmy a) {
		double m = 1;
		for (ADSupply s : all) {
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
		for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
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