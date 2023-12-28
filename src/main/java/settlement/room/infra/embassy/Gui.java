package settlement.room.infra.embassy;

import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<EmbassyInstance, ROOM_EMBASSY> {
	
	public Gui(ROOM_EMBASSY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<EmbassyInstance> getter, int x1, int y1) {
		
		
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	
	@Override
	protected void hover(GBox box, EmbassyInstance i) {
		super.hover(box, i);

	}

	@Override
	protected void appendMain(GGrid r, GGrid text, GuiSection sExtra) {

	}

}
