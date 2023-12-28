package world.army;

import game.faction.FACTIONS;
import init.D;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.regions.Region;
import world.regions.data.RD;

final class WArmyUpdater {
	
	private static CharSequence ¤¤Starving = "¤Supplies low!";
	private static CharSequence ¤¤StarvingD = "¤Essential supplies have not been delivered to our army, affecting health and morale. Low health will stop training of new recruits and cause deaths and desertion. Low morale will affect your troops performance on the battlefield.  Fill up our military depots and fortify the army immediately. Affected army: {0}";
	private static CharSequence ¤¤Desertion = "¤Desertion!";
	private static CharSequence ¤¤DesertionD = "¤Army supplies are low, and as a result {0} soldiers have deserted from {1}.";
	
	static {
		D.ts(WArmyUpdater.class);
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
		
		if (AD.men(null).get(a) == 0)
			return true;
		
		if (a.faction() == null) {
			updateRebel(a);
		}else {
		
		}
		return true;
		
	}
	
	void starve(WArmy a) {
		
		double health = AD.supplies().health(a);
		int am = 0;
		for (int di = 0; di < a.divs().size(); di++) {
			ADDiv div = a.divs().get(di);
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
			
			AD.supplies().update(a);
			
			if (AD.supplies().health(a) < 1) {
				if (AD.supplies().health.get(a) == 0) {
					AD.supplies().health.set(a, 1);
					Str.TMP.clear();
					Str.TMP.add(¤¤StarvingD).insert(0, a.name);
					new MessageText(¤¤Starving, Str.TMP).send();
					return false;
				}
				return false;
			}
			if (AD.supplies().morale(a) < 1) {
				if (AD.supplies().morale.get(a) == 0) {
					AD.supplies().morale.set(a, 1);
					Str.TMP.clear();
					Str.TMP.add(¤¤StarvingD).insert(0, a.name);
					new MessageText(¤¤Starving, Str.TMP).send();
				}
				return true;
			}
			AD.supplies().health.set(a, 0);
			AD.supplies().morale.set(a, 0);
		}else if (a.acceptsSupplies()){
			for (ADSupply s : AD.supplies().all) {
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
				d.updateDay();
			}else if (div instanceof WDivStored) {
				((WDivStored) div).age();
			}
		}
	}
	
	void updateRebel(WArmy a) {
		
		if (AD.men(null).get(a) == 0) {
			a.disband();
			return;
		}
			
		
		Region r = a.region();
		if (r == null) {
			RDist rr = WORLD.PATH().tmpRegs.single(a.ctx(), a.cty(), WTREATY.NEIGHBOURS(null), ally.get(a));
			if (rr == null) {
				a.disband();
			}else {
				a.setDestination(rr.reg.cx(), rr.reg.cy());
			}
		}else if(r.faction() == FACTIONS.player()) {
			if (AD.power().get(a) > RD.MILITARY().power.getD(r)) {
				a.besiege(r);
			}else if (RND.oneIn(16)) {
				a.disband();
			}
		}else {
			RDist rr = WORLD.PATH().tmpRegs.single(a.ctx(), a.cty(), WTREATY.NEIGHBOURS(r), rebelTarget.get(a));
			if (rr != null && AD.power().get(a) > RD.MILITARY().power.getD(rr.reg)) {
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
	
	private static final Sel ally = new Sel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == null;
		}
	};
	
	private static final Sel rebelTarget = new Sel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == FACTIONS.player();
		}
	};
	
	static abstract class Sel extends WRegSel {
		WArmy army;
		
		public WRegSel get(WArmy army){
			this.army = army;
			return this;
		}
	}

	
}
