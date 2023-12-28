package settlement.entity.humanoid.ai.main;

import game.GAME;
import game.boosting.BOOSTABLES;
import init.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.thing.DRAGGABLE;
import settlement.thing.DRAGGABLE.DRAGGABLE_HOLDER;
import snake2d.LOG;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

public class AISTATES {

	public final STOP STAND_SWORD = new STOP(HSprites.SWORD_STAND);
	public final STOP STAND = new STOP(HSprites.STAND);
	public final MOVE_TOWARDS MOVE_TO = new MOVE_TOWARDS();
	public final MOVE_TOWARDS MOVE_TO_SWORD = new MOVE_TOWARDS(HSprites.SWORD_STAND);
	public final WALK WALK = new WALK(0.6);
	public final WALK RUN = new WALK(1.0);
	public final STOP STOP = new STOP(HSprites.STAND);
	public final WALK_DEST WALK2 = new WALK_DEST(0.6);
	public final WALK_DEST RUN2 = new WALK_DEST(0.9);
	public final WALK_DEST DRAG = new WALK_DEST(true);
	public final WALK_DEST WALK2_SWORD = new WALK_DEST(0.4, HSprites.SWORD_STAND);
	public final Animation WORK = new Animation("working", HSprites.TOOL_HIT);
	public final SLEEP SLEEP = new SLEEP();
	public final Animations anima = new Animations();
	public final AnimationArrays animaArr = new AnimationArrays();
	public final STOP layStop = new STOP(HSprites.LAY);
	public final Animation LAY = new Animation("laying", HSprites.LAY);
	public final FLY FLY = new FLY();

	public class SLEEP {

		private SLEEP() {

		}

		public AISTATE activate(Humanoid a, AIManager d, float time) {
			d.stateTimer = time;
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
			if (!a.speed.dir().isOrtho())
				a.speed.setDirCurrent(a.speed.dir().next(1));
			return state;
		};

		private final AISTATE state = new AISTATE("sleeping") {

			@Override
			public HSprite sprite(Humanoid a) {
				return HSprites.SLEEP;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				d.stateTimer -= ds;
				if (d.stateTimer <= 0) {
					return false;
				}
				return true;
			}

		};

	}

	public static class WALK {

		private final double target;
		private final HSprite sprite;

		public WALK(double target) {
			this(target, HSprites.MOVE);
		}

		public WALK(double target, HSprite sprite) {
			this.target = target;
			this.sprite = sprite;
		}

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			d.stateTimer = (float) time;
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return state;
		};

		AISTATE activate(Humanoid a, AIManager d, float time, float x, float y) {
			a.speed.turn2(x, y).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		public AISTATE activate(Humanoid a, AIManager d, float time, ENTITY other) {
			a.speed.turn2(a.body(), other.body()).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}
		
		public AISTATE activateFRom(Humanoid a, AIManager d, float time, ENTITY other) {
			a.speed.turn2(other.body(), a.body()).magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		AISTATE activate(Humanoid a, AIManager d, float time, double deg) {
			a.speed.turnWithAngel(deg);
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		AISTATE activateRND(Humanoid a, AIManager d, float time) {
			a.speed.turnRandom();
			a.speed.magnitudeTargetSet(target + RND.rFloat(0.1));
			return activate(a, d, time);
		}

		private final AISTATE state = new AISTATE("walking") {

			@Override
			public HSprite sprite(Humanoid a) {
				return sprite;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.speed.magnitudeAdjust(ds, 1.0, 1.0);
				// a.stats.staminaIncrement(-0.3f*ds);
				d.stateTimer -= ds;
				return d.stateTimer > 0;
			}

		};

	}

	public static class STOP {

		private final HSprite s;

		public STOP(HSprite s) {
			this.s = s;
		}

		public AISTATE activate(Humanoid a, AIManager d) {
			d.stateTimer = 0.1f;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};
		
		public AISTATE instant(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			d.stateTimer = 0.1f;
			return state;
		};
		
		public AISTATE aDirRND(Humanoid a, AIManager d, float time) {
			a.speed.turnRandom();
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			d.stateTimer = (float) time;
			a.speed.magnitudeTargetSet(0);
//			a.speed.magnitudeAdjust(0.25f, 1, 1);
			return state;
		};

		private final AISTATE state = new AISTATE("stopping") {

			@Override
			public HSprite sprite(Humanoid a) {
				return a.speed.magnitude() >= a.speed.magintudeMax() ? HSprites.LAY : s;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				
				if (a.speed.isZero()) {
					d.stateTimer -= ds;
					return d.stateTimer > 0;
				}
				a.speed.brake(ds);
				return true;
			}

		};
		
		
	}
	
	

	public class FLY {

		private FLY() {

		}

		public AISTATE activate(Humanoid a, AIManager d, float seconds) {
			d.stateTimer = seconds / 2f;
			return state;
		};
		
		public AISTATE add(Humanoid a, AIManager d, float seconds) {
			d.stateTimer += seconds / 2f;
			return state;
		};

		private final AISTATE state = new AISTATE("flying") {

			@Override
			public HSprite sprite(Humanoid a) {
				return HSprites.LAY;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.physics.setHeightOverGround(a.physics.getZ() + ds * d.stateTimer * C.TILE_SIZE);
				d.stateTimer -= ds;
				if (a.physics.getZ() < 0) {
					a.physics.setHeightOverGround(0);
					return false;
				}
				return true;
			}

		};

	}

	public class MOVE_TOWARDS {

		private final HSprite sprite;
		
		private final AISTATE state = new AISTATE("walking") {

			@Override
			public HSprite sprite(Humanoid a) {
				return sprite;
			}

			@Override
			public boolean update(Humanoid a, AIManager d, float ds) {
				a.speed.magnitudeAdjust(ds, 1.0, 1.0);
				float dx = d.X - a.body().cX();
				float dy = d.Y - a.body().cY();

				if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
					a.physics.body().moveC(d.X, d.Y);
					return false;
				} else if (dx == 0 && dy == 0) {
					return false;
				}
				d.stateTimer -= ds;
				if (d.stateTimer <= 0) {
					return false;
				}
				return true;
			}

		};

		private MOVE_TOWARDS() {
			this(HSprites.MOVE);
		}
		
		public MOVE_TOWARDS(HSprite sprite) {
			this.sprite = sprite;
		}

		public AISTATE move(Humanoid a, AIManager d, int destX, int destY, double time, double speed) {
			d.X = destX;
			d.Y = destY;
			if (d.X != a.physics.body().cX() || d.Y != a.physics.body().cY()) {
				a.speed.turn2(a, d.X, d.Y).magnitudeTargetSetPrecise(speed);

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (float) time;
			return state;
		}

	}
	
	public static class WALK_DEST {

		private final AISTATE state;
		private final double speed;

		public WALK_DEST(double speed, HSprite sprite) {
			this.speed = speed;

			state = new AISTATE("walking") {

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, float ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					float dx = d.X - a.body().cX();
					float dy = d.Y - a.body().cY();

					if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
						a.physics.body().moveC(d.X, d.Y);
						return false;
					} else if (dx == 0 && dy == 0) {
						return false;
					}
					d.stateTimer -= ds;
					if (d.stateTimer <= 0) {
						LOG.ln(ds + " " + a.tc() + " " + a.body().cX() + " " + d.X + " " + a.speed.nX() + " " + a.speed.magnitude() + " " + a.speed.magintudeMax() + " " + BOOSTABLES.PHYSICS().SPEED.get(a.indu())*C.TILE_SIZE);
//						GAME.Notify(a.speed.magnitude() + " " + a.physics.tileC() + " " + d.path + " " + a.speed.nX()
//								+ " " + a.speed.nY());
						d.stateTimer = (10f);
						a.physics.body().moveC(d.X, d.Y);
						return false;
					}
					return true;
				}

			};
		}
		
		private WALK_DEST(double speed) {
			this(speed, HSprites.MOVE);
		}
		
		private WALK_DEST(boolean shittycoding) {
			this.speed = 0.3;
			state = new AISTATE("dragging") {

				@Override
				public HSprite sprite(Humanoid a) {
					return HSprites.DRAG;
				}

				@Override
				public boolean update(Humanoid a, AIManager d, float ds) {
					a.speed.magnitudeAdjust(ds, 1.0, 1.0);
					float dx = d.X - a.body().cX();
					float dy = d.Y - a.body().cY();

					if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
						a.physics.body().moveC(d.X, d.Y);
						return false;
					} else if (dx == 0 && dy == 0) {
						return false;
					}
					d.stateTimer -= ds;
					if (d.stateTimer <= 0) {
						GAME.Notify(a.speed.magnitude() + " " + a.physics.tileC() + " " + d.path + " " + a.speed.nX()
								+ " " + a.speed.nY());
						d.stateTimer = (48f);
					}
					DRAGGABLE c = DRAGGABLE_HOLDER.all().get(d.subPathByte).draggable((short) d.planObject);
					if (c != null) {
						if (c.canBeDragged())
							c.drag(a.speed.dir(), a.physics.body().cX(), a.physics.body().cY(), a.physics.body().width()<<1);
					}else {
						d.debug(a, "draggable has mysteriously dissapeared!");
						
					}
					
					return true;
				}

			};
		}

		public AISTATE cTile(Humanoid a, AIManager d) {
			int x2 = (a.physics.tileC().x() << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (a.physics.tileC().y() << C.T_SCROLL) + C.TILE_SIZEH;
			return free(a, d, x2, y2);
		};

//		public AISTATE path(Humanoid a, AIManager d) {
//			int x2 = d.path.getSettCX();
//			int y2 = d.path.getSettCY();
//			if (d.path.isDest()) {
//				int dd = (C.TILE_SIZE - a.species().physics.hitBoxsize() - 2) / 2;
//				if (d.path.isFull()) {
//					if (dd > 3)
//						dd = 3;
//					x2 += RND.rInt0(dd);
//					y2 += RND.rInt0(dd);
//				} else {
//					int dy = d.path.destY() - d.path.y();
//					int dx = d.path.destX() - d.path.x();
//					x2 += dx * dd;
//					y2 += dy * dd;
//				}
//			}
//			return free(a, d, x2, y2);
//		};
		//natural pathing
		public AISTATE path(Humanoid a, AIManager d) {

			int x2 = d.path.getSettCX();
			int y2 = d.path.getSettCY();
			
			
			
			if (x2 == a.physics.body().cX() && y2 == a.physics.body().cY()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			if (a.physics.tileC().isSameAs(d.path) && a.physics.isWithinTile()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			
			a.speed.turn2(a, x2, y2);
			double dy = Math.max(
					(a.body().y2()+2)-(d.path.y()+1)*C.TILE_SIZE, 
					d.path.y()*C.TILE_SIZE - (a.body().y1()-2))
					/ Math.abs(a.speed.nY());
			double dx = Math.max(
					(a.body().x2()+2)-(d.path.x()+1)*C.TILE_SIZE, 
					d.path.x()*C.TILE_SIZE - (a.body().x1()-2)) 
					/ Math.abs(a.speed.nX());
			
			if (dx > dy) {
				x2 =  (int) (a.physics.body().cX() + a.speed.nX()*dx);
				y2 = (int) (a.physics.body().cY() + a.speed.nY()*dx);
			}else {
				x2 =  (int) (a.physics.body().cX() + a.speed.nX()*dy);
				y2 = (int) (a.physics.body().cY() + a.speed.nY()*dy);
			}
			d.X = x2;
			d.Y = y2;
			a.speed.turn2(a, x2, y2);
			a.speed.magnitudeTargetSet(speed + RND.rFloat(0.05));
			d.stateTimer = (48f);
			return state;
			
			
			
		};
		
		public AISTATE edge(Humanoid a, AIManager d, DIR dir) {

			int dd = (C.TILE_SIZE - a.race().physics.hitBoxsize() - 1) / 2;
			
			int x2 = a.physics.tileC().x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*dd;
			int y2 = a.physics.tileC().y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*dd;
			
			if (x2 == a.physics.body().cX() && y2 == a.physics.body().cY()) {
				return free(a, d, a.physics.body().cX(), a.physics.body().cY());
			}
			if (!a.physics.isWithinTile()) {
				return cTile(a, d);
			}
			
			free(a, d, x2, y2);
			a.speed.magnitudeTargetSet(0.2);
			d.stateTimer = (48f);
			return state;
			
			
			
		};
		
		public AISTATE moveToEdge(Humanoid a, AIManager d, DIR dir) {

			int x2 = (a.tc().x())*C.TILE_SIZE + C.TILE_SIZEH;
			int y2 = (a.tc().y())*C.TILE_SIZE + C.TILE_SIZEH;
			int dd = ((C.TILE_SIZE - a.race().physics.hitBoxsize())-2) / 2;
			x2 += dir.x() * dd;
			y2 += dir.y() * dd;
			
			return free(a, d, x2, y2);
			
			
			
		};

		public AISTATE dirTile(Humanoid a, AIManager d, DIR dir) {
			int x2 = (a.physics.tileC().x() << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (a.physics.tileC().y() << C.T_SCROLL) + C.TILE_SIZEH;
			x2 += dir.x() * C.TILE_SIZE;
			y2 += dir.y() * C.TILE_SIZE;
			return free(a, d, x2, y2);
		}

		public AISTATE free(Humanoid a, AIManager d, int x2, int y2) {
			d.X = x2;
			d.Y = y2;
			if (x2 != a.physics.body().cX() || y2 != a.physics.body().cY()) {
				a.speed.turn2(a, x2, y2).magnitudeTargetSet(speed + RND.rFloat(0.05));

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (48f);
			return state;
		}

		public AISTATE tile(Humanoid a, AIManager d, int tx, int ty) {
			int x2 = (tx << C.T_SCROLL) + C.TILE_SIZEH;
			int y2 = (ty << C.T_SCROLL) + C.TILE_SIZEH;
			d.X = x2;
			d.Y = y2;
			if (x2 != a.physics.body().cX() || y2 != a.physics.body().cY()) {
				a.speed.turn2(a, x2, y2).magnitudeTargetSet(speed + RND.rFloat(0.05));

			} else {
				a.speed.magnitudeTargetSet(0);
				a.speed.magnitudeInit(0);
			}
			d.stateTimer = (48f);
			return state;
		}

	}

	public final class Animations {

		private Animations() {
			
		}
		
		public final Animation work = new Animation("Working", HSprites.TOOL_HIT);
		public final Animation box = new Animation("box", HSprites.GRAB);
		public final Animation wave = new Animation("waiving", HSprites.WAVE);
		public final Animation throww = new Animation("throw", HSprites.THROW);
		public final Animation stand = new Animation("waiving", HSprites.STAND);
		public final Animation grab = new Animation("grabbing", HSprites.BOX);
		public final Animation fist = new Animation("shaking fist", HSprites.FIST);
		public final Animation fistRight = new Animation("shaking fist", HSprites.ARM_RIGHT);
		public final Animation fistLeft = new Animation("shaking fist", HSprites.ARM_LEFT);
		public final Animation dance = new Animation("shaking fist", HSprites.DANCE);
		public final Animation danceE = new Animation("shaking fist", HSprites.DANCE_EXTRA);
		public final Animation sword = new Animation("bracing", HSprites.SWORD_STAND);
		public final Animation stab = new Animation("stab", HSprites.SWORD_STAB);
		public final Animation sword_out = new Animation("bracing", HSprites.SWORD_OUT);
		public final Animation sword_in = new Animation("bracing", HSprites.SWORD_IN);
		public final Animation lay = new Animation("laying", HSprites.LAY);
		public final Animation carry = new Animation("carry", HSprites.CARRY);
		public final Animation armsOut = new Animation("carry", HSprites.ARMS_OUT);
		public final Animation layoff = new Animation("laying", HSprites.LAYOFF);
		public final Animation archer1 = new Animation("archer", HSprites.ARM_LEFT2);
		public final Animation archer2 = new Animation("archer", HSprites.ARM_LEFT);
		// final Animation sword_stab = new Animation("stabbing", Sprite.SWORD_STAB);

	}
	
	public final class AnimationArrays {

		private AnimationArrays() {
			
		}
		private final Animation[] speak = new Animation[] {
			anima.carry,
			anima.fist,
			anima.grab,
			anima.fistRight,
			anima.fistRight,
			anima.fistRight,
		};
		private final Animation[] dance = new Animation[] {
			anima.carry,
			anima.fist,
			anima.grab,
			anima.fistRight,
			anima.fistLeft,
			anima.dance,
			anima.dance,
			anima.dance,
			anima.danceE,
			anima.danceE,
			anima.danceE,
		};
		private final Animation[] lecture = new Animation[] {
			anima.box,
			anima.fist,
			anima.grab,
			anima.wave,
		};
		
		public Animation speak() {
			return get(speak);
		}
		
		public Animation dance() {
			return get(dance);
		}
		
		public Animation lecture() {
			return get(lecture);
		}
		
		public Animation get(Animation[] as) {
			return as[RND.rInt(as.length)];
		}
	}

	public static final class Animation {

		final AISTATE state;
		public final double time;

		public AISTATE activate(Humanoid a, AIManager d, double time) {
			a.spriteTimer = 0;
			d.stateTimer = ((float) time);
			return state;
		}
		
		public AISTATE resume(Humanoid a, AIManager d, double time) {
			d.stateTimer = ((float) time);
			return state;
		}

		public AISTATE activate(Humanoid a, AIManager d) {
			a.spriteTimer = 0;
			d.stateTimer = (float) (state.sprite(a).time);
			return state;
		}

		public Animation(String name, HSprite sprite) {

			state = new AISTATE(name) {

				@Override
				public boolean update(Humanoid a, AIManager d, float ds) {
					d.stateTimer -= ds;
					return d.stateTimer >= 0;
				}

				@Override
				public HSprite sprite(Humanoid a) {
					return sprite;
				}
			};

			time = sprite.time;
		}

	}

}
