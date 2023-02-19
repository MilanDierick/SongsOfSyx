package settlement.entity.humanoid.ai.battle;

import static settlement.main.SETT.*;

import init.C;
import init.boostable.BOOSTABLES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.rnd.RND;

public class SubFight extends AISUB.Resumable {

	private final AISTATES.STOP stopBraced = new AISTATES.STOP(HSprites.SWORD_STAND);

	private final double stopMom = 0.5;
	private static final VectorImp vec = new VectorImp();

	public SubFight() {
		super("fighting");
	}

	AISubActivation initReady(AIManager d, Humanoid a, ENTITY other, double norX, double norY, double faceDot,
			double momentum) {

		d.otherEntitySet((Humanoid) other);
		if (momentum > stopMom) {
			return activate(a, d, stop);
		} else if (faceDot > 0.5) {
			return activate(a, d, strike);
		} else {
			return activate(a, d, beBraced);
		}
	}

	@Override
	protected AISTATE init(Humanoid a, AIManager d) {
		if (d.otherEntity() != null)
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
		return stop.set(a, d);
	}

	private final Resumer stop = new ResumerB() {

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch (e.type) {
			case IMPACT_DAMAGE:
				return 0;
			default:
				return super.poll(a, d, e);
			}
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch (e.event) {
			case COLLISION_SOFT:

				if (d.otherEntity() != null)
					a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				else if (e.other != null)
					a.speed.setDirCurrent(DIR.get(a.body(), e.other.body()));
				return false;
			case COLLISION_TILE:
				if (d.otherEntity() != null)
					a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				else
					a.speed.setDirCurrent(DIR.get(-e.norX, -e.norY));
				return false;
			case MEET_ENEMY:
				d.otherEntitySet((Humanoid) e.other);
				a.speed.setDirCurrent(DIR.get(-e.norX, -e.norY));
				return false;
			default:
				return super.event(a, d, e);
			}

		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (a.speed.isZero())
				return findFooting.set(a, d);
			return AI.STATES().STOP.activate(a, d);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return findFooting.set(a, d);
		}
	};

	private final Resumer findFooting = new Resumer() {

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.IMPACT_DAMAGE)
				return 0;
			return super.poll(a, d, e);
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (d.otherEntity() != null)
				a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return AI.STATES().STAND.activate(a, d, 0.1 + Util.getTraining(a, d) * 2.0f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}

	};

	private final Resumer beBraced = new ResumerB() {

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (d.otherEntity() == null)
				return exit.set(a, d);
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return stopBraced.activate(a, d, RND.rFloat() * 2 + Util.getTraining(a, d) * 2.0f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			Humanoid ene = d.otherEntity();
			
			
			if (ene == null || ene.isRemoved()) {
				return exit.set(a, d);
			}

			
			
			AISTATE s = escape.set(a, d);
			if (s != null)
				return s;

			int dist = a.body().getDistance(ene.body());
			if (dist > C.TILE_SIZE * 10)
				return exit.set(a, d);

			if (dist <= (a.body().width() + ene.body().width()) / 2 + 6)
				return backup.set(a, d);

			if (dist > C.TILE_SIZE * 4)
				return charge.set(a, d);

			if (dist > (a.body().width() + a.body().width()) / 2 + 8)
				return move_closer.set(a, d);
			return strike.set(a, d);
		}
	
	};

	private final Resumer escape = new ResumerB() {
		
		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			
			if (!Util.shouldMoveIntoDivPosition(a, d))
				return null;

			double m = vec.set(a.body().cX(), a.body().cY(), a.division().reporter.getPixel(a.divSpot()));
			if (m > C.TILE_SIZE*2) {
				return AI.STATES().STAND_SWORD.activate(a, d, 0.1);
			}
			boolean can = true;
			if (m > C.TILE_SIZE) {
				for (int i = 1; i < 3 && m > 0; i++) {
					int tx = ((int) (a.body().cX() + vec.nX() * C.TILE_SIZE * i)) >> C.T_SCROLL;
					int ty = ((int) (a.body().cY() + vec.nY() * C.TILE_SIZE * i)) >> C.T_SCROLL;
					if (!IN_BOUNDS(tx, ty) || SETT.PATH().finders().entity.getEnemies(a, tx, ty) > 0) {
						can = false;
						break;
					}
					m -= C.TILE_SIZE;
				}
			}
			if (can) {
				
				AISTATE s = AI.STATES().WALK2_SWORD.free(a, d, (int)(a.body().cX()+vec.nX()*C.TILE_SIZE*2), (int)(a.body().cY()+vec.nY()*C.TILE_SIZE*2));
				a.speed.setPrevDir();
				return s;
			}
			
			Humanoid ene = d.otherEntity();
			
			vec.set(ene.body().cX(), ene.body().cY(), a.body().cX(), a.body().cY());
			int tx = ((int) (a.body().cX() + vec.nX() * C.TILE_SIZE)) >> C.T_SCROLL;
			int ty = ((int) (a.body().cY() + vec.nY() * C.TILE_SIZE)) >> C.T_SCROLL;
			if (IN_BOUNDS(tx, ty) && SETT.PATH().finders().entity.getEnemies(a, tx, ty) <= 0) {
				AISTATE s = AI.STATES().WALK2_SWORD.free(a, d, (int)(a.body().cX()+vec.nX()*C.TILE_SIZE*2), (int)(a.body().cY()+vec.nY()*C.TILE_SIZE*2));
				a.speed.setPrevDir();
				return s;
			}
			
			return null;

		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				d.otherEntitySet(null);
				a.speed.setPrevDir();
				d.overwrite(a, AI.STATES().STAND_SWORD.activate(a, d, 0.1));
				return true;
			}
			return super.event(a, d, e);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return null;
		}
	};

	private final Resumer move_closer = new ResumerB() {
		private final AISTATES.WALK state = new AISTATES.WALK(0.2, HSprites.SWORD_STAND);

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return state.activate(a, d, 1 + RND.rFloat(1), d.otherEntity());
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				d.otherEntitySet(null);
				d.overwrite(a, stop.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.DEFENCE)
				return super.poll(a, d, e)*1.5;
			return super.poll(a, d, e);
		}

	};

	private final Resumer charge = new ResumerB() {
		private final AISTATES.WALK state = new AISTATES.WALK(0.9, HSprites.SWORD_STAND);

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return state.activate(a, d, 1 + RND.rFloat(1), d.otherEntity());
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				d.otherEntitySet(null);
				d.overwrite(a, stop.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}

	};

	private final Resumer strike = new ResumerB() {
		private final double offMom = 0.25;
		private final AISTATE state = new AISTATE.Custom("striking", HSprites.SWORD_OUT) {
			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.speed.magnitudeAdjust(ds, 1.5, 1.0);
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};
		private final float time = (float) HSprites.SWORD_OUT.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {

			if (d.otherEntity() != null) {
				a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				if (RND.rInt(3) == 0)
					return charge.set(a, d);
			}
			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
			return state;
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.MEET_ENEMY) {
				if (e.speedHasChanged)
					a.speed.setPrevDir();
				if (e.momentum > offMom)
					return super.event(a, d, e);
				return false;
			} else {
				return super.event(a, d, e);
			}
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return strike2.set(a, d);
		}

	};

	private final Resumer strike2 = new ResumerB() {

		private final AISTATE state = new AISTATE.Custom("striking", HSprites.SWORD_IN) {
			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.speed.magnitudeAdjust(ds, 1.5, 1.0);
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};
		private final double offMom = 0.25;
		private final float time = (float) HSprites.SWORD_IN.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			
			if (d.otherEntity() != null) {
				int dist = a.body().getDistance(d.otherEntity().body());
				if (dist <= (a.body().width() + d.otherEntity().body().width()) / 2 + 24) {
					Humanoid enemy = d.otherEntity();
					
					Util.attack(a, d, enemy);
					
				}
			}

			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
		
			return state;
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.MEET_ENEMY) {
				if (e.speedHasChanged)
					a.speed.setPrevDir();
				if (e.momentum > offMom)
					return super.event(a, d, e);
				return false;
			}
			return super.event(a, d, e);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}

	};

	private final Resumer backup = new ResumerB() {

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE && RND.rBoolean()) {

				a.speed.turn90();
				if (RND.rBoolean()) {
					a.speed.turn90();
					a.speed.turn90();
				}
				return true;
			} else if (e.event == HEvent.COLLISION_SOFT) {
				a.speed.setPrevDir();
				beBraced.set(a, d);
				return false;
			} else {
				return super.event(a, d, e);
			}
		}

		private final AISTATE state = new AISTATE.Custom("backing up", HSprites.SWORD_STAND) {
			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.speed.magnitudeAdjust(ds, 1.0, 1.0);
				// int dist = a.body().getDistance(d.otherEntity().body());
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}
		};

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			d.stateTimer = 0.25f + RND.rFloat(0.5);
			a.speed.turn2(a.body(), d.otherEntity().body()).turn90().turn90();
			a.speed.setDirCurrent(a.speed.dir().perpendicular());
			a.speed.magnitudeTargetSet(0.3);
			return state;
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.DEFENCE)
				return super.poll(a, d, e)*1.5;
			return super.poll(a, d, e);
		}
	};

	private final Resumer exit = new ResumerB() {

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			return stopBraced.activate(a, d, 1.5f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			d.otherEntitySet(null);
			return null;
		}
	};

	private abstract class ResumerB extends Resumer {

		private final double stopMom = 0.5;
	
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch (e.event) {
			case COLLISION_SOFT:
				a.speed.setPrevDir();
				if (e.momentum > stopMom) {
					d.overwrite(a, stop.set(a, d));
				}
				return true;
			case COLLISION_HARD:
				d.otherEntitySet(null);
				return super.event(a, d, e);
			case COLLISION_TILE:
				a.speed.setPrevDir();
				d.overwrite(a, stop.set(a, d));
				return false;
			case MEET_HARMLESS:
				return false;
			case MEET_ENEMY:
				if (e.other.isRemoved()) {
					a.speed.setPrevDir();
					return false;
				}

				d.otherEntitySet((Humanoid) e.other);

				if (e.momentum > stopMom) {
					d.overwrite(a, stop.set(a, d));
				} else if (e.facingDot > 0.5) {
					d.overwrite(a, strike.set(a, d));
				} else if (e.speedHasChanged) {
					a.speed.setPrevDir();
				}
				return false;
			case EXHAUST:
				if (RND.oneIn(BOOSTABLES.PHYSICS().STAMINA.get(a) * 8)) {
					if (STATS.NEEDS().EXHASTION.indu().isMax(a.indu())) {
						d.interrupt(a, e);
						d.overwrite(a, AI.listeners().EXHAUSTED.activate(a, d));
					}
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 1);
				}
				return false;
			default:
				return InterBattle.listener.event(a, d, e);
			}
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch (e.type) {
			case COLLIDING:
				return 1;
			default:
//				if (ready) {
//					return InterBattle.pollReady(a, d, e);
//				}
				return InterBattle.listener.poll(a, d, e);
			}
		}

	}

}
