package settlement.army.ai.divs;

import settlement.army.Div;
import settlement.army.ai.divs.Plans.Data;
import settlement.army.ai.divs.Plans.Plan;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;

final class PlanFireDiv extends PlanWalkAbs{

	private final INT_OE<AIManager> pathI;
	private final INT_OE<AIManager> pathd;
	private final INT_OE<AIManager> timer;
	private final static DivTDataStatus otherStatus = new DivTDataStatus();
	private final VectorImp vec = new VectorImp();
	
	public PlanFireDiv(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
		pathI = data.new DataShort();
		timer = data.new DataShort();
		pathd = data.new DataBit();
	}
	
	@Override
	void init() {

		if (checkTarget()) {
			if (traj.hasAny()) {
				fire.set();
			}else {
				wait.set();
			}	
		}else {
			fail.set();
		}
	}
	
	private boolean checkTarget() {
		if (info.projVel == 0)
			return false;
		Div target = task.targetDiv();
		if (target == null || !target.order().active()) {
			fail.set();
			return false;
		}
		return true;
	}
	
	private void setDest() {
		Div target = task.targetDiv();
		target.order().status.get(otherStatus);
		
		int sx = status.currentPixelCX();
		int sy = status.currentPixelCY();
		
		int dx = otherStatus.currentPixelCX();
		int dy = otherStatus.currentPixelCY();
		
		double nx = 1;
		double ny = 0;
		
		if (sx != dx || sy != dy) {
			vec.set(sx, sy, dx, dy);
			vec.rotate90();
			nx = vec.nX();
			ny = vec.nY();
		}
		
		int w = (int) Math.sqrt(info.men*2);
		
		DivFormation f = t.deployer.deployCentre(info.men, settings.formation, dx, dy, nx, ny, w, a);
		
		if (f == null) {
			fail.set();
			return;
		}
		
		dest.copy(f);
		m.order.dest.set(dest);
		setWalkToDest();	
		pathd.set(m, path.currentI()&1);
	}
	
	@Override
	void update(int upI, int gamemillis) {
		
		if (!checkTarget()) {
			fail.set();
			return;
		}
		
		if (state(m) == wait || state(m) == fire) {
			state(m).update(upI, gamemillis);
			return;
		}
		
		if (traj.hasAny()) {
			dest.copy(next);
			path.clear();
			m.order.dest.set(dest);
			m.order.path.set(path);
			fire.set();
			return;
		}
		
		state(m).update(upI, gamemillis);
		
		if (status.enemyCollisions() > 0) {
			return;
		}

		if ((path.currentI() & 1) != pathd.get(m)) {
			pathd.set(m, path.currentI()&1);
			pathI.inc(m, 1);
			
			int tres = 50;
			if (path.isComplete()) {
				tres = CLAMP.i(path.length()-path.currentI(), 1, 50); 
			}
			
			if (pathI.get(m) > tres) {
				setDest();
			}
		}
	}

	@Override
	void finished() {
		fail.set();
	}
	
	private final STATE fire = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			shouldFire = true;
			if (!checkTarget()) {
				fail.set();
				return;
			}
			
			if (traj.hasAny()) {
				return;
			}
			
			setDest();
			setWalkToDest();
		}
		
		@Override
		boolean setAction() {
			return true;
		}
	};
	
	private final STATE wait = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			if (!checkTarget()) {
				fail.set();
				return;
			}
			
			if (traj.hasAny()) {
				fire.set();
			}
			timer.inc(m, gameMillis);
			if (timer.get(m) > 1000) {
				setDest();
				setWalkToDest();
			}
		}
		
		@Override
		boolean setAction() {
			timer.set(m, 0);
			return true;
		}
	};
	
	private final STATE fail = new STATE() {
		
		@Override
		void update(int updateI, int gameMillis) {
			
		}
		
		@Override
		boolean setAction() {
			task.stop();
			m.order.task.set(task);
			return true;
		}
	};
	



}
