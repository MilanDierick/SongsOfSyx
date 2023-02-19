package settlement.entity.humanoid.ai.types.prisoner;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing;
import settlement.stats.law.Processing.Punishment;
import snake2d.util.bit.Bits;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;

public final class PrisonerData {

	static PrisonerData self;
	
	public final INT_OE<HAI> judged;
	final INT_OE<HAI> hasWaitedJudge;
	public final INT_OE<HAI> noJudge;
	final INT_OE<HAI> reportedPunish;
	
	public final GETTER_TRANSE<HAI, Punishment> punishment;
	public final INT_OE<HAI> prisonTimeLeft;
	public final INT_OE<HAI> daysWaited;

	
	public PrisonerData() {
		self = this;
		judged = new Wrap(new Bits(0b0000_0001), AIModules.data().byte1);
		hasWaitedJudge = new Wrap(new Bits(0b0000_0010), AIModules.data().byte1);
		noJudge = new Wrap(new Bits(0b0000_0100), AIModules.data().byte1);
		daysWaited = new Wrap(new Bits(0b0001_1000), AIModules.data().byte1);
		reportedPunish = new Wrap(new Bits(0b0010_1000), AIModules.data().byte1);
		Wrap pp = new Wrap(new Bits(0b0000_1111), AIModules.data().byte2);
		punishment = new GETTER_TRANSE<HAI, Processing.Punishment>() {
			
			@Override
			public Punishment get(HAI f) {
				return LAW.process().punishments.get(pp.get(f));
			}
			
			@Override
			public void set(HAI f, Punishment t) {
				pp.set(f, t.index());
			}
		};
		prisonTimeLeft = new Wrap(new Bits(0x0FF), AIModules.data().byte3);
		
	}
	
	protected void init(Humanoid a, AIManager d) {
		AI.modules().coo(d).set(-1, -1);
		AIModules.data().byte1.set(d, 0);
		prisonTimeLeft.set(d, AIModule_Prisoner.PRISON_DAYS);
		punishment.set(d, LAW.process().getPunishment(a));
		noJudge.set(d, 1);
	}
	
	private static class Wrap implements INT_OE<HAI>{
		
		private final Bits bits;
		private final INT_OE<AIManager> data;
		
		Wrap(Bits bits, INT_OE<AIManager> data){
			this.bits = bits;
			this.data = data;
		}

		@Override
		public int get(HAI t) {
			return bits.get(data.get((AIManager) t));
		}

		@Override
		public int min(HAI t) {
			return 0;
		}

		@Override
		public int max(HAI t) {
			return bits.mask;
		}

		@Override
		public void set(HAI t, int i) {
			int d = data.get((AIManager) t);
			d = bits.set(d, i);
			data.set((AIManager) t, d);
		}
		
	}
}
