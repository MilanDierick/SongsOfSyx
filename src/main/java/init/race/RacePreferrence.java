package init.race;

import java.util.Arrays;
import java.util.Comparator;

import init.biomes.BUILDING_PREF;
import init.biomes.BUILDING_PREFS;
import init.resources.Edible;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.*;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.keymap.KEY_COLLECTION;

public final class RacePreferrence {

	public LIST<Edible> food;
	public final long foodMask;
	private final double[] structure = new double[BUILDING_PREFS.ALL().size()];
	private final double[] work = new double[SETT.ROOMS().employment.ALLS().size()];
	private final double[] others = new double[RACES.all().size()];
	private final double[] otherssqrt = new double[RACES.all().size()];
	public final LIST<ROOM_RESTHOME> resthomes;
	
	RacePreferrence(Json data) {
		
		data = data.json("PREFERRED");
		food = new ArrayList<>(RESOURCES.EDI().MAP.getManyByKeyWarn("FOOD", data));
		if (food.size() == 0)
			data.error("Must have a favorite food!", "FOOD");
		
		
		long m = 0;
		for (Edible e : food)
			m |= e.resource.bit;
		foodMask = m;
		{
			
			for (BUILDING_PREF p : BUILDING_PREFS.ALL())
				structure[p.index()] = p.defaultPref;
			
			BUILDING_PREFS.MAP().fill(structure, data, 0, 1);
		}
		
		
		
		{
			
			
			for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS())
				work[e.eindex()] = e.defaultFullfillment;
			
			new RoomsJson("WORK", data) {
				
				@Override
				public void doWithTheJson(RoomBlueprintImp pp, Json j, String key) {
					if (pp != null && pp instanceof RoomBlueprintIns<?> && ((RoomBlueprintIns<?>) pp).employment() != null) {
						work[((RoomBlueprintIns<?>) pp).employment().eindex()] = j.d(key, 0, 1);
					}else if (!key.equals(KEY_COLLECTION.WILDCARD)){
						j.errorGet("Not a workable room", key);
					}
				}
			};
			
			
		}
		
		{
			int rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					rh++;
				}
			}
			ROOM_RESTHOME[] hh = new ROOM_RESTHOME[rh];
			rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					hh[rh++] = h;
				}
			}
			
			Arrays.sort(hh, new Comparator<ROOM_RESTHOME>() {

				@Override
				public int compare(ROOM_RESTHOME o1, ROOM_RESTHOME o2) {
					return work[o1.employment().eindex()] > work[o2.employment().eindex()] ? 1 : -1;
				}
			});
			
			resthomes = new ArrayList<>(hh);
		}
		
		Arrays.fill(others, 1);
		
		if (data.has("OTHER_RACES")) {
			RACES.map().fill("OTHER_RACES", others, data, -100000, 1000000);
		}
		
		for (int i = 0; i < others.length; i++) {
			otherssqrt[i] = Math.sqrt(others[i]);
		}
		
	}
	
	public double structure(BUILDING_PREF p) {
		return structure[p.index()];
	}

	
	public double getWork(RoomEmploymentSimple e) {
		return work[e.eindex()];
	}
	
	public double other(Race race) {
		return others[race.index];
	}
	
	public double othersqrt(Race race) {
		return otherssqrt[race.index];
	}
	
	
}
