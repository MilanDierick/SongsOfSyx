package game.faction.npc.stockpile;

import game.faction.npc.stockpile.Updater.SIns;
import game.faction.npc.stockpile.Updater.SOutput;
import init.biomes.BUILDING_PREFS;
import init.need.NEEDS;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.knowledge.library.ROOM_LIBRARY;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.employment.RoomEquip;
import settlement.stats.STATS;
import settlement.stats.colls.StatsService.StatService;
import settlement.stats.equip.*;
import settlement.stats.stat.STAT;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Valued.ValuedImp;

class Needer {
	
	private final R[] races = new R[RACES.all().size()];

	
	Needer(){
		
		for (Race r : RACES.all()) {
			races[r.index()] = new R(r);
		}
	}
	
	public void setNeeds(Updater os, Race race, NPCStockpile stockpile){
		
		for (SOutput o : os.allO) {
			o.needed = 0;
		}
		
		for (ValuedImp<RESOURCE> res : races[race.index].all) {
			if (res.value > 0)
				os.o(res.t).needed = 1.0;
		}
		
		double prodTot = 0;
		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			setNeeded(os, os.o(ri), os.o(ri).needed);
			prodTot += os.o(ri).prodSpeed;
		}
		double max = 0;
		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			max = Math.max(max, os.o(ri).needed);
		}
		
		if (max > 0)
			max = 1.0/max;

		for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
			double p = prodTot/os.allO[ri].prodSpeed;
			p *= 0.1 + os.o(ri).needed*max*0.9;
			stockpile.setBuyValue(RESOURCES.ALL().get(ri), p);
		}
		
	}
	
	private void setNeeded(Updater os, SOutput o, double value) {
		
		o.needed = Math.max(value, o.needed);
		o.needed = CLAMP.d(o.needed, 0, 1);
		double pm = 0;
		for (int i = 0; i < o.producers.size(); i++) {
			pm = Math.max(o.producers.get(i).prodSpeed, pm);
		}
		for (int i = 0; i < o.producers.size(); i++) {
			setNeeded(os, o.producers.get(i), o.producers.get(i).prodSpeed*value/pm);
		}
	}

	private void setNeeded(Updater os, SIns o, double value) {
		
		//value /= o.out.rate*policy.getD(o.ins);
		
		for (int i = 0; i < o.inputs.size(); i++) {
			setNeeded(os, o.inputs.get(i), value);
		}
	}

	
	private static class R {
		
		public final LinkedList<ValuedImp<RESOURCE>> all = new LinkedList<>();
		public final double[] values = new double[RESOURCES.ALL().size()];
		
		public R(Race r) {
			
			final double di = 1.0/16;
			
			for (ResG e : r.pref().food) {
				values[e.resource.index()] += NEEDS.TYPES().HUNGER.rate.get(r)/r.pref().food.size();
			}
			double d = 1.0/RESOURCES.DRINKS().all().size();
			for (RESOURCE res : RESOURCES.DRINKS().res()) {
				values[res.index()] += NEEDS.TYPES().THIRST.rate.get(r)*d;
			}
			
			for (TBuilding b : SETT.TERRAIN().BUILDINGS) {
				if (b.resource != null)
					values[b.resource.index()] += di*r.pref().structure(BUILDING_PREFS.get(b));
			}
			
			for (EquipCivic e : STATS.EQUIP().civics()) {
				
				
				values[e.resource().index()] += di*e.stat().standing().normalized(HCLASS.CITIZEN, r)*e.max()*(1.0/e.wearRate());
			}
			
			for (RESOURCE res : RESOURCES.ALL()) {
				STAT e = STATS.STORED().all().get(res.index());
				if (e.standing().get(HCLASS.CITIZEN, r, 1.0) > 0)
					values[res.index()] += di*e.standing().normalized(HCLASS.CITIZEN, r)/e.standing().definition(r).mul;
				
				values[res.index()] += di*STATS.HOME().max(HCLASS.CITIZEN, r, res);
			}
			
			for (StatService s : STATS.SERVICE().allE()) {
				RoomBlueprintImp blue = s.service().room();
				if (blue instanceof INDUSTRY_HASER) {
					
					INDUSTRY_HASER ins = (INDUSTRY_HASER) blue;
					double v = s.total().standing().normalized(HCLASS.CITIZEN, r);
					if (v > 0) {
						for (IndustryResource rr : ins.industries().get(0).ins()) {
							if (RESOURCES.EDI().is(rr.resource))
								continue;
							if (RESOURCES.DRINKS().is(rr.resource))
								continue;
							values[rr.resource.index()] += di*0.25*rr.rate*v;
						}
					}
				}
			}
			
			
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				
				
				values[e.resource().index()] += di*di*e.max()*(1.0/e.wearRate());
			}
			
			for (RoomEquip e : SETT.ROOMS().employment.tools.ALL) {
				values[e.resource.index()] += di*di*4*(1.0/e.degradePerDay*16);
			}
			
			for (ROOM_LIBRARY l : SETT.ROOMS().LIBRARIES) {
				for (settlement.room.industry.module.Industry i : l.industries()) {
					for (IndustryResource ii : i.ins())
						values[ii.resource.index()] += 0.2;
				}
			}
			
			for (RESOURCE res : RESOURCES.ALL()) {
				if (values[res.index()] > 0) {
					all.add(new ValuedImp<RESOURCE>(res).set(values[res.index()]));
				}
					
			}
			
		}
	}

	

	
}
