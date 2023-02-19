package game;

import java.io.IOException;
import java.nio.file.Path;

import init.RES;
import init.paths.PATHS;
import snake2d.CORE;
import snake2d.util.color.COLOR;
import snake2d.util.file.FilePutter;
import util.save.SaveFile;
import util.save.SaveGame;
import view.main.VIEW;

public class GameSaver {

	private double timeOfLastSave;
	
	public GameSaver(){
		timeOfLastSave = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
	}
	
	public Path save(String saveFolder) throws IOException{
		
		RES.loader().init();
		RES.loader().print("Saving the world...");
		//while(RES.pathFinder().isMakingPaths());
		FilePutter fp;
		Path path;
		try {
			path = init.paths.PATHS.local().SAVE.create(saveFolder);
			fp = new FilePutter(path, (1<<26));
			SaveGame b = new SaveGame();
			fp.object(b);
			GAME.save(fp);
			VIEW.saver().save(fp);
			CORE.checkIn();
			if (init.paths.PATHS.local().SAVE.exists(saveFolder)) {
				init.paths.PATHS.local().SAVE.delete(saveFolder);
			}
			fp.i(b.check);
			CORE.checkIn();
			fp.zip();
			timeOfLastSave = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
			fp = null;
			
		} catch (IOException e) {
			e.printStackTrace();
			PATHS.local().SAVE.delete(saveFolder);
			throw e;
			
		}
		System.gc();
		CORE.getInput().clearAllInput();
		return path;
	}
	

	
	public double getTimeSinceLastSave(){
		return CORE.getUpdateInfo().getSecondsSinceFirstUpdate() - timeOfLastSave;
	}
	
	public void quicksave() {
		if (!VIEW.canSave())
			return;
		try {
			
			String path = SaveFile.stamp("QuickSave");
			new GameSaver().save(path);
			for (String s : PATHS.local().SAVE.getFiles()) {
				SaveFile f = new SaveFile(s);
				if (f.name.equals("QuickSave") && !s.contains(path)) {
					PATHS.local().SAVE.delete(s);
				}
			}
			
			if (!RES.loader().isMini())
				VIEW.inters().fullScreen.activate("Game Saved!", COLOR.WHITE100, null);
			GAME.saveReset();
			timeOfLastSave = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
		} catch (IOException e) {
			VIEW.inters().fullScreen.activate("Unable to save! See log for details", COLOR.RED100, null);
			e.printStackTrace();
		}
	}
	
}
