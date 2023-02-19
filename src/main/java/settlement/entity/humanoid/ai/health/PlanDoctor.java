package settlement.entity.humanoid.ai.health;

import game.time.TIME;
import init.C;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.health.physician.ROOM_PHYSICIAN;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;

class PlanDoctor extends AIPLAN.PLANRES{

	private final ROOM_PHYSICIAN b = SETT.ROOMS().PHYSICIAN;
	private final AIDataBit suspend = AI.data(). new AIDataBit();
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		AISubActivation s = res.set(a, d);
		if (s == null)
			suspend.set(d, true);
		return s;
	}
	
	private final Resumer res = new Resumer(b.service().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.service(a, d, b.service());
			if (s != null) {
				d.planTile.set(d.path.destX(), d.path.destY());
				b.service().reportAccess(a, d.planTile);
				b.service().reportDistance(a);
				suspend.set(d, false);
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return exam.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			suspend.set(d, true);
			
		}
	};
	
	private final Resumer exam = new Resumer(b.service().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			STATS.POP().NAKED.set(a.indu(), 1);
			DIR dir = b.getLayDir(d.planTile.x(), d.planTile.y());
			a.speed.setDirCurrent(dir);
			int x = d.planTile.x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*(C.TILE_SIZEH-2);
			int y = d.planTile.y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*(C.TILE_SIZEH-2);
			a.physics.body().moveC(x, y);
			return AI.SUBS().LAY.activateTime(a, d, 25);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			can(a, d);
			STATS.NEEDS().MEDICAL.fixMax(a.indu());
			for (DIR dir : DIR.ORTHO) {
				if (!SETT.PATH().solidity.is(a.tc(), dir)) {
					int x = a.tc().x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*(C.TILE_SIZE);
					int y = a.tc().y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*(C.TILE_SIZE);
					a.physics.body().moveC(x, y);
					break;
					
				}
			}
			
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 0);
			if (b.service().finder.getReserved(d.planTile.x(), d.planTile.y()) != null)
				b.service().finder.getReserved(d.planTile.x(), d.planTile.y()).findableReserveCancel();
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_UNREACHABLE)
				return true;
			return super.event(a, d, e);
		}
	};
	
	void update(Humanoid a, AIManager d) {
		if (suspend.is(d)) {
			b.service().clearAccess(a);
		}
		suspend.set(d, false);
	}
	
	public int isTime(Humanoid a, AIManager d) {

		if (!b.service().accessRequest(a)) {
			b.service().clearAccess(a);
			return 0;
		}
		
		if (TIME.light().nightIs()) {
			return 0;
		}
		if (suspend.is(d))
			return 0;
		
		return (int) (4*STATS.NEEDS().MEDICAL.getPrio(a.indu()));
		
		
	}
	

}
