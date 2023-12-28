package world.regions.data;

import game.boosting.*;
import game.faction.Faction;
import init.D;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.dic.DicGeo;
import util.dic.DicMisc;
import view.interrupter.IDebugPanel;
import world.regions.Region;
import world.regions.data.RD.RDAddable;
import world.regions.data.RD.RDInit;

public class RDAdmin {

	private static CharSequence ¤¤desc = "Admin is primarily produced in your city. It is then used to build and upgrade regions.";
	
	static {
		D.ts(RDAdmin.class);
	}
	
	public final Boostable boost;
	public final INT_OE<Faction> factionSource;
	private final INT_OE<Faction> changed;
	private final INT_OE<Faction> consumed;
	private ArrayListGrower<BoostSpec> cons = null;
	
	RDAdmin(RDInit init) {
		factionSource = init.rCount.new DataInt();
		changed = init.rCount.new DataBit();
		consumed = init.rCount.new DataInt();
		
		boost = BOOSTING.push("ADMIN", 0, DicMisc.¤¤Admin, ¤¤desc, UI.icons().s.admin, BoostableCat.WORLD);
		init.deficiencies.register(boost);
		init.adders.add(new RDAddable() {
			
			@Override
			public void removeFromFaction(Region r) {
				change(r);
				
			}
			
			@Override
			public void addToFaction(Region r) {
				change(r);
			}
		});
		
		IDebugPanel.add("admin + 1000", new ACTION() {
			
			@Override
			public void exe() {
				
				BoosterSimple s = new BoosterSimple(new BSourceInfo("Cheat", UI.icons().s.death), false) {
					
					@Override
					public double vGet(Faction f) {
						return 1;
					}
					
					@Override
					public double to() {
						return 1000;
					}
					
					@Override
					public double from() {
						return 0;
					}
				};
				s.add(boost);
			}
		});
		
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				
				
				new BoosterImp(new BSourceInfo(SETT.ROOMS().ADMINS.get(0).info.names, UI.icons().s.admin), 0, 1000000, false) {

					@Override
					public double vGet(Region t) {
						if (t.realm() == null)
							return 20000;
						return factionSource.get(t.faction());
					}

					@Override
					public double get(Boostable bo, BOOSTABLE_O o) {
						return o.boostableValue(bo, this);
					};
					
					@Override
					public double vGet(Faction f) {
						return factionSource.get(f);
					}
					
				}.add(boost);
				
				new BoosterImp(new BSourceInfo(DicGeo.¤¤Realm, UI.icons().s.crown), -10000000, 0, false) {

					@Override
					public double get(Boostable bo, BOOSTABLE_O o) {
						return o.boostableValue(bo, this);
					};
					
					@Override
					public double vGet(Region reg) {
						int am = -count(reg);
						for (BoostSpec b : consumers()) {
							am -= b.get(reg);
						}
						return am;
					};
					
					@Override
					public double vGet(Faction f) {
						return -count(f);
					}
					
				}.add(boost);
			}
		});
		
	}
	
	public void change(Region reg) {
		if (reg != null && reg.realm() != null) {
			changed.set(reg.faction(), 1);
		}
	}
	
	private LIST<BoostSpec> consumers(){
		if (cons == null) {
			cons = new ArrayListGrower<>();
			for (BoostSpec b : boost.adds()) {
				if (b.booster.to() < 0) {
					cons.add(b);
				}
			}
		}
		return cons;
	}
	
	private int count(Region reg) {
		if (reg.realm() == null) {
			return 0;
		}
		return count(reg.faction());
	}
	
	private int count(Faction f) {
		if (changed.get(f) ==  1) {
			changed.set(f, 0);
			
			int am = 0;
			for (int ri = 0; ri < f.realm().regions(); ri++) {
				Region r = f.realm().region(ri);
				for (BoostSpec b : consumers()) {
					am += b.get(r);
				}
			}
			consumed.set(f, -am);
		}
		return consumed.get(f);
	}
	
	public int consumed(Region reg) {
		int am = 0;
		for (BoostSpec b : consumers()) {
			double v = b.get(reg);
			if (v < 0)
				am += b.get(reg);
		}
		return am;
	}
	
	public int available(Faction f) {
		return factionSource.get(f)-consumed.get(f);
	}
	
	public int consumed(Faction f) {
		int am = 0;
		for (int ri = 0; ri < f.realm().regions(); ri++) {
			Region r = f.realm().region(ri);
			for (BoostSpec b : consumers()) {
				am += b.get(r);
			}
		}
		return -am;
	}


}
