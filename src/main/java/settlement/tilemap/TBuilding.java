package settlement.tilemap;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import game.faction.player.PlayerColors.PlayerColor;
import init.C;
import init.RES;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sound.SoundSettlement;
import init.sound.SoundSettlement.Sound;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.job.Job;
import settlement.main.RenderData;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.Terrain.TerrainTile;
import settlement.tilemap.TerrainDiagonal.Diagonalizer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.dic.DicMisc;
import util.info.INFO;
import util.keymap.RCollection;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.sett.IDebugPanelSett;
import view.tool.*;

public final class TBuilding extends INFO implements INDEXED {

	private static final String KEY = "STRUCTURE";
	public final String key;
	public final CharSequence nameWall;
	public final CharSequence nameCeiling;
	private final SPRITE iconWall;
	private final SPRITE iconCeiling;
	public final SPRITE iconCombo;
	private final TILE_SHEET spriteWall;
	private final TILE_SHEET spriteOpening;
	private final TILE_SHEET spriteCeiling;
	public final double durability;
	public final RESOURCE resource;
	public final int resAmount;
	public final PlayerColor tint;
	public final COLOR miniColor;
	private final int index;

	public final Wall wall;
	final Wall broken;
	public final Ceiling roof;
	public final Sound sound;

	static class Collection extends RCollection<TBuilding> {

		private final LIST<TBuilding> all;

		Collection(Terrain terrain) throws IOException {
			super(KEY);
			String f = key.toLowerCase(Locale.ROOT);
			PATH data = PATHS.INIT_SETTLEMENT().getFolder(f);
			PATH sprite = PATHS.SPRITE_SETTLEMENT().getFolder(f);
			PATH text = PATHS.TEXT_SETTLEMENT().getFolder(f);

			String[] keys = data.getFiles();
			ArrayList<TBuilding> all = new ArrayList<>(keys.length);
			HashMap<String, TBuilding> others = new HashMap<>();
			for (String key : keys) {
				Json d = new Json(data.get(key));
				Json t = new Json(text.get(key));
				map.put(key, new TBuilding(key, all, terrain, d, t, sprite, others));
			}
			this.all = all;
		}

		@Override
		public LIST<TBuilding> all() {
			return all;
		}

		@Override
		public TBuilding getAt(int index) {
			return all.get(index);
		}
	}

	TBuilding(String key, LISTE<TBuilding> all, Terrain t, Json data, Json text, PATH sg,
			HashMap<String, TBuilding> otherSprites) throws IOException {
		super(text);
		this.key = key;
		nameWall = text.text("NAME_WALL");
		nameCeiling = text.text("NAME_CEILING");
		String s = data.value("SPRITE");
		sound = RES.sound().settlement.action.get(data);
		if (otherSprites.containsKey(s)) {
			TBuilding o = otherSprites.get(s);
			iconWall = o.iconWall;
			iconCeiling = o.iconCeiling;
			iconCombo = o.iconCombo;
			spriteOpening = o.spriteOpening;
			spriteCeiling = o.spriteCeiling;
			spriteWall = o.spriteWall;
		} else {

			spriteWall = new ITileSheet(sg.get(s), 576, 372) {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.house.init(0, 0, 4, 2, t);
					
					s.house.setVar(0).paste(1, true);
					s.house.setVar(1).pasteRotated(2, true);
					s.house.setVar(1).pasteRotated(3, true);
					
					//dia
					s.house.setVar(2).paste(1, true);
					s.house.setVar(3).pasteRotated(2, true);
					s.house.setVar(3).pasteRotated(3, true);
					
					
					
					//broken
					s.house.setVar(4).paste(1, true);
					s.house.setVar(5).pasteRotated(2, true);
					s.house.setVar(5).pasteRotated(3, true);
					
					s.house.setVar(0).pasteEdges(true);
					s.house.setVar(2).pasteEdges(true);

					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, t);
					s.full.paste(true);

					s.full.init(0, s.full.body().y2(), 1, 1, 16, 1, t);
					s.full.paste(true);

					return t.saveGame();
				}
			}.get();

			spriteOpening = (new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

					ComposerDests.Tile t = d.s16;
					s.house.init(0, s.full.body().y2(), 4, 1, t);
					s.house.setVar(0).paste(true);
					s.house.setVar(1).paste(true);
					s.house.setVar(2).paste(true);
					s.house.setVar(3).paste(true);
					
					s.house.setVar(0).pasteEdges(true);
					s.house.setVar(2).pasteEdges(true);

					s.full.init(0, s.house.body().y2(), 1, 1, 8, 1, t);
					s.full.setSkip(8, 0).paste(2, true);

					return t.saveGame();
				}

			}).get();

			spriteCeiling = (new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.house.init(0, s.full.body().y2(), 2, 1, t);

					for (int i = 0; i < 2; i++) {
						s.house.setVar(i);
						s.house.setSkip(0, 16).paste(1, true);
					}
					s.house.setVar(0);
					s.house.setSkip(0, 1).pasteEdges(true);
					s.house.setVar(1).setSkip(0, 1).paste(true);
					return t.saveGame();
				}
			}).get();
			otherSprites.put(s, this);
			
			iconCombo = new SPRITE.Imp(ICON.BIG.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteOpening.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteOpening.render(r, DIR.S.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteOpening.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteOpening.render(r, DIR.N.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
			
			iconWall = new SPRITE.Imp(ICON.BIG.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteWall.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteWall.render(r, DIR.S.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteWall.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteWall.render(r, DIR.N.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
			
			iconCeiling = new SPRITE.Imp(ICON.BIG.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteCeiling.render(r, DIR.N.mask() | DIR.W.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteCeiling.render(r, DIR.N.mask() | DIR.E.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteCeiling.render(r, DIR.S.mask() | DIR.W.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteCeiling.render(r, DIR.S.mask() | DIR.E.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
		}

		durability = data.d("DURABILITY", 0, 1.0)*C.TILE_SIZE;
		resource = RESOURCES.map().get(data);
		resAmount = data.i("RESOURCE_AMOUNT", 0, 16);
		tint = new PlayerColor(new ColorImp(data), "BUILDING_" + key, DicMisc.¤¤Structures, name);
		miniColor = new ColorImp(data, "MINIMAP_COLOR");
		
		index = all.add(this);

		wall = new WallFull(t);
		broken = new WallBroken(t);
		roof = new Ceiling(t);

		PLACABLE room = new PlacableMulti(name) {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				if (tx > a.body().x1() && tx < a.body().x2() - 1 && ty > a.body().y1() && ty < a.body().y2() - 1) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (Math.abs(tx - a.body().cX()) < 2) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (Math.abs(ty - a.body().cY()) < 2) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (wall.isPlacable(tx, ty)) {
					wall.placeFixed(tx, ty);
					GRASS().current.set(tx, ty, 0);
				}
			}

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return !roof.is(tx, ty) && wall.isPlacable(tx, ty) ? null : "";
			}

		};

		IDebugPanelSett.add("room", room);
	}

	public final MAP_BOOLEAN isser = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			return wall.is(tx, ty) || roof.is(tx, ty);
		}

		@Override
		public boolean is(int tile) {
			return wall.is(tile) || roof.is(tile);
		}
	};

	public abstract class BuildingComponent extends Terrain.TerrainTile {

		private final TerrainClearing clearing;

		protected BuildingComponent(Terrain t, CharSequence name, SPRITE icon, COLOR c, RESOURCE needed) {
			super(t, name, icon, c);

			clearing = new TerrainClearing() {

				
				@Override
				public RESOURCE resource() {
					return needed;
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
					shared.NADA.placeFixed(tx, ty);
					return 1;
				}

				@Override
				public SoundSettlement.Sound sound() {
					return sound;
				}

				@Override
				public boolean isStructure() {
					return true;
				}
				
				@Override
				public boolean canDestroy(int tx, int ty) {
					return false;
				}
				
				@Override
				public void destroy(int tx, int ty) {
					
				}
				

			};
		}

		// protected final int getDegrade(int data) {
		// data = data >> 14;
		// return data & 0b0000000000000011;
		// }
		//
		// private final int setDegrade(int data, int down) {
		// down = down << 14;
		// data &= 0b0011111111111111;
		// data |= down;
		// return data;
		// }
		//
		// public void degrade(int tx, int ty, int amount) {
		// int data = shared.data.get(tx, ty);
		// int down = getDegrade(data);
		// down += amount;
		// if (down <= downgrades && down >= 0)
		// data = setDegrade(data, down);
		// shared.data.set(tx, ty, data);
		// }
		//
		// public boolean canDegrade(int tx, int ty, int amount) {
		// int data = shared.data.get(tx, ty);
		// int down = getDegrade(data);
		// down += amount;
		// return down <= downgrades && down >= 0;
		// }

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		public final TBuilding building() {
			return TBuilding.this;
		}

	}

	public class Wall extends BuildingComponent implements Diagonalizer{

		private final static int SET = 16;
		private final TILE_SHEET sheet;
		private final int DIAGONAL = 4*SET;
		private final int BROKEN = DIAGONAL + 4*SET;
		private final int CORNERS = BROKEN + 4*SET;
		
		private final int SINGLES = CORNERS+2*SET;
		private final int FULLS = SINGLES + SET;
		private final boolean broken;
		private int DIA = 0x02000;

		private Wall(Terrain t, boolean broken) {
			super(t, nameWall, iconWall, miniColor.shade(0.9), resource);
			this.broken = broken;
			this.sheet = spriteWall;
		}

		@Override
		protected boolean place(int x, int y) {

			boolean dia = shared.get(x, y) instanceof Diagonalizer && ((Diagonalizer) shared.get(x, y)).getDia(x, y);
			placeRaw(x, y);
			
			int res = 0;
			for (DIR d : DIR.ORTHO) {
				if (joins(x, y, d))
					res |= d.mask();
			}
			
			int cor = 0;
			for (DIR d : DIR.NORTHO) {
				if (!joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(-1)))
					cor |= d.mask();
			}
			res |= cor << 4;

			if (res != 0 && IN_BOUNDS(x, y, DIR.N)) {
				TerrainTile t = shared.get(x, y, DIR.N);
				if (t != this && t != roof.opening && t.wallIsWally() && !t.roofIs() && ((res & DIR.N.mask()) != 0)) {
					res |= 0b0_0001_0000_0000;
				}
			}
			if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
				TerrainTile t = shared.get(x, y, DIR.W);
				if (t != this && t != roof.opening && t.wallIsWally() && !t.roofIs() && ((res & DIR.W.mask()) != 0)) {
					res |= 0b0_0010_0000_0000;
				}
			}
//			if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
//				TerrainTile t = shared.get(x, y, DIR.W);
//				if (t != this && t != roof.opening && t.wallIsWally() && !t.roofIs()) {
//					if ((res & DIR.SW.mask()) != 0)
//						res |= 0b0_1000_0000_0000;
//					if ((res & DIR.NW.mask()) != 0)
//						res |= 0b0_0100_0000_0000;
//				}
//			}


			shared.data.set(x, y, res);
			setDia(x, y, dia);
			return false;
		}

		private boolean joins(int x, int y, DIR d) {
			x += d.x();
			y += d.y();
			if (!IN_BOUNDS(x, y))
				return true;
			return jwall.is(x, y);
		}

		@Override
		public boolean isMassiveWall() {
			return true;
		}
		
		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			int code = data & 0x0F;
			int cor = (data >> 4) & 0x0F;
			
			tint.color.bind();
			if (code == 0x0F) {
				int c = FULLS + (i.ran() & 0x07);
				if (broken) {
					c += SET/2;
				}
				sheet.render(r, c, i.x(), i.y());
				if (cor != 0) {
					s.setHeight(3).setDistance2Ground(8);
					sheet.render(s, c, i.x(), i.y());
					sheet.render(r, CORNERS+((data & DIA) != 0 ? SET : 0) + cor, i.x(), i.y());
				}
				COLOR.unbind();
				renderEdges(r, s, i, data);
				return !broken;
			}

			if (code == 0) {
				s.setHeight(3).setDistance2Ground(8);
				
				int c = (data & DIA) != 0 ? 4:0;
				c += (i.ran() & 0x03);
				c += SINGLES;
				if (broken)
					c += SET/2;
				sheet.render(r, c, i.x(), i.y());
				sheet.render(s, c, i.x(), i.y());
			} else {
				
				int c = code + (i.ran() & 0b011) * SET;				
				
				if (broken) {
					c += 8*SET;
					s.setHeight(0).setDistance2Ground(8);
					sheet.render(s, c, i.x(), i.y());
				}else if((data & DIA) != 0) {
					c += 4*SET;
				}
				s.setHeight(12).setDistance2Ground(0);
				sheet.render(r, c, i.x(), i.y());
				sheet.render(s, c, i.x(), i.y());

				if (cor != 0) {
					sheet.render(r, CORNERS+((data & DIA) != 0 ? SET : 0) + cor, i.x(), i.y());
				}
				
				
				renderEdges(r, s, i, data);
			}
			COLOR.unbind();
			return false;
		}
		
		private void renderEdges(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int cor = (data >> 4) & 0x0F;
			if ((data & 0b0_0001_0000_0000) != 0) {
				if ((data & DIR.W.mask()) == 0 || (cor & DIR.NW.mask()) != 0)
					SPRITES.sett().map.wall_merge.render(r, 0, i.x(), i.y() - 4);
				else
					SPRITES.sett().map.wall_merge.render(r, 2, i.x(), i.y() - 4);
				if ((data & DIR.E.mask()) == 0 || (cor & DIR.NE.mask()) != 0)
					SPRITES.sett().map.wall_merge.render(r, 1, i.x(), i.y() - 4);
				else
					SPRITES.sett().map.wall_merge.render(r, 3, i.x(), i.y() - 4);
			}
			if ((data & 0b0_0010_0000_0000) != 0) {
				if ((data & DIR.N.mask()) == 0 || (cor & DIR.NW.mask()) != 0)
					SPRITES.sett().map.wall_merge.render(r, 4+1, i.x()-4, i.y());
				else
					SPRITES.sett().map.wall_merge.render(r, 4+3, i.x()-4, i.y());
				if ((data & DIR.S.mask()) == 0 || (cor & DIR.SW.mask()) != 0)
					SPRITES.sett().map.wall_merge.render(r, 4+0, i.x()-4, i.y());
				else
					SPRITES.sett().map.wall_merge.render(r, 4+2, i.x()-4, i.y());
			}
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return broken ? null : AVAILABILITY.SOLID;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		boolean wallJoiner() {
			return true;
		}

		@Override
		public boolean wallIsWally() {
			return true;
		}

		@Override
		public int miniDepth() {
			return 2;
		}

		@Override
		public void setDia(int x, int y, boolean dia) {
			if (!is(x, y))
				return;
			int data = shared.data.get(x, y);
			if (dia)
				data |= DIA;
			else
				data &= ~DIA;
			shared.data.set(x,  y, data);
		}
		
		@Override
		public boolean getDia(int tx, int ty) {
			if (!is(tx, ty))
				return false;
			return (shared.data.get(tx, ty) & DIA) != 0;
		}

		@Override
		public int heightStart(int tx, int ty) {
			return 0;
		}

		@Override
		public int heightEnd(int tx, int ty) {
			return 3;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.SOLID;
		}

		public boolean isFull(int tx, int ty) {
			int data = shared.data.get(tx, ty);
			return (data & 0x0F) == 0x0F && ((data >> 4) & 0x0F) == 0;
		}
		
		@Override
		public boolean coversCompletely(int tx, int ty) {
			return (shared.data.get(tx, ty) & 0x0F) == 0x0F;
		}


	}
	
	class WallFull extends Wall{

		final TerrainClearing clearing = new TerrainClearing() {

			
			@Override
			public RESOURCE resource() {
				return resource;
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
				shared.NADA.placeFixed(tx, ty);
				return 1;
			}

			@Override
			public SoundSettlement.Sound sound() {
				return sound;
			}

			@Override
			public boolean isStructure() {
				return true;
			}
			
			@Override
			public void destroy(int tx, int ty) {
				broken.placeFixed(tx, ty);
			}

			@Override
			public double strength() {
				return durability*1000;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return true;
			}

		};

		private WallFull(Terrain t) {
			super(t, false);
		}
		
		@Override
		public TerrainClearing clearing() {
			return clearing;
		}
		

	}
	
	class WallBroken extends Wall implements TILE_FIXABLE{


		final TerrainClearing clearing = new TerrainClearing() {

			
			@Override
			public RESOURCE resource() {
				return resource;
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
				shared.NADA.placeFixed(tx, ty);
				return 1;
			}

			@Override
			public SoundSettlement.Sound sound() {
				return sound;
			}

			@Override
			public boolean isStructure() {
				return false;
			}
			
			@Override
			public double strength() {
				return 0;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return false;
			}

		};

		private WallBroken(Terrain t) {
			super(t, true);
		}
		
		@Override
		public Job fixJob(int tx, int ty) {
			return SETT.JOBS().build_structure.get(index).wall;
		}
		
		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public TerrainTile getTerrain(int tx, int ty) {
			return wall;
		}
		

	}
	
	public class Ceiling extends BuildingComponent {

		private final static int SET = 16;
		private final TILE_SHEET sheet;
		private final int SHEET_CORNER;
		private final int SHEET_SHADOW;
		private final Opening opening;

		private Ceiling(Terrain t) {
			super(t, nameCeiling, iconCeiling, miniColor, resource);
			this.sheet = spriteCeiling;
			this.SHEET_CORNER = SET * 4;
			this.SHEET_SHADOW = SHEET_CORNER + SET;
			opening = new Opening(t);
		}

		@Override
		protected boolean place(int x, int y) {

			if (opening.isPlacable(x, y)) {
				return opening.place(x, y);
			}

			super.placeRaw(x, y);
			int data = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (joins(x, y, d)) {
					data |= d.mask();
				}
			}

			data = setCorners(x, y, data);
			data = shadowSet(x, y, data);

			shared.data.set(x, y, data);
			return false;

		}

		private int setCorners(int x, int y, int res) {
			int corner = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if (joins(x, y, d) && !joins(x, y, d.next(-1)) && !joins(x, y, d.next(1))) {
					corner |= d.mask();
				}
			}
			res |= (corner << 4);
			return res;
		}

		private int shadowSet(int x, int y, int res) {
			int s = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				if (jwall.is(x, y, DIR.ORTHO.get(i)))
					s |= DIR.ORTHO.get(i).mask();
			}

			res |= (s << 8);

			return res;
		}

		private int shadowGet(int data) {
			return (data >> 8) & 0x0F;
		}

		private int getCorners(int data) {
			return (data >> 4) & 0x0F;
		}
		
		private int getData(int data, RenderData.RenderIterator i) {
			int res = data & 0xF;
			Room r = SETT.ROOMS().map.get(i.tx(), i.ty());
			if (r != null && r.constructor() != null && r.constructor().mustBeIndoors()) {
				for (DIR d : DIR.ORTHO) {
					if (!r.isSame(i.tx(), i.ty(), i.tx()+d.x(), i.ty()+d.y())) {
						if (!(TERRAIN().get(i.tx()+d.x(), i.ty()+d.y()) instanceof Opening))
							res |= d.mask();
					}
				}
			}else {
				for (DIR d : DIR.ORTHO) {
					r = SETT.ROOMS().map.get(i.tx(), i.ty(), d);
					if (r != null && r.constructor() != null && r.constructor().mustBeIndoors())
						res |= d.mask();
				}
			}
			
			return res;
		}

		public void renderEdge(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int mask) {
			if (mask != 0) {
				int j = mask + (i.ran() & 0b011) * SET;
				sheet.render(r, j, i.x(), i.y());
			}
		}
		
		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			int a = getData(data, i);
			if (a != 0) {
				int j = a + (i.ran() & 0b011) * SET;
				sheet.render(r, j, i.x(), i.y());
			}

			a = getCorners(data);
			if (a != 0) {
				sheet.render(r, SHEET_CORNER + a, i.x(), i.y());
			}

			a = shadowGet(data);

			// s.setHard();
			s.setDistance2Ground(0).setHeight(2);
			sheet.render(s, SHEET_SHADOW, i.x(), i.y());
			// s.setSoft();

			// if (a != 0x0F) {
			// s.setDistance2Ground(2).setHeight(8);
			// sheet.render(s, SHEET_SHADOW + a, i.x(), i.y());
			//
			// }

			return false;
		}

		@Override
		protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return null;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			// for (DIR d: DIR.ALL) {
			// if (!joins(tx, ty, d) && !is(tx, ty, d))
			// return false;
			// }
			return true;
		}

		private boolean joins(int x, int y, DIR d) {
			return shared.get(x, y, d).wallIsWally() && !(shared.get(x,y,d) instanceof Opening);
		}

		@Override
		public boolean is(int tx, int ty) {
			return super.is(tx, ty) || opening.is(tx, ty);
		}

		@Override
		boolean wallJoiner() {
			return true;
		}

		@Override
		public boolean wallIsWally() {
			return false;
		}

		@Override
		public boolean roofIs() {
			return true;
		}

		public class Opening extends BuildingComponent implements Diagonalizer{

			private final static int SET = 16;
			private final TILE_SHEET sheet;
			private final TILE_SHEET shadow;
			private final int CORNERS = SET*4;
			private final int SINGLES = CORNERS + 2*SET;
			private final int DIA = 0x01000;

			private Opening(Terrain t) {
				super(t, nameCeiling, iconCeiling, miniColor, resource);
				this.sheet = spriteOpening;
				shadow = spriteWall;
			}

			@Override
			protected boolean place(int x, int y) {
				if (!isPlacable(x, y))
					return Ceiling.this.place(x, y);

				
				boolean dia = shared.get(x, y) instanceof Diagonalizer && ((Diagonalizer) shared.get(x, y)).getDia(x, y);
				placeRaw(x, y);
				
				int res = 0;
				for (DIR d : DIR.ORTHO) {
					if (joins(x, y, d))
						res |= d.mask();
				}
				
				int cor = 0;
				for (DIR d : DIR.NORTHO) {
					if (!joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(-1)))
						cor |= d.mask();
				}
				res |= cor << 8;

				if (res != 0 && IN_BOUNDS(x, y, DIR.N)) {
					TerrainTile t = shared.get(x, y, DIR.N);
					if (t != this && t != wall && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b000100000;
						if ((res & DIR.NE.mask()) != 0)
							res |= 0b000010000;
					}
				}
				if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
					TerrainTile t = shared.get(x, y, DIR.W);
					if (t != this && t != wall && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.SW.mask()) != 0)
							res |= 0b00010000000;
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b00001000000;
					}
				}

				shared.data.set(x, y, res);
				setDia(x, y, dia);
				return false;
			}

			@Override
			protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
				// s.setHard();
				// s.setHeight(0).setDistance2Ground(0);
				int tile = data & 0x0F;
				int cor = (data >> 8) & 0x0F;
				if (tile == 0) {
					// shadow.render(s, wall.SINGLES, i.x(), i.y());
					// s.setSoft();
					sheet.render(r, SINGLES + (i.ran() & 0x0F), i.x(), i.y());
					s.setHeight(3).setDistance2Ground(0);
					shadow.render(s, wall.SINGLES, i.x(), i.y());
				} else {
					// shadow.render(s, data, i.x(), i.y());
					// s.setSoft();
					int j = tile + (i.ran() & 0b01) * SET;
					if (tile != 0x0F && (data & DIA) != 0)
						j += 2*SET;
					sheet.render(r, j, i.x(), i.y());
					s.setHeight(12).setDistance2Ground(0);
					if (tile == 0x0F)
						shadow.render(s, wall.FULLS, i.x(), i.y());
					else
						shadow.render(s, tile, i.x(), i.y());
					
					if (cor != 0) {
						sheet.render(r, CORNERS+((data & DIA) != 0 ? SET : 0) + cor, i.x(), i.y());
					}
					
//					if ((data & 0b000110000) != 0)
//						SPRITES.sett().map.wall_merge.render(r, ((data >> 4) & 0b011) - 1, i.x(), i.y() - 4);
//					if ((data & 0b011000000) != 0)
//						SPRITES.sett().map.wall_merge.render(r, (data >> 6) + 2, i.x() - 4, i.y());
				}
				return false;

			}

			private boolean joins(int tx, int ty, DIR d) {
				return jwall.is(tx, ty, d);
			}

			@Override
			protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i,
					int data) {
				return false;
			}

			@Override
			public AVAILABILITY getAvailability(int x, int y) {
				return null;
			}

			@Override
			public boolean isPlacable(int tx, int ty) {

				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					if (! jwall.is(tx, ty, d)) {
						return true;
					}
				}

				return false;
			}

			@Override
			boolean wallJoiner() {
				return true;
			}

			@Override
			public boolean wallIsWally() {
				return true;
			}

			@Override
			public boolean roofIs() {
				return true;
			}
			
			@Override
			public void setDia(int x, int y, boolean dia) {
				if (!is(x, y))
					return;
				int data = shared.data.get(x, y);
				if (dia)
					data |= DIA;
				else
					data &= ~DIA;
				shared.data.set(x,  y, data);
			}
			
			@Override
			public boolean getDia(int tx, int ty) {
				if (!is(tx, ty))
					return false;
				return (shared.data.get(tx, ty) & DIA) != 0;
			}

			@Override
			public int heightStart(int tx, int ty) {
				return 3;
			}

			@Override
			public int heightEnd(int tx, int ty) {
				return 3;
			}
			
			@Override
			public LOS los(int tx, int ty) {
				return LOS.CEILING;
			}

		}

		@Override
		public int heightStart(int tx, int ty) {
			return 3;
		}

		@Override
		public int heightEnd(int tx, int ty) {
			return 4;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.CEILING;
		}

	}

	@Override
	public int index() {
		return index;
	}
	
	private final static MAP_BOOLEAN jwall = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (TERRAIN().get(tx, ty).wallJoiner())
				return true;
			Room r = SETT.ROOMS().map.get(tx, ty);
			return r != null && r.wallJoiner();
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%TWIDTH, tile/TWIDTH);
		}
	};

}
