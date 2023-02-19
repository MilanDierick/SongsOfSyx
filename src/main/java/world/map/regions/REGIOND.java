package world.map.regions;

import java.io.IOException;

import game.GAME;
import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import world.World;
import world.map.buildings.camp.WCampInstance;

public class REGIOND {

	static LinkedList<RegionDecree> decrees;
	
	private static final class Data implements SAVABLE{
		
		private final RegionRace[] races;
		private final RegionPopulation population;
		private final RegionMilitary military;
		private final RegionCivics civic;
		private final RegionTaxes production;
		private final RegionOwner owner;
		private final LIST<RegionDecree> all;
		
		Data(RegionInit init){
			decrees = new LinkedList<>();
			data = this;
			
			population = new RegionPopulation(init);
			races = new RegionRace[RACES.all().size()];
			for (Race r : RACES.all())
				races[r.index] = new RegionRace(init, r);
			
			owner = new RegionOwner(init);
			
			military = new RegionMilitary(init);
			civic = new RegionCivics(init);
			production = new RegionTaxes(init);
			
			init.finish();
			
			all = new ArrayList<>(decrees);
			decrees = null;
		}

		@Override
		public void save(FilePutter file) {
			for (RResource r : RResource.all) {
				if (r.saver() != null)
					r.saver().save(file);
			}
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (RResource r : RResource.all) {
				if (r.saver() != null)
					r.saver().load(file);
			}
			
		}

		@Override
		public void clear() {
			for (RResource r : RResource.all) {
				if (r.saver() != null)
					r.saver().clear();;
			}
			
		}
	}
	
	static abstract class RResource {
		
		static final ArrayList<RResource> all = new ArrayList<>(64);
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					all.clear();
				}
			};
		}
		
		RResource(){
			all.add(this);
		}
		
		abstract void remove(Region r, FRegions old);
		abstract void add(Region r, FRegions newR);
		abstract void update(Region r, double ds);
		abstract void generateInit(Region r);
		abstract SAVABLE saver();
		
	}
	
	private static Data data;
	
	static SAVABLE saver() {
		return data;
	}

	
	public static void init(RegionInit init) {
		new Data(init);
	}
	
	public static RegionPopulation POP() {
		return data.population;
	}
	
	public static RegionCivics CIVIC() {
		return data.civic;
	}
	
	public static RegionTaxes RES() {
		return data.production;
	}
	
	public static RegionMilitary MILITARY() {
		return data.military;
	}
	
	public static RegionRace RACE(Race r) {
		return data.races[r.index];
	}
	
	public static Faction faction(Region r) {
		FRegions k = data.owner.realm.get(r);
		if (k != null)
			return k.faction();
		return null;
	}
	
	public static RegionOwner OWNER() {
		return data.owner;
	}
	
	public static LIST<RegionDecree> ALL(){
		return data.all;
	}



	public static boolean isCapitol(Region region) {
		return faction(region) != null && faction(region).kingdom().realm().capitol() == region;
	}

	private static double uprisingChance = TIME.secondsPerDay*16.0;

	static void update(Region reg, double timeSinceLast) {
		for (RResource r : RResource.all) {
			r.update(reg, timeSinceLast);
		}
		
		if (reg.faction() == FACTIONS.player()) {
			double chance = timeSinceLast/uprisingChance;
			chance *= 1.0 - Math.pow(OWNER().loyalty_current.getD(reg), 0.75);
			if (chance > RND.rFloat()) {
				GAME.events().raider.tryRebel(reg);
			}
			
			
		}
	}


	public static FRegions REALM(Region r) {
		return data.owner.realm.get(r);
	}


	public static void initNewfaction(Region f) {
		for (RegionDecree d : ALL())
			d.set(f, 0);
		MILITARY().soldiers.set(f, 0);
		if (f.faction() == GAME.player()) {
			return;
		}
		
		for (WCampInstance i : World.camps().all()) {
			if (World.REGIONS().getter.get(i.coo()) == f)
				i.factionSet(f.faction());
		}
		
		MILITARY().decreeSoldiers.set(f, 1 + RND.rInt(MILITARY().decreeSoldiers.max(null)));
		
		
	}


	
}
