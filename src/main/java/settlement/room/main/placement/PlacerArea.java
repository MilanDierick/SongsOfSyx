package settlement.room.main.placement;

import init.D;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.construction.ConstructionData;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import util.gui.misc.GBox;
import view.tool.*;

class PlacerArea extends PlacableMulti{

	private static CharSequence ¤¤name = "¤Expand Room";
	private static CharSequence ¤¤shrink = "¤Shrink Room";
	static {
		D.t(PlacerArea.class);
	}
 	
	private final RoomPlacer embrio;
	private final PlacableMulti undo = new PlacableMulti(¤¤shrink, ¤¤shrink, SPRITES.icons().m.shrink) {
		
		@Override
		public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (embrio.instance.is(tx, ty))
				clear(tx, ty);
			embrio.history.placeEmbryo(tx, ty, -1);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (!embrio.instance.is(tx, ty)) {
				return PlacableMessages.¤¤ROOM_MUST;
			}
			return null;
		}
		
		@Override
		public PLACABLE getUndo() {
			return PlacerArea.this;
		}
		
		@Override
		public void finishPlacing(AREA placedArea) {
			for (COORDINATE c : placedArea.body())
				if (placedArea.is(c)) {
					validateItems(c.x(), c.y());
				}
				
			super.finishPlacing(placedArea);
		}
		
		@Override
		public void placeInfo(GBox b, int oktiles, AREA a) {
			// TODO Auto-generated method stub
			super.placeInfo(b, oktiles, a);
		}
	};
	

	
	void clear(int tx, int ty) {
		if (ConstructionData.dFloored.is(tx, ty, 1)) {
			embrio.resources.removeTile(tx, ty);
			SETT.FLOOR().clearer.clear(tx, ty);
			
		}
		
		SETT.MAINTENANCE().evaluate(tx, ty);
		
		FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
		if (it != null) {
			boolean constructued = ConstructionData.dConstructed.is(tx, ty, 1);
			COORDINATE c = SETT.ROOMS().fData.itemX1Y1(tx, ty, Coo.TMP);
			
			int x1 = c.x();
			int y1 = c.y();
			
			SETT.ROOMS().fData.itemClear(tx, ty, embrio.instance);
			embrio.history.placeItem(it, x1, y1, -1);
			if (constructued) {
				embrio.resources.removeItem(x1, y1, it);
			}
		}
		
//		for(int i = 0; i < DIR.ORTHO.size(); i++) {
//			DIR d = DIR.ORTHO.get(i);
//			embrio.door.undo.place(tx+d.x(), ty+d.y(), null, null);
//		}
		
		embrio.instance.clear(tx, ty);
		

	}
	
	public PlacerArea(RoomPlacer embrio) {
		super(¤¤name, null, null, null);
		this.embrio = embrio;
	}
	
	private void validateItems(int tx, int ty) {
		
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR d = DIR.ALL.get(i);
			int dx = tx+d.x();
			int dy = ty+d.y();
			if (!embrio.instance.is(dx, dy))
				continue;
			FurnisherItem it = SETT.ROOMS().fData.item.get(dx, dy);
			if (it != null) {
				
				boolean constructued = ConstructionData.dConstructed.is(dx, dy, 1);
				
				COORDINATE c = SETT.ROOMS().fData.itemX1Y1(dx, dy, Coo.TMP);
				int x1 = c.x();
				int y1 = c.y();
				SETT.ROOMS().fData.itemClear(dx, dy, embrio.instance);
				
				if (!replaceItem(x1, y1, it, constructued)) {
					embrio.history.placeItem(it, x1, y1, -1);
					if (constructued)
						embrio.resources.removeItem(x1, y1, it);
				}
			}
		}
	}
	
	private boolean replaceItem(int x1, int y1, FurnisherItem it, boolean constructed) {
		for (int y = 0; y < it.height(); y++) {
			for (int x = 0; x < it.width(); x++) {
				if (embrio.placability.itemPlacable(x1+x, y1+y, x, y, it, embrio.instance) != null)
					return false;
			}
		}
		if (embrio.placability.itemProblem(x1, y1, it.group, it, embrio.instance) != null)
			return false;
		SETT.ROOMS().fData.itemSet(x1, y1, it, embrio.instance);
		if (constructed) {
			for (int y = 0; y < it.height(); y++) {
				for (int x = 0; x < it.width(); x++) {
					if (it.get(x, y) != null)
						ConstructionData.dConstructed.set(embrio.instance, x+x1, y+y1, 1);
				}
			}
		}
		return true;
		
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
			PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
		super.renderPlaceHolder(r, mask, x, y, tx, ty, area, type, isPlacable, areaIsPlacable);
		if (isPlacable && embrio.autoWalls.isOn()) {
			embrio.door.renderTmpPlaceArea(r, x, y, tx, ty, area);
		}
		
	}

	int am = 0;
	
	private final Rec rec = new Rec();
	private final Rec rec2 = new Rec();
	
	@Override
	public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
		
		
		
		//am = 0;
		
		return null;
		
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {

		if (ty == a.body().y1() && !a.is(tx-1, ty)) {
			rec.clear();
			
			for (COORDINATE c: embrio.instance.body()) {
				if (embrio.instance.is(c))
					rec.unify(c.x(), c.y());
			}
			am = 0;
		}
		
		CharSequence s = PLACEMENT.placable(tx, ty, embrio.blueprint(), embrio.buildOnWalls.isOn());
		if (s != null)
			return s;
		
		s = embrio.blueprint().constructor().placable(tx, ty);
		if (s != null)
			return s;
		
		am++;
		if (embrio.instance.area() + am >= Room.MAX_SIZE)
			return PlacableMessages.¤¤MAX_SIZE_REACHED;
		
		if (tx - a.body().x1() >= Room.MAX_DIM)
			return PlacableMessages.¤¤MAX_DIMENSION_REACHED;
		
		if (ty - a.body().y1() >= Room.MAX_DIM)
			return PlacableMessages.¤¤MAX_DIMENSION_REACHED;
		
		if (embrio.instance.area() > 0) {
			rec2.set(rec);
			rec2.unify(tx, ty);
			if (rec2.width() > Room.MAX_DIM)
				return PlacableMessages.¤¤MAX_DIMENSION_REACHED;
			if (rec2.height() > Room.MAX_DIM)
				return PlacableMessages.¤¤MAX_DIMENSION_REACHED;
		}
		
		
		
		return null;
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (embrio.instance.area() >= Room.MAX_SIZE)
			return;
		am--;
		if (!embrio.instance.is(tx, ty)) {
			SETT.JOBS().clearer.set(tx, ty);
			embrio.door.undo.place(tx, ty, null, null);
			for(int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				embrio.door.undo.place(tx+d.x(), ty+d.y(), null, null);
			}
			
			embrio.instance.set(tx, ty);
			SETT.ROOMS().data.set(embrio.instance, tx, ty, 0);
			validateItems(tx, ty);
			
			embrio.history.placeEmbryo(tx, ty, 1);
		}
	}

	@Override
	public PLACABLE getUndo() {
		return undo;
	}
	
	

}
