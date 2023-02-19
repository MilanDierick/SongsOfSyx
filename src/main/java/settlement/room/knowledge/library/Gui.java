package settlement.room.knowledge.library;

import init.D;
import init.sprite.SPRITES;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
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

class Gui extends UIRoomModuleImp<LibraryInstance, ROOM_LIBRARY> {

	private final GChart chart = new GChart();
	private static CharSequence ¤¤TargetD = "¤Estimation of how much knowledge can be boosted by";

	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_LIBRARY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<LibraryInstance> getter, int x1, int y1) {
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				GBox b = (GBox) text;
				b.text(¤¤TargetD);
				double m =  blueprint.boostPerStation()*getter.get().employees().employed();
				double p = IndustryUtil.calcProductionRate(1, null, blueprint.bonus, getter.get());
				p *= m;
				b.NL();
				b.add(perc(b.text(), p, m));
				b.NL(8);
				
				IndustryUtil.hoverProductionRate(b, 1, null, blueprint.bonus, getter.get());
			}
			
		};
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				double p = IndustryUtil.calcProductionRate(1, null, blueprint.bonus, getter.get());
				GFORMAT.perc(text, p, 1);
				
			}
		}.hh(SPRITES.icons().s.hammer), 0, 0);
		
		s.addRightC(100, new GStat() {
			
			@Override
			public void update(GText text) {
				double m =  blueprint.boostPerStation()*getter.get().employees().employed();
				double p = IndustryUtil.calcProductionRate(1, null, blueprint.bonus, getter.get());
				p *= m;
				perc(text, p, m);
				
			}
		}.hh(SPRITES.icons().s.arrow_right));
		
		s.body().incrW(64);
		
		s.addRelBody(8, DIR.N, new GHeader(DicMisc.¤¤Knowledge));
		
		section.addRelBody(8,  DIR.S, s);
		
	}
	
	private GText perc(GText t, double v, double m) {
		t.add('+');
		GFORMAT.perc(t, v, 4);
		GFORMAT.colorInterInv(t, v, m);
		return t;
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	
	@Override
	protected void hover(GBox box, LibraryInstance i) {
		super.hover(box, i);
		box.NL(8);
		box.textLL(DicMisc.¤¤Knowledge);
		box.add(GFORMAT.perc(box.text(), blueprint.boost(), 4));
	}


	@Override
	protected void appendMain(GGrid r, GGrid text, GuiSection sExtra) {
		text.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, blueprint.boost(), 4);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				chart.clear();
				chart.add(blueprint.data.utilizedHistory);
				b.add(chart);
			};
			
		}.hh(DicMisc.¤¤Knowledge));
		text.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, blueprint.projection(), 4);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤TargetD);
				b.add(GFORMAT.perc(b.text(), blueprint.projection(), 4));
			};
			
		}.hh(DicMisc.¤¤Target));
	}

}
