package settlement.room.main.util;

import java.io.Serializable;

import game.faction.FACTIONS;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;

public abstract class RoomState implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public abstract void apply(Room r, int tx, int ty);
	
	public static class RoomStateInstance extends RoomState {

		private static final long serialVersionUID = 1L;
		private final int workersTarget;
		private final int industry;
		private boolean auto; 
		
		
		public RoomStateInstance(RoomInstance ins) {
			this.workersTarget = ins.employees().hardTarget();
			if (ins.blueprintI() instanceof ROOM_EMPLOY_AUTO) {
				ROOM_EMPLOY_AUTO a = (ROOM_EMPLOY_AUTO) ins.blueprintI();
				auto = a.autoEmploy(ins);
			}
			if (ins instanceof ROOM_PRODUCER) {
				industry = ((ROOM_PRODUCER) ins).industryI();
			}else {
				industry = 0;
			}
		}
		
		@Override
		public void apply(Room room, int tx, int ty) {
			if (!(room instanceof RoomInstance))
				return;
			
			RoomInstance ins = (RoomInstance) room;
			//ins.name().clear().add(name);
			if (ins.blueprintI().employment() != null) {
				ins.employees().neededSet(workersTarget);
			}
			if (ins.blueprintI() instanceof ROOM_EMPLOY_AUTO) {
				ROOM_EMPLOY_AUTO a = (ROOM_EMPLOY_AUTO) ins.blueprintI();
				a.autoEmploy(ins, auto);
			}
			if (ins instanceof ROOM_PRODUCER && ins.blueprint() instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) ins.blueprint();
				if (industry >= 0 && industry < h.industries().size() && h.industries().getC(industry).lockable().passes(FACTIONS.player()))
					((ROOM_PRODUCER) ins).setIndustry(industry);
			}
			applyIns(ins);
		}
		
		protected void applyIns(RoomInstance ins) {
			
		}
		
	}
	
	public final static RoomState DUMMY = new RoomState() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void apply(Room r, int tx, int ty) {
			
		}
	};
	
}