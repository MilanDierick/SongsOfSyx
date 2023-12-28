package world.entity.army;

import game.faction.FACTIONS;
import game.time.TIME;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLORS_MAP;
import util.dic.DicArmy;
import util.dic.DicGeo;
import util.gui.misc.GText;
import view.main.VIEW;
import world.WORLD;
import world.regions.Region;

public abstract class WArmyState implements INDEXED{

	private static LIST<WArmyState> all = new ArrayList<>(0); 
	
	private static CharSequence ¤¤siege = "Are you sure you wish to besiege {0} and declare war on the faction of {0}?"; 
	
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
			if (!a.path().move(a, WArmy.speed*ds)) {
				a.stateFloat = 0;
				
				return fortifying;
			}
			
			return this;
		}
		


		@Override
		public GText info(WArmy a, GText box) {
			Region reg = WORLD.REGIONS().map.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLORS_MAP.get(reg.faction()));
				text.add(DicArmy.¤¤MarchingTo).insert(0, reg.info.name());
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
			
			if (other == null || !a.path().isValid()) {
				a.stateFloat = 0;
				return fortifying;
			}
			
			if (a.path().destX() == other.ctx() && a.path().destY() == other.cty()) {
				a.path().move(a, WArmy.speed*ds);
			}
			
			double dist = COORDINATE.tileDistance(a.path().destX(), a.path().destY(), other.ctx(), other.cty());
			if (dist*10 > a.path().remaining())
				if (!a.path().find(a.ctx(), a.cty(), other.ctx(), other.cty())) {
					a.stateFloat = 0;
					return fortifying;
				}
			
		
			return this;
		}
		
		private WArmy intercepting(WArmy a) {
			if (a.stateShort != -1) {
				WArmy aa = WORLD.ENTITIES().armies.get(a.stateShort);
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
		
		Region aReg;
		WArmy aa;
		private ACTION besiege = new ACTION() {
			
			@Override
			public void exe() {
				FACTIONS.DIP().war.set(aReg.faction(), aa.faction(), true);
				aa.besiege(aReg);
			}
		};
		
		@Override
		WArmyState update(WArmy a, double ds) {
			
			Region reg = WORLD.REGIONS().getByIndex(a.stateShort);
			if (!a.path().isValid()) {
				if (!a.besieging(reg)) {
					a.path().clear();
					a.stateFloat = 0;
					return fortifying;
				}
				return this;
			}
			
			if (a.path().move(a, WArmy.speed*ds)) {
				return this;
			}else {
				a.path().clear();
				if (a.faction() == FACTIONS.player() && reg.faction() != a.faction() && !FACTIONS.DIP().war.is(reg.faction(), a.faction()) && reg.faction() != null) {
					aReg = reg;
					aa = a;
					VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤siege).insert(0, reg.info.name()).insert(0, reg.faction().name), besiege, ACTION.NOP, true);
					return this;
				}else {
					WORLD.BATTLES().besige(a, reg);
				}
			}
			
			return this;
			
		}

		@Override
		public GText info(WArmy a, GText box) {
			Region reg = WORLD.REGIONS().map.get(a.path().destX(), a.path().destY());
			if (reg == null) {
				box.normalify();
				box.add(name(a));
			}else {
				GText text = box;
				text.color(GCOLORS_MAP.get(reg.faction()));
				text.add(DicArmy.¤¤BesiegingSomething).insert(0, reg.info.name());
			}
			return box;
		}

		@Override
		public CharSequence name(WArmy a) {
			return DicArmy.¤¤Besieging;
		}
	};
	
	public static boolean canBesiege(WArmy a, Region reg) {
		
		
		return (reg != null && a.faction() != reg.faction());
	}
	
	private final int index;
	
	private WArmyState () {
		all = all.join(this);
		index = all.size()-1;
	}
	
	abstract WArmyState update(WArmy a, double ds);
	
	public abstract GText info(WArmy a, GText text);
	public abstract CharSequence name(WArmy a);
	
	@Override
	public int index() {
		return index;
	}
	
}
