package settlement.army.ai.general;

import java.io.IOException;

import settlement.main.SETT;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.map.MAP_INTE;
import snake2d.util.sets.Bitsmap1D;

final class AbsMap implements MAP_INTE, SAVABLE{
	
	public static final int size = 4;
	public static final int scroll = Integer.numberOfTrailingZeros(size);
	public static final int W = SETT.TWIDTH>>scroll;
	public static final RECTANGLE bounds = new Rec(W, SETT.THEIGHT>>scroll); 
	private static Rec tiles = new Rec.RecThreadSafe().setDim(size);
	private final Bitsmap1D is;
	public final int max;

	public AbsMap(int bits) {
		is = new Bitsmap1D(0, bits, SETT.TAREA/(size*size));
		max = (1 << bits)-1;
	}

	@Override
	public void save(FilePutter file) {
		is.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		is.load(file);
	}

	@Override
	public void clear() {
		is.clear();
	}
	
	public static RECTANGLE tiles(int absX, int absY) {
		tiles.moveX1Y1(absX<<scroll, absY<<scroll);
		return tiles;
	}
	
	public static int getI(int tx, int ty) {
		return (tx>>scroll) + W*(ty>>scroll);
	}
	
	public static int getI(COORDINATE tile) {
		return getI(tile.x(), tile.y());
	}

	@Override
	public int get(int tile) {
		return is.get(tile);
	}

	@Override
	public int get(int tx, int ty) {
		if (!bounds.holdsPoint(tx, ty))
			return 0;
		return get(tx+ty*W);
	}

	@Override
	public MAP_INTE set(int tile, int value) {
		is.set(tile, value);
		return this;
	}

	@Override
	public MAP_INTE set(int tx, int ty, int value) {
		if (bounds.holdsPoint(tx, ty))
			is.set(tx+ty*W, value);
		return this;
	}
	

}