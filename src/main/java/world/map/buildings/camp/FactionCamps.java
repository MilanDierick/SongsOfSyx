package world.map.buildings.camp;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import snake2d.util.sets.LIST;

public final class FactionCamps {

	private final int[][] availableRace = new int[FACTIONS.MAX][RACES.all().size()+1];
	private long[][] replenishPerDayRace = new long[FACTIONS.MAX][RACES.all().size()+1];
	private final int[] totalMax;
	private int[][] available;
	private static long rI = 100000;
	private static double dI = 1.0/rI;
	
	FactionCamps(LIST<WCampType> types) {
		totalMax = new int[types.size()];
		available = new int[FACTIONS.MAX][types.size()];
	}
	
	
	
	void add(WCampInstance c, int delta) {
		int am = c.max*delta;
		if (c.faction() != null) {
			
			availableRace[c.faction().index()][c.race().index] += am;
			availableRace[c.faction().index()][RACES.all().size()] += am;
			long re = (long) (c.replenishRateDay*rI*delta);
			replenishPerDayRace[c.faction().index()][c.race().index] += re;
			replenishPerDayRace[c.faction().index()][RACES.all().size()] += re;
			available[c.faction().index()][c.type().index()] += am;
		}
		totalMax[c.type().index()] += am;
	}
	
	public int max(Faction f, Race race) {
		return availableRace[f.index()][race == null ? RACES.all().size(): race.index];
	}
	
	public double replenishPerDay(Faction f, Race race) {
		return replenishPerDayRace[f.index()][race == null ? RACES.all().size(): race.index]*dI;
	}
	
	void clear() {
		for (int[] i : availableRace)
			Arrays.fill(i, 0);
		for (int[] i : available)
			Arrays.fill(i, 0);
		for (long[] l : replenishPerDayRace)
			Arrays.fill(l, 0);
		Arrays.fill(totalMax, 0);
	}
	
	int total(WCampType t) {
		return totalMax[t.index()];
	}
	
	int current(Faction f, WCampType t) {
		return available[f.index()][t.index()];
	}
	
}
