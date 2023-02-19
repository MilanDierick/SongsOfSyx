package settlement.entity.humanoid.ai.health;

import init.D;
import init.sound.SOUND;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;

class PlanSick extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "Being sick";
	private final SubPlanSeekHospital ho = new SubPlanSeekHospital(this);
	
	static {
		D.ts(PlanSick.class);
	}
	
	public PlanSick() {
		
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		AISubActivation s = ho.init(a, d);
		if (s != null)
			return s;
		return res.set(a, d);
	}
	
	private final Resumer res = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if ((a.indu().randomness() & 0x0FF) > 200) {
				
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h != null) {
					if (!h.is(a.tc())) {
						int sx= h.service().x();
						int sy = h.service().y();
						h.done();
						return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
						
					}else if (SETT.ENTITIES().hasAtTileHigher(a, a.tc().x(), a.tc().y())){
						for (DIR dir : DIR.ORTHO) {
							if (h.is(a.tc(), dir) && !SETT.PATH().solidity.is(a.tc(), dir) && !SETT.ENTITIES().hasAtTileHigher(a, a.tc().x()+dir.x(), a.tc().y()+dir.y())) {
								h.done();
								return AI.SUBS().walkTo.cooFull(a, d, a.tc().x()+dir.x(), a.tc().y()+dir.y());
							}
						}
					}
					h.done();
				}
				
			}
			
			SOUND.sett().action.pain.rnd(a.body());
			
			return AI.SUBS().LAY.activateTime(a, d, 60);
			
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (STATS.NEEDS().disease.getter.get(a.indu()) == null) {
				return null;
			}
			
			AISubActivation s = ho.init(a, d);
			if (s != null)
				return s;
			
			return set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	



}
