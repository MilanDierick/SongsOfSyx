package world;

import static world.World.*;

import java.io.IOException;

import game.faction.Faction;
import init.D;
import init.RES;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap1D;
import util.rendering.Minimap;
import world.map.regions.*;

public final class WorldMinimap {

	WorldMinimap() throws IOException{
		// TODO Auto-generated constructor stub
	}
	
	public final Minimap map = new Minimap(256);
	private Bitmap1D changes = new Bitmap1D(Regions.MAX, false);
	private int changedI = Regions.MAX;
	private final ColorImp cWork = new ColorImp();
	private static CharSequence ¤¤painting = "¤Painting world minimap";
	static {
		D.ts(WorldMinimap.class);
	}
	public void updateRegion(Region region) {
		changedI = Math.min(region.index(), changedI);
		changes.set(region.index(), true);
	}
	
	void clear() {
		changedI = Regions.MAX;
		changes.clear();
	}
	
	void update(float ds) {
		int max = 0;
		while(changedI < Regions.MAX && max++ < 100) {
			Region c = World.REGIONS().getByIndex(changedI);
			changedI++;
			if (!changes.get(c.index()))
				continue;
			changes.set(c.index(), false);
			
			int px1 = CLAMP.i(map.width()*c.bounds().x1()/World.TWIDTH()-4, 0, map.width());
			int py1 = CLAMP.i(map.height()*c.bounds().y1()/World.THEIGHT()-4, 0, map.height());
			int px2 = CLAMP.i(map.width()*c.bounds().x2()/World.TWIDTH()+4, 0, map.width());
			int py2 = CLAMP.i(map.height()*c.bounds().y2()/World.THEIGHT()+4, 0, map.height());
			
			for (int py = py1; py < py2; py++) {
				for (int px = px1; px < px2; px++) {
					cWork.set(getColorP(px, py));
					//cWork.shadeSelf(2);
					map.putPixel(px, py, cWork);
				}
				
			}
			
			return;
			
			
		}
	}
	

	private final static COLOR cNone = new ColorImp(100, 100, 100);
	private final COLOR cBorderDark = new ColorImp(35,35,35);
	private final COLOR cBorderLight = new ColorImp(127, 127, 127);
	private final COLOR cOcean = new ColorImp(30,35,60);
	private final COLOR cOceanBorder = cOcean.shade(0.5);
	private final COLOR cMountain = new ColorImp(27,20,25);
	
	
	private COLOR getColorP(int pixelX, int pixelY) {
		
		double dx = World.TWIDTH();
		dx /= map.width();
		double dy = World.THEIGHT();
		dy /= map.height();
		
		double wx = (double)World.TWIDTH()*pixelX/map.width();		
		double wy = (double)World.THEIGHT()*pixelY/map.height();
		
		
		
		{
			double rx = dx*6;
			double ry = dy*6;
			
			for (int y = (int) (wy-ry); y <= wy+ry; y++) {
				for (int x = (int) (wx-rx); x <= wx+rx; x++) {
					if (!World.IN_BOUNDS(x, y))
						continue;
					Region r2 = World.REGIONS().getter.get(x, y);
					if (r2 != null && !r2.isWater() && REGIOND.isCapitol(r2) && r2.cx() == x && r2.cy() == y) {
						int px = (int) (x/dx);
						int py = (int) (y/dy);
						int ddx = pixelX-px;
						int ddy = pixelY-py;
						double rad = Math.abs(ddx) + Math.abs(ddy);
						//return COLOR.BLACK;
						if (rad <= 3)
							return cWork.set(REGIOND.faction(r2).banner().colorBG()).shadeSelf(0.5);
						if (rad <= 4)
							return COLOR.BLACK;
						if (rad <= 5)
							return COLOR.WHITE100;
					}	
				}
			}
		}
		
		{
			
			double rx = dx*3;
			double ry = dy*3;
			
			for (int y = (int) (wy-ry); y <= wy+ry; y++) {
				for (int x = (int) (wx-rx); x <= wx+rx; x++) {
					if (!World.IN_BOUNDS(x, y))
						continue;
					Region r2 = World.REGIONS().getter.get(x, y);
					if (r2 != null && !r2.isWater()  && !REGIOND.isCapitol(r2) && r2.cx() == x && r2.cy() == y) {
						int px = (int) (x/dx);
						int py = (int) (y/dy);
						int ddx = pixelX-px;
						int ddy = pixelY-py;
						double rad = Math.abs(ddx) + Math.abs(ddy);
						if (rad <= 1)
							return COLOR.BLACK;
						if (rad <= 2)
							return COLOR.WHITE100;
					}	
				}
			}
			
		}
		
		Region r = REGIONS().setter.get((int)wx,(int)wy);
		
		if (r != null) {
			
			COLOR b = r.isWater() ? cOceanBorder : cBorderLight;
			
			if (World.IN_BOUNDS((int)(wx+dx), (int)(wy)) && !REGIONS().setter.is((int)(wx+dx), (int)(wy), r))
				return cBorderDark;
			if (World.IN_BOUNDS((int)(wx), (int)(wy+dy)) && !REGIONS().setter.is((int)(wx), (int)(wy+dy), r))
				return cBorderDark;
			if (World.IN_BOUNDS((int)(wx-dx), (int)(wy)) && isDiffRealm((int)(wx-dx), (int)(wy), r))
				return b;
			if (World.IN_BOUNDS((int)(wx), (int)(wy-dy)) && isDiffRealm((int)(wx), (int)(wy-dy), r))
				return b;
			
			if (r.isWater())
				return cOcean;
			
			Faction k = REGIOND.faction(r);
			if (k != null)
				return k.banner().colorBG();
			return cNone;
		}
		
		int tx = (int) wx;
		int ty = (int) wy;
		if (WATER().has.is(tx, ty) && WATER().coversTile.is(tx, ty)) {
			return cOcean;
		} else if (MOUNTAIN().is(tx, ty)) {
			return cMountain;
		}
		return cNone;
	}
	
	private boolean isDiffRealm(int x, int y, Region r) {
		Region r2 = REGIONS().getter.get(x, y);
		if (r == r2)
			return false;
		if (r2 == null)
			return true;
		if (r.realm() == null || r2.realm() == null || r.realm() != r2.realm())
			return true;
		return false;
	}
	
	public void repaint() {
		RES.loader().print(¤¤painting);
		clear();
		int pWidth = map.width();
		int pHeight = map.height();

		byte[] pixels = new byte[pWidth * pHeight * 4];

		int i = 0;
		
		for (int py = 0; py < pHeight; py ++) {
			for (int px = 0; px < pWidth; px++) {
				setPixel(pixels, i, getColorP(px, py));
				i += 4;
			}
		}

		map.putPixels(pixels);
	}
	
	private static void setPixel(byte[] pixels, int i, COLOR c) {
		pixels[i + 0] = (byte) ((c.red()&0x0FF));
		pixels[i + 1] = (byte) ((c.green()&0x0FF));
		pixels[i + 2] = (byte) ((c.blue()&0x0FF));
		pixels[i + 3] = (byte) 255;
	}
	
}
