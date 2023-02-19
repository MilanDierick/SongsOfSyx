package game;

import java.io.IOException;

import init.RES;
import init.paths.PATHS;
import init.settings.S;
import snake2d.CORE;
import snake2d.util.file.*;
import util.dic.DicMisc;
import util.save.SaveFile;
import view.main.VIEW;

final class AutoSaver implements SAVABLE{

	private long last = -1;
	private long count = 0;
	private int fileI = 0;
	
	AutoSaver(){
		
	}
	
	void autosave(double ds) {
		
		
		if (S.get().autoSaveInterval.get() > 0 && VIEW.canSave()) {
			if (ds != 0) {
				if (last != -1) {
					count += CORE.getUpdateInfo().getNowMillis() - last;
				}
				last = CORE.getUpdateInfo().getNowMillis();
				
				long time = 1 + 2*(S.get().autoSaveInterval.max() - S.get().autoSaveInterval.get());
				time*= 1000*60;
				
				if (count >= time && VIEW.current().uiManager.isGoodTimeToSave()) {
					save();
					count = 0;
				}
			}
			
			
		}else {
			count = 0;
		}
	}
	
	private void save() {
		fileI += 1;
		fileI = fileI % S.get().autoSaveFiles.get();
		RES.loader().minify(true, DicMisc.¤¤SAVING);
		RES.loader().print("AutoSaving...");
		String sname = "AutoSave" + fileI;
		String name = SaveFile.stamp(sname);
		for (String s : PATHS.local().SAVE.getFiles()) {
			if (new SaveFile(s).name.equals(sname)){
				PATHS.local().SAVE.delete(s);
			}
		}
		
		try {
			new GameSaver().save(name);
			
		} catch (IOException e) {
			
		}
		RES.loader().minify(false, null);
	}
	
	public void saveNew() {
		String sname = "A New Beginning";
		String name = SaveFile.stamp(sname);
		for (String s : PATHS.local().SAVE.getFiles()) {
			if (new SaveFile(s).name.equals(sname)){
				PATHS.local().SAVE.delete(s);
			}
		}
		try {
			new GameSaver().save(name);
		} catch (IOException e) {
			
		}
	}

	@Override
	public void save(FilePutter file) {
		file.i(fileI);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		fileI = file.i();
	}

	@Override
	public void clear() {
		fileI = 0;
		last = -1;
		count = 0;
	}


	
}
