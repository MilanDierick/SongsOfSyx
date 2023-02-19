package settlement.room.health.asylum;

import init.D;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HTYPE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<AsylumInstance, ROOM_ASYLUM> {
	
	private static CharSequence ¤¤Treatment = "¤Treatment";
	private static CharSequence ¤¤TreatmentD = "¤Treatment factor is determined my the number of employed wards and degrade of the room. Keep rooms fully employed for the best recover rates.";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_ASYLUM s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<AsylumInstance> g, int x1, int y1) {
		
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkNoColor(text, g.get().prisoners(), g.get().prisonersMax());
			}
		}.hv(HTYPE.DERANGED.names));
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, blueprint.treatmentFactor(g.get()));
			}
		}.hv(¤¤Treatment).hoverInfoSet(¤¤TreatmentD));
		
		
	}
	
	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, blueprint.prisoners(), blueprint.prisonersMax());
			}
		}.hh(SPRITES.icons().s.crazy).hoverInfoSet(HTYPE.DERANGED.desc);
		
		text.add(r);
	}
	
	@Override
	protected void hover(GBox box, AsylumInstance i) {
		box.NL();
		box.text(HTYPE.DERANGED.names);
		box.add(GFORMAT.iofk(box.text(), i.prisoners(), i.prisonersMax()));
	}
	


}
