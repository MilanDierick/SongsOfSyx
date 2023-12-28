package settlement.tilemap.terrain;

import java.io.IOException;
import java.util.Locale;

import game.GAME;
import init.C;
import init.RES;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sound.SoundSettlement;
import init.sound.SoundSettlement.Sound;
import init.sprite.UI.Icon;
import init.sprite.game.*;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.tilemap.terrain.TDestroyed.TDestoryable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import settlement.tilemap.terrain.TerrainDiagonal.Diagonalizer;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.keymap.RCollection;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class TFence extends TerrainTile implements INDEXED, TDestoryable, Diagonalizer{



	final static class TFences extends RCollection<TFence>{

		private final LIST<TFence> all;
		
		TFences(Terrain t) throws IOException {
			
			
			
			super("FENCE");
			String f = key.toLowerCase(Locale.ROOT);
			PATH data = PATHS.INIT_SETTLEMENT().getFolder(f);
			PATH text = PATHS.TEXT_SETTLEMENT().getFolder(f);
			
			
			String[] keys = data.getFiles();
			ArrayList<TFence> all = new ArrayList<>(keys.length);
			
			for (String key : keys) {
				Json da = new Json(data.get(key));
				Json te = new Json(text.get(key));
				
				Sheets sSquare = new Sheets(SheetType.sCombo, da.json("SPRITE_SQUARE_COMBO"));
				Sheets sRound = new Sheets(SheetType.sCombo, da.json("SPRITE_ROUND_COMBO"));
				
				
				SPRITE icon = new SPRITE.Imp(Icon.L) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						SheetPair da = sSquare.get(0);
						int z = CORE.renderer().getZoomout();
						CORE.renderer().setZoom(Integer.numberOfTrailingZeros(C.SCALE));
						X1*=C.SCALE;
						Y1*=C.SCALE;
						int d = C.TILE_SIZE;
						da.s.render(da.d, X1, Y1, null, r, DIR.S.mask() | DIR.E.mask(), 0, 0);
						da.s.render(da.d, X1+d, Y1, null, r, DIR.S.mask() | DIR.W.mask(), 0, 0);
						da.s.render(da.d, X1, Y1+d, null, r, DIR.N.mask() | DIR.E.mask(), 0, 0);
						da.s.render(da.d, X1+d, Y1+d, null, r, DIR.N.mask() | DIR.W.mask(), 0, 0);
						CORE.renderer().setZoom(z);
					}
				};
				
				TFence fe = new TFence(key, t, all, da, te, icon, sSquare, sRound);
				map.put(key, fe);	
			}
			if (all.size() > 16) {
				throw new Errors.GameError("Too many fences have been declared. Maximum is 16");
			}
			
			this.all = all;
			
		}

		@Override
		public LIST<TFence> all() {
			return all;
		}

		@Override
		public TFence getAt(int index) {
			return all.get(index);
		}

	}
	
	private final TerrainClearing clearing = new TerrainClearing() {
		
		@Override
		public RESOURCE clear1(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return RND.oneIn(3) ? resource : null;
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return 0;
		}
		
		@Override
		public SoundSettlement.Sound sound() {
			return sound;
		}
		
		@Override
		public boolean isEasilyCleared() {
			return false;
		};
		
		@Override
		public boolean isStructure() {
			return true;
		}; 
		
		@Override
		public void destroy(int tx, int ty) {
			shared.DESTROYED.place(tx, ty, TFence.this, getDia(tx, ty) ? 1 : 0);
		}

		@Override
		public double strength() {
			return 300*C.TILE_SIZE;
		}
		
	};

	
	private final Sound sound;
	public final String key;
	public final COLOR miniColor;
	public final CharSequence name;
	public final CharSequence desc;

	private final Sheets sSquare;
	private final Sheets sRound;

	public final RESOURCE resource;
	public final int resAmount;
	private final int index;
	private final int DIA = 0x0100;
	
	TFence(String key, Terrain t, LISTE<TFence> all, Json data, Json text, SPRITE icon,  Sheets sSquare, Sheets sRound) throws IOException {
		super("FENCE_" + key, t, text.text("NAME"), icon, new ColorImp(data, "MINIMAP_COLOR"));
		this.sSquare = sSquare;
		this.sRound = sRound;
		miniColor = new ColorImp(data, "MINIMAP_COLOR");
		name = text.text("NAME");
		desc = text.text("DESC");
		this.key = key;
		resource = RESOURCES.map().get(data);
		resAmount = data.i("RESOURCE_AMOUNT");
		index = all.add(this);
		sound = RES.sound().settlement.action.get(data);
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	
	@Override
	protected boolean place(int tx, int ty) {
		
		boolean dia = (shared.get(tx, ty) instanceof Diagonalizer) && ((Diagonalizer) shared.get(tx, ty)).getDia(tx, ty);
		
		int res = 0;
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null) {
			for (DIR d : DIR.ORTHO) {
				if (is(tx, ty, d)) {
					Room r2 = SETT.ROOMS().map.get(tx, ty, d);
					if (r2 == null || r2 == r)
						res |= d.mask();
				}
			}
		}else {
			for (DIR d : DIR.ORTHO)
				if (is(tx, ty, d))
					res |= d.mask();
		}
		
		super.placeRaw(tx, ty);
		shared.data.set(tx, ty, res);
		setDia(tx, ty, dia);
		
		return false;
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		if (((GAME.updateI() + i.tx()) & 0x0FF) == 0) {
			if (!SETT.ROOMS().map.is(i.tile()))
				SETT.FLOOR().setFloorMatch(i.tx(), i.ty(), null);
		}
		
		Sheets sh = sSquare;
		
		if ((data & DIA) != 0) {
			sh = sRound;
		}
		
		int k = (data>>4)&0x0F;
		SheetPair sheet = sh.get(i.ran());
		if (sheet == null)
			return false;
		sheet.d.color(k).bind();
		int ran = i.ran();
		
		int tile = SheetType.sCombo.tile(sSquare.get(0), data&0x0F, 0, 0);
		
		sheet.s.render(sheet.d, i.x(), i.y(), i, r, tile, ran, 0);
		COLOR.unbind();
		if (s != null)
			sheet.s.renderShadow(sheet.d, i.x(), i.y(), i, s, tile, ran);
		return false;

	}

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return AVAILABILITY.SOLID;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}

	@Override
	public int miniDepth() {
		return 2;
	}

	@Override
	public int index() {
		return index;
	}
	

	@Override
	public void setDia(int tx, int ty, boolean dia) {
		if (is(tx, ty)) {
			int d = shared.data.get(tx, ty);
			if (dia)
				d |= DIA;
			else
				d &= ~DIA;
			shared.data.set(tx, ty, d);
		}
		
	}

	@Override
	public boolean getDia(int tx, int ty) {
		if (is(tx, ty)) {
			return (shared.data.get(tx, ty) & DIA) != 0;
		}
		return false;
	}

	@Override
	public Job fixJob() {
		return SETT.JOBS().fences.get(index);
	}

	@Override
	public int resAmount() {
		return 2;
	}



	@Override
	public RESOURCE breakableRes() {
		return resource;
	}
	

}
