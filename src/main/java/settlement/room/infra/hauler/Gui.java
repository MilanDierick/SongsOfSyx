package settlement.room.infra.hauler;

import init.D;
import init.resources.RESOURCE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.common.GResSelector;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<HaulerInstance, ROOM_HAULER> {
	
	{D.gInit(this);}
	
	Gui(ROOM_HAULER s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<HaulerInstance> g, int x1, int y1) {
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().amount, Crate.size*g.get().crates);
			}
		}.hv(D.g("hauled")));
		
		section.addRelBody(8, DIR.S, new GButt.CheckboxTitle(D.g("fetch")) {
			
			@Override
			protected void clickA() {
				g.get().fetch(!g.get().fetch());
			}
			
			@Override
			protected void renAction() {
				selectedSet(g.get().fetch());
			}
			
		}.hoverInfoSet(D.g("fetchDesc", "If this is enabled, haulers will take resources from Warehouses with Fetch disabled, as well as loose resources on the ground.")));
		
		section.addRelBody(8, DIR.S, new GResSelector() {
			
			@Override
			protected void select(RESOURCE r, int li) {
				g.get().setResource(r);
				
			}
			
			@Override
			protected RESOURCE getResource() {
				return g.get().resource();
			}
		});
		
	}
	
	@Override
	protected void hover(GBox box, HaulerInstance i) {
		super.hover(box, i);
		if (i.resource() != null) {
			box.add(i.resource().icon());
			box.add(GFORMAT.iofk(box.text(), i.amount, i.crates*Crate.size));
		}
			
	}
	
	
	

}
