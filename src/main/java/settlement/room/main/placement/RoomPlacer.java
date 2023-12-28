package settlement.room.main.placement;

import static settlement.main.SETT.*;
import static settlement.room.main.construction.ConstructionData.*;

import java.io.IOException;

import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.construction.ConstructionData;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.furnisher.*;
import settlement.room.main.util.RoomState;
import settlement.tilemap.terrain.TBuilding;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.*;
import snake2d.util.file.*;
import util.colors.GCOLORS_MAP;
import util.data.BOOLEAN.BOOLEANImp;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.tool.*;

public final class RoomPlacer {

	final PlacerArea placerArea;
	final PlacerItemArea placerAreaItem;
	final PlacerItemSingle placerItemSingle;
	final Instance instance;
	final UtilStats resources = new UtilStats(this);
	final UtilHistory history = new UtilHistory(this);
	final UtilExtraCost cost = new UtilExtraCost();
	final UtilPlacability placability = new UtilPlacability(this);
	public final PlacerDoor door;
	public final PlacableMulti placerDoor;
	public final UtilStructure structure = new UtilStructure(this);
	
	public final BOOLEANImp buildOnWalls = new BOOLEANImp(true);
	public final AutoWalls autoWalls = new AutoWalls();
	
	final PLACEMENT p;
	
	private RoomState state;
	private int oldDegrade = 0;
	boolean renderExpense;

	
	
	public RoomPlacer(PLACEMENT p, Instance ins){
		this.p = p;
		instance = ins;
		this.placerArea = new PlacerArea(this);
		this.placerAreaItem = new PlacerItemArea(this);
		this.placerItemSingle = new PlacerItemSingle(this);
		this.door = new PlacerDoor(this);
		this.placerDoor = door.placer;
		
		new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
				RenderData.RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					Room room = ROOMS().map.get(it.tx(), it.ty());
					if (room == instance) {
						renderPlaceholder(r, shadowBatch, it);
					}
					it.next();
				}
				
				
				
			}
		}.add();
	}
	
	private Coo rCoo = new Coo();
	
	private void renderPlaceholder(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		FurnisherItemTile it = ROOMS().fData.tile.get(i.tile());
		
		if (blueprint().constructor().usesArea() && blueprint().constructor().mustBeIndoors()) {
			if (ConstructionData.dExpensive.is(i.tile(), 1))
				GCOLORS_MAP.SOSO.bind();
			else if (renderExpense){
				double d = cost.get(instance, i.tx(), i.ty());
				ColorImp.TMP.interpolate(SPRITES.cons().color.ok, SPRITES.cons().color.great, d);
				ColorImp.TMP.bind();
			}else {
				SPRITES.cons().color.ok.bind();
			}
		}else {
			SPRITES.cons().color.ok.bind();
		}
		
		
		if (it != null && it.sprite() != null) {
			if (dConstructed.is(i.tile(), 0) && dBroken.is(i.tile(), 0)) {
				FurnisherItem item = ROOMS().fData.item.get(i.tile());
				COORDINATE cc = ROOMS().fData.itemX1Y1(i.tx(), i.ty(), rCoo);
				int rx = i.tx() - cc.x();
				int ry = i.ty() - cc.y();
				if (item.get(rx, ry) == it)
					it.sprite.renderPlaceholder(r, i.x(), i.y(), ROOMS().fData.spriteData.get(i.tile()), i.tx(), i.ty(), rx, ry, item);
				else {
					throw new RuntimeException(item.group.blueprint.blue() + " " + item.width() + " " + item.height());
					//COLOR.RED100.render(r, i.x()+(cc.x()-i.tx())*C.TILE_SIZE, i.y()+(cc.y()-i.ty())*C.TILE_SIZE);
				}if (it.mustBeReachable)
					SPRITES.cons().ICO.arrows_inwards.render(r, i.x(), i.y());
			}
			
			
		}else {
			int m = 0;
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (instance.is(i.tx(), i.ty(), d))
					m |= d.mask();
			}
			blueprint().constructor().renderEmbryo(r, m, i, dFloored.is(i.tile(), 1), instance);
		}
		COLOR.unbind();
		
		if (autoWalls.is()) {
			door.renderWall(r, i);
		}
		
		
	}
	
	public class AutoWalls implements BOOLEAN_MUTABLE{
		boolean on = true;
		
		@Override
		public boolean is() {
			return on && blueprint() != null && blueprint().constructor().mustBeIndoors();
		}

		public boolean getBool() {
			return on;
		}
		
		@Override
		public BOOLEAN_MUTABLE set(boolean bool) {
			on = bool;
			return this;
		}
	}
	
	public RoomBlueprintImp blueprint() {
		return instance.blue;
	}
	
	public void setUpgrade(int upgrade) {
		instance.upgradeSet(upgrade);
		resources.updatee();
	}
	
	public void init(RoomBlueprintImp b, int upgrade) {
		
		if (b == null && instance.blueprint() == null)
			return;
		
		for (COORDINATE c : instance.body()) {
			if (instance.is(c)) {
				placerArea.clear(c.x(), c.y());
			}
		}
		instance.clearRegardless();
		instance.upgradeSet(upgrade);
		
		instance.init(b);
		resources.clear();
		history.clear();
		oldDegrade = 0;
		setState(null);
	}
	
	public RoomBlueprintImp reconstruct(TmpArea area, int upgrade, int degrade, RoomState state, RoomBlueprintImp ret) {

		
		
		
		init(ret, upgrade);
		setState(state);
		oldDegrade = degrade;
		TBuilding b = null;
		
		for (COORDINATE c : area.body()) {
			
			if (!area.is(c))
				continue;

			if (SETT.TERRAIN().get(c) instanceof TBuilding.BuildingComponent) {
				b = ((TBuilding.BuildingComponent) SETT.TERRAIN().get(c)).building();
			}

			blueprint().constructor().doBeforePlanning(c.x(), c.y());
			SETT.LIGHTS().remove(c.x(), c.y());
			
		}
		if (ret.constructor().mustBeIndoors() && b != null)
			structure.set(b);
		
		instance.set(area, ret);
		instance.upgradeSet(upgrade);
		for (COORDINATE c : instance.body()) {
			
			if (!instance.is(c))
				continue;
			
			if (blueprint().constructor().usesArea() && blueprint().constructor().mustBeIndoors()) {
				door.init(c.x(), c.y());
			}
		}
		if (b != null)
			structure.set(b);
		
		return ret;
	}
	
	private void setState(RoomState state) {
		this.state = state;
	}
	
	public void renderExpense() {
		renderExpense = true;
	}
	
	public PLACABLE area() {
		return placerArea;
	}
	
	public PlacableFixed item(int itemGroup) {
		if (!blueprint().constructor().usesArea()) {
			placerItemSingle.set(blueprint(), itemGroup, instance.upgrade());
			return placerItemSingle;
		}
		placerAreaItem.set(blueprint(), itemGroup, instance.upgrade());
		return placerAreaItem;
	}
	
	
	
	public PLACABLE itemPlacerCurrent() {
		if (blueprint() == null)
			return null;
		if (!blueprint().constructor().usesArea()) {
			return placerItemSingle;
		}
		return placerAreaItem;
	}
	
	public COORDINATE create() {
		if (createProblem() != null) {
			throw new RuntimeException(""+createProblem());
		}
		TBuilding structure = null;
		if (blueprint().constructor().mustBeIndoors()) {
			structure = this.structure.get();
		}
		
		if (blueprint().constructor().mustBeIndoors()) {
			if (autoWalls.is()) {
				door.build(structure);
			}else {
				
			}
		}
		
		ConstructionInit init = new ConstructionInit(instance.upgrade(), instance.constructor(), structure, oldDegrade, state);
		Coo.TMP.set(instance.mX(), instance.mY());
		TmpArea tmp = SETT.ROOMS().tmpArea(this);
		tmp.set(instance, instance.mX(), instance.mY());
		SETT.ROOMS().construction.createWithConstructionData(tmp, init);
		instance.clearRegardless();
		return Coo.TMP;
	}

	
	public CharSequence createProblem() {
		if (instance.blue == null)
			return PLACABLE.E;
		return placability.createProblem(instance);
	}
	
	public CharSequence createWarning() {
		if (instance.blue == null)
			return null;
		return instance.blue.constructor().warning(instance);
	}
	
	public FurnisherItemGroup createProblemItem() {
		return placability.createProblemGroup();
	}
	
	public boolean createProblemWalls() {
		if (autoWalls.is())
			return door.createProblem() != null;
		return false;
	}

	
	public double resNeeded(int rI) {
		if (instance.blue == null)
			return 0;
		double am = resources.needed(rI)-resources.allocated(rI);
		if (blueprint().constructor().usesArea() && blueprint().constructor().mustBeIndoors())
			am *= 1.0 + extraExpense()*2;
		return Math.ceil(am);
	}
	
	public int unroofed() {
		if (instance.blue == null)
			return 0;
		return instance.unroofed;
	}
	
	public int walls() {
		if (instance.blue == null)
			return 0;
		if (autoWalls.is())
			return resources.walls;
		return 0;
	}
	
	public double itemStats(int si) {
		if (instance.blue == null)
			return 0;
		return resources.stat(si);
	}
	
	public PlacableFixed createItemPlacer(RoomBlueprintImp b, int group) {
		PlacerItemSingle it = new PlacerItemSingle(this);
		it.set(b, 0, group);
		return it;
	}
	
	public boolean hasHistory() {
		if (instance.blue == null)
			return false;
		if (history.hasHistory())
			return true;
		return false;	
	}
	
	public boolean popHistory() {
		if (instance.blue == null)
			return false;
		if (history.hasHistory()) {
			history.popHistory();
			return true;
		}
		return false;
		
	}
	
	public boolean removeAllItems() {
		if (instance.blue == null)
			return false;
		if (resources.items > 0) {
			for (COORDINATE c : instance.body()) {
				if (instance.is(c)) {
					placerAreaItem.removeItem(c.x(), c.y());
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean removeArea() {
		if (instance.blue == null)
			return false;
		if (instance.area() > 0) {
			RoomBlueprintImp b = blueprint();
			instance.clear(blueprint());
			resources.clear();
			history.clear();
			init(b, instance.upgrade());
			return true;
		}
		return false;
	}
	
	public double isolation() {
		if (instance.blue == null)
			return 0;
		if (!blueprint().constructor().usesArea()) {
			AREA a = placerItemSingle.itemAreaCurrent;
			if (a == null)
				return 0;
			return door.isolation(instance.blue, a, autoWalls.is());
		}else {
			return door.isolation(instance.blue, instance, autoWalls.is());
		}
	}
	
	public double extraExpense() {
		if (instance.blue == null)
			return 0;
		return cost.get(instance);
	}
	
	void update(double ds) {
		renderExpense = false;
//		if (!VIEW.s().isActive())
//			saver.clear();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(blueprint() == null ? -1 : blueprint().index());
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int i = file.i();
			RoomBlueprintImp blue = null;
			if (i != -1 && SETT.ROOMS().all().get(i) instanceof RoomBlueprintImp)
				blue = (RoomBlueprintImp) SETT.ROOMS().all().get(i);
			
			
			instance.blue = blue;
			if (blue != null) {
				for (int y = instance.body().y1(); y < instance.body().y2(); y++)
					for (int x = instance.body().x1(); x < instance.body().x2(); x++) {
						if (instance.is(x, y)) {
							placerArea.clear(x, y);
						}
					}
			}
			instance.clearRegardless();
			
			init(null, 0);
		}
		
		@Override
		public void clear() {
			init(null, 0);
		}
	};
	
}
