package settlement.environment;

import java.io.IOException;

import settlement.environment.ENVIRONMENT.EnvResource;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.rnd.HeightMap;
import snake2d.util.sets.Bitsmap1D;

public class SettFish extends EnvResource implements MAP_DOUBLEE{

	private final Bitsmap1D amounts = new Bitsmap1D(-1, 2, SETT.TAREA);
	private final double bI = 1.0/0b100;
	
	@Override
	protected void update(double ds) {
		
	}

	@Override
	protected void generate(CapitolArea area) {
		HeightMap height = new HeightMap(SETT.TWIDTH, SETT.THEIGHT, 32, 16);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			amounts.set(c.x()+c.y()*SETT.TWIDTH, (int)Math.round(0b11*height.get(c.x(), c.y())));
		}
	}

	@Override
	protected void save(FilePutter file) {
		amounts.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		amounts.load(file);
	}

	@Override
	public double get(int tile) {
		if (SETT.TERRAIN().WATER.is(tile)) {
			return (1 +amounts.get(tile))*bI;
		}
		return 0;
	}

	@Override
	public double get(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return get(tx+ty*SETT.TWIDTH);
		return 0;
	}

	@Override
	public MAP_DOUBLEE set(int tile, double value) {
		amounts.set(tile, (int) (0b11*value));
		return this;
	}

	@Override
	public MAP_DOUBLEE set(int tx, int ty, double value) {
		if (SETT.IN_BOUNDS(tx, ty))
			set(tx+ty*SETT.TWIDTH, value);
		return this;
	}
	


	
	

}
