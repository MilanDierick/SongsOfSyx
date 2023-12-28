package settlement.room.service.nursery;

import game.time.TIME;
import init.D;
import settlement.entity.ENTETIES;
import settlement.entity.humanoid.HTYPE;
import settlement.room.industry.module.IndustryUtil;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.data.GETTER;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<NurseryInstance, ROOM_NURSERY> {

	private static CharSequence ¤¤next = "Days until next child: ";
	private static CharSequence ¤¤capabilitity = "Reproduction Capability";
	private static CharSequence ¤¤capabilitityD = "Reproduction Capability is determined of your current adult population.";

	
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
		
	
		GuiSection s = new GuiSection();
		int M = 280;
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.rmax());
			}
		}.hh(¤¤capabilitity, ¤¤capabilitityD, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().getWork().size());
			}
		}.hh(DicRes.¤¤Capacity, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().kidspotsUsed);
			}
		}.hh(HTYPE.CHILD.names, M));
		
		s.addDown(4, new GStat() {
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
		}.hh(DicMisc.¤¤Babies, M));
		grid.NL();
		
		s.addDown(4, new GStat() {
			
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
		}.hh(¤¤next, M));
		
		section.addRelBody(8, DIR.S, s);
		
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid r, GuiSection sExtra) {
		
		GuiSection s = new GuiSection();
		int M = 280;
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.rmax());
			}
		}.hh(¤¤capabilitity, ¤¤capabilitityD, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsTotal);
			}
		}.hh(DicRes.¤¤Capacity, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsUsed);
			}
		}.hh(HTYPE.CHILD.names, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.babies);
			}
		}.hh(DicMisc.¤¤Babies, M));

		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.f(text, (blueprint.BABY_DAYS + blueprint.race.physics.adultAt)/TIME.years().bitConversion(TIME.years()));
			}
		}.hh(DicMisc.¤¤AdultAge, M));
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return ENTETIES.MAX;
			}
			
			@Override
			public int get() {
				return CLAMP.i(blueprint.limit, min(), max());
			}
			
			@Override
			public void set(int t) {
				CLAMP.i(t, min(), max());
				blueprint.limit = t;
			}
		};
		
		r.NL();
		
		r.section.addRelBody(0, DIR.S, s);
		
		r.section.addRelBody(8, DIR.S, new GHeader(DicMisc.¤¤limit));
		
		r.section.addRelBody(2, DIR.S, new GSliderInt(in, 200, true));
		
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
