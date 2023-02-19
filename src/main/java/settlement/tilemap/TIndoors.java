package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import settlement.misc.util.FINDABLE;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap1D;

public class TIndoors implements MAP_BOOLEAN{

	private final Bitmap1D reservable = new Bitmap1D(TAREA, false);
	private int x,y;
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			reservable.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			reservable.load(file);
		}
		
		@Override
		public void clear() {
			reservable.clear();
		}
	};
	
	void remove(int tx, int ty) {
		if (is(tx, ty)) {
			x = tx;
			y = ty;
			if (service.findableReservedCanBe())
				PATH().finders.indoor.report(service, -1);
		}
	}
	
	void add(int tx, int ty) {
		if (is(tx, ty)) {
			x = tx;
			y = ty;
			if (service.findableReservedCanBe())
				PATH().finders.indoor.report(service, 1);
		}
	}
	
	public FINDABLE findable(int tx, int ty) {
		if (is(tx, ty)) {
			x = tx;
			y = ty;
			return service;
		}
		return null;
	}
	
	private final FINDABLE service = new FINDABLE() {
		
		
		
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
			return !reservable.get(x+y*TWIDTH);
		}

		@Override
		public void findableReserve() {
			if (!findableReservedCanBe()) {
				throw new RuntimeException();
			}
			
			PATH().finders.indoor.report(this, -1);
			reservable.set(x+y*TWIDTH, true);
		}

		@Override
		public boolean findableReservedIs() {
			return reservable.get(x+y*TWIDTH);
		}

		@Override
		public void findableReserveCancel() {
			if (findableReservedIs()) {
				reservable.set(x+y*TWIDTH, false);
				PATH().finders.indoor.report(this, 1);
			}
			
		}
	};
	
	@Override
	public boolean is(int tile) {
		return TERRAIN().get(tile).roofIs() && PATH().availability.get(tile).player > 0;
	}

	@Override
	public boolean is(int tx, int ty) {
		return TERRAIN().get(tx, ty).roofIs() && PATH().availability.get(tx, ty).player > 0;
	}
}
