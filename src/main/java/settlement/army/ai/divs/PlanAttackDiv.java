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

class PlanAttackDiv extends PlanWalkAbs{

	private final INT_OE<AIManager> pathI;
	protected final INT_OE<AIManager> pathd;
	private final DivTDataStatus otherStatus = new DivTDataStatus();
	private final VectorImp vec = new VectorImp();
	
	public PlanAttackDiv(Tools tools, ArrayList<Plan> all, Data data) {
		super(tools, all, data);
		pathI = data.new DataShort();
		pathd = data.new DataBit();
	}
	
	@Override
	void init() {

		
		
		if (!checkTarget())
			return;
		
		setDest();
	}
	
	protected boolean checkTarget() {
		Div target = task.targetDiv();
		
		if (target == null || !target.order().active()) {
			fail.set();
			return false;
		}
		return true;
	}
	
	private void setDest() {
		if (!checkTarget()) {
			fail.set();
			return;
		}
		
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
	
	protected final STATE fail = new STATE() {
		
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
