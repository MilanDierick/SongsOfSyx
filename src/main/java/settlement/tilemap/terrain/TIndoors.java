package settlement.tilemap.terrain;

import static settlement.main.SETT.*;

import java.io.IOException;

import settlement.misc.util.FINDABLE;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap2D;

public class TIndoors implements MAP_BOOLEAN{

	private final Bitmap2D reservable = new Bitmap2D(TILE_BOUNDS, false);
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
				service.findableReserve();
		}
	}
	
	void add(int tx, int ty) {
		if (is(tx, ty)) {
			x = tx;
			y = ty;
			service.findableReserveCancel();
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
			return reservable.is(x+y*TWIDTH);
		}

		@Override
		public void findableReserve() {
			if (!findableReservedCanBe()) {
				throw new RuntimeException();
			}
			
			PATH().finders.indoor.report(this, -1);
			reservable.set(x+y*TWIDTH, false);
		}

		@Override
		public boolean findableReservedIs() {
			return !reservable.is(x+y*TWIDTH);
		}

		@Override
		public void findableReserveCancel() {
			if (findableReservedIs()) {
				reservable.set(x+y*TWIDTH, true);
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
