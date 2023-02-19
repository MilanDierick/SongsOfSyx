package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import game.faction.player.PlayerColors.PlayerColor;
import init.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.ICON;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.tilemap.TBuilding.Wall;
import settlement.tilemap.Terrain.TerrainTile;
import snake2d.*;
import snake2d.util.bit.Bits;
import snake2d.util.color.*;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.map.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.dic.DicMisc;
import util.keymap.KEY_COLLECTION;
import util.keymap.RCollection;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class Floors extends RCollection<Floors.Floor>{

	private final ArrayList<Floor> all;
	private final RFloorExtra extra;
	public final LIST<Floor> roads;
	private final byte[] tiles = new byte[TAREA];
	private final Bits bDegradeO = new Bits(0b011110000);
	
	private final int NOTHING = 0;
	private final Bitsmap1D types = new Bitsmap1D(-1, 6, TAREA);

	public final MAP_DOUBLE walkValue = new MAP_DOUBLE() {

		@Override
		public double get(int tile) {
			if (!getter.is(tile))
				return 0;
			return getter.get(tile).walkValue * (1.0 - degrade.get(tile));
		}

		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return 0;
		}
	};


	Floors() throws IOException {
		super("FLOOR");
		String mainKey = key.toLowerCase(Locale.ROOT);
		PATH gData = PATHS.INIT_SETTLEMENT().getFolder(mainKey);
		PATH gSprite = PATHS.SPRITE_SETTLEMENT().getFolder(mainKey);
		PATH gText = PATHS.TEXT_SETTLEMENT().getFolder(mainKey);
		
		String[] keys = gData.getFiles();
		{
			String[] kk = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				kk[i] = keys[i];
			}
			kk[kk.length-1] = "_GRASS";
		}
		LinkedList<Floor> all = new LinkedList<>();
		
		all.add((Floor) null);
		HashMap<String, Floor> others = new HashMap<>();
		for (String key : keys) {
			Json data = new Json(gData.get(key));
			Json text = data.has("ROAD") ? new Json(gText.get(key)) : null;
			String sp = data.value("SPRITE");
			if (others.containsKey(sp)) {
				map.put(key, new Floor(all, key, data, text, others.get(sp).sheet));
			}else {
				TILE_SHEET sheet = new ITileSheet(gSprite.get(sp), 536, 28) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.full.init(0, 0, 1, 1, 16, 1, d.s16);
						s.full.paste(true);
						return d.s16.saveGame();
					}
				}.get();
				Floor floor = new Floor(all, key, data, text, sheet);
				
				map.put(key, floor);
				others.put(sp, floor);
			}

		}
		
		this.all = new ArrayList<>(all);
		if (all.size() > 0b111111) {
			throw new Errors.GameError("Too many floors have been declared. Maximum is " + 0b111111);
		}
		
		int r = 0;
		for (Floor fl : all) {
			if (fl != null && fl.isRoad)
				r++;
		}
		ArrayList<Floor> roads = new ArrayList<>(r);
		r = 0;
		for (Floor fl : all) {
			if (fl != null && fl.isRoad) {
				fl.indexroad = r++;
				roads.add(fl);
			}
		}
		this.roads = roads;
		
		
		extra = new RFloorExtra(gSprite);
		
		for (Floor f : all) {
			if (f == null)
				continue;
			IDebugPanelSett.add(key, new PlacableMulti(f.name(), f.desc, null) {
				
				@Override
				public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
					f.placeFixed(tx, ty);
					
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
					return f.isPlacable(tx, ty) ? null : "";
				}
				
			});
		}
		
		IDebugPanelSett.add(key, new PlacableMulti("clear roads and floor") {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				clearer.clear(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return IN_BOUNDS(tx, ty) && getter.is(tx, ty) ? null : "";
			}
		});
		
		IDebugPanelSett.add(key, new PlacableMulti("floor degrade") {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				degradeInc(tx+ty*SETT.TWIDTH, 1);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return IN_BOUNDS(tx, ty) && getter.is(tx, ty) ? null : "";
			}
		});


	}
	
	private final TileMap.Resource tResource = new TileMap.Resource() {
		
		@Override
		protected void clearAll() {
			types.clear();
		}

		@Override
		protected void save(FilePutter saveFile) {
			saveFile.bs(tiles);
			types.save(saveFile);
		}

		@Override
		protected void load(FileGetter saveFile) throws IOException {
			saveFile.bs(tiles);
			types.load(saveFile);
		}

		@Override
		protected void update(float ds) {
			
		}

		@Override
		protected COLOR miniColor(int tx, int ty) {
			return getter.is(tx, ty) ? getter.get(tx, ty).miniColor : null;
		}

		@Override
		protected COLOR miniColorPimped(ColorImp col, int tx, int ty, boolean n, boolean s) {
			if (n == s)
				return col;
			if (n)
				col.shadeSelf(1.2);
			else
				col.shadeSelf(0.8);
			return col;
		}
	};


	
	void render(Renderer r, float ds, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles();

		if (r.getZoomout() <= 1 && S.get().graphics.get()>0) {
			
			while (i.has()) {

				Floor f = getter.get(i.tile());
				
				if (f != null) {
					renderDetailed(i, f, tiles[i.tile()]);

				} else {
					//brightness.bind();
					GROUND().render(r, i.tile(), i.ran(), i.x(), i.y());
					GROUND().renderMinerals(r, i.tile(), i.ran(), i.x(), i.y());
				}
				ROOMS().renderAfterGround(r, shadowBatch, i);
				
				i.next();
			}
			
		}else {
			while (i.has()) {

				Floor f = getter.get(i.tile());
				
				if (f != null) {
					renderSimple(i, f, tiles[i.tile()]);

				} else {
					//brightness.bind();
					GROUND().render(r, i.tile(), i.ran(), i.x(), i.y());
					GROUND().renderMinerals(r, i.tile(), i.ran(), i.x(), i.y());
				}
				ROOMS().renderAfterGround(r, shadowBatch, i);
				
				i.next();
			}
		}


	}
	
	public int rotMask(RenderData.RenderIterator i) {
		return tiles[i.tile()] & 0x0F;
	}
	
	public void renderSimple(SPRITE_RENDERER ren, RenderData.RenderIterator i, Floor f){
		f.sheet.render(ren, 0, i.x(), i.y());
	}
	
	private void renderSimple(RenderData.RenderIterator i, Floor f, int t) {

		i.hiddenSet();
		TILE_SHEET sheet = f.sheet;
		
		Renderer r = CORE.renderer();
		
		int mask = t & 0x0F;
		int textureTile = i.ran() & 0x0F;
		
		int de = degrade(i.tx(), i.ty()); 
		int broken = (de>>2);
		int filth = CLAMP.i(de, 0, 8)-1;
		
		int ran = i.ran();
		
		if (broken > 0 || (mask != 0x0F)) {
			GROUND().render(r, i.tile(), i.ran(), i.x(), i.y());
		}
		
		//road texture
		f.tint().bind();
		
		int stencil = broken*RFloorExtra.eSet;
		if (mask == 0) {
			stencil += RFloorExtra.eSingles + (ran&0x0F);
			ran = ran >> 4;
		}else if (mask == 0x0F) {
			stencil += RFloorExtra.eFulls + (ran&0x0F);
			ran = ran >> 4;
		}else {
			stencil += mask + (ran & 3)*16;
			ran = ran >> 2;
		}
		
		//extra normal for surrounding ground. 	
		extra.edge.render(r, stencil, i.x(), i.y());
		
		extra.stencil.renderTextured(sheet.getTexture(textureTile), stencil, i.x(), i.y());
		
		COLOR.unbind();
		
		if (filth > 0) {
			OPACITY.O99.bind();
			filth *= 8;
			filth += (ran&0x07);
			ran = ran >> 3;
			extra.stencil.renderTextured(extra.filth.getTexture(filth), stencil, i.x(), i.y());
			OPACITY.unbind();
		}

	}
	
	private void renderDetailed(RenderData.RenderIterator i, Floor f, int t) {

		i.hiddenSet();
		TILE_SHEET sheet = f.sheet;
		
		Renderer r = CORE.renderer();
		
		int mask = t & 0x0F;
		int textureTile = i.ran() & 0x0F;
		
		int de = degrade(i.tx(), i.ty()); 
		int broken = (de>>2);
		int filth = CLAMP.i(de, 0, 8)-1;
		
		int ran = i.ran();
		
		
		
		
		int edge = 0;
		Room room = SETT.ROOMS().map.get(i.tile());
		if (room != null)
			edge = 0x0F;
		else {
			for (DIR d : DIR.NORTHO) {
				int m1 = d.next(-1).mask();
				int m2 = d.next(1).mask();
				if ((mask & m1) != 0 && ((mask & m2) != 0) && getter.get(i.tile()+d.x()+d.y()*TWIDTH) != null)
					edge |= d.mask();
			}
		}
		
		if (broken > 0 || (mask != 0x0F || edge != 0x0F))
			GROUND().render(r, i.tile(), i.ran(), i.x(), i.y());
		
		//road texture
		f.tint().bind();
		
		int stencil = broken*RFloorExtra.eSet;
		if (mask == 0) {
			stencil += RFloorExtra.eSingles + (ran&0x0F);
			ran = ran >> 4;
		}else if (mask == 0x0F) {
			stencil += RFloorExtra.eFulls + (ran&0x0F);
			ran = ran >> 4;
		}else {
			stencil += mask + (ran & 3)*16;
			ran = ran >> 2;
		}
		
		//extra normal for surrounding ground. 	
		extra.edge.render(r, stencil, i.x(), i.y());
		
		extra.stencilDetail.renderTextured(sheet.getTexture(textureTile), stencil, i.x(), i.y());
		
		COLOR.unbind();
		
		if (filth > 0) {
			OPACITY.O99.bind();
			filth *= 8;
			filth += (ran&0x07);
			ran = ran >> 3;
			extra.stencilDetail.renderTextured(extra.filth.getTexture(filth), stencil, i.x(), i.y());
			OPACITY.unbind();
		}
		
		if (edge != 0) {
			f.tint().bind();
			extra.stencil.renderTextured(sheet.getTexture(textureTile), RFloorExtra.eCorner+edge, i.x(), i.y());
			COLOR.unbind();
		}
		
		int mm = 0;
		
		for (DIR d : DIR.ORTHO) {
			Floor fl = getter.get(i.tx(), i.ty(), d);
			if ((mask & d.mask()) != 0) {
				if (fl == f)
					mm |= d.mask();
				if (room != null && room.blueprint() == SETT.ROOMS().DUMP)
					mm|= d.mask();
			}
				
			
		}
		
		
		if (mm != 0x0F)
			extra.stencil.renderTextured(extra.normalEdge.getTexture(mm), stencil, i.x(), i.y());

	}
	
	public void renderOntop(RenderData.RenderIterator i, Floor f, int mask) {

		i.hiddenSet();
		
		Renderer r = CORE.renderer();
		
		TILE_SHEET sheet = f.sheet;
		int tile = i.ran() & 0x0F;
		int de = (degrade(i.tx(), i.ty())>>1)&0b0111; 
		f.tint().bind();
		sheet.render(r, tile, i.x(), i.y());
		COLOR.unbind();
		
		if (de > 0) {
			OPACITY.O50.bind();
			extra.filth.render(r, (de-1)*8+(i.ran()&0x07), i.x(), i.y());
			OPACITY.unbind();
		}
		
		if (mask != 0x0FF) {
			extra.normalEdge.render(r, mask, i.x(), i.y());
		}

	}



	public class Floor implements INDEXED{

		private final PlayerColor tint;
		public final COLOR miniColor;
		public final CharSequence name;
		public final CharSequence desc;
		public final boolean isRoad;
		public final double walkValue;
		private final double[] envValues = new double[SETT.ENV().environment.all().size()];
		public final AVAILABILITY speed;
		public final double durability;
		public final TILE_SHEET sheet;
		public final RESOURCE resource;
		public final int resAmount;
		protected final int code;
		private final ICON.BIG icon;
		private int indexroad;
		public final String key;
		public final boolean isGrass;
		
		protected Floor(LISTE<Floor> all, String key, Json data, Json text, TILE_SHEET sheet) {
			code = all.add(this);
			this.sheet = sheet;
			this.key = key;
			
			
			isRoad = data.has("ROAD");
			isGrass = data.bool("IS_GRASS", false);
			if (isRoad) {
				Json road = data.json("ROAD");
				miniColor = new ColorImp(road, "MINIMAP_COLOR");
				name = text.text("NAME");
				desc = text.text("DESC");
				KEY_COLLECTION.fill(envValues, SETT.ENV().environment.rmap, road, 1.0);
				walkValue = road.d("ACCESS", 0, 1);
				speed = AVAILABILITY.ROADS[road.i("SPEED", 0, AVAILABILITY.ROADS.length-1)];
				durability = road.d("DURABILITY", 0, 1);
				resource = RESOURCES.map().tryGet(road);
				if (road.has("RESOURCE")) {
					resAmount = road.i("RESOURCE_AMOUNT");
				}else {
					
					resAmount = 0;
				}
			}else {
				miniColor = COLOR.BLUE50;
				name = DicMisc.¤¤floor + "#" + index();
				desc = "";
				walkValue = 0;
				durability = 1.0;
				resource = null;
				resAmount = 0;
				speed = AVAILABILITY.ROAD0;
			}
			this.icon = new ICON.BIG() {
				
				@Override
				public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					int w = (X2-X1)/2;
					int h = (Y2-Y1)/2;
					for (int y = 0; y < 2; y++) {
						for (int x = 0; x < 2; x++) {
							int i = y*2+x;
							extra.icon.render(r, i, X1+x*w, Y1+y*h);
							tint().bind();
							extra.icon.renderTextured(sheet.getTexture(i), i+4, X1+x*w, Y1+y*h);
							COLOR.unbind();
						}
					}
				}
			};
			
			tint = new PlayerColor(new ColorImp(data, "COLOR_MASK"), "FLOOR_"+key, DicMisc.¤¤floor, name);
			
		}

		public int indexRoad() {
			return indexroad;
		}
		
		public void placeFixed(int tx, int ty) {

			place(tx, ty);

			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (getter.is(tx, ty, d))
					getter.get(tx, ty, d).place(tx + d.x(), ty + d.y());
			}

		}

		private void place(int tx, int ty) {

			Floor old = getter.get(tx, ty);

			if (!IN_BOUNDS(tx, ty))
				return;

			int d = 0;

			if (TERRAIN().get(tx, ty) instanceof TBuilding.Ceiling || ROOMS().map.is(tx, ty))
				d = 0x0F;
			else {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR dir = DIR.ORTHO.get(i);
					TerrainTile t = TERRAIN().get(tx, ty, dir);
					if (t instanceof TBuilding.Wall) {
						TBuilding.Wall w = (Wall) t;
						if (w.getDia(tx+dir.x(), ty+dir.y()))
							d |= DIR.ORTHO.get(i).mask();
					}
					else if (getter.is(tx, ty, dir) || TERRAIN().get(tx, ty, dir) instanceof TFortification || SETT.ROOMS().map.is(tx, ty, dir)) {
						d |= DIR.ORTHO.get(i).mask();
					}
				}
			}
			

			


			if (old == this) {
				int deg = tiles[ty * C.SETTLE_TSIZE + tx] & 0xF0;
				d |= deg;
			}

			tiles[ty * C.SETTLE_TSIZE + tx] = (byte) d;
			types.set(ty * C.SETTLE_TSIZE + tx, code);
			if (old != this)
				tResource.updateMiniMap(tx, ty);
			GRASS().current.set(tx, ty, 0);
			PATH().availability.updateAvailability(tx, ty);
		}

		public CharSequence name() {
			return name;
		}

		public boolean isPlacable(int x, int y) {
			return true;
		}

		public ICON.BIG getIcon() {
			return icon;
		}

		@Override
		public int index() {
			return code;
		}
		
		public double envValue(SettEnv e) {
			return envValues[e.index()];
		}
		
		public COLOR tint() {
			if (isGrass)
				return SETT.GRASS().color(0);
			return tint.color;
		}

	}

	public AVAILABILITY getAvailability(int tx, int ty) {
		return getter.is(tx, ty) ? getter.get(tx, ty).speed : null;
	}
	
	void updateStructure(int tx, int ty) {
		Floor f = getter.get(tx, ty);
		if (f != null)
			f.place(tx, ty);
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			f = getter.get(tx, ty, DIR.ORTHO.get(i));
			if (f != null)
				f.place(tx+DIR.ORTHO.get(i).x(), ty+DIR.ORTHO.get(i).y());
		}
		
		
	}

	public final MAP_OBJECT<Floor> getter = new MAP_OBJECT<Floors.Floor>() {
		
		@Override
		public Floor get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return null;
		}
		
		@Override
		public Floor get(int tile) {
			return all.get(types.get(tile));
		}
	};
	
	public final MAP_DOUBLEE degrade = new MAP_DOUBLEE() {
		
		private final double i = 1/7.0;
		
		@Override
		public double get(int tx, int ty) {
			return get(tx+ty*TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			return (bDegradeO.get(tiles[tile])>>1)*i;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			set(tx+ty*TWIDTH, value);
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			value = CLAMP.d(value, 0, 1);
			tiles[tile] = (byte) bDegradeO.set(tiles[tile], ((int) (value*0x07)*2)<<1);
			return this;
		}
	};
	
	public int degrade(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null)
			return (int) (0x0F*r.getDegrade(tx, ty));
		return bDegradeO.get(tiles[tx+ty*SETT.TWIDTH]);
	}
	
	public void degradeInc(int tile, int am) {
		tiles[tile] = (byte) (bDegradeO.inc(tiles[tile], am));
			
	}
	
	
	
	public final MAP_CLEARER clearer = new MAP_CLEARER() {
		
		@Override
		public MAP_CLEARER clear(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				clear(tx+ty*TWIDTH);
			return this;
		}
		
		@Override
		public MAP_CLEARER clear(int tile) {
			if (getter.is(tile)) {
				types.set(tile, NOTHING);
				tResource.updateMiniMap(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
				int tx = tile%SETT.TWIDTH;
				int ty = tile/SETT.TWIDTH;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					Floor f = getter.get(tx, ty, d);
					if (f != null) {
						f.place(tx+d.x(), ty+d.y());
					}
				}
				
			}
			
			return this;
		}
	};

	@Override
	public Floor getAt(int index) {
		return all.get(index);
	}

	@Override
	public LIST<Floor> all() {
		return all;
	}

	private static final class RFloorExtra{

		public final static int eSingles = 4*16;
		public final static int eFulls = 5*16;
		public final static int eSet = 6*16;
		
		public final static int eCorner = eSet*4;
		
//		public final static int eEdgeStencil = eDegSet*4;
//		public final static int eEdgeNormal = eDegSet*4+16;
//		public final static int eEdge2 = eEdgeNormal + 16;
		
		public final TILE_SHEET stencil;
		public final TILE_SHEET stencilDetail;
		public final TILE_SHEET edge;
		public final TILE_SHEET filth;
		public final TILE_SHEET normalEdge;
		public final TILE_SHEET icon;
		
		RFloorExtra(PATH g) throws IOException{
			stencil = new ITileSheet(g.get("_FloorExtra"), 1152, 50) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 0);
				}
			}.get();
			stencilDetail = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 128);
				}
			}.get();
			edge = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 256);
				}
			}.get();
			
			normalEdge = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, 256+128, 1, 1, d.s16);
					s.house.paste(true);
					return d.s16.saveGame();
				}
			}.get();
			filth = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.house.body().x2(), s.house.body().y1(), 1, 1, 8, 8, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}
			}.get();
			icon = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), s.full.body().y1(), 2, 1, 2, 2, d.s16);
					s.full.paste(true);
					s.full.setVar(1).paste(true);
					return d.s16.saveGui();
				}
			}.get();
		}
		
		private TILE_SHEET stencil(ComposerUtil c, ComposerSources s, ComposerDests d, int y2) {
			int x1 = 0;
			for (int x = 0; x < 4; x++) {
				int y1 = y2;
				for (int y = 0; y < 1; y++) {
					s.house.init(x1, y1, 2, 1, d.s16);
					s.house.setVar(0).paste(1, true);
					s.house.setVar(1).paste(1, true);
					
					s.full.init(x1, s.house.body().y2(), 1, 1, 8, 1, d.s16);
					s.full.paste(1, true);
					s.full.init(x1, s.full.body().y2(), 1, 1, 8, 1, d.s16);
					s.full.paste(1, true);
					y1 = s.full.body().y2();
				}
				
				x1 = s.house.body().x2();
			}
			
			s.house.init(0, y2, 1, 1, d.s16);
			s.house.setVar(0).pasteEdges(true);
//			s.full.init(0, s.house.body().y2(), 1, 1, 8, 1, d.s16);
//			s.full.init(0, s.full.body().y2(), 1, 1, 8, 1, d.s16);
//			s.house.init(0, s.full.body().y2(), 1, 1, d.s16);
//			s.house.setVar(0).pasteEdges(true);
			
			
			return d.s16.saveGame();
		}
		
	}

}
