package settlement.room.law.stocks;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.RES;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.path.finder.SFinderRoomService;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomAreaWrapper;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ROOM_STOCKS extends RoomBlueprintImp implements PUNISHMENT_SERVICE{

	private final MConstructor constructor;
	final Instance instance;
	int used;
	int total;
	
	public final SFinderRoomService finder = new SFinderRoomService("Stock") {
		private int x,y;
		private final FSERVICE s = new FSERVICE() {
			
			final RoomAreaWrapper wrap = new RoomAreaWrapper();
			
			@Override
			public int y() {
				return y;
			}
			
			@Override
			public int x() {
				return x;
			}
			
			@Override
			public boolean findableReservedIs() {
				return SETT.ROOMS().data.get(x, y) >= 1;
			}
			
			@Override
			public boolean findableReservedCanBe() {
				return SETT.ROOMS().data.get(x, y) == 0;
			}
			
			@Override
			public void findableReserveCancel() {
				if (findableReservedIs()) {
					finder.report(x, y, 1);
					used --;
				}
				wrap.done();
				wrap.init(instance, x, y);
				SETT.ROOMS().data.set(wrap.area(), x, y, 0);
			}
			
			@Override
			public void findableReserve() {
				if (!findableReservedIs()) {
					finder.report(x, y, -1);
					used ++;
				}
				wrap.done();
				wrap.init(instance, x, y);
				SETT.ROOMS().data.set(wrap.area(), x, y, 1);
			}

			@Override
			public void consume() {
				findableReserveCancel();
			}
			
			@Override
			public void startUsing() {
				if (findableReservedIs()) {
					wrap.done();
					wrap.init(instance, x, y);
					SETT.ROOMS().data.set(wrap.area(), x, y, 2);
				}
			};
		};
		
		@Override
		public FSERVICE get(int tx, int ty) {
			if (constructor.service(tx, ty)) {
				x = tx;
				y = ty;
				return s;
			}
			return null;
		}
	};
	
	public ROOM_STOCKS(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(init, 0, "_STOCKS", cat);
		this.constructor = new MConstructor(this, init);
		this.instance = new Instance(init.m, this);
		
	}

	
	@Override
	protected void save(FilePutter f) {
		f.i(total);
		f.i(used);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		total = f.i();
		used = f.i();
	}

	@Override
	protected void clear() {
		total = 0;
		used = 0;
	}
	
	@Override
	public Room get(int tx, int ty) {
		if (ROOMS().map.get(tx, ty) == instance)
			return instance;
		return null;
	}

	@Override
	protected void update(float ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	@Override
	public MConstructor constructor() {
		return constructor;
	}
	


	public DIR dir(int tx, int ty, DIR d) {
		FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
		if (it == null)
			return d;
		if ((RES.ran2().get(tx, ty) & 1) == 0)
			return DIR.ORTHO.getC(it.rotation-1);
		return DIR.ORTHO.getC(it.rotation+1);
	}
	
	public boolean isStock(int tx, int ty) {
		return constructor.service(tx, ty);
	}
	
	public boolean isUsed(int tx, int ty) {
		return finder.getReserved(tx, ty) != null;
	}


	@Override
	public int punishTotal() {
		return total;
	}


	@Override
	public int punishUsed() {
		return used;
	}

	
}
