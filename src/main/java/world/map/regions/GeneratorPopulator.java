package world.map.regions;

import static world.World.*;

import java.util.Arrays;

import init.biomes.*;
import init.race.RACES;
import init.race.Race;
import settlement.main.SETT;
import settlement.room.industry.mine.ROOM_MINE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionTaxes.RegionIndustry;
import world.map.terrain.WorldTerrainInfo;

final class GeneratorPopulator {


	
	public GeneratorPopulator() {
		
	}

	private final StatCounter count = new StatCounter();

	
	void init(Region a) {
		if (a.isWater())
			return;
		count.clear();
		
		
		for (COORDINATE c : a.bounds()) {
			if (REGIONS().setter.get(c) == a) {
				count.count(c.x(), c.y());
			}
		}
		count.init(a);
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
			
			set(d.forest, r, Math.pow(info.get(TERRAINS.FOREST()).getD(), 1.5));
			
			for (RegionIndustry dd : d.inGrowable) {
				double m = 0;
				for (CLIMATE cl : CLIMATES.ALL()){
					if (dd.industry.blue.isAvailable(cl)) {
						double em = CLIMATES.BONUS().mul(dd.industry.bonus(),cl)*(1+CLIMATES.BONUS().add(dd.industry.bonus(),cl));
						m += climates[cl.index()]*em;
					}
				}
				m *= Math.pow(info.fertility().getD(), 1.5)*2.0;
				m *= info.get(TERRAINS.NONE()).getD();
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
					m *= Math.pow(info.fertility().getD(), 0.5);
					m *= Math.pow(info.get(TERRAINS.NONE()).getD(), 1.5);
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
				m*= (info.get(TERRAINS.OCEAN()).getD() + info.get(TERRAINS.WET()).getD());
				double v = m*1.5*(1.0 + RND.rSign()*RND.rExpo());
				set(dd, r, v);
			}
			
		
			for (ROOM_MINE m : SETT.ROOMS().MINES) {
				double amount = 0;
				
				for (TERRAIN t : TERRAINS.ALL()) {
					amount += info.get(t).getD()*m.minable.terrain(t)*2;
				}
				RegionIndustry res = d.inMinable.get(m.typeIndex());
				
				double a = amount*(RND.rFloat());
				
				if (RND.rFloat() < Math.pow(amount, 0.5))
					a += RND.rFloat();

				set(res, r,a);
				
			}

			{
				double pop = info.fertility().getD()*area/GeneratorAssigner.maxSize;
				
				REGIOND.POP().maxbase.setD(r, CLAMP.d(pop, 0, 1));
				for (Race ra : RACES.all()) { 
					double t = 0;
					for (TERRAIN te : TERRAINS.ALL()) {
						t += info.get(te).getD()*ra.population().terrain(te);
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
