package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.slaver.SlaverStation;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.util.CAUSE_ARRIVE;

class Enslaved extends AIPLAN.PLANRES{

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		SlaverStation s = SETT.ROOMS().SLAVER.exectuionReserve();
		if (s == null)
			return null;
		d.planTile.set(s.coo());
		return walk.set(a, d);
		
	}
	
	private final Resumer walk = new Resumer(LAW.process().enslaved.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null)
				return s;
			cancel(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return ready.set(a, d);
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
	
	private final Resumer ready = new Resumer(LAW.process().enslaved.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 1);
			SlaverStation s = SETT.ROOMS().SLAVER.executionSpot(d.planTile);
			s.clientUse();
			d.planByte1 = (byte) TIME.hours().bitCurrent();
			d.planByte2 = (byte) TIME.days().bitCurrent();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			SlaverStation s = SETT.ROOMS().SLAVER.executionSpot(d.planTile);
			if (s.clientExecuted()) {
				s.clientClear();
				STATS.POP().NAKED.set(a.indu(), 0);
				LAW.process().enslaved.inc(a.race());
				PrisonerData.self.reportedPunish.set(d, 1);
				a.HTypeSet(HTYPE.SLAVE, null, CAUSE_ARRIVE.PAROLE);
				return d.resumeOtherPlan(a, AI.plans().NOP);
			}
			a.speed.setDirCurrent(s.clientGetTurn());
			return AI.SUBS().LAY.activateTime(a, d, 5);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			if (d.planByte2 != TIME.days().bitCurrent() && TIME.hours().bitCurrent() > d.planByte1) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 0);
		}
		
	};
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		SlaverStation s = SETT.ROOMS().SLAVER.executionSpot(d.planTile);
		if (s != null)
			s.clientClear();
		super.cancel(a, d);
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		SlaverStation s = SETT.ROOMS().SLAVER.executionSpot(d.planTile);
		if (s != null && (s.clientReserved() || s.clientExecuted()))
			return super.shouldContinue(a, d);
		return false;
	}

}
