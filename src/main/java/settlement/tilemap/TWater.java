package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.time.TIME;
import init.*;
import init.biomes.TERRAIN;
import init.biomes.TERRAINS;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.settings.S;
import init.sound.SOUND;
import init.sound.SoundSettlement;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.bit.Bits;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public class TWater extends TerrainTile {

	private final Sprites sprites;
	protected static String ¤¤name = "¤Water";
	protected static String ¤¤nameDeep = "¤Deep Water";

	static {
		D.ts(TWater.class);
	}
	
	public final Deep DEEP;
	
	private final static int saltBit = 				0b0000_1000_0000_0000;
	private final static Bits bradius = new Bits(	0b1111_0000_0000_0000);
	private final int reserveBit = 					0b0000_0001_0000_0000;
	private final int deepBit = 					0b0000_0010_0000_0000;
	private  int iceI = 0;
	
	private TerrainClearing clearing = new TerrainClearing() {
		@Override
		public RESOURCE resource() {
			return null;
		}
		
		@Override
		public boolean clear1(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return false;
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			return 0;
		}
		
		@Override
		public SoundSettlement.Sound sound() {
			return SOUND.sett().action.dig;
		}
		
		@Override
		public double strength() {
			return 2000*C.TILE_SIZE;
		};


	};
	
	public MAP_BOOLEAN hasFish = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tile) {
			return TWater.this.is(tile) && (shared.data.get(tile) & 0x0F) == 0x0F;
		}

		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return is(tx+ty*TWIDTH);
			return false;
		}
		
	};
	
	public MAP_BOOLEANE isSalty = new MAP_BOOLEANE() {

		@Override
		public boolean is(int tile) {
			return TWater.this.is(tile) && (shared.data.get(tile) & saltBit) != 0;
		}

		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return is(tx+ty*TWIDTH);
			return false;
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			int d = shared.data.get(tile);
			if (value)
				d |= saltBit;
			else
				d &= ~saltBit;
			shared.data.set(tile, d);
			return this;
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			if (TWater.this.is(tx, ty)) {
				set(tx+ty*TWIDTH, value);
			}
			return this;
		}
		
	};
	
	public MAP_DOUBLE radius = new MAP_DOUBLE() {
		
		private final double ri = 1.0/16;
		
		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return (bradius.get(shared.data.get(tile))+1)*ri;
		}
	};

	public TWater(Terrain t) throws IOException {
		super(t, ¤¤name, SPRITES.icons().m.cancel, t.colors.minimap.water);
		this.sprites = new Sprites();
		
		DEEP = new Deep(t);
		
	}

	
	public boolean isService(int tx, int ty) {
		if (is(tx, ty)) {
			int d = shared.data.get(tx, ty);
			return (d & 0x0F) == 0x0F;
		}
		return false;
	}
	
	public boolean reservable(int tx, int ty) {
		return isService(tx, ty) && (shared.data.get(tx, ty) & reserveBit) != reserveBit;
	}
	
	public boolean reserved(int tx, int ty) {
		return isService(tx, ty) && (shared.data.get(tx, ty) & reserveBit) == reserveBit;
	}
	
	public void reserve(int tx, int ty) {
		shared.data.set(tx, ty, (shared.data.get(tx, ty) | reserveBit));
	}
	
	public void unreserve(int tx, int ty) {
		shared.data.set(tx, ty, (shared.data.get(tx, ty) & ~reserveBit));
	}
	
	@Override
	void unplace(int tx, int ty) {
		if (reservable(tx, ty))
			PATH().finders.water.report(tx, ty, -1);
	}
	
	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	COLOR miniCPimp(ColorImp c, int x, int y, boolean northern, boolean southern) {
		return mini.miniCPimp(c, x, y, northern, southern);
	}

	private boolean setRadiusAndData(int old, int data, int x, int y) {
		double am = 0;
		int i = 0;
		boolean salt = isSalty.is(x, y);
		while (RES.circle().radius(i) < 4) {
			int dx = x+RES.circle().get(i).x();
			int dy = y+RES.circle().get(i).y();
			if (isWater(dx, dy) && isSalty.is(dx, dy) == salt)
				am++;
			i++;
		}
		int ra = (int) (0x0F*CLAMP.d(2.0*am/i, 0, 1));
		
		int n = bradius.set(data, ra);
		shared.data.set(x, y, n);
		
		return bradius.get(old) != bradius.get(n);
	}
	
	public boolean isOpen(int x, int y) {
		if (is(x, y)) {
			return (shared.data.get(x, y) & 0x0F) == 0x0F;
		}
		return DEEP.is(x, y);
	}
	
	public boolean isOpenNonFrozen(int x, int y) {
		if (is(x, y)) {
			return (shared.data.get(x, y) & 0x0F) == 0x0F && !isIce(x, y);
		}
		return DEEP.is(x, y);
	}

	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}

	@Override
	protected boolean place(int x, int y) {
		
		
		if (IN_BOUNDS(x, y)) {
			return setCode(x, y);
		}
		return false;
	}

	private boolean setCode(int x, int y) {
		
		int sBit = isSalty.is(x, y) ? saltBit : 0;
		int old = shared.data.get(x, y);
		super.placeRaw(x, y);
		int c = getCode(x, y);
		c |= getCodeCorner(c, x, y) << 4;
		c |= sBit;
		for (DIR d : DIR.ORTHO) {
			if (DEEP.is(x+d.x(), y+d.y())) {
				c |= deepBit;
				break;
			}
		}
		boolean ret = setRadiusAndData(old, c, x, y);
		if (reservable(x, y))
			PATH().finders.water.report(x, y, 1);
		return ret;
	}
	
	private int getCode(int x, int y) {
		int m = 0;
		for (DIR d : DIR.ORTHO) {
			if (isWater(x+d.x(), y+d.y()) || !SETT.IN_BOUNDS(x,y,d))
				m |= d.mask();
		}
		return m;
	}
	
	private int getCodeCorner(int m, int x, int y) {
		
		int c = 0;
		for (DIR d : DIR.NORTHO) {
			if ((m & d.next(-1).mask()) != 0 && (m & d.next(1).mask()) != 0 && !isWater(x+d.x(), y+d.y())) {
				c |= d.mask();
			}
		}
		return c;
	}

	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
//		if (isService(i.tx(), i.ty()))
//			if (reservable(i.tx(), i.ty()))
//				COLOR.GREEN100.render(r, i.x(), i.y());
//			else
//				COLOR.RED100.render(r, i.x(), i.y());
		return false;
	}

	@Override
	protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		sprites.render(data&0x0F, (data>>4)&0x0F, i, sprites.shore, sprites.normal);
		sprites.renderTexture(i);
		COLOR.unbind();
		i.countWater();
		
		return (data&0x0F) == 0x0F;

	}
	
	@Override
	protected boolean renderMid(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
		if (isIce(i.tile())) {
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (isIce(i, d) || !SETT.IN_BOUNDS(i.tx(),i.ty(),d))
					m |= d.mask();
			}
			sprites.renderIce(m, 0, i);
			return m == 0x0F;
		}
		return false;
	}

	private boolean isIce(RenderIterator i, DIR d) {
		return is(i.tx(), i.ty(), d) && isIce(i.tx()+d.x(), i.ty()+d.y());
	}
	
	private boolean isIce(int tx, int ty) {
		return iceI > (RES.ran1().get(tx+ty*TWIDTH)&0x0FFFF) && !SETT.ROOMS().map.is(tx, ty);
	}
	
	private boolean isIce(int tile) {
		return iceI > (RES.ran1().get(tile)&0x0FFFF) && !SETT.ROOMS().map.is(tile);
	}
	

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return AVAILABILITY.PENALTY3;
	}

	protected void update(float ds) {
		sprites.update(ds);
		iceI = (int) (SETT.WEATHER().ice.getD()*0x01FFFF);
	}
	
	public boolean isWater(int x, int y) {
		return is(x,y) || DEEP.is(x, y);
	}
	
	@Override
	void hoverInfo(GBox box, int tx, int ty) {
		super.hoverInfo(box, tx, ty);
		if (S.get().developer)
			box.add(box.text().add(radius.get(tx, ty)));
	}
	
	@Override
	public TERRAIN terrain(int tx, int ty) {
		if (SETT.GROUND().getter.get(tx, ty).fertility == 0)
			return TERRAINS.OCEAN();
		return TERRAINS.WET();
	}
	
	private final Minimap mini = new Minimap();
	
	private static class Minimap {
		
		
		private final int w = 64;
		private final int h = 32;
		private byte[][] values = new byte[h][w];
		
		Minimap() {

			for (int i = 0; i < h; i++) {
				drawWave(i, (byte) (24-(i&0b11)*16));
			}
		}
		
		private void drawWave(int sy, byte v) {
			
			double period = 5;
			double bend = 3;
			
			for(int i = 0; i < w; i++) {
				int x = i;
				double d = period*(i/(double)w)*Math.PI;
				int y = sy + (int) (bend*Math.sin(d));
				y &= h-1;
				values[y][x] += v;
			}
		}
		
		COLOR miniCPimp(ColorImp c, int x, int y, boolean northern, boolean southern) {
			x &= w-1;
			y &= h-1;
			if (values[y][x] != 0) {
				double v = 1.0-(0.5*values[y][x]/128.0);
				c.shadeSelf(v);
			}
			return c;
		}
		
	}
	
	public class Deep extends TerrainTile{
		
		Deep(Terrain t) {
			super(t, ¤¤nameDeep, SPRITES.icons().m.cancel, t.colors.minimap.water_deep);
			
		}

		@Override
		protected boolean place(int x, int y) {
			int old = shared.data.get(x, y);
			super.placeRaw(x, y);
			int sBit = isSalty.is(x, y) ? saltBit : 0;
			int m = 0;
			boolean sh = false;
			for (DIR d : DIR.ORTHO) {
				if (is(x+d.x(), y+d.y()) || !SETT.IN_BOUNDS(x,y,d)) {
					m |= d.mask();
				}else if (TWater.this.is(x+d.x(), y+d.y())) {
					sh = true;
				}
			}
			int c = 0;
			for (DIR d : DIR.NORTHO) {
				if ((m & d.next(-1).mask()) != 0 && (m & d.next(1).mask()) != 0 && !is(x+d.x(), y+d.y())) {
					c |= d.mask();
				}
			}
			m |= c << 4;
			if (sh)
				m |= 0b1_0000_0000;
			m |= sBit;
			return setRadiusAndData(old, m, x, y);
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		@Override
		protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int d = 0;
			if ((data & 0b1_0000_0000) != 0) {
				d = TWater.this.getCode(i.tx(), i.ty());
				int c = TWater.this.getCodeCorner(d, i.tx(), i.ty());
				sprites.render(d, c, i, sprites.shore, sprites.normal);
			}
			
			sprites.render(data&0x0F, (data >> 4)&0x0F, i, sprites.normal, sprites.deep);
			sprites.renderTexture(i);
			COLOR.unbind();
			i.countWater();
			
			return ((data|d) &0x0F) == 0x0F;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return AVAILABILITY.NOT_ACCESSIBLE;
		}
		
		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}
		
		@Override
		public int miniDepth() {
			// TODO Auto-generated method stub
			return 1;
		}
		
		@Override
		public TERRAIN terrain(int tx, int ty) {
			if (SETT.GROUND().getter.get(tx, ty).fertility == 0)
				return TERRAINS.OCEAN();
			return TERRAINS.WET();
		}
		
		@Override
		COLOR miniCPimp(ColorImp c, int x, int y, boolean northern, boolean southern) {
			if ((shared.data.get(x, y) & 0b1_0000_0000) != 0)
				c.interpolate(c, shared.WATER.miniC, 0.5);
			
			return mini.miniCPimp(c, x, y, northern, southern);
		}
	}

	
	@Override
	public int miniDepth() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	private static class Sprites {
		
		private final TILE_SHEET stencil;
		private final TILE_SHEET animation;
		private final TILE_SHEET animation_corner;
		private final TILE_SHEET single_stencil;
		private final TILE_SHEET single_animation;
		private final TILE_SHEET ice;
		private final TILE_SHEET ice_fulls;
		
		public final ColorImp shore = new ColorImp(10, 40, 100);
		public final ColorImp normal = shore.shade(0.5);
		public final ColorImp deep = normal.shade(0.5);
		
		private short shoreOff = 0;
		private double waterTimer;
		private int shoreDir;
		
		private final TileTextureScroller dis1 = SPRITES.textures().dis_big.scroller(1.1, -1.1);
		private final TileTextureScroller dis2 = SPRITES.textures().dis_tiny.scroller(-0.8, 0.8);
		private final TileTextureScroller tex1 = SPRITES.textures().water3.scroller(-1, -1);
		private final TileTextureScroller tex2 = SPRITES.textures().water2.scroller(1.5, 1.5);
		private final OpacityImp o2 = new OpacityImp((int) (255*0.25));
		
		private final int[] offs = new int[16];

		Sprites() throws IOException {
			
			for (int i = 0; i < offs.length; i++) {
				int k = i;
				if (i > 7) {
					k = 7 - (i-7);
					if (k < 0)
						k = 0;
				}
				offs[i] = k;
			}
			
			stencil = new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Water"), 576, 300) {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, 0, 4, 1, d.s16);
					for (int i = 0; i < 4; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();
			
			animation = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.house.body().y2(), 4, 2, d.s16);
					for (int i = 0; i < 8; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();
			
			animation_corner = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					for (int i = 0; i < 8; i+=2)
						s.house.setVar(i).pasteEdges(true);
					return d.s16.saveGame();
				}

			}.get();
			
			single_stencil = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();
			
			single_animation = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.full.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();
			
			
			ice = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.full.body().y2(), 4, 1, d.s16);
					for (int i = 0; i < 4; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();
			
			ice_fulls = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();
		}
		
		protected void update(float ds) {
			waterTimer += ds;
			if (waterTimer > 0.2f) {
				waterTimer -= 0.2f;
				shoreOff += shoreDir;
				if (shoreOff >= 7) {
					shoreDir = -1;
				} else if (shoreOff == 0) {
					shoreDir = 1;
				}

			}
			
			double wx = ds*(1 + SETT.WEATHER().wind.getD()*6)*SETT.WEATHER().wind.dirX();
			double wy = ds*(1 + SETT.WEATHER().wind.getD()*6)*SETT.WEATHER().wind.dirY();
			dis1.update(ds);
			dis2.update(SETT.WEATHER().wind.dirX()*ds*0.8, SETT.WEATHER().wind.dirY()*ds*0.8);
			tex1.update(ds);
			tex2.update(-wx*1.5, -wy*1.2);
			
			shore.interpolate(SETT.TERRAIN().colors.waternormal, SETT.TERRAIN().colors.waterWinter, 1.0-SETT.WEATHER().growth.getD());
			normal.set(shore).shadeSelf(0.5);
			deep.set(normal).shadeSelf(0.75);
			
		}
		
		public void renderIce(int mask, int corner, RenderIterator it) {
			int ran = it.ran();
			int x = it.x();
			int y = it.y();
			
			if (mask == 0x0F) {
				ice_fulls.render(CORE.renderer(), ran&0x0F, x, y);
			}else {
				ice.render(CORE.renderer(), (ran&0b11)*16 + mask, x, y);
			}
		}
		
		public void render(int mask, int corner, RenderIterator it, COLOR cForeGround, COLOR bg) {
			
			int ran = it.ran();
			int x = it.x();
			int y = it.y();
			
			if (mask == 0) {
				bg.bind();
				int t = ran &0x0F;
				single_stencil.render(CORE.renderer(), t, x, y);
				cForeGround.bind();
				single_stencil.renderTextured(single_animation.getTexture(t), t, x, y);
			}else {
				int stenI = (ran&0b11)*16 + mask;
				
				
				ran = ran >> 2;
				int off = (int) ((TIME.currentSecond())*5);
				off += -1 + (ran&3);
				off &= 0x0F;
				off = offs[off];
				
				int texI = off*16 + mask;

				bg.bind();
				stencil.render(CORE.renderer(), stenI, x, y);
				
				cForeGround.bind();
				if (mask != 0x0F)
					stencil.renderTextured(animation.getTexture(texI), stenI, x, y);
				
				
				if (corner != 0) {
					animation_corner.render(CORE.renderer(), 16*(off/2) + corner, x, y);
				}
			}
		}
		
		void renderTexture(RenderData.RenderIterator i) {

			if (S.get().graphics.get() == 0)
				return;
			
			normal.bind();
			o2.bind();
			
			CORE.renderer().renderDisplace(dis1.x1(i.tx()+4), dis1.y1(i.ty()+4), tex1.x1(i.tx()+2), tex1.y1(i.ty()+2), 
					C.T_PIXELS, C.T_PIXELS, 16, 
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);
			
			CORE.renderer().renderDisplace(dis1.x1(i.tx()+4), dis1.y1(i.ty()+4), tex1.x1(i.tx()+2), tex1.y1(i.ty()+2), 
					C.T_PIXELS, C.T_PIXELS, 16, 
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);
			COLOR.unbind();
			OPACITY.O25.bind();
			
			CORE.renderer().renderDisplace(dis2.x1(i.tx()), dis2.y1(i.ty()), tex2.x1(i.tx()), tex2.y1(i.ty()), 
					C.T_PIXELS, C.T_PIXELS, 8, 
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);
//			
			OPACITY.unbind();
		}
		
		
	}
	
	public void renderOverlayed(RenderData.RenderIterator i) {
		sprites.renderTexture(i);
	}
	


}
