package settlement.entity.humanoid.ai.needs;

import static settlement.main.SETT.*;

import game.time.TIME;
import init.D;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.health.AIModule_Hygine;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.service.hearth.ROOM_HEARTH;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

public final class AIModule_Exposure extends AIModule {

	private final AIDataSuspender suspender = AI.suspender();
	private final AIDataBit used = AI.bit();
	private final AIModule_Hygine bath; 
	
	private static CharSequence ¤¤freezing = "Freezing!";
	private static CharSequence ¤¤cover = "Cooling down";
	private static CharSequence ¤¤nearDeath = "(Near Death!)";
	
	static {
		D.ts(AIModule_Exposure.class);
	}
	
	public AIModule_Exposure(AIModule_Hygine bath) {
		this.bath = bath;
	}

	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {

		if ((is(a, d) && STATS.NEEDS().EXPOSURE.count.get(a.indu()) > 0) || STATS.NEEDS().EXPOSURE.inDanger(a.indu())) {
			double e = SETT.WEATHER().temp.getEntityTemp();
			if (e > 0) {
				AiPlanActivation p = bath.getHot(a, d);
				if (p != null)
					return p;
			}else if (e < 0) {
				AiPlanActivation p = warm.activate(a, d);
				
				if (p != null)
					return p;
			}
			AiPlanActivation p = inside.activate(a, d);
			if (p != null)
				return p;
		}else if (used.get(d) == 0) {
			AiPlanActivation p = warm.activate(a, d);
			if (p != null)
				return p;
		}
		
		suspender.suspend(d);
		return null;
	}

	public AiPlanActivation get(Humanoid a, AIManager d) {
		return getPlan(a, d)
				;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		if (newDay) {
			used.set(d, 0);
		}
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
		
		if (used.get(d) == 0) {
			if (!SETT.ROOMS().HEARTH.service().stats().access(a) && SETT.ROOMS().HEARTH.service().accessRequest(a))
				return 2;
			else if((TIME.days().bitsSinceStart() & 0x0F) == (a.indu().randomness() & 0x0Fl)) {
				return 2;
			}
		}
		return 0;

	}


	
	private final AIPLAN warm = new AIPLAN.PLANRES() {
		
		private final ROOM_HEARTH hearth = SETT.ROOMS().HEARTH;
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (!hearth.service().stats().permission().has(a))
				return null;
			return start.set(a, d);
		}
		

		private final Resumer start = new Resumer(hearth.service().verb) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, hearth.service(), STATS.NEEDS().EXPOSURE.inDanger(a.indu()) ? Integer.MAX_VALUE : hearth.service().radius);
				if (s == null) {
					hearth.service().clearAccess(a);
				}
				else {
					hearth.service().reportAccess(a, d.path().destX(), d.path().destY());
					hearth.service().reportDistance(a);
				}
				return s;
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return mourn.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
			}
		};
		
		private final Resumer mourn = new Resumer(hearth.service().verb) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				hearth.service().service(d.path().destX(), d.path().destY()).startUsing();
				d.planByte1 = 0;
				RoomInstance r = hearth.get(a.tc().x(), a.tc().y());
				a.speed.turn2(r.body().cX()-a.tc().x(), r.body().cY()-a.tc().y());
				
				return AI.SUBS().STAND.activateTime(a, d, 2+RND.rInt(15));
				
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				used.set(d, true);
				d.planByte1++;
				STATS.NEEDS().EXPOSURE.count.inc(a.indu(), -6);
				if (STATS.NEEDS().EXPOSURE.count.get(a.indu()) != 0 || d.planByte1 < 8) {
					
					Room r = SETT.ROOMS().HEARTH.get(a.tc());
					
					if (r != null) {
						ROOM_SERVICER ss = (ROOM_SERVICER) r;
						if (ss.service().total()-ss.service().reserved() > 4) {
							if ((d.planByte1 & 1) == 1) {
								
								Animation s = RND.rBoolean() ? AI.STATES().anima.box : AI.STATES().anima.wave;
								return AI.SUBS().single.activate(a, d, s, 1+RND.rFloat(3));
							}
							return AI.SUBS().STAND.activateTime(a, d, 2+RND.rInt(10));
						}
						
					}
					
				}
				
				FSERVICE s = hearth.service().service(d.path().destX(), d.path.destY());
				if (s != null)
					s.consume();
				
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				FSERVICE s = hearth.service().service(d.path().destX(), d.path.destY());
				return (s != null && s.findableReservedIs());
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				 FSERVICE s = hearth.service().service(d.path().destX(), d.path.destY());
				 if (s != null)
					 s.findableReserveCancel();
			}

		};
	};
	
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

}
