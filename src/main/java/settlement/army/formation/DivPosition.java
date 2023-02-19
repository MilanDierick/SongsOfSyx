package settlement.army.formation;

import init.RES;
import settlement.army.order.Copyable;

public class DivPosition extends DivPositionAbs implements Copyable<DivPosition>{
	
	public DivPosition() {
		super(RES.config().BATTLE.MEN_PER_DIVISION);
	}
	
	public DivPosition(int maxMen) {
		super(maxMen);
	}
	
	@Override
	public void copy(DivPosition pos) {
		copyy(pos);
	}
	
}
