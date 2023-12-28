package world.regions.data.building;

import java.util.Arrays;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lock;
import init.sprite.UI.UI;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEANO;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.DicGeo;
import util.dic.DicMisc;
import util.info.INFO;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;
import world.regions.data.RDDefis.RDDef;

public final class RDBuilding implements INDEXED{
	
	public final ArrayListGrower<BoostSpec> baseFactors = new ArrayListGrower<>();
	private final BoostSpecs boosters;
	private final ArrayListGrower<BBoost> bboosts = new ArrayListGrower<>();
	public final Boostable efficiency;
	public final INT_OE<Region> level;
//	public final INT_OE<Region> targetLevel;
//	public final INT_OE<Region> constructionProgress;
	public final LIST<RDBuildingLevel> levels;
	public final INFO info;
	private final int index;
	public final RDBuildingCat cat;
	final String kk;
	public final boolean AIBuild;
	public final boolean notify;
	final String order;
	private final ArrayList<INT_OE<Faction>> levelAm;
	
	RDBuilding(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, String key, INFO info, LIST<RDBuildingLevel> levels, boolean AIBuilds, boolean notify, String order) {
		this.info = info;
		this.cat = cat;
		this.AIBuild = AIBuilds;
		this.notify = notify;
		this.order = order;
		cat.all.add(this);
		index = all.add(this);
		kk = cat.key + "_" + key;
		key = "BUILDING_" +  cat.key + "_" + key;
		this.efficiency = BOOSTING.push(key, 1, info.name, info.desc, levels.get(0).icon, BoostableCat.WORLD);
		BOOSTING.addToMaster("BUILDING_" + cat.key, efficiency);
		BOOSTING.addToMaster("BUILDING", efficiency);
		
		boosters = new BoostSpecs(info.name, levels.get(0).icon, true);
		RDBuildingLevel flevel = new RDBuildingLevel(DicMisc.¤¤Clear, UI.icons().m.cancel, GVALUES.REGION.LOCK.push());
		ArrayList<RDBuildingLevel> ll = new ArrayList<>(levels.size() + 1);
		ll.add(flevel);
		ll.add(levels);
		this.levels = ll;
		level = init.count.new DataNibble(ll.size()-1) {
			@Override
			public void set(Region t, int s) {
				if (get(t) != 0 && t.faction() != null)
					levelAm.get(get(t)-1).inc(t.faction(), -1);
				super.set(t, s);
				if (get(t) != 0 && t.faction() != null)
					levelAm.get(get(t)-1).inc(t.faction(), 1);
				RD.ADMIN().change(t);
			}
		};
		
		int i = 0;
		for (RDBuildingLevel lll : this.levels) {
			lll.index = i++;
		}
		
		levelAm = new ArrayList<>(levels.size());
		while(levelAm.hasRoom()) {
			INT_OE<Faction> la = init.rCount.new DataShort();
			
			GVALUES.FACTION.pushI(key + "_LEVEL_" + (levelAm.size()+1), levels.get(levelAm.size()).name, la);
			GVALUES.REGION.pushI(key + "_KINGDOM_LEVEL_" + (levelAm.size()+1), levels.get(levelAm.size()).name, new INT_O<Region>() {

				@Override
				public int get(Region t) {
					if (t.faction() == null)
						return level.get(t);
					return la.get(t.faction());
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
			levelAm.add(la);
			
		}
		GVALUES.REGION.pushI(key + "_LEVEL", DicMisc.¤¤Level + ": "+ info.name, level);
		GVALUES.FACTION.pushI(key, info.names, new INT_O<Faction>() {

			@Override
			public int get(Faction t) {
				int am = 0;
				for (INT_OE<Faction> l : levelAm)
					am += l.get(t);
				return am;
			}

			@Override
			public int min(Faction t) {
				return 0;
			}

			@Override
			public int max(Faction t) {
				return Integer.MAX_VALUE;
			}
			
		});
		GVALUES.REGION.push(key, DicMisc.¤¤Buildings + ": " + info.names, new BOOLEANO<Region>() {

			@Override
			public boolean is(Region t) {
				return level.get(t) > 0;
			}
			
		});
		
		GVALUES.REGION.pushI(key + "_KINGDOM", DicMisc.¤¤Buildings + " ( "  + DicGeo.¤¤Realm + "): " + info.names, new INT_O<Region>() {

			@Override
			public int get(Region t) {
				if (t.faction() == null)
					return level.get(t) > 0 ? 1 : 0;
				int am = 0;
				for (INT_OE<Faction> l : levelAm)
					am += l.get(t.faction());
				return am;
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
		
	}

	public SPRITE icon() {
		return levels.get(1).icon;
	}
	
	protected void connect(RDInit init) {
		
		
		KeyMap<BBoost> map = new KeyMap<>();
		KeyMap<Integer> defmap = new KeyMap<>();
		
		for (RDBuildingLevel l : levels) {

			for (int bi = 0; bi < l.local.all().size(); bi++) {
				BoostSpec lb = l.local.all().get(bi);
				
				String k = lb.identifier();
				if (!map.containsKey(k)) {
					BBoost b = new BBoost(this, false, lb);
					if (!defmap.containsKey(lb.boostable.key) && !lb.booster.isMul) {
						
						RDDef def = init.deficiencies.get(lb.boostable, b);
						if (def != null) {
							new BoosterImp(new BSourceInfo("< " + lb.boostable.name, l.icon), 0, 1.0, true) {

								@Override
								public double vGet(Region t) {
									if (t.faction() == FACTIONS.player()) {
										if (level.get(t) >= l.index)
											return def.get(t);
									}
									return 1;
								}

								@Override
								public double vGet(Faction f) {
									return 0;
								}
								
								@Override
								public boolean has(Class<? extends BOOSTABLE_O> b) {
									return b == Region.class;
									
								};
								
							}.add(efficiency);
							
						}
						
						defmap.put(lb.boostable.key, 1);
					}
					
					map.put(k, b);
				}
			}
			
			for (int bi = 0; bi < l.global.all().size(); bi++) {
				BoostSpec lb = l.global.all().get(bi);
				
				String k = lb.identifier()+"G";
				if (!map.containsKey(k)) {
					
					BBoost b = new BBoost(this, true, lb);
					map.put(k, b);
				}
			}
		}
		
		for (RDBuildingLevel l : levels) {
	
			for (BoostSpec s : l.global.all()) {
				
				l.local.push(s.booster, s.boostable);
			}
			l.global = null;
		}
		
		KeyMap<String> lmap = new KeyMap<>();
		
		for (RDBuildingLevel lev : levels) {
			
			for (Lock<Region> l : lev.reqs.all()) {
				String k = ""+l.unlocker.name;
				if (!lmap.containsKey(k)) {
					lmap.put(k, k);
					new BoosterImp(new BSourceInfo(DicMisc.¤¤Requirement,  UI.icons().s.boom), 0, 1, true) {
						final int ll = lev.index;
						@Override
						public double vGet(Region t) {
							if (t.faction() == FACTIONS.player()) {
								if (level.get(t) >= ll)
									return l.unlocker.inUnlocked(t) ? 1 : 0;
							}
							return 1;
						}

						@Override
						public double vGet(Faction f) {
							return 0;
						}
						
						@Override
						public boolean has(Class<? extends BOOSTABLE_O> b) {
							return b == Region.class;
							
						};
						
					}.add(efficiency);
					
					
				}
			}
			
		}
		
	}
	
	public boolean canAfford(Region reg, int level) {
		
		RDBuildingLevel tt = levels.get(level);
		
		if (level <= this.level.get(reg))
			return true;
		
		if (reg.faction() != null) {
			int cr = this.levels.get(level).cost - this.levels.get(this.level.get(reg)).cost;
			if (cr > reg.faction().credits().credits())
				return false;
		}
		
		if (!tt.reqs.passes(reg)) {
			return false;
		}
		
		for (BBoost b : bboosts)
			if (!b.canAfford(reg, level))
				return false;

		return true;
		
	}
	
	public LIST<RDBuildingLevel> levels(){
		return levels;
	}
	
	public BoostSpecs boosters(){
		return boosters;
	}

	@Override
	public int index() {
		return index;
	}
	
	private static class BBoost extends BoosterSimple {

		final boolean global;
		final BoostSpec b;
		final RDBuilding bu;
		public double min = Double.MAX_VALUE;
		public double max = Double.MIN_VALUE;
		
		public final double[] froms;
		public final double[] tos;
		
		public BBoost(RDBuilding bu, boolean global, BoostSpec b) {
			super(new BSourceInfo(bu.info.name, global ? DicMisc.¤¤global : null, bu.levels.get(0).icon), b.booster.isMul);
			this.global = global;
			froms = new double[bu.levels.size()];
			tos = new double[bu.levels.size()];
			if (b.booster.isMul) {
				Arrays.fill(froms, 1);
				Arrays.fill(tos, 1);
			}
			this.bu = bu;
			this.b = b;
			
			for (int li = 1; li < bu.levels.size(); li++) {
				RDBuildingLevel l = bu.levels.get(li);
				BoostSpecs coll = global ? l.global : l.local;
				for (BoostSpec bb : coll.all()) {
					if (b.isSameAs(bb)) {

						froms[li] = bb.booster.from();
						tos[li] = bb.booster.to();
						double mi =  Math.min(tos[li], froms[li]);
						double ma = Math.max(tos[li], froms[li]);
						min = Math.min(min, mi);
						max = Math.max(max, ma);
						
						
					}
				}
			}
			
			bu.boosters.push(this, b.boostable, global ? DicMisc.¤¤global : null);
			bu.bboosts.add(this);
			
		}

		@Override
		public double get(Boostable bo, BOOSTABLE_O o) {
			return o.boostableValue(bo, this);
		}
		
		@Override
		public double vGet(Region t) {
			
			if (global && t.realm() != null) {
				return vGet(t.faction());
				
			}
			return g(t);
		}
		
		@Override
		public double vGet(Faction f) {
			
			double res = 0;
			for (int i = 1; i < froms.length; i++) {
				double am = bu.levelAm.get(i-1).get(f);
				res += (tos[i]-froms[i])*am;
			}
			if (b.booster.isMul) {
				res += 1;

			}
	
			return res;
		}
		
		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			if (global)
				return true;
			return b == Region.class;
			
		};
		
		private double g(Region t) {
			double ta = tos[bu.level.get(t)];
			if (!b.booster.isMul && ta < 0)
				return ta;
			int i = bu.level.get(t);
			double vv = tos[i];
			if (b.booster.isMul || vv > 0) {
				return froms[i] + bu.efficiency.get(t)*(tos[i]-froms[i]);
			}
			return vv;
			
		}

		@Override
		public double from() {
			return froms[0];
		}

		@Override
		public double to() {
			return tos[tos.length-1];
		}
		
		public boolean canAfford(Region reg, int level) {
			if (b.booster.isMul)
				return true;
			
			if (RD.DEFS().get(b.boostable, b.booster) != null) {
				double am = tos[bu.level.get(reg)] - tos[level];
				return am <= b.boostable.get(reg);
			}
			return true;
		}


		
	}
	
}