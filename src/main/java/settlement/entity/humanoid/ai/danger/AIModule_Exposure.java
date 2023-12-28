package settlement.entity.humanoid.ai.danger;

import static settlement.main.SETT.*;

import init.D;
import init.need.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.stats.STATS;
import settlement.stats.util.CAUSE_LEAVE;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

final class AIModule_Exposure extends AIModule {

	private final AIDataSuspender suspender = AI.suspender();
	
	private static CharSequence ¤¤freezing = "Freezing!";
	private static CharSequence ¤¤cover = "Cooling down";
	private static CharSequence ¤¤nearDeath = "(Near Death!)";
	
	static {
		D.ts(AIModule_Exposure.class);
	}

	
	public AIModule_Exposure() {

	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		if (STATS.NEEDS().EXPOSURE.count.get(a.indu()) == 0)
			return null;
		
		if (!suspender.is(d)) {
			double e = SETT.WEATHER().temp.getEntityTemp();
			if (e > 0) {
				AiPlanActivation p = getHot(a, d);
				if (p != null)
					return p;
			}else if (e < 0) {
				AiPlanActivation p = getCold(a, d);
				if (p != null)
					return p;
			}
			AiPlanActivation p = inside.activate(a, d);
			if (p != null)
				return p;
		}
		suspender.suspend(d);
		
		if (STATS.NEEDS().EXPOSURE.inDanger(a.indu())) {
			return dying.activate(a, d);
		}
		
		return null;
	}

	public AiPlanActivation getHot(Humanoid a, AIManager d) {
		AiPlanActivation p = AI.modules().needs.get(a, d, NEEDS.TYPES().SKINNYDIP, 500);
		if (p != null)
			return p;
		
		for (ROOM_WELL w : SETT.ROOMS().WELLS) {
			p = AI.modules().needs.get(a, d, w.service(), 500);
			if (p != null)
				return p;
		}
		return null;
	}
	
	public AiPlanActivation getCold(Humanoid a, AIManager d) {
		AiPlanActivation p = AI.modules().needs.get(a, d, SETT.ROOMS().HEARTH.service(), 500);
		if (p != null)
			return p;
		return null;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		suspender.update(d);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {

		

		if (STATS.NEEDS().EXPOSURE.inDanger(a.indu())) {
			return (int) (8);			
		}
		
		if (is(a, d) && STATS.NEEDS().EXPOSURE.count.get(a.indu()) > 0)
			return 5;
		
		if (suspender.is(d))
			return 0;
		
		if (STATS.NEEDS().EXPOSURE.count.getD(a.indu()) > 0.5)
			return 5;
		
		
		

		return 0;

	}

	
	final AIPLAN inside = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return find.set(a, d);
		}
		
		private final Resumer find = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				exit.set(a, d);
				FINDABLE f = PATH().finders.indoor.getReservable(a.tc().x(), a.tc().y());
				if (f != null) {
					d.planTile.set(a.tc().x(), a.tc().y());
					f.findableReserve();
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.indoor, 64);
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
					return s;
				}
				
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				return exit.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer exit = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (5 + RND.rInt(5));
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1--;
				
				
				if (d.planByte1 > 0 && STATS.NEEDS().EXPOSURE.count.get(a.indu()) > 0 && moduleCanContinue(a, d)) {
					if (STATS.NEEDS().EXPOSURE.critical(a.indu())) {
						return AI.SUBS().LAY.activateTime(a, d, 8);
					}
					
					if (RND.rBoolean()) {
						return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.wave.activate(a, d, 2+RND.rFloat()*2));
					}
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
				if (f != null)
					f.findableReserveCancel();
			}
		};

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (SETT.WEATHER().temp.getEntityTemp() < 0) {
				string.add(¤¤freezing);
			}else
				string.add(¤¤cover);
			if (STATS.NEEDS().EXPOSURE.critical(a.indu()))
				string.s().add(¤¤nearDeath);
			
		};
		
	};
	
	private final AIPLAN dying = new AIPLAN.PLANRES() {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return exit.set(a, d);
		}
		
		private final Resumer exit = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 8);
			};
			
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (STATS.NEEDS().EXPOSURE.count.isMax(a.indu())) {
					AIManager.dead = SETT.WEATHER().temp.getEntityTemp() > 0 ? CAUSE_LEAVE.HEAT : CAUSE_LEAVE.COLD;
					return AI.SUBS().LAY.activateTime(a, d, 8);
				}
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {

			}
		};

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (SETT.WEATHER().temp.getEntityTemp() < 0) {
				string.add(¤¤freezing);
			}else
				string.add(¤¤cover);
			string.s().add(¤¤nearDeath);
		};
		
	};

}
