package world.regions.map;

import static world.WORLD.*;

import java.io.IOException;
import java.util.Arrays;

import init.RES;
import init.biomes.*;
import init.resources.Minable;
import init.resources.RESOURCES;
import snake2d.PathTile;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import world.WORLD;
import world.regions.Region;
import world.regions.centre.WorldCentrePlacablity;

public final class RegionInfo {
	
	private static int[] countTerrain = new int[TERRAINS.ALL().size()];
	private static int[] countMinable = new int[RESOURCES.minables().all().size()];
	
	
	private static double bi = 1.0/0x0FF;
	
	public static final int nameSize = 24;
	private final Str name = new Str(nameSize);
	private int area;
	private final Rec bounds = new Rec();
	private short cx,cy;
	private byte fertility;
	private final byte[] climateTerrMin = new byte[1+TERRAINS.ALL().size()+RESOURCES.minables().all().size()];
	
	public RegionInfo() {
		clear();
	}
	
	void save (FilePutter f) {
		name.save(f);
		f.s(cx).s(cy);
		bounds.save(f);
		f.i(area);
		f.b(fertility);
		f.bs(climateTerrMin);
	}
	
	void load (FileGetter f) throws IOException {
		name.load(f);
		cx = (short) f.s();
		cy = (short) f.s();
		bounds.load(f);
		area = f.i();
		fertility = f.b();
		f.bs(climateTerrMin);
	}
	
	void clear(){
		name.clear();
		cx = -1;
		cy = -1;
		bounds.clear();
		area = 0;
		fertility = 0;
		Arrays.fill(climateTerrMin, (byte)0);
	}
	
	public Str name() {
		return name;
	}

	public int cx() {
		return cx;
	}
	
	public int cy() {
		return cy;
	}
	
	void centreSet(int tx, int ty) {
		
		cx = (short) tx;
		cy = (short) ty;
	}
	
	public int area() {
		return area;
	}
	
	public RECTANGLE bounds() {
		return bounds;
	}
	
	public double fertility() {
		return (fertility & 0x0FF)*bi;
	}
	
	public double climate(CLIMATE c) {
		double ci = climateI();
		
		if ((int) ci == c.index()) {
			return 1 - (ci-(int)ci);
		}else if ((int) ci == c.index()-1)
			return (ci-(int)ci);
		return 0;
	}
	
	public double climateI() {
		return (CLIMATES.ALL().size()-1)*(climateTerrMin[0] & 0x0FF)*bi;
	}
	
	public double terrain(TERRAIN c) {
		return (climateTerrMin[1+ c.index()] & 0x0FF)*bi;
	}
	
	public int minable(Minable m) {
		return (climateTerrMin[1+ TERRAINS.ALL().size() + m.index()] & 0x0FF);
	}
	
	public double minableBonus(Minable m) {
		return minableBonus(m, minable(m));
	}
	
	public static double minableBonus(Minable m, int am) {
		
		double min = 0.15+((double)WORLD.MINERALS().total(m)) / (RESOURCES.minables().all().size()+  WORLD.MINERALS().total(null));
		double worth = 1.0 - (double)WORLD.MINERALS().total(m)/WORLD.REGIONS().active().size();
		
		return min + (1.0-min)*am*worth;
		
	}
	
	boolean init(int sx, int sy, RECTANGLE body) {
		
		double climate = 0;
		Arrays.fill(countTerrain, 0);
		Arrays.fill(countMinable, 0);
		
		double fertility = 0;
		
		if (WORLD.REGIONS().map.get(sx, sy).info != this)
			throw new RuntimeException();
		
		Region a = REGIONS().map.get(sx, sy);
		
		bounds.moveX1Y1(sx, sy).setDim(1);
		area = 0;
		
		for (COORDINATE c : body) {
			if (!REGIONS().map.is(c, a)) {
				continue;
			}
			climate += WORLD.CLIMATE().getter.get(c).index();
			countTerrain[TERRAINS.world.get(c).index()]++;
			if (WORLD.MINERALS().get(c) != null)
				countMinable[WORLD.MINERALS().get(c).index()]++;
			fertility += WORLD.GROUND().getFertility(c.x(), c.y());
			area++;
			bounds.unify(c.x(), c.y());
		}
		
		Rec tmp = new Rec(bounds);
		tmp.incr(-1, -1).incrW(2).incrH(2);
		

		RES.flooder().init(this);
		for (COORDINATE c : tmp) {
			if (!REGIONS().map.is(c, a)) {
				RES.flooder().pushSloppy(c.x(), c.y(), 0);
			}
		}
		
		PathTile centre = null;
		PathTile backup = null;
		while (RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollSmallest();
			
			if (REGIONS().map.is(t, a)){
				if (WorldCentrePlacablity.regionC(t.x(), t.y()) == null) {
					centre = t;
				}else if (WorldCentrePlacablity.terrainC(t.x(), t.y()) == null) {
					backup = t;
				}else if (backup == null) {
					backup = t;
				}
			}
			
			
			
			for (DIR d : DIR.ALL) {
				if (REGIONS().map.is(t, d, a))
					RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		RES.flooder().done();
		
		boolean ret = true;
		if (centre == null) {
			centre = backup;
			ret = false;
		}
		
		if (WORLD.REGIONS().map.get(cx, cy) != a || WorldCentrePlacablity.regionC(cx, cy) != null) {
			cx = (short) centre.x();
			cy = (short) centre.y();
		}
		
		climate /= area*(CLIMATES.ALL().size()-1);
		climate = CLAMP.d(climate, 0, 1);
		climateTerrMin[0] = (byte) (0xFF*climate);
		
		for (int i = 0; i < countTerrain.length; i++) {
			climateTerrMin[1 + i] = (byte) (0xFF*((double)countTerrain[i]/area));
		}
		for (int i = 0; i < countMinable.length; i++) {
			climateTerrMin[1  + countTerrain.length+ i] = (byte) CLAMP.i(countMinable[i], 0, 0x0FF);
		}
		this.fertility = (byte) (0x0FF*fertility/area);
		Arrays.fill(countTerrain, 0);
		Arrays.fill(countMinable, 0);
		
		return ret;
	}
	
}