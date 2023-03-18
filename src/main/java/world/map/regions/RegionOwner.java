package world.map.regions;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.D;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.*;
import util.data.BOOLEAN_OBJECT.BOOLEAN_OBJECTE;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import view.tool.PlacableSimpleTile;
import view.world.IDebugPanelWorld;
import world.World;
import world.entity.WPathing;
import world.map.regions.REGIOND.RResource;
import world.map.regions.RegionFactor.RegionFactorImp;

public final class RegionOwner extends RResource{
	
	public final INT_OE<Region> distanceToCapitol;
	private final DataO<Region>.DataShort realmI; 
	private final DataO<Region>.DataShort realmIOld;
	private final DataO<Region>.DataNibble realmOldCount;
	public final GETTER_TRANSE<Region, FRegions> realm;
	public final BOOLEAN_OBJECT<Region> capitol;
	public final BOOLEAN_OBJECTE<Region> rebel; 
	final INT_OE<Region> adminPoints;
	public final INT_O<Region> adminPenalty;
	public final DOUBLE_O<Region> deficiency;
	
	public final RegionFactors order;
	public final DOUBLE_O<Region> loyalty_current;
	public final DOUBLE_O<Region> loyalty_target;
	
	RegionOwner(RegionInit init) {
		D.gInit(this);
		
		IDebugPanelWorld.add(new PlacableSimpleTile("Claim region") {
			
			@Override
			public void place(int x, int y) {
				Region r = World.REGIONS().getter.get(x, y);
				if (r != null)
					realm.set(r, FACTIONS.player().kingdom().realm());
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				Region r = World.REGIONS().getter.get(x, y);
				if (r != null)
					return null;
				return E;
			}
		});
		
		loyalty_current = new DOUBLE_O<Region>() {
			
			private final INFO info = new INFO(
					D.g("Loyalty"),
					D.g("LoyaltyD", "Current loyalty overall")
					
					);

			@Override
			public double getD(Region reg) {
				double n = 0;
				int pop = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					n += REGIOND.RACE(r).loyalty.getD(reg)*REGIOND.RACE(r).population.get(reg);
					pop += REGIOND.RACE(r).population.get(reg);
				}
				if (pop == 0) {
					return n > 0 ? 1 : 0;
				}
				return n / pop;
			}
			
			@Override
			public INFO info() {
				return info;
			}
			
		};
		loyalty_target = new DOUBLE_O<Region>() {
			
			private final INFO info = new INFO(
					D.g("Loyalty-Target"),
					D.g("LoyaltyTargetD", "Target loyalty overall")
					
					);

			@Override
			public double getD(Region reg) {
				double t = 0;
				double pop = 0;
				for (Race r : RACES.all()) {
					
					t += REGIOND.RACE(r).loyalty_target.next(reg)*REGIOND.RACE(r).population_target.next(reg);
					pop += REGIOND.RACE(r).population_target.next(reg);
				}
				if (pop == 0) {
					return t > 0 ? 1 : 0;
				}
				return t / pop;
			}
			
			@Override
			public INFO info() {
				return info;
			}
			
		};
		
		order = new RegionFactors(D.g("Order"), D.g("Low order increases the chances of Rebellion"));
		new RegionFactorImp(order, loyalty_target.info()) {
			@Override
			public double getD(Region r) {
				return loyalty_current.getD(r);
			}

			@Override
			public double next(Region r) {
				return loyalty_target.getD(r);
			}
		};
		
		distanceToCapitol = init.count.new DataNibble(D.g("Distance"), D.g("DistanceD", "Distance Penalty to capitol"));
		realmI = init.count.new DataShort(); 
		realmIOld = init.count.new DataShort();
		realmOldCount = init.count.new DataNibble();
		adminPoints = init.count.new DataByte(D.g("AdminPoints", "Admin points"), D.g("AdminPointsD", "Allocated Admin points"));
		adminPenalty = new INT_O<Region>() {

			private final INFO info = new INFO(D.g("AdminPenalty", "Admin Penalty"), D.g("AdminPenaltyD", "Penalties for admin are added based on region capacity and distance to the capitol, as well as the amount of previously allocated points."));
			
			@Override
			public int get(Region t) {
			
				return cost(t, adminPoints.get(t)+1)-cost(t, adminPoints.get(t));
			}

			@Override
			public int min(Region t) {
				return 0;
			}

			@Override
			public int max(Region t) {
				return 10000;
			}
			
			@Override
			public INFO info() {
				return info;
			}
		
		};

		deficiency = new DOUBLE_O<Region>() {
			
			private final INFO info = new INFO(
					D.g("Deficiency"),
					D.g("DeficiencyD", "More Administration points are allocated than you can support. All allocations will work with less efficiency")
					
					);

			@Override
			public double getD(Region t) {
				if (t.faction() == GAME.player()) {
					return GAME.player().admin().penalty();
				}
				return 1;
			}
			
			@Override
			public INFO info() {
				return info;
			}
			
		};
		
		
		realm = new GETTER_TRANSE<Region, FRegions>() {
			
			private final INFO info = new INFO(D.g("Realm"), D.g("RealmD", "The realm this region belongs to."));
					
			
			@Override
			public FRegions get(Region f) {
				int ri = realmI.get(f);
				if (ri == 0)
					return null;
				return FACTIONS.all().get(ri-1).kingdom().realm();
			}

			@Override
			public void set(Region f, FRegions t) {
				
				if (t != null && !t.regions.hasRoom())
					t = null;
				
				FRegions old = get(f);
				if (t == get(f))
					return;
				
				realmOldCount.set(f, 0);
				
				if (t != null && t.capitol() == null)
					throw new RuntimeException("set capitol first");
				
				if (old != null) {
					if (old.capitol() == f)
						old.capitolI = -1;
					for (int i = 0; i < old.regions.size(); i++) {
						if (old.regions.get(i) == f.index()) {
							old.regions.remove(i);
							break;
						}
					}
					if (!rebel.is(f))
						for (RResource r : RResource.all)
							r.remove(f, old);
				}
				
				realmI.set(f,  t == null ? 0 : t.faction().index()+1);
				
				
				if (t != null) {
					t.regions.add(f.index());
					if (!rebel.is(f))
						for (RResource r : RResource.all)
							r.add(f, t);
					t.recount();
				}
					
				World.MINIMAP().updateRegion(f);
				World.REGIONS().ownershipHasChanged = true;
				REGIOND.initNewfaction(f);

			};
			
			@Override
			public INFO info() {
				return info;
			}
		};
		
		rebel = init.count.new DataBit(
				D.g("Rebel"), 
				D.g("RebelD", "A region that is rebelling towards its master. Rebel regions doesn't count towards your stats, and will control themselves. Allies won't attack rebel regions, although they will attack their armies. A rebel region can be recaptured. One can also declare its independence.")) {
			
			@Override
			public BOOLEAN_OBJECTE<Region> set(Region t, boolean b) {

				super.set(t, b);
				
				if (t.realm() != null) {
					if (b) {
						for (RResource res : RResource.all)
							res.remove(t, t.realm());
					}else {
						for (RResource res : RResource.all)
							res.add(t, t.realm());
					}
				}				
				return this;
			}
			
		};
		
		capitol = new BOOLEAN_OBJECT<Region>() {

			private final INFO info = new INFO(D.g("Capitol"), D.g("CapitolD", "Indicated that this is the capitol region, where the capitol of a faction resides."));
			
			@Override
			public boolean is(Region t) {
				return realm.get(t) != null && realm.get(t).capitol() == t;
			}
			
			@Override
			public INFO info() {
				return info;
			}
		};
	}
	
	/**
	 * Will reset faction regions current settings. Can not be placed on other owned region. Must be placed on an ok spot in a region
	 * @param cx
	 * @param cy
	 * @param faction
	 */
	public void setCapitol(int cx, int cy, Faction faction) {
		
		if (!canSetCapitol(cx, cy, faction)) {
			throw new RuntimeException("");
		}
		
		FRegions realm = faction.kingdom().realm();
		Region r = World.REGIONS().getter.get(cx, cy);
		
		
		r.centreSet(cx, cy);
		
		for (Region rr : realm.regions()) {
			for (RResource res : RResource.all)
				res.remove(rr, realm);
		}
		

		
		if (r.realm() != faction.kingdom().realm())
			realm.regions.add(r.index());
		
		realmI.set(r,  faction.index()+1);
		realm.capitolI = (short) r.index();
		
		for (Region rr : realm.regions()) {
			for (RResource res : RResource.all)
				res.add(rr, realm);
		}
		
		realm.recount();
			
	}
	
	public boolean canSetCapitol(int cx, int cy, Faction faction) {
		
		Region r = World.REGIONS().getter.get(cx, cy);
		if (r == null)
			return false;
		
		if (r.realm() != null && r.realm() != faction.kingdom().realm())
			return false;
		
		if (r.isWater())
			return false;
		return true;
			
	}

	@Override
	void remove(Region r, FRegions old) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void add(Region r, FRegions newR) {
		double d = WPathing.regPath(r, r.faction().capitolRegion(), Integer.MAX_VALUE).distance;
		d /= 32;
		d = CLAMP.d(d, 0, 15);
		distanceToCapitol.set(r, (int) d);
	}

	@Override
	void update(Region r, double ds) {
		if (realm.get(r) != null && RND.oneIn(8)) {
			if (realmOldCount.isMax(r)) {
				realmIOld.set(r, realm.get(r).faction().index()+1);
			}
		}
	}
	
	public Faction oldOwner(Region r) {
		int i  = realmIOld.get(r);
		if (i == 0)
			return null;
		return FACTIONS.getByIndex(i);
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
	
	public int nextPointCost(Region r, int points) {
		return cost(r, adminPoints().get(r)+points)-cost(r, adminPoints().get(r));
	}
	
	public double adminCostBase(Region r) {
		return Math.ceil(1 + REGIOND.POP().base().getD(r)*Math.ceil(distanceToCapitol.get(r)*0.25));
	}
	
	public int adminCostAll(Region r) {
		return cost(r, adminPoints().get(r));
	}
	
	private int cost(Region r, int points) {
		if (REGIOND.OWNER().rebel.is(r))
			return 0;
		double c = points * points*0.125/2.0;
		return (int) Math.ceil(c*10*adminCostBase(r));
	}
	
	public INT_O<Region> adminPoints(){
		return adminPoints;
	}
	
	
}
