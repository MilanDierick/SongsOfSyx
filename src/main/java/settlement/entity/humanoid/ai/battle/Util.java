package settlement.entity.humanoid.ai.battle;

import init.C;
import init.boostable.BOOSTABLES;
import init.config.Config;
import init.sound.SOUND;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.ENTITY.ECollision;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
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
		double attack = getAttackValue(enemy, dot, a, 1.0);
		if (attack > 0) {
			SOUND.sett().action.sword.rnd(enemy.physics.body());
			double mom = attack*C.TILE_SIZE*BOOSTABLES.BATTLE().BLUNT_DAMAGE.get(a);
			double nY = enemy.speed.y() - vec.nY()*mom*enemy.physics.getMassI();
			double nX = enemy.speed.x() - vec.nX()*mom*enemy.physics.getMassI();
			coll.speedHasChanged = true;
			enemy.speed.setRaw(nX, nY);
			coll.pierceDamage = attack*BOOSTABLES.BATTLE().PIERCE_DAMAGE.get(a);
			coll.momentum = mom;
			enemy.collide(coll);
			if (enemy.isRemoved()) {
				STATS.BATTLE().ENEMY_KILLS.indu().inc(a.indu(), 1);
				STATS.BATTLE().COMBAT_EXPERIENCE.indu().inc(a.indu(), 1 + RND.rInt(4));
			}
		}else {
			
			coll.pierceDamage = 0;
			coll.momentum = 0;
			
			enemy.collide(coll);
		}
		
		
		
		
		
		
			
	}
	
	public static double getAttackValue(ENTITY other, double otherFaceDot, Humanoid a, double aFaceDot) {
		double def = 0.1 + Math.max(other.getDefenceSkill(otherFaceDot), 0);
		double attack = ((0.1 + BOOSTABLES.BATTLE().OFFENCE.get(a))*aFaceDot);
		
		attack -= (def*RND.rFloat()*4*Config.BATTLE.BLOCK_CHANCE);
		return CLAMP.d(attack, 0, 1);
	}
	
	static double getTraining(Humanoid a, AIManager d) {
		double de = 0.2*BOOSTABLES.BATTLE().OFFENCE.get(a.indu()) + 0.5*BOOSTABLES.BATTLE().DEFENCE.get(a.indu());
		return CLAMP.d(1.0-de, 0.3, 1);	
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
