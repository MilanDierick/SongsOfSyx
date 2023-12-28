package settlement.entity.humanoid.ai.battle;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class MarchSoftCollision extends AISUB.Resumable{
	
	AISubActivation initReady(AIManager d, Humanoid a, ENTITY other, double norX, double norY, double faceDot,
			double momentum) {
		if (a.division() != null)
			a.speed.setDirCurrent(a.division().dir());
		if (faceDot > 0.5 && other != null)
			return activate(a, d, stop);
		else
			return activate(a, d, stop);
	}
	
	protected MarchSoftCollision() {

	}

	@Override
	protected AISTATE init(Humanoid a, AIManager d) {
		d.subByte = (byte) TIME.currentSecond();
		return null;
	}

	
	
	private final Resumer stop = new ResumerB() {
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT) {
				if (a.division() != null)
					a.speed.setDirCurrent(a.division().dir());
//				if (e.facingDot > 0.5)
//					d.overwrite(a, stop_back.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}
		
		@Override
		protected AISTATE setAction(Humanoid a, AIManager d) {
			return AI.STATES().STAND_SWORD.activate(a, d, 1.0+RND.rFloat0(0.5));
		}
		
		@Override
		protected AISTATE res(Humanoid a, AIManager d) {
			return fail.set(a, d);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch(e.type) {
			case COLLIDING:{
				return 1;
			}

			default :
				return InterBattle.listener.poll(a, d, e);
			}
		}
	};
	
	private final Resumer fail = new ResumerB() {
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			
			if (e.event == HEvent.COLLISION_SOFT) {
				
				return false;
			}
			return super.event(a, d, e);
		}
		
		@Override
		protected AISTATE setAction(Humanoid a, AIManager d) {
			if (a.division() != null)
				a.speed.setDirCurrent(a.division().dir());
			return AI.STATES().STAND_SWORD.activate(a, d, 2.0+RND.rFloat0(1.5));
		}
		
		@Override
		protected AISTATE res(Humanoid a, AIManager d) {
			return null;
		}
	};
	
	private abstract class ResumerB extends Resumer {
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch(e.event) {
			case COLLISION_SOFT:
				d.overwrite(a, stop.set(a, d));
				return false;
			case EXHAUST:
				if (RND.oneIn(BOOSTABLES.PHYSICS().STAMINA.get(a.indu())*8)) {
					if (STATS.NEEDS().EXHASTION.indu().isMax(a.indu())) {
						d.interrupt(a, e);
						d.overwrite(a, AI.listeners().EXHAUSTED.activate(a, d));
					}
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 1);
				}
				return false;
			default :
				return InterBattle.listener.event(a, d, e);
			}
			
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch(e.type) {
			case COLLIDING:{
				if (InterBattle.listener.poll(a, d, e) == 0)
					return STATS.BATTLE().DIV.get(a) != null && STATS.BATTLE().DIV.get(a).settings.isFighting() && !STATS.BATTLE().DIV.get(a).settings.moppingUp() ? 1 :0;
				return 0;
			}
				
			
			default :
				return InterBattle.listener.poll(a, d, e);
			}
		}
		
	}
	
}
