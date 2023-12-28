package settlement.tilemap.growth;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.TileMap;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LinkedList;
import view.sett.IDebugPanelSett;
import view.tool.*;

public class Fertility extends TileMap.Resource{

	public static final int MAX = 0x0F;
	public static final double MAXI = 1.0/MAX;
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA);
	
	public static CharSequence ¤¤name = "¤Fertility";
	
	
	static {
		D.ts(Fertility.class);
	}
		
	public Fertility(TileMap creator) throws IOException{
		
		LinkedList<PLACABLE> placables = new LinkedList<PLACABLE>();
		
		
		for (int i = 0; i <= MAX; i++) {
			
			final int k = i;
			placables.add(new PlacableMulti("base: " + i, null, SPRITES.icons().m.cancel) {
				
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					if (!IN_BOUNDS(tx, ty))
						return;
					base.set(tx, ty, k);
					
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
					return null;
				}
			});
			
		}
		
		placables.add(new PlacableMulti("base increase") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!IN_BOUNDS(tx, ty))
					return;
				base.increment(tx, ty, 1);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!IN_BOUNDS(tx, ty))
					return null;
				int i = tx+ty*TWIDTH;
				int v = base.get(i);
				return v < MAX ? null : E;
			}
		});
		
		placables.add(new PlacableMulti("decrease") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!IN_BOUNDS(tx, ty))
					return;
				base.increment(tx, ty, -1);
					
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!IN_BOUNDS(tx, ty))
					return E;
				int i = tx+ty*TWIDTH;
				int v = base.get(i);
				return v > 0 ? null : E;
			}
		});
		
		IDebugPanelSett.add("fertility", placables);
		//PanelSettlement.addPlacer(h);
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		data.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		data.load(saveFile);
	}

	@Override
	protected void clearAll() {
		data.clear();
	}
	
	public final MAP_DOUBLE target = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			return CLAMP.d(base.get(tx, ty)*MAXI + SETT.ENV().environment.WATER_SWEET.get(tx, ty)*0.5, 0, 1);
		}
		
		@Override
		public double get(int tile) {
			return CLAMP.d(base.get(tile)*MAXI + SETT.ENV().environment.WATER_SWEET.get(tile)*0.5, 0, 1);
		}
	};
	
	public final MAP_INTE base = new MAP_INTE.INT_MAPEImp(TWIDTH, THEIGHT) {
		
		@Override
		public int get(int tile) {
			return data.get(tile);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			value = CLAMP.i(value, 0, MAX);
			data.set(tile, value);
			return this;
		}
	};
	
	public final MAP_DOUBLEE baseD = new MAP_DOUBLEE() {
		
		@Override
		public double get(int tx, int ty) {
			return base.get(tx, ty)*MAXI;
		}
		
		@Override
		public double get(int tile) {
			return base.get(tile)*MAXI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			base.set(tile, (int) (value*MAX));
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			base.set(tx, ty, (int) (value*MAX));
			return this;
		}
	};
	

	
	
}