package settlement.entity.humanoid.ai.subwalk;

import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.*;
import settlement.entity.humanoid.ai.main.AISTATES.WALK;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.stats.CAUSE_LEAVE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class SubFlee extends AISUB.Simple{

	private final WALK run = new AISTATES.WALK(0.7);
	private final WALK run2 = new AISTATES.WALK(0.7, HSprites.WAVE);
	
	public SubFlee() {
		// TODO Auto-generated constructor stub
	}
	
	AISubActivation activate(Humanoid a, AIManager d, ENTITY other){
		a.speed.turn2(other.body(), a.body());
		return activate(a, d);
	}
	
	AISubActivation activate(Humanoid a, AIManager d, int iterations){
		d.subPathByte = (byte) (iterations+1);
		return activate(a, d);
	}
	
	@Override
	public AISubActivation activate(Humanoid a, AIManager d) {
		d.subPathByte = (byte) (2 + RND.rInt(5));
		d.subPathByte2 = (byte) (2 + RND.rInt(15));
		return super.activate(a, d);
	}
	
	@Override
	protected AISTATE resume(Humanoid a, AIManager d) {
		a.speed.turnWithAngel(RND.rFloat0(90));
		d.subPathByte--;
		if (SETT.TERRAIN().WATER.DEEP.is(a.tc())) {
			d.subPathByte2 --;
			if (d.subPathByte2 <= 0) {
				HumanoidResource.dead = CAUSE_LEAVE.DROWNED;
			}
		}
		
		if (d.subPathByte > 0) {
			if (RND.oneIn(3))
				return run2.activate(a, d, 2f + RND.rFloat()*3);
			return run.activate(a, d, 2f + RND.rFloat()*3);
		}
		
		return null;
	}
	
	@Override
	protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
		return null;
	};
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		return 0;
	}
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.COLLISION_UNREACHABLE) {
			DIR dd = a.speed.dir();
			if (!dd.isOrtho())
				dd = dd.next(1);
			for (int i = 0; i < 4; i++) {
				if (SETT.PATH().connectivity.is(a.tc(), dd)) {
					break;
				}
				dd = dd.next(2);
				//a.speed.turn90();
			}
			if (SETT.PATH().connectivity.is(a.tc(), dd)) {
				a.speed.setRaw(dd, 0.5);
			}
		}else if (e.event == HEvent.MEET_ENEMY) {
			a.speed.turn2(-e.norX, -e.norY);
			d.stateTimer = 10;
		}else if (e.event == HEvent.COLLISION_TILE) {
			a.speed.turn2(e.norX, e.norY);
			
			return true;
		}else if(e.event == HEvent.EXHAUST) {
			return super.event(a, d, e);
		}else if (e.event == HEvent.COLLISION_HARD) {
			return super.event(a, d, e);
		}
		return false;
	}

}
