package world.regions.data.pop;

import java.io.IOException;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import game.values.GVALUES;
import init.D;
import init.biomes.CLIMATES;
import init.biomes.TERRAINS;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.data.INT_O.INT_OE;
import util.dic.DicMisc;
import util.info.INFO;
import util.keymap.ResColl.RCAction;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.regions.Region;
import world.regions.data.RBooster;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.RData.RDataE;
import world.regions.data.building.RDBuilding;
import world.regions.data.building.RDBuildingLevel;

public class RDRace implements INDEXED{
	
	private static CharSequence ¤¤PopulationTarget = "Pop. Target";
	private static CharSequence ¤¤RulingSpecies = "¤Ruling Species";

	private static CharSequence ¤¤Prosecute = "¤Prosecution";
	private static CharSequence ¤¤ProsecuteD = "¤Prosecuting a species, severely diminishes growth and decreases happiness.";
	
	private static CharSequence ¤¤Exile = "¤Exile";
	private static CharSequence ¤¤ExileD = "¤Forbid this species from immigrating and sends off any citizens to neighbouring regions where they are still welcome.";
	
	
	private static CharSequence ¤¤Massacre = "¤Massacre";
	private static CharSequence ¤¤MassacreD = "¤Commit genocide and instantly rid yourself of this species. Will cause an outrage of course, make sure you have enough military presence to handle an eventual uprising.";
	
	private static CharSequence ¤¤Biome = "¤Species Biome";
	private static CharSequence ¤¤Distant = "¤Distant";

	private static CharSequence ¤¤Armies = "¤Army presence";
	static {
		D.ts(RDRace.class);
	}
	
	public final Race race;
	public final RDRaceEdict sanction;
	public final RDRaceEdict exile;
	public final RDRaceEdict massacre;
	public final LIST<RDRaceEdict> edicts;
	
	public final RDRacePopulation pop;
	public final RDRaceLoyalty loyalty;

	private int index;
	
	public final RDNames names;
	
	RDRace(Race race, RDInit init, int index){
		this.race = race;
		
		this.index = index;
		double maxPop = 0.01;
		double growth = 0.01;
		
		names = new RDNames(race, init);
		
		
		
		maxPop = race.population().max;
		growth = race.population().growth;


		
		pop = new RDRacePopulation(init, this, maxPop, growth);
		loyalty = new RDRaceLoyalty(init, this);
		
		sanction = new RDRaceEdict(init, new INFO(¤¤Prosecute, ¤¤ProsecuteD), UI.icons().m.descrimination, this, 0.25, 0.5);
		exile = new RDRaceEdict(init, new INFO(¤¤Exile, ¤¤ExileD), UI.icons().m.exit, this, 1.0, 0.6);
		massacre = new RDRaceEdict(init, new INFO(¤¤Massacre, ¤¤MassacreD), UI.icons().m.skull, this, 1.0, 1.0);
		this.edicts = new ArrayList<RDRace.RDRaceEdict>(sanction,exile,massacre);

		if (race.pref().worldBuildingOverride != null) {
			init.beforeConnect.add(new ACTION() {
				
				@Override
				public void exe() {
					RD.BUILDINGS().MAP.process("BUILDING_LOYALTY_OVERRIDE", race.pref().worldBuildingOverride, new RCAction<RDBuilding>() {
						
						@Override
						public void doWithJson(RDBuilding t, Json json, String key) {
							double v = json.d(key);
							for (int i = 0; i < t.levels.size(); i++) {
								RDBuildingLevel l = t.levels.get(i+1);
								for (int bi = 0; bi < l.local.all().size(); bi++) {
									BoostSpec sp = l.local.all().get(bi);
									
									if (sp.boostable == loyalty.target) {
										replace(l.local, bi, sp, v);
									}else if (sp.boostable == pop.dtarget) {
										replace(l.local, bi, sp, v);
									}else if (sp.boostable == pop.growth) {
										replace(l.local, bi, sp, v);
									}
									
								}
							}
						}
						
						void replace(BoostSpecs l, int i, BoostSpec sp, double value) {
							
							double from = sp.booster.from();
							double to = sp.booster.to();
							if (sp.booster.isMul) {
								from = (from-1)*value + 1;
								to = (to-1)*value + 1;
							}else {
								from*= value;
								to *= value;
							}
							
							RBooster nn = new RBooster(sp.booster.info, from, to, sp.booster.isMul) {
								
								@Override
								public double get(Region t) {
									return 1.0;
								}
							};
							
							l.replace(i, nn, sp.boostable);
							
						}
					});
					
					race.pref().worldBuildingOverride = null;
					
					
				}
			});
		}
		
		
		

		
		init.upers.add(new Up());
	}
	
	public double loyaltyTarget(Region reg) {
		return loyalty.get(reg) > 1 ? 1 : 0;
	}
	
	private static double dtime = 1.0/(TIME.secondsPerDay*8*16);
	
	private class Up implements RDUpdatable {
		@Override
		public void update(Region reg, double ds) {
			if (reg.faction() != null && reg.capitol()) {
				for (RDRaceEdict e : edicts) {
					int am = 0;
					for (int ri = 0; ri < reg.faction().realm().regions(); ri++) {
						Region r = reg.faction().realm().region(ri);
						am+=e.toggled.get(r);
					}
					
					if (am == 0) {
						e.realm.incFraction(reg.faction(), am*0.5*ds*TIME.secondsPerDayI*e.realm.max(null));
					}else {
						e.realm.incFraction(reg.faction(), -ds*dtime*e.realm.max(null));
					}
					
				}
			}
		}
		
		@Override
		public void init(Region reg) {
			
			if (reg.faction() == FACTIONS.player()) {
				
				for (RDRaceEdict e : edicts) {
					e.toggled.set(reg, 0);
					e.realm.setD(reg.faction(), 0);
				}
			}else if (reg.faction() != null && reg.capitol()) {
				for (RDRaceEdict e : edicts) {
					e.realm.setD(reg.faction(), 0);
					for (int ri = 0; ri < reg.faction().realm().regions(); ri++) {
						Region r = reg.faction().realm().region(ri);
						if (e.toggled.get(r) == 1) {
							e.realm.setD(reg.faction(), 1.0);
						}
					}
				}
			}
		}
	}
	

	


	@Override
	public int index() {
		return index;
	}
	
	
	public static final class RDRaceLoyalty extends RDataE implements RDUpdatable {

		public final Boostable target;

		private static final double DTime = 8.0/(TIME.secondsPerDay);
		RDRaceLoyalty(RDInit init, RDRace race) {
			super(init.count.new DataByte(), 
					init, RDRaces.¤¤Loyalty + ": " + race.race.info.names);
			target = BOOSTING.push("LOYALTY_" + race.race.key, 1.0, name, name, race.race.appearance().iconBig, BoostableCat.WORLD_CIVICS);
			BOOSTING.addToMaster("LOYALTY", target);
			init.upers.add(this);
			init.connectable.add(new ACTION() {
				
				@Override
				public void exe() {
					BoosterImp bo = new RBooster(new BSourceInfo(STATS.ENV().OTHERS.info().name, UI.icons().s.citizen), 0.75, 1.0, true) {
						@Override
						public double get(Region t) {
							double tot = RD.RACES().population.get(t);
							if (tot == 0)
								return 0;
							double rr = 0;
							for (RDRace o : RD.RACES().all) {
								rr += o.pop.get(t)*(race.race.pref().race(o.race));
							}
							return CLAMP.d(rr/tot, 0, 1);
						}
					};
					bo.add(target);

					
					

					
					new RBooster(new BSourceInfo(DicMisc.¤¤Population, UI.icons().s.human), 0, -10.0, false) {
						
						@Override
						public double get(Region t) {
							double d = (double)RD.RACES().population.get(t)/(1.0+RD.RACES().maxPop());
							d = (int)(d*100)/100.0;
							return d;
						}
					}.add(target);
					
					new RBooster(new BSourceInfo(¤¤Armies, UI.icons().s.sword), 0, 20, false) {
						@Override
						public double get(Region t) {
							double power = 0;
							for (WArmy a : WORLD.ENTITIES().armies.fill(t))
								if (a.faction() == t.faction())
									power +=  AD.power().get(a);
							return (power)/(RD.RACES().pop.get(t)+1);
						}
					}.add(target);
				}
			});
		}

		@Override
		public void update(Region reg, double time) {
			
			double d = increase(reg)*DTime*time;
			moveTo(reg, d, d < 0 ? 0 : 255);
		}
		
		public double increase(Region reg) {
			return (int)(target.get(reg)*10)/10.0;
		}

		@Override
		public void init(Region reg) {
			double d = target.get(reg);
			set(reg, d < 0 ? 0 : 255);
		}
		
	}
	
	public static final class RDRacePopulation extends RDataE implements RDUpdatable {

		public final double maxPopulation;
		public final double growthBase;
		public final Boostable dtarget;
//		private Boostable<Region> targetBase;
		public final Boostable growth;
		private static final double DTime = 5000.0/(TIME.secondsPerDay*16);
		private final BoosterImp biome;
		
		RDRacePopulation(RDInit init, RDRace race, double max, double growthBase) {
			super(init.count.new DataShortE() {
				
				@Override
				public void set(Region t, int s) {
					RD.RACES().pop.set(t, RD.RACES().population.get(t)-get(t));
					super.set(t, s);
					RD.RACES().pop.set(t, RD.RACES().population.get(t)+get(t));
				}
				
			}, init, DicMisc.¤¤Population + ": " + race.race.info.names);
			init.upers.add(this);
			maxPopulation = max;
			this.growthBase = growthBase;
			dtarget = BOOSTING.push("POPULATION_TARGET_" + race.race.key, 1, ¤¤PopulationTarget + ": " + race.race.info.names, race.race.info.names, race.race.appearance().iconBig, BoostableCat.WORLD_CIVICS);
			growth = BOOSTING.push("POPULATION_GROWTH_" + race.race.key, 1, DicMisc.¤¤Growth + ": " + race.race.info.names, race.race.info.names, race.race.appearance().iconBig, BoostableCat.WORLD_CIVICS);
			BOOSTING.addToMaster("POPULATION_TARGET", dtarget);
			BOOSTING.addToMaster("POPULATION_GROWTH", dtarget);
			biome = new RBooster(new BSourceInfo(¤¤Biome, UI.icons().s.temperature), 0.1, 2, true) {
				@Override
				public double get(Region reg) {
					
					double c = 0;
					for (int i = 0; i < CLIMATES.ALL().size(); i++)
						c += reg.info.climate(CLIMATES.ALL().get(i))*race.race.population().climate(CLIMATES.ALL().get(i));
					
					double t = 0;
					for (int i = 0; i < TERRAINS.ALL().size(); i++)
						t += reg.info.terrain(TERRAINS.ALL().get(i))*race.race.population().terrain(TERRAINS.ALL().get(i));
					return c*t;
				}
			};
			biome.add(dtarget);
			init.connectable.add(new ACTION() {
				
				@Override
				public void exe() {
					
					
					new RBooster(new BSourceInfo(¤¤RulingSpecies, UI.icons().s.crown), 1, 1.2, true) {
						@Override
						public double get(Region t) {
							if (t.faction() != null && t.faction().race() == race.race)
								return 1;
							return 0;
						}
					}.add(dtarget);	
					
					new RBooster(new BSourceInfo(DicMisc.¤¤Base, UI.icons().s.cancel), 0, 1, true) {
						@Override
						public double get(Region t) {
							return growthBase;
						}
					}.add(growth);
					
					new RBooster(new BSourceInfo(race.loyalty.name, UI.icons().s.happy), 0, 10, true) {
						@Override
						public double get(Region t) {
							if (t.faction() == FACTIONS.player())
								return CLAMP.d(race.loyalty.getD(t), 0, 10)/10.0;
							return 1;
						}
					}.add(growth);
				}
			});
			
			GVALUES.REGION.pushI("POPULATION_RACE_" + race.race.key, DicMisc.¤¤Population + ": " + race.race.info.names, this);
		}

		@Override
		public void update(Region reg, double time) {
			int t = target(reg);
			double d = growth(reg)*time*DTime;
			moveTo(reg, d, t);
		}
		

		@Override
		public void init(Region reg) {
			set(reg, target(reg));
		}
		
		public int target(Region reg) {
			double d = dtarget(reg);
			d *= RD.RACES().capacity(reg);
			d *= maxPopulation;
			return (int)Math.round(d);
		}
		
		public double dtarget(Region reg) {
			double d = dtarget.get(reg);
			double tot = totdTarget.getD(reg);
			if (tot > 0)
				d /= tot;
			return Math.round(d*100)/100.0;
		}
		
		public double growth(Region reg) {
			double n = get(reg);
			int t = target(reg);
			if (t == n)
				return 0;
			if (t < n) {
				double d = (t-n)/n;
				return d;
			}else {
				return growth.get(reg);
			}
		}
		
		public double base(Region reg) {
			return biome.get(dtarget, reg);
		}
		
		private static int upI = -1;
		static void clearCaache() {
			upI = -1;
		}
		
		private static final DOUBLE_O<Region> totdTarget = new DOUBLE_O<Region>() {

			
			private Region upR = null;
			private double cache;
			
			@Override
			public double getD(Region t) {
				if (upI != GAME.updateI() || upR != t) {
					upI = GAME.updateI();
					upR = t;
					cache = 0;
					for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
						cache += RD.RACES().all.get(ri).pop.dtarget.get(t);
					}
				}
				return cache;
			}
		};

		
	}
	
	

	
	public static final class RDRaceEdict {
		
		public final INT_OE<Region> toggled;
		public final INT_OE<Faction> realm;
		public final INFO info;
		public final SPRITE icon;
		public final BoostSpecs boosts;
		
		private RDRaceEdict(RDInit init, INFO info, SPRITE icon, RDRace r, double loyalty, double growth) {
			this.info = info;
			toggled = init.count.new DataBit();
			realm = init.rCount.new DataByte();
			boosts = new BoostSpecs(info.name, icon, true);
			
			boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-loyalty, true) {

				@Override
				public double get(Region t) {
					return toggled.get(t);
				}
			
			}, r.loyalty.target);
			
			boosts.push(new BoosterImp(new BSourceInfo(¤¤Distant + ": " + info.name, icon), 1, 1.0-loyalty, true) {

				@Override
				public double vGet(Region t) {
					if (t.faction() != null && realm.get(t.faction()) > 0)
						return CLAMP.d(realm.getD(t.faction())*2.0, 0, 1);
					return 0;
				}

				@Override
				public double vGet(Faction f) {
					return CLAMP.d(realm.getD(f)*2.0, 0, 1);
				}
				
				@Override
				public boolean has(Class<? extends BOOSTABLE_O> b) {
					return b == Region.class || b == Faction.class;
				}

			}, r.loyalty.target);
			
			boosts.push(new RBooster(new BSourceInfo(info.name, icon), 1, 1.0-growth, true) {

				@Override
				public double get(Region t) {
					return toggled.get(t);
				}
			
			}, r.pop.dtarget);			
			this.icon = icon;
		}
	}

	public static class RDNames {
		

		public final RDNameList intros;
		public final RDNameList fNames;
		public final RDNameList rIntro;
		public final RDNameList rNames;
		
		RDNames(Race r, RDInit init){
			intros = new RDNameList(r.info.winfo.intros);
			fNames = new RDNameList(r.info.winfo.fNames);
			rIntro = new RDNameList(r.info.winfo.rIntro);
			rNames = new RDNameList(r.info.winfo.rNames);
			
			init.savable.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					file.i(intros.i);
					file.i(fNames.i);
					file.i(rIntro.i);
					file.i(rNames.i);
					
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					intros.i = file.i();
					fNames.i = file.i();
					rIntro.i = file.i();
					rNames.i = file.i();
				}
				
				@Override
				public void clear() {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		
		
	}
	
	public static class RDNameList{
		
		private int i = 0;
		private final ArrayList<String> all;
		
		private RDNameList(String[] nn) {
			all = new ArrayList<String>(nn);
			i = RND.rInt(all.size());
		}
		
		public String next() {
			i%= all.size();
			String s = all.get(i);
			i++;
			return s;
		}
		
	}


	
}