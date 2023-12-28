package settlement.room.infra.janitor;

import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<JanitorInstance, ROOM_JANITOR> {

	private final CharSequence ¤¤Missing = "¤Missing Resources";
	private final CharSequence ¤¤MissingDesc = "¤Depending on what is maintained and repaired, certain resources might be required. A janitor will search for these in a radius of 150 tiles and if not found, maintenance work will be much slower.";
	
	Gui(ROOM_JANITOR s) {

		super(s);
		D.t(this);


	}
	
	@Override
	public void hover(GBox box, JanitorInstance i) {
		super.hover(box, i);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<JanitorInstance> getter, int x1, int y1) {
		
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤Missing);
				text.text(¤¤MissingDesc);
				GBox b = (GBox) text;
				b.NL(8);
				for (RESOURCE res : RESOURCES.ALL()) {
					if (getter.get().resourcesMissing.has(res)) {
						b.add(b.text().errorify().add(res.name));
						b.NL();
					}
				}
			}
		};
		GHeader h = new GHeader(¤¤Missing);
		s.add(h);
		s.addRightC(8, new RENDEROBJ.RenderImp(Icon.M*8, Icon.M) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				SETT.OVERLAY().MAINTENANCE.add();
				int i = 0;
				for (RESOURCE res : RESOURCES.ALL()) {
					if (getter.get().resourcesMissing.has(res)) {
						res.icon().render(r, body().x1()+i*Icon.S, body().y1());
						i++;
					}
					if (i > 12)
						break;
				}
					
			}
		});
		
		section.addDownC(8, s);
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}


}
