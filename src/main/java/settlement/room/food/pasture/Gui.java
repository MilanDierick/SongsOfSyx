package settlement.room.food.pasture;

import init.D;
import settlement.entity.humanoid.HCLASS;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<PastureInstance, ROOM_PASTURE> {

	static CharSequence ¤¤Animals = "¤Animals";
	static CharSequence ¤¤Tending = "¤Tending";
	static CharSequence ¤¤Skill = "¤Skill";
	static CharSequence ¤¤SkillD = "¤Skill gets put into the tending, and is multiplies the output.";
	static CharSequence ¤¤BaseRate = "¤Base Rate";
	
	
	static CharSequence ¤¤FailedTending = "¤Failed Tending";
	static CharSequence ¤¤DailyWork = "¤Daily Tending";
	static CharSequence ¤¤DailyWorkD = "¤Daily Tending is the amount of work needed to keep this pasture functioning. If the workers fail do do the tending, animals will start to die. Resets each day.";
	static CharSequence ¤¤SlaughterAll = "¤Slaughter all";
	static CharSequence ¤¤SlaughterAllDesc = "¤Slaughter all animals and immediately receive some produce?";
	
	static CharSequence ¤¤ProdExp = "¤Production of the current day is based on the work of the previous day. The workers must tend to the animals each day. Failing to tend to all animals for one day will result in lower produce. Failing two days in a row and livestock will die.";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_PASTURE s) {

		super(s);


	}
	
	@Override
	public void hover(GBox box, PastureInstance i) {
		super.hover(box, i);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<PastureInstance> getter, int x1, int y1) {
		
		
		GuiSection s = new GuiSection();
		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, getter.get().animalsCurrent, getter.get().animalsMax);
				//text.s().s().s().add(getter.get().animalsToFetch);
			}
		}.hv(¤¤Animals));
		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, CLAMP.i(getter.get().work, 0, getter.get().workMax), getter.get().workMax);
//				text.s().add('(').add(getter.get().neededWork(getter.get().animalsCurrent+1));
//				text.s().add('(').add(getter.get().animalsToDie);
			}
		}.hv(¤¤DailyWork, ¤¤DailyWorkD));
		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, getter.get().skill());
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤Skill);
				b.text(¤¤SkillD);
				b.NL(8);
				IndustryUtil.hoverBoosts(b, 1, null, getter.get().blueprintI().indus.get(0).bonus(), getter.get());
			};
			
		}.hv(¤¤Skill));
		
		section.addRelBody(8, DIR.S, s);
		
		{
//			s = new GuiSection();
//			
//			
//			s.addDownC(2, new GStat() {
//				
//				@Override
//				public void update(GText text) {
//					text.add(getter.get().needsWork()).s().add('-').s().add(getter.get().hasLivestockFetch());
//					
//				}
//			}.hv("w - f"));
//			
//			s.addDownC(2, new GStat() {
//				
//				@Override
//				public void update(GText text) {
//					text.add(getter.get().missingLivestock).s().add('-').s().add(getter.get().searchForLivestock);
//					
//				}
//			}.hv("m - s"));
//			
//			section.addRelBody(8, DIR.S, s);
		
		}
		RENDEROBJ b = new GButt.ButtPanel(¤¤SlaughterAll) {
			private final ACTION yes = new ACTION() {
				
				@Override
				public void exe() {
					getter.get().slaughterAll();
				}
			};
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(¤¤SlaughterAllDesc, yes, ACTION.NOP, true);
			}
		}.hoverInfoSet(¤¤SlaughterAllDesc);
		
		section.addRelBody(8, DIR.S, b);
		
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}

	public static void industryHoverProductionRate(GBox b, IndustryResource i, RoomInstance ins) {
		PastureInstance ii = (PastureInstance) ins;
		b.NL(8);
		b.text(¤¤ProdExp);
		b.NL(8);
		b.textLL(DicMisc.¤¤Multipliers);
		b.NL();
		
		b.text(¤¤BaseRate);
		b.tab(5);
		b.add(GFORMAT.f(b.text(), i.rate));
		b.NL();
		
		b.text(DicMisc.¤¤Capacity);
		b.tab(5);
		b.add(GFORMAT.f(b.text(), ii.blueprintI().constructor.ferarea.get(ii)*ROOM_PASTURE.WORKERS_PER_TILE));
		b.NL();
		
		b.text(¤¤Skill);
		b.tab(5);
		b.add(GFORMAT.fRel(b.text(), ii.skill(), ii.blueprintI().bonus2.get(HCLASS.CITIZEN, null)));
		b.NL();
		
		b.text(¤¤Animals);
		b.tab(5);
		b.add(GFORMAT.f1(b.text(), ii.animalsCurrent/(double)ii.animalsMax));
		b.NL();
		
		b.text(¤¤FailedTending);
		b.tab(5);
		b.add(GFORMAT.f1(b.text(), Math.max((ii.animalsCurrent-ii.animalsToDie) /(double)ii.animalsCurrent, 0)));
		b.NL();

		b.NL(8);
		double a = ii.animalsCurrent-ii.animalsToDie;
		a /= ii.animalsMax;
		a = Math.max(a, 0);
		a *= ii.blueprintI().constructor.ferarea.get(ii)*ROOM_PASTURE.WORKERS_PER_TILE;
		a *= i.rate*ii.skill();
		b.textL(DicMisc.¤¤Total);
		b.tab(5);
		b.add(GFORMAT.f(b.text(), a));
		b.NL();
		
		
	}
	
	public static double industryFormatProductionRate(GText text, IndustryResource i, RoomInstance ins) {
		text.add('+');
		PastureInstance ii = (PastureInstance) ins;
		double a = ii.animalsCurrent-ii.animalsToDie;
		a /= ii.animalsMax;
		a = Math.max(a, 0);
		a *= ii.blueprintI().constructor.ferarea.get(ii)*ROOM_PASTURE.WORKERS_PER_TILE;
		a *= i.rate*ii.skill();
		GFORMAT.f(text, a);
		return a;
	}


}
