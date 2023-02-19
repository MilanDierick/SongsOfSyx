package view.sett.ui.room.construction;

import init.C;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.gui.panel.GPanelS;
import view.main.VIEW;

final class SFrame {

	private int width = C.SG*470;
	private final State state;
	private final GPanelS panel = new GPanelS();
	private GuiSection section = new GuiSection();
	private final Str title = new Str(50);
	private final GuiSection bottomButtons = new GuiSection();
	{
		D.gInit(this);
	}
	private CharSequence sconstruction = D.g("{0} construction");
	private CharSequence sexpensive = D.g("expensive", "The layout of the room will make it more expensive to construct and maintain. The yellow squares denote where support for the room is weak and will need extra materials. To increase support, remove some of the room in this area, so that it can be used to build supportive walls. Proceed anyway?");
	private boolean message = false;

	SFrame(State state){
		this.state = state;
	}
	
	{
		bottomButtons.add(UI.PANEL().panelL.get(DIR.E), 0, 0);
		bottomButtons.addRight(0, UI.PANEL().panelL.get(DIR.E, DIR.W));
		bottomButtons.addRight(0, UI.PANEL().panelL.get(DIR.E, DIR.W));
		bottomButtons.addRight(0, UI.PANEL().panelL.get(DIR.E, DIR.W));
		bottomButtons.addRight(0, UI.PANEL().panelL.get(DIR.W));
		
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new GButt.Panel(SPRITES.icons().m.trash, D.g("removeRoom", "remove room")) {
			
			@Override
			protected void clickA() {
				state.config.build = false;
				VIEW.s().tools.placer.deactivate();
			};				
		});
		
		s.addRightC(16, new GButt.Panel(SPRITES.icons().m.arrow_left, D.g("undo")) {
			
			@Override
			protected void clickA() {
				SETT.ROOMS().placement.placer.popHistory();
			};
			
			@Override
			protected void renAction() {
				activeSet(SETT.ROOMS().placement.placer.hasHistory());
			}
			
		});

		ACTION create = new ACTION() {
			
			@Override
			public void exe() {
				SETT.ROOMS().placement.placer.create();
//				if (c != null && SETT.ROOMS().map.get(c) != null)
//					SETT.ROOMS().map.get(c).upgradeSet(state.upgrade[state.b.index()]);
				VIEW.s().tools.placer.deactivate();
			}
		};
		
		s.addRightC(0, new GButt.Panel(SPRITES.icons().m.ok, D.g("construct!")) {
			
			@Override
			protected void clickA() {
				CharSequence s = SETT.ROOMS().placement.placer.createProblem();
				if (s != null) {
					if (SETT.ROOMS().placement.placer.createProblemItem() != null) {
						state.problemGroup = SETT.ROOMS().placement.placer.createProblemItem();
						state.problemTimer = VIEW.renderSecond() + 4;
					}if (SETT.ROOMS().placement.placer.createProblemWalls()) {
						state.problemTimer = VIEW.renderSecond() + 4;
						state.problemneedDoor = true;
					}
					VIEW.mouseBox(true).error(s);
				}else {
					CharSequence warn = SETT.ROOMS().placement.placer.createWarning();
					if (warn != null) {
						VIEW.inters().yesNo.activate(warn, create, ACTION.NOP, true);
					}else {
						if (!message && state.b.constructor().mustBeIndoors() && state.placement.placer.extraExpense() > 0 ) {
							message = true;
							VIEW.inters().yesNo.activate(sexpensive, create, ACTION.NOP, true);
							
						}else
							create.exe();
					}
				}
				
			};
			
		});
		
		s.body().centerIn(bottomButtons);
		
		bottomButtons.add(s);
	}
	
	GuiSection get(GuiSection s) {
		section.clear();
		if (state.collection != null)
			title.clear().add(state.collection.name());
		else
			title.clear().add(sconstruction).insert(0, state.b.info.name);
		title.toUpper();
		

		if (s.body().width() < width)
			s.pad((width-s.body().width())/2, 0);
		panel.inner().set(s);
		panel.setTitle(title);
		panel.titleCenter();
		section.add(panel);
		s.body().centerIn(panel.inner());
		section.add(s);
		
		if (state.b.constructor().usesArea()) {
			bottomButtons.body().moveX2(section.body().x2()-60);
			bottomButtons.body().moveCY(section.body().y2());
			section.add(bottomButtons);
		}
		
		
		return section;
	}
	
}
