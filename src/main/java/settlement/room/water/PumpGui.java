package settlement.room.water;

import init.D;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

final class PumpGui extends UIRoomModuleImp<PumpInstance, Pump>{

	public static CharSequence ¤¤GroundWater = "Ground Water";
	private static CharSequence ¤¤Workers = "Work Value";
	private static CharSequence ¤¤Preasure = "Pressure (tiles)";
	
	static {
		D.ts(PumpGui.class);
	}
	
	public PumpGui(Pump blueprint) {
		super(blueprint);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void hover(GBox box, PumpInstance ins) {
		box.textLL(¤¤GroundWater);
		box.add(GFORMAT.perc(box.text(), ins.valueBase/PumpInstance.valueMax));
		box.NL();
		box.textLL(¤¤Workers);
		box.add(GFORMAT.perc(box.text(), (double)ins.value/ins.valueBase));
		box.NL();
		box.textLL(¤¤Preasure);
		box.add(GFORMAT.iofkInv(box.text(), ins.value, (int)PumpInstance.valueMax));
	}
	
	@Override
	protected void appendPanel(GuiSection section, GGrid g, GETTER<PumpInstance> getter, int x1, int y1) {
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				PumpInstance ins = getter.get();
				GFORMAT.perc(text, ins.valueBase/PumpInstance.valueMax);
			}
		}.hv(¤¤GroundWater));
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				PumpInstance ins = getter.get();
				GFORMAT.perc(text, (double)ins.value/ins.valueBase);
			}
		}.hv(¤¤Workers));
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				PumpInstance ins = getter.get();
				GFORMAT.iofkInv(text, ins.value, (int)PumpInstance.valueMax);
			}
		}.hv(¤¤Preasure));
	}
	
}
