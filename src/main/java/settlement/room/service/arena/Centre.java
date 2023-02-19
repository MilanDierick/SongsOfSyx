package settlement.room.service.arena;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.Coo;

final class Centre {
	
	private final Bits activated = 	new Bits(0b0000_0000_0000_0000_0000_0000_0000_0001);
	private final Bits dused = 		new Bits(0b0000_0000_0000_0000_0000_0000_0000_0010);
	private ArenaInstance ins;
	private final Coo coo = new Coo();
	private int data;
	private final ROOM_ARENA b;
	
	Centre(ROOM_ARENA b){
		this.b = b;
	}
	
	public FSERVICE service(int tx, int ty) {
		if (init(tx, ty))
			return service;
		return null;
	}
	
	public boolean init(int tx, int ty) {
		ins = b.getter.get(tx, ty);
		if (ins != null && SETT.ROOMS().fData.tileData.get(tx, ty) == ArenaConstructor.STATION) {
			coo.set(tx, ty);
			data = SETT.ROOMS().data.get(tx, ty);
			return true;
		}
		return false;
	}
	
	void activate(int tx, int ty) {
		if (init(tx, ty)) {
			data = activated.set(data, 1);
			save();
		}
	}
	
	void deactivate(int tx, int ty) {
		if (init(tx, ty)) {
			data = activated.set(data, 0);
			save();
		}
	}
	
	private void save() {
		int ndata = data;
		data = SETT.ROOMS().data.get(coo);
		if (ndata == data)
			return;
		if (service.findableReservedCanBe()) { 
			ins.service.report(service, ins.blueprintI().data, -1);
		}
		data = ndata;
		if (service.findableReservedCanBe()) { 
			ins.service.report(service, ins.blueprintI().data, 1);
		}
		SETT.ROOMS().data.set(ins, coo, data);
		
	}
	
	private final FSERVICE service = new FSERVICE(){
		
		@Override
		public void consume() {
			
		}
		
		@Override
		public int x() {
			return coo.x();
		}

		@Override
		public int y() {
			return coo.y();
		}

		@Override
		public boolean findableReservedCanBe() {
			return dused.get(data) == 0 && activated.get(data) == 1;
		}

		@Override
		public void findableReserve() {
			if (!findableReservedCanBe()) {
				throw new RuntimeException();	
			}
			data = dused.set(data, 1);
			save();
		}

		@Override
		public boolean findableReservedIs() {
			return dused.get(data) == 1;
		}

		@Override
		public void findableReserveCancel() {
			data = dused.set(data, 0);
			save();
		}
		
	};
	
}
