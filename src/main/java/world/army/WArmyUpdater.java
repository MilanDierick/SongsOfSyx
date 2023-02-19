package world.army;

import game.faction.FACTIONS;
import init.D;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEAN_OBJECT;
import view.main.MessageText;
import world.army.WARMYD.WArmySupply;
import world.entity.WPathing;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.REGIOND;
import world.map.regions.Region;

final class WArmyUpdater {
	
	private static CharSequence ¤¤Starving = "¤Supplies low!";
	private static CharSequence ¤¤StarvingD = "¤Essential supplies have not been delivered to our army, affecting health and morale. Low health will stop training of new recruits and cause deaths and desertion. Low morale will affect your troops performance on the battlefield.  Fill up our military depots and fortify the army immediately. Affected army: {0}";
	private static CharSequence ¤¤Desertion = "¤Desertion!";
	private static CharSequence ¤¤DesertionD = "¤Army supplies are low, and as a result {0} soldiers have deserted from {1}.";
	
	{
		D.ts(this.getClass());
	}
	
	boolean update(WArmy a) {
		

		if (supply(a)) {
			train(a);
		}else {
			starve(a);
			a.stop();
			if (!a.added())
				return false;
		}
			
		if (a.state() != WArmyState.fortifying && a.state() != WArmyState.fortified)
			return true;
		
		if (WARMYD.men(null).get(a) == 0)
			return true;
		
		if (a.faction() == null) {
			updateRebel(a);
		}else {
		
		}
		return true;
		
	}
	
	void starve(WArmy a) {
		
		double health = WARMYD.supplies().health(a);
		int am = 0;
		for (int di = 0; di < a.divs().size(); di++) {
			WDIV div = a.divs().get(di);
			if (health < RND.rFloat()) {
				if (div.needSupplies()) {
					int aa = (int)(div.men()*(1.0-health)*(0.5+0.5*RND.rFloat()));
					am += aa;
					div.menSet(div.men()-aa);
				}
			}
		}
		
		if (am > 0) {
			Str.TMP.clear();
			Str.TMP.add(¤¤DesertionD).insert(0, am).insert(1, a.name);
			new MessageText(¤¤Desertion, Str.TMP).send();
		}
		
		
	}
	
	boolean supply(WArmy a) {
		if (a.faction() == FACTIONS.player()) {
			
			WARMYD.supplies().update(a);
			
			if (WARMYD.supplies().health(a) < 1) {
				if (WARMYD.supplies().health.get(a) == 0) {
					WARMYD.supplies().health.set(a, 1);
					Str.TMP.clear();
					Str.TMP.add(¤¤StarvingD).insert(0, a.name);
					new MessageText(¤¤Starving, Str.TMP).send();
					return false;
				}
				return false;
			}
			if (WARMYD.supplies().morale(a) < 1) {
				if (WARMYD.supplies().morale.get(a) == 0) {
					WARMYD.supplies().morale.set(a, 1);
					Str.TMP.clear();
					Str.TMP.add(¤¤StarvingD).insert(0, a.name);
					new MessageText(¤¤Starving, Str.TMP).send();
				}
				return true;
			}
			WARMYD.supplies().health.set(a, 0);
			WARMYD.supplies().morale.set(a, 0);
		}else if (a.acceptsSupplies()){
			for (WArmySupply s : WARMYD.supplies().all) {
				double am = Math.ceil(s.used().get(a)/16.0);
				am = CLAMP.d(am, 0, s.needed(a));
				s.current().inc(a, (int)am);
			}	
		}
		return true;
	}
	
	void train(WArmy a) {
		for (int di = 0; di < a.divs().size(); di++) {
			WDIV div = a.divs().get(di);
			if (div instanceof WDivRegional) {
				WDivRegional d = (WDivRegional) div;
				int t = (int) (d.menTarget());
				int m = d.men();
				if (t < m) {
					;
				}else if (t > m) {
					
					if (a.acceptsSupplies()) {
						if (d.daysUntilMenArrives() == 1) {
							m += d.amountOfMenThatWillArrive()*WARMYD.supplies().health(a);
							d.timerReset();
						}else {
							d.timerInc(-1);
						}
					}
					m = CLAMP.i(m, 0, t);
				}
				d.menSet(m);
			}else if (div instanceof WDivStored) {
				((WDivStored) div).age();
			}
		}
	}
	
	void updateRebel(WArmy a) {
		
		if (WARMYD.men(null).get(a) == 0) {
			a.disband();
			return;
		}
			
		
		Region r = a.region();
		if (r == null) {
			r = WPathing.findAdjacentRegion(a.ctx(), a.cty(), ally.get(a));
			if (r == null) {
				a.disband();
			}else {
				COORDINATE c = WPathing.random(r);
				a.setDestination(c.x(), c.y());
			}
		}else if(r.faction() == FACTIONS.player()) {
			if (WARMYD.quality().get(a) > REGIOND.MILITARY().power.get(r)) {
				a.besiege(r);
			}else if (RND.oneIn(16)) {
				a.disband();
			}
		}else {
			r = WPathing.findAdjacentRegion(a.ctx(), a.cty(), rebelTarget.get(a));
			if (r != null) {
				a.besiege(r);
			}
		}
		
		
	}
	
//	static void updateFaction(WArmy a) {
//		Region r = a.region();
//		if (r == null) {
//			r = WPathing.findAdjacentRegion(a.ctx(), a.cty(), ally.get(a));
//			if (r == null) {
//				a.disband();
//			}else {
//				COORDINATE c = WPathing.random(r);
//				a.setDestination(c.x(), c.y());
//			}
//			return;
//		}else if(FACTIONS.rel().enemy(null, r.faction())) {
//			if (WARMYD.quality().get(a) > REGIOND.MILITARY().power.get(r)) {
//				a.besiege(r);
//			}else if (RND.oneIn(32)) {
//				a.disband();
//			}
//		}
//	}
	
	private static final Finder ally = new Finder() {
		
		@Override
		public boolean is(Region t) {
			return FACTIONS.rel().ally(army.faction(), t.faction());
		}
	};
	
	private static final Finder rebelTarget = new Finder() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == FACTIONS.player();
		}
	};
	
	static abstract class Finder implements BOOLEAN_OBJECT<Region> {
		WArmy army;
		
		public BOOLEAN_OBJECT<Region> get(WArmy army){
			this.army = army;
			return this;
		}
	}

	
}
