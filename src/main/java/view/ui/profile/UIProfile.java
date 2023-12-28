package view.ui.profile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FBanner;
import init.D;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import view.ui.manage.IFullView;

public final class UIProfile extends IFullView{

	private final CLICKABLE.ClickSwitch switcher;
	private final Level level;	
	private final Bonus bonus;	
	private final Visual visual;
	private final Titles titles;	
	
	public static CharSequence ¤¤Name = "¤Status";
	public static CharSequence ¤¤Desc = "¤A number of settings and information regarding your faction and profile.";
	private static CharSequence ¤¤visuals = "¤Visuals";
	
	static {
		D.ts(UIProfile.class);
	}

	
	public UIProfile() {
		super(¤¤Name);


		section.body().setWidth(WIDTH).setHeight(1);
		
		section.addRelBody(8, DIR.S, picker());
		
		int height = HEIGHT-section.body().height()-16;
		
		level = new Level(height);
		bonus = new Bonus(height);
		visual = new Visual(height);
		titles = new Titles(height);
		
		switcher = new CLICKABLE.ClickSwitch(level);
		switcher.setD(DIR.N);
		
	
		
		section.addRelBody(16, DIR.S, switcher);
		
	}
	
	private GuiSection picker() {
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new GButt.ButtPanel(GAME.player().level().info.name) {
			@Override
			protected void clickA() {
				switcher.set(level);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == level);
			}
		}.setDim(120, 32).hoverSet(GAME.player().level().info));
		
		s.addRightC(0, new GButt.ButtPanel(FACTIONS.player().titles.info.name) {
			@Override
			protected void clickA() {
				switcher.set(titles);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == titles);
				if (!selectedIs() && !hoveredIs() && FACTIONS.player().titles.hasNew()) {
					bg(COLOR.WHITE202WHITE100);
				}else {
					bgClear();
				}
			}
			
			
		}.setDim(120, 32).hoverSet(FACTIONS.player().titles.info));
		
		s.addRightC(0, new GButt.ButtPanel(DicMisc.¤¤Boosts) {
			@Override
			protected void clickA() {
				switcher.set(bonus);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == bonus);
			}
		}.setDim(120, 32).hoverTitleSet(DicMisc.¤¤Boosts));
		
		s.addRightC(0, new GButt.ButtPanel(¤¤visuals) {
			@Override
			protected void clickA() {
				switcher.set(visual);
			}
			
			@Override
			protected void renAction() {
				selectedSet(switcher.current() == visual);
			}
		}.setDim(120, 32).hoverInfoSet(FBanner.name()));
		
		return s;
	}

//	public void activate() {
//		show(VIEW.inters().manager);
//	}
	
//	@Override
//	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
//		return section.hover(mCoo);
//	}
//
//	@Override
//	protected void mouseClick(MButt button) {
//		if (button == MButt.RIGHT)
//			hide();
//		else if (button == MButt.LEFT)
//			section.click();
//	}
//	
//	@Override
//	protected boolean otherClick(MButt button) {
//		hide();
//		return true;
//	}
//
//	@Override
//	protected void hoverTimer(GBox text) {
//		section.hoverInfoGet(text);
//	}
//
//	@Override
//	protected boolean render(Renderer r, float ds) {
//		section.render(r, ds);
//		return true;
//	}
//
//	@Override
//	protected boolean update(float ds) {
//		// TODO Auto-generated method stub
//		return true;
//	}
	
}
