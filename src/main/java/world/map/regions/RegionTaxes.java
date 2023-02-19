package world.map.regions;

import java.util.Arrays;

import game.time.TIME;
import init.D;
import init.RES;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicRes;
import util.info.INFO;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionDecree.RegionDecreeImp;
import world.map.regions.RegionFactor.RegionFactorImp;

public final class RegionTaxes extends RResource{

	public final LIST<RegionIndustry> inGrowable;
	public final LIST<RegionIndustry> inHusbandry;
	public final LIST<RegionIndustry> inMarine;
	public final LIST<RegionIndustry> inMinable;
	public final LIST<RegionIndustry> all;

	public final INT_O<Region> total_all;
	public final INT_OE<Region> total_farming;
	public final INT_OE<Region> total_fish;
	public final INT_OE<Region> total_mining;
	public final RegionIndustry forest;

	public final LIST<RegionResource> res;
	
	public final RegionDecree exhausting;
	public final INT_OE<Region> exhaustion;
	
	private final RegionResource[] map = new RegionResource[RESOURCES.ALL().size()];
	
	private static CharSequence ¤¤Prospect = "¤Prospect";
	private static CharSequence ¤¤ProspectD = "¤Prospects determine the potential output of this industry. It varies from region to region. The final output is your population multiplied by population and admin. Racial bonuses are also applied.";
	private static CharSequence ¤¤Exhaust = "¤Exhaust";
	private static CharSequence ¤¤ExhaustD = "¤If an industry is set to exhaust, its output is doubled. Industries can be exhausted for maximum a year and after, they will not produce any resources for four times the time they were exhausted.";
	private static CharSequence ¤¤Exhausted = "¤Exhausted";
	private static CharSequence ¤¤ExhaustedD = "¤Exhausted Industries will not produce anything until they've had time to recover. A fully exhausted resource needs a full year to recover.";
	static {
		D.ts(RegionTaxes.class);
	}
	
	RegionTaxes(RegionInit init){
		
		INT_OE<Region>	total_all = init.count.new DataShort();
		this.total_all = total_all;
		total_farming = init.count.new DataNibble() {
			
			@Override
			public void set(Region t, int s) {
				total_all.inc(t, -get(t));
				super.set(t, s);
				total_all.inc(t, get(t));
			}
		};
		total_fish = init.count.new DataNibble() {
			
			@Override
			public void set(Region t, int s) {
				total_all.inc(t, -get(t));
				super.set(t, s);
				total_all.inc(t, get(t));
			}
		};
		total_mining = init.count.new DataNibble() {
			
			@Override
			public void set(Region t, int s) {
				total_all.inc(t, -get(t));
				super.set(t, s);
				total_all.inc(t, get(t));
			}
		};

		exhausting = new RegionDecreeImp(init.count.new DataBit(), 4, ¤¤Exhaust, ¤¤ExhaustD);
		exhausting.connect(init.tmpLoyalty, 1, -0.2);
		
		exhaustion = init.count.new DataByte(new INFO(
				¤¤Exhausted, ¤¤ExhaustedD), (int) (4*TIME.years().bitConversion(TIME.days())));
		
		
		forest = new RegionIndustry(init, SETT.ROOMS().WOOD_CUTTER.industries().get(0), total_all);
		inGrowable = make(SETT.ROOMS().FARMS, init, total_farming);
		inHusbandry = make(SETT.ROOMS().PASTURES, init, total_farming);
		inMarine = make(SETT.ROOMS().FISHERIES, init, total_fish);
		inMinable = make(SETT.ROOMS().MINES, init, total_mining);
		all = inGrowable.join(inHusbandry).join(inMarine).join(inMinable).join(forest);
		
		final int[] rMap = new int[RESOURCES.ALL().size()]; 
		{
			Arrays.fill(rMap, -1);
			boolean[] added = new boolean[RESOURCES.ALL().size()];
			int am = 0;
			for (RegionIndustry i : all) {
				for (IndustryResource r : i.industry.outs()) {
					if (!added[r.resource.index()]) {
						am++;
						added[r.resource.index()] = true;
					}
				}
			}
			ArrayList<RegionResource> resources = new ArrayList<>(am);
			Arrays.fill(added, false);
			am = 0;
			for (RegionIndustry i : all) {
				for (int ri = 0; ri < i.industry.outs().size(); ri++) {
					IndustryResource r = i.industry.outs().get(ri);
					if (!added[r.resource.index()]) {
						rMap[r.resource.index()] = am;
						am++;
						added[r.resource.index()] = true;
						resources.add(new RegionResource(init, r.resource));
					}
					resources.get(rMap[r.resource.index()]).add(i, ri);
				}
			}
			this.res = resources;
		}
		
		new RegionFactorImp(init.tmpLoyalty, DicRes.¤¤Taxes, "") {
			@Override
			public double getD(Region r) {
				return 1 - 0.05*total_all.get(r);
			}

			@Override
			public double next(Region r) {
				return getD(r);
			}
		};
		
		for (RegionResource rr : res) {
			map[rr.resource.index()] = rr;
		}
		
		
	}
	
	private LIST<RegionIndustry> make(LIST<? extends INDUSTRY_HASER> rooms, RegionInit init, INT_OE<Region> total){
		int am = 0;
		for (INDUSTRY_HASER r : rooms) {
			for (@SuppressWarnings("unused") Industry i : r.industries())
				am++;
		}
		ArrayList<RegionIndustry> res = new ArrayList<>(am);
		for (INDUSTRY_HASER r : rooms) {
			for (Industry i : r.industries())
				res.add(new RegionIndustry(init, i, total));
		}
		return res;
	}
	
	public RegionResource map(RESOURCE res) {
		return map[res.index()];
	}
	
	@Override
	void remove(Region r, FRegions old) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void add(Region r, FRegions newR) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void update(Region r, double ds) {
		if (exhausting.get(r) == 1) {
			exhaustion.inc(r, 4);
			if (exhaustion.isMax(r)) {
				exhausting.set(r, 0);
			}	
		}else {
			exhaustion.inc(r, -1);
		}
		
		for (RegionResource rr : res) {
			double t = 0;
			{
				int am = 0;
				for (int ii = 0; ii < rr.industries.size(); ii++) {
					RegionIndustryResource i = rr.industries.get(ii);
					am += i.industry.factors.getD(r)*i.resource().rate;
				}
				t = Math.ceil(am);
			}
			int n = rr.current_output.get(r);
			if (t < n) {
				rr.current_output.set(r, (int) t);
			}else if (n < t){
				double d = t-n;
				d /= 8;
				if (d < 100)
					d = 100;
				n += d;
				if (n > t)
					n = (int) t;
				rr.current_output.set(r, (int)n);
			}
			
			
		}
		
	}

	@Override
	void generateInit(Region r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	SAVABLE saver() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static final RegionFactor baseFactor = new RegionFactor() {
		
		@Override
		public INFO info() {
			return REGIOND.POP().total.info();
		}
		
		@Override
		public double getD(Region r) {
			return Math.ceil(RES.config().WORLD.TRIBUTE*REGIOND.POP().total.get(r)/18.0);
		}
		
		@Override
		public double next(Region r) {
			return getD(r);
		}
	};
	
	public final class RegionIndustry {
		
		public final Industry industry;
		final INT_OE<Region> total;
		
		public final INT_OE<Region> prospect;
		
		public final RegionFactors factors;
		public final RegionDecree decree;
		public final RegionFactor raceWorkskill;
		
		RegionIndustry(RegionInit init, Industry industry, INT_OE<Region> total){
			this.industry = industry;
			factors = new RegionFactors(industry.blue.info);
			
			this.total = total;
			this.prospect = init.count.new DataNibble(¤¤Prospect, ¤¤ProspectD);
			
			new RegionFactorImp(factors, this.prospect.info()) {
				
				@Override
				public double getD(Region r) {
					return prospect.getD(r);
				}

				@Override
				public double next(Region r) {
					return getD(r);
				}
			};
			
			new RegionFactorImp(factors, ¤¤Exhaust, ¤¤ExhaustD) {
				@Override
				public double getD(Region r) {
					if (exhausting.get(r) == 1 && !exhaustion.isMax(r))
						return 2;
					return 1;
				}
				
				@Override
				public double next(Region r) {
					return getD(r);
				}
			};
			new RegionFactorImp(factors, ¤¤Exhausted, ¤¤ExhaustedD) {
				@Override
				public double getD(Region r) {
					if (exhaustion.isMax(r) || (exhausting.get(r) == 0 && exhaustion.get(r) > 0))
						return 0;
					return 1;
				}
				
				@Override
				public double next(Region r) {
					return getD(r);
				}
			};
			
			
			
			
			decree = new RegionDecreeImp(init.count.new DataNibble(10), 1, DicRes.¤¤Taxes, "") {
				@Override
				public void set(Region t, int i) {
					if (total != null)
						total.inc(t, -get(t));
					super.set(t, i);
					if (total != null)
						total.inc(t, get(t));
				}
			};
			decree.connect(factors, 0, 0.05);
			factors.addFactor(baseFactor);
			
			
			raceWorkskill = new RegionFactorImp(factors, RACES.name(), "") {
				
				@Override
				public double getD(Region r) {
					double res = 0;
					double pop = 0;
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						double p = REGIOND.RACE(race).population.getD(r);
						double rres = industry.bonus().race(race); 
						res += rres*p;
						pop += p;
					}
					if (pop == 0)
						return 0;
					res /= pop;
					return res;
				}
				
				@Override
				public double next(Region r) {
					double res = 0;
					double pop = 0;
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						double p = REGIOND.RACE(race).population_target.getD(r);
						double rres = industry.bonus().race(race);
						res += rres*p;
						pop += p;
					}
					if (pop == 0)
						return 0;
					res /= pop;
					return res;
				}
			};
		}
		
	}
	
	public static final class RegionIndustryResource {
		
		public final RegionIndustry industry;
		private final int oi;
		
		RegionIndustryResource(RegionIndustry industry, int oi) {
			this.industry = industry;
			this.oi = oi;
		}
		
		public IndustryResource resource() {
			return industry.industry.outs().get(oi);
		}
		
	}
	
	public static final class RegionResource {
		
		public final RESOURCE resource;
		
		public final DOUBLE_O<Region> output_target;
		public final INT_OE<Region> current_output;
		
		private LIST<RegionIndustryResource> industries = new ArrayList<>(0);
		
		private void add(RegionIndustry indu, int oI) {
			industries = industries.join(new RegionIndustryResource(indu, oI));
		}

		RegionResource(RegionInit init, RESOURCE res){
			this.resource = res;
			output_target = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region r) {
					int am = 0;
					for (int ii = 0; ii < industries.size(); ii++) {
						RegionIndustryResource i = industries.get(ii);
						am += i.industry.factors.next(r)*i.resource().rate;
					}
					return Math.ceil(am);
				}
				
				
			};
			current_output = init.count. new DataShort();
			
			
		}
		
		public LIST<RegionIndustryResource> industries() {
			return industries;
		}
		

		public int maxOutput(Region r) {
			

			
			double res = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race race = RACES.all().get(ri);
				double p = REGIOND.RACE(race).population.get(r);
				double rres = 0;
				for (int ii = 0; ii < industries.size(); ii++) {
					
					RegionIndustryResource i = industries.get(ii);
					double ires = i.industry.industry.bonus().race(race);
					
					ires *= i.industry.prospect.getD(r);
					ires *= i.resource().rate;
					rres = Math.max(rres, ires);
				}
				res += rres*p;
			}
			
			return (int) Math.ceil(res*0.5);
		}
		
	}
	



	
	
}
