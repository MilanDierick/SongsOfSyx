package settlement.room.military.archery;

import init.D;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

final class Gui extends UIRoomModuleImp<ArcheryInstance, ROOM_ARCHERY>{
	
	private static CharSequence ¤¤Limit = "¤Recruits limit";
	private static CharSequence ¤¤LimitD = "¤The number of recruits that you allow to train simultaneously.";
	
	private static CharSequence ¤¤speed = "¤Training Speed";
	private static CharSequence ¤¤speedD = "¤The speed at which subjects are trained.";
	private static CharSequence ¤¤maxLevel = "¤Days to reach max level: ";
	
	
	static {
		D.ts(Gui.class);
	}
	
	protected Gui(ROOM_ARCHERY blueprint) {
		super(blueprint);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid g, GETTER<ArcheryInstance> getter, int x1, int y1) {
		INTE t = new INTE() {
			
			@Override
			public int min() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int max() {
				return getter.get().employees().max();
			}
			
			@Override
			public int get() {
				return getter.get().employees().target();
			}
			
			@Override
			public void set(int t) {
				getter.get().employees().neededSet(t);
			}
		};
		
		GGaugeMutable m = new GGaugeMutable(t, 220) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.i(text, t.get());
				return 48;
			}
		};
		m.hoverInfoSet(¤¤LimitD);
		
		section.addRelBody(8, DIR.S, new GHeader(¤¤Limit).hoverInfoSet(¤¤LimitD));
		
		section.addRelBody(4, DIR.S, m);
		
		GStat ss = new GStat() {
			
			@Override
			public void update(GText text) {
				double d = IndustryUtil.calcProductionRate(1, blueprint.rate, getter.get());
				GFORMAT.perc(text, d);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤speed);
				b.text(¤¤speedD);
				b.NL(4);
				IndustryUtil.hoverBoosts(b, 1, blueprint.rate, blueprint.rate.bonus(), getter.get());
				b.NL(8);
				b.textLL(¤¤maxLevel);

				double d = 1.0/blueprint.DAY_RATE/IndustryUtil.calcProductionRate(1, blueprint.rate, getter.get());
				
				b.add(GFORMAT.f(b.text(), d));
			}
		};
		
		section.addRelBody(8, DIR.S, ss.hv(¤¤speed));
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		// TODO Auto-generated method stub
		super.appendTableFilters(filters, sorts, appliers);
	}
	
	@Override
	protected void hover(GBox box, ArcheryInstance i) {
		// TODO Auto-generated method stub
		super.hover(box, i);
	}
	
	@Override
	protected void problem(ArcheryInstance i, GBox box) {
		// TODO Auto-generated method stub
		super.problem(i, box);
	}

}
