package settlement.army.ai.divs;

import init.C;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.formation.DivFormation;
import settlement.army.order.DivTDataTask.DIVTASK;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;

final class PlanFight extends Plan {

	private final INT_OE<AIManager> changePosition;
	private final INT_OE<AIManager> timer;

	public PlanFight(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
		changePosition = data.new DataBit();
		timer = data.new DataShort();
	}

	@Override
	void init() {

		if (settings.guard && task.taskPrev() == DIVTASK.STOP && next.deployed() > 0
				&& t.div.distanceAverageFromCurrentToNext(current, next) < C.TILE_SIZEH)
			changePosition.set(m, 0);
		else
			changePosition.set(m, 1);

		if (settings.guard)
			fightGuard.set();
		else
			fight.set();

	}

	@Override
	void update(int upI, int gamemillis) {

		state(m).update(upI, gamemillis);
	}

	private STATE fight = new STATE() {

		@Override
		void update(int updateI, int gameMillis) {

			if (settings.guard) {
				fightGuard.set();
				return;
			}
			
			shouldFire = false;
			
			timer.inc(m, -gameMillis);
			if (status.enemyCollisions() == 0) {
				if (changePosition.get(m) == 0) {
					wait.set();
				} else if (timer.get(m) == 0) {
					DivFormation f = t.battle.getAdvanced(next);
					if (f != null) {
						m.order.current.get(current);
						t.mover.moveFromIntoTo(current, next, f);
						m.order.next.set(next);
						timer.inc(m, 5000);
					} else {
						wait.set();
					}
				}
				return;
			} else if (timer.get(m) == 0) {
				if (changePosition.get(m) == 1) {
					DivFormation f = t.battle.getBestAttack(next);

					if (f != null) {
						double dot = next.dx() * f.dx() + next.dy() * f.dy();
						m.order.current.get(current);
						f = t.mover.getFromMovedIntoTo(current, next, f);
						if (dot < 0.9 || t.div.distanceAverageFromCurrentToNext(f, next) > C.TILE_SIZE) {
							next.copy(f);
							m.order.next.set(next);
						}
					}
				}

				timer.set(m, 3000);
			}

		}

		@Override
		boolean setAction() {
			DivFormation f = t.battle.getBestGuard(current);
			if (f != null) {
				m.order.current.get(current);
				t.mover.moveFromIntoTo(current, next, f);
				m.order.next.set(next);
			}
			timer.set(m, 3000);
			return false;
		}
	};

	private STATE fightGuard = new STATE() {

		@Override
		void update(int updateI, int gameMillis) {
			if (!settings.guard) {
				fight.set();
				return;
			}
			timer.inc(m, -gameMillis);
			if (status.enemyCollisions() == 0) {
				if (changePosition.get(m) == 0) {
					wait.set();
				} else if (timer.get(m) == 0) {
					DivFormation f = t.battle.getAdvanced(next);
					if (f != null) {
						m.order.current.get(current);
						t.mover.moveFromIntoTo(current, next, f);
						m.order.next.set(next);
						timer.inc(m, 5000);
					} else {
						wait.set();
					}
				}
				return;
			} else if (timer.get(m) == 0) {
				if (changePosition.get(m) == 1) {
					DivFormation f = t.battle.getBestGuard(next);

					if (f != null) {
						double dot = next.dx() * f.dx() + next.dy() * f.dy();
						m.order.current.get(current);
						f = t.mover.getFromMovedIntoTo(current, next, f);
						if (dot < 0.9 || t.div.distanceAverageFromCurrentToNext(f, next) > C.TILE_SIZE) {
							next.copy(f);
							m.order.next.set(next);
						}
					}
				}

				timer.set(m, 3000);
			}

			shouldFire = true;
		}

		@Override
		boolean setAction() {
			if (changePosition.get(m) == 1) {
				DivFormation f = t.battle.getBestGuard(current);
				if (f != null) {
					m.order.current.get(current);
					t.mover.moveFromIntoTo(current, next, f);
					m.order.next.set(next);
				}
			}
			timer.set(m, 3000);
			return false;
		}
	};

	private STATE wait = new STATE() {

		@Override
		void update(int updateI, int gameMillis) {
			
			if (status.enemyCollisions() > 0) {
				if (settings.guard) {
					fightGuard.set();
				} else
					fight.set();
			} else {
				task.setPrev();
				m.order.task.set(task);
			}
		}

		@Override
		boolean setAction() {
			timer.set(m, 0);
			return false;
		}
	};

}
