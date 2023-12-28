package settlement.entity.humanoid.ai.main;

import util.data.BOOLEANO.BOOLEAN_OBJECTE;
import util.data.DataOL;

public final class AIData extends DataOL<AIManager>{


	AIData() {
		
	}
	
	
	@Override
	protected long[] data(AIManager t) {
		return t.longs;
	}
	
	public class AIDataBit extends DataOL<AIManager>.DataBit implements BOOLEAN_OBJECTE<AIManager>{
		
		
		public AIDataBit(){
			
		}
		
		@Override
		public boolean is(AIManager d) {
			return get(d) == 1;
		}
		
		@Override
		public BOOLEAN_OBJECTE<AIManager> set(AIManager d, boolean s) {
			set(d, s ? 1 : 0);
			return this;
		}

	}

	public final class AIDataSuspender extends util.data.DataOL<AIManager>.DataCrumb {
		
		public AIDataSuspender(){
			
		}
		
		public boolean is(AIManager d) {
			return get(d) != 0;
		}
		
		public void suspend(AIManager d) {
			set(d, 2);
		}
		
		public void update(AIManager d) {
			if (is(d)) {
				set(d, get(d)-1);
			}
		}
		
	}

}