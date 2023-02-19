package settlement.army.ai.general;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import init.C;
import settlement.army.ai.general.Pathing.PATHCOST;
import settlement.army.formation.DivPositionAbs;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.util.file.*;
import snake2d.util.map.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap1D;

final class PathingMap implements SAVABLE{


	private final Context c;
	public BM path = new BM(); 
	public final Cost cost = new Cost();
	public final PATHCOST CostNoPath = new PATHCOST() {
		
		public MAP_DOUBLE abs = new MAP_DOUBLE() {
			
			@Override
			public double get(int tx, int ty) {
				if (!AbsMap.bounds.holdsPoint(tx, ty))
					return -1;
				return get(tx+ty*AbsMap.W);
			}
			
			@Override
			public double get(int tile) {
				return cost.values[tile];
			}
		};
		
		@Override
		public double get(int tx, int ty) {
			return getBaseValue(tx, ty);
		}
		
		@Override
		public double get(int tile) {
			return getBaseValue(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
		
		@Override
		public MAP_DOUBLE abs() {
			return abs;
		}
	};
	
	
	public PathingMap(Context context) {
		c = context;
	}
	
	void init() {
		clear();
		cost.init();
	}
	
	public void markPath(GDiv div, PathTile t) {

		while(t.getParent() != null) {
			this.path.setter.set(t);
			t = t.getParent();
		}

		
	}
	

	
	public void markPos(DivPositionAbs f) {
		for (int i = 0; i < f.deployed(); i++) {
			path.setter.set(f.tile(i));
		}
		
	}
	
	public void unmarkPos(DivPositionAbs f) {
		
		for (int i = 0; i < f.deployed(); i++) {
			path.clearer.set(f.tile(i));
		}
		
	}
	


	
	@Override
	public void save(FilePutter file) {
		path.save(file);
		cost.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		path.load(file);
		cost.load(file);
	}

	@Override
	public void clear() {
		path.clear();
		cost.clear();
	}

	public final static class BM implements MAP_BOOLEAN {
		
		private final Bitmap1D data = new Bitmap1D(SETT.TAREA, false);
		public final AbsMap abs = new AbsMap(32 - Integer.numberOfLeadingZeros(AbsMap.size*AbsMap.size-1));
		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*TWIDTH);
		}
		
		@Override
		public boolean is(int tile) {
			return data.get(tile);
		}

		private void save(FilePutter file) {
			data.save(file);
			abs.save(file);
		}

		private void load(FileGetter file) throws IOException {
			data.load(file);
			abs.load(file);
		}

		public void clear() {
			data.clear();
			abs.clear();
		}
		
		public final MAP_SETTER setter = new MAP_SETTER() {
			
			@Override
			public MAP_SETTER set(int tx, int ty) {
				if (SETT.IN_BOUNDS(tx, ty)) {
					data.set(tx+ty*SETT.TWIDTH, true);
					int v = abs.get(AbsMap.getI(tx, ty))+1;
					v = CLAMP.i(v, 0, abs.max);
					abs.set(AbsMap.getI(tx, ty), v);
				}
				return this;
			}
			
			@Override
			public MAP_SETTER set(int tile) {
				throw new RuntimeException();
			}
		};
		
		private final MAP_SETTER clearer = new MAP_SETTER() {
			
			@Override
			public MAP_SETTER set(int tx, int ty) {
				if (SETT.IN_BOUNDS(tx, ty)) {
					if (data.get(tx+ty*SETT.TWIDTH)) {
						data.set(tx+ty*SETT.TWIDTH, false);
						int v = abs.get(AbsMap.getI(tx, ty))-1;
						v = CLAMP.i(v, 0, abs.max);
						abs.set(AbsMap.getI(tx, ty), v);
					}
					
				}
				return this;
			}
			
			@Override
			public MAP_SETTER set(int tile) {
				throw new RuntimeException();
			}
		};

	}
	
	public double getBaseValue(int dx, int dy) {
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(c.army)) {
			return 1 + SETT.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().environment.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}
	
	public final class Cost implements PATHCOST{
		
		private final short[] values = new short[SETT.TAREA/(AbsMap.size*AbsMap.size)];
		private final AbsMap attackable = new AbsMap(1);

		
		public double getValue(int dx, int dy) {
			if (c.pmap.path.is(dx, dy))
				return 256 + getBaseValue(dx, dy);
			return getBaseValue(dx, dy);
		}
		
		public MAP_DOUBLE abs = new MAP_DOUBLE() {
			
			@Override
			public double get(int tx, int ty) {
				if (!AbsMap.bounds.holdsPoint(tx, ty))
					return -1;
				return get(tx+ty*AbsMap.W);
			}
			
			@Override
			public double get(int tile) {
				return values[tile] + (path.abs.get(tile)*16);
			}
		};
		
		private void init() {
			
			attackable.clear();
			
			for (int y = 0; y < SETT.TWIDTH; y++) {
				for (int x = 0; x < SETT.THEIGHT; x++) {
					int i = AbsMap.getI(x, y);
					double m = values[i];
					m += getBaseValue(x, y);
					short s = (short) CLAMP.d(m, 0, Short.MAX_VALUE);
					values[i] = s;
					if (SETT.ARMIES().map.attackable.is(x, y, c.army))
						attackable.set(AbsMap.getI(x, y), 1);
				}
			}
			
			for (int i = 0; i < values.length; i++) {
				values[i] /= AbsMap.size;
			}
			
		}
		
		private void save(FilePutter file) {
			file.ss(values);
			attackable.save(file);
		}

		private void load(FileGetter file) throws IOException {
			file.ss(values);
			attackable.load(file);
		}

		private void clear() {
			Arrays.fill(values, (short)0);
		}

		@Override
		public double get(int tile) {
			return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}

		@Override
		public double get(int dx, int dy) {
			if (c.pmap.path.is(dx, dy))
				return 256 + getBaseValue(dx, dy);
			return getBaseValue(dx, dy);
		}

		@Override
		public MAP_DOUBLE abs() {
			return abs;
		}

	}
	



	
	
}
