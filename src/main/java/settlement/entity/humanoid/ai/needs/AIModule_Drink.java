package settlement.entity.humanoid.ai.needs;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.resources.RESOURCES;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.util.FSERVICE;
import settlement.room.service.food.tavern.ROOM_TAVERN;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;

public final class AIModule_Drink extends AIModule{

	{D.gInit(this);}
	
	
	private final ArrayList<Tavern> eateries;
	
	public AIModule_Drink() {
		eateries = new ArrayList<Tavern>(ROOMS().TAVERNS.size());
		for (ROOM_TAVERN a : ROOMS().TAVERNS) {
			eateries.add(new Tavern(a));
		}
		
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		boolean superAccess = STATS.MULTIPLIERS().DRINK.markIs(a);
		
		if (!superAccess)
			for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().DRINK.get(a.indu().clas().index())) {
				s.service().clearAccess(a);
			}
		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().DRINK.get(a.indu().clas().index())) {
			if (s.service().room() instanceof ROOM_TAVERN) {
				if (s.service().accessRequest(a)) {
					AiPlanActivation p = eateries.get(s.service().room().typeIndex()).activate(a, d);
					if (p != null)
						return p;
				}
			}
			
			else {
				//throw new RuntimeException();
			}
		}
		AiPlanActivation p = drink.activate(a, d);
		if (p == null) {
			STATS.NEEDS().THIRST.fixMax(a.indu());
			STATS.MULTIPLIERS().DRINK.mark(a, false);
		}
		
		return p;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.MULTIPLIERS().DRINK.markIs(a))
			return 3;
		
		if (STATS.FOOD().DRINK.decree().get(a) == 0)
			return 0;
		
		return (int) Math.ceil(STATS.NEEDS().THIRST.getPrio(a.indu())*8);
		
	
	}
	


	private final AISUB subEat = new AISUB.Simple("drinking") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 2 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			case 3 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 4 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			}
			return null;
		}

		
	};
	

	

	private final class Tavern extends AIPLAN.PLANRES {
		
		private final Resumer walk;
		private final Resumer eat;
		
		Tavern(ROOM_TAVERN e){
			
			walk = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.service(a, d, e.service());
					if (s != null) {
						d.planTile.set(d.path.destX(), d.path.destY());
						e.service().reportDistance(a);
						e.service().reportAccess(a, d.path.destX(), d.path().destY());
						return s;
					}
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return eat.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return true;
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					
				}
			};
			
			eat = new Resumer(e.service().verb) {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					FSERVICE f = e.service().service(d.planTile.x(), d.planTile.y());
					f.startUsing();
					d.planByte1 = (byte) (STATS.FOOD().DRINK.decree().get(a) + (STATS.MULTIPLIERS().DRINK.markIs(a) ? 1 : 0));
					d.planByte2 = 0;
					return subEat.activate(a, d);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					FSERVICE f = e.service().service(d.planTile.x(), d.planTile.y());
					f.consume();
					GAME.player().res().outConsumed.inc(RESOURCES.ALCOHOL(), 1);
					
					STATS.NEEDS().THIRST.fix(a.indu());
					d.planByte2 ++;
					if (d.planByte2 < d.planByte1 && f.findableReservedCanBe()) {
						f.findableReserve();
						f.startUsing();
						return subEat.activate(a, d);
					}
					
					int am = d.planByte2;
					if (STATS.MULTIPLIERS().DRINK.markIs(a)) {
						STATS.MULTIPLIERS().DRINK.consume(a);
						am--;
					}
					if (am >= 0)
						STATS.FOOD().DRINK.indu().set(a.indu(), am);
					
					if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
						return d.resumeOtherPlan(a, beDrunk);
					return null;
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					FSERVICE f = e.service().service(d.planTile.x(), d.planTile.y());
					return f != null && f.findableReservedIs();
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					FSERVICE f = e.service().service(d.planTile.x(), d.planTile.y());
					if ( f != null && f.findableReservedIs())
						f.findableReserveCancel();
				}
			};
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
	}
	
	private final AIPLAN drink = new AIPLAN.PLANRES(){
		
		private final CharSequence sDrunk = D.g("Drinking");

		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(sDrunk) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				long bits = RESOURCES.DRINKS().mask & STATS.FOOD().fetchMask(a);
				return AI.SUBS().walkTo.resource(a, d, bits);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
	
				
				GAME.player().res().outConsumed.inc(d.resourceCarried(), 1);
				STATS.NEEDS().THIRST.fix(a.indu());
				STATS.FOOD().DRINK.indu().set(a.indu(), 1);
				if (STATS.MULTIPLIERS().DRINK.markIs(a)) {
					STATS.MULTIPLIERS().DRINK.consume(a);
				}
				
				return drink.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}

		};
		
		private final Resumer drink = new Resumer(sDrunk) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				if (RND.rBoolean())
					return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
				d.resourceCarriedSet(null);
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.fist, RND.rFloat()*4);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
				if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
					return d.resumeOtherPlan(a, beDrunk);
				return null;
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
		};
		
	};
	
	private final AIPLAN beDrunk = new AIPLAN.PLANRES(){
		
		private final CharSequence sDrunk = D.g("intoxicated");
		private final CharSequence sSobering = D.g("sobering", "Sobering Up");

		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkWeird.set(a, d);
		}
		
		private final AISUB walk = new AISUB.Simple() {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				if (d.subByte == 0) {
					a.speed.turnRandom();
					AISTATE s =  AI.STATES().WALK.activate(a, d, 4+RND.rInt(5));
					a.speed.magnitudeTargetSet(0.2);
					a.speed.setDirCurrent(DIR.ALL.rnd());
					d.subByte = 1;
					return s;
				}
				a.speed.magnitudeInit(0);
				a.speed.magnitudeTargetSet(0);
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_TILE) {
					return true;
				}
				return super.event(a, d, e);
			};
		};
		
		private final Resumer walkWeird = new Resumer(sDrunk) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return walk.activate(a, d);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				if (RND.rBoolean()) {
					return setAction(a, d);
				}else if (RND.oneIn(4)) {
					return sleep.set(a, d);
				}else {
					return drink.set(a, d);
				}
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_TILE) {
					return true;
				}
				return super.event(a, d, e);
			};

		};
		
		private final Resumer drink = new Resumer(sDrunk) {
			
			private final Animation[] animi = new Animation[] {
				AI.STATES().anima.grab,
				AI.STATES().anima.box,
				AI.STATES().anima.fist,
				AI.STATES().anima.work,
			};
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				if (RND.rBoolean())
					return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
				return AI.SUBS().single.activate(a, d, animi[RND.rInt(animi.length)], RND.rFloat()*4);
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (RND.oneIn(8))
					return sleep.set(a, d);
				return setAction(a, d);
				
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
		};
		
		private final Resumer sleep = new Resumer(sSobering) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 20+RND.rInt(40));
			};
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceCarriedSet(null);
			}
		};


		
		
	};
	
}
