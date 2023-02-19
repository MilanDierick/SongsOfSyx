package world.map.terrain;

import static world.World.*;

import java.io.IOException;

import game.time.TIME;
import init.C;
import init.RES;
import init.biomes.TERRAINS;
import init.sprite.ICON;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_INT;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.*;
import util.rendering.ShadowBatch;
import view.tool.*;
import view.world.IDebugPanelWorld;
import world.World;
import world.World.WorldResource;

public class WorldMountain extends WorldResource{

	private byte[] data;
	private static byte NOTHING = -1;
	private final TILE_SHEET sheet = World.sprites().mountain;
	private static final int MAX_HEIGHT = 15;
	private final COLOR[] colors = COLOR.interpolate(new ColorImp(80,80,80), new ColorImp(210,210,210), MAX_HEIGHT);
	private final Bitmap1D top = new Bitmap1D(TAREA(), false);
	public final PLACABLE placer;
	public final SPRITE icon;
	
	public WorldMountain() {
		data = new byte[TAREA()];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) NOTHING;
		
		PLACABLE clear = new Placable("clear mountain") {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return isp(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				clear(tx, ty);
				for (int i = 0; i < DIR.ALL.size(); i++) {
					fix(tx+DIR.ALL.get(i).x(), ty+DIR.ALL.get(i).y());
				}
			}

		};
		
		IDebugPanelWorld.add(clear);
		
		placer = new Placable("mountain") {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return IN_BOUNDS(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!isp(tx, ty)) {
					placeRaw(tx, ty);
					fix(tx, ty);
					for (int i = 0; i < DIR.ALL.size(); i++) {
						fix(tx+DIR.ALL.get(i).x(), ty+DIR.ALL.get(i).y());
					}
				}
			}
			
			@Override
			public PLACABLE getUndo() {
				return clear;
			}
			
			
		};
		
		IDebugPanelWorld.add(placer);
	
		PLACABLE t;
		
		t = new Placable("sink mountain") {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return isp(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				sink(tx, ty);
			}
			
		};
		
		IDebugPanelWorld.add(t);
		
		icon = new SPRITE.Imp(ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int d = (Y2-Y1)/2;
				sheet.render(r, EDGES + DIR.SE.mask(), X1, X1+d, Y1, Y1+d);
				sheet.render(r, EDGES + DIR.SW.mask(), X1+d, X1+d*2, Y1, Y1+d);
				sheet.render(r, EDGES + DIR.NE.mask(), X1, X1+d, Y1+d, Y1+d*2);
				sheet.render(r, EDGES + DIR.NW.mask(), X1+d, X1+d*2, Y1+d, Y1+d*2);
			}
		};
	}
	
	private abstract class Placable extends PlacableMulti{
		
		
		public Placable(CharSequence name) {
			super(name);
		}

		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
				PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
			super.renderPlaceHolder(r, mask, x + C.TILE_SIZEH, y + C.TILE_SIZEH, tx, ty, area, type, isPlacable, areaIsPlacable);
		}
		
		
	}
	
	private void set(int tx, int ty, int value) {
		data[tx + ty*TWIDTH()] &= 0b11110000;
		data[tx + ty*TWIDTH()] |= value & 0b00001111;
	}
	
	private int get(int tile) {
		return data[tile] & 0b00001111;
	}
	
	private int height(int tx, int ty) {
		return height(tx+ty*TWIDTH());
	}
	
	private int height(int tile) {
		return (data[tile] >> 4) & 0b00001111;
	}
	
	private void heightSet(int tile, int h) {
		if (h > 14)
			h = 14;
		else if (h < 0)
			h = 0;
		h = h << 4;
		data[tile] &= 0b00001111;
		data[tile] |= h;
		
	}
	
	private boolean has(int tile) {
		return data[tile] != NOTHING;
	}
	
	void clear(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			data[tx + ty*TWIDTH()] = (byte) NOTHING;
		}
	}
	
	void placeRaw(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			data[tx + ty*TWIDTH()] = (byte) 0;
		}
	}
	
	private boolean isp(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			return data[tx + ty*TWIDTH()] != NOTHING;
		}
		return false;
	}
	

	
	boolean isp(int tx, int ty, DIR d) {
		return isp(tx+d.x(), ty+d.y());
	}
	
	void fix(int tx, int ty) {
		
		if (!isp(tx, ty))
			return;
		
		setHeight(tx, ty);
		RES.flooder().init(this);
		for (int i = 0; i < DIR.ALL.size(); i++) {
			if (isp(tx, ty, DIR.ALL.get(i))) {
				RES.flooder().pushSloppy(tx, ty, DIR.ALL.get(i), 0);
			}
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollAndReopen();
			if (!setHeight(t.x(), t.y()))
				continue;
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if (isp(t.x(), t.y(), DIR.ALL.get(i)))
					RES.flooder().pushSloppy(t.x(), t.y(), DIR.ALL.get(i), 0);
			}
		}
		
		RES.flooder().done();
		
		int h = getHeight(tx, ty);
		int da = get(tx+ty*TWIDTH());
		int i = tx+ty*TWIDTH();
		top.set(i, true);
		for (DIR d : DIR.ORTHO) {
			if (IN_BOUNDS(tx, ty, d) && (d.mask() & da) != 0 && getHeight(tx+d.x(), ty+d.y()) > h && ((get(i+d.x()+d.y()*TWIDTH()) & 0x0F) != 0))
				top.set(tx+ty*TWIDTH(), false);
		}
		
	}
	
	void sink(int tx, int ty) {
		
		if (!isp(tx, ty))
			return;
		
		int h = height(tx, ty);
		int newHeight = h-1;
		if (newHeight < 0)
			newHeight = 0;
		heightSet(tx+ty*TWIDTH(), newHeight);
		
		RES.flooder().init(this);
		for (int i = 0; i < DIR.ALL.size(); i++) {
			if (isp(tx, ty, DIR.ALL.get(i))) {
				RES.flooder().pushSloppy(tx, ty, DIR.ALL.get(i), 0);
			}
		}
		
		while(RES.flooder().hasMore()) {
			PathTile t = RES.flooder().pollAndReopen();
			if ((tx == t.x() && ty == t.y()) || !setHeight(t.x(), t.y()))
				continue;
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if (isp(t.x(), t.y(), DIR.ALL.get(i)))
					RES.flooder().pushSloppy(t.x(), t.y(), DIR.ALL.get(i), 0);
			}
		}
		
		RES.flooder().done();
	}
	
	private boolean setHeight(int tx, int ty) {
		
		int height = getHeight(tx, ty);
		
		if (!neigboursTerrain(tx, ty)) {
			int lowest = 15;
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int h = getHeight(tx, ty, d);
				if (h < lowest) {
					lowest = h;
				}
			}
			
			if (height != lowest+1 && height != 15) {
				heightSet(tx+ty*TWIDTH(), lowest);
				return true;
			}
		}else {
			heightSet(tx+ty*TWIDTH(), 0);
		}
	
		set(tx, ty, getJoin(tx, ty));
		
		
		return false;
	}
	
	private int getJoin(int tx, int ty) {
		int height = getHeight(tx, ty);
		int res = 0;
		
		for (DIR d : DIR.NORTHO) {
			int x = tx + (d.x()+1)/2;
			int y = ty + (d.y()+1)/2;
			if (!WATER().has.is(x, y)) {
				if (getHeight(tx, ty, d) >= height && getHeight(tx, ty, d.next(-1)) >= height && getHeight(tx, ty, d.next(1)) >= height)
					res |= d.mask();
				
			}
		}
		
		return res;
	}
	
	public int getHeight(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return 15;
		if (!isp(tx, ty))
			return 0;
		if (neigboursTerrain(tx, ty))
			return 1;
		return 1+height(tx, ty);
	}
	
	private boolean neigboursTerrain(int tx, int ty){
		return WATER().has.is(tx, ty) || WATER().has.is(tx+1, ty+1) || WATER().has.is(tx+1, ty) || WATER().has.is(tx, ty+1);
	}
	
	private int getHeight(int tx, int ty,DIR d) {
		return getHeight(tx+d.x(), ty+d.y());
	}
	
	double getHeightNormalized(int tx, int ty) {
		return getHeight(tx, ty)/15.0;
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		saveFile.bs(data);
		top.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(data);
		top.load(saveFile);
	}
	
	private final static int SET = 16;
	private final static int EDGES = 0;
	private final static int FULLS = EDGES+8*SET;
	private final static int TOPS = FULLS+8*SET;
	private final static int SINGLES = TOPS+4*SET;
	private final static int SINGLES_FULLS = SINGLES+2*SET;
	
	private int snowHeightI;
	private final int[] snowHeights = new int[] {3,4,3,2};
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData data){
		
		RenderIterator it = data.onScreenTiles(1, 0, 1, 0);
		
		snowHeightI = TIME.seasons().bitCurrent();
		
		while(it.has()) {
			
			if (has(it.tile())) {
				int t = get(it.tile());
				int h = height(it.tile());
				colors[h].bind();
				int ran = it.ran();
				
				
				//colors[h].bind();�
				
//				int hi = heighter.get(it.tile());
//				if (hi > 0) {
//					COLOR.RED100.render(r, it.x(),it.y());
//				}
				
				int x = it.x()+C.TILE_SIZEH;
				int y = it.y()+C.TILE_SIZEH;
				if (t == 0) {
					if (h == 0)
						sheet.render(r, SINGLES+(ran&0x1F), x, y);
					else {
						sheet.render(r, SINGLES_FULLS+(ran&0x0F), x, y);
						if (h > 2) {
							render(h, 15, ran, SINGLES_FULLS+(ran&0x0F), x, y);
						}
					}
				}else {
					if (h == 0) {
						sheet.render(r, t+EDGES+(ran&0x07)*SET, x, y);
					}else {
						int tile = t+FULLS+(ran&7)*SET;
						sheet.render(r, tile, x, y);
						render(h, t, ran, tile, x, y);
							
					}
					if (top.get(it.tile())) {
						int tile = t+TOPS+(ran&3)*SET;
						sheet.render(r, t+TOPS+(ran&3)*SET, x, y);
						render(h, t, ran, tile, x, y);
					}
					
					
				}
				if ( h >= 2)
					it.hiddenSet();
				
				
			}

			it.next();
		}
		COLOR.unbind();
				
	}
	
	private void render(int h, int rot, int ran, int tile, int x, int y) {
		rot &= 0x00F;
		if (h >= snowHeights[snowHeightI]) {
			

			OPACITY.O99.bind();
			COLOR.WHITE150.bind();
			if (h > snowHeights[snowHeightI]) {
				TextureCoords text = sheet.getTexture(0x0F);
				sheet.renderTextured(text, tile, x, y);
			}else if (rot != 0 && rot != 0x0F){
				TextureCoords text = sheet.getTexture(TOPS+rot + (ran&3)*SET);
				sheet.renderTextured(text, tile, x, y);
			}
			OPACITY.unbind();
			COLOR.unbind();
			//SPRITES.world().map.forest.render(r, 16*4+(ran&0x3), x, y);
		}
	}
	
	
	private final DIR[] checks = new DIR[] {DIR.C, DIR.W, DIR.NW, DIR.N};
	
	public boolean coversTile(int tx, int ty) {
		if (WATER().has.is(tx, ty))
			return false;
		if (getHeight(tx, ty) == 0 && (get(tx+ty*TWIDTH()) & DIR.NW.mask()) == 0)
			return false;
		for (DIR d : checks) {
			int x = tx+d.x();
			int y = ty+d.y();
			int i = x+y*TWIDTH();
			if (!IN_BOUNDS(x, y))
				continue;
			if (!(has(i)))
				return false;
		}
		return true;
	}
	
	public boolean is(int tx, int ty) {
		return is(tx, ty, DIR.NW) || is(tx-1, ty, DIR.NE) || is(tx-1, ty-1, DIR.SE) || is(tx, ty-1, DIR.SW);
	}
	
	private final boolean[] centres = new boolean[] {
			true,false,false,false,false,true,false,true,false,false,true,true,false,true,true,true
	};
	
	private boolean is(int tx, int ty, DIR d) {
		if (!isp(tx, ty))
			return false;
		if (getHeight(tx, ty) > 1)
			return true;
		int m = get(tx + ty*TWIDTH());
		return (m & d.mask()) > 0 || centres[m];
	}
	
	public MAP_BOOLEAN haser = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && WorldMountain.this.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			int tx = tile % TWIDTH();
			int ty = tile /TWIDTH();
			return is(tx, ty);
		}
	};
	
	public MAP_INT heighter = new MAP_INT() {
		
		@Override
		public int get(int tx, int ty) {
			int h = 16;
			for (DIR d : checks) {
				int x = tx+d.x();
				int y = ty+d.y();
				if (!IN_BOUNDS(x, y))
					continue;
				int i = x+y*TWIDTH();
				if ((has(i))) {
					h = Math.min(h, height(i)+1);
				}else
					return 0;
			}
			return h;
		}
		
		@Override
		public int get(int tile) {
			return get(tile%TWIDTH(), tile/TWIDTH());
		}
	};
	
	public final AreaTileMountain area = new AreaTileMountain();
	
	public final static class AreaTileMountain{
		
		private AreaTileMountain() {
			
		}
		
		public boolean is(int tx, int ty, DIR d) {
			
			if (d == DIR.C)
				return ispp(tx, ty, DIR.NW);
			else if (d == DIR.N) {
				return ispp(tx, ty-1, DIR.W);
			}else if (d == DIR.NE) {
				return ispp(tx, ty-1, DIR.C);
			}else if (d == DIR.E) {
				return ispp(tx, ty, DIR.N);
			}else if (d == DIR.SE) {
				return ispp(tx, ty, DIR.C);
			}else if (d == DIR.S) {
				return ispp(tx, ty, DIR.W);
			}else if (d == DIR.SW) {
				return ispp(tx-1, ty, DIR.C);
			}else if (d == DIR.W) {
				return ispp(tx-1, ty, DIR.N);
			}else { //NW
				return ispp(tx-1, ty-1, DIR.C);
			}
			
			
		}
		
		private boolean ispp(int tx, int ty, DIR d) {
			
			if (!World.MOUNTAIN().isp(tx, ty))
				return false;
			if (World.MOUNTAIN().getHeight(tx, ty) > 1)
				return true;
			int m = World.MOUNTAIN().get(tx + ty*TWIDTH());
			if (d == DIR.C)
				return World.MOUNTAIN().centres[m];
			if (d.isOrtho()) {
				return (m & d.next(1).mask()) > 0 && (m & d.next(-1).mask()) > 0;
			}
			return (m & d.mask()) > 0;
			
		}
		
		public boolean borders(int tx, int ty, DIR d) {
			
			if (d == DIR.C)
				throw new RuntimeException();
			
			if (d == DIR.N) {
				return World.MOUNTAIN().is(tx, ty-1, DIR.NW);
			}else if (d == DIR.NE) {
				return World.MOUNTAIN().is(tx, ty-1, DIR.NE);
			}else if (d == DIR.E) {
				return World.MOUNTAIN().is(tx, ty, DIR.NE);
			}else if (d == DIR.SE) {
				return World.MOUNTAIN().is(tx, ty, DIR.SE);
			}else if (d == DIR.S) {
				return World.MOUNTAIN().is(tx, ty, DIR.SW);
			}else if (d == DIR.SW) {
				return World.MOUNTAIN().is(tx-1, ty, DIR.SW);
			}else if (d == DIR.W) {
				return World.MOUNTAIN().is(tx-1, ty, DIR.NW);
			}else { //NW
				return World.MOUNTAIN().is(tx-1, ty-1, DIR.NW);
			}
			
		}
		
	}
	
	double add(WorldTerrainInfo info, int tx, int ty) {
		if (is(tx, ty)) {
			double m = 0;
			for (DIR d : DIR.ALLC) {
				if (is(tx, ty, d))
					m += 1;
			}
			m /= DIR.ALLC.size();
			info.add(TERRAINS.MOUNTAIN(), m);
			return m;
		}
		return 0;
	}

	
}
