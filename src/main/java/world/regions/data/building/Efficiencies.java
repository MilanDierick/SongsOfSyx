package world.regions.data.building;

import game.boosting.BSourceInfo;
import init.biomes.*;
import init.resources.Minable;
import init.sprite.UI.UI;
import snake2d.util.misc.CLAMP;
import util.dic.DicMisc;
import world.regions.Region;
import world.regions.data.RBooster;
import world.regions.data.RD;

class Efficiencies {

	static RBooster MINABLE(Minable m, double from, double to, boolean isMul) {
		return new RBooster(new BSourceInfo(DicMisc.¤¤Minerals, m.resource.icon()), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				return t.info.minableBonus(m);
			}
			
		};
	}
	
	static RBooster CLIMATE(double[] clim, double from, double to, boolean isMul) {
		return new RBooster(new BSourceInfo(CLIMATES.INFO().name, UI.icons().s.heat), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				double res = 0;
				for (CLIMATE c : CLIMATES.ALL()) {
					res += t.info.climate(c)*clim[c.index()];
				}
				return res;
			}
			
		};
	}
	
	
	static RBooster RAN_PROSPECT(RDBuilding bu, double from, double to, boolean isMul) {
		double ii = 1.0/0b011;
		
		return new RBooster(new BSourceInfo(DicMisc.¤¤Prospect, UI.icons().s.magnifier), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				return RD.RAN().get(t, bu.index()*2, 2)*ii;
			}
			
			
		};
	}
	
	static RBooster FERTILITY(double from, double to, boolean isMul) {

		return new RBooster(new BSourceInfo(DicMisc.¤¤Fertility, UI.icons().s.sprout), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				return t.info.fertility();
			}
			
		};
	}
	
	static RBooster WATER(double from, double to, boolean isMul) {

		return new RBooster(new BSourceInfo(DicMisc.¤¤Water, UI.icons().s.fish), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				return CLAMP.d((t.info.terrain(TERRAINS.OCEAN()) + t.info.terrain(TERRAINS.WET()))*3, 0, 1);
			}
		
			
		};
	}

	static RBooster FOREST(double from, double to, boolean isMul) {

		return new RBooster(new BSourceInfo(TERRAINS.FOREST().name, UI.icons().s.sprout), from, to, isMul) {
			
			@Override
			public double get(Region t) {
				return CLAMP.d(t.info.terrain(TERRAINS.FOREST())*3, 0, 1);
			}
		
			
		};
	}
	
	
	
}
