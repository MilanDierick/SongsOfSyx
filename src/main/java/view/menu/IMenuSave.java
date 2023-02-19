package view.menu;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import init.*;
import init.paths.PATHS;
import menu.screens.Screener;
import snake2d.MButt;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import util.gui.table.GScrollable;
import util.save.SaveFile;
import view.main.VIEW;

class IMenuSave extends GuiSection implements STRING_RECIEVER{
	
	private final CLICKABLE overwrite;
	private final CLICKABLE delete;
	private ACTION successfullAction;
	
	private SaveFile[] saves = new SaveFile[0];
	
	private int selectedSave = -1;
	
	private final ACTION overwriteAction;
	
	private static CharSequence ¤¤¤nameYour = "¤Name your save-game";
	private static CharSequence ¤¤failed = "¤failed to be overwritten";
	private static CharSequence ¤¤success = "¤successfully overwritten";
	private static CharSequence ¤¤overwrite = "¤overwrite";
	private static CharSequence ¤¤successSave = "{0} successfully saved!";
	private static CharSequence ¤¤charsAllowed = "Only characters: {0} are allowed!";
	private static CharSequence ¤¤fail = "Save failed. See error report!";
	
	static {
		D.ts(IMenuSave.class);
	}
	
	IMenuSave(IMenu m, Font font, Font small, ACTION successfullAction) {
		
		
		Screener sc = new Screener(DicMisc.¤¤save, GCOLOR.T().H1) {
			
			@Override
			protected void back() {
				m.setMain();
			}
		};
		
		this.successfullAction = successfullAction;
		
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

		
		//NEW
		CLICKABLE newButt = new Screener.ScreenButton(DicMisc.¤¤new) {
			@Override
			protected void clickA() {
				String name = FACTIONS.player().appearence().name() + "-";
				KeyMap<String> m = new KeyMap<>();
				for (SaveFile f : saves) {
					if (f.name.startsWith(name)) {
						String n = f.name.substring(name.length(), f.name.length());
						m.put(n, n);
					}
				}
				
				String ph = "";
				
				for (int i = 0; i < 512; i++) {
					String k = ""+i;
					if (!m.containsKey(k)) {
						ph = name + k;
						break;
					}
				}
				
				
				VIEW.inters().input.requestInput(IMenuSave.this, ¤¤¤nameYour, ph);
			}
		};
		sc.addButt(newButt);


		//OVERWRITE
		GButt yes = new GButt.Glow(DicMisc.¤¤confirm);
		yes.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				
				try {
					PATHS.local().SAVE.delete(saves[selectedSave].fullName);
					RES.saver().save(SaveFile.stamp(saves[selectedSave].name));
				} catch (IOException e) {
					e.printStackTrace();
					VIEW.inters().fullScreen.activate(saves[selectedSave].name + " " + ¤¤failed, COLOR.RED100, null);
					return;
				}
				VIEW.inters().fullScreen.activate(saves[selectedSave].name + " " + ¤¤success, COLOR.WHITE100, successfullAction);
				m.setMain();
			}
		});
		GButt no = new GButt.Glow(DicMisc.¤¤cancel);
		
		overwriteAction = new ACTION() {
			@Override
			public void exe() {
				VIEW.inters().fullScreen.activate(¤¤overwrite + " " + saves[selectedSave].name + "?", COLOR.WHITE100, null, yes, no);
			}
		};
		overwrite = new Screener.ScreenButton(¤¤overwrite) {
			
			@Override
			protected void renAction() {
				activeSet(selectedSave != -1);
			}
			
		};
		overwrite.clickActionSet(overwriteAction);
		sc.addButt(overwrite);
		
		//DELETE
		GButt yes2 = new GButt.Glow(DicMisc.¤¤confirm);
		yes2.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				PATHS.local().SAVE.delete(saves[selectedSave].fullName);
				VIEW.inters().fullScreen.activate(saves[selectedSave].name + " deleted!", COLOR.WHITE100, null);
				populateSaves();
			}
		});
		
		delete = new Screener.ScreenButton(DicMisc.¤¤delete) {
			@Override
			protected void clickA() {
				VIEW.inters().fullScreen.activate(DicMisc.¤¤delete + " " + saves[selectedSave].name, COLOR.WHITE100, null, yes2, no);
			}
			
			@Override
			protected void renAction() {
				activeSet(selectedSave != -1);
			}
		};
		sc.addButt(delete);
		
		
		
		add(sc);
		moveLastToBack();
		
		populateSaves();
		
		
	}
	
	private void populateSaves(){
		
		String[] ss = PATHS.local().SAVE.getFiles();
		
		saves = new SaveFile[ss.length];
		for (int i = 0; i < ss.length; i++) {
			saves[i] = new SaveFile(ss[i]);
		}
		Arrays.sort(saves);

		selectedSave = -1;
		overwrite.activeSet(false);
		delete.activeSet(false);
		
	}

	@Override
	public void acceptString(CharSequence string) {
		
		if (string == null)
			return;
		
		if (!FileManager.NAME.okName(string)){
			Str.TMP.clear().add(¤¤charsAllowed).insert(0,  FileManager.NAME.legalChars);
			VIEW.inters().fullScreen.activate(Str.TMP, COLOR.RED100, null);
			return;
		}
		
		for (int i = 0; i < saves.length; i++) {
			if ((""+ saves[i].name).contentEquals(string)) {
				selectedSave = i;
				overwriteAction.exe();
				return ;
			}
		}
		
		try {
			RES.saver().save(SaveFile.stamp(string));
		} catch (IOException e) {
			e.printStackTrace();
			VIEW.inters().fullScreen.activate(¤¤fail, COLOR.RED100, null);
			return;
		}
		
		
		VIEW.inters().menu.setMain();
		Str.TMP.clear().add(¤¤successSave).insert(0, string);
		VIEW.inters().fullScreen.activate(Str.TMP, COLOR.WHITE100, successfullAction);
	}
	
	private class SaveEntry extends Savebutt {
		
		public SaveEntry() {
			
		}

		@Override
		protected void clickA() {
			selectedSave = index;
			overwrite.activeSet(true);
			delete.activeSet(true);
			if (MButt.LEFT.isDouble()) {
				overwriteAction.exe();
			}
		}

		@Override
		protected boolean selected(int index) {
			return index == selectedSave;
		}

		@Override
		protected SaveFile save(int index) {
			return saves[index];
		}
		
	}
	
}
