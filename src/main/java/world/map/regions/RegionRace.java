package world.map.regions;

import init.D;
import init.race.*;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.data.*;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionDecree.RegionDecreeImp;
import world.map.regions.RegionFactor.RegionFactorImp;

public final class RegionRace extends RResource {

	public final Race race;
	public final INT_OE<Region> population;
	public final RegionFactors population_target;
	public final RegionFactors population_growth;
	
	private final INT_OE<Region> ployalty;
	public final RegionFactors loyalty_target;
	public final INT_O<Region> loyalty;

	
	public final DOUBLE_OE<Region> biome;
	public final RegionDecree prosecute;
	public final RegionDecree exile;
	public final RegionDecree massacre;
	public final RegionDecree elevation;
	public final LIST<RegionDecree> decs;


	
	
	public final DOUBLE_O<Region> crowding = new DOUBLE_O<Region>() {

		private final INFO info = new INFO(¤¤Crowding, ¤¤CrowdingD);
		
		@Override
		public double getD(Region r) {
			double am = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				double g = REGIOND.RACE(RACES.all().get(ri)).population_target.getD(r);
				if (g <= 0)
					continue;

				am += g;
			}
			
			if (am == 0)
				return 0;
			
			double d = REGIOND.POP().capacity.getD(r)/am;
			if (d > 1)
				d = 1;
			
			return d;
		}
		
		@Override
		public INFO info() {
			return info;
		};
	
	};


	private static CharSequence ¤¤Population = "¤Population";
	private static CharSequence ¤¤PopulationD = "¤Current population per race.";
	private static CharSequence ¤¤Loyalty = "¤Loyalty";
	private static CharSequence ¤¤LoyaltyD = "¤Current Loyalty. Loyalty determines the chance of rebellion. Loyalty changes slowly based on the target. Increase loyalty by allocating admin points into loyalty boosting areas. Loyalty will also increase the longer a region has belonged to you. Loyalty is species specific.";

	private static CharSequence ¤¤Prosecute = "¤Prosecution";
	private static CharSequence ¤¤ProsecuteD = "¤Prosecuting a species, severely diminishes growth and decreases happiness.";
	
	private static CharSequence ¤¤Exile = "¤Exile";
	private static CharSequence ¤¤ExileD = "¤Forbid this species from immigrating and sends off any citizens to neighbouring regions where they are still welcome.";
	
	
	private static CharSequence ¤¤Massacre = "¤Massacre";
	private static CharSequence ¤¤MassacreD = "¤Commit genocide and instantly rid yourself of this species. Will cause an outrage of course, make sure you have enough military presence to handle an eventual uprising.";
	
	private static CharSequence ¤¤Elevate = "¤Elevation";
	private static CharSequence ¤¤ElevateD = "¤Some special treatment for a species increases growth and happiness.";
	
	private static CharSequence ¤¤Biome = "¤Species Biome";
	private static CharSequence ¤¤BiomeD = "¤The mix of climate and terrain of this region. Different species are better suited for different biomes";
	private static CharSequence ¤¤Crowding = "¤Crowding";
	private static CharSequence ¤¤CrowdingD = "¤As land is settled and it becomes more crowded, growth slows down.";
	private static CharSequence ¤¤Distant = "¤Distant {0}";
	
	private static CharSequence ¤¤Base = "¤Base";
	
	static {
		D.ts(RegionRace.class);
	}
	
	RegionRace(RegionInit init, Race race) {

		this.race = race;

		loyalty_target = new RegionFactors(¤¤Loyalty, ¤¤LoyaltyD);
		population_target = new RegionFactors(¤¤Population, ¤¤PopulationD);
		biome = init.count.new DataByte(¤¤Biome, ¤¤BiomeD);
		population_growth = new RegionFactors(init.tmpGrowth.info());
		
		new RegionFactorImp(population_growth, RacePopulation.¤¤reproductionRate, RacePopulation.¤¤reproductionRateD) {
			@Override
			public double getD(Region r) {
				return race.population().reproductionRate;
			}

			@Override
			public double next(Region r) {
				return getD(r);
			}
		};

		race.population();
		race.population();
		new RegionFactorImp(population_target, RacePopulation.¤¤rarity, RacePopulation.¤¤rarityD) {
			@Override
			public double getD(Region r) {
				return race.population().rarity;
			}
			
			@Override
			public double next(Region r) {
				return getD(r);
			}
		};
		
		new RegionFactorImp(population_target, biome.info()) {
			@Override
			public double getD(Region r) {
				return biome.getD(r);
			}
			
			@Override
			public double next(Region r) {
				return getD(r);
			}
		};
		
		population = init.count.new DataInt(¤¤Population, ¤¤PopulationD) {
			@Override
			public void set(Region t, int i) {
				if (t.realm() != null) {
					t.realm().population.inc(race, -get(t));
				}
				REGIOND.POP().total.inc(t, -get(t));
				super.set(t, i);
				if (t.realm() != null) {
					t.realm().population.inc(race, get(t));
				}
				REGIOND.POP().total.inc(t, get(t));
				
			};
			
			@Override
			public int max(Region t) {
				return (int) REGIOND.POP().capacity.getD(t);
			}
		};
		


		ployalty = init.count.new DataByte(¤¤Loyalty, ¤¤LoyaltyD);
		loyalty = ployalty;
		new RegionFactorImp(loyalty_target, ¤¤Base, ¤¤Base) {
			
			@Override
			public double getD(Region t) {
				if (REGIOND.OWNER().capitol.is(t))
					return 1.0;
				return 0.5;
			}
			
			@Override
			public double next(Region r) {
				if (REGIOND.OWNER().capitol.is(r))
					return 1.0;
				return 0.5;
			}
		};
		
		
		elevation = new Decree(init, 5, ¤¤Elevate, ¤¤ElevateD);
		elevation.connect(population_target, 1.5);
		elevation.connect(loyalty_target, 1.5);
		
		prosecute = new Decree(init, 1, ¤¤Prosecute, ¤¤ProsecuteD);
		prosecute.connect(population_target, 0.5);
		prosecute.connect(loyalty_target, 0.5);
		
		exile = new Decree(init, 2, ¤¤Exile, ¤¤ExileD);
		exile.connect(population_target, 0);
		exile.connect(loyalty_target, 0.5);
		
		massacre = new Decree(init, 20, ¤¤Massacre, ¤¤MassacreD);
		massacre.connect(population_target, 0);
		massacre.connect(loyalty_target, 0);
		
		decs = new ArrayList<>(elevation, prosecute, exile, massacre);
		
		
	}
	
	@Override
	void add(Region r, FRegions rr) {
		rr.population.inc(race, population.get(r));
	}
	
	@Override
	void remove(Region r, FRegions rr) {
		rr.population.inc(race, -population.get(r));
		for (RegionDecree d : decs)
			d.set(r, 0);
	}
	
	@Override
	void generateInit(Region r) {
		ployalty.setD(r, 1.0);
		population.set(r, targetPop(r));
		
		
	}

	public int targetPop(Region r) {
		
		
		int res = (int) (((population_target.getD(r)))*(crowding.getD(r)));
		if (res < 0)
			return 0;
		return res;

	}
	

	@Override
	void update(Region r, double ds) {
		
		{
			int n = ployalty.get(r);
			int t = (int) (loyalty_target.next(r)*ployalty.max(r));
			double d = t-n;
			if (d < 0) {
				n += -Math.ceil(-d*0.125); 
			}else
				n += Math.ceil(d*0.125/4);
			n = CLAMP.i(n, 0, ployalty.max(r));
			ployalty.set(r, n);
		}
		
		int n = targetPop(r);
		int c = population.get(r);
		
		
		if (n > c) {
			double nn = ((c+10.0)*population_growth.getD(r));
			
			c += (int)nn;
			nn -= (int)nn;
			if (nn > 0 && nn < 1)
				if (RND.rFloat() < nn)
					c+= 1;
			
			c = CLAMP.i(c, 0, n);
			population.set(r, c);
		}else if (n < c){
			double d = -(c)/16;
			if (d > -1)
				d = -1;
			c += d;
			c = CLAMP.i(c, n, c);
			population.set(r, c);
		}
		
	}
	


	
	@Override
	SAVABLE saver() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Decree extends RegionDecreeImp {

		
		final DataO<FRegions>.DataInt distantG;
		
		public Decree(RegionInit init, int cost, CharSequence name, CharSequence desc) {
			super(init.count.new DataBit(), cost, name, desc);
			distantG = init.realmcount.new DataInt();
		}

		@Override
		public void set(Region t, int i) {
			if (t.realm() != null)
				distantG.inc(t.realm(), -get(t));
			super.set(t, i);
			if (t.realm() != null)
				distantG.inc(t.realm(), get(t));
		}
		
		@Override
		void connect(RegionFactors f, double am) {
			super.connect(f, am);
			
			CharSequence n = new Str(¤¤Distant).insert(0, info().name);
			
			new RegionFactorImp(f, n, n) {
				
				@Override
				public double getD(Region r) {
					if (get(r) == 1)
						return 1;
					if (r.realm() != null && distantG.get(r.realm()) > 0) {
						if (am < 1)
							return am + (1.0-am)/8;
						else
							return 1 + (am -1)/8;
					}
					return 1;
				}
				
				@Override
				public double next(Region r) {
					return getD(r);
				}
			};
			
		}
	
		
	}



}
