package settlement.room.law.guard;

import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<GuardInstance, ROOM_GUARD> {
	
	private CharSequence ¤¤effDesc = "Determines radius and thus chance to apprehend criminals and deter crime.";
	
	
	Gui(ROOM_GUARD s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<GuardInstance> g, int x1, int y1) {
		
		section.addDownC(1, new RENDEROBJ.RenderImp(1) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				SETT.OVERLAY().RadiusInter(blueprint, blueprint.finder);
			}
			
		});
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, g.get().eff());
			}
		}.hv(DicMisc.¤¤Effectivness, ¤¤effDesc));

	}
	
	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		
	}
	
	@Override
	protected void hover(GBox box, GuardInstance i) {
		//SETT.OVERLAY().RadiusInter(blueprint, blueprint.finder);
	}
	


}
