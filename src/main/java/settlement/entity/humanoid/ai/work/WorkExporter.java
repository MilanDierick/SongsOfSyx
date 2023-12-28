package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.*;

import game.boosting.Boostable;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.main.SETT;
import settlement.room.infra.export.ExportInstance;
import settlement.room.infra.export.ExportWork;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;

final class WorkExporter extends PlanBlueprint {

	private static final double carryInit = 6;
	
	protected WorkExporter(AIModule_Work module, PlanBlueprint[] map) {
		super(module, ROOMS().EXPORT, map);
	}

	private final RBITImp bit = new RBITImp();
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		
		ExportInstance i = (ExportInstance) work(a);
		
		if (!i.workHas())
			return null;
		
		if (i.resource() == null)
			return null;
		
		ExportWork inter = i.work();
		
		bit.clear();
		if (inter.reservable(i.resource()) > 0)
			bit.or(i.resource());
		
		if (bit.isClear()) {
			i.workFail();
			return null;
		}
		int max = carryCap(i, a, d);
		max = CLAMP.i(max, 0, inter.reservable(i.resource()));
		AISubActivation s = fetch.activate(a, d, bit, max, i.radius(), true, true); 
		
		if (s == null) {
			i.workFail();
			return null;
		}
		
		inter = i.work();
		d.planByte1 = (byte) max;
		inter.reserve(fetch.resource(a, d), d.planByte1);
		
		return s;
	}
	
	static int carryCap(RoomInstance i, Humanoid a, AIManager d) {
		Boostable b = SETT.ROOMS().STOCKPILE.bonus();
		double am = carryInit*(1+b.get(a.indu()));
		return CLAMP.i((int)(Math.ceil(am)), 1, 48);
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 48) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			if (work(a) == null) {
				d.resourceDrop(a);
				return null;
			}
			d.planByte3 = resource(a, d).bIndex();
			return return_resource.set(a, d);
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			ExportInstance i = (ExportInstance) work(a);
			if (i == null)
				return;
			ExportWork inter = i.work();
			inter.reserve(resource(a, d), -d.planByte1);
		}
	};

	private final Resumer return_resource = new Resumer("bringing ware to depot") {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			ExportInstance i = (ExportInstance) work(a);
			ExportWork inter = i.work();
			RESOURCE r = ress(d);
			
			COORDINATE c = inter.getReservableSpot(a.physics.tileC().x(), a.physics.tileC().y(), r);
			
			if (c == null) {
				inter.reserve(r, -d.planByte1);
				d.resourceDrop(a);
				return null;
			}
			
			d.planTile.set(c);
			inter.reserve(r, -d.planByte1);
			int am = CLAMP.i(d.resourceA(), 0, inter.reservable(r, c));
			d.planByte2 = (byte) am;
			d.planByte1 -= am;
			inter.reserve(r, c, d.planByte2);
			inter.reserve(r, d.planByte1);
			
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
			
			
			if (s == null) {
				can(a, d);
				return null;
			}
			
			
			return s;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			ExportInstance i = (ExportInstance) work(a);
			ExportWork inter = i.work();
			RESOURCE r = ress(d);
			COORDINATE c = d.planTile;
			
			int am = CLAMP.i(d.planByte2, 0, inter.reserved(r, c));
			inter.finish(r, c, am);
			d.resourceAInc(-am);
			d.planByte2 -= am;
			
			inter.reserve(ress(d), d.planTile, -CLAMP.i(d.planByte2, 0, inter.reserved(ress(d), d.planTile)));
			if (d.resourceA() > 0) {
				return set(a, d);
			}
			can(a, d);
			return null;

		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			ExportInstance i = (ExportInstance) work(a);
			if (i == null)
				return false;
			ExportWork inter = i.work();
			return inter.reserved(ress(d), d.planTile) > 0;
		}
		

		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceDrop(a);
			ExportInstance i = (ExportInstance) work(a);
			if (i == null)
				return;
			ExportWork inter = i.work();
			inter.reserve(ress(d), -d.planByte1);
			if (inter.reserved(ress(d), d.planTile) > 0) {
				inter.reserve(ress(d), d.planTile, -CLAMP.i(d.planByte2, 0, inter.reserved(ress(d), d.planTile)));
			}
		}

		
		private RESOURCE ress(AIManager d) {
			return RESOURCES.ALL().get(d.planByte3);
		}

	};

	
}