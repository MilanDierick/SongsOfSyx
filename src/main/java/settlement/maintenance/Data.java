package settlement.maintenance;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.finder.SFinderFindable;
import settlement.room.main.Room;
import settlement.tilemap.Floors.Floor;
import snake2d.util.file.*;
import snake2d.util.map.*;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap1D;

final class Data implements SAVABLE{

	private final Bitmap1D bisser = new Bitmap1D(SETT.TAREA, false);
	private final Bitsmap1D bresource = new Bitsmap1D(0, 4, SETT.TAREA);
	private final Bitmap1D breserved = new Bitmap1D(SETT.TAREA, false);
	
	private final Service service = new Service();
	
	@Override
	public void save(FilePutter file) {
		bisser.save(file);
		breserved.save(file);
		bresource.save(file);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		bisser.load(file);
		breserved.load(file);
		bresource.load(file);
	}
	@Override
	public void clear() {
		bisser.clear();
		breserved.clear();
		bresource.clear();
	}
	
	public final MAP_OBJECT<RESOURCE> resource = new MAP_OBJECT<RESOURCE>() {

		@Override
		public RESOURCE get(int tile) {
			return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}

		@Override
		public RESOURCE get(int tx, int ty) {
			int tile = tx + ty*SETT.TWIDTH;
			int i = bresource.get(tile);
			if (i == 0)
				return null;
			if (!is.is(tile))
				return null;
			i -= 1;
			Room room = ROOMS().map.get(tx, ty);
			if (room != null) {
				if (room.constructor() != null && room.constructor().resources() > 0)
					return room.constructor().resource(i%room.constructor().resources());
				return null;
			}
			
			if (SETT.MAINTENANCE().floor.validate(tx, ty)) {
				Floor f = SETT.FLOOR().getter.get(tx, ty);
				return f.resource;
			}
			return null;
		}

	};
	
	public final MAP_INTE resourceSetter = new MAP_INTE() {
		
		@Override
		public int get(int tx, int ty) {
			return get(tx+ty*SETT.TWIDTH);
		}
		
		@Override
		public int get(int tile) {
			return bresource.get(tile);
		}
		
		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			return set(tx+ty*SETT.TWIDTH, value);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			bresource.set(tile, value);
			return this;
		}
	};
	
	private final MAP_BOOLEANE is = new MAP_BOOLEANE() {

		@Override
		public boolean is(int tile) {
			return bisser.get(tile);
		}

		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*SETT.TWIDTH);
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			bisser.set(tile, value);
			return this;
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			return set(tx+SETT.TWIDTH*ty, value);
		}
		
	};
	
	private final MAP_BOOLEANE reserved = new MAP_BOOLEANE() {

		@Override
		public boolean is(int tile) {
			return breserved.get(tile);
		}

		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*SETT.TWIDTH);
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			breserved.set(tile, value);
			return this;
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			return set(tx+SETT.TWIDTH*ty, value);
		}
		
	};
	
	public final MAP_PLACER setter = new MAP_PLACER() {
		
		@Override
		public boolean is(int tile) {
			return bisser.get(tile);
		}

		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*SETT.TWIDTH);
		}
		
		@Override
		public MAP_PLACER set(int tx, int ty) {
			if(!is.is(tx, ty)) {
				reserved.set(tx, ty, false);
				is.set(tx, ty, true);
				reserved.set(tx, ty, false);
				add(tx, ty);
			}
			return this;
		}
		
		@Override
		public MAP_PLACER set(int tile) {
			return set(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
		
		@Override
		public MAP_PLACER clear(int tx, int ty) {
			if (is.is(tx, ty)) {
				remove(tx, ty);
				is.set(tx, ty, false);
				reserved.set(tx, ty, false);
			}
			return this;
		}
		
		@Override
		public MAP_PLACER clear(int tile) {
			return clear(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
	};
	
	void remove(int tx, int ty) {
	
		FINDABLE f = finder.getReservable(tx, ty);
		if (f != null)
			finder.report(f, -1);
	}
	
	void add(int tx, int ty) {
		FINDABLE f = finder.getReservable(tx, ty);
		if (f != null)
			finder.report(f, 1);
	}
	
	final class Service implements FINDABLE {

		int x, y;

		Service() {
		}

		Service init(int tx, int ty) {
			this.x = tx;
			this.y = ty;
			return this;
		}

		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}

		@Override
		public boolean findableReservedCanBe() {
			return !reserved.is(x, y);
		}

		@Override
		public void findableReserve() {
			if (findableReservedCanBe()) {
				reserved.set(x, y, true);
				finder.report(x, y, -1);
			}
		}

		@Override
		public boolean findableReservedIs() {
			return reserved.is(x, y);
		}

		@Override
		public void findableReserveCancel() {
			if (findableReservedIs()) {
				reserved.set(x, y, false);
				finder.report(x, y, 1);
			}
		}

	}
	
	public final SFinderFindable finder = new SFinderFindable("maintenance") {


		@Override
		public FINDABLE getReservable(int tx, int ty) {
			if (is.is(tx, ty) && service.init(tx, ty).findableReservedCanBe())
				return service;
			return null;
		}

		@Override
		public FINDABLE getReserved(int tx, int ty) {
			if (is.is(tx, ty) && service.init(tx, ty).findableReservedIs())
				return service;
			return null;
		}
	};
	
	
}
