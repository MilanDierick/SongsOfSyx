package world.map.regions;

import init.D;
import init.race.RACES;
import init.race.Race;
import util.data.DataO;
import world.map.regions.RegionTaxes.RegionIndustry;

final class RegionInit {

	public final DataO<Region> count;
	public final DataO<FRegions> realmcount;
	
	{
		D.gInit(this);
	}

	
	public final RegionFactors tmpGrowth = new RegionFactors(
			D.g("PopulationGrowth", "Population Growth"),
			D.g("PopulationGrowthDesc", "Affects how fast your subjects grow in numbers."));
	public final RegionFactors tmpLoyalty = new RegionFactors(
			D.g("Loyalty"), 
			D.g("LoyaltyD", "Loyalty decreases the chances of uprisings"));
	public final RegionFactors tmpProduction = new RegionFactors(
			D.g("Production"), 
			D.g("ProductionD", "Increases production of all regional industries"));
	public final RegionFactors soldiers = new RegionFactors(
			D.g("Soldiers"), 
			D.g("SoldierD", "Increases the amount of soldiers"));
	
	
	RegionInit(){
		this.count = new DataO<Region>() {
			
			@Override
			protected int[] data(Region t) {
				return t.data;
			}
		};
		this.realmcount = new DataO<FRegions>() {
			
			@Override
			protected int[] data(FRegions t) {
				return t.data;
			}
		};
	}
	
	public void finish() {
		for (Race r : RACES.all()) {
			
			for (RegionFactor f : tmpGrowth.factors())
				REGIOND.RACE(r).population_growth.addFactor(f);
			for (RegionFactor f : tmpLoyalty.factors())
				REGIOND.RACE(r).loyalty_target.addFactor(f);
			for (RegionFactor f : REGIOND.POP().capacity.factors())
				REGIOND.RACE(r).population_target.addFactor(f);
		}
		
		for (RegionIndustry r : REGIOND.RES().all) {
			for (RegionFactor f : tmpProduction.factors())
				r.factors.addFactor(f);
		}
//		
//		new RegionFactors.RegionFactorImp(REGIOND.RACE(r)) {
//			
//			@Override
//			public double factor(Region r) {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//		});
	}
	
	
}
