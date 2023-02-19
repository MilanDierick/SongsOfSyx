package view.sett.ui.room;

import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.main.*;
import settlement.room.main.furnisher.Furnisher;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleConstructor implements ModuleMaker {
	

	public ModuleConstructor(Init init) {
	
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintImp) {
			RoomBlueprintImp pp = (RoomBlueprintImp) p;
			if (pp.constructor() != null)
				l.add(new I());
		}
	}

	private final class I extends UIRoomModule {
		

		
		I(){

		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			Furnisher f = room.constructor();
			if (f != null) {
				boolean has = false;
				for (SettEnv e : SETT.ENV().environment.all()) {
					if (f.envValue(e)) {
						if (!has) {
							box.NL(8);
							box.textL(DicMisc.¤¤Emits);
							box.NL();
						}
						box.text(e.name);
					}
				}
				if (has)
					box.NL(8);
			}
			
			
		}

		@Override
		public void problem(GBox box, Room room, int rx, int ry) {
		
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			// TODO Auto-generated method stub
			
		}


		
	}
	
	



	
}
