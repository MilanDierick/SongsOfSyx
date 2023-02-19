package view.sett.ui.room.construction;

import settlement.main.SETT;
import settlement.room.main.Room.RoomInstanceImp;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.TmpArea;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomState;
import view.main.VIEW;
import view.tool.PlacableFixed;

public class UIRoomPlacer {

	private final State state;
	
	public UIRoomPlacer() {
		state = new State();
	}
	
	public void init(RoomBlueprintImp b, int tx, int ty) {
		state.init(b, false);
		
		state.placement.placer.init(b, 0);
		if (b.constructor().usesArea())
			VIEW.s().tools.place(state.placement.placer.area(), state.config);
		else {
			
			
			FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
			if (it != null) {
				PlacableFixed pp = state.placement.placer.item(it.group.index());
				pp.rotSet(it.rotation);
				int size = 0;
				for (int i = 0; i < it.group.size(); i++) {
					if (it.group.item(i, it.rotation) == it) {
						size = i;
						break;
					}
				}
				pp.sizeSet(size);
				VIEW.s().tools.place(pp, state.config);
			}else{
				PlacableFixed pp = state.placement.placer.item(state.item());
				VIEW.s().tools.place(pp, state.config);
			}
			
				
		}
	}
	
	public void init(RoomBlueprintImp b, RoomCategorySub bb) {
		state.init(b, bb);
		
		state.placement.placer.init(b, 0);
		if (b.constructor().usesArea())
			VIEW.s().tools.place(state.placement.placer.area(), state.config);
		else {
			PlacableFixed pp = state.placement.placer.item(state.item());
			VIEW.s().tools.place(pp, state.config);
		}
	}
	
	public void init(RoomInstanceImp ins) {
		state.init(ins.constructor().blue(), true);
		
		RoomBlueprintImp b = ins.constructor().blue();
		RoomState stat = ins.makeState(ins.mX(), ins.mY());
		int up = ins.upgrade();
		int deg = ins.degrader(ins.mX(), ins.mY()) == null ? 0 : ins.degrader(ins.mX(), ins.mY()).getData();
		TmpArea a = ins.remove(ins.mX(), ins.mY(), false, this, false);
		if (a == null)
			return;
		if (a.area() <= 0) {
			a.clear();
			return;
		}
			
		SETT.ROOMS().placement.placer.reconstruct(a, up, deg, stat, b);
		
		VIEW.s().tools.place(state.placement.placer.area(), state.config);
		
	}
	
	
	public void init(int tx, int ty) {
		if (state.placement.canReconstruct(tx, ty)) {
			RoomInstanceImp r = (RoomInstanceImp) SETT.ROOMS().map.get(tx, ty);
			init(r);
			
		}
	}
	
	public boolean isActive(RoomBlueprintImp b) {
		return VIEW.s().tools.configCurrent() == state.config && b == this.state.b;
	}
	
}
