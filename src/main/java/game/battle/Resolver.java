package game.battle;

import game.battle.Conflict.Side;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.RES;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.World;
import world.army.WARMYD;
import world.army.WDIV;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class Resolver {

	public static void resolveAI(Conflict conflict) {
		
		
		if (conflict.sideA.power < conflict.sideB.power/2) {
			Resolver.autoRetreat(conflict.sideA);
			Resolver.autoRetreatVictor(conflict.sideB);
			Resolver.retreat(conflict.sideA, conflict.sideB);
		}else if (conflict.sideB.power < conflict.sideA.power/2) {
			Resolver.autoRetreat(conflict.sideB);
			Resolver.autoRetreatVictor(conflict.sideA);
			Resolver.retreat(conflict.sideB, conflict.sideA);
		}else {
			conflict.randomize();
			
			Resolver.autoFight(conflict.sideB);
			Resolver.autoFight(conflict.sideA);
			if (conflict.sideA.victory) {
				Resolver.retreat(conflict.sideB, conflict.sideA);
			}else {
				Resolver.retreat(conflict.sideA, conflict.sideB);
			}
		}
	}
	
	public static SideResult autoRetreat(Side s) {
		if (s.mustFightMen == 0) {
			return new SideResult(s, Result.RETREAT);
		}
		return apply(s, Result.RETREAT);
	}
	
	public static SideResult autoRetreatVictor(Side s) {
		return new SideResult(s, Result.VICTORY);
	}
	
	public static SideResult autoFight(Side s) {
		return apply(s,  s.victory ? Result.VICTORY : Result.DEFEAT);
	}
	
	public static void retreat(Side s, Side victor) {
		for (WArmy e : s)
			retreat(e);
		
		for (WArmy e : victor) {
			if (e.state() != WArmyState.besieging)
				e.stop();
		}
	}
	
	public static void retreat(final WArmy a) {
		
		
		if (!a.added())
			return;
		
		a.stop();
		
		if (!a.added())
			return;
		
		RES.flooder().init(a);
		RES.flooder().pushSloppy(a.ctx(), a.cty(), 0);
		
		
		while(RES.flooder().hasMore()) {
			
			PathTile t = RES.flooder().pollSmallest();
			
			if (!Util.isHostileTile(a, t)) {
				RES.flooder().done();
				a.teleport(t.x(), t.y());
				return;
			}
			
			int ri = RND.rInt(DIR.ORTHO.size());
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get((ri+di)%DIR.ORTHO.size());
				if (World.REGIONS().haser.is(t, d)) {
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
			
		}
		RES.flooder().done();
		
		if (WARMYD.men(null).get(a) == 0)
			a.disband();
		
	}
	
	public static void conquer (Region reg, Faction f, double kills) {

		if (reg.faction() != null && reg.faction().kingdom().realm().capitol() == reg) {
			Region newCapitol = reg;
			int ri = RND.rInt(reg.faction().kingdom().realm().regions().size());
			for (int i = 0; i < reg.faction().kingdom().realm().regions().size(); i++) {
				Region r = reg.faction().kingdom().realm().regions().get((i+ri)%reg.faction().kingdom().realm().regions().size());
				if (r != newCapitol) {
					newCapitol = r;
					break;
				}
			}
			
			if (newCapitol == reg || 
					(f != null && WARMYD.qualityF().get(reg.faction()) <  WARMYD.qualityF().get(f)/2 && RND.oneIn(reg.faction().kingdom().realm().regions().size()))){
				new Messages.MessageFactionDestroyed(reg.faction());
				reg.faction().remove();
				
				
			}else {
				REGIOND.OWNER().setCapitol(newCapitol.cx(), newCapitol.cy(), reg.faction());
				REGIOND.OWNER().realm.set(reg, f == null ? null : f.kingdom().realm());
				new Messages.MessageCapitolMoved(newCapitol.faction());
			}
			
			REGIOND.OWNER().realm.set(reg, f == null ? null : f.kingdom().realm());
			
		}else {
			if (reg.faction() == FACTIONS.player()) {
				new Messages.MessageFallen(reg);
			}
			
			
			REGIOND.OWNER().realm.set(reg, f == null ? null : f.kingdom().realm());
		}
		
		for (Race r : RACES.all()) {
			REGIOND.RACE(r).population.incD(reg, -kills);
//			REGIOND.RACE(r).loyalty.setD(reg, 1-kills);
		}
	}
	
	
	public static class PlayerBattle {
		
		private WInduStored[][] divs = new WInduStored[Config.BATTLE.DIVISIONS_PER_ARMY][];
		private int[] enemySurvivors = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
		private int[] capturedPlayer = new int[RACES.all().size()];
		private int[] capturedEnemy = new int[RACES.all().size()];
		private final Result result;
		
		PlayerBattle(boolean timer, boolean retreat){
			if (retreat)
				result = Result.RETREAT;
			else
				result = (timer || SETT.ARMIES().player().men() == 0) ? Result.DEFEAT : Result.VICTORY; 
			
			
			int[] count = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
			
			if (retreat || timer) {
				int losses = (int) Math.ceil(SETT.ARMIES().enemy().men()*Conflict.retreatPenalty);
				double dlosses = (double)losses/SETT.ARMIES().player().men();
				for (Div d : SETT.ARMIES().player().divisions()) {
					int am = (int) (STATS.BATTLE().DIV.stat().div().get(d)*dlosses);
					if (d.settings.isFighting())
						am += (STATS.BATTLE().DIV.stat().div().get(d)*0.75);
					am = CLAMP.i(am, 0, STATS.BATTLE().DIV.stat().div().get(d));
					count[d.index()] = am;
				}
				for (Div d : SETT.ARMIES().player().divisions()) {
					divs[d.index()] = new WInduStored[STATS.BATTLE().DIV.stat().div().get(d) - count[d.index()]];
					count[d.index()] = 0;
				}
			}else {
				for (Div d : SETT.ARMIES().player().divisions()) {
					divs[d.index()] = new WInduStored[STATS.BATTLE().DIV.stat().div().get(d)];
				}
			}
			
			
			
			
			for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
				if (e instanceof Humanoid) {
					Humanoid h = (Humanoid) e;
					Div d = STATS.BATTLE().DIV.get(h);
					if (d == null) {
						if (h.indu().hType() == HTYPE.ENEMY)
							capturedEnemy[h.race().index]++;
						else
							capturedPlayer[h.race().index]++;
					} else {
						
						if (d.index() >= Config.BATTLE.DIVISIONS_PER_ARMY) {
							
							enemySurvivors[d.index() - Config.BATTLE.DIVISIONS_PER_ARMY] ++;
						}else {
							if (count[d.index()] >= divs[d.index()].length) {
								
							}else {
								divs[d.index()][count[d.index()]++] = new WInduStored(h);
							}
							
						}
					}
				}
			}
			
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				capturedEnemy[ri] *= 0.3;
				capturedPlayer[ri] *= 0.3;
			}
		}
		
		SideResult apply(Side side) {
			
			if (!side.isPlayer()) {
				Result result = (this.result == Result.VICTORY ? Result.DEFEAT : Result.VICTORY);
				return Resolver.apply(side, enemySurvivors, capturedEnemy, result, null);
			}
			
			int[] divsCount = new int[divs.length];
			for (int i = 0; i < divsCount.length; i++)
				divsCount[i] = divs[i].length;
			
			return Resolver.apply(side, divsCount, capturedPlayer, result, divs);
			
		}
		
	}
	
	private static SideResult apply(Side side, Result result) {
		
		double deaths = 0;
		double captives = 0;
		
		if (result == Result.VICTORY) {
			if (side.losses == 0)
				return new SideResult(side, result);
			deaths = (double)side.losses/side.men;
			captives = RND.rFloat()*deaths;
			
			deaths -= captives;
		}else if (result == Result.RETREAT) {
			deaths = (double)side.retreatLosses/side.mustFightMen;
			captives = RND.rFloat()*deaths;
			deaths -= captives;
		}else if (result == Result.DEFEAT) {
			deaths = (double)side.losses/side.men;
			captives = RND.rFloat()*deaths;
			deaths -= captives;
		}
		
		if (deaths + captives > 1) {
			captives = 1.0-deaths;
		}
		
		int am = 0;
		if (side.garrison() != null) {
			am += REGIOND.MILITARY().divisions(side.garrison()).size();
		}
		
		for (WArmy a : side) {
			am += a.divs().size();
		}
		
		int[] survivors = new int[am];
		int[] captured = new int[RACES.all().size()];
		int[] max = new int[am];
		
		int di = 0;
		if (side.garrison() != null) {
			for (WDIV d : REGIOND.MILITARY().divisions(side.garrison())) {
				int dd = (int) Math.ceil(d.men()*deaths);
				int cc = (int) Math.ceil(d.men()*captives);
				if (dd+cc > d.men()) {
					cc = d.men()-dd;
				}
				survivors[di] = d.men()-(dd+cc);
				captured[d.race().index()] += cc;
				max[di] = d.men();
				di++;
			}
		}
		
		int ai = 0;
		for (WArmy a : side) {
			
			if (di >= Config.BATTLE.DIVISIONS_PER_ARMY)
				break;
			if (!side.mustFight[ai]) {
				for (int i = 0; i < a.divs().size(); i++) {
					WDIV d = a.divs().get(i);
					survivors[di] = d.men();
					max[di] = d.men();
					di++;
					
					if (di >= Config.BATTLE.DIVISIONS_PER_ARMY)
						break;
				}
			}else {
				for (int i = 0; i < a.divs().size(); i++) {
					WDIV d = a.divs().get(i);
					int dd = (int) Math.ceil(d.men()*deaths);
					int cc = (int) Math.ceil(d.men()*captives);
					if (dd+cc > d.men()) {
						cc = d.men()-dd;
					}
					survivors[di] = d.men()-(dd+cc);
					captured[d.race().index()] += cc;
					max[di] = d.men();
					di++;
					
					if (di >= Config.BATTLE.DIVISIONS_PER_ARMY)
						break;
				}
			}
		}
		
		di = side.garrison() != null ? REGIOND.MILITARY().divisions(side.garrison()).size() : 0;
		
		for (int i = 0; i < survivors.length*6; i++) {
			int i1 = RND.rInt(survivors.length);
			int i2 = RND.rInt(survivors.length);
			if (survivors[i1] == 0)
				continue;
			int a = RND.rInt(survivors[i1]);
			a = CLAMP.i(a, 0, max[i2]-survivors[i2]);
			survivors[i2] += a;
			survivors[i1] -= a;
			
		}
		
		
		return apply(side, survivors, captured, result, null);
		
		
	}
	
	private static SideResult apply(Side side, int[] survivorDivs, int[] captured, Result result, WInduStored[][] indus) {

		SideResult res = new SideResult(side, result);
		
		for (int i = 0; i < captured.length; i++)
			res.captured[i] = captured[i];
		
		int di = 0;
		if (side.garrison() != null) {
			for (WDIV d : REGIOND.MILITARY().divisions(side.garrison())) {
				
				for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
					double had = d.equip(m)*d.men();
					double has = d.equip(m)*survivorDivs[di];
					int lost = (int) Math.ceil(had-has);
					res.lost[m.resource().index()] += lost;
				}
				di++;
			}
			
			if (result != Result.VICTORY)
				REGIOND.MILITARY().extractSpoils(side.garrison(), res.lost);
			
			di = 0;
			for (WDIV d : REGIOND.MILITARY().divisions(side.garrison())) {
				res.garrisonLost += d.men()-survivorDivs[di];
				if (indus != null)
					d.resolve(indus[di]);
				else
					d.resolve(survivorDivs[di], CLAMP.d(side.dPower + d.experience(), 0, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null)));
				di++;
			}			
		}
		
		int ai = 0;
		
		for (WArmy a : side) {
			int dii = di;
			
			if (dii >= Config.BATTLE.DIVISIONS_PER_ARMY)
				break;
			int[] ll = new int[STATS.EQUIP().military_all().size()]; 
			for (int i = 0; i < a.divs().size(); i++) {
				WDIV d = a.divs().get(i);
				res.deaths[ai] += d.men() - survivorDivs[dii];
				for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
					double had = d.equip(m)*d.men();
					double has = d.equip(m)*survivorDivs[dii];
					int lost = (int) Math.ceil(had-has);
					ll[m.indexMilitary()] += lost;
				}
				dii++;
				
				if (dii >= Config.BATTLE.DIVISIONS_PER_ARMY)
					break;
			}

			for (int i = 0; i < a.divs().size(); i++) {
				WDIV d = a.divs().get(i);
				if (indus != null)
					d.resolve(indus[di]);
				else
					d.resolve(survivorDivs[di], CLAMP.d(side.dPower + d.experience(), 0, STATS.BATTLE().COMBAT_EXPERIENCE.indu().max(null)));
				di++;
				
				if (di >= Config.BATTLE.DIVISIONS_PER_ARMY)
					break;
			}
			
			if (result != Result.VICTORY) {
				double p = result == Result.RETREAT ? 0.8 : 0.2;
				for (ArmySupply s : RESOURCES.SUP().ALL()) {
					int am = (int) (WARMYD.supplies().get(s).current().get(a)*p);
					res.lost[s.resource.index()] += WARMYD.supplies().get(s).current().get(a) - am;
					WARMYD.supplies().get(s).current().set(a, am);
				}
				for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
					res.lost[m.resource().index()] += CLAMP.i(ll[m.indexMilitary()], 0, WARMYD.supplies().get(m).current().get(a));
					WARMYD.supplies().get(m).current().inc(a, -ll[m.indexMilitary()]);
				}
			}
			ai++;
		}

		
		return res;
	}
	
	enum Result {
		VICTORY,
		DEFEAT,
		RETREAT
	}
	
	public static class SideResult {
		
		public final int[] deaths;
		public final int[] captured = new int[RACES.all().size()];
		public final int[] lost = new int[RESOURCES.ALL().size()];
		public final Result res;
		public int garrisonLost;
		
		public SideResult(Side side, Result res) {
			deaths = new int[side.size()];
			this.res = res;
		}
		
	}
	
}
