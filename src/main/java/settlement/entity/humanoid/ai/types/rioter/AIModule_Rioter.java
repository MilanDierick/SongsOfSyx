package settlement.entity.humanoid.ai.types.rioter;

import init.D;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.ai.crime.AIModule_Crime;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

public final class AIModule_Rioter extends AIModule{


	private final CharSequence ¤¤verb = "¤Rioting";
	

	
	{
		D.t(this);
	}
	
	private final Animation[] anima = new Animation[]{
		AI.STATES().anima.fist,
		AI.STATES().anima.grab,
		AI.STATES().anima.lay,
	};
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return planRout.activate(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 11;
	}
	
	{D.gInit(this);}
	
	private final AIPLAN planRout = new AIPLAN.PLANRES(){
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return go.set(a, d);
		}

		private final Resumer go = new Resumer(¤¤verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				AIModule_Crime.notify(a);
				SETT.ROOMS().GUARD.reportCriminal(a, false);
				return AI.SUBS().walkTo.run_arround_crazy(a, d, 1);
				

			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				if (a.indu().hType() != HTYPE.RIOTER) {
					return null;
				}
				
				d.planByte1 -= 5;
				if (d.planByte1 < 0)
					d.planByte1 = 0;
				
				AIModule_Crime.notify(a);
				if (RND.oneIn(5))
					SETT.ROOMS().GUARD.reportCriminal(a, false);
				if (RND.rBoolean()) {
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							return AI.SUBS().single.activate(a, d, anima[RND.rInt(anima.length)].activate(a, d, 2+RND.rInt(5)));
						}
					}
				}
				
				if (RND.rBoolean())
					return AI.SUBS().STAND.activateTime(a, d, 2+RND.rInt(5));
				
				return AI.SUBS().walkTo.run_arround_crazy(a, d, 1);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
		};
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.EXHAUST || e.event == HEvent.COLLISION_HARD)
				return super.event(a, d, e);
			
			if (e.event == HEvent.COLLISION_TILE) {
				
				if (d.planByte1 > 20 && AI.modules().battle.breakTile(a, d, e.tx, e.ty)) {
					return true;
				}else {
					d.planByte1 ++;
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							return true;
						}
					}
					a.speed.turn2(e.norX, e.norY);
					
					return true;
				}
			}else if(e.event == HEvent.MEET_ENEMY) {
				d.interrupt(a, e);
				d.overwrite(a, AI.modules().battle.interrupt(a, d, e.other));	
			}else if (e.event == HEvent.COLLISION_UNREACHABLE) {
				DIR dd = a.speed.dir();
				if (!dd.isOrtho())
					dd = dd.next(1);
				for (int i = 0; i < 4; i++) {
					if (SETT.PATH().connectivity.is(a.tc(), dd)) {
						break;
					}
					dd = dd.next(2);
					//a.speed.turn90();
				}
				if (SETT.PATH().connectivity.is(a.tc(), dd)) {
					a.speed.setRaw(dd, 0.5);
				}
				
			}else if (e.event == HEvent.MEET_ENEMY) {
				a.speed.turn2(-e.norX, -e.norY);
				d.stateTimer = 10;
			}
			return false;
		}

		
	};

}
