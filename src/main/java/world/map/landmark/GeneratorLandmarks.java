package world.map.landmark;

import static world.World.*;

import init.RES;
import init.paths.PATHS;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;

class GeneratorLandmarks {

	private final Polymap polly = new Polymap(TWIDTH(), THEIGHT(), 40*(TWIDTH()/256), 1.0);
	private final WorldLandmark rubbish = LANDMARKS().getByIndex(0);
	private final Rec work = new Rec();
	
	
	private final Json json = new Json(PATHS.NAMES().get("WorldLandmarks"));
	
	private final Type[] types = new Type[] {
		new Type(json, "MOUNTAIN", 40, 1000) {

			@Override
			public boolean is(int tx, int ty) {
				return MOUNTAIN().heighter.get(tx, ty) > 0 && !WATER().has.is(tx,ty);
			}
		},
		new Type(json, "LAKE", 20, 10000) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().LAKE.is(tx, ty);
			}
		},
		new Type(json, "RIVER", 25, 100) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().isRivery.is(tx, ty);
			}
		},
		new Type(json, "OCEAN", 50, 5000) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().isOCEAN.is(tx, ty);
			}
		},
	};
	
	
	
	
	public GeneratorLandmarks() {
		
		
		
		for (COORDINATE c : TBOUNDS()) {
			LANDMARKS().setter.set(c, null);
		}
		
		int nr = 1;
		
		for (COORDINATE c : TBOUNDS()) {
			if (nr >= WorldLandmarks.MAX)
				break;
			if (assignTerrain(c.x(), c.y(), LANDMARKS().getByIndex(nr)))
				nr++;
		}
		

		
		

		final Bitmap1D check = new Bitmap1D(TAREA(), true);
		
		
		for (COORDINATE c : TBOUNDS()) {
			if (check.get(c.x()+c.y()*TWIDTH()))
				continue;
			if (LANDMARKS().setter.get(c) == rubbish) {
				LANDMARKS().setter.set(c, null);
				continue;
			}
			if (LANDMARKS().setter.get(c) == null) {
				continue;
			}
			init(c, check);
			
		}
		
		
		
	}
	
	private boolean assignTerrain(int tx, int ty, WorldLandmark ass) {
		
		if (LANDMARKS().setter.get(tx, ty) != null) {
			return false;
		}
		
		Type type = type(tx, ty);
		if (type == null)
			return false;
		
		
		polly.checkInit();
		polly.checker.set(tx, ty, true);
		
		

		int minSize = type.minSize;
		int maxSize = type.maxSize;
		
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(tx, ty, 0);
		int area = 0;
		
		WorldLandmark neigh = null;
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			LANDMARKS().setter.set(t, ass);
			
			
			
			area++;
			
			if (area > maxSize)
				break;
			
			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (TBOUNDS().holdsPoint(dx, dy)) {
					if (!type.is(dx, dy))
						continue;
					
					WorldLandmark kuk = LANDMARKS().setter.get(dx, dy);
					if (kuk != null) {
						if (kuk != rubbish && kuk != ass)
							neigh = kuk;
						continue;
					}
						
					
					double q = t.getValue() + d.tileDistance();
					double dd = polly.checker.is(t, d) ? q : q+100;
					polly.checker.set(t, true);
					RES.flooder().pushSmaller(t, d, dd);
				}
			}
			
		}
		
		RES.flooder().done();
		
		if (area < minSize) {
			if (neigh != null) {
				assign(tx, ty, ass, neigh);
			}else {
				assign(tx, ty, ass, rubbish);
			}
			return false;
		}
		
		type.init(ass);
		return true;
	}
	
	private void init(COORDINATE start, Bitmap1D check) {
		
		RES.filler().init(this);
		RES.filler().filler.set(start);
		
		
		WorldLandmark a = LANDMARKS().setter.get(start);
		
		int x1 = TWIDTH();
		int x2= -1;
		int y1 = THEIGHT();
		int y2 = -1;
		int area = 0;
		
		while(RES.filler().hasMore()) {
			COORDINATE c = RES.filler().poll();
			if (!LANDMARKS().setter.is(c, a)) {
				if (c.x() < x1)
					x1 = c.x();
				if (c.x() > x2)
					x2 = c.x();
				if (c.y()< y1)
					y1 = c.y();
				if (c.y() > y2)
					y2 = c.y();
			}else {
				area++;
				check.set(c.x()+c.y()*TWIDTH(), true);
				for (DIR d : DIR.ORTHO)
					if (TBOUNDS().holdsPoint(c, d))
						RES.filler().filler.set(c, d);
			}
			
		}

		
		RES.filler().done();
		
		
		RES.flooder().init(this);
		work.set(x1,x2,y1,y2);
		int cx = work.cX();
		int cy = work.cY();
		int dist = Integer.MAX_VALUE;
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				if (!LANDMARKS().setter.is(x, y, a) || work.isOnEdge(x, y))
					RES.flooder().pushSloppy(x, y, 0);
			}
		}
		
		PathTile t = null;
		while (RES.flooder().hasMore()) {
			t = RES.flooder().pollSmallest();
			
			if (LANDMARKS().setter.is(t, a)) {
				if (COORDINATE.tileDistance(t.x(), t.y(), work.cX(), work.cY()) < dist) {
					cx = t.x();
					cy = t.y();
					dist = (int) COORDINATE.tileDistance(t.x(), t.y(), work.cX(), work.cY());
				}
			}
			
			for (DIR d : DIR.ORTHO) {
				if (work.holdsPoint(t, d))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		RES.flooder().done();
		
		if (t.getValue() > 14)
			a.init(t.x(), t.y(), area, 3);
		else if (t.getValue() > 8)
			a.init(t.x(), t.y(), area, 2);
		else if (t.getValue() > 5)
			a.init(t.x(), t.y(), area, 1);
		else {
			a.init(cx, cy, area, 0);
			
			
		}

		
	}
	

	

	
	private void assign(int tx, int ty, WorldLandmark old, WorldLandmark newa) {
		
		RES.flooder().init(this);
		
		RES.flooder().pushSloppy(tx, ty, 0);
		
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			if (LANDMARKS().setter.get(t) != old)
				continue;
			LANDMARKS().setter.set(t, newa);
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
			
		}
		
		RES.flooder().done();
	}
	
	public Type type(int tx, int ty) {
		for (Type t : types)
			if (t.is(tx, ty))
				return t;
		return null;
	}
	
	private abstract static class Type implements MAP_BOOLEAN{
		
		private final String[] names;
		private int nameI = 0;
		private final Json[] specials;
		private int sI = 0;
		private int minSize;
		private int maxSize;
		
		Type(Json json, String key, int min, int max){
			this.minSize = min;
			this.maxSize = max;
			names = json.texts(key);
			for (int i = 0; i < names.length; i++) {
				int k = RND.rInt(names.length);
				String o = names[i];
				names[i] = names[k];
				names[k] = o;
			}
			
			specials = json.json("SPECIAL").jsons(key);
			
			for (int i = 0; i < specials.length; i++) {
				int k = RND.rInt(specials.length);
				Json o = specials[i];
				specials[i] = specials[k];
				specials[k] = o;
			}
		}
		
		@Override
		public boolean is(int tile) {
			return false;
		}
		
		void init(WorldLandmark l) {
			
			
			if (sI < specials.length) {
				l.name.clear().add(specials[sI].text("NAME"));
				l.description.clear().add(specials[sI].text("LORE"));
				if (l.description.length() > 1024)
					specials[sI].error("Lore is too long...", "LORE");
				sI ++;
			}else {
				if (names.length == 0) {
					l.name.clear().add(l.index);
				}else {
					l.name.clear().add(names[nameI++]);
					if (nameI >= names.length)
						nameI = 0;
				}
			}
		}
		
		
	}
	
}
