package settlement.entity.humanoid.ai.battle;

import game.boosting.BOOSTABLES;
import init.C;
import init.config.Config;
import init.sound.SOUND;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class Util {

	private final static VectorImp vec = new VectorImp();
	private static ECollision coll = new ECollision();
	
	
	static void attack(Humanoid a, AIManager d, Humanoid enemy) {
		

		
		DIR od = enemy.speed.dir();
		vec.set(a.body(), enemy.body());
		coll.other = a;
		
		double dot = (1 -od.xN()*vec.nX() -od.yN()*vec.nY())*0.5;
		coll.norX = vec.nX();
		coll.norY = vec.nY();
		coll.dirDot = dot;
		coll.speedHasChanged = false;
		coll.momentum = 0;
		coll.dirDot = 1.0;
		
		if (setDamage(a, d, coll)) {
			SOUND.sett().action.sword.rnd(enemy.physics.body());
			
			double nY = enemy.speed.y() + vec.nY()*coll.momentum*enemy.physics.getMassI();
			double nX = enemy.speed.x() + vec.nX()*coll.momentum*enemy.physics.getMassI();
			coll.speedHasChanged = true;
			enemy.speed.setRaw(nX, nY);

			
			enemy.collide(coll);
			if (enemy.isRemoved()) {
				STATS.BATTLE().ENEMY_KILLS.indu().inc(a.indu(), 1);
				STATS.BATTLE().COMBAT_EXPERIENCE.indu().inc(a.indu(), 1 + RND.rInt(4));
			}
			
		}else {
			
			coll.damageStrength = 0;
			coll.momentum = 0;
			
			enemy.collide(coll);
		}
		
		
		
		
		
		
			
	}
	
	public static boolean setDamage(Humanoid a, AIManager d, ECollision e) {
		double attack = getAttackValue(e.other, e.dirDotOther, a, e.dirDot);
		if (attack > 0) {			
			
			e.momentum = strengthMomentum(a, d);
			e.damageStrength = attack*BOOSTABLES.BATTLE().BLUNT_ATTACK.get(a.indu());
			
			for (int i = 0; i < e.damage.length; i++) {
				double da = BOOSTABLES.BATTLE().DAMAGES.get(i).attack.get(a.indu());
				e.damage[i] = da;
			}
			
			
			return true;
		}
		return false;
	};
	
	public static double strengthMomentum(Humanoid a, AIManager d) {
		return C.TILE_SIZE*BOOSTABLES.BATTLE().BLUNT_ATTACK.get(a.indu())*(0.5+RND.rFloat()*2);
	};
	
	
	public static double getAttackValue(ENTITY other, double otherFaceDot, Humanoid a, double aFaceDot) {
		double def = 0.1 + Math.max(other.getDefenceSkill(otherFaceDot), 0);
		double attack = 0.1 + Math.max(BOOSTABLES.BATTLE().OFFENCE.get(a.indu()), 0);
		
		double d = attack-def;
		
		d -= RND.rFloat()*Config.BATTLE.BLOCK_CHANCE;
		return CLAMP.d(d, 0, 1);
	}
	
	static double getAttackPause(Humanoid a, AIManager d) {
		double de = (BOOSTABLES.BATTLE().ATTACK_RATE.get(a.indu())+1.0)/(BOOSTABLES.BATTLE().ATTACK_RATE.max(Induvidual.class)+1.0); 
		de*= STATS.NEEDS().EXHASTION.indu().getD(a.indu());
		return 1.0-de;
	}
	
	static boolean isInPosition(COORDINATE dest, Humanoid a, AIManager d) {
		return dest.isSameAs(a.physics.body().cX(), a.physics.body().cY());
	}
	
	static boolean hasSpot(Humanoid a, AIManager d) {
		Div div = a.division();
		return div != null && div.reporter.getPixel(a.divSpot()) != null;
	}
	
	static boolean shouldMoveIntoDivPosition(Humanoid a, AIManager d) {
		if (a.division() == null)
			return false;
		if (!a.division().settings.mustering())
			return false;
		if (a.division().settings.moppingUp())
			return false;
		if (a.division().reporter.getPixel(a.divSpot()) == null)
			return false;
		return true;
	}
	
}
