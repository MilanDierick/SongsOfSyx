package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.*;

import init.D;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.main.throne.THRONE;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

public final class AIPlans {

	private static CharSequence ¤¤swimming = "¤Swimming";
	private static CharSequence ¤¤trapped = "¤Cut off from the throne";
	private static CharSequence ¤¤skinnydipping = "¤Skinny dipping";
	private static CharSequence ¤¤WalkingToCentre = "¤Walking to the center of the city";
	private static CharSequence ¤¤Fleeing = "¤Fleeing";
	
	static {
		D.ts(AIPlans.class);
	}
	
	
	public final AIPLAN unreachable = new AIPLAN.PLANRES() {
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			
			if (SETT.PATH().finders().reachable.find(a.tc(), d.path, 8)) {
				return path.set(a, d);
			}
			
			STATS.POP().TRAPPED.indu().set(a.indu(), 1);
			
			if (a.inWater && TERRAIN().WATER.DEEP.is(a.tc()))
				return drowning.set(a, d);
			else {
				return start.set(a, d);
			}

			
		}

		private final Resumer path = new Resumer(¤¤trapped) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.path(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (a.inWater && TERRAIN().WATER.DEEP.is(a.tc()))
					return drowning.set(a, d);
				if (!PATH().connectivity.is(a.physics.tileC()))
					AIManager.dead = CAUSE_LEAVE.OTHER;
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
		
		private final Resumer drowning = new Resumer(¤¤swimming) {

			private AISUB sub = new AISUB.Simple("") {
				
				@Override
				public AISTATE resume(Humanoid a, AIManager d) {
					
					
					
					if (d.subByte > 20 && RND.oneIn(3))
						return null;
					else
						d.subByte++;
						
					return AI.STATES().STAND.aDirRND(a, d, (float) (0.2+RND.rFloat(0.3)));
				}
			};
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return sub.activate(a, d);
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				AIManager.dead = CAUSE_LEAVE.DROWNED;
				return null;
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
		
		private AISUB sub = new AISUB.Simple("") {

			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte != 1)
					return null;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR dir = DIR.ALL.get(di);
					if (PATH().connectivity.is(a.tc(), dir)) {
						if (!dir.isOrtho() 
								&& !PATH().availability.get(a.tc().x()+dir.x(), a.tc().y()).tileCollide
							&& !PATH().availability.get(a.tc().x(), a.tc().y()+dir.y()).tileCollide) {
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}else if(dir.isOrtho() && !PATH().availability.get(a.tc().x()+dir.x(), a.tc().y()+dir.y()).tileCollide){
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
					}
					
				}
				switch(RND.rInt(3)) {
				case 0:return AI.STATES().STAND.activate(a, d, 0.5f+RND.rFloat(5));
				case 1:return AI.STATES().anima.wave.activate(a, d, 0.5f+RND.rFloat(5));
				}
				if (a.indu().hType().hostile && a.division() != null) {
					a.setDivision(null);
					STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
				}
				return AI.STATES().anima.box.activate(a, d, 0.5f+RND.rFloat(5));
			}
			
		};

		private final Resumer start = new Resumer(¤¤trapped) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 0;
				return AI.SUBS().STAND.activate(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				d.planByte1++;
				if (d.planByte1 > 5)
					return null;
				if (PATH().connectivity.is(a.physics.tileC())) {
//					STATS.POP().TRAPPED.indu().set(a.indu(), 0);
//					if (a.division() != null)
//						a.division().reporter.reportReachable(a.divSpot(), true);
					return null;
				}
//				
//				if (a.division() != null)
//					a.division().reporter.reportReachable(a.divSpot(), false);
				return sub.activate(a, d);
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
		protected void cancel(Humanoid a, AIManager d) {
			super.cancel(a, d);
			//STATS.POP().TRAPPED.indu().set(a.indu(), 0);
		};
	}; 
	
	
	public final AIPLAN NOP = new AIPLAN.PLANRES() {


		private final Resumer start = new Resumer("") {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activateTime(a, d, 0);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
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
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
	};
	
	public final AIPLAN skinnydip = new AIPLAN.PLANRES() {

		private final AISUB sub = new AISUB.Simple("") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {

				d.subByte++;

				if (!a.speed.isZero())
					a.speed.magnitudeInit(0);

				if (d.subByte > 1) {
					STATS.NEEDS().DIRTINESS.fix(a.indu());
				}

				if (d.subByte > 20)
					return null;

				if (RND.oneIn(5)) {

					DIR dir = DIR.ALL.get(RND.rInt(DIR.ALL.size()));

					for (int i = 0; i < 8; i++) {
						int x = a.physics.tileC().x() + dir.x();
						int y = a.physics.tileC().y() + dir.y();
						if (SETT.PATH().coster.player.getCost(a.tc().x(), a.tc().y(), x, y) > 0 && TERRAIN().WATER.isService(x, y)) {
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
						dir = dir.next(1);
					}

				}

				if (RND.oneIn(3))
					return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));
				return AI.STATES().LAY.activate(a, d, 1 + RND.rFloat(5));
			}
		};

		private final Resumer walkToWater = new Resumer(¤¤skinnydipping) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.water, 100);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return bathe.set(a, d);
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {

			}
		};

		private final Resumer bathe = new Resumer(¤¤skinnydipping) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				STATS.POP().NAKED.set(a.indu(), 1);
				STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
				return sub.activate(a, d);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				can(a, d);
				STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
				STATS.NEEDS().DIRTINESS.fix(a.indu());
				return null;
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				return s != null && s.findableReservedIs();
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				if (s != null)
					s.findableReserveCancel();
				STATS.POP().NAKED.set(a.indu(), 0);
			}
		};

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkToWater.set(a, d);
		}
	};
	
	final AIPLAN dead = new AIPLAN.PLANRES() {
		
		private final AISUB dead = new AISUB.Simple("dead") {
			
			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				return AI.STATES().STAND.activate(a, d, 100);
			}
		};
		
		final Resumer r = new Resumer("dead") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return dead.activate(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return dead.activate(a, d);
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
		public AISubActivation init(Humanoid a, AIManager d) {
			return r.set(a, d);
		}
	};
	
	final AIPLAN GoToThrone = new AIPLAN.PLANRES() {
		
		final Resumer go = new Resumer(¤¤WalkingToCentre) {

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				if (a.physics.tileC().tileDistanceTo(THRONE.coo()) < 30)
					return AI.SUBS().STAND.activate(a, d);
				COORDINATE c = PATH().finders.arround.find(THRONE.coo().x(), THRONE.coo().y(), 5, 20);
				if (c == null)
					return AI.SUBS().STAND.activate(a, d);
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
				return trySub(a, d, s, null);
			}

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
				
				
			}
			
		};
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			return go.set(a, d);
		}
	};
	
	final AIPLAN runToSafety = new AIPLAN.PLANRES(){

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return pathing.set(a, d);
		}
		
		private final Resumer pathing = new Resumer(¤¤Fleeing) {
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_SOFT) {
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							return true;
						}
					}
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, run.set(a, d));
					return true;
				}else if (e.event == HEvent.MEET_ENEMY) {
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, run.set(a, d));
				}else if (e.event == HEvent.COLLISION_TILE) {
					d.overwrite(a, run.set(a, d));
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.EXHAUST) {
					return super.event(a, d, e);
				}else if (e.event == HEvent.COLLISION_HARD) {
					return super.event(a, d, e);
				}
				return false;
			}
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (PATH().finders.entity.findSafety(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, 100)) {
					return AI.SUBS().walkTo.pathRun(a, d);
				}
				a.speed.turnWithAngel(RND.rFloat0(90));
				
				return run.set(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
					return null;
				if (ARMIES().enemy().men() > 0) {
					a.speed.turnWithAngel(RND.rInt0(90));
					return AI.SUBS().STAND.activateTime(a, d, 3+RND.rInt(3));
					
				}
				return null;
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
		
		private final Resumer run = new Resumer(¤¤Fleeing) {
			
			private final AISUB sub = new AISUB.Simple() {
				@Override
				protected AISTATE resume(Humanoid a, AIManager d) {
					
					d.planByte1--;
					
					if (d.planByte1 > 0)
						return AI.STATES().RUN.activate(a, d, 4f + RND.rFloat()*3);
					return null;
				}
				
				@Override
				protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
					d.planByte1 = 0;
					return AI.STATES().RUN.activate(a, d, 4f + RND.rFloat()*3);
				};
			};
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = (byte) (2 + RND.rInt(5));
				d.planByte1 = (byte) (2 + RND.rInt(5));
				return sub.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
					return null;
				if (ARMIES().enemy().men() > 0) {
					return pathing.set(a, d);
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
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				// TODO Auto-generated method stub
				return super.poll(a, d, e);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_SOFT) {
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							d.overwrite(a, AI.STATES().STOP.activate(a, d));
						}
					}
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.MEET_ENEMY) {
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
				}else if (e.event == HEvent.COLLISION_TILE) {
					d.planByte2--;
					if (d.planByte2 < 0) {
						d.overwrite(a, pathing.set(a, d));
						return true;
					}
					
					
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.EXHAUST) {
					return super.event(a, d, e);
				}else if (e.event == HEvent.COLLISION_HARD) {
					return super.event(a, d, e);
				}
				return false;
			}
		};

	};

	
	
}
