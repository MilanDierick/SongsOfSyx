package world.regions.data.pop;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.values.GVALUES;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.entity.ENTETIES;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DoubleOCached;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicGeo;
import util.dic.DicMisc;
import util.info.INFO;
import view.main.VIEW;
import world.WConfig;
import world.WORLD;
import world.regions.Region;
import world.regions.WREGIONS;
import world.regions.data.*;
import world.regions.data.RD.RDInit;
import world.regions.data.RData.RDataE;
import world.regions.data.pop.RDRace.RDRaceEdict;
import world.regions.data.pop.RDRace.RDRacePopulation;

public class RDRaces {
	

	private static CharSequence ¤¤workforceD = "workforce is needed as employees in certain buildings.";

	
	static CharSequence ¤¤Loyalty = "¤Loyalty";
	private static CharSequence ¤¤LoyaltyD = "¤Current Loyalty. Loyalty determines the chance of rebellion. Loyalty changes slowly based on the target. Increase loyalty by allocating admin points into loyalty boosting areas. Loyalty will also increase the longer a region has belonged to you. Loyalty is species specific.";
	private static CharSequence ¤¤RegionCapacity = "¤Region Capacity";
	private static CharSequence ¤¤RegionCapacityD = "¤Region Population capacity.";
	{
		D.ts(RDRaces.class);
	}
	
	private final RDRace[] map = new RDRace[RACES.all().size()];
	public final LIST<RDRace> all;
	public final Boostable capacity;
	public final Boostable workforce;
	final RDataE pop;
	public final RData population;
	public final Visuals visuals;
	
	private double maxPop = 20000;
	private double maxFerArea = 250*1.0;
	private double maxPopI = 1.0/maxPop;
	
	public final DoubleOCached<Region> popTarget = new DoubleOCached<Region>() {

		@Override
		public double getValue(Region t) {
			double cache = 0;
			for (int ri = 0; ri < all.size(); ri++) {
				cache += all.get(ri).pop.target(t);
			}
			return cache;
		}
	};
	
	public final DOUBLE_O<Region> loyaltyAll = new DOUBLE_O<Region>(){

		final INFO info = new INFO(¤¤Loyalty, ¤¤LoyaltyD);
		
		@Override
		public double getD(Region t) {
			double d = 0;
			for (RDRace r : all) {
				d += r.pop.get(t)*r.loyalty.getD(t);
			}
			if (population.get(t) > 0)
				d /= population.get(t);
			return d;
		}
		
		@Override
		public INFO info() {
			return info;
		};
		
		
	};

	public RDRaces(RDInit init){
		
		workforce = BOOSTING.push("WORKFORCE", 0, DicMisc.¤¤Workforce, ¤¤workforceD, UI.icons().s.pickaxe, BoostableCat.WORLD);
		init.deficiencies.register(workforce);
		init.connectable.add(new ACTION() {
			@Override
			public void exe() {
//				new RBooster(new BSourceInfo(DicMisc.¤¤Population, UI.icons().s.human), 0, 100000, false) {
//
//					@Override
//					public double get(Boostable bo, BOOSTABLE_O o) {
//						return o.boostableValue(bo, this);
//					}
//
//					@Override
//					public double get(Region reg) {
//						return Math.ceil(0.058*population.get(reg));
//					};
//					
//					
//					
//				}.add(workforce);
			}
		});
		
		capacity = BOOSTING.push("POPULATION_CAPACITY", 1, ¤¤RegionCapacity, ¤¤RegionCapacityD, UI.icons().s.human, BoostableCat.WORLD, 1.0);
		init.connectable.add(new ACTION() {
			@Override
			public void exe() {
//				new RBooster(new BSourceInfo(DicMisc.¤¤Base, UI.icons().s.cancel), 0, WConfig.data.TILE_POPULATION, false) {
//
//
//					@Override
//					public double get(Region reg) {
//						return 1.0;
//					};
//
//					
//				}.add(capacity);
				new RBooster(new BSourceInfo(DicMisc.¤¤Area, UI.icons().s.expand), 0, WConfig.data.REGION_SIZE, true) {

					@Override
					public double get(Region t) {
						return (double)t.info.area()/WConfig.data.REGION_SIZE;
					}

					
				}.add(capacity);
				new RBooster(new BSourceInfo(DicMisc.¤¤Fertility, UI.icons().s.sprout), 0.2, 1, true) {

					@Override
					public double get(Region t) {
						return t.info.fertility();
					}

					
					
				}.add(capacity);
			}
		});
		
		pop = new RDataE(init.count.new DataInt(), init, DicMisc.¤¤Population);
		population = pop;
		GVALUES.REGION.pushI("POPULATION", DicMisc.¤¤Population, pop);
		GVALUES.REGION.pushI("POPULATION_KINGDOM", DicMisc.¤¤Population + ": " + DicGeo.¤¤Realm, new INT_O<Region>() {

			@Override
			public int get(Region t) {
				if (t.faction() == null)
					return 0;
				return pop.faction().get(t.faction());
			}

			@Override
			public int min(Region t) {
				return 0;
			}

			@Override
			public int max(Region t) {
				return Integer.MAX_VALUE;
			}
			
		});
		
		
		ArrayList<RDRace> all = new ArrayList<RDRace>(RACES.playable().size());
	
		for (Race r : RACES.playable()) {
			RDRace rr = new RDRace(r, init, all.size());
			map[r.index()] = rr;
			all.add(map[r.index()]);
			
			
		}
		
		this.all = all;
		visuals = new Visuals(init);
		ACTION bp = new ACTION() {
			@Override
			public void exe() {
				
				double aveArea = 0;
				int am = 0;
				for (int i = 0; i < WREGIONS.MAX; i++) {
					Region r = WORLD.REGIONS().getByIndex(i);
					if (r.info.area() > 0) {
						aveArea += r.info.area()*(0.1+0.9*r.info.fertility());
						am++;
					}
				}
				aveArea/= am;
				maxPop = aveArea*capacity.max(Region.class)/WConfig.data.REGION_SIZE;

				maxFerArea = aveArea;
				maxPopI = 1.0/(maxPop*0.5);
			}
		};
		
		init.gens.add(new ACTION_O<Region>() {
			
			@Override
			public void exe(Region r) {
				bp.exe();
				for (RDRace rr : all)
					rr.pop.set(r, (int) rr.pop.target(r));
			}
		});
		
		init.beforePlay.add(bp);
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player()) {
					for (RDRace r : all) {
						for (RDRaceEdict e : r.edicts)
							e.toggled.set(reg, 0);
					}
				}
			}
		};
	}
	
	public void initPopulation(Region reg) {
		RDRacePopulation.clearCaache();
		
		for (RDRace r : all) {
			r.pop.set(reg, (int) Math.round(r.pop.target(reg)));
		}

	}
	
	public RDRace get(Race race) {
		return map[race.index];
	}

	public double maxPop() {
		return maxPop;
	}
	
	public double popSize(Region reg) {
		if (reg.capitol()) {
			return population.get(reg)/(double)ENTETIES.MAX;
		}
		return CLAMP.d(population.get(reg)*maxPopI, 0, 1);
	}
	
	double capacity(Region reg) {
		
		if (reg.faction() instanceof FactionNPC) {
			
			double fa = maxPop*reg.info.area()*reg.info.fertility()/maxFerArea;
			double min = fa*0.1;
			double max = fa;

			FactionNPC f = (FactionNPC) reg.faction();
			
			
			double empireSize = f.realm().ferArea()/(10*maxFerArea);
			empireSize = CLAMP.d(empireSize, 0, 1);
			
			double competence = (0.25 + 0.75*f.court().king().size());

			if (reg.capitol()) {
				return min +(ENTETIES.MAX-min)*competence*empireSize;
			}
			
			return min +(max-min)*competence*Math.pow(empireSize, 0.5);
		}
		return capacity.get(reg);
		
		
	}

	
	public final class Visuals {
		
		private final INT_OE<Region> cRace;
		private final INT_OE<Region> cacheI;
		private final ArrayList<INT_OE<Region>> vVill = new ArrayList<INT_OE<Region>>(16);
		
		private Visuals(RDInit init) {
			if (all.size() > 255)
				throw new RuntimeException("too many races");
			cRace = init.count.new DataByte();
			cacheI = init.count.new DataNibble();
			while(vVill.hasRoom())
				vVill.add(init.count.new DataByte());
		}
		
		public Race cRace(Region reg) {
			cache(reg);
			return all.get(cRace.get(reg)).race;
		}
		
		public Race vRace(Region reg, int ran) {
			cache(reg);
			ran &= 0x0F;
			return all.get(vVill.get(ran).get(reg)).race;
		}
		
		private void cache(Region reg) {
			int ri = (0x0F-((VIEW.RI()>>6)&0x0F));
			if (cacheI.get(reg) == ri)
				return;
			cacheI.set(reg, ri);
			RDRace biggest = null;
			int bb = -1;
			int vi = 0;
			for (int rri = 0; rri < all.size(); rri++) {
				RDRace r = all.get(rri);
				if (r.pop.get(reg) > bb) {
					biggest = r;
					bb = r.pop.get(reg);
				}
				if (population.get(reg) > 0) {
					int vam = 16*r.pop.get(reg)/population.get(reg);
					for (int i = 0; i < vam && vi < 16; i++) {
						vVill.get(vi++).set(reg, r.index());
					}
				}
			}

			cRace.set(reg, biggest.index());
			
			for (; vi < 16; vi++) {
				vVill.get(vi).set(reg, biggest.index());
			}
			
		}
		
	}
	
}