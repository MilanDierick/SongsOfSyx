package init.race;

import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.colls.StatsService.StatService;
import settlement.stats.colls.StatsTraits;
import settlement.stats.colls.StatsTraits.StatTrait;
import settlement.stats.equip.Equip;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.standing.StatStanding;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.stats.stat.STAT;
import settlement.stats.util.StatsJson;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sets.Tuple.TupleImp;

public class RaceStats {

	private final double[] traitOccurence = new double[64];
	private final StandingDef[] reps;
	private final double[][] repNormalized;
	private final ArrayList<LIST<STAT>> standings = new ArrayList<>(HCLASS.ALL().size());
	private final LinkedList<Tuple<STAT, Double>> arrival = new LinkedList<>();
	
	public RaceStats(Race race, Json json) {
		{
			StatsTraits traits = STATS.TRAITS();
			for (StatTrait t : traits.all()) {
				traitOccurence[t.index()] = t.defaultOccurence;
			}
			traits.fill(traitOccurence, json, 0, 1);
		}
		
		reps = new StandingDef[STATS.all().size()];
		repNormalized = new double[HCLASS.ALL().size()][reps.length];
		
		for (int i = 0; i < reps.length; i++) {
			reps[i] = STATS.all().get(i).standing().base();
			for (HCLASS c : HCLASS.ALL())
				repNormalized[c.index()][i] = 0;
		}
		
		{
			

			new StatsJson(json) {
				
				@Override
				public void doWithTheJson(STAT s, Json j, String key) {
					j = j.json(key);
					boolean prio = j.has("PRIO");
					StandingDef def = new StandingDef(j);
					reps[s.index()] = def;
					if (!prio)
						reps[s.index()].prio = s.standing().base().prio;
				}

				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					// TODO Auto-generated method stub
					
				}

				
			};
			
		}
		
		
		
		
		for (HCLASS c : HCLASS.ALL()) {
			double rmax = 0;
			
			ArrayList<STAT> stats = new ArrayList<>(STATS.all().size());
			
			for(STAT s : STATS.all()) {
				StandingDef d = reps[s.index()];
				if (d.get(c).max > 0) {
					stats.add(s);
					if (d.get(c).max > rmax)
						rmax = d.get(c).max;
				}
				
			}
			
			if (rmax == 0 && (c == HCLASS.CITIZEN || c == HCLASS.SLAVE)) {
				throw new Errors.GameError(c.name + ", race: " + race.info.name + " has no standing boosts!");
			}
			if (rmax <= 0)
				rmax = 1;
				
			else
				for(STAT s : STATS.all()) {
					if (reps[s.index()].get(c).max > 0) {
						repNormalized[c.index()][s.index()] = reps[s.index()].get(c).max/rmax;
					}
				}
			
			standings.add(new ArrayList<STAT>(stats));
			
			for (StatService s : STATS.SERVICE().allE()) {
				StandingDef d = reps[s.total().index()];
				
				s.permission().setD(c.get(race), d.get(c).max > 0 ? 1 : 0);
			}
			
			for (StatGrave s : STATS.BURIAL().graves()) {
				StandingDef d = reps[s.index()];
				s.grave().permission().set(c, race, d.get(c).max > 0 || c == HCLASS.CHILD);
			}
			
			//reps[STATS.ENV().CLIMATE.index()].get(c).dismiss = true;
		}
		

		
		if (json.has("STATS_ON_SPAWN")) {
			
			new StatsJson("STATS_ON_SPAWN", json) {
				
				@Override
				public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void doWithTheJson(STAT s, Json j, String key) {
					arrival.add(new TupleImp<STAT, Double>(s, j.d(key)));
				}
			};
		}
		
	}
	
	public LIST<Tuple<STAT, Double>> arrivalStats(){
		return arrival;
	}
	

	
	public double traitOccurence(StatTrait t) {
		return traitOccurence[t.index()];
	}
	
	public int equipArrivalLevel(Equip e) {
		return 0;
	}
	
	public StandingDef def(StatStanding s) {
		return reps[s.stat().index()];
	}
	
	public double defNormalized(HCLASS c, StatStanding s) {
		return repNormalized[c.index()][s.stat().index()];
	}
	
	public LIST<STAT> standings(HCLASS c){
		return standings.get(c.index());
	}
	
}
