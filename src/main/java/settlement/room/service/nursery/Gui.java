package settlement.room.service.nursery;

import game.time.TIME;
import init.D;
import settlement.entity.ENTETIES;
import settlement.entity.humanoid.HTYPE;
import settlement.room.industry.module.IndustryUtil;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.data.DOUBLE;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<NurseryInstance, ROOM_NURSERY> {

	private static CharSequence ¤¤next = "Days until next child: ";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_NURSERY s) {
		super(s);
	}
	
	@Override
	protected void problem(NurseryInstance i, GBox box) {
		super.problem(i, box);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<NurseryInstance> getter, int x1, int y1) {
		
	
		grid.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().getWork().size());
			}
		}.hh(DicRes.¤¤Capacity));
		
		grid.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().kidspotsUsed);
			}
		}.hh(HTYPE.CHILD.names));
		
		grid.add(new GStat() {
			@Override
			public void update(GText text) {
				int am = 0;
				for (COORDINATE c : getter.get().body()) {
					if (getter.get().is(c) && blueprint.ss.init(c.x(), c.y())) {
						am += blueprint.ss.age.get() > 0 ? 1 : 0;
					}
				}
				GFORMAT.i(text,am);
			}
		}.hh(DicMisc.¤¤Babies));
		grid.NL();
		
		grid.add(new GStat() {
			
			@Override
			public void update(GText text) {
				double min = 0;
				for (COORDINATE c : getter.get().body()) {
					if (getter.get().is(c) && blueprint.ss.init(c.x(), c.y())) {
						if (blueprint.ss.age.get() > min) {
							min = blueprint.ss.age.get();
						}
					}
				}
				GFORMAT.i(text, (int)Math.ceil((blueprint.BABY_DAYS-min)/IndustryUtil.roomBonus(getter.get(), blueprint.productionData)));
			}
		}.hh(¤¤next));
		
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid r, GuiSection sExtra) {
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsTotal);
			}
		}.hh(DicRes.¤¤Capacity));
		
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsUsed);
			}
		}.hh(HTYPE.CHILD.names));
		
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.babies);
			}
		}.hh(DicMisc.¤¤Babies));
		
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.BABY_DAYS);
			}
		}.hh(DicMisc.¤¤IncubationDays));
		
		r.add(new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.f(text, blueprint.race.physics.adultAt/TIME.years().bitConversion(TIME.years()));
			}
		}.hh(DicMisc.¤¤AdultAge));
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return ENTETIES.MAX/25;
			}
			
			@Override
			public int get() {
				return blueprint.limit/25;
			}
			
			@Override
			public void set(int t) {
				blueprint.limit = t*25;
			}
		};
		
		r.NL();
		
		r.add(new CLICKABLE.Pair(new GHeader(DicMisc.¤¤limit), new GGaugeMutable(in, 200) {
			@Override
			protected int setInfo(DOUBLE d, GText text) {
				GFORMAT.iBig(text, blueprint.limit);
				return 64;
			}
		}, DIR.S, 4));
		
	}
	
	@Override
	protected void hover(GBox box, NurseryInstance i) {
		box.textL(DicRes.¤¤Capacity);
		box.tab(5);
		box.add(GFORMAT.i(box.text(), i.getWork().size()));
		
		box.NL();
		box.textL(HTYPE.CHILD.names);
		box.tab(5);
		box.add(GFORMAT.i(box.text(), i.kidspotsUsed));
		
		box.NL();
		box.textL(DicMisc.¤¤Babies);
		box.tab(5);
		int am = 0;
		for (COORDINATE c : i.body()) {
			if (i.is(c) && blueprint.ss.init(c.x(), c.y())) {
				am += blueprint.ss.age.get() > 0 ? 1 : 0;
			}
		}
		box.add(GFORMAT.i(box.text(), am));
	}

}
