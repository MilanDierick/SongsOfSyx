package settlement.room.main.furnisher;

import static settlement.main.SETT.*;

import java.io.IOException;
import java.nio.file.Path;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.environment.SettEnvMap.SettEnvValue;
import settlement.main.RenderData.RenderIterator;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.*;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomInitData;
import settlement.room.sprite.RoomSprite;
import settlement.tilemap.Floors.Floor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.info.INFO;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.*;

public abstract class Furnisher{
	
	public final static int MAX_RESOURCES = 4;
	
	final ArrayList<FurnisherItemTile> tiles = new ArrayList<>(255);
	final ArrayList<FurnisherItem> allItems = new ArrayList<>(255);
	final ArrayList<FurnisherItemGroup> groups;
	final ArrayList<FurnisherStat> stats;
	private final LIST<RESOURCE> resources;
	private final double[] areaCost;
	protected final LIST<Floor> floor;
	protected ICON.BIG icon;
	public final TILE_SHEET sheet;

	public final COLOR miniColor;
	private final FurnisherMinimapColor colorPimp;
	
	protected static Json[] jsonGroupText;
	protected static Json[] jsonGroupData;
	protected static Json[] jsonStat;
	
	private final double[] envValue = new double[SETT.ENV().environment.all().size()];
	private final double[] envRadius = new double[SETT.ENV().environment.all().size()];
	

	
	protected Furnisher(RoomInitData init, int items, int stats, int sheetWidth, int sheetHeight) throws IOException {
		this(init.data(), init.text(), init.sp(), items, stats, sheetWidth, sheetHeight);
	}
	
	protected Furnisher(RoomInitData init, int items, int stats) throws IOException {
		this(init.data(), init.text(), null, items, stats, 0, 0);
	}
	
	private Furnisher(Json data, Json text, Path sp, int items, int stats, int sheetWidth, int sheetHeight) throws IOException {
		
		tiles.add((FurnisherItemTile)null);
		allItems.add((FurnisherItem)null);
		if (FurnisherItem.itemsTmp.size() != 0)
			throw new RuntimeException("someone forgot to flush...");
		
		resources = RESOURCES.map().getMany(data);
		if (resources.size() > MAX_RESOURCES)
			data.error("Too many resources declared. Max is 4", "RESOURCES");
		areaCost = data.ds("AREA_COSTS", resources.size());
		if (data.has(SETT.FLOOR().key)) {
			if (data.arrayIs(SETT.FLOOR().key)) {
				floor = SETT.FLOOR().getManyByKeyWarn(SETT.FLOOR().key, data);
			}else {
				Floor f = SETT.FLOOR().tryGet(data);
				if (f == null)
					data.error("no floor named: ", SETT.FLOOR().key);
				floor = new ArrayList<>(f);
			}
			
		}else {
			floor = null;
		}
		
		jsonStat = null;
		if (stats > 0) {
			jsonStat = text.jsons("STATS", stats);
			if (stats != jsonStat.length)
				text.error("Invalid amount of stats declared. Should be " + stats + " not " + jsonStat.length, "STATS");
		}
		this.stats = new ArrayList<>(stats);
		
		jsonGroupText = null;
		jsonGroupData = null;
		if (items > 0) {
			jsonGroupData = data.jsons("ITEMS", items);
			jsonGroupText = text.jsons("ITEMS", items);
			if (items != jsonGroupData.length)
				data.error("Invalid amount of items declared. Should be " + items + " not " + jsonGroupData.length, "ITEMS");
		}
		if (items == 0)
			items = 1;
		this.groups = new ArrayList<>(items);
		
		

		
		miniColor = new ColorImp(data, "MINI_COLOR");
		colorPimp = new FurnisherMinimapColor(data);
		
		if (data.has("ENVIRONMENT_EMIT")) {
			Json j = data.json("ENVIRONMENT_EMIT");
			for (String k : j.keys()) {
				SettEnv e = SETT.ENV().environment.rmap.getByKeyWarn(k, j);
				if (e != null) {
					Json jj = j.json(k);
					envValue[e.index()] = jj.d("VALUE", 0, 1);
					envRadius[e.index()] = jj.d("RADIUS", 0, 1);
				}
			}
		}
		
		if (sp != null) {
		
			icon = IIcon.LARGE.get(new ISpriteData(sp, sheetWidth, sheetHeight) {
				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 2, 1, 1, 1, d.s32);
					s.singles.setVar(0).paste(true);
					return d.s32.saveSprite();
				}
			}.get());
			
			sheet = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d)  {
					return sheet(c,s,d,s.singles.body().y2());
				}
			}.get();
		}else {
			icon = SPRITES.icons().l.dop_realms;
			sheet = null;
		}
	}
	
	public boolean envValue(SettEnv e, SettEnvValue v, int tx, int ty) {
		if (envRadius[e.index()] != 0) {
			v.radius = envRadius[e.index()];
			v.value = envValue[e.index()];
			return true;
		}
		return false;
	}
	
	public boolean envValue(SettEnv e) {
		if (envRadius[e.index()] != 0) {
			return true;
		}
		return false;
	}
	
	public final int resources() {
		return resources.size();
	}
	
	public final RESOURCE resource(int index) {
		return resources.get(index);
	}
	
	public final double areaCost(int index, int upgrade) {
		return areaCost[index]*blue().upgrades().resMask(upgrade, index);
	}
	
	public final double areaCostFlat(int index) {
		return areaCost[index];
	}
	
	public ICON.BIG icon() {
		return icon;
	}
	
	protected TILE_SHEET sheet(ComposerUtil c, ComposerSources s, ComposerDests d, int y1) {
		return null;
	}

	public abstract boolean usesArea();
	


	public CharSequence placable(int tx, int ty) {
		
		return null;
	}
	
	protected final FurnisherItemGroup flush(int min, int max, int rots) {
		
		if (rots != 0 && rots != 1 && rots != 3)
			throw new RuntimeException("" + rots);
		return new FurnisherItemGroup(
				this, rots, 
				jsonGroupText[groups.size()].text("NAME"), 
				jsonGroupText[groups.size()].text("DESC"),
				min,
				max, 
				jsonGroupData[groups.size()].ds("COSTS", resources.size()),
				jsonGroupData[groups.size()].ds("STATS", stats.size())
				);
		
	}
	
	protected final void flushSingle(INFO info) {
		if (jsonGroupText != null)
			throw new RuntimeException(""+jsonGroupText.length);
		FurnisherItemTile t = new FurnisherItemTile(this, new RoomSprite.Dummy() {
			@Override
			public boolean render(SPRITE_RENDERER r, ShadowBatch shadowBatch, int data, RenderIterator it,
					double degrade, boolean isCandle) {
				sheet.render(r, 0, it.x(), it.y());
				return false;
			}
			
			@Override
			public void renderPlaceholder(SPRITE_RENDERER r, int x, int y, int data, int tx, int ty, int rx, int ry,
					FurnisherItem item) {
				SPRITES.cons().BIG.filled.render(r, 0, x, y);
			}
		}, AVAILABILITY.PENALTY4, false);
		new FurnisherItem(new FurnisherItemTile[][] {
			{t,},
		}, 0);
		new FurnisherItemGroup(
				this, 0, 
				info.name, 
				info.desc,
				0,
				1, 
				new double[0],
				new double[0]
				);
	}
	
	public final FurnisherItemGroup flush(int rots) {
		return flush(0, Integer.MAX_VALUE, rots);
	}
	
	protected final FurnisherItemGroup flush(int min, int rots) {
		return flush(min, Integer.MAX_VALUE, rots);
	}
	
	public void putFloor(int tx, int ty, int upgrade, AREA area) {
		if (floor != null) {
			upgrade = CLAMP.i(upgrade, 0, floor.size()-1);
			floor.get(upgrade).placeFixed(tx, ty);
		}
	}
	
	public final LIST<FurnisherItemGroup> groups() {
		return groups;
	}
	
	public final FurnisherItem item(int index) {
		return allItems.get(index);
	}
	
	public final FurnisherItemTile tile(int index) {
		return tiles.get(index);
	}
	
	public final LIST<FurnisherStat> stats(){
		return stats;
	}
	
	public void renderEmbryo(SPRITE_RENDERER r, int mask, RenderIterator it, boolean isFloored, AREA area) {
		if (isFloored) {
			if (mask != 0x0F) {
				SPRITES.cons().BIG.outline_dashed.render(r, mask, it.x(), it.y());
			}
		}else {
			SPRITES.cons().BIG.dashed.render(r, mask, it.x(), it.y());
		}
		
	}
	
	public void renderTileBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, boolean floored) {
		if (floored) {
			
		}
	}
	
	public void renderExtra(SPRITE_RENDERER r, int x, int y, int tx, int ty, int rx, int ry, FurnisherItem item) {
		
	}
	
	public void renderExtra() {
		
	}
	
	public void doBeforePlanning(int tx, int ty) {
		
	}
	
	public boolean removeFertility() {
		return true;
	}
	
	public boolean removeTerrain(int tx, int ty) {
		return !SETT.TERRAIN().NADA.is(tx, ty);
	}
	
	public final COLOR miniColor(int tx, int ty) {
		if (colorPimp != null)
			return colorPimp.get(tx, ty);
		return miniColor;
	}
	
	public final COLOR miniColorPimped(ColorImp origional, int tx, int ty, boolean northern, boolean southern) {
		for (DIR d : DIR.ORTHO) {
			Room r2 = ROOMS().map.get(tx, ty, d);
			if (r2 == null || !r2.isSame(tx+d.x(), ty+d.y(), tx, ty))
				return origional.shadeSelf(0.8);
		}
		return origional;
	}
	
	public boolean mustBeIndoors() {
		return true;
	}
	
	public boolean needsIsolation() {
		return mustBeIndoors() && blue().degradeRate() > 0;
	}
	
	public boolean needFlooring() {
		return true;
	}
	
	public boolean mustBeOutdoors() {
		return false;
	}
	
	public abstract Room create(TmpArea area, RoomInit init);
	
	public abstract RoomBlueprintImp blue();
	
	public boolean canBeCopied() {
		return true;
	}

	public FurnisherItem secretReplacementItem(int rot, FurnisherItem origional) {
		return null;
	}
	
	public CharSequence warning(AREA area) {
		return null;
	}
	
	public CharSequence constructionProblem(AREA area) {
		
		return null;
	}
	
	public boolean hasShape() {
		return usesArea();
	}
	
}
