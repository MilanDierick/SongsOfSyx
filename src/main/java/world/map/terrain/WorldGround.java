package world.map.terrain;

import static world.World.*;

import java.io.IOException;

import game.time.TIME;
import init.paths.PATHS;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.light.PointLight;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.*;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.world.IDebugPanelWorld;
import world.World;

public class WorldGround extends World.WorldResource {

	private final byte[] data;
	private final Bitsmap1D types = new Bitsmap1D(0, 3, TAREA());

	private final ArrayListResize<WGROUND> all = new ArrayListResize<>(10, 16);
	private final COLOR[] seasonColors = new COLOR[64];

	private final PointLight light = new PointLight();
	
	private final TILE_SHEET stencil = (new ITileSheet(PATHS.SPRITE().getFolder("world").getFolder("map").get("Ground"), 576, 538) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 8, 2, d.s16);
			s.singles.setSkip(1, 1).pasteEdges(true);
			return d.s16.saveGame();
			
		}
	}).get();
	
	
	public final SPRITE icon = new SPRITE.Imp(ICON.BIG.SIZE) {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int d = (Y2-Y1)/2;
			sheet.render(r, 0, X1, X1+d, Y1, Y1+d);
			sheet.render(r, 1, X1+d, X1+d*2, Y1, Y1+d);
			sheet.render(r, 2, X1, X1+d, Y1+d, Y1+d*2);
			sheet.render(r, 3, X1+d, X1+d*2, Y1+d, Y1+d*2);
			
		}
	};
	
	private final TILE_SHEET sheet = (new ITileSheet() {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			
			s.singles.init(0, 0, 1, 1, 8, 2, d.s16);
			s.house.init(0, s.singles.body().y2(), 4, 1, d.s16);
			s.full.init(0, s.house.body().y2(), 1, 5, 16, 4, d.s16);
			
			//lush
			//s.full.setVar(0).setSkip(64, 0).paste(true);
			
			make(c, s, d, 0, 0);
			
			//grass
			make(c, s, d, 0, 1);
			//patched
			make(c, s, d, 1, 2);
			//steppe
			make(c, s, d, 2, 3);
			//dessert
			make(c, s, d, 3, 4);
			return d.s16.saveGame();
			
		}
		
		private void make(ComposerUtil c, ComposerSources s, ComposerDests d, int bg, int fg) {
			s.full.setVar(bg).setSkip(64, 0).paste(false);
			s.full.setVar(fg).setSkip(16, 0).pasteStenciled(s.house.setVar(0), 0);
			s.full.setVar(fg).setSkip(16, 16).pasteStenciled(s.house.setVar(1), 0);
			s.full.setVar(fg).setSkip(16, 32).pasteStenciled(s.house.setVar(2), 0);
			for (int i = 0; i < 16; i++) {
				s.full.setVar(fg).setSkip(1, 32+i).pasteStenciled(s.singles.setSkip(0, 1), 0);
			}
			//s.full.setVar(fg).setSkip(16, 48).pasteStenciled(s.house.setVar(3), 0);
			s.full.setVar(fg).setSkip(32, 0).paste(true);
			

		}
	}).get();

	public final WGROUND LUSH = new WGROUND("lush lands", 0.8f, 0);
	
	public final WGROUND GRASS_LAND = new WGROUND("grassland", 0.65f, 0);
	
	public final WGROUND PATCHED_GRASS = new WGROUND("patched grass", 0.5f, 0);
	
	public final WGROUND STEPPE = new WGROUND("steppe", 0.35f, 4);
	
	public final WGROUND DESERT = new WGROUND("desert", 0.1f, 2);




	private final static int SET = 16;
	private final static int FULL_ROW = 6 * 16;
	private final static int FULLS = 4 * 16;

	public WorldGround() throws IOException{

		data = new byte[TAREA()];
		light.setGreen(1).setRed(2).setBlue(0.5);
		light.setFalloff(1);
		all.trim();

		for (WGROUND g : all) {
			IDebugPanelWorld.add(g);
		}


		ColorImp winter = new ColorImp(90, 105, 127);
		for (double i = 0; i < seasonColors.length; i++) {
			ColorImp p = new ColorImp();
			double d = i / (seasonColors.length - 1);
			if (d < 0.5)
				d = d * 2;
			else
				d = 1.0 - (d - 0.5) * 2;
			
			p.interpolate(COLOR.WHITE100, winter, CLAMP.c(d+0.75+0.125, 1));
			seasonColors[(int) i] = p;

		}
	}

	public LIST<WGROUND> all() {
		return all;
	}

	@Override
	protected void save(FilePutter f) {
		f.bs(data);
		types.save(f);
	}

	@Override
	protected void load(FileGetter f) throws IOException {
		f.bs(data);
		types.load(f);
	}

	public void render(Renderer r, RenderData data, double season) {

		RenderIterator it = data.onScreenTiles();
		int i = (int) (TIME.years().bitPartOf() * seasonColors.length);
		i %= seasonColors.length;
		ColorImp.TMP.interpolate(COLOR.WHITE100, seasonColors[i], season).bind();;
		;
		CORE.renderer().setUniLight(light);
		while (it.has()) {
			getter.get(it.tile()).render(r, it.x(), it.y(), this.data[it.tile()], it.ran() & 0x1F);
			it.next();
		}
		COLOR.unbind();

	}

	public void renderTextures(RenderIterator it, TextureCoords tex) {
		int t = all.get(types.get(it.tile())).getTile(data[it.tile()], it.ran() & 0x1F);
		sheet.renderTextured(tex, t, it.x(), it.y());
	}

	public TextureCoords texture(RenderIterator it) {
		return sheet.getTexture(all.get(types.get(it.tile())).getTile(data[it.tile()], it.ran() & 0x1F));
	}

	private void set(int tx, int ty, int code, int data) {
		int t = tx + ty * TWIDTH();
		this.data[t] = (byte) data;
		types.set(t, code);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return value between -1 to 0.5
	 */
	public float getFertility(int x, int y) {
		if (IN_BOUNDS(x, y))
			return getter.get(x, y).getFertility();
		return 0;
	}

	public final MAP_OBJECT<WGROUND> getter = new MAP_OBJECT<WorldGround.WGROUND>() {

		@Override
		public WGROUND get(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return null;
			return get(tx + ty * TWIDTH());
		}

		@Override
		public WGROUND get(int tile) {
			return all.get(types.get(tile));
		}
	};

	public class WGROUND extends PlacableMulti {

		protected final float fertility;
		protected final int code = all.add(this);

		protected WGROUND(String name, float fertility, int bg) {
			super(name);
			this.fertility = fertility;
		}

		boolean place(int tx, int ty) {
			int res = 0;
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				WGROUND neigh = getter.get(tx + d.x(), ty + d.y());
				if (neigh != null && code-neigh.code > 1) {
					all.get(code-1).place(tx, ty);
					return true;
				}
				
			}
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (joins(tx, ty, d)) {
					res |= d.mask();
				}
				
				
			}
			int sres = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if ((res & d.next(-1).mask()) != 0 && (res & d.next(1).mask()) != 0 && !joins(tx, ty, d))
					sres |= d.mask();
				
			}
			
			res |= sres << 4;
			res &= 0x0FF;
			set(tx, ty, code, res);
			
			return false;
		}
		
		private boolean joins(int tx, int ty, DIR d) {
			WGROUND neigh = getter.get(tx + d.x(), ty + d.y());
			if (neigh == null || neigh == this || (neigh.code - 1 == this.code)) {
				return true;
			}
			return false;
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			if (!IN_BOUNDS(tx, ty))
				return;
			WGROUND old = getter.get(tx, ty);
			if (place(tx, ty) || old != getter.get(tx, ty)) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					if (IN_BOUNDS(tx, ty, d)) {
						getter.get(tx + d.x(), ty + d.y()).place(tx + d.x(), ty + d.y(), area, type);
					}
				}
			}
		}

		final void placeRaw(int tx, int ty) {
			set(tx, ty, code, 0);
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {

			return null;
		}

		float getFertility() {
			return fertility;
		}

		@Override
		public ICON.MEDIUM getIcon() {
			return SPRITES.icons().m.cancel;
		}

		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && code == types.get(tx + ty * TWIDTH());
		}
		
		protected final void render(Renderer r, int x, int y, int data, int ran) {
			sheet.render(r, getTile(data&0x0F, ran), x, y);
			data = data >> 4;
			data &= 0x0F;
			if (data > 0) {
				stencil.renderTextured(sheet.getTexture((code) * FULL_ROW+SET*3), data, x, y);
			}
		}

		protected int getTile(int data, int ran) {
			int t = (code) * FULL_ROW;
			
			if (data == 0x0F) {
				t += FULLS;
				t += ran;
			} else {
				t += data;
				t += SET * (ran % 0x03);
			}
			return t;
		}

		public double fertility() {
			return fertility;
		}

		public WGROUND fallback() {
			return PATCHED_GRASS;
		}

	}



}
