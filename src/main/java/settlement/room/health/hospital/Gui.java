package settlement.room.health.hospital;

import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<HospitalInstance, ROOM_HOSPITAL> {

	Gui(ROOM_HOSPITAL s) {
		super(s);
	}
	
	@Override
	protected void problem(HospitalInstance i, GBox box) {
		
		super.problem(i, box);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<HospitalInstance> getter, int x1, int y1) {
		
	}

}
