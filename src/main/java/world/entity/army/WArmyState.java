package world.entity.army;

import game.GAME;
import game.faction.FACTIONS;
import game.time.TIME;
import snake2d.util.sets.*;
import util.colors.GCOLORS_MAP;
import util.dic.DicArmy;
import util.dic.DicGeo;
import util.gui.misc.GText;
import world.World;
import world.map.regions.Region;

public abstract class WArmyState implements INDEXED{

	private static LIST<WArmyState> all = new ArrayList<>(0); 
	
	public static LIST<WArmyState> all(){
		return all;
	}
	
	public final static WArmyState fortified = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			return this;
		}
		
		@Override
		public GText info(WArmy a, GText box) {
			box.normalify();
			box.set(DicArmy.¤¤Fortified);
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicArmy.¤¤Fortified;
		}
	}; 
	
	public final static WArmyState fortifying = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			a.stateFloat += ds;
			if (a.stateFloat > TIME.secondsPerDay/2)
				return fortified;
			return this;
				
		}
		
		@Override
		public GText info(WArmy a, GText box) {
			box.normalify();
			box.set(DicArmy.¤¤Fortifying);
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicArmy.¤¤Fortifying;
		}
	}; 
	
	public final static WArmyState moving = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			if (!a.path().move(a, WArmy.speed*ds, a.cost())) {
				a.stateFloat = 0;
				
				return fortifying;
			}
			
			return this;
		}
		


		@Override
		public GText info(WArmy a, GText box) {
			Region reg = World.REGIONS().getter.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLORS_MAP.get(reg.faction()));
				text.add(DicArmy.¤¤MarchingTo).insert(0, reg.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicGeo.¤¤Moving;
		}
	};
	
	public final static WArmyState intercepting = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			WArmy other = intercepting(a);
			if (other == null ||  !a.path().intercept(a, WArmy.speed*ds, a.cost(), other)){
				a.stateFloat = 0;
				return fortifying;
			}
			return this;
		}
		
		private WArmy intercepting(WArmy a) {
			if (a.stateShort != -1) {
				WArmy aa = World.ENTITIES().armies.get(a.stateShort);
				if (aa == null || !aa.added()) {
					a.stateShort = -1;
					return null;
				}
				return aa;
			}
			return null;
		}

		@Override
		public GText info(WArmy a, GText box) {
			WArmy aa = intercepting(a);
			if (aa == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLORS_MAP.get(aa.faction()));
				text.add(DicArmy.¤¤Intercepting).insert(0, aa.name);
			}
			return box;
			
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicGeo.¤¤Moving;
		}
	};
	
	public final static WArmyState besieging = new WArmyState() {
		
		@Override
		WArmyState update(WArmy a, double ds) {
			Region reg = World.REGIONS().getByIndex(a.stateShort);
			if (canBesiege(a, reg)) {
				if (!a.path().isValid()) {
					a.stateFloat += ds;
					reg.besiege(a, a.stateFloat);
					return this;
				}else if (Math.abs(reg.cx()-a.ctx()) <= 2 && Math.abs(reg.cy()-a.cty()) <= 2) {
					GAME.battle().besiegeFirst(a, reg, 0);
					a.path().clear();
					return this;
					
				}else if (a.path().move(a, WArmy.speed*ds, a.cost())) {
					return this;
					
				}
				
			}
			a.path().clear();
			a.stateFloat = 0;
			return fortifying;
			
			
		}
		
		@Override
		WArmyState updateLong(WArmy a) {
			Region reg = World.REGIONS().getByIndex(a.stateShort);
			if (canBesiege(a, reg) && !a.path().isValid() && Math.abs(reg.cx()-a.ctx()) <= 2 && Math.abs(reg.cy()-a.cty()) <= 2) {
				GAME.battle().besiegeContinous(a, reg, a.stateFloat);
			}
			return this;
		}

		@Override
		public GText info(WArmy a, GText box) {
			Region reg = World.REGIONS().getter.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLORS_MAP.get(reg.faction()));
				text.add(DicArmy.¤¤BesiegingSomething).insert(0, reg.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicArmy.¤¤Besieging;
		}
	};
	
	public static boolean canBesiege(WArmy a, Region reg) {
		
		
		return (reg != null && (!FACTIONS.rel().ally(a.faction(), reg.faction()) || FACTIONS.rel().vassalTo.get(a.faction(), reg.faction()) == 1));
	}
	
	private final int index;
	
	private WArmyState () {
		all = all.join(this);
		index = all.size()-1;
	}
	
	abstract WArmyState update(WArmy a, double ds);
	
	WArmyState updateLong(WArmy a) {
		return this;
	}
	
	public abstract GText info(WArmy a, GText text);
	public abstract CharSequence name(WArmy a);
	
	@Override
	public int index() {
		return index;
	}
	
}
