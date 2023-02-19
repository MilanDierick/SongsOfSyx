package settlement.army.ai.divs;

import init.C;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;

abstract class PlanWalkAbs extends Plan {

	private final INT_OE<AIManager> inPosition;
	private final INT_OE<AIManager> timer;
	final INT_OE<AIManager> colTimer;
	private final INT_OE<AIManager> tilesDestCheck;
	private final INT_OE<AIManager> destId;
	static int amountOfPaths = 0;

	public PlanWalkAbs(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
		inPosition = data.new DataByte();
		timer = data.new DataInt();
		colTimer = data.new DataShort();
		tilesDestCheck = data.new DataByte();
		destId = data.new DataNibble();
	}

	void setWalkToDest() {

		path.clear();
		m.order.path.set(path);

		wait.set();
	}

	private boolean checkNextDest() {
		if (t.div.needsFixing(dest, info.men, a, settings.formation)) {
			if (t.deployer.fixFormation(dest, settings.formation, info.men, a)) {
				m.order.dest.set(dest);
			}
		}

		int di = m.order.dest.setI() & 0x0F;

		if (destId.get(m) != di) {
			destId.set(m, di);
			return true;
		}
		return false;
	}

	private final STATE wait = new STATE() {

		@Override
		void update(int updateI, int gameMillis) {
			if (amountOfPaths++ > 10)
				return;
			setStart.set();

		}

		@Override
		boolean setAction() {
			return true;
		}
	};

	private final STATE setStart = new STATE() {

		@Override
		boolean setAction() {

			if (!t.walk.setStart(ToolsWalk.destMoveStart)) {

				return moveIntoDest.set();
			}

			if (t.div.intersectsSomewhat(next, dest))
				return moveIntoDest.set();
			timer.set(m, 0);
			inPosition.set(m, 0);
			return true;
		}

		@Override
		void update(int updateI, int gamemillis) {

			if (checkNextDest()) {
				setWalkToDest();
				return;
			}

			if (t.div.needsFixing(next, info.men, a, settings.formation)) {
				t.deployer.fixFormation(next, settings.formation, info.men, a);
				m.order.next.set(next);
			}

			int pos = countPosition();

			if (pos > 0) {
				if (pos == 1) {
					timer.set(m, 0);
				}
				if (settings.running || pos >= info.men - info.unreachable) {
					followPath.set();
					return;
				}
				if (pos > inPosition.get(m)) {
					timer.set(m, 0);
				}
				inPosition.set(m, pos);

				timer.inc(m, gamemillis);
				if (timer.get(m) >= 2000) {
					followPath.set();
					return;
				}
			} else {
				timer.inc(m, gamemillis);
				if (timer.get(m) > 1000) {
					setAction();
					return;
				}
			}

			if (path.isDest()) {
				return;
			}

			if (path.currentI() < path.length() - 1) {
				m.order.current.get(current);
				int d1 = t.div.distanceTO(path.x(), path.y(), current);
				path.currentIInc(1);
				int d2 = t.div.distanceTO(path.x(), path.y(), current);
				path.currentIInc(-1);

				if (d1 <= d2)
					return;
				t.walk.setNextPosition(ToolsWalk.destMoveStart);
				timer.set(m, 0);
			}

		}

	};

	private final STATE followPath = new STATE() {

		@Override
		boolean setAction() {
			tilesDestCheck.set(m, ToolsWalk.destMoveStart - ToolsWalk.destMoveResume);
			inPosition.set(m, countPosition());
			timer.set(m, 0);
			colTimer.set(m, 0);
			return true;
		}

		@Override
		void update(int updateI, int gameMillis) {

			if (checkNextDest()) {
				setWalkToDest();
				return;
			}

			if (wait2(gameMillis)) {
				return;
			}
			if (path.isDest()) {
				resume();
				return;
			}
			m.order.path.get(path);
			tilesDestCheck.inc(m, -1);

			if (!t.walk.setNextPosition(ToolsWalk.destMoveResume + tilesDestCheck.get(m))) {
				init();
				return;
			}

			// double dist = t.div.distanceMaxFromCurrentToNext(tmp, next);
			// timer.set(m, 0);
			// double ma = C.TILE_SIZE + C.TILE_SIZEH;
			// if (dist > ma) {
			// timer.inc(m, (int) (1000.0*(dist-ma)/ma));
			// }

		}

		private boolean wait2(int gameMillis) {
			// colTimer.inc(m, -gameMillis);
			// if (colTimer.get(m) > 0)
			// return true;

			if (!wait(gameMillis)) {
				// int cc = t.coll.isCollidingWithFriendly();
				// if (cc > 0) {
				// colTimer.set(m, 3000);
				// if (cc > 1)
				// return true;
				// }
				return false;
			}
			return true;
		}

		private boolean wait(int gameMillis) {

			if (t.div.needsFixing(next, info.men, a, settings.formation)) {
				t.deployer.fixFormation(next, settings.formation, info.men, a);
				m.order.next.set(next);
				timer.set(m, 1000);
			}

			int in = countPosition();

			if (in == 0) {
				colTimer.inc(m, gameMillis);
				if (colTimer.get(m) > 3000) {
					setStart.set();
				} else if (path.currentI() < path.length() - 1) {

					m.order.current.get(current);
					int d1 = t.div.distanceTO(path.x(), path.y(), current);
					path.currentIInc(1);
					int d2 = t.div.distanceTO(path.x(), path.y(), current);
					path.currentIInc(-1);
					if (d1 > d2) {
						if (!t.walk.setNextPosition(ToolsWalk.destMoveResume + tilesDestCheck.get(m))) {
							init();
							return true;
						}
						timer.set(m, 0);
						colTimer.set(m, 0);
						return true;
					}
				}

				return true;
			}
			colTimer.set(m, 0);

			if (in > inPosition.get(m) - info.unreachable)
				inPosition.set(m, in);

			if (settings.running)
				return false;

			// if (!settings.running)
			// return false;
			//
			if (in >= next.deployed())
				return false;

			if (in < (inPosition.get(m) - info.unreachable)) {

				int tt = timer.get(m) - gameMillis;
				if (tt < 0) {
					int am = 1 + -tt / 50;
					tt += am * 50;
					inPosition.inc(m, -am);
					if (in < (inPosition.get(m) - info.unreachable)) {
						timer.set(m, tt);
						return true;
					}
					return false;

				}
				timer.set(m, tt);
				return true;
			}

			return false;
		}

		void resume() {
			if (path.isDest() && path.isComplete()) {
				moveIntoDest.set();
				return;
			}
			if (!t.walk.setStart(ToolsWalk.destMoveStart)) {
				init();
				return;
			}
			if (t.div.intersectsSomewhat(next, dest))
				moveIntoDest.set();
			else
				inPosition.set(m, CLAMP.i(countPosition(), 0, info.men));
		}

	};

	private int countPosition() {
		int dist = C.TILE_SIZEH;
		if (settings.running)
			dist += C.TILE_SIZE + C.TILE_SIZEH;
		return t.div.inPosition(current, next, dist);
	}

	private final STATE moveIntoDest = new STATE() {

		@Override
		boolean setAction() {
			path.clear();
			m.order.path.set(path);
			t.mover.rearrangeDest(next, dest);
			m.order.dest.set(dest);
			int di = m.order.dest.setI() & 0x0F;
			destId.set(m, di);
			timer.set(m, 100);
			inPosition.set(m, countPosition());
			update(0, 0);
			return true;
		}

		@Override
		void update(int updateI, int gamemillis) {
			if (checkNextDest()) {
				setWalkToDest();
				return;
			}
			if (wait(m, gamemillis))
				return;
			if (!t.mover.merge(next, dest)) {
				stayInDest.set();
				return;
			}

			m.order.next.set(next);
		}

		private boolean wait(AIManager m, int gamemillis) {

			timer.inc(m, -gamemillis);
			int in = countPosition();

			if (in == 0)
				return true;

			if (in < inPosition.get(m)) {
				timer.inc(m, -gamemillis);
				if (timer.get(m) <= 0) {
					inPosition.inc(m, -1);
					timer.set(m, 100);
				}
				return true;
			} else {
				inPosition.set(m, in);
			}
			return false;
		}

	};

	private final STATE stayInDest = new STATE() {

		@Override
		boolean setAction() {

			m.order.next.set(dest);

			return true;
		}

		@Override
		void update(int updateI, int gamemillis) {

			if (checkNextDest()) {
				setWalkToDest();
				return;
			}

			int in = countPosition();
			if (in < (next.deployed() - info.unreachable) / 2)
				return;

			finished();
		}

	};

	abstract void finished();

}
