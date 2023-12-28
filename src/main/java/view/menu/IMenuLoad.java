package view.menu;

import game.GameLoader;
import init.C;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollable;
import util.save.SaveFile;
import util.save.SaveGame;
import view.main.VIEW;

class IMenuLoad extends GuiSection{
	

	private final CLICKABLE load;
	private final CLICKABLE delete;
	
	private SaveFile[] saves = new SaveFile[0];
	private int selectedSave = -1;
	
	private CharSequence problem;
	
	IMenuLoad(IMenu m) {
		
		Screener sc = new Screener(DicMisc.¤¤load, GCOLOR.T().H1) {
			
			@Override
			protected void back() {
				m.setMain();
			}
		};
		add(sc);
		
		SaveEntry[] entries = new SaveEntry[] {
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
			new SaveEntry(),
		};
		
		GScrollable scroll = new GScrollable(entries) {
			
			@Override
			public int nrOFEntries() {
				return saves.length;
			}
		};
		
		scroll.getView().body().centerIn(C.DIM());
		add(scroll.getView());
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				if (problem == null && selectedSave > 0) {
					problem = saves[selectedSave].problem();
				}
				if (problem != null)
					text.add(problem);
				text.errorify();
				problem = null;
			}
		}.increase().r(DIR.C), C.DIM().cX(), getLastY2()+16);
		
		
		load = new Screener.ScreenButton(DicMisc.¤¤load) {
			@Override
			protected void renAction() {
				activeSet(selectedSave != -1);
			}
		};
		load.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				CharSequence p = SaveGame.problem(PATHS.local().SAVE.get(saves[selectedSave].fullName), false);
				if (p != null) {
					VIEW.inters().fullScreen.activate(p, COLOR.WHITE100, null);
					return;
				}
				
				new GameLoader(PATHS.local().SAVE.get(saves[selectedSave].fullName)).set();
			}
			
		
		});
		
		sc.addButt(load);
		
		
		//delete
		
		GButt yes = new GButt.Glow(UI.FONT().H2.getText(DicMisc.¤¤confirm));
		yes.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				VIEW.inters().fullScreen.activate(saves[selectedSave].name + " deleted!", COLOR.WHITE100, null);
				PATHS.local().SAVE.delete(saves[selectedSave].fullName);
				populateSaves();
			}
		});
		GButt no = new GButt.Glow(UI.FONT().H2.getText(DicMisc.¤¤cancel));
		
		delete = new Screener.ScreenButton(DicMisc.¤¤delete) {
			@Override
			protected void renAction() {
				selectedSet(selectedSave != -1);
			}
		};
		delete.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				VIEW.inters().fullScreen.activate("delete \"" + saves[selectedSave].name + "\"?", COLOR.WHITE100, null, yes, no);
			}
		});
		sc.addButt(delete);

		
		populateSaves();
		
	}
	
	public void init() {
		populateSaves();
		selectedSave = -1;
	}
	
	private void populateSaves(){
		
		saves = SaveFile.list();
		
		selectedSave = -1;
		load.activeSet(false);
		delete.activeSet(false);
		
	}
	
	private class SaveEntry extends Savebutt{

		
		public SaveEntry() {
			
		}
		
		@Override
		protected void clickA() {
			selectedSave = index;
			load.activeSet(true);
			delete.activeSet(true);
			if (MButt.LEFT.isDouble()) {
				CharSequence p = SaveGame.problem(PATHS.local().SAVE.get(saves[selectedSave].fullName), false);
				if (p != null) {
					VIEW.inters().fullScreen.activate(p, COLOR.WHITE100, null);
					return;
				}
				new GameLoader(PATHS.local().SAVE.get(saves[selectedSave].fullName)).set();
			}
		}

		@Override
		protected boolean selected(int index) {
			return selectedSave == index;
		}

		@Override
		protected SaveFile save(int index) {
			if (index < 0 || index >= saves.length)
				return null;
			return saves[index];
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			super.hover(mCoo);
			if (super.hoveredIs()) {
				problem = saves[index].problem();
				return true;
			}
			return false;
		}
		
	}
	
}
