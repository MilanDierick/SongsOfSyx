package settlement.entity.humanoid.ai.battle;

import init.C;
import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class PlanAttackTile extends AIPLAN.PLANRES {

	private static CharSequence ¤¤name = "¤attacking terrain";

	public PlanAttackTile() {

	}

	private static int tx,ty;
	
	AIPLAN init(AIManager d, Humanoid a, int tx, int ty) {
		PlanAttackTile.tx = tx;
		PlanAttackTile.ty = ty;

		return this;
	}

	public boolean shouldattackTile(AIManager d, Humanoid a, int tx, int ty) {
		if (!SETT.ARMIES().map.attackable.is(tx, ty, a.indu().army()))
			return false;
		if (a.division() == null)
			return true;
		if (a.division().reporter.getTile(a.divSpot()) == null)
			return true;
		if (!a.division().settings.mustering() || a.division().settings.moppingUp())
			return true;
		if (COORDINATE.tileDistance(a.division().reporter.getTile(a.divSpot()).x(), a.division().reporter.getTile(a.divSpot()).y(), tx, ty) < 2)
			return true;
		return false;
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		d.planByte1 = 0;
		d.planTile.set(tx, ty);
		return stop.set(a, d);
	}

	private final Resumer stop = new Resumer(¤¤name) {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 0);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return moveToEdge.set(a, d);
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

	private final Resumer moveToEdge = new Resumer(¤¤name) {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (!shouldattackTile(d, a, d.planTile.x(), d.planTile.y()))
				return null;
			int x = (d.planTile.x() << C.T_SCROLL) + C.TILE_SIZEH;
			int y = (d.planTile.y() << C.T_SCROLL) + C.TILE_SIZEH;
			DIR dir = DIR.get(a.body().cX(), a.body().cY(), x, y);
			x = (a.tc().x() << C.T_SCROLL) + C.TILE_SIZEH;
			y = (a.tc().y() << C.T_SCROLL) + C.TILE_SIZEH;
			
			int dist = (C.TILE_SIZE-a.body().width()-1)/2;
			
			x += dir.x()*dist;
			y += dir.y()*dist;

			AISTATE s = AI.STATES().WALK2_SWORD.free(a, d, x, y);
			a.speed.setDirCurrent(dir);
			return AI.SUBS().single.activate(a, d, s);
		}

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return wait.set(a, d);
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

	private final Resumer wait = new Resumer(¤¤name) {

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {

			return attack.set(a, d);
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			int tx = (d.planTile.x() << C.T_SCROLL) + C.TILE_SIZEH;
			int ty = (d.planTile.y() << C.T_SCROLL) + C.TILE_SIZEH;
			DIR dir = DIR.get(a.body().cX(), a.body().cY(), tx, ty);
			a.speed.setDirCurrent(dir);
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword.activate(a, d, 2 + RND.rFloat() * 2));
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

	private final Resumer attack = new Resumer(¤¤name) {

		@Override
		public AISubActivation res(Humanoid a, AIManager d) {

			if (!shouldattackTile(d, a, d.planTile.x(), d.planTile.y()))
				return null;

			double mom = C.TILE_SIZE * BOOSTABLES.BATTLE().BLUNT_DAMAGE.get(a);

			double str = SETT.ARMIES().map.strength.get(d.planTile);
			
			

			while (mom > 0) {
				if (mom > RND.rFloat() * str)
					d.planByte1++;
				mom -= str;
			}
			
			
			if (d.planByte1 >= 4) {
				SETT.ARMIES().map.breakIt(d.planTile.x(), d.planTile.y());
				return null;
			}

			return wait.set(a, d);
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.stab.activate(a, d));
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
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.COLLIDING)
			return 0;
		return InterBattle.listener.poll(a, d, e);
	}

	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {

		switch (e.event) {

		case COLLISION_TILE:
			
			if (shouldattackTile(d, a, e.tx, e.ty)) {
				if (!d.planTile.isSameAs(e.tx, e.ty)) {
					d.planTile.set(e.tx, e.ty);
					d.planByte1 = 0;
				}
				d.overwrite(a, stop.set(a, d));
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
		case COLLISION_SOFT: {
			if (shouldattackTile(d, a, e.tx, e.ty)) {
				d.overwrite(a, stop.set(a, d));
				a.speed.setPrevDir();
			}
		}
		default:
			return InterBattle.listener.event(a, d, e);
		}

	}

}
