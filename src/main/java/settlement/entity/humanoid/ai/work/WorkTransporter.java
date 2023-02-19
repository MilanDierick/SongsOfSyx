package settlement.entity.humanoid.ai.work;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.transport.ROOM_DELIVERY_INSTANCE;
import settlement.room.infra.transport.TransportInstance;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.datatypes.Coo;
import snake2d.util.misc.CLAMP;

final class WorkTransporter extends PlanBlueprint{

	private final WorkDeliveryman deliveryman;
	private final WorkAbs work;
	
	protected WorkTransporter(AIModule_Work module, PlanBlueprint[] map, Works w) {
		super(module, SETT.ROOMS().TRANSPORT, map);
		map[SETT.ROOMS().TRANSPORT.index()] = null;
		deliveryman = new WorkDeliveryman(module, map, blueprint);
		map[SETT.ROOMS().TRANSPORT.index()] = null;
		work = new WorkAbs(module, blueprint, map, w);
		map[SETT.ROOMS().TRANSPORT.index()] = this;
	}

	int transportAmount(Humanoid a, AIManager d) {
		if (getResumer(d) == go) {
			return am(d);
		}
		return -1;
	}
	
	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {
		TransportInstance ins = (TransportInstance) work(a);
		AiPlanActivation p = super.activate(a, d);
		if (p != null)
			return p;
		
		p = work.activate(a, d);
		
		if (p != null) {
			return p;
		}
		
		
		
		if (ins.hasStorage())
			return deliveryman.activate(a, d);
		
		return null;
	}
	

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {

		
		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.7)
			return null;
		Coo[] job = w(a).getDeliveryJob();
		
		if (job == null)
			return null;
		
		AISubActivation s = AI.SUBS().walkTo.coo(a, d, job[0]);
		if (s == null)
			return null;
		
		d.planByte1 = w(a).resource().bIndex();
		d.planByte2 = (byte) w(a).deliveryAmount(job[0].x(), job[0].y());
		d.planTile.set(job[1]);
		
		Room del = SETT.ROOMS().map.get(job[1]);
		if (del == null) {
			return null;
		}
		
		TILE_STORAGE st = del.storage(job[1].x(), job[1].y());
		if (st == null)
			return null;
		
		d.planByte3 = (byte) CLAMP.i(am(d), 0, st.storageReservable());
		st.storageReserve(reserved(d));
		
		start.set(a, d);
		return s;
	}
	
	@Override
	public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) <= 0.7;
	}
	
	private TransportInstance w(Humanoid a) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins != null && ins instanceof TransportInstance)
			return (TransportInstance) ins;
		return null;
	}
	
	private RESOURCE ress(AIManager d) {
		return RESOURCES.ALL().get(d.planByte1);
	}
	
	private int am(AIManager d) {
		return d.planByte2 & 0x0FF;
	}
	
	private int reserved(AIManager d) {
		return d.planByte3 & 0x0FF;
	}
	
	private void unreserve(AIManager d, int tx, int ty) {
		Room r =  SETT.ROOMS().map.get(tx, ty);
		if (r != null) {
			TILE_STORAGE s = r.storage(tx, ty);
			if (s != null && s.resource() == ress(d)) {
				int am = reserved(d);
				am = CLAMP.i(am, 0, s.storageReserved());
				s.storageUnreserve(am);
			}
		}
	}
	
	private final Resumer start = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return go.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return w(a) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			if (w(a) != null) {
				w(a).deliveryJobCancel(d.path.destX(), d.path.destY());
			}
			unreserve(d, d.planTile.x(), d.planTile.y());
		}
	};
	
	private final Resumer go = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (w(a).doDeliveryJob(d.path.destX(), d.path.destY())) {
				int dx = d.planTile.x();
				int dy = d.planTile.y();
				d.planTile.set(d.path.destX(), d.path.destY());
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, dx, dy);
				if (s == null) {
					can(a, d);
					return null;
				}
				int ran = SETT.tileRan(dx, dy);
				SETT.HALFENTS().transports.make(a, dx, dy, ress(d), (byte) ran);
				return s;
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (deliver(a, d)) {
				return AI.SUBS().WORK_HANDS.activate(a, d, 4);
			}
			
			AISubActivation s = tryNext(a, d);
			if (s != null)
				return s;
			
			can(a, d);
			return null;
			

			
		}
		
		private boolean deliver(Humanoid a, AIManager d) {
			Room r =  SETT.ROOMS().map.get(d.path.destX(), d.path.destY());
			if (r == null) {
				return false;
			}
			TILE_STORAGE s = r.storage(d.path.destX(), d.path.destY());
			if (s == null) {
				return false;
			}
			
			int extra = CLAMP.i(am(d)-reserved(d), 0, s.storageReservable());
			if (extra > 0) {
				s.storageReserve(extra);
				d.planByte3 = (byte) (reserved(d)+extra);
			}
			
			int am = CLAMP.i(reserved(d), 0, s.storageReserved());
			am = CLAMP.i(am, 0, 16);
			if (am == 0) {
				return false;
			}else {
				s.storageDeposit(am);
				d.planByte3 = (byte) (reserved(d)-am);
				d.planByte2 = (byte) (am(d)-am);
				return true;
			}
		}
		
		private AISubActivation tryNext(Humanoid a, AIManager d) {
			Room r =  SETT.ROOMS().map.get(d.path.destX(), d.path.destY());
			d.planByte3 = 0;
			if (r == null) {
				return null;
			}
			if (am(d) <= 6) {
				return null;
			}
			if (r instanceof ROOM_DELIVERY_INSTANCE) {
				ROOM_DELIVERY_INSTANCE ins = (ROOM_DELIVERY_INSTANCE) r;
				TILE_STORAGE s = ins.getDeliveryCrate(ress(d).bit, 6);
				if (s == null)
					return null;
				AISubActivation ss = AI.SUBS().walkTo.coo(a, d, s);
				if (ss != null) {
					r =  SETT.ROOMS().map.get(d.path.destX(), d.path.destY());
					if (r != null) {
						s = r.storage(d.path.destX(), d.path.destY());
						if (s != null && s.resource() == ress(d)) {
							int reserved = CLAMP.i(am(d), 0, s.storageReservable());
							s.storageReserve(reserved);
							d.planByte3 = (byte) reserved;
							return ss;
						}
					}
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
			if (w(a) != null) {
				w(a).finishDeliveryJob(d.planTile.x(), d.planTile.y());
			}
			unreserve(d, d.path.destX(), d.path.destY());
			SETT.THINGS().resources.create(a.tc(), ress(d), am(d));
		}
	};

}
