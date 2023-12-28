package settlement.entity.humanoid.ai.types.prisoner;

import game.GAME;
import init.D;
import init.need.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanGladiator;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.service.arena.RoomArenaWork;
import settlement.room.service.arena.grand.ROOM_ARENA;
import settlement.room.service.arena.pit.ROOM_FIGHTPIT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;

class Arena extends AIPlanGladiator{

	private static CharSequence ¤¤name = "¤Fighting to death in the arena.";
	
	static {
		D.ts(Arena.class);
	}
	
	
	public Arena() {
		super(true, ¤¤name);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		d.planByte3 = 0;
		
		NEEDS.TYPES().HUNGER.stat().fixMax(a.indu());
		STATS.NEEDS().EXPOSURE.count.set(a.indu(), 0);
		
		for (ROOM_FIGHTPIT arena : SETT.ROOMS().ARENAS) {
			RoomInstance ins = arena.work.reserveDeath(a.tc());
			if (ins != null) {
				d.planTile.set(arena.work.gladiatorGetSpot(ins));
				return super.init(a, d);
			}
			d.planByte3 ++;	
		}
		
		for (ROOM_ARENA arena : SETT.ROOMS().GARENAS) {
			RoomInstance ins = arena.work.reserveDeath(a.tc());
			if (ins != null) {
				d.planTile.set(arena.work.gladiatorGetSpot(ins));
				return super.init(a, d);
			}
			d.planByte3 ++;	
		}
		
		return null;
	}

	@Override
	protected RoomArenaWork w(Humanoid a, AIManager d) {
		if (d.planByte3 >= SETT.ROOMS().ARENAS.size())
			return SETT.ROOMS().GARENAS.get(d.planByte3-SETT.ROOMS().ARENAS.size()).work;
		
		return SETT.ROOMS().ARENAS.get(d.planByte3).work;
	}
	
	@Override
	protected AISubActivation resume(Humanoid a, AIManager d) {
		AISubActivation sub = super.resume(a, d);
		if (sub == null) {
			w(a,d).unreserveDeath(d.planTile.x(), d.planTile.y());
			RoomInstance ins = w(a, d).reserveDeath(a.tc());
			if (ins != null) {
				d.planTile.set(w(a,d).gladiatorGetSpot(ins));
				return super.init(a, d);
			}
		}
		
			
		return sub;
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		RoomArenaWork w = w(a, d);
		if (w != null)
			w.unreserveDeath(d.planTile.x(), d.planTile.y());
		super.cancel(a, d);
	}
	
	@Override
	protected void remove(Humanoid a, AIManager d) {
		LAW.process().arena.inc(a.race());
		PrisonerData.self.reportedPunish.set(d, 1);
		GAME.count().EXECUTIONS.inc(1);
		super.remove(a, d);
	}
	
	

}
