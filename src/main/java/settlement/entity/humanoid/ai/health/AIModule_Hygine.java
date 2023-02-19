package settlement.entity.humanoid.ai.health;

import static settlement.main.SETT.*;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.service.hygine.bath.*;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;

public final class AIModule_Hygine extends AIModule {

	final AIDataBit suspenderSkinny = AI.bit();
	final AIDataBit suspenderRoom = AI.bit();
	
	private final ArrayList<PlanBath> baths;
	private final ArrayList<PlanWell> wells;

	public AIModule_Hygine() {
		baths = new ArrayList<PlanBath>(ROOMS().BATHS.size());
		for (ROOM_BATH a : ROOMS().BATHS) {
			baths.add(new PlanBath(a));
		}
		wells = new ArrayList<PlanWell>(ROOMS().WELLS.size());
		for (ROOM_WELL a : ROOMS().WELLS) {
			wells.add(new PlanWell(a));
		}
	}

	public AiPlanActivation getHot(Humanoid a, AIManager d) {
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().HYGINE.get(a.indu().clas().index())) {
			s.service().clearAccess(a);
		}
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().HYGINE.get(a.indu().clas().index())) {
			
			if (s.service().room() instanceof ROOM_WELL) {
				if (s.service().accessRequest(a)) {
					AiPlanActivation p = wells.get(s.service().room().typeIndex()).activate(a, d);
					if (p != null)
						return p;
				}
			}
		}
		
		if (SETT.WEATHER().ice.canBatheOutside()) {
			AiPlanActivation p = AI.plans().skinnydip.activate(a, d);
			if (p != null)
				return p;
		}
		return null;
	}
	
	@Override
	protected AiPlanActivation getPlan(Humanoid a, AIManager d) {

		
		for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().HYGINE.get(a.indu().clas().index())) {
			s.service().clearAccess(a);
		}
		
		if (!suspenderRoom.is(d)) {
			for (ROOM_SERVICE_ACCESS_HASER s : a.race().service().HYGINE.get(a.indu().clas().index())) {
				
				if (s.service().room() instanceof ROOM_BATH) {
					if (s.service().accessRequest(a)) {
						AiPlanActivation p = baths.get(s.service().room().typeIndex()).activate(a, d);
						if (p != null)
							return p;
					}
				}else if (s.service().room() instanceof ROOM_WELL) {
					if (s.service().accessRequest(a)) {
						AiPlanActivation p = wells.get(s.service().room().typeIndex()).activate(a, d);
						if (p != null)
							return p;
					}
				}
				
				
				else {
					throw new RuntimeException();
				}
			}
			
		}
		suspenderRoom.set(d, true);
		
		double pp = 1.0-a.race().behaviour.skinnyDips.getD();
		if (!suspenderSkinny.is(d) && SETT.WEATHER().ice.canBatheOutside() && STATS.NEEDS().DIRTINESS.getPrio(a.indu())>pp) {
			AiPlanActivation p = AI.plans().skinnydip.activate(a, d);
			if (p != null)
				return p;
		}
		suspenderSkinny.set(d, true);
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		if (newDay) {
			suspenderSkinny.set(d, false);
			suspenderRoom.set(d, false);
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		double p = STATS.NEEDS().DIRTINESS.getPrio(a.indu());
		
		
		if (!suspenderRoom.is(d)) {
			return (int) Math.ceil(8*p);
			
		}

		if (!suspenderSkinny.is(d) && SETT.WEATHER().ice.canBatheOutside() && 1.0-a.race().behaviour.skinnyDips.getD() >= p)
			return (int) (2);
		
		return 0;
	}
	
	private final AISUB sub = new AISUB.Simple("Bathing") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {

			if (!a.speed.isZero())
				a.speed.magnitudeInit(0);

			d.subByte++;

			if (d.subByte > 20) {
				cancel(a, d);
				return null;
			}

			if (d.subByte == 1) {
				for (DIR dir : DIR.ORTHO) {
					int x = a.physics.tileC().x() + dir.x();
					int y = a.physics.tileC().y() + dir.y();
					if (ROOM_BATH.isPool(x, y))
						return AI.STATES().WALK2.dirTile(a, d, dir);
				}
				d.debug(a, "No bath!");
				return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));
			}

			if (d.subByte > 1) {
				STATS.POP().NAKED.set(a.indu(), 0);
			}

			return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));

		}
		
		@Override
		public boolean event(Humanoid a, AIManager ai, HEventData e) {
			if (e.event == HEvent.MEET_HARMLESS) {
				if (a.speed.isZero()) {
					DIR d = DIR.ORTHO.get(RND.rInt(4));
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						int x = a.physics.tileC().x() + d.x();
						int y = a.physics.tileC().y() + d.y();
						if (ROOM_BATH.isPool(x, y) && !ENTITIES().hasAtTile(a, x, y)) {
							ai.overwrite(a, AI.STATES().WALK2.tile(a, ai, x, y));
						}
						d = d.next(2);
					}

				}
				return false;
			}
			return super.event(a, ai, e);
		};

	};
	
	private class PlanWell extends AIPLAN.PLANRES {
		
		private final ROOM_WELL bath;
		final Resumer walkToWater;
		
		PlanWell(ROOM_WELL bath){
			this.bath = bath;
			
			final Resumer bathe = new Resumer(bath.service().verb) {

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					STATS.POP().NAKED.set(a.indu(), 1);
					bath.service().service(d.planTile.x(), d.planTile.y()).startUsing();
					d.planByte1 = (byte) (1 + RND.rInt(8));
					return AI.SUBS().STAND.activate(a, d);
				}

				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					if(d.planByte1 == 0) {
						STATS.NEEDS().DIRTINESS.fix(a.indu());
						STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
						if (STATS.NEEDS().DIRTINESS.getPrio(a.indu()) == 0) {
							can(a, d);
							return null;
						}
						d.planByte1 = (byte) (1 + RND.rInt(8));
					}
					d.planByte1 --;
					if ((d.planByte1 & 1) == 1) {
						return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
					}else {
						return AI.SUBS().single.activate(a, d, AI.STATES().anima.box, 1+RND.rInt(5));
					}
				}

				@Override
				public boolean con(Humanoid a, AIManager d) {
					FSERVICE s = bath.service().service(d.planTile.x(), d.planTile.y());
					return s != null && s.findableReservedIs();
				}

				@Override
				public void can(Humanoid a, AIManager d) {
					FSERVICE s = bath.service().service(d.planTile.x(), d.planTile.y());
					if (s != null && s.findableReservedIs())
						s.consume();
					STATS.POP().NAKED.set(a.indu(), 0);
				}
			};
			
			walkToWater = new Resumer(bath.service().verb) {

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, bath.service(), STATS.NEEDS().EXPOSURE.inDanger(a.indu()) ? Integer.MAX_VALUE : bath.service().radius);
					if (s == null) {
						return null;
					}
				
					d.planTile.set(d.path.destX(), d.path.destY());
					bath.service().reportDistance(a);
					bath.service().reportAccess(a, d.planTile);
					return s;
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
		}

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkToWater.set(a, d);
		}
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			bath.service().clearAccess(a);
			super.cancel(a, d);
		}
		
	}
	
	private class PlanBath extends AIPLAN.PLANRES {
		
		private final ROOM_BATH bath;
		
		private final Resumer walkToWater;
		private final Resumer bathe;
		private final Resumer walk2Bench;
		private final Resumer relax;
		
		PlanBath(ROOM_BATH bath){
			this.bath = bath;
			
			walkToWater = new Resumer(bath.service().verb) {

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, bath.service());
					if (s == null) {
						return null;
					}
				
					d.planTile.set(d.path.destX(), d.path.destY());
					bath.service().reportDistance(a);
					bath.service().reportAccess(a, d.planTile);
					return s;
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

			bathe = new Resumer(bath.service().verb) {

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					STATS.POP().NAKED.set(a.indu(), 1);
					return sub.activate(a, d);
				}

				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					can(a, d);
					STATS.NEEDS().DIRTINESS.fixMax(a.indu());
					return walk2Bench.set(a, d);
				}

				@Override
				public boolean con(Humanoid a, AIManager d) {
					FINDABLE s = bath.bath(d.planTile.x(), d.planTile.y());
					return s != null && s.findableReservedIs();
				}

				@Override
				public void can(Humanoid a, AIManager d) {
					Bath s = bath.bath(d.planTile.x(), d.planTile.y());
					if (s != null && s.findableReservedIs()) {
						s.consume();
					}
					STATS.POP().NAKED.set(a.indu(), 0);
				}
			};

			walk2Bench = new Resumer(bath.service().verb) {

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					BathInstance b = bath.get(a.physics.tileC().x(), a.physics.tileC().y());
					if (b != null) {
						COORDINATE c = b.getBench();
						if (c != null) {
							STATS.POP().NAKED.set(a.indu(), 1);
							return trySub(a, d, AI.SUBS().walkTo.cooFull(a, d, c), null);
						}
					}
					return null;
				}

				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					return relax.set(a, d);
				}

				@Override
				public boolean con(Humanoid a, AIManager d) {
					return bath.isBench(d.path.destX(), d.path.destY());
				}

				@Override
				public void can(Humanoid a, AIManager d) {
					BathInstance b = bath.get(a.physics.tileC().x(), a.physics.tileC().y());
					if (b != null) {
						b.returnBench(d.path.destX(), d.path.destY());
					}

					STATS.POP().NAKED.set(a.indu(), 0);
				}
			};

			relax = new Resumer(bath.service().verb) {

				final AISUB sub = new AISUB.Simple("Relaxing") {

					@Override
					protected AISTATE resume(Humanoid a, AIManager d) {
						d.subByte++;
						if (d.subByte == 1)
							return AI.STATES().anima.layoff.activate(a, d, 10 + RND.rInt(20));
						return null;
					}
				};

				@Override
				protected AISubActivation setAction(Humanoid a, AIManager d) {
					DIR dir = bath.getBenchDir(a.physics.tileC().x(), a.physics.tileC().y());
					a.speed.setDirCurrent(dir.perpendicular());
					return sub.activate(a, d);
				}

				@Override
				protected AISubActivation res(Humanoid a, AIManager d) {
					can(a, d);
					return null;
				}

				@Override
				public boolean con(Humanoid a, AIManager d) {
					return bath.isBench(d.path.destX(), d.path.destY());
				}

				@Override
				public void can(Humanoid a, AIManager d) {
					BathInstance b = bath.get(a.physics.tileC().x(), a.physics.tileC().y());
					if (b != null) {
						b.returnBench(d.path.destX(), d.path.destY());
					}
					STATS.POP().NAKED.set(a.indu(), 0);
				}
			};
		}
		


		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walkToWater.set(a, d);
		}
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			bath.service().clearAccess(a);
			super.cancel(a, d);
		}
		
	}
	
	public boolean naked(Induvidual i) {
		return naked(i);
	}

}
