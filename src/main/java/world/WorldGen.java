package world;

import java.io.IOException;
import java.nio.file.Path;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.rnd.RND;

public final class WorldGen implements SAVABLE {
	
	public boolean hasGeneratedTerrain;
	public boolean hasPlacedCapitol;
	public double lat = 0.5;
	public String map = null;
	public int seed = RND.seed();
	public int playerX,playerY;
	
	
	public WorldGen(World world) {
		clear();
	}

	@Override
	public void save(FilePutter file) {
		file.bool(hasGeneratedTerrain);
		file.bool(hasPlacedCapitol);
		file.d(lat);
		if (map != null) {
			file.bool(true);
			file.chars(map);
		}else {
			file.bool(false);
		}
		file.i(seed);
		file.i(playerX);
		file.i(playerY);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		hasGeneratedTerrain = file.bool();
		hasPlacedCapitol = file.bool();
		lat = file.d();
		if (file.bool()) {
			map = file.chars();
		}else
			map = null;
		seed = file.i();
		playerX = file.i();
		playerY = file.i();
		
	}

	@Override
	public void clear() {
		hasGeneratedTerrain = false;
		hasPlacedCapitol = false;
		lat = 0.5;
		map = null;
		seed = RND.rInt(Integer.MAX_VALUE);
		playerX = -1;
		playerY = -1;
	}

	public static final class WorldGenMapType {

		public static final int DIM = 100;
		private final byte[][] map = new byte[100][100];
		private static double ii = 1.0/256;
		
		private static COLOR[] cols = new COLOR[] {
			new ColorImp(25, 25, 50),
			new ColorImp(50, 60, 20),
			new ColorImp(30, 25, 25),
		};
		
		public final String name;
		
		public WorldGenMapType(String name) {
			this.name = name;
			Path path = PATHS.SPRITE().getFolder("world").getFolder("generatorMaps").get(name);
			SnakeImage im;
			try {
				im = new SnakeImage(path, DIM, DIM);
				for (int y = 0; y < im.height; y++) {
					for (int x = 0; x < im.width; x++) {
						map[y][x] = (byte) ((im.rgb.get(x, y)>>8) & 0x0FF);
					}
				}
				im.dispose();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private double g(int x, int y) {
			return (map[y][x]&0x0FF)*ii;
		}
		
		private double dd(double d) {
			if (d < 0.5)
				return -(0.5-d)*2;
			return (d-0.5)*2;
		}
		
		public double h(int x, int y) {
			return dd(g(x, y));
			
		}
		
		public double h(int x, int y, int w, int h) {
			double xx = x;
			xx/= w;
			x = (int) (xx*DIM);
			xx -= (int) xx;
			double yy = y;
			yy/= h;
			y = (int) (yy*DIM);
			yy -= (int)yy;
			
			//C
			double area = 0;
			double res = 0;
			{
				double a = (1-xx)*(1-yy);
				res += a*g(x, y);
				area+= a;
			}
			if (x + 1 < map.length) {
				double a = xx*(1-yy);
				res+= a*g(x+1, y);
				area += a;
			}
			
			if (y + 1 < map.length) {
				double a = (1-xx)*(yy);
				res+= a*g(x, y+1);
				area += a;
			}
			
			if (x + 1 < map.length && y + 1 < map.length) {
				double a = (xx)*(yy);
				res+= a*g(x+1, y+1);
				area += a;
			}
			
			return dd(res/area);
		}
		
		public void save(FilePutter f) {
			f.bs(map);
		}

		public void render(SPRITE_RENDERER r, int x1, int y1, int i) {
			for (int y = 0; y < DIM; y++) {
				for (int x = 0; x < DIM; x++) {
					int e = (int) ((map[y][x]&0x0FF)*ii*cols.length);
					int xx = x1 + x*i;
					int yy = y1 + y*i;
					cols[e].render(r, xx, xx+i, yy, yy+i);
				}
			}
			
		}
		
		public static WorldGenMapType[] getAll() {
			PATH p = PATHS.SPRITE().getFolder("world").getFolder("generatorMaps");
			String[] files = p.getFiles();
			WorldGenMapType[] res = new WorldGenMapType[files.length];
			for (int i = 0; i < files.length; i++)
				res[i] = new WorldGenMapType(files[i]);
			return res;
		}
		
		
		
	}
}
