package world.regions.data.building;

import game.boosting.*;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomsJson;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import util.info.GFORMAT;
import world.regions.Region;
import world.regions.data.RBooster;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;
import world.regions.data.pop.RDRace;

class RDBuildingGeneration {
	
	private final KeyMap<LISTE<Gen>> gens = new KeyMap<>();

	public void generate(LISTE<RDBuilding> all, RDInit init, ResFolder p, String folder, RDBuildingCat cat) {
		if (gens.get(folder) != null)
			for (Gen g : gens.get(folder))
				g.generate(all, init, p, cat);
		
	}
	
	
	public RDBuildingGeneration() {
		
		{
			ArrayListGrower<RoomBlueprintImp> all = new ArrayListGrower<>();
			for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().FARMS).join(SETT.ROOMS().ORCHARDS)) {
				if (b.constructor().mustBeIndoors())
					all.add(b);
			}
			
			new GenIndustry("agriculture", "_GENERATE_INDOORS", all) {
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					connectRan(bu, blue);
					mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
					
					super.connect(bu, blue, local, global);
				}
			};
			all.clear();
			for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().FARMS).join(SETT.ROOMS().ORCHARDS)) {
				if (!b.constructor().mustBeIndoors())
					all.add(b);
			}
			new GenIndustry("agriculture", "_GENERATE_OUTDOORS", all){
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					connectRan(bu, blue);
					mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
					double mi = 0.1;
					double ma = 2.0;
					if (blue instanceof ROOM_ORCHARD) {
						mi = 0.5;
						ma = 1.5;
					}
						
					BoostSpec bo = Efficiencies.FERTILITY(mi, ma, true).add(bu.efficiency);
					
					bu.baseFactors.add(bo);
					super.connect(bu, blue, local, global);
				}
			};
		}
		{
			ArrayListGrower<RoomBlueprintImp> all = new ArrayListGrower<>();
			for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().PASTURES).join(SETT.ROOMS().FISHERIES)) {
				if (b.constructor().mustBeIndoors())
					all.add(b);
			}
			
			new GenIndustry("pasture", "_GENERATE_INDOORS", all) {
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					connectRan(bu, blue);
					mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
					if (blue instanceof ROOM_FISHERY) {
						
						
						BoostSpec bo = Efficiencies.WATER(0.1, 2.0, true).add(bu.efficiency);
						bu.baseFactors.add(bo);
						
					}
					super.connect(bu, blue, local, global);
				}
			};
			all.clear();
			for (RoomBlueprintImp b : new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().PASTURES).join(SETT.ROOMS().FISHERIES)) {
				if (!b.constructor().mustBeIndoors())
					all.add(b);
			}
			new GenIndustry("pasture", "_GENERATE_OUTDOORS", all){
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					connectRan(bu, blue);
					mimic(bu, ((INDUSTRY_HASER) blue).industries().get(0).bonus());
					if (blue instanceof ROOM_FISHERY) {
						BoostSpec bo = Efficiencies.WATER(0.1, 2.0, true).add(bu.efficiency);
						bu.baseFactors.add(bo);
						
					}else {
						BoostSpec bo = Efficiencies.FERTILITY(0.75, 1.25, true).add(bu.efficiency);
						bu.baseFactors.add(bo);
					}
					super.connect(bu, blue, local, global);
				}
			};
			
			
			
		}
		
		{
			new GenIndustry("mine", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().MINES)) {
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					ROOM_MINE f = (ROOM_MINE) blue;
					BoostSpec bo = Efficiencies.MINABLE(f.minable, 0, 1, true).add(bu.efficiency);

					bu.baseFactors.add(bo);
					
					super.connect(bu, blue, local, global);
				}
			};
		}
		
//		{
//			
//			LinkedList<RoomBlueprintImp> li = new LinkedList<RoomBlueprintImp>();
//			for (ROOM_NURSERY n : SETT.ROOMS().NURSERIES) {
//				if (RD.RACE(n.race) != null)
//					li.add(n);
//			}
//			
//			new Gen("growth", "_GENERATE", li) {
//				
//				@Override
//				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
//					ROOM_NURSERY t = (ROOM_NURSERY) blue;
//					consume(bu, local, RD.RACE(t.race).pop.dtarget, false, false);
//					consume(bu, global, RD.RACE(t.race).pop.dtarget, false, true);
//					consume(bu, local, 0.5, RD.RACES().capacity, false, false);
//				}
//			};
//		}
		
		{
			new Gen("religion", "_GENERATE", new LinkedList<RoomBlueprintImp>().join(SETT.ROOMS().TEMPLES.ALL)) {
				
				@Override
				void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
					ROOM_TEMPLE t = (ROOM_TEMPLE) blue;
					consume(bu, local, t.religion.boostable, false, false);
					consume(bu, global, t.religion.boostable, false, true);
				}
			};
		}
		
		
	}
	
	private abstract class Gen {
		
		public final String file;
		private final LIST<RoomBlueprintImp> rooms;
		private final ArrayListGrower<Tuple<RDBuilding, RoomBlueprintImp>> tmps = new ArrayListGrower<>();
		
		Gen(String folder, String file, LIST<RoomBlueprintImp> rooms){
			this.file = file;
			this.rooms = new ArrayList<>(rooms);
			if (!gens.containsKey(folder))
				gens.put(folder, new ArrayListGrower<>());
			gens.get(folder).add(this);

		}
		
		void generate(LISTE<RDBuilding> all, RDInit init, ResFolder p, RDBuildingCat cat) {
			
			if (!p.init.exists(file))
				return;
			
			Json json = new Json(p.init.get(file));
			
			boolean aibuild = json.bool("AI_BUILDS", true);
			double[] local = json.ds("LOCAL_LEVELS");
			double[] global = json.ds("GLOBAL_LEVELS");
			Json[] levels = json.jsons("LEVELS");
			LIST<RoomBlueprintImp> omitt = RoomsJson.list("OMITT_ROOMS", json);
			String order = "";
			if (json.has("ORDER"))
				order = json.value("ORDER");
			for (RoomBlueprintImp room : rooms) {
				if (omitt.contains(room))
					continue;
				RDBuilding bu = generate(all, init, levels, cat, room, aibuild, order);
				tmps.add(new Tuple.TupleImp<>(bu, room));
			}
			
			init.connectable.add(new ACTION() {
				
				@Override
				public void exe() {
					for (Tuple<RDBuilding, RoomBlueprintImp> t : tmps) {
						Gen.this.connect(t.a(), t.b(), local, global);
					}
				}
			});
			
		}
		
		RDBuilding generate(LISTE<RDBuilding> all, RDInit init, Json[] jlevels, RDBuildingCat cat, RoomBlueprintImp blue, boolean aiBuild, String order) {
			

			ArrayListGrower<RDBuildingLevel> levels = new ArrayListGrower<>();
			
			for (int i = 0; i < jlevels.length; i++) {
				String n = blue.info.name + ": " + GFORMAT.toNumeral(new Str(4), i+1);
				Lockable<Region> needs = GVALUES.REGION.LOCK.push("BUILDING_" + blue.key + "_"+(i+1), n, blue.info.desc, blue.iconBig());

				RDBuildingLevel b = new RDBuildingLevel(n, blue.iconBig(), needs);
				levels.add(b);
				
			}
			
			RDBuilding bu = new RDBuilding(all, init, cat, blue.key, blue.info, levels, aiBuild, false, order);
			
			for (int i = 0; i < jlevels.length; i++) {
				RDBuildingLevel l = bu.levels.get(i+1);
				Json j = jlevels[i];
				l.local.push("BOOST", j, RDBuildingCat.lValue);
				l.global.push("BOOST_GLOBAL", j, RDBuildingCat.lGlobal, DicMisc.¤¤global, false);
				
				
				
				l.cost = j.i("CREDITS", 0, 1000000, 0);
			}

			
			
			return bu;
		}
		
		
		abstract void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global);
		
		void consume(RDBuilding bu, double[] values, Boostable bo, boolean mul, boolean global) {
			consume(bu, values, 1.0, bo, mul, global);
		}
		
		protected void consume(RDBuilding bu, double[] values, double dv, Boostable bo, boolean mul, boolean global) {
			
			for (int i = 0; i < values.length; i++) {
				RDBuildingLevel b = bu.levels.get(i+1);
				double v = values[i]*dv;
				if (v == 0 && !mul)
					continue;
				if (v == 1 && mul)
					continue;
				b.local.push(new LBoost(bu, v, mul), bo);
			}
		}
		
		protected void connectConsume(RDBuilding bu, double endPoint, Boostable bo) {
			double delta = (1-endPoint)/(bu.levels.size()-1);
			for (int i = 1; i < bu.levels.size(); i++) {
				RDBuildingLevel b = bu.levels.get(i);
				int am = (int) Math.round(100*delta);
				if (i == bu.levels.size()-1)
					am = (int) Math.ceil(100*delta);
				if (am != 0) {
					b.local.push(new LBoost(bu, am/100.0, false), bo);
					
				}
					
			}
		}
		
	}
	
	private class GenIndustry extends Gen {

		GenIndustry(String folder, String file, LIST<RoomBlueprintImp> rooms) {
			super(folder, file, rooms);
			
		}
		
		@Override
		void connect(RDBuilding bu, RoomBlueprintImp blue, double[] local, double[] global) {
			
			INDUSTRY_HASER h = (INDUSTRY_HASER) blue;
			
			
			for (IndustryResource r : h.industries().get(0).outs()) {
				consume(bu, local, r.rate, RD.OUTPUT().get(r.resource).boost, false, false);
				consume(bu, global, r.rate, RD.OUTPUT().get(r.resource).boost, false, true);
			}
			
			
			
			for (RDRace r : RD.RACES().all) {
				double wb = r.race.pref().getWork(blue.employment())-0.5;
				if (wb != 0) {
					
					connectConsume(bu, wb, r.loyalty.target);
				}
			}
		}
		
		
	}
	
	protected static void connectRan(RDBuilding bu, RoomBlueprintImp ins) {
		BoostSpec bo = new RBooster(new BSourceInfo(DicMisc.¤¤Prospect, ins.icon), 0.6, 1.5, true) {
			
			@Override
			public double get(Region t) {
				return RD.RAN().get(t, ins.index()*2, 2)/3.0;
			};
			
		}.add(bu.efficiency);
		bu.baseFactors.add(bo);
	}
	
	protected static void mimic(RDBuilding bu, Boostable bo) {
		
		for (CLIMATE c : CLIMATES.ALL()) {
			for (int si = 0; si < c.boosters.all().size(); si++) {
				BoostSpec s = c.boosters.all().get(si);
				if (s.boostable == bo) {
					BoostSpec sp = CLIMATES.pushIfDoesntExist(c, s.booster.to(), bu.efficiency, s.booster.isMul);
					if (sp != null && !bu.baseFactors.contains(sp))
						bu.baseFactors.add(sp);
				}
			}
		}
		
		for (RDRace c : RD.RACES().all) {
			for (int si = 0; si < c.race.boosts.all().size(); si++) {
				BoostSpec s = c.race.boosts.all().get(si);
				if (s.boostable == bo) {
					BoostSpec sp = RACES.boosts().pushIfDoesntExist(c.race, s.booster.to(), bu.efficiency, s.booster.isMul);
					if (sp != null && !bu.baseFactors.contains(sp))
						bu.baseFactors.add(sp);
				}
			}
		}
		
	}
	
	private static class LBoost extends BoosterImp{

		private final RDBuilding bu;
		
		public LBoost(RDBuilding bu, double value, boolean isMul) {
			super(new BSourceInfo(bu.info.name, bu.levels.get(1).icon), value, isMul);
			this.bu = bu;
		}

		@Override
		public double vGet(Region reg) {
			if (isPositive(1.0))
				return getValue(bu.efficiency.get(reg));
			else {
				return getValue(1.0);
			}
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b == Region.class;
		};

		@Override
		public double vGet(Faction f) {
			return 0;
		}
		
	}

	
}
