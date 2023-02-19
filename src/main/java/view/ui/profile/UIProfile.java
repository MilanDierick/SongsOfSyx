package view.ui.profile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FBanner;
import init.C;
import init.D;
import init.boostable.BOOSTABLES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanelL;
import view.interrupter.Interrupter;
import view.main.VIEW;

public final class UIProfile extends Interrupter{

	private final CLICKABLE.Switcher switcher;
	private final Level level;	
	private final Bonus bonus;	
	private final Visual visual;
	private final Titles titles;	
	
	public final CharSequence ¤¤Name = "¤Status";
	public final CharSequence ¤¤Desc = "¤A number of settings and information regarding your faction and profile.";

	private final GuiSection section;
	
	public UIProfile() {
		GPanelL pan = new GPanelL();
		pan.body.setDim(800, 600);
		D.t(this);
		pan.setTitle(¤¤Name);
		pan.setCloseAction(new ACTION() {
			
			@Override
			public void exe() {
				hide();
			}
		});
		
		pan.body().centerIn(C.DIM());
		
		section = new GuiSection();
		section.add(pan);
		
		GuiSection s = picker();
		s.body().moveY1(pan.getInnerArea().y1());
		s.body().centerX(pan);
		section.add(s);
		
		int height = pan.getInnerArea().y2()-s.body().y2()-32;
		
		level = new Level(height);
		bonus = new Bonus(height);
		visual = new Visual(height);
		titles = new Titles();
		
		level.body().moveY1(s.body().y2()+16);
		level.body().centerX(s);
		
		switcher = new CLICKABLE.Switcher(level);
		
	
		
		section.add(switcher);
		
	}
	
	private GuiSection picker() {
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new GButt.ButtPanel(GAME.player().level().info.name) {
			@Override
			protected void clickA() {
				switcher.set(level, DIR.N);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.get() == level);
			}
		}.setDim(120, 32).hoverSet(GAME.player().level().info));
		
		s.addRightC(0, new GButt.ButtPanel(FACTIONS.player().titles.info.name) {
			@Override
			protected void clickA() {
				switcher.set(titles, DIR.N);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.get() == titles);
				if (!selectedIs() && !hoveredIs() && FACTIONS.player().titles.hasNew()) {
					bg(COLOR.WHITE202WHITE100);
				}else {
					bgClear();
				}
			}
			
			
		}.setDim(120, 32).hoverSet(FACTIONS.player().titles.info));
		
		s.addRightC(0, new GButt.ButtPanel(BOOSTABLES.INFO().name) {
			@Override
			protected void clickA() {
				switcher.set(bonus, DIR.N);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.get() == bonus);
			}
		}.setDim(120, 32).hoverSet(BOOSTABLES.INFO()));
		
		s.addRightC(0, new GButt.ButtPanel(D.g("Visuals")) {
			@Override
			protected void clickA() {
				switcher.set(visual, DIR.N);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.get() == visual);
			}
		}.setDim(120, 32).hoverInfoSet(FBanner.name()));
		
		return s;
	}

	public void activate() {
		show(VIEW.inters().manager);
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.RIGHT)
			hide();
		else if (button == MButt.LEFT)
			section.click();
	}
	
	@Override
	protected boolean otherClick(MButt button) {
		hide();
		return true;
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		// TODO Auto-generated method stub
		return true;
	}
	
}
