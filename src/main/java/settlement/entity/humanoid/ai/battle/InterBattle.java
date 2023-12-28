package settlement.entity.humanoid.ai.battle;

import game.boosting.BOOSTABLES;
import init.sound.SOUND;
import settlement.army.Div;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIEventListeners.HEventListener;

class InterBattle {
	
	public final static HEventListener listener = new HEventListener() {

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch(e.event) {
			case MEET_HARMLESS:
				return false;
			case COLLISION_SOFT:
				d.interrupt(a, e);
				d.overwrite(a, AI.modules().battle.subSoft.initReady(d, a, e.other, e.norX, e.norY, e.facingDot, e.momentum));
				break;
			case MEET_ENEMY:
				d.interrupt(a, e);
				SOUND.sett().action.sword.rnd(e.other.physics.body());
				d.overwrite(a, AI.modules().battle.fight.initReady(d, a, e.other, e.norX, e.norY, e.facingDot, e.momentum));
				break;
			case CHECK_MORALE:
				Div div = a.division();
				if (div == null) {
					return AIEventListeners.def.event(a, d, e);
				} else {
					if (div.settings.mustering() && div.morale.get() <= 0) {
						d.overwrite(a, AI.modules().battle.dessert);
					}
				}
				break;
			case COLLISION_TILE:
				if (AI.modules().battle.tile.shouldattackTile(d, a, e.tx, e.ty)) {
					d.overwrite(a, AI.modules().battle.tile.init(d, a, e.tx, e.ty));
					break;
				}else {
					d.interrupt(a, e);
					d.overwrite(a, AI.SUBS().STAND.activateTime(a, d, 1));
				}
				return true;
			default:
				return AIEventListeners.def.event(a, d, e);
			}
			return false;
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return AIEventListeners.def.poll(a, d, e);
		};
		
		
		
		
	};
	
	public static double pollReady(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.IMPACT_DAMAGE) {
			if (e.isEnemy) {
				
				double attack = Util.getAttackValue(e.colli.other, e.colli.dirDotOther, a, e.colli.dirDot);
				if (attack > 0) {
					
					double str = BOOSTABLES.BATTLE().BLUNT_ATTACK.get(a.indu());
					attack*= str;
					
					
					e.damage.damageStrength = attack;
					
					for (int i = 0; i < e.damage.damage.length; i++) {
						double da = BOOSTABLES.BATTLE().DAMAGES.get(i).attack.get(a.indu());
						e.damage.damage[i] = da;
					}
					
					e.damage.momentum += Util.strengthMomentum(a, d);
				}
			}else {
				e.damage.damageStrength = 0;
			}
			return 0;
		}
		if (e.type == HPoll.DEFENCE)
			return AIEventListeners.def.poll(a, d, e)*1.5;
		
		return AIEventListeners.def.poll(a, d, e);
	};
	

	
	
}
