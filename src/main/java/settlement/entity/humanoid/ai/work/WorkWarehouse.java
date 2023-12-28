package settlement.entity.humanoid.ai.work;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.stockpile.StockpileInstance;
import snake2d.util.misc.CLAMP;

final class WorkWarehouse extends PlanBlueprint{

	private final WorkDeliveryman deliveryman;
	
	protected WorkWarehouse(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().STOCKPILE, map);
		map[SETT.ROOMS().STOCKPILE.index()] = null;
		deliveryman = new WorkDeliveryman(module, map, blueprint);
		map[SETT.ROOMS().STOCKPILE.index()] = this;
	}
	
	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {
		AiPlanActivation p = deliveryman.activate(a, d);
		if (p != null)
			return p;
		return super.activate(a, d);
		
	}
	

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {

		return emptyTo.set(a, d);
	}

	private final Resumer emptyTo = new Resumer("Emptying to another warehouse") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			StockpileInstance i = (StockpileInstance) work(a);
			
			TILE_STORAGE s = i.emptyJob(a.tc(), d.path);
			if (s == null) {
				return null;
			}
			d.planTile.set(s.x(), s.y());
			
			int m = CLAMP.i(WorkDeliveryman.carryCap(i, a, d), 0, s.storageReservable());
			RESOURCE_TILE r = i.resourceTile(d.path.destX(), d.path.destY());
			m = CLAMP.i(m, 0, r.reservable());
			d.planByte3 = (byte) m;
			d.planByte2 = r.resource().bIndex();
			for (int k = 0; k < m; k++) {
				r.findableReserve();
			}
			
			i = i.blueprintI().get(d.planTile.x(), d.planTile.y());
			s = i.storage(d.planTile.x(), d.planTile.y());
			s.storageReserve(m);
			
			return AI.SUBS().walkTo.path(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			RESOURCE_TILE r = ((StockpileInstance) work(a)).resourceTile(d.path.destX(), d.path.destY());
			d.resourceCarriedSet(r.resource());
			for (int k = 0; k < d.planByte3 & r.findableReservedIs(); k++) {
				r.resourcePickup();
				d.resourceAInc(1);
			}
			return emptyTo2.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			final StockpileInstance i = (StockpileInstance) work(a);
			if (i == null)
				return false;
			RESOURCE_TILE r = i.resourceTile(d.path.destX(), d.path.destY());
			if (r == null)
				return false;
			if (!r.findableReservedIs())
				return false;
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			StockpileInstance i = (StockpileInstance) work(a);
			if (i != null) {
				RESOURCE_TILE r = i.resourceTile(d.path.destX(), d.path.destY());
				if (r != null && r.resource() != null && r.resource().bIndex() == d.planByte2) {
					for (int k = 0; k < d.planByte3 & r.findableReservedIs(); k++) {
						r.findableReserveCancel();
					}
				}

			}
			i = SETT.ROOMS().STOCKPILE.getter.get(d.planTile);
			if (i != null) {
				TILE_STORAGE s = i.storage(d.planTile.x(), d.planTile.y());
				if (s != null && s.resource() != null && s.resource().bIndex() == d.planByte2) {
					s.storageUnreserve(d.planByte3);
				}				
			}
			
			
		}
	};
	
	private final Resumer emptyTo2 = new Resumer("Emptying to another warehouse") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (!con(a, d)) {
				can(a, d);
				return null;
			}
			
			AISubActivation ss = AI.SUBS().walkTo.coo(a, d, d.planTile);
			
			if (ss == null)
				can(a, d);
			
			return ss;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			StockpileInstance i = SETT.ROOMS().STOCKPILE.getter.get(d.planTile);
			TILE_STORAGE s = i.storage(d.planTile.x(), d.planTile.y());
			
			s.storageUnreserve(d.planByte3);
			int am = d.resourceA();
			am = CLAMP.i(am, 0, s.storageReservable());
			s.storageReserve(am);
			s.storageDeposit(am);
			d.resourceAInc(-am);
			d.resourceDrop(a);
			
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			StockpileInstance i = SETT.ROOMS().STOCKPILE.getter.get(d.planTile);
			if (i == null)
				return false;
			TILE_STORAGE s = i.storage(d.planTile.x(), d.planTile.y());
			if (s == null || s.resource() == null || s.storageReserved() <= 0 || s.resource().bIndex() != d.planByte2)
				return false;
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			StockpileInstance i = SETT.ROOMS().STOCKPILE.getter.get(d.planTile);
			if (i != null) {
				TILE_STORAGE s = i.storage(d.planTile.x(), d.planTile.y());
				if (s != null && s.resource() != null &&  s.resource().bIndex() == d.planByte2) {
					s.storageUnreserve(d.planByte3);
				}				
			}
			d.resourceDrop(a);
		}
	};
}
