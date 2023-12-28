package settlement.room.knowledge.school;

import init.D;
import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<SchoolInstance, ROOM_SCHOOL> {

	private static CharSequence ¤¤learningRate = "¤Learning Rate";
	private static CharSequence ¤¤learningRateI = "¤{0}% / day";

	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_SCHOOL s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<SchoolInstance> getter, int x1, int y1) {
		grid.NL();
		grid.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(¤¤learningRateI);
				text.insert(0, blueprint.learningSpeed(getter.get().mX(), getter.get().mY())*100, 2);
			}
		}.hh(¤¤learningRate));
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	



	@Override
	protected void appendMain(GGrid r, GGrid text, GuiSection sExtra) {
		text.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(¤¤learningRateI);
				text.insert(0, (int)Math.ceil(blueprint.learningSpeed*100));
			}
		}.hh(¤¤learningRate));
	}

}
