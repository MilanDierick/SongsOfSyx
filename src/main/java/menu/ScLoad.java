package menu;

import static menu.GUI.*;

import game.GameLoader;
import game.VERSION;
import init.C;
import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.screens.Screener;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollable;
import util.info.GFORMAT;
import util.save.SaveFile;

class ScLoad implements SCREEN{

	private final GuiSection main;
	private final GuiSection prompt;
	private GuiSection current;
	private final CLICKABLE load;
	private final CLICKABLE delete;
	private final Loader loader;
	private final GText error = new GText(UI.FONT().M, 128).color(COLOR.YELLOW100);
	private final int eY1;
	private SaveFile[] saves = new SaveFile[0];
	private int selectedSave = -1;
	private final Menu menu;
	static CharSequence ¤¤name = "¤load";
	static CharSequence ¤¤delete = "¤Delete Save?";
	
	ScLoad(Menu menu) {
		
		D.t(this);
		
		loader = new Loader(menu);
		this.menu = menu;
		main = new GuiSection();
		load = new Screener.ScreenButton(DicMisc.¤¤load);
		load.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (selectedSave >= 0) {
					loader.load(PATHS.local().SAVE.get(saves[selectedSave].fullName));
				}
				
			}
		});
		load.activeSet(false);
		main.add(load);
		
		delete = new Screener.ScreenButton(DicMisc.¤¤delete);
		delete.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (selectedSave >= 0)
					current = prompt;
			}
		});
		delete.activeSet(false);
		main.addRightC(bottomMarginX, delete);
		
		main.body().centerX(bounds.x1(), bounds.x2());
		main.body().moveY1(bottomY);
		
		
		
		ScrollRow[] butts = new ScrollRow[] {
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
		};
		
		Scrollable s = new GScrollable(butts) {
			
			@Override
			public int nrOFEntries() {
				return saves.length;
			}
		};
		
		s.getView().body().centerIn(bounds);
		main.add(s.getView());
		
		eY1 = main.getLastY2()+8;
		
		Screener scr = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		scr.addButt(load);
		scr.addButt(delete);
		
		main.add(scr);
		main.moveLastToBack();
		

		
		prompt = new GuiSection();
		addTitleText(prompt, ¤¤delete);
		prompt.addDownC(16, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(saves[selectedSave].name);
			}
		}.increase().r(DIR.C));
		CLICKABLE yes = getNavButt(DicMisc.¤¤Yes);
		yes.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (selectedSave >= 0) {
					PATHS.local().SAVE.delete(saves[selectedSave].fullName);
					selectedSave = -1;
					delete.activeSet(false);
					populateSaves();
				}
				current = main;
			}
		});
		
		CLICKABLE no = getNavButt(DicMisc.¤¤No);
		no.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				current = main;
			}
		});
		
		yes.body().centerIn(bounds);
		yes.body().incrX(-(yes.body().width() + no.body().width() + 32)/2);
		yes.body().moveY1(prompt.body().y2()+64);
		
		prompt.add(yes);
		prompt.addRight(64+32, no);
		prompt.body().centerIn(C.DIM());
		current = main;
		
		populateSaves();
		
	}
	
	private void populateSaves() {
		saves = SaveFile.list();
		delete.activeSet(false);
		selectedSave = -1;
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (current.hover(mCoo))
			return true;
		return false;
	}

	@Override
	public boolean click() {
		current.click();
		return false;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		current.render(r, ds);
		
		if (current == main && error.length() > 0) {
			error.renderC(r, C.DIM().cX(), eY1+16);
		}
		
		error.clear();
		if (selectedSave >= 0 && saves[selectedSave].problem() != null) {
			error.set(saves[selectedSave].problem());
		}
		
		
	}
	
	private static Str version = new Str(16);
	private GText tmp = new GText(UI.FONT().H2, 64);
	
	private class Savebutt extends CLICKABLE.ClickableAbs implements ScrollRow {

		int index = -1;
		
		public Savebutt() {
			body.setWidth(1000);
			body.setHeight(28);
		}
		
		@Override
		public void init(int index) {
			this.index = index;
		}
		
		private Font font() {
			return UI.FONT().H2;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			SaveFile s = saves[index];
			version.clear();
			version.add(VERSION.versionMajor(s.version));
			version.add('.');
			version.add(VERSION.versionMinor(s.version));
			if (s.problem() != null) {
				COLOR.YELLOW100.bind();
			}else {
				if (index == selectedSave) {
					COLORS.selected.bind();
				}else if (isHovered){
					COLORS.hover.bind();
				}
			}
			font().render(r, version, body().x1(), body().y1());
			
			if (index == selectedSave) {
				COLORS.selected.bind();
			}else if (isHovered){
				COLORS.hover.bind();
			}
			
			
			
			font().render(r, s.name, body().x1() + 60, body().y1());

			tmp.clear().add('p').s();
			GFORMAT.i(tmp, s.pop);
			font().render(r, tmp, body().x1() + 740, body().y1());
			
			font().render(r, s.ago, body().x1() + 820, body().y1());
			COLOR.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				SaveFile s = saves[index];
				if (s.problem() != null)
					error.clear().set(s.problem());
				return true;
			}
			return false;
		}
		
		@Override
		protected void clickA() {
			if (MButt.LEFT.isDouble()) {
				loader.load(PATHS.local().SAVE.get(saves[index].fullName));
			}else {
				selectedSave = index;
				load.activeSet(true);
				delete.activeSet(true);
			}
			
			
		}
		
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
	public boolean hasSaves() {
		return saves.length != 0;
	}
	
	public void loadSave() {
		if (hasSaves()) {
			menu.start(new GameLoader(PATHS.local().SAVE.get(saves[0].fullName)));
		}
	}
	
}
