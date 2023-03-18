package world.map.terrain;

import static world.World.*;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.C;
import init.biomes.TERRAINS;
import init.paths.PATHS;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import view.tool.*;
import view.world.IDebugPanelWorld;
import world.World.WorldResource;
import world.map.terrain.WorldWater.WATER;

public class WorldWater extends WorldResource implements MAP_OBJECT<WATER>{

	private final Bitsmap1D tiles = new Bitsmap1D(-1, 4, TAREA());
	private final byte[] data;
	private final ArrayListResize<WATER> all = new ArrayListResize<>(255, 255);
	private final COLOR[] seasonColors = new COLOR[64];
	private final WorldWaterSprites sprites = new WorldWaterSprites();
	
	public final WATER NOTHING = new WATER("clear") {
		
		
		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		
		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
		}
		
		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}; 
		
		@Override
		boolean isFertile() {
			return false;
		}

	};
	
	public final OpenSet LAKE;
	public final OpenSet OCEAN;
	public final WATER RIVER = new River();
	public final WATER RIVER_SMALL = new RiverSmall();
	
	public final SPRITE iconSweet;
	public final SPRITE iconSalt;
	
	public WorldWater() throws IOException{

		Json js = new Json(PATHS.CONFIG().get("GenerationWorld")).json("WATER");

		LAKE = new OpenSet("lake", new ColorImp(js, "COLOR_LAKE_SHORE"), new ColorImp(js, "COLOR_LAKE"), true);
		OCEAN = new OpenSet("ocean", new ColorImp(js, "COLOR_OCEAN_SHORE"), new ColorImp(js, "COLOR_OCEAN"), false);
		
		data = new byte[TAREA()];
		all.trim();
		
		for (WATER t : all) {
			IDebugPanelWorld.add(t);
		}
		ColorImp winter = new ColorImp(127, 100, 127);
		for (double i = 0; i < seasonColors.length; i++) {
			ColorImp p = new ColorImp();
			double d = i/(seasonColors.length-1);
			if (d < 0.5)
				d = d*2;
			else
				d = 1.0 - (d-0.5)*2;
			
			p.interpolate(COLOR.WHITE100, winter, d);
			seasonColors[(int) i] = p;
			
		}
		
		iconSweet = new SPRITE.Imp(ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				((Normal)LAKE.normal).renderIcon(r, X1, Y1, X2-X1);
				
			}
		};
		
		iconSalt = new SPRITE.Imp(ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				((Normal)OCEAN.normal).renderIcon(r, X1, Y1, X2-X1);
				
			}
		};
		
	}
	
	public LIST<WATER> all(){
		return all;
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(data);
		tiles.load(saveFile);
		
	}

	@Override
	protected void save(FilePutter saveFile){
		saveFile.bs(data);
		tiles.save(saveFile);
	}
	
	@Override
	protected void clear() {
		tiles.setAll(0);
		Arrays.fill(data, (byte)0);
	}
	
	@Override
	protected void update(float ds) {
		sprites.update(ds);
	}
	
	public void renderShorelines(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();

		while(it.has()) {
			ColorImp.TMP.bind();
			if (get(it.tx(), it.ty()).renderShore(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	public void renderMid(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();

		while(it.has()) {
			ColorImp.TMP.bind();
			if (get(it.tx(), it.ty()).renderMid(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	public void render(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();
		
		int i = (int) (TIME.years().bitPartOf()*seasonColors.length);
		i %= seasonColors.length;
		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();
		LAKE.update(i, ColorImp.TMP);
		OCEAN.update(i, ColorImp.TMP);
		
		while(it.has()) {
			ColorImp.TMP.bind();
			if (get(it.tx(), it.ty()).render(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	
	private int dataGet(int tile) {
		return data[tile] & 0xFF;
	}
	
	private void dataSet(int tx, int ty, int d) {
		dataSet(tx+ty*TWIDTH(), d);
	}
	
	private void dataSet(int tile, int d) {
		data[tile] = (byte) d;
	}
	
	@Override
	public WATER get(int tx, int ty){
		if (!IN_BOUNDS(tx, ty))
			return NOTHING;
		return all.get(tiles.get(tx+ty*TWIDTH())); 
	}
	
	@Override
	public WATER get(int tile) {
		return all.get(tiles.get(tile)); 
	}
	
	public abstract class WATER extends PlacableMulti implements MAP_BOOLEAN{
		
		protected final int code;
		
		protected WATER(String name) {
			super(name);
			this.code = all.add(this);
		}

		abstract boolean coversCompleatly(int tile);

		void placeRaw(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				tiles.set(tx + ty*TWIDTH(), code);
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			if(IN_BOUNDS(tx, ty)) {
				int old = tiles.get(tx+ty*TWIDTH());
				pplace(tx, ty);
				if (old != tiles.get(tx+ty*TWIDTH())) {
					for (int i = 0; i < DIR.ALL.size(); i++) {
						DIR d = DIR.ALL.get(i);
						get(tx+d.x(), ty+d.y()).pplace(tx+d.x(), ty+d.y());
					}
				}
			}
		}

		
		
		abstract void pplace (int tx, int ty);
		
		final void place (int tx, int ty, DIR d) {
			pplace(tx+d.x(), ty+d.y());
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)) == this; 
		}
		
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*TWIDTH()); 
		}
		
		abstract boolean render(SPRITE_RENDERER r, int data, RenderIterator it);
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		boolean renderMid(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		abstract boolean isFertile();

		@Override
		public PLACABLE getUndo() {
			return NOTHING;
		}
		
		@Override
		public final SPRITE getIcon() {
			return SPRITES.icons().m.cancel;
		}
		
	}
	
	public final class OpenSet {

		private final COLOR cShore;
		private final COLOR cWater;
		private final ColorImp col = new ColorImp();
	
		public final WATER normal;
		public final WATER deep;
		public final WATER delta;
		
		private OpenSet(String name, COLOR cShore, COLOR cWater, boolean isFertile) {
			this.cShore = cShore;
			this.cWater = cWater;
			
			delta = new Delta(name + " delta", this);
			normal = new Normal(name, this, isFertile);
			deep = new Deep(name, this, isFertile);
		}
		
		void update(double ts, COLOR season) {
			col.set(cWater);
			col.multiply(season);
		}
		
		public MAP_BOOLEAN is = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return normal.is(tx, ty) || deep.is(tx, ty) || delta.is(tx, ty);
			}
			
			@Override
			public boolean is(int tile) {
				return normal.is(tile) || deep.is(tile) || delta.is(tile);
			}
		};
		
		public MAP_BOOLEAN isOpen = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return normal.is(tx, ty) || deep.is(tx, ty);
			}
			
			@Override
			public boolean is(int tile) {
				return normal.is(tile) || deep.is(tile);
			}
		};

	}
	
	private final class Normal extends WATER {

		private final OpenSet set;
		private final boolean fertile;
		
		private Normal(String name, OpenSet set, boolean fertile) {
			super(name);
			this.set = set;
			this.fertile = fertile;
		}

		
		@Override
		boolean coversCompleatly(int tile) {
			return dataGet(tile) == 0x0F;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (joins(tx,ty,d)) {
					res |= d.mask();
				}
			}
			int edge = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if (!joins(tx, ty, d) && joins(tx, ty, d.next(-1)) && joins(tx, ty, d.next(1))) {
					edge |= d.mask();
				}
			}
			if (res == 0x0F) {
				MOUNTAIN().clear(tx, ty);
				FOREST().amount.set(tx, ty, 0);
			}
			res |= edge << 4;
			dataSet(tx, ty, res);
		}
		
		private boolean joins(int tx, int ty, DIR d) {
			return !IN_BOUNDS(tx, ty, d) || is(tx, ty, d) || set.deep.is(tx, ty, d) || set.delta.is(tx, ty, d);
		}
		
		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			int d = data & 0x0F;
			int c = (data >> 4)&0x0F;
			set.col.bind();
			sprites.render(r, it, d, c);
			return data == 0x0F;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			set.cShore.bind();
			int d = data & 0x0F;
			int c = (data >> 4)&0x0F;
			sprites.renderBackground(r, it, d, c);
			return data == 0x0F;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}
		
		@Override
		boolean isFertile() {
			return fertile;
		}
		
		public void renderIcon(SPRITE_RENDERER r, int x, int y, int dim) {
			set.cShore.bind();
			sprites.bgSingles.render(r, 0, x, x+dim, y, y+dim);
			sprites.bgSingles.render(r, 0, x, x+dim, y, y+dim);
			set.cWater.bind();
			sprites.sheetSingles.render(r, 0, x, x+dim, y, y+dim);
			COLOR.unbind();
		}

	}
	
	private final class Deep extends WATER {

		private final OpenSet set;
		private final boolean fertile;
		
		private Deep(String name, OpenSet set, boolean fertile) {
			super(name);
			this.set = set;
			this.fertile = fertile;
		}
		
		@Override
		boolean coversCompleatly(int tile) {
			return true;
		}

		@Override
		void pplace(int tx, int ty) {
			
			if (isPlacable(tx, ty, null, null) != null) {
				set.normal.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}
			}
			MOUNTAIN().clear(tx, ty);
			FOREST().amount.set(tx, ty, 0);
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			set.col.bind();
			sprites.renderDeep(r, it, data&0x0F);
			return true;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (IN_BOUNDS(tx, ty, d) && !is(tx, ty, d) && !set.normal.is(tx, ty, d))
					return "";
				if (set.delta.is(tx, ty))
					return "";
			}
			return null;
		}
		
		@Override
		boolean isFertile() {
			return fertile;
		}

	}

	private final class River extends WATER{

		private River() {
			super("river");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return dataGet(tile) == 0x0F;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}else if(LAKE.delta.is(tx, ty, d) || OCEAN.delta.is(tx, ty, d)) {
					int x = tx+d.x()*2;
					int y = ty+d.y()*2;
					if (OCEAN.normal.is(x, y) || LAKE.normal.is(x, y)) {
						res |= d.mask();
					}
				}
			}
			if (res == 0x0F) {
				MOUNTAIN().clear(tx, ty);
				FOREST().amount.set(tx, ty, 0);
			}
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.col.bind();
			sprites.riverFG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			sprites.renderTexture(it);
			return false;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.cShore.bind();
			sprites.riverBG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}
		
	}
	
	private final class RiverSmall extends WATER{

		private RiverSmall() {
			super("river small");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return dataGet(tile) == 0x0F;
		}

		@Override
		void pplace(int tx, int ty) {
			placeRaw(tx, ty);
			int res = 0;
			int other = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d)) {
					res |= d.mask();
				}else if(get(tx, ty, d) != NOTHING) {
					res |= d.mask();
					if (!this.is(tx, ty, d))
						other |= d.mask();
				}
			}
			dataSet(tx, ty, res | (other <<4));
		}

		@Override
		boolean renderMid(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.col.bind();
			int o = data >>> 4;
			data &= 0x0F;
			sprites.riverSmallFG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (o != 0) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if ((d.mask() & o) != 0) {
						int x = it.x()+d.x()*C.TILE_SIZE;
						int y = it.y()+d.y()*C.TILE_SIZE;
						int da = d.perpendicular().mask();
						sprites.riverSmallFG.render(r, da+(it.ran()&7)*16, x, y);
					}
				}
			}
			
			return false;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			LAKE.cShore.bind();
			int o = data >>> 4;
			data &= 0x0F;
			sprites.riverSmallBG.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (o != 0) {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if ((d.mask() & o) != 0) {
						int x = it.x()+d.x()*C.TILE_SIZE;
						int y = it.y()+d.y()*C.TILE_SIZE;
						int da = d.perpendicular().mask();
						sprites.riverSmallBG.render(r, da+(it.ran()&7)*16, x, y);
					}
				}
			}
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			return false;
		}
		
	}
	
	private final class Delta extends WATER {

		private final OpenSet set;
		
		private Delta(String name, OpenSet set) {
			super(name);
			this.set = set;
		}

		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		void pplace(int tx, int ty) {
			if (isPlacable(tx, ty, null, null) != null) {
				set.normal.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (set.normal.is(tx, ty, d)) {
					res = i;
					break;
				}
			}

			dataSet(tx, ty, res);
		}

		@Override
		boolean render(SPRITE_RENDERER r, int data, RenderIterator it) {
			RIVER.render(r, DIR.ORTHO.get(data).mask()|DIR.ORTHO.get(data).perpendicular().mask(), it);
			set.col.bind();
			sprites.delta.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			return false;
		}
		
		@Override
		boolean renderShore(SPRITE_RENDERER r, int data, RenderIterator it) {
			RIVER.renderShore(r,  DIR.ORTHO.get(data).mask()|DIR.ORTHO.get(data).perpendicular().mask(), it);
			set.cShore.bind();
			sprites.deltaShore.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			return false;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return bordersCount(tx, ty, set.normal) == 1 ? null : "";
		}
		
		@Override
		boolean isFertile() {
			return true;
		}
	}
	
	public boolean canCrossRiver(int fromX, int fromY, int toX, int toY) {
		if (RIVER.is(fromX, fromY)) {
			return (Math.abs(fromX-toX) + Math.abs(fromY-toY) <= 1) && !RIVER.is(toX, toY);
		}
		return false;
		
	}
	
	public MAP_BOOLEAN isRivery = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return RIVER.is(tx, ty) || LAKE.delta.is(tx, ty) || OCEAN.delta.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return RIVER.is(tile) || LAKE.delta.is(tile) || OCEAN.delta.is(tile);
		}
	};
	
	public MAP_BOOLEAN isDELTA = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return get(tx, ty) instanceof Delta;
		}
		
		@Override
		public boolean is(int tile) {
			return get(tile) instanceof Delta;
		}
	};
	
	double add(WorldTerrainInfo info, int tx, int ty) {
		if (RIVER.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.125);
			info.addFertility(0.05);
			return 0.125;
		}else if (OCEAN.delta.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.OCEAN(), 0.5);
			return 0.75;
		}else if (LAKE.delta.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.WET(), 0.5);
			info.addFertility(0.1);
			return 0.75;
		}else if (OCEAN.isOpen.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (OCEAN.is.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.OCEAN(), m);
			return m;
		}else if (LAKE.isOpen.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (LAKE.is.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.WET(), m);
			info.addFertility(0.1);
			return m;
		}
		return 0;
			
	}
	
	public MAP_BOOLEAN has = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return !NOTHING.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)) != NOTHING;
		}
	};
	
	public MAP_BOOLEAN fertile = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return get(tx, ty).isFertile();
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)).isFertile();
		}
	};
	
	public MAP_BOOLEAN coversTile = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return is(tx+ty*TWIDTH());
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(tiles.get(tile)).coversCompleatly(tile);
		}
	};
	
	public boolean borders(int x, int y, WATER terrain){
		
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (terrain.is(x, y, DIR.ORTHO.get(i)))
				return true;
		}	
		return false;
	}
	
	public int bordersCount(int x, int y, WATER tiles) {
		
		int j = 0;
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (tiles.is(x, y, DIR.ORTHO.get(i)))
				j++;
		}	
		return j;
	}
	
}
