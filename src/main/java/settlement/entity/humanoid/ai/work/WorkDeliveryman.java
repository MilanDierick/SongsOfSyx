package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.*;

import game.boosting.Boostable;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.main.*;
import settlement.room.main.job.StorageCrate;
import settlement.room.main.job.StorageCrate.STORAGE_CRATE_HASSER;
import snake2d.util.misc.CLAMP;

final class WorkDeliveryman extends PlanBlueprint {

	private final static double carryInit = 8;
	
	protected WorkDeliveryman(AIModule_Work module, PlanBlueprint[] map, RoomBlueprintIns<?> b) {
		super(module, b, map);
	}

	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		
		RoomInstance i = (RoomInstance) work(a);
		STORAGE_CRATE_HASSER st = (STORAGE_CRATE_HASSER) i;
		TILE_STORAGE c = st.job(a.tc(), d.path);
		if (c == null) {
			return null;
		}
		RESOURCE res = c.resource();
		
		int max = carryCap(i, a, d); 
		
		d.planByte1 = (byte) CLAMP.i(max, 0, c.storageReservable());

		c.storageReserve(d.planByte1);
		
		d.planTile.set(c.x(), c.y());
		
		AISubActivation s = fetch.activateFound(a, d, res, d.planByte1, st.getsMaximum(res), st.fetchesFromEveryone(res));
		if (s == null) {
			unreserve(a, d);
		}
		return s;
		
	}
	
	static int carryCap(RoomInstance i, Humanoid a, AIManager d) {
		STORAGE_CRATE_HASSER st = (STORAGE_CRATE_HASSER) i;
		Boostable b = st.carryBonus();
		double am = carryInit*(1+b.get(a.indu()));
		return CLAMP.i((int)(Math.ceil(am)), 1, 48);
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 48) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			d.planByte2 = resource(a, d).bIndex();
			return return_resource.set(a, d);
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			
			unreserve(a, d);
		}
	};
	
	private void unreserve(Humanoid a, AIManager d) {
		TILE_STORAGE c = targetStorage(a, d);
		if (c != null) {
			int i = CLAMP.i(d.planByte1, 0, c.storageReserved());
			c.storageUnreserve(i);
		}
	}
	
	private TILE_STORAGE targetStorage(Humanoid a, AIManager d) {
		Room r = ROOMS().map.get(d.planTile);
		if (r != null && r instanceof StorageCrate.STORAGE_CRATE_HASSER) {
			return ((StorageCrate.STORAGE_CRATE_HASSER) r).job(d.planTile.x(), d.planTile.y());
		}
		return null;
	}
	
	private final Resumer return_resource = new Resumer("returning resources") {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (!con(a, d)) {
				can(a, d);
				return WAIT_AND_EXIT.set(a, d);
			}
			return AI.SUBS().walkTo.coo(a, d, d.planTile);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!con(a, d)) {
				can(a, d);
				return WAIT_AND_EXIT.set(a, d);
			}
			TILE_STORAGE c = targetStorage(a, d);
			int am = d.resourceA();
			am = CLAMP.i(am, 0, c.storageReserved());
			c.storageDeposit(am);
			
			int res = d.planByte1-am;
			if (res > 0)
				c.storageUnreserve(res);
			
			d.resourceAInc(-am);
			d.resourceDrop(a);
			
			int i = d.resourceA()-am;
			if (i > 0)
				d.resourceDrop(a);
			d.resourceCarriedSet(null);
			return WAIT_AND_EXIT.set(a, d);

		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			if (work(a) != null) {
				TILE_STORAGE c = targetStorage(a, d);
				if (c != null && c.storageReserved() > 0 && c.resource().bIndex() == d.planByte2) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			unreserve(a, d);
			d.resourceDrop(a);
		}


	};
	
	
}