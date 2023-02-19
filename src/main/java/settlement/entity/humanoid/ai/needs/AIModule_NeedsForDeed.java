package settlement.entity.humanoid.ai.needs;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.util.FINDABLE;
import settlement.room.service.lavatory.*;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;

public final class AIModule_NeedsForDeed extends AIModule{

	private final ArrayList<Plan> plans;
	
	public AIModule_NeedsForDeed() {
		plans = new ArrayList<Plan>(ROOMS().LAVATORIES.size());
		for (ROOM_LAVATORY a : ROOMS().LAVATORIES) {
			plans.add(new Plan(a));
		}
		
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().SHIT.get(a.indu().clas().index())) {
			s.service().clearAccess(a);
		}
		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().SHIT.get(a.indu().clas().index())) {
			if (s.service().room() instanceof ROOM_LAVATORY) {
				if (s.service().accessRequest(a)) {
					AiPlanActivation p = plans.get(s.service().room().typeIndex()).activate(a, d);
					if (p != null)
						return p;
				}
			}
		}
		
		STATS.NEEDS().CONSTIPATION.fixMax(a.indu());

		return null;
		
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		
		double p = STATS.NEEDS().CONSTIPATION.getPrio(a.indu()); 
		return (int) Math.ceil(p*8);
		
	}
	
	private static class Plan extends AIPLAN.PLANRES {

		private final ROOM_LAVATORY blue;
		final Resumer walkToToilet;
		final Resumer takingDump;
		final Resumer walk2Water;
		final Resumer washing;
		
		Plan(ROOM_LAVATORY blue){
			this.blue = blue;
			
			walkToToilet = new Resumer("Walking to latrine") {
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, blue.service());
					
					if (s == null)
						return null;
					d.planTile.set(d.path.destX(), d.path.destY());
					blue.service().reportDistance(a);
					blue.service().reportAccess(a, d.planTile);
					return s;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return takingDump.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					FINDABLE s = get(a, d);
					return s != null && s.findableReservedIs();
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					
				}
			};
			
			takingDump = new Resumer("Discharging") {
				
				private final AISUB sub = new AISUB.Simple("taking a dump") {
					@Override
					protected AISTATE resume(Humanoid a, AIManager d) {
						d.subByte++;
						
						if (d.subByte > 3 + RND.rInt(5))
							return null;

						if (blue.service().usageSound != null && RND.oneIn(5))
							blue.service().usageSound.rnd(a.physics.body(), 0.5);
						
						return AI.STATES().STAND.activate(a, d, 5f);
					}
				};
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					Lavatory l = get(a, d);
					a.speed.setDirCurrent(l.getDir());
					STATS.NEEDS().CONSTIPATION.fix(a.indu());
					STATS.NEEDS().DIRTINESS.inc(a.indu(), 1);
					return sub.activate(a, d);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					get(a, d).consume();
					return walk2Water.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					FINDABLE s = get(a, d);
					return s != null && s.findableReservedIs();
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					FINDABLE s = get(a, d);
					if (s != null)
						s.findableReserveCancel();
				}
			};
			
			walk2Water = new Resumer("Washing up") {
				
				
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					LavatoryInstance b = blue.get(a.physics.tileC().x(), a.physics.tileC().y());
					if (b != null) {
						COORDINATE c = b.getExtra();
						
						if (c != null) {
							AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
							if (s == null) {
								can(a, d);
								return null;
							}
							return s;
						}
					}
					return null;
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return washing.set(a, d);
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return blue.isExtra(d.path.destX(), d.path.destY());
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					if (blue.isExtra(d.path.destX(), d.path.destY())) {
						LavatoryInstance b = blue.get(d.path.destX(), d.path.destY());
						b.returnExtra(d.path.destX(), d.path.destY());
					}
				}
			};
			
			washing = new Resumer("Washing up") {
				
				private final AISUB sub = new AISUB.Simple("washing") {
					@Override
					protected AISTATE resume(Humanoid a, AIManager d) {
						d.subByte++;
						
						if (d.subByte > 1)
							return null;
						
						return AI.STATES().anima.box.activate(a, d, 15f);
					}
				};
				
				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					return sub.activate(a, d);
				}
				
				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					can(a, d);
					STATS.NEEDS().DIRTINESS.inc(a.indu(), -1);
					return null;
				}
				
				@Override
				public boolean con(Humanoid a, AIManager d) {
					return blue.isExtra(d.path.destX(), d.path.destY());
				}
				
				@Override
				public void can(Humanoid a, AIManager d) {
					if (blue.isExtra(d.path.destX(), d.path.destY())) {
						LavatoryInstance b = blue.get(d.path.destX(), d.path.destY());
						b.returnExtra(d.path.destX(), d.path.destY());
					}
				}
			};
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkToToilet.set(a, d);
		}
		

		private Lavatory get(Humanoid a, AIManager d) {
			return blue.getService(d.planTile.x(), d.planTile.y());
		}
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			blue.service().clearAccess(a);
			super.cancel(a, d);
		}
		
		
		
	}

}
