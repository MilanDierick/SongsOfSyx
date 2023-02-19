package settlement.entity.humanoid.ai.subwalk;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;

public class AISub_runto extends PathWalker{

	protected AISub_runto() {
		super(AI.STATES().RUN2, "running");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean hasFailed(Humanoid a, AIManager d) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void abort(Humanoid a, AIManager d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void arrive(Humanoid a, AIManager d) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected AISTATE setLast(Humanoid a, AIManager d) {
		return null;
	};

}
