package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.law.stocks.ROOM_STOCKS;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Extra;
import snake2d.util.datatypes.DIR;

class Stocked extends AIPLAN.PLANRES{

	
	private final Extra p = LAW.process().stocks;
	private final ROOM_STOCKS blue = SETT.ROOMS().STOCKS;
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		
		AISubActivation s = walk.set(a, d);
		if (s != null)
			return s;
		LAW.process().stocks.inc(a.race(), false);
		return null;
	}
	
	private final Resumer walk = new Resumer(p.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.serviceInclude(a, d, SETT.ROOMS().STOCKS.finder, 200);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return sit.set(a, d);
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
	
	private final Resumer sit = new Resumer(p.verb) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planTile.set(d.path.destX(), d.path.destY());
			DIR dir = blue.dir(d.planTile.x(), d.planTile.y(), a.speed.dir());
			a.speed.setDirCurrent(dir);
			blue.finder.get(d.planTile.x(), d.planTile.y()).startUsing();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			FINDABLE f = blue.finder.getReserved(d.planTile.x(), d.planTile.y());
			if (f == null)
				return null;
			if (TIME.light().nightIs() || STATS.NEEDS().EXPOSURE.inDanger(a.indu())) {
				can(a, d);
				LAW.process().stocks.inc(a.race(), true);
				PrisonerData.self.prisonTimeLeft.inc(d, -1);
				return null;
			}
			return AI.SUBS().LAY.activateTime(a, d, 16);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE f = blue.finder.getReserved(d.planTile.x(), d.planTile.y());
			if (f != null)
				f.findableReserveCancel();
		}
		
	};

}
