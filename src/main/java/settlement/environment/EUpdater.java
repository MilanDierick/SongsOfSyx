package settlement.environment;

import static settlement.environment.SettEnvMap.*;
import static settlement.main.SETT.*;

import init.RES;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.misc.util.TileRayTracer;
import settlement.misc.util.TileRayTracer.Ray;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

abstract class EUpdater {

	private static final TileRayTracer traces = new TileRayTracer(RADIUS);
	private static final IntChecker rCheck = new IntChecker(traces.rays().length);
	private static final ArrayList<Ray> rays = new ArrayList<Ray>(traces.rays().length);
	private static final int MAXR = SettEnvMap.RADIUS-1;

	private static final Rec eArea = new Rec();
	
	
	public abstract void update(SettEnv s, RECTANGLE bounds, RECTANGLE area);
	public abstract void addExtraView(RECTANGLE area, Flooder f, SettEnv thing, double value, double radius, int tx, int ty, int w, int h);
	public abstract double getExtraValue(SettEnv s, double g, int tx, int ty);
	
	public static EUpdater tracer = new EUpdater() {
		
		
		@Override
		public void update(SettEnv s, RECTANGLE bounds, RECTANGLE area) {
			for (COORDINATE c : area) {
				s.map.set(c.x() + c.y() * SETT.TWIDTH, 0);
				RES.flooder().setValue2(c, 0);
			}
			
			for (COORDINATE c : bounds) {
				if (tracetest(area, s, c)) {
					trace(c.x(), c.y(), s, area);
				}
			}
			
			for (COORDINATE c : area) {
				s.map.set(c.x()+c.y()*TWIDTH, CLAMP.i((int)Math.ceil(RES.flooder().getValue2(c.x(), c.y())), 0, s.max));
			}
			
		}
		

		
		@Override
		public void addExtraView(RECTANGLE area, Flooder f, SettEnv thing, double value, double radius, int tx, int ty, int w, int h) {
			for (COORDINATE c : area) {
				RES.flooder().setValue2(c, 0);
			}
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (SETT.IN_BOUNDS(tx + x, ty + y)) {
						trace(tx + x, ty + y, thing, value, radius, area);
					}
				}
			}
			
			eArea.set(area);
			
//			for (COORDINATE c : area) {
//				RES.flooder().close(c.x(), c.y(), RES.flooder().getValue2(c.x(), c.y())/thing.max);
//			}
//			return;
			
		}

		@Override
		public double getExtraValue(SettEnv s, double g, int tx, int ty) {
			if (!eArea.holdsPoint(tx, ty))
				return g;
			double d = Math.ceil(RES.flooder().getValue2(tx, ty));
			d /= s.max;
			g += d;
			return g;
		}
	};
	
	private static boolean tracetest(RECTANGLE area, SettEnv s, COORDINATE c) {
		int ra = (int) (s.getRadius(c.x(), c.y())*MAXR);
		if (ra == 0)
			return false;
		if (s.getBaseValue(c.x(), c.y()) == 0)
			return false;
		if (area.holdsPoint(c))
			return true;
		if (Math.abs(area.cX()-c.x())-ra > RADIUS/2)
			return false;
		if (Math.abs(area.cY()-c.y())-ra > RADIUS/2)
			return false;
		return true;
	}
	
	private static void trace(int sourceX, int sourceY, SettEnv s, RECTANGLE area) {
		
		trace(sourceX, sourceY, s, s.getBaseValue(sourceX, sourceY), s.getRadius(sourceX, sourceY), area);

	}
	
	private static void trace(int sourceX, int sourceY, SettEnv s, double value, double radius, RECTANGLE area) {
		
		radius = (int)(radius*MAXR);
		
		traces.checkInit();

		
		
		for (Ray r : rays(sourceX, sourceY, area)){
			double cost = 0;
			for (int i = 0; i < r.size(); i++) {
				COORDINATE d = r.get(i);
				int dx = d.x()+sourceX;
				int dy = d.y()+sourceY;
				if (!SETT.IN_BOUNDS(dx, dy))
					break;
				
				
				if (r.radius(i) + cost >= radius)
					break;
				
				if (area.holdsPoint(dx, dy)) {
					if (traces.check(d)) {
						double rv = (radius-r.radius(i)-cost)/radius;
						double vv = value*rv;
						double v = (RES.flooder().getValue2(dx, dy) + vv*s.max);
						RES.flooder().setValue2(dx, dy, v);
					}
					//

				}
				cost+= s.getCost(dx, dy)-1;
				
			}
			
		}

	}
	
	private static LIST<Ray> rays(int sourceX, int sourceY, RECTANGLE area) {
		rays.clear();
		
		if (area.touches(sourceX, sourceY)) {
			rays.add(traces.rays());
			return rays;
		}
		
		rCheck.init();
		
		int y1 = area.y1()-sourceY;
		int y2 = area.y2()-sourceY;
		for (int x = area.x1(); x < area.x2(); x++) {
			int dx = x-sourceX;
			for (Ray r : traces.rays(dx, y1))
				if (!rCheck.isSetAndSet(r.index))
					rays.add(r);
			for (Ray r : traces.rays(dx, y2))
				if (!rCheck.isSetAndSet(r.index))
					rays.add(r);
		}
		
		int x1 = area.x1()-sourceX;
		int x2 = area.x2()-sourceX;
		
		for (int y = area.y1(); y <= area.y2(); y++) {
			int dy = y-sourceY;
			for (Ray r : traces.rays(x1, dy))
				if (!rCheck.isSetAndSet(r.index))
					rays.add(r);
			for (Ray r : traces.rays(x2, dy))
				if (!rCheck.isSetAndSet(r.index))
					rays.add(r);
		}
		return rays;
	}
	
	public static EUpdater water = new EUpdater() {
		
		@Override
		public void update(SettEnv s, RECTANGLE bounds, RECTANGLE area) {
			RES.flooder().init(this);
			for (COORDINATE c : area) {
				s.map.set(c.x() + c.y() * SETT.TWIDTH, 0);
				if (s.getBaseValue(c.x(), c.y()) == 0) {
					RES.flooder().pushSloppy(c, 0);
					RES.flooder().setValue2(c, 0);
				}else {
					RES.flooder().setValue2(c, s.max*s.getBaseValue(c.x(), c.y()));
				}
			}
			
			RES.coos().set(0);
			
			while(RES.flooder().hasMore()) {
				PathTile t = RES.flooder().pollGreatest();
				if (s.getBaseValue(t.x(), t.y()) > 0) {
					RES.coos().get().set(t);
					RES.coos().inc();
					continue;
				}
				for (DIR d : DIR.ALL) {
					if (bounds.holdsPoint(t, d)) {
						RES.flooder().pushSloppy(t, d, 0);
					}
				}
			}
			RES.flooder().done();
			
			int k = RES.coos().getI();
			
			for (int ci = 0; ci < k; ci++) {
				COORDINATE c = RES.coos().set(ci);
				if (tracetest(area, s, c)) {
					double v = s.getBaseValue(c.x(), c.y());
					traces.checkInit();
					for (Ray r : rays(c.x(), c.y(), area)){
						double cost = 0;
						for (int i = 0; i < r.size(); i++) {
							COORDINATE d = r.get(i);
							int dx = d.x()+c.x();
							int dy = d.y()+c.y();
							if (!SETT.IN_BOUNDS(dx, dy))
								break;
							
							
							if (r.radius(i) + cost >= MAXR)
								break;
							
							if (area.holdsPoint(dx, dy)) {
								if (traces.check(d)) {
									double rv = (MAXR-r.radius(i)-cost)/MAXR;
									v +=  s.getBaseValue(dx, dy)*rv;
								}

							}
							cost+= s.getCost(dx, dy)-1;
							
						}
						
					}
					
					trace(c.x(), c.y(), s, v, s.getRadius(c.x(), c.y()), area);
				}
			}

			
			for (COORDINATE c : area) {
				s.map.set(c.x()+c.y()*TWIDTH, CLAMP.i((int)Math.round(RES.flooder().getValue2(c.x(), c.y())), 0, s.max));
			}
			
		}
		
		@Override
		public void addExtraView(RECTANGLE area, Flooder f, SettEnv thing, double value, double radius, int tx, int ty,
				int w, int h) {
			double b = value;
			double dr = maxRadiusI / (radius) * b;

			if (b > 0) {
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						if (SETT.IN_BOUNDS(tx + x, ty + y)) {
							RES.flooder().pushSloppy(tx + x, ty + y, b);
							RES.flooder().setValue2(tx + x, ty + y, dr);
						}
					}
				}

			}
			
			while (f.hasMore()) {
				PathTile t = f.pollGreatest();
				dr = t.getValue2();
				value = t.getValue();

				for (DIR d : DIR.ALL) {
					tx = t.x() + d.x();
					ty = t.y() + d.y();
					double v = value - dr * d.tileDistance() * thing.getCost(tx, ty);
					if (v > 0) {
						if (f.pushGreater(tx, ty, v) != null) {
							f.setValue2(tx, ty, dr);
						}
					}
				}
			}

			
		}

		@Override
		public double getExtraValue(SettEnv s, double g, int tx, int ty) {
			g = Math.max(g, RES.flooder().getValue(tx, ty));
			return g;
		}
	};
	
	public static EUpdater water2 = new EUpdater() {
		
		@Override
		public void update(SettEnv s, RECTANGLE bounds, RECTANGLE area) {
			RES.flooder().init(this);
			for (COORDINATE c : area) {
				s.map.set(c.x() + c.y() * SETT.TWIDTH, 0);
				if (s.getBaseValue(c.x(), c.y()) == 0) {
					RES.flooder().pushSloppy(c, 0);
					RES.flooder().setValue2(c, 0);
				}else {
					RES.flooder().setValue2(c, s.getBaseValue(c.x(), c.y()));
				}
			}
			
			
			
			while(RES.flooder().hasMore()) {
				PathTile t = RES.flooder().pollGreatest();
				if (s.getBaseValue(t.x(), t.y()) > 0) {
					RES.coos().get().set(t);
					RES.coos().inc();
					continue;
				}
				for (DIR d : DIR.ALL) {
					if (bounds.holdsPoint(t, d)) {
						RES.flooder().pushSloppy(t, d, 0);
					}
				}
			}
			RES.flooder().done();
			
			int k = RES.coos().getI();
			
			for (COORDINATE c : area) {
				if (s.getBaseValue(c.x(), c.y()) != 0)
					continue;
				rCheck.init();
				double v = 0;
				for (int ci = 0; ci < k && v < 1; ci++) {
					COORDINATE so = RES.coos().set(ci);
					
					for (Ray r : traces.rays(c.x()-so.x(), c.y()-so.y())) {
						if (rCheck.isSetAndSet(r.index)) {
							continue;
						}
						if (v >= 1)
							break;
						
						double cost = 0;
						for (int i = 0; i < r.size() && v < 1; i++) {
							COORDINATE d = r.get(i);
							int dx = -d.x()+c.x();
							int dy = -d.y()+c.y();
							if (!SETT.IN_BOUNDS(dx, dy))
								break;
							
							
							if (r.radius(i) + cost >= MAXR)
								break;
							
							double b = s.getBaseValue(dx, dy);
							if (b > 0) {
								double rv = (MAXR-r.radius(i)-cost)/MAXR;
								v += b*rv;
							}
							cost+= s.getCost(dx, dy)-1;
							
						}
						
					}
				}
				RES.flooder().setValue2(c, v);
					
			}
			
			for (COORDINATE c : area) {
				s.map.set(c.x()+c.y()*TWIDTH, CLAMP.i((int)Math.round(RES.flooder().getValue2(c.x(), c.y())*s.max), 0, s.max));
			}
			
		}
		
		@Override
		public void addExtraView(RECTANGLE area, Flooder f, SettEnv thing, double value, double radius, int tx, int ty,
				int w, int h) {
			double b = value;
			double dr = maxRadiusI / (radius) * b;

			if (b > 0) {
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						if (SETT.IN_BOUNDS(tx + x, ty + y)) {
							RES.flooder().pushSloppy(tx + x, ty + y, b);
							RES.flooder().setValue2(tx + x, ty + y, dr);
						}
					}
				}

			}
			
			while (f.hasMore()) {
				PathTile t = f.pollGreatest();
				dr = t.getValue2();
				value = t.getValue();

				for (DIR d : DIR.ALL) {
					tx = t.x() + d.x();
					ty = t.y() + d.y();
					double v = value - dr * d.tileDistance() * thing.getCost(tx, ty);
					if (v > 0) {
						if (f.pushGreater(tx, ty, v) != null) {
							f.setValue2(tx, ty, dr);
						}
					}
				}
			}

			
		}

		@Override
		public double getExtraValue(SettEnv s, double g, int tx, int ty) {
			g = Math.max(g, RES.flooder().getValue(tx, ty));
			return g;
		}
	};
	
	public static EUpdater flooder = new EUpdater() {
		
		@Override
		public void update(SettEnv s, RECTANGLE bounds, RECTANGLE area) {
			for (COORDINATE c : area) {
				s.map.set(c.x() + c.y() * SETT.TWIDTH, 0);
			}
			RES.flooder().init(this);
			flood2(RES.flooder(), bounds, area, s);
			RES.flooder().done();
			
		}
		
		private void flood2(Flooder f, RECTANGLE bounds, RECTANGLE area, SettEnv thing) {

			double r = 1.0/thing.radius();
			
			for (COORDINATE c : bounds) {
				double b = thing.getBaseValue(c.x(), c.y())*RADIUS;
				if (b > 0) {
					f.pushGreater(c, b);
				}
			}

			while (f.hasMore()) {
				PathTile t = f.pollGreatest();
				double value = t.getValue();

				for (DIR d : DIR.ALL) {
					int tx = t.x() + d.x();
					int ty = t.y() + d.y();
					if (bounds.holdsPoint(tx, ty)) {
						double v = value - d.tileDistance() * thing.getCost(tx, ty)*r;
						if (v > 0) {
							f.pushGreater(tx, ty, v);
						}
					}
				}
			}

			for (COORDINATE c : area) {
				if (f.hasBeenPushed(c.x(), c.y())) {
					int v = (int) Math.ceil(thing.max * f.getValue(c)*maxRadiusI);
					thing.map.set(c.x() + c.y() * SETT.TWIDTH, v);
				}
			}

		}
		
		@Override
		public void addExtraView(RECTANGLE area, Flooder f, SettEnv thing, double value, double radius, int tx, int ty,
				int w, int h) {
			double b = value;
			double dr = maxRadiusI / (radius) * b;

			if (b > 0) {
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						if (SETT.IN_BOUNDS(tx + x, ty + y)) {
							RES.flooder().pushSloppy(tx + x, ty + y, b);
							RES.flooder().setValue2(tx + x, ty + y, dr);
						}
					}
				}

			}
			
			while (f.hasMore()) {
				PathTile t = f.pollGreatest();
				dr = t.getValue2();
				value = t.getValue();

				for (DIR d : DIR.ALL) {
					tx = t.x() + d.x();
					ty = t.y() + d.y();
					double v = value - dr * d.tileDistance() * thing.getCost(tx, ty);
					if (v > 0) {
						if (f.pushGreater(tx, ty, v) != null) {
							f.setValue2(tx, ty, dr);
						}
					}
				}
			}

			
		}

		@Override
		public double getExtraValue(SettEnv s, double g, int tx, int ty) {
			g = Math.max(g, RES.flooder().getValue(tx, ty));
			return g;
		}
	};
	
}
