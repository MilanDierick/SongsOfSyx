package view.sett.ui.room;

import init.C;
import init.D;
import settlement.main.SETT;
import settlement.room.main.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleInstance implements ModuleMaker {
	

	private CharSequence ¤¤ACTIVATE = "¤Activate";
	private CharSequence ¤¤DEACTIVATE = "¤Deactivate";
	private CharSequence ¤¤UNREACHABLE = "¤Room is not reachable and will not work properly. Make sure there is a clear path to your throne.";
	private CharSequence ¤¤DEACTIVATED = "¤Deactivated!";
	
	public ModuleInstance(Init init) {
		D.t(this);
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintIns<?>) {
			l.add(new I((RoomBlueprintIns<?>) p));
		}
	}

	private final class I extends UIRoomModule {
		
		RoomBlueprintIns<?> blue;
		
		I(RoomBlueprintIns<?> blue){
			this.blue = blue;
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
			if (blue.employment() != null || blue == SETT.ROOMS().DUMP) {
				appliers.add(new UIRoomBulkApplier(¤¤ACTIVATE) {
					
					@Override
					protected void apply(RoomInstance t) {
						t.activate(true);
					}
				});
				appliers.add(new UIRoomBulkApplier(¤¤DEACTIVATE) {
					
					@Override
					protected void apply(RoomInstance t) {
						t.activate(false);
					}
				});
			}
			
			
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {

			
		}

		@Override
		public void problem(GBox box, Room room, int rx, int ry) {
			RoomInstance i = (RoomInstance) room;
			if (!i.reachable())
				box.add(box.text().errorify().add(¤¤UNREACHABLE)).NL(C.SG*2);
			if (!i.active())
				box.add(box.text().errorify().add(¤¤DEACTIVATED)).NL(C.SG*2);		
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			// TODO Auto-generated method stub
			
		}


		
	}
	
	



	
}
