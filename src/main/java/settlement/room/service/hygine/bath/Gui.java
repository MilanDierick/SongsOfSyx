package settlement.room.service.hygine.bath;

import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<BathInstance, ROOM_BATH> {

	Gui(ROOM_BATH s) {
		super(s);
	}
	
	@Override
	protected void problem(BathInstance i, GBox box) {
		if (i.getHeat() < 1) {
			box.add(box.text().errorify().add(blueprint.sHeatingProblem));
		}
			
		super.problem(i, box);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<BathInstance> getter, int x1, int y1) {
		
	
		grid.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, getter.get().getHeat());
			}
		}.hh(blueprint.sHeating).hoverInfoSet(blueprint.sHeatingDesc));
		
	}

}
