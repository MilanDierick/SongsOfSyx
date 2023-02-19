package world.map.terrain;

import static world.World.*;

import java.io.IOException;

import game.time.TIME;
import init.biomes.TERRAINS;
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
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import view.tool.*;
import view.world.IDebugPanelWorld;
import world.World;
import world.World.WorldResource;

public class WorldWater extends WorldResource{

	private final Bitsmap1D tiles = new Bitsmap1D(-1, 3, TAREA());
	private final byte[] data;
	private final ArrayListResize<WATER> all = new ArrayListResize<>(255, 255);
	private final COLOR[] seasonColors = new COLOR[64];
	
	
	public final WATER NOTHING = new WATER("clear") {
		
		
		@Override
		boolean render(Renderer r, int data, RenderIterator it) {
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
		public ICON.MEDIUM getIcon() {
			return SPRITES.icons().m.cancel;
		}
		
		@Override
		boolean isFertile() {
			return false;
		}

	};
	public final WATER LAKE = new Lake();
	public final WATER OCEAN = new Ocean();
	public final WATER ABYSS = new Abyss();
	public final WATER RIVER = new River();
	public final DeltaLake DELTA_LAKE = new DeltaLake();
	public final DeltaOcean DELTA_OCEAN = new DeltaOcean();
	public final SPRITE iconSweet;
	public final SPRITE iconSalt;
	
	private final WorldWaterAnimation animation = new WorldWaterAnimation();
	
	public WorldWater(){

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
				int d = (Y2-Y1)/2;
				World.sprites().lake.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+d, Y1, Y1+d);
				World.sprites().lake.render(r, DIR.S.mask() | DIR.W.mask(), X1+d, X1+d*2, Y1, Y1+d);
				World.sprites().lake.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+d, Y1+d, Y1+d*2);
				World.sprites().lake.render(r, DIR.N.mask() | DIR.W.mask(), X1+d, X1+d*2, Y1+d, Y1+d*2);
			}
		};
		
		iconSalt = new SPRITE.Imp(ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int d = (Y2-Y1)/2;
				World.sprites().ocean.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+d, Y1, Y1+d);
				World.sprites().ocean.render(r, DIR.S.mask() | DIR.W.mask(), X1+d, X1+d*2, Y1, Y1+d);
				World.sprites().ocean.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+d, Y1+d, Y1+d*2);
				World.sprites().ocean.render(r, DIR.N.mask() | DIR.W.mask(), X1+d, X1+d*2, Y1+d, Y1+d*2);
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
	protected void update(float ds) {
		animation.update(ds);
	}
	
	public void render(Renderer r, RenderData data, double season){
		
		RenderIterator it = data.onScreenTiles();
		
		int i = (int) (TIME.years().bitPartOf()*seasonColors.length);
		i %= seasonColors.length;
		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();
		
		while(it.has()) {
			if (get(it.tx(), it.ty()).render(r, dataGet(it.tile()), it)) {
				it.hiddenSet();
			}
			it.next();
		}
		COLOR.unbind();
	}
	
	private int codeGet(int tx, int ty) {
		return codeGet(tx+ty*TWIDTH());
	}
	
	private int codeGet(int tile) {
		return tiles.get(tile);
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
	
	public MAP_BOOLEAN has = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return !NOTHING.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(codeGet(tile)) != NOTHING;
		}
	};
	
	public MAP_BOOLEAN fertile = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return get(tx, ty).isFertile();
		}
		
		@Override
		public boolean is(int tile) {
			return all.get(codeGet(tile)).isFertile();
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
			return all.get(codeGet(tile)).coversCompleatly(tile);
		}
	};
	
	public WATER get(int tx, int ty){
		if (!IN_BOUNDS(tx, ty))
			return NOTHING;
		return all.get(codeGet(tx, ty)); 
	}
	
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
				int old = codeGet(tx, ty);
				pplace(tx, ty);
				if (old != codeGet(tx, ty)) {
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
			return all.get(codeGet(tile)) == this; 
		}
		
		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*TWIDTH()); 
		}
		
		abstract boolean render(Renderer r, int data, RenderIterator it);
		
		abstract boolean isFertile();

		@Override
		public PLACABLE getUndo() {
			return NOTHING;
		}
		
	}
	
	class Lake extends WATER{

		private final int NORMALS = 0;
		private final int CORNERS = NORMALS+8*16;
		private final int SINGLES = CORNERS + 16;
		
		private Lake() {
			super("lake");
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
				if (!IN_BOUNDS(tx, ty, d) || this.is(tx, ty, d) || DELTA_LAKE.is(tx, ty ,d)) {
					res |= d.mask();
				}
			}
			int edge = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if (!is(tx, ty, d) && is(tx, ty, d.next(-1)) && is(tx, ty, d.next(1))) {
					edge |= d.mask();
					break;
				}
			}
			if (res == 0x0F) {
				MOUNTAIN().clear(tx, ty);
				FOREST().amount.set(tx, ty, 0);
			}
			res |= edge << 4;
			
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(Renderer r, int data, RenderIterator it) {

			if (data == 0) {
				World.sprites().lake.render(r, SINGLES+(it.ran()&0x0F), it.x(), it.y());
				animation.render(it);
				return false;
			}
			
			int corners = data >> 4;
			data &= 0x0F;
			World.sprites().lake.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (corners != 0) {
				World.sprites().lake.render(r, CORNERS + corners, it.x(), it.y());
			}
			animation.render(it);
			return data == 0x0F;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return null;
		}

		@Override
		public ICON.MEDIUM getIcon() {
			return World.sprites().icons.lake;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}
	}
	
	private class Ocean extends WATER{

		private final int NORMALS = 0;
		private final int CORNERS = NORMALS+8*16;
		
		protected Ocean() {
			super("ocean");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return (dataGet(tile)) == 0x0F;
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
					break;
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
			return !IN_BOUNDS(tx, ty, d) || is(tx, ty, d) || ABYSS.is(tx, ty, d) || DELTA_OCEAN.is(tx, ty, d);
		}

		@Override
		boolean render(Renderer r, int data, RenderIterator it) {
			int corners = data >> 4;
			data &= 0x0F;
			World.sprites().ocean.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			if (corners != 0) {
				World.sprites().ocean.render(r, CORNERS + corners, it.x(), it.y());
			}
			animation.render(it);
			return data == 0x0F;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public ICON.MEDIUM getIcon() {
			return World.sprites().icons.ocean;
		}
		
		@Override
		boolean isFertile() {
			return false;
		}
		
	}
	
	class Abyss extends WATER{

		private final int start = 8*16+16;
		
		private Abyss() {
			super("abyss");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return true;
		}

		@Override
		void pplace(int tx, int ty) {
			
			if (isPlacable(tx, ty, null, null) != null) {
				OCEAN.pplace(tx, ty);
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
		boolean render(Renderer r, int data, RenderIterator it) {
			data += start;
			World.sprites().ocean.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			
			animation.render(it);
			return true;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (IN_BOUNDS(tx, ty, d) && !is(tx, ty, d) && !OCEAN.is(tx, ty, d))
					return "";
				if (DELTA_OCEAN.is(tx, ty))
					return "";
			}
			return null;
		}
		
		@Override
		public ICON.MEDIUM getIcon() {
			return World.sprites().icons.abyss;
		}
		
		@Override
		boolean isFertile() {
			return false;
		}
	}
	
	final class River extends WATER{

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
				}else if(DELTA_LAKE.is(tx, ty, d) || DELTA_OCEAN.is(tx, ty, d)) {
					int x = tx+d.x()*2;
					int y = ty+d.y()*2;
					if (OCEAN.is(x, y) || LAKE.is(x, y)) {
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
		boolean render(Renderer r, int data, RenderIterator it) {
			World.sprites().river.render(r, data+(it.ran()&7)*16, it.x(), it.y());
			animation.render(it);
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public ICON.MEDIUM getIcon() {
			return World.sprites().icons.river;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}
		
		public final MAP_BOOLEANE crossing = new MAP_BOOLEANE() {
			
			@Override
			public boolean is(int tx, int ty) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean is(int tile) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public MAP_BOOLEANE set(int tx, int ty, boolean value) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public MAP_BOOLEANE set(int tile, boolean value) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
	}
	
	final class DeltaLake extends WATER{

		private DeltaLake() {
			super("lake delta");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		void pplace(int tx, int ty) {
			if (isPlacable(tx, ty, null, null) != null) {
				LAKE.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (LAKE.is(tx, ty, d)) {
					res = i;
					break;
				}
			}

			dataSet(tx, ty, res);
		}

		@Override
		boolean render(Renderer r, int data, RenderIterator it) {
			data += 16*9;
			World.sprites().river.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			animation.render(it);
			return false;
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return bordersCount(tx, ty, LAKE) == 1 && + bordersCount(tx, ty, OCEAN) == 0 ? null : "";
		}
		
		private final ICON.MEDIUM icon = new ICON.MEDIUM() {
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				World.sprites().icons.lake.renderTextured(texture, X1, X2, Y1, Y2);
				World.sprites().icons.river.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				World.sprites().icons.lake.render(r, X1, X2, Y1, Y2);
				World.sprites().icons.river.render(r, X1, X2, Y1, Y2);
			}
		};
		
		@Override
		public ICON.MEDIUM getIcon() {
			return icon;
		}
		
		@Override
		boolean isFertile() {
			return true;
		}
		
	}
	
	final class DeltaOcean extends WATER{

		private DeltaOcean() {
			super("ocean delta");
		}

		@Override
		boolean coversCompleatly(int tile) {
			return false;
		}

		@Override
		void pplace(int tx, int ty) {
			if (isPlacable(tx, ty, null, null) != null) {
				OCEAN.pplace(tx, ty);
				return;
			}
			
			placeRaw(tx, ty);
			int res = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (OCEAN.is(tx, ty, d) || ABYSS.is(tx, ty, d)) {
					res = i;
					break;
				}
			}
			dataSet(tx, ty, res);
		}

		@Override
		boolean render(Renderer r, int data, RenderIterator it) {
			data += 16*8;
			World.sprites().river.render(r, data+(it.ran()&3)*4, it.x(), it.y());
			animation.render(it);
			return false;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return bordersCount(tx, ty, LAKE) == 0 && (bordersCount(tx, ty, OCEAN) == 1 || bordersCount(tx, ty, ABYSS) == 1) ? null : "";
		}
		
		private final ICON.MEDIUM icon = new ICON.MEDIUM() {
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				World.sprites().icons.ocean.renderTextured(texture, X1, X2, Y1, Y2);
				World.sprites().icons.river.renderTextured(texture, X1, X2, Y1, Y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				World.sprites().icons.ocean.render(r, X1, X2, Y1, Y2);
				World.sprites().icons.river.render(r, X1, X2, Y1, Y2);
			}
		};
		
		@Override
		public ICON.MEDIUM getIcon() {
			return icon;
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
			return RIVER.is(tx, ty) || DELTA_LAKE.is(tx, ty) || DELTA_OCEAN.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return RIVER.is(tile) || DELTA_LAKE.is(tile) || DELTA_OCEAN.is(tile);
		}
	};
	
	public MAP_BOOLEAN isOCEAN = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return OCEAN.is(tx, ty) || ABYSS.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return OCEAN.is(tile) || ABYSS.is(tile);
		}
	};
	
	public MAP_BOOLEAN isOceany = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return OCEAN.is(tx, ty) || ABYSS.is(tx, ty) || DELTA_OCEAN.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return OCEAN.is(tile) || ABYSS.is(tile) || DELTA_OCEAN.is(tile);
		}
	};
	
	public MAP_BOOLEAN isLaky = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return DELTA_LAKE.is(tx, ty) || LAKE.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return DELTA_LAKE.is(tile) || LAKE.is(tile);
		}
	};
	
	public MAP_BOOLEAN isDELTA = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return DELTA_LAKE.is(tx, ty) || DELTA_OCEAN.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return DELTA_LAKE.is(tile) || DELTA_OCEAN.is(tile);
		}
	};
	
	double add(WorldTerrainInfo info, int tx, int ty) {
		if (RIVER.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.125);
			info.addFertility(0.05);
			return 0.125;
		}else if (DELTA_OCEAN.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.OCEAN(), 0.5);
			return 0.75;
		}else if (DELTA_LAKE.is(tx, ty)) {
			info.add(TERRAINS.WET(), 0.25);
			info.add(TERRAINS.WET(), 0.5);
			info.addFertility(0.1);
			return 0.75;
		}else if (OCEAN.is(tx, ty) || ABYSS.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (isOceany.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.OCEAN(), m);
			return m;
		}else if (LAKE.is(tx, ty)) {
			double m = 0.5;
			for (DIR d : DIR.ORTHO) {
				if (isLaky.is(tx, ty, d)) {
					m += 0.25/2;
				}
			}
			info.add(TERRAINS.WET(), m);
			info.addFertility(0.1);
			return m;
		}
		return 0;
			
	}
	
}
