package menu;

import java.util.Arrays;

import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.screens.Screener;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Font;
import util.gui.table.GScrollable;
import util.save.SaveFile;

class ScExamples implements SCREEN{

	private final GuiSection main;
	private GuiSection current;
	private final CLICKABLE load;
	
	private SaveFile[] saves = new SaveFile[0];
	private int selectedSave = -1;
	private final Loader loader;
	
	final CharSequence ¤¤name = "¤examples";
	
	ScExamples(Menu menu) {
		
		loader = new Loader(menu);
		
		D.t(this);
		
		Screener ss = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		main = new GuiSection();
		load = new Screener.ScreenButton(D.g("load"));
		load.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				loader.load(PATHS.MISC().EXAMPLES.get(saves[selectedSave].fullName), false);
			}
		});
		load.activeSet(false);
		ss.addButt(load);
		
		main.add(ss);
		
		
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
		
		s.getView().body().centerIn(main.body());
		main.add(s.getView());
		

		
		
		
		current = main;
		
		populateSaves();
		
	}
	
	private void populateSaves() {
		String[] ss = PATHS.MISC().EXAMPLES.getFiles();
		
		saves = new SaveFile[ss.length];
		for (int i = 0; i < ss.length; i++) {
			saves[i] = new SaveFile(ss[i]);
		}
		Arrays.sort(saves);

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
		
	}
	
	private class Savebutt extends CLICKABLE.ClickableAbs implements ScrollRow {

		int index = -1;
		
		public Savebutt() {
			body.setWidth(300);
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
			if (index == selectedSave) {
				COLORS.selected.bind();
			}else if (isHovered){
				COLORS.hover.bind();
			}
			SaveFile s = saves[index];
			font().render(r, s.name, body().x1(), body().y1());
			COLOR.unbind();
		}
		
		@Override
		protected void clickA() {
			selectedSave = index;
			load.activeSet(true);
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
	
}
