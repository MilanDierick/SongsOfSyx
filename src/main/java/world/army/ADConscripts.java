package world.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicArmy;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.regions.data.RD;
import world.regions.data.pop.RDRace;

public final class ADConscripts {

	final ArrayList<INT_OE<Faction>> total = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	private final INT_O<Faction> totalAll;
	final ArrayList<INT_OE<Faction>> used = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	private final INT_O<Faction> usedAll;
	final ArrayList<INT_OE<Faction>> usedTarget = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	private final INT_O<Faction> usedTargetAll;
	private final ArrayList<INT_O<Faction>> available = new ArrayList<INT_O<Faction>>(RACES.all().size());
	private final INT_O<Faction> availableAll;
	
	public INT_O<Faction> total(Race race) {
		if (race == null)
			return totalAll;
		return total.get(race.index);
	}
	
	public INT_O<Faction> used(Race race) {
		if (race == null)
			return usedAll;
		return used.get(race.index);
	}
	
	public INT_O<Faction> usedTarget(Race race) {
		if (race == null)
			return usedTargetAll;
		return usedTarget.get(race.index);
	}
	
	public INT_O<Faction> available(Race race) {
		if (race == null)
			return availableAll;
		return available.get(race.index);
	}
	
	public boolean canTrain(Race race, Faction f) {
		if (f == null)
			return true;
		return used(race).get(f) < total(race).get(f);
	}
	
	public int canTrainI(Race race, Faction f) {
		if (f == null)
			return 100000;
		return  total(race).get(f)-used(race).get(f);
	}
	
	ADConscripts(ADInit init){
		for (Race r : RACES.all()) {
			total.add(init.dataT. new DataInt(DicArmy.¤¤Conscriptable, DicArmy.¤¤ConscriptsD) {
				
				@Override
				public int get(Faction t) {
					if (r.population().max <= 0) {
						if (t == FACTIONS.player()) {
							return 0;
						}else {
							return WORLD.camps().factions.max(t, r);
						}
					}
					return super.get(t);
				}
				
			});
			
			used.add(init.dataT. new DataInt(DicArmy.¤¤Conscriptable, DicArmy.¤¤ConscriptsD));
			usedTarget.add(init.dataT. new DataInt(DicArmy.¤¤Conscriptable, DicArmy.¤¤ConscriptsD));
			available.add(new INT_O<Faction>() {

				@Override
				public int get(Faction t) {
					return total.get(r.index).get(t)-usedTarget.get(r.index()).get(t);
				}

				@Override
				public int min(Faction t) {
					return 0;
				}

				@Override
				public int max(Faction t) {
					return Integer.MAX_VALUE;
				}
				
			});
		}
		
		totalAll = tot(total);
		usedAll = tot(used);
		usedTargetAll = tot(usedTarget);
		availableAll = tot(available);
		IDebugPanelWorld.add("Conscripts 1000", new ACTION() {
			
			@Override
			public void exe() {
				for (RDRace rr : RD.RACES().all)
					total.get(rr.race.index).inc(FACTIONS.player(), 1000);
			}
		});
	}

	void init(Faction f) {
		for (Race r : RACES.all()) {
			total.get(r.index()).set(f, RD.MILITARY().conscripts(r, f));
		}
	}
	
	void update(Faction f, double timeSinceLast) {

		for (Race r : RACES.playable()) {
			int n = total(r).get(f);
			int t = RD.MILITARY().conscripts(r, f);
			
			double d = t-n;
			
			if (d > 0) {
				
				d *= TIME.secondsPerDayI*timeSinceLast/8.0;
				n += (int) d;
				if (RND.rFloat() < (d - (int) d))
					n++;
			}else if (d < 0) {
				n = t;
			}
			
			n = CLAMP.i(n, 0, t);
			
			total.get(r.index()).set(f, n);
		}
		
	}
	
	void count(WDIV div, boolean supplies, boolean conscripts, int credits, int d){
		if (div.faction() != null && conscripts) {
			used.get(div.race().index).inc(div.faction(), d*div.men());
			usedTarget.get(div.race().index).inc(div.faction(), d*div.menTarget());
		}
	}
	
	private static INT_O<Faction> tot(LIST<? extends INT_O<Faction>> li){
		return new INT_O<Faction>() {

			@Override
			public int get(Faction t) {
				int am = 0;
				for (INT_O<Faction> f : li)
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
	}
}
