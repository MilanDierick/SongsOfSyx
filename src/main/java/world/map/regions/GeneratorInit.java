package world.map.regions;

import static world.World.*;

import java.util.Arrays;

import init.RES;
import init.biomes.*;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.room.industry.mine.ROOM_MINE;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.World;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionTaxes.RegionIndustry;
import world.map.terrain.WorldTerrainInfo;

final class GeneratorInit {


	
	public GeneratorInit() {
		
	}

	private final StatCounter count = new StatCounter();
	private final Json jland = new Json(PATHS.NAMES().get("WorldAreas"));
	private final LandCounter cMisc = new LandCounter(jland.texts("MISC"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return MOUNTAIN().is(tx, ty);
			}
		};
	private final LandCounter cOcean = new LandCounter(jland.texts("OCEAN"), 0.15) {
		@Override
		boolean count(int tx, int ty) {
			return WATER().OCEAN.is(tx, ty);
		}
	};
	private final LandCounter cLake = new LandCounter(jland.texts("LAKE"), 0.15) {
		@Override
		boolean count(int tx, int ty) {
			return WATER().LAKE.is(tx, ty);
		}
	};
	
	private final CharSequence[] sOceans = jland.texts("OCEAN_ADDONS");
	private final CharSequence[] sLakes  = jland.texts("LAKE_ADDONS");
	private final CharSequence[] sIslands  = jland.texts("ISLAND_ADDONS");
		
	private final LandCounter[] counts = new LandCounter[] {
		new LandCounter(jland.texts("MOUNTAIN"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return MOUNTAIN().is(tx, ty);
			}
		},
		new LandCounter(jland.texts("FOREST"), 0.4) {
			@Override
			boolean count(int tx, int ty) {
				return FOREST().is.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("RIVER"), 0.1) {
			@Override
			boolean count(int tx, int ty) {
				return WATER().isRivery.is(tx, ty);
			}
		},
		cLake,
		cOcean,
		new LandCounter(jland.texts("STEPPE"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return GROUND().STEPPE.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("DESERT"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return GROUND().DESERT.is(tx, ty);
			}
		},
	};
	
	void initWater(COORDINATE start) {
		
		RES.filler().init(this);
		RES.filler().filler.set(start);
		
		Region a = REGIONS().setter.get(start);
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		int area = 0;
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (!REGIONS().setter.is(c, a)) {
				continue;
			}
			area++;
			if (c.x() < x1)
				x1 = c.x();
			if (c.x() > x2)
				x2 = c.x();
			if (c.y()< y1)
				y1 = c.y();
			if (c.y() > y2)
				y2 = c.y();
			for (DIR d : DIR.ORTHO)
				if (TBOUNDS().holdsPoint(c, d))
					RES.filler().filler.set(c, d);
			
		}
		
		x2 += 1;
		y2 += 1;
		a.bounds.set(x1, x2, y1, y2);
		
		RES.filler().done();
		RES.flooder().init(this);
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (IN_BOUNDS(x, y) && REGIONS().setter.is(x, y, a)) {
					for (DIR d : DIR.ALL) {
						if (!REGIONS().setter.is(x, y, d, a)) {
							RES.flooder().pushSloppy(x, y, 0);
							break;
						}
					}
				}
				
			}
		}
		
		PathTile centre = null;
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (REGIONS().setter.is(t, a)) {
				centre = t;
			}
			
			for (DIR d : DIR.ALL) {
				if (REGIONS().setter.is(t, d, a))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		
		RES.flooder().done();
		
		int tz = 0;
		
		if (centre.getValue() > 14)
			tz = 3;
		else if (centre.getValue() > 4)
			tz = 2;
		else if (centre.getValue() > 2)
			tz = 1;
		
		a.init(centre.x(), centre.y(), centre.x(), centre.y(), area, tz);
		
		if (World.WATER().isLaky.is(centre)) {
			a.name().clear().add(sLakes[RND.rInt(sLakes.length)]);
			a.name().insert(0, cLake.getName());
		}else {
			a.name().clear().add(sOceans[RND.rInt(sOceans.length)]);
			a.name().insert(0, cOcean.getName());
		}
		
	}
	
	void init(COORDINATE start) {
		
		RES.filler().init(this);
		RES.filler().filler.set(start);
		count.clear();
		
		Region a = REGIONS().setter.get(start);
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		int area = 0;
		
		boolean isIsland = true;
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (!REGIONS().setter.is(c, a)) {
				isIsland &= WATER().has.is(c);
			}else {
				count.count(c.x(), c.y());
				area++;
				if (c.x() < x1)
					x1 = c.x();
				if (c.x() > x2)
					x2 = c.x();
				if (c.y()< y1)
					y1 = c.y();
				if (c.y() > y2)
					y2 = c.y();
				for (DIR d : DIR.ORTHO)
					if (TBOUNDS().holdsPoint(c, d))
						RES.filler().filler.set(c, d);
			}
			
		}
		
		x2 += 1;
		y2 += 1;
		a.bounds.set(x1, x2, y1, y2);
		
		for (LandCounter c : counts) {
			c.count = 0;
			c.value = 0;
		}
		
		RES.filler().done();
		RES.flooder().init(this);
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				if (IN_BOUNDS(x, y) && REGIONS().setter.is(x, y, a)) {
					for (DIR d : DIR.ALL) {
						if (!REGIONS().setter.is(x, y, d, a)) {
							RES.flooder().pushSloppy(x, y, 0);
							break;
						}
					}
				}
				
			}
		}
		
		PathTile centre = null;
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (REGIONS().setter.is(t, a)) {
				
				if (Generator.test(t.x(), t.y(), a)) {
					centre = t;
				}
				
				for (LandCounter c : counts) {
					if (c.count(t.x(), t.y()))
						c.count++;
				}
					
			}
			
			for (DIR d : DIR.ALL) {
				if (REGIONS().setter.is(t, d, a))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		COORDINATE cc = centre;
		{
			for (int i = 0; i < 5; i++) {
				int x = centre.x() + (int) (RND.rSign()*RND.rInt((int) centre.getValue()));
				int y = centre.y() + (int) (RND.rSign()*RND.rInt((int) centre.getValue()));
				if (REGIONS().setter.is(x, y, a) && Generator.test(x, y, a)) {
					cc = RES.flooder().get(x, y);
					break;
				}
			}
		}
		
		
		RES.flooder().done();
		
		int tz = 0;
		
		if (centre.getValue() > 14)
			tz = 3;
		else if (centre.getValue() > 4)
			tz = 2;
		else if (centre.getValue() > 2)
			tz = 1;
		
		a.init(cc.x(), cc.y(), centre.x(), centre.y(), area, tz);
		
		LandCounter bestFit = cMisc;
		
		for (LandCounter c : counts) {
			c.value = c.count/(double)area;
			c.value /= c.treshold;
			if (c.value > 1.0 && c.value > bestFit.value)
				bestFit = c;
		}
		
		
		if (isIsland) {
			a.name().clear().add(sIslands[RND.rInt(sIslands.length)]);
			a.name().insert(0, bestFit.getName());
		}else {
			a.name().clear().add(bestFit.getName());
		}
		
		count.init(a);
		
	}
	
	abstract class LandCounter {
		
		private final String[] names;
		private int nameI =0;
		private double treshold;
		private double value;
		
		LandCounter(String[] names, double treshold){
			for (int i = 0; i < names.length; i++) {
				String old = names[i];
				int k = RND.rInt(names.length);
				names[i] = names[k];
				names[k] = old;
			}
			
			this.names = names;
			this.treshold = treshold;
		}
		
		String getName() {
			int i = nameI;
			nameI++;
			if (nameI >= names.length)
				nameI = 0;
			return names[i];
		}
		
		int count;
		abstract boolean count(int tx, int ty);
		
	}
	
	private static class StatCounter {
		
		private final WorldTerrainInfo info = new WorldTerrainInfo();
		
		int area = 0;
		double[] climates = new double[CLIMATES.ALL().size()];
		
		private void clear() {
			area = 0;
			Arrays.fill(climates, 0);
			info.clear();
		}
		
		private void count(int tx, int ty) {
			area++;
			info.add(tx, ty);
			climates[CLIMATE().getter.get(tx, ty).index()] ++;
			
		}
		
		private void init(Region r) {
			info.divide(area);
			for (int i = 0; i < climates.length; i++) {
				climates[i] /= area;
			}
			


			
			RegionTaxes d = REGIOND.RES();
			
			set(d.forest, r, Math.pow(info.get(TERRAINS.FOREST()), 1.5));
			
			for (RegionIndustry dd : d.inGrowable) {
				double m = 0;
				for (CLIMATE cl : CLIMATES.ALL()){
					if (dd.industry.blue.isAvailable(cl)) {
						double em = CLIMATES.BONUS().mul(dd.industry.bonus(),cl)*(1+CLIMATES.BONUS().add(dd.industry.bonus(),cl));
						m += climates[cl.index()]*em;
					}
				}
				m *= Math.pow(info.fertility(), 1.5)*2.0;
				m *= info.get(TERRAINS.NONE());
				double v = m*(1.0 + RND.rExpo());
				set(dd, r, v);
			}
			
			{
				for (RegionIndustry dd : d.inHusbandry) {
					double m = 0;
					for (CLIMATE cl : CLIMATES.ALL()){
						if (dd.industry.blue.isAvailable(cl)) {
							double em = CLIMATES.BONUS().mul(dd.industry.bonus(),cl)*(1+CLIMATES.BONUS().add(dd.industry.bonus(),cl));
							m += climates[cl.index()]*em;
						}
					}
					m *= Math.pow(info.fertility(), 0.5);
					m *= Math.pow(info.get(TERRAINS.NONE()), 1.5);
					double v = m*(1.0 + RND.rExpo());
					set(dd, r, v);
				}
			}
			
			
			
			for (RegionIndustry dd : d.inMarine) {
				double m = 0;
				for (CLIMATE cl : CLIMATES.ALL()){
					if (dd.industry.blue.isAvailable(cl)) {
						double em = CLIMATES.BONUS().mul(dd.industry.bonus(),cl)*(1+CLIMATES.BONUS().add(dd.industry.bonus(),cl));
						m += climates[cl.index()]*em;
					}
				}
				m*= (info.get(TERRAINS.OCEAN()) + info.get(TERRAINS.WET()));
				double v = m*1.5*(1.0 + RND.rSign()*RND.rExpo());
				set(dd, r, v);
			}
			
		
			for (ROOM_MINE m : SETT.ROOMS().MINES) {
				double amount = 0;
				
				for (TERRAIN t : TERRAINS.ALL()) {
					amount += info.get(t)*m.minable.terrain(t)*2;
				}
				RegionIndustry res = d.inMinable.get(m.typeIndex());
				
				double a = amount*(RND.rFloat());
				
				if (RND.rFloat() < Math.pow(amount, 0.5))
					a += RND.rFloat();

				set(res, r,a);
				
			}

			{
				double pop = info.fertility()*area/GeneratorAssigner.maxSize;
				
				REGIOND.POP().maxbase.setD(r, CLAMP.d(pop, 0, 1));
				for (Race ra : RACES.all()) { 
					double t = 0;
					for (TERRAIN te : TERRAINS.ALL()) {
						t += info.get(te)*ra.population().terrain(te);
					}
					double c = 0;
					for (CLIMATE cl : CLIMATES.ALL())
						c += climates[cl.index()]*ra.population().climate(cl);
					double rate = c*t;
					REGIOND.RACE(ra).biome.setD(r, CLAMP.d(rate, 0, 1));
				}
			}
			
			
			//REGIOND.MILITARY().troops.setD(r, RND.rFloat());
//			REGIOND.POP().growth(RACES.all().get(0)).setD(r, 1.0);
//			d.populations.get(0).current.set(r, pop);
			
			
			for (RResource rr : RResource.all) {
				rr.generateInit(r);
			}
		}
		
		private void set(RegionIndustry res, Region r, double d) {
			res.prospect.setD(r, d);
		}
		
	}
	
}
