package settlement.environment;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.RES;
import init.boostable.BBoost;
import init.boostable.BOOSTABLES;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.environment.ENVIRONMENT.EnvResource;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.path.AvailabilityListener;
import settlement.room.main.Room;
import settlement.stats.STANDING.StandingDef;
import settlement.stats.STAT;
import settlement.stats.STATS;
import snake2d.Errors;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.info.INFO;
import util.keymap.RCollection;

public final class SettEnvMap extends EnvResource {

	public final SettEnv NOISE;
	public final SettEnv LIGHT;
	public final SettEnv SPACE;
	public final SettEnv WATER_SWEET;
	public final SettEnv WATER_SALT;

	public final RCollection<SettEnv> rmap;
	private final ArrayList<SettEnv> all;

	private final Chunks chunks = new Chunks();
	private final Updater updater = new Updater();

	public static final int RADIUS = 16;
	static final double maxRadiusI = 1.0 / RADIUS;

	public SettEnvMap() {
		new AvailabilityListener() {

			@Override
			protected void changed(int tx, int ty, AVAILABILITY a, AVAILABILITY old, boolean playerChange) {
				chunks.change(tx, ty);
			}
		};

		PATH jp = PATHS.INIT().getFolder("settlement").getFolder("environment");
		PATH tp = PATHS.TEXT().getFolder("settlement").getFolder("environment");

		String[] keys = jp.getFiles();

		LinkedList<SettEnv> all = new LinkedList<>();
		final KeyMap<SettEnv> kmap = new KeyMap<>();

		NOISE = new SettEnv(all, "_NOISE", jp, tp, 2, EUpdater.flooder) {

			@Override
			public double getCost(int toX, int toY) {
				if (SETT.LIGHTS().los().get(toX, toY).blocksEnv(toX, toY))
					return 8;
				if (SETT.TERRAIN().get(toX, toY).roofIs())
					return 3;
				return 1;
			}

		};
		kmap.put(NOISE.key, NOISE);
		LIGHT = new SettEnv(all, "_LIGHT", jp, tp, 2, EUpdater.tracer) {

			@Override
			public double getCost(int toX, int toY) {
				return SETT.LIGHTS().los().get(toX, toY).blocksEnv(toX, toY) ? RADIUS : 1;
			}
		};
		SPACE = new SettEnv(all, "_SPACE", jp, tp, 4, EUpdater.flooder) {

			@Override
			public double getCost(int toX, int toY) {
				return 1;
			}

			@Override
			public double get(int tile) {
				return 1.0 - super.get(tile);
			}

			@Override
			double getBaseValue(int tx, int ty) {
				return SETT.PATH().solidity.is(tx, ty) ? 1 : 0;
			}

			@Override
			public double radius() {
				return 0.5;
			}
			
			@Override
			double getRadius(int tx, int ty) {
				return 0.5;
			}
		};
		kmap.put(LIGHT.key, LIGHT);
		
//		WATER_SWEET = new SettEnv(all, "_WATER_SWEET", jp, tp, 4, EUpdater.water2) {
//
//			@Override
//			public double getCost(int toX, int toY) {
//				return 1;
//			}
//
//			@Override
//			double getBaseValue(int tx, int ty) {
//				if(SETT.TERRAIN().WATER.isWater(tx, ty))
//					return !SETT.TERRAIN().WATER.isSalty.is(tx, ty) ? 1.0/16 : 0;
//				return super.getBaseValue(tx, ty);
//			}
//			
//			@Override
//			public double radius() {
//				return 1.0;
//			}
//			
//			@Override
//			double getRadius(int tx, int ty) {
//				return 1.0;
//			}
//			
//
//		};
		WATER_SWEET = new SettEnv(all, "_WATER_SWEET", jp, tp, 4, EUpdater.flooder) {

			@Override
			public double getCost(int toX, int toY) {
				return 1;
			}

			@Override
			double getBaseValue(int tx, int ty) {
				if(SETT.TERRAIN().WATER.isWater(tx, ty))
					return !SETT.TERRAIN().WATER.isSalty.is(tx, ty) ? SETT.TERRAIN().WATER.radius.get(tx, ty) : 0;
				return super.getBaseValue(tx, ty);
			}
			
			@Override
			public double radius() {
				return 1.0;
			}
			

		};
		
		kmap.put(WATER_SWEET.key, WATER_SWEET);
		
		WATER_SALT = new SettEnv(all, "_WATER_SALT", jp, tp, 1, EUpdater.flooder) {

			@Override
			public double getCost(int toX, int toY) {
				return 1;
			}

			@Override
			double getBaseValue(int tx, int ty) {
				if (SETT.TERRAIN().WATER.isWater(tx, ty))
					return SETT.TERRAIN().WATER.isSalty.is(tx, ty) ? SETT.TERRAIN().WATER.radius.get(tx, ty) : 0;
				return super.getBaseValue(tx, ty);
			}

			@Override
			public double radius() {
				return 1.0;
			}
		};
		kmap.put(WATER_SWEET.key, WATER_SWEET);

		if (keys.length > 32 - all.size())
			throw new Errors.DataError("Too many environments declared, max is " + (32 - all.size()), jp.get());

		for (String k : keys) {
			SettEnv e = new SettEnv(all, k, jp, tp, 4, EUpdater.tracer);
			kmap.put(e.key, e);
		}

		this.all = new ArrayList<SettEnvMap.SettEnv>(all);

		kmap.expand();

		rmap = new RCollection<SettEnvMap.SettEnv>("ENVIRONMENT", kmap) {

			@Override
			public SettEnv getAt(int index) {
				return all.get(index);
			}

			@Override
			public LIST<SettEnv> all() {
				return all;
			}
		};

	}

	@Override
	protected void save(FilePutter file) {

	}

	@Override
	protected void load(FileGetter file) throws IOException {

	}

	@Override
	protected void init() {
		
		long n = System.currentTimeMillis();
		
		//System.out.println("here");
		
		for (SettEnv s : all) {
			s.map.clear();
		}

		chunks.clear();

		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				for (SettEnv t : all) {
					if (t.getBaseValue(tx, ty) > 0) {
						chunks.change(tx, ty, t.bit);
					}
				}
			}
		}
		while (chunks.has()) {
			int m = chunks.nextMask();
			COORDINATE c = chunks.next();
			updater.update(c.x(), c.y(), m);
		}
		//System.out.println("TIME: " + (System.currentTimeMillis()-n));
	}
	
	public void initWater() {
		WATER_SWEET.map.clear();
		chunks.clear();

		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				if (WATER_SWEET.getBaseValue(tx, ty) > 0) {
					chunks.change(tx, ty, WATER_SWEET.bit);
				}
			}
		}
		while (chunks.has()) {
			int m = chunks.nextMask();
			COORDINATE c = chunks.next();
			updater.update(c.x(), c.y(), m);
		}
	}

	public void setChanged(int tx, int ty) {
		chunks.change(tx, ty);
	}

	@Override
	protected void update(double ds) {
		updater.update(ds);
	}

	public LIST<SettEnv> all() {
		return all;
	}

	@Override
	protected void generate(CapitolArea area) {
		// TODO Auto-generated method stub

	}

	public static class SettEnvValue {

		private final static SettEnvValue self = new SettEnvValue();

		private SettEnvValue() {

		};

		public double radius;
		public double value;
	}

	public class SettEnv extends INFO implements MAP_DOUBLE, INDEXED {

		private final int index;
		private int extraI = -1;
		public final String key;
		public final double declineSpeed;
		public final LIST<BBoost> bonuses;
		public final StandingDef standing;
		final Bitsmap1D map;
		final int max;
		private final double maxI;
		private final int bit;
		private final EUpdater uper;

		SettEnv(LISTE<SettEnv> all, String key, PATH pj, PATH tj, int bits, EUpdater uper) {
			super(new Json(tj.get(key)));
			this.key = key;
			index = all.add(this);
			bit = 1 << index;
			Json j = new Json(pj.get(key));
			declineSpeed = j.d("DECLINE_VALUE", 0, 1);
			bonuses = BOOSTABLES.boosts(j);
			standing = new StandingDef(j);
			map = new Bitsmap1D(0, bits, SETT.TAREA);
			max = (1 << bits) - 1;
			maxI = 1.0 / max;
			this.uper = uper;
		}

		@Override
		public double get(int tile) {
			double v = map.get(tile) * maxI;
			// Add floor
			if (SETT.FLOOR().getter.get(tile) != null && !SETT.ROOMS().map.is(tile))
				v += SETT.FLOOR().getter.get(tile).envValue(this);
			return CLAMP.d(v, 0, 1);
		}

		public double getCost(int toX, int toY) {
			return SETT.LIGHTS().los().get(toX, toY).blocksEnv(toX, toY) ? RADIUS : 1;
		}

		double getRadius(int tx, int ty) {
			if (SETT.ROOMS().construction.isser.is(tx, ty) || SETT.ROOMS().placement.embryo.is(tx, ty))
				return 0;
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r.constructor() != null && r.constructor().envValue(this, SettEnvValue.self, tx, ty)) {
				return SettEnvValue.self.radius;
			}
			return 0;
		}

		double getBaseValue(int tx, int ty) {
			if (SETT.ROOMS().construction.isser.is(tx, ty) || SETT.ROOMS().placement.embryo.is(tx, ty))
				return 0;
			
			Room r = SETT.ROOMS().map.get(tx, ty);

			if (r != null && !SETT.ROOMS().construction.isser.is(tx, ty) && r.constructor() != null
					&& r.constructor().envValue(this, SettEnvValue.self, tx, ty)) {
				return SettEnvValue.self.value;
			}
			return 0;
		}

		@Override
		public double get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return 0;
			return get(tx + ty * SETT.TWIDTH);
		}

		public void addExtraView(double value, double radius, int tx, int ty, int w, int h) {
			RES.flooder().init(this);
			updater.addExtraView(RES.flooder(), this, value, radius, tx, ty, w, h);
			RES.flooder().done();
			
			extraI = GAME.updateI();
		}

		public double getView(int tx, int ty) {
			double g = get(tx, ty);
			if (extraI == GAME.updateI()) {
				g = uper.getExtraValue(this, g, tx, ty);
				g = CLAMP.d(g, 0, 1);
			}

			return g;
		}

		@Override
		public int index() {
			return index;
		}

		public STAT stat() {
			return STATS.ACCESS().all().get(index);
		}
		
		public int max() {
			return max;
		}
		
		public double radius() {
			return 1.0;
		}

	}

	private final class Chunks {

		private final int mask = ~(RADIUS - 1);
		private final int width = SETT.TWIDTH / RADIUS;
		private final int size = width * SETT.THEIGHT / RADIUS;

		private final Bitmap1D changed = new Bitmap1D(size, false);
		private final int[] upMasks = new int[size];
		private final QueueInteger updatables = new QueueInteger(size + 1);
		private Coo coo = new Coo();

		Chunks() {

		}

		public void change(int tx, int ty, int mask) {

			if (!SETT.IN_BOUNDS(tx, ty))
				return;
			int x1 = tx - RADIUS;
			int x2 = tx + RADIUS;
			int y1 = ty - RADIUS;
			int y2 = ty + RADIUS;

			for (int y = y1; y <= y2; y+=RADIUS) {
				for (int x = x1; x <= x2; x+=RADIUS) {
					if (SETT.IN_BOUNDS(x, y)) {
						changeP(x, y, mask);
					}
				}
			}

		}

		public void changeP(int tx, int ty, int mask) {
			int c = getChunk(tx, ty);
			upMasks[c] |= mask;
			if (!changed.get(c)) {
				changed.set(c, true);
				updatables.push(c);

			}
		}

		private int getChunk(int tx, int ty) {
			tx &= mask;
			ty &= mask;
			int c = tx / RADIUS + width * (ty / RADIUS);
			return c;
		}

		public void change(int tx, int ty) {

			if (!SETT.IN_BOUNDS(tx, ty))
				return;
			
			

			int m = 0;
			for (SettEnv e : all) {
				if (e.getBaseValue(tx, ty) > 0)
					m |= e.bit;
				else {
					for (int di = 0; di < DIR.ALL.size(); di++) {
						DIR d = DIR.ALL.get(di);
						int dx = tx + d.x();
						int dy = ty + d.y();
						if (IN_BOUNDS(dx, dy) && e.map.get(dx+dy*TWIDTH) > 0) {
							m|= e.bit;
							break;
						}
					}
				}
				
			}
			if (m != 0)
				change(tx, ty, m);
		}

		void clear() {
			changed.clear();
			updatables.clear();
			Arrays.fill(upMasks, 0);
		}

		public boolean has() {
			return updatables.hasNext();
		}

		private int pt;
		
		public COORDINATE next() {
			int x = pt % width;
			int y = pt / width;
			coo.set(RADIUS * x, RADIUS * y);
			changed.set(pt, false);
			upMasks[pt] = 0;
			return coo;
		}

		public int nextMask() {
			pt = updatables.poll();
			return upMasks[pt];
		}

	}

	private class Updater {

		private double timer = 0;
		private double chunksPerTick = 1;

		protected void update(double ds) {
			timer += chunksPerTick;
			if (timer < 1)
				return;
			int am = (int) timer;
			timer -= am;
			while (chunks.has() && am > 0) {
				int m = chunks.nextMask();
				COORDINATE c = chunks.next();
				update(c.x(), c.y(), m);
				am--;
			}
		}

		private final Rec bounds = new Rec();
		private final Rec area = new Rec(RADIUS);
		private final int MAXR = RADIUS-1;

		private void update(int tx, int ty, int mask) {
			int x1 = CLAMP.i(tx - RADIUS, 0, SETT.TWIDTH);
			int x2 = CLAMP.i(tx + RADIUS * 2, 0, SETT.TWIDTH);
			int y1 = CLAMP.i(ty - RADIUS, 0, SETT.THEIGHT);
			int y2 = CLAMP.i(ty + RADIUS * 2, 0, SETT.THEIGHT);
			bounds.set(x1, x2, y1, y2);
			area.set(tx, CLAMP.i(tx + RADIUS, 0, SETT.TWIDTH), ty, CLAMP.i(ty + RADIUS, 0, SETT.THEIGHT));
			
			for (SettEnv s : all) {
				if ((s.bit & mask) == 0)
					continue;
				s.uper.update(s, bounds, area);
				
			}
		}

	

		public void addExtraView(Flooder f, SettEnv thing, double value, double radius, int tx, int ty, int w, int h) {
			if (value <= 0)
				return;
			
			int rr = (int) Math.ceil(radius*MAXR);
			area.set(CLAMP.i(tx -rr, 0, SETT.TWIDTH), CLAMP.i(tx +rr+1, 0, SETT.TWIDTH), CLAMP.i(ty - rr, 0, SETT.THEIGHT), CLAMP.i(ty +1 + rr, 0, SETT.THEIGHT));
			
			thing.uper.addExtraView(area, f, thing, value, radius, tx, ty, w, h);
			

			RES.flooder().done();

		}

	}

}
