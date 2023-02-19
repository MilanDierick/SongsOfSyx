package settlement.room.service.arena;

import init.D;
import init.sprite.SPRITES;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<ArenaInstance, ROOM_ARENA> {

	private static CharSequence ¤¤gladiators = "Gladiators";
	private static CharSequence ¤¤gladiatorsD = "The amount of prisoners that have performed. This affects quality. Poor access to prisoners will drag his value down.";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_ARENA s) {
		super(s);
	}
	
	@Override
	protected void problem(ArenaInstance i, GBox box) {
		super.problem(i, box);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<ArenaInstance> getter, int x1, int y1) {
		
		blueprint.constructor.quality.appendPanel(section, grid, getter, x1, y1);
		
		grid.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, getter.get().gladiatorValue());
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.add(GFORMAT.perc(b.text(), getter.get().gladiatorValue()));
				b.add(SPRITES.icons().m.arrow_right);
				b.add(GFORMAT.perc(b.text(), getter.get().gladiatorValueNext()));
				b.NL();
				b.text(¤¤gladiatorsD);
				
				b.NL(8);
				b.text(DicMisc.¤¤Current);
				b.add(GFORMAT.iofkInv(b.text(), getter.get().gladiatorsMax-getter.get().gladiatorsNeeded(), getter.get().gladiatorsMax));
			};
			
		}.hh(¤¤gladiators));
		
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid r, GuiSection sExtra) {
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, blueprint.gladiators, blueprint.gladiatorMax);
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤gladiatorsD);
			};
			
		}.hh(¤¤gladiators));
		
	}
	
	@Override
	protected void hover(GBox box, ArenaInstance i) {
		
	}

}
