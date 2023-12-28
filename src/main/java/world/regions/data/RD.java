package world.regions.data;

import java.io.IOException;
import java.util.Arrays;

import game.GameDisposable;
import game.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.race.Race;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LinkedList;
import util.data.DataOL;
import world.WORLD;
import world.WORLD.WorldResource;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.building.RDBuildings;
import world.regions.data.gen.WGenRD;
import world.regions.data.pop.RDRace;
import world.regions.data.pop.RDRaces;
import world.regions.data.updating.RDUpdater;

public class RD extends WorldResource{

	private static RD self;
	

	private final RDBuildings buildings;
	private final RDOutput resources;
	private final RDRandom random;
	private final RDRaces races;
	private final RDAdmin admin;
	private final RDMilitary military;
	private final RDTax tax;
	private final RDHealth health;
	private final RDDistance distance;
	private final RDReligions religion;
	private final RDOwner owner;
	private final RDDevastation deva;
	private RDUpdater updater;
	private final WGenRD gen;
	private boolean distDirty = true;
	
	final RegData[] dreg = new RegData[WREGIONS.MAX];
	final Realm[] drea = new Realm[FACTIONS.MAX];
	private final RDInit init = new RDInit();
	
	public RD(WREGIONS regions) throws IOException {
		
		self = this;
		
		admin = new RDAdmin(init);
		distance = new RDDistance(init);
		random = new RDRandom(init);
		health = new RDHealth(init);
		resources = new RDOutput(init);
		military = new RDMilitary(init);
		races = new RDRaces(init);
		tax = new RDTax(init);
		religion = new RDReligions(init);
		buildings = new RDBuildings(init);
		owner = new RDOwner(init);
		deva = new RDDevastation(init);
		gen = new WGenRD(init) {

			@Override
			public void clear() {
				RD.this.clear();
			}
			
		};
		
		new Faction.FactionActivityListener() {
			
			@Override
			public void remove(Faction f) {
				while(f.realm().regions() > 0) {
					Region r = f.realm().region(0);
					setFaction(r, null);
				}
			}

			@Override
			public void add(Faction f) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	

	@Override
	public void initAfterGameSetup() {
		
		for (ACTION c : init.beforeConnect)
			c.exe();
		
		for (ACTION c : init.connectable)
			c.exe();
		
		init.connectable.clear();
		updater = new RDUpdater(init);
		for (int i = 0; i < WREGIONS.MAX; i++)
			dreg[i] = new RegData(init.count.longCount());
		for (int i = 0; i < FACTIONS.MAX; i++)
			drea[i] = new Realm(init.rCount.longCount(), i);
		distDirty = true;
	}
	
	@Override
	protected void save(FilePutter file) {
		for (RegData r : dreg) {
			r.save(file);
		}
		for (Realm r : drea) {
			r.saver.save(file);
		}
		for (SAVABLE s : init.savable)
			s.save(file);
		init.count.checkSave(file);
		init.rCount.checkSave(file);
		updater.saver.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		
		for (RegData r : dreg) {
			r.load(file);
		}
		for (Realm r : drea) {
			r.saver.load(file);
		}
		
		
		for (SAVABLE s : init.savable)
			s.load(file);
		
		if (!init.count.check(file) | !init.rCount.check(file)) {
			for (RegData r : dreg) {
				Arrays.fill(r.data, 0);
			}
			for (Realm r : drea) {
				Arrays.fill(r.data, 0);
			}
		}
		
		updater.saver.load(file);
		distDirty = true;
		RD.ADMIN().change(FACTIONS.player().capitolRegion());
	}
	
	@Override
	protected void initBeforePlay() {
		for (ACTION a : init.beforePlay)
			a.exe();
	}
	
	@Override
	protected void clear() {
		for (RegData r : dreg) {
			r.clear();
		}
		for (Realm r : drea) {
			r.saver.clear();
		}
		for (SAVABLE s : init.savable)
			s.clear();;
		updater.saver.clear();
		distDirty = true;
	}
	
	@Override
	public void update(float ds, Profiler prof) {
		prof.logStart(this);
		if (distDirty) {
			distDirty = false;
			distance.init();
		}
		updater.update(ds);
		prof.logEnd(this);
	}
	

	final static class RegData{

		final long[] data;
		short factionI = -1;
		
		private RegData(int am){
			data = new long[am];
		}

		public void save(FilePutter file) {
			file.lsE(data);
			file.s(factionI);
		}
		
		public void load(FileGetter file) throws IOException {
			file.lsE(data);
			factionI = file.s();
		}

		public void clear() {
			Arrays.fill(data, 0);
			factionI = -1;
		}
		
	}
	

	public final class RDInit {
		
		public final DataOL<Region> count = new DataOL<Region>() {

			@Override
			protected long[] data(Region t) {
				return dreg[t.index()].data;
			}
		
		};
		
		public final DataOL<Faction> rCount = new DataOL<Faction>() {

			@Override
			protected long[] data(Faction t) {
				return drea[t.index()].data;
			}
		
		};
		
		public LinkedList<RDUpdatable> upers = new LinkedList<>();
		public final LinkedList<RDAddable> adders = new LinkedList<>();
		public final LinkedList<ACTION> connectable = new LinkedList<>();
		public final LinkedList<ACTION> beforeConnect = new LinkedList<>();
		public final LinkedList<ACTION> beforePlay = new LinkedList<>();
		public final LinkedList<SAVABLE> savable = new LinkedList<>();
		public final RDDefis deficiencies = new RDDefis();
		
//		public final ResColl<RDBuilding> buildingMap = new ResColl<>("WORLD_BUILDINGS");
//		public final ArrayListGrower<RDBuilding> buildings = new ArrayListGrower<>();
		public final ArrayListGrower<ACTION_O<Region>> gens = new ArrayListGrower<>();
	}
	
	public interface RDUpdatable {
		void update(Region reg, double time);
		void init(Region reg);
	}
	
	public interface RDAddable {
		void removeFromFaction(Region r);
		void addToFaction(Region r);
	}
	
	public interface RDGeneratable {
		
		void generate(Region r);
		
	}

	
	public static RDBuildings BUILDINGS() {
		return self.buildings;
	}
	
	public static RDOutput OUTPUT() {
		return self.resources;
	}
	
	public static RDRandom RAN() {
		return self.random;
	}
	
	public static RDRaces RACES() {
		return self.races;
	}
	
	public static RDAdmin ADMIN() {
		return self.admin;
	}
	
	public static RDMilitary MILITARY() {
		return self.military;
	}
	
	public static RDTax TAX() {
		return self.tax;
	}
	
	public static RDHealth HEALTH() {
		return self.health;
	}
	
	public static RDDistance DIST() {
		return self.distance;
	}
	
	public static RDReligions RELIGION() {
		return self.religion;
	}
	
	public static RDOwner OWNER() {
		return self.owner;
	}
	
	public static RDUpdater UPDATER() {
		return self.updater;
	}
	
	public static RDDevastation DEVASTATION() {
		return self.deva;
	}
	
	public static RDDefis DEFS() {
		return self.init.deficiencies;
	}

	public static Realm REALM(Region reg) {
		if (self.dreg[reg.index()].factionI != -1)
			return self.drea[self.dreg[reg.index()].factionI];
		return null;
	}
	
	public static Realm REALM(Faction f) {
		return self.drea[f.index()];
	}
	
	private static void removeFaction(Region region) {
		RegData dd = RD.self.dreg[region.index()];
		if (dd.factionI == -1)
			return;
		
		for (RDAddable d : self.init.adders) {
			d.removeFromFaction(region);
		}
		
		Realm rr = RD.self.drea[dd.factionI];
		dd.factionI = -1;
		
		rr.regions.removeShort((short) region.index());
		if(rr.capitolI == region.index()) {
			if (rr.regions.size() > 0)
				rr.capitolI = (short) rr.regions.get(rr.regions.size()-1);
			else
				rr.capitolI = -1;
		}

	}
	
	public static void setFaction(final Region region, final Faction f) {
		
		FACTIONS.debug();
		
		RegData dd = RD.self.dreg[region.index()];
		if (f != null && dd.factionI == f.index())
			return;
		
		RD.OWNER().ownerI.set(region, (RD.OWNER().ownerI.get(region)+1)%RD.OWNER().ownerI.max(region));
		
		final Faction fold = region.faction();
		
		if (fold != null && fold instanceof FactionNPC && fold.realm().regions() <= 1) {
			FACTIONS.remove((FactionNPC) fold, true);
		}
		
		removeFaction(region);
		
		if (f != null) {
			Realm rr = f.realm();
			if (rr.regions.hasRoom()) {
				dd.factionI = (short) f.index();
				rr.regions.add((short)region.index());
				
				if (rr.capitolI == -1)
					rr.capitolI = (short) region.index();
				
				for (RDAddable d : self.init.adders) {
					d.addToFaction(region);
				}
			}
			f.realm().ferArea = 0;
			for (int ri = 0; ri < f.realm().regions(); ri++) {
				Region r = WORLD.REGIONS().all().get(ri);
				f.realm().ferArea += r.info.area()*r.info.fertility();
			}
		}
		
		self.distDirty = true;

		WORLD.MINIMAP().updateRegion(region);
		
		
		RDOwnerChanger.changeI ++;
		for (RDOwnerChanger ch : RDOwnerChanger.ownerChanges) {
			ch.change(region, fold, f);
		}
		
		FACTIONS.debug();

	}
	
	public static void setCapitol(Region region) {
		RegData dd = RD.self.dreg[region.index()];
		if (dd.factionI == -1)
			throw new RuntimeException("Can't set a rebel region as a capitol");
		
		Realm rr = RD.self.drea[dd.factionI];
		for (RDAddable d : self.init.adders) {
			d.removeFromFaction(region);
		}
		rr.capitolI = (short) region.index();
		for (RDAddable d : self.init.adders) {
			d.addToFaction(region);
		}
		self.distDirty = true;
		
		rr.regions.swap(0, rr.regions.indexOf((short) region.index()));
		WORLD.MINIMAP().updateRegion(region);
	}
	
	public static WGenRD generator() {
		return self.gen;
	}
	

	public static RDRace RACE(Race r) {
		return RACES().get(r);
	}
	
	public static abstract class RDOwnerChanger {
		
		public static int changeI;
		static final ArrayListGrower<RDOwnerChanger> ownerChanges = new ArrayListGrower<>();
		
		static {
			new GameDisposable() {
				
				@Override
				protected void dispose() {
					ownerChanges.clear();
				}
			};
		}
		
		public RDOwnerChanger() {
			ownerChanges.add(this);
		}
		
		public abstract void change(Region reg, Faction oldOwner, Faction newOwner);
	}
	
}
