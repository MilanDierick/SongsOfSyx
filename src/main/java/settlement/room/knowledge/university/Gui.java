package settlement.room.knowledge.university;

import game.faction.FACTIONS;
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

class Gui extends UIRoomModuleImp<UniversityInstance, ROOM_UNIVERSITY> {

	private static CharSequence ¤¤learningRate = "¤Learning Rate";
	private static CharSequence ¤¤learningRateI = "¤{0}% / day";

	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_UNIVERSITY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<UniversityInstance> getter, int x1, int y1) {
		grid.NL();
		grid.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(¤¤learningRateI);
				text.insert(0, (int)Math.ceil(blueprint.learningSpeed(getter.get(), FACTIONS.player())*100));
			}
		}.hh(¤¤learningRate));
		grid.NL();

		
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
