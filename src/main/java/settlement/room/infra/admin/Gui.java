package settlement.room.infra.admin;

import game.time.TIME;
import init.D;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<AdminInstance, ROOM_ADMIN> {

	private final GChart chart = new GChart();
	private static CharSequence ¤¤TargetD = "¤Estimation of how much Administration can be produced and maintained:";
	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_ADMIN s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<AdminInstance> getter, int x1, int y1) {
		
		grid.add(new GStat() {
			
			@Override
			public void update(GText text) {
				double p = IndustryUtil.calcProductionRate(blueprint.workValue, blueprint.industry, getter.get());
				
				GFORMAT.f0(text, TIME.workSeconds*p/Job.time, TIME.workSeconds*blueprint.workValue/Job.time);
				text.s().add('|').s();
				
				double v = p/blueprint.workValue;
				int am = (int) (blueprint.knowledgePerStation()*getter.get().employees().employed()*v);
				text.add(am);
			}
			@Override
			public void hoverInfoGet(GBox b) {
				
				b.text(¤¤TargetD);
				double p = IndustryUtil.calcProductionRate(blueprint.workValue, blueprint.industry, getter.get());
				double v = p/blueprint.workValue;
				int am = (int) (blueprint.knowledgePerStation()*getter.get().employees().employed()*v);
				b.NL();
				b.add(GFORMAT.f(b.text(), am));
				b.NL(8);
				
				IndustryUtil.hoverProductionRate(b, TIME.workSeconds*blueprint.workValue/Job.time, blueprint.industry, getter.get());
			};
			
		}.hh(DicMisc.¤¤ProductionRate));
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	
	@Override
	protected void hover(GBox box, AdminInstance i) {
		super.hover(box, i);
		box.NL(8);
		box.textLL(DicMisc.¤¤Admin);
		box.add(GFORMAT.i(box.text(), blueprint.knowledge()));
	}


	@Override
	protected void appendMain(GGrid r, GGrid text, GuiSection sExtra) {
		text.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.knowledge());
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				chart.clear();
				chart.add(blueprint.data.utilizedHistory);
				b.add(chart);
			};
			
		}.hh(DicMisc.¤¤Admin));
		text.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.employment().employed()*blueprint.knowledgePerStation());
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤TargetD);
				b.add(GFORMAT.i(b.text(), blueprint.employment().employed()*blueprint.knowledgePerStation()));
			};
			
		}.hh(DicMisc.¤¤Target));
	}

}
