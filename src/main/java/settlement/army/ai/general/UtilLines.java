package settlement.army.ai.general;

import java.io.IOException;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.*;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEAN;
import util.rendering.RenderData.RenderIterator;

final class UtilLines {

	private final Bitmap2D blocked = new Bitmap2D(SETT.TILE_BOUNDS, false);
	
	private final Context context;
	private final LineMap map;
	private final ArrayListGrower<BOOLEAN> steps = new ArrayListGrower<>();
	private int stepI;
	
	public final MAP_OBJECT<Line> getter;
	private final Maker maker = new Maker();
	private final Util u = new Util();
	private final LineFactory tmpFactory;
	private static final int DistanceToBlob = 4;
	private static final int DistanceToBlobMax = 8;
	
	public final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(stepI);
			map.save(file);
			tmpFactory.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			stepI = file.i();
			map.load(file);
			tmpFactory.load(file);
		}
		
		@Override
		public void clear() {
			stepI = 0;
		}
	};
	int keeps = 0;
	int nn = 0;

	UtilLines(Context context){
		this.context = context;
		map = new LineMap(context);
		getter = map;
		tmpFactory = new LineFactory(context);
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				blocked.clear();
				tmpFactory.clear();
				return false;
				
			}
		});
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				for (int i = 0; i < lines(); i++) {
					Line n = tmpFactory.next();
					Line o = line(i);
					n.tx1 = o.tx1;
					n.ty1 = o.ty1;
					n.dir = o.dir;
					n.tileLength = o.tileLength;
				}
				return false;
			}
		});
		
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				map.clear();
				keeps = 0;
				return false;
			}
		});
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				for (int i = 0; i < tmpFactory.freeI; i++) {
					Line o = tmpFactory.free.get(i);
					LineTmp l = maker.keep(o);
					if (l != null) {
						keeps ++;
						map.add(l);
					}
				}
				return false;
			}
		});
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				nn = 0;
				return false;
			}
		});
		
		steps.add(new BOOLEAN() {
			
			@Override
			public boolean is() {
				RECTANGLE rec = context.blob.area();
				for (int y = rec.y1()-1; y <= rec.y2(); y++) {
					for (int x = rec.x1()-1; x <= rec.x2(); x++) {
						if (makeNew(x, y) > 0)
							nn++;
					}
					
				}
				//LOG.ln(keeps + " " + nn);
				return false;

			}
		});
		
	}
	
	boolean update() {


		
		if (!steps.get(stepI).is()) {
			stepI++;
		}
		
		if (stepI >= steps.size()) {
			stepI = 0;
			return false;
		}
		return true;		
	}
	
	
	public void render(Renderer r, RenderIterator it) {
		
		if (map.is(it.tile())) {
			COLOR.RED100.bind();
			Line ll = getter.get(it.tx(), it.ty());
			if (ll.menMax == 0)
				COLOR.BLACK.bind();
			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
			Str.TMP.clear().add(ll.menMax);
			SPRITES.cons().ICO.arrows2.get(ll.dir.id()).render(r, it.x(), it.y());
			UI.FONT().S.render(r, Str.TMP, it.x(), it.y()+C.TILE_SIZE);
		}else if (blocked.is(it.tile())) {
			COLOR.WHITE50.bind();
			UI.icons().s.cancel.renderScaled(r, it.x(), it.y(), C.SCALE);
		}
	}
	
	private int makeNew(int x, int y) {
		DIR d = u.getDir(x, y);
		if (d != null) {
			LineTmp l = maker.make(x, y, d);
			if (l != null) {
				
				int back = Math.min(DistanceToBlob, l.maxBack-4);
				if (back > 0) {
					d = d.perpendicular();
					l.tx1 += d.x()*back;
					l.ty1 += d.y()*back;
				}
				
				map.add(l);
				return 1;
			}
		}
		return 0;
	}



	



	
	public int lines() {
		return map.fac.freeI;
	}
	
	public Line line(int i) {
		return map.fac.free.get(i);
	}

	private static class LineTmp {
		

		short tx1;
		short ty1;
		short tileLength;
		public DIR dir;
		public int menMax;
		public int maxBack;
		
		private LineTmp(){
			
		}
		
	}
	
	final static class Line {
		
		private Line next;
		short tx1;
		short ty1;
		public short menMax;
		short tileLength;
		public DIR dir = DIR.N;
		
		public int back;
		public boolean active;
		public byte mark;
		
		private Line(){
			
		}
		
		private void init(LineTmp s) {
			
			this.tx1 = s.tx1;
			this.ty1 = s.ty1;
			this.tileLength = s.tileLength;
			this.dir = s.dir;
			this.menMax = (short) s.menMax;
			next = null;
			back = 0;
			active = true;
			mark = 0;
		}
		
		
		
	}
	
	private class Maker {
		
		private final LineTmp line = new LineTmp();
		
		private LineTmp make(int x, int y, DIR dir) {
			
			if (!u.isValidStart(x, y, dir)) {
				u.block(x, y, null);
				return null;
			}
			

			if (u.enemiesInFront(x, y, dir, false) == 0) {
				u.block(x, y, null);
				return null;
			}
			
			if (u.getSpaceInBack(x, y, dir, false) < 4) {
				u.block(x, y, null);
				return null;
			}
			
			
			
			DIR side = dir.next(2);
			int forward = u.length(x, y, dir, side, 0);
			int back = u.length(x, y, dir, side.perpendicular(), forward);
			
			x -= back*side.x();
			y -= back*side.y();
			int length = back+forward;
			
			
			
			if (length < 3) {
				for (int i = 0; i <= length; i++) {
					int dx = (x + side.x()*i);
					int dy = (y + side.y()*i);
					u.block(dx, dy, side);
				}
				return null;
			}
			

			int maxMen = 0;
			int enemies = 0;
			line.maxBack = Integer.MAX_VALUE;
			for (int i = 0; i <= length; i++) {
				int dx = (x + side.x()*i);
				int dy = (y + side.y()*i);
				int b = u.getSpaceInBack(dx, dy, dir, true);
				line.maxBack = Math.min(line.maxBack, b);
				maxMen += b;
				enemies += u.enemiesInFront(dx, dy, dir, true);
				u.block(dx, dy, side);
			}
			line.tx1 = (short) (x);
			line.ty1 = (short) (y);
			line.tileLength = (short) length;
			line.dir = dir;
			
			if (!dir.isOrtho()) {
				x+= side.next(1).x();
				y+= side.next(1).y();
				for (int i = 0; i <= length; i++) {
					int dx = (x + side.x()*i);
					int dy = (y + side.y()*i);
					maxMen += u.getSpaceInBack(dx, dy, dir, true);
					enemies += u.enemiesInFront(dx, dy, dir, true);
					u.block(dx, dy, side);
				}
			}
			
			maxMen = Math.min(maxMen, enemies*2);
			line.menMax = maxMen;
			return line;
			
		}
		
		private LineTmp keep(Line old) {
			
			DIR dir = old.dir;
			int di = getKeepDistance(dir, old.tx1, old.ty1, DistanceToBlobMax);
			if (di < 0)
				return null;
			
			
			int x = old.tx1 + old.dir.x()*di;
			int y = old.ty1 + old.dir.y()*di;
			
			int length = old.tileLength;
			DIR side = old.dir.next(2);
			if (u.length(x, y, dir, side, 0) != length)
				return null;

			int maxMen = 0;
			int enemies = 0;
			line.maxBack = Integer.MAX_VALUE;
			for (int i = 0; i <= length; i++) {
				int dx = (x + side.x()*i);
				int dy = (y + side.y()*i);
				int b = u.getSpaceInBack(dx, dy, dir, true);
				line.maxBack = Math.min(line.maxBack, b);
				maxMen += b;
				enemies += u.enemiesInFront(dx, dy, dir, true);
				u.block(dx, dy, side);
			}
			line.tx1 = old.tx1;
			line.ty1 = old.ty1;
			line.tileLength = old.tileLength;
			line.dir = dir;
			
			if (!dir.isOrtho()) {
				x+= side.next(1).x();
				y+= side.next(1).y();
				for (int i = 0; i <= length; i++) {
					int dx = (x + side.x()*i);
					int dy = (y + side.y()*i);
					maxMen += u.getSpaceInBack(dx, dy, dir, true);
					enemies += u.enemiesInFront(dx, dy, dir, true);
					u.block(dx, dy, side);
				}
			}
			
			maxMen = Math.min(maxMen, enemies*2);
			line.menMax = maxMen;
			return line;
			
		}
		
		
		private int getKeepDistance(DIR d, int x, int y, int maxDistance) {
			
			for (int i = 0; i < maxDistance; i++) {
				if (u.isValidStart(x+i*d.x(), y+i*d.y(), d))
					return i;
				if (!u.canMove(x+(i+1)*d.x(), y+(i+1)*d.y(), d))
					return -1;
			}
			return -1;
		}

	}

	private class Util {

		public int length(int sx, int sy, DIR dir, DIR side, int initial) {
			
			final int initialRadius = (int) ((8-initial)/dir.tileDistance());
			final int extraRadius = (int) (initialRadius + 4/dir.tileDistance());
			
			int lastEnemy = 0;
			
			
			for (int w = 1; w < initialRadius; w++) {
				
				int dx = sx + side.x()*(w-1);
				int dy = sy + side.y()*(w-1);
				
				if (!canMove(dx, dy, side))
					return lastEnemy;
				
				dx += side.x();
				dy += side.y();
				
				if (!isValidStart(dx, dy, dir)) {
					return lastEnemy;
				}
				
				if (getSpaceInBack(dx, dy, dir, false) < 4)
					return lastEnemy;
				
				if (enemiesInFront(dx, dy, dir, false) > 0) {
					lastEnemy = w;
				}
				
			}
			
			for (int w = initialRadius; w < extraRadius; w++) {
				
				int dx = sx + side.x()*(w-1);
				int dy = sy + side.y()*(w-1);
				
				if (!canMove(dx, dy, side))
					return lastEnemy;
				
				dx += side.x();
				dy += side.y();
				
				if (!isValidStart(dx, dy, dir)) {
					return lastEnemy;
				}
				if (getSpaceInBack(dx, dy, dir, false) < 4)
					return lastEnemy;
				
				if (enemiesInFront(dx, dy, dir, false)>0) {
					lastEnemy = w;
				}
			}
			
			return lastEnemy;
		}
		
		public void block(int dx, int dy, DIR to) {
			int x = (int) dx;
			int y = (int) dy;
			blocked.set(x, y, true);
//			if (!to.isOrtho()) {
//				blocked.set(x, y, to.next(1), true);
//			}
		}
		

		
		public boolean isValidStart(int dx, int dy, DIR dir) {
			
			if (solid(dx, dy))
				return false;
			
			int tx = (int) dx;
			int ty = (int) dy;
			if (blocked.is(tx, ty))
				return false;
			
			return isDir(dir, tx, ty);
			
		}
		

		

		
		public int getSpaceInBack(int dx, int dy, DIR dir, boolean alsoBlock) {
			dir = dir.perpendicular();
			for (int i = 1; i < 24; i++) {
				int x = dx + dir.x()*i;
				int y = dy + dir.y()*i;
				if (!canMove(x, y, dir))
					return i;
				if (alsoBlock)
					block(x, y, dir);
			}
			return 24;
		}
		
		public int enemiesInFront(int sx, int sy, DIR dir, boolean alsoMark) {
			
			int blockUntil = Integer.MAX_VALUE;
			boolean inBlob = context.blob.is(sx, sy);
			
			int t = 0;
			for (int i = 1; i < 2000; i++) {
				
				int dx = sx + dir.x()*i;
				int dy = sy + dir.y()*i;
				
				if (!canMove(dx, dy, dir))
					break;
				
				
				if (inBlob) {
					if (!context.blob.is(dx, dy)) {
						break;
					}
				}else {
					if (i > DistanceToBlobMax)
						break;
					inBlob = context.blob.is(dx, dy);
				}
				
				
				if (ArmyAIUtil.map().hasEnemy.is(dx, dy, context.army)) {
					if (i < blockUntil)
						blockUntil = i;
					t++;
				}
				
			}
			
			if (alsoMark) {
				
				
				
				int k = t == 0 ? 16 : blockUntil;
				k = Math.min(k, 16);
				for (int i = 1; i < k; i++) {
					int dx = sx + dir.x()*i;
					int dy = sy + dir.y()*i;
					block(dx, dy, dir);
				}
				
			}
			return t;
			
		}
		
		public boolean canMove(int dx, int dy, DIR dir) {
			
			int nx = (int) (dx+dir.x());
			int ny = (int) (dy+dir.y());
			if (solid(nx, ny))
				return false;
			if (blocked.is(nx, ny))
				return false;
			if (!dir.isOrtho()) {
				int ox = (int) dx;
				int oy = (int) dx;
				if (solid(ox, ny) && solid(nx, oy))
					return false;
				if (blocked.is(ox, ny) && blocked.is(nx, oy))
					return false;
			}
			return true;
		}
		
		public boolean solid(double dx, double dy) {
			
			int tx = (int) dx;
			int ty = (int) dy;
			if (!SETT.IN_BOUNDS(tx, ty))
				return true;
			if (SETT.PATH().solidity.is(tx, ty))
				return true;
			return false;
		}
		
		public DIR getDir(int tx, int ty) {
			if (solid(tx, ty))
				return null;
			if (context.blob.is(tx, ty))
				return null;
			DIR res = null;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (context.blob.is(tx, ty, d))
					if (res != null)
						return null;
					else
						res = d;
			}
			
			if (res == null) {
				for (int i = 0; i < DIR.NORTHO.size(); i++) {
					DIR d = DIR.NORTHO.get(i);
					if (context.blob.is(tx, ty, d))
						if (res != null)
							return null;
						else
							res = d;
				}
			}
			
			return res;
		}
		
		public boolean isDir(DIR dir, int tx, int ty) {
			if (solid(tx, ty))
				return false;
			if (context.blob.is(tx, ty))
				return false;
			if (!dir.isOrtho()) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					if (d != dir && context.blob.is(tx, ty, d))
						return false;
				}
			}else {
				if (context.blob.is(tx, ty, dir.perpendicular()))
					return false;
			}
			return true;
		}
	}
	

	
//	private class Dir {
//		
//
//		
//		public int isDistance(DIR dir, int maxDistance, int tx, int ty) {
//			
//			
//			for (int i = 0; i < maxDistance; i++) {
//				if (isDir(dir, tx+dir.x()*i, ty+dir.y()*i))
//					return i;
//				if (!u.canMove(tx+dir.x()*(i+1), ty+dir.y()*(i+1), dir))
//					return -1;
//			}
//			return -1;
//		}
//		
//	}
	


	
	private static class LineMap implements MAP_OBJECT<Line>, SAVABLE{
		
		
		private final Bitmap2D marked = new Bitmap2D(SETT.TILE_BOUNDS, false);
		private final LineFactory fac;
		private final Line[][] grid = new Line[(int) Math.ceil(SETT.THEIGHT/16.0)][(int) Math.ceil(SETT.TWIDTH/16.0)];
		
		LineMap(Context context){
			fac = new LineFactory(context);
		}
		
		@Override
		public void save(FilePutter file) {
			fac.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			clear();
			fac.load(file);
			for (int i = 0; i < fac.freeI; i++) {
				Line l = fac.free.get(i);
				short tx1 = l.tx1;
				short ty1 = l.ty1;
				marked.set(tx1, ty1, true);
				tx1 /= 16;
				ty1 /= 16;
				l.next = grid[ty1][tx1];
				grid[ty1][tx1] = l;
			}
		}
		
		@Override
		public void clear() {
			fac.clear();
			marked.clear();
			for (int y = 0; y < grid.length; y++) {
				for (int x = 0; x < grid[y].length; x++) {
					grid[y][x] = null;
				}
			}
		}
		
		void add(LineTmp line) {
			short tx1 = line.tx1;
			short ty1 = line.ty1;
			marked.set(tx1, ty1, true);
			Line l = fac.next();
			l.init(line);
			
			tx1 /= 16;
			ty1 /= 16;
			l.next = grid[ty1][tx1];
			grid[ty1][tx1] = l;
		}
		

		
		@Override
		public Line get(int tile) {
			throw new RuntimeException();
		}
		
		@Override
		public boolean is(int tile) {
			return marked.is(tile);
		}
		
		@Override
		public Line get(int tx, int ty) {
			if (!marked.is(tx, ty))
				return null;
			Line l = grid[ty/16][tx/16];
			while(l != null) {
				if (l.tx1 == tx && l.ty1 == ty)
					return l;
				l = l.next;
			}
			return null;
		}


		


	}
	
	private static class LineFactory implements SAVABLE{
		
		private int freeI = 0;
		private final ArrayListGrower<Line> free = new ArrayListGrower<>();
		
		public LineFactory(Context context) {
			
		}
		
		public Line next() {
			if (freeI >= free.size()) {
				for (int i = 0; i < 128; i++)
					free.add(new Line());
			}
			Line l = free.get(freeI);
			l.next = null;
			freeI ++;
			return l;
		}
		
		@Override
		public void clear() {
			freeI = 0;
		}

		@Override
		public void save(FilePutter file) {
			file.i(freeI);
			for (int i = 0; i < freeI; i++) {
				Line l = free.get(i);
				file.s(l.tileLength);
				file.s(l.tx1);
				file.s(l.ty1);
				file.b((byte) l.dir.id());
				file.s(l.menMax);
			}
		}

		@Override
		public void load(FileGetter file) throws IOException {
			freeI = file.i();
			free.clear();
			for (int i = 0; i < freeI; i++) {
				Line l = new Line();
				l.tileLength = file.s();
				l.tx1 = file.s();
				l.ty1 = file.s();
				l.dir = DIR.ALL.get(file.b());
				l.menMax = file.s();
				free.add(l);
			}
		}
		
	}
}
