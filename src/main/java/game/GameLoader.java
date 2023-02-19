package game;

import java.nio.file.Path;

import init.paths.PATHS;
import script.ScriptLoad;
import snake2d.*;
import snake2d.CORE_STATE.Constructor;
import snake2d.util.file.FileGetter;
import util.save.SaveFile;
import util.save.SaveGame;
import view.main.VIEW;

public class GameLoader implements Constructor {

	private final Path saveFile;
	private final String scriptFile;
	private boolean achiving = true;
	
	public GameLoader(java.nio.file.Path path){
		this(path, null);
	}
	
	public GameLoader(java.nio.file.Path path, boolean achiveing){
		this(path, null);
		this.achiving = achiveing;
	}
	
	public GameLoader(java.nio.file.Path path, String script){
		saveFile = path;
		scriptFile = script;
	}
	

	
	@Override
	public CORE_STATE getState() {
		FileGetter fg = null;
		String error = "Savegame is not compatible with current version. This could be the game was saved with a lower version of the game, or with lower version mods. Older versions of the game are usually available to check out in your store should you wish to continue this save.";
		
		SaveGame b = null;
		
		LOG.ln("LOADING GAME", "Game version: " + VERSION.VERSION_STRING);

		try {
			fg = new FileGetter(saveFile, true, ScriptLoad.getLoader());
			b = (SaveGame) fg.object();
			
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new Errors.GameError("Save is corrupted and can not be loaded!");
		}
		
		LOG.ln("Save Version: " + VERSION.versionString(b.version));
		LOG.ln("mod: " + b.modHash);
		
		CharSequence p = b.problem(true);
		
		try {
			new GAME(fg, scriptFile);
		} catch(Errors.GameError ee) {
			
			throw ee;
		}catch (Exception e) {
			e.printStackTrace(System.out);
			throw new Errors.GameError("Save is corrupted and can not be loaded! " + p);
		} 
		
		
		VIEW v = new VIEW();
		
		try {
			v.saver.load(fg);
			if (fg.i() != b.check)
				throw new Errors.DataError(error, ""+saveFile);
			
			fg.close();
			CORE.getInput().clearAllInput();
			VIEW.inters().load.activate();
			GAME.achieve(achiving);
			return v;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		

		
		
		
	}
	
	public final void set() {
		CORE.setCurrentState(this);
	}
	
	public static boolean quickload() {
		SaveFile ff = null;
		for (String s : PATHS.local().SAVE.getFiles()) {
			SaveFile f = new SaveFile(s);
			if (ff == null || f.t > ff.t)
				ff = f;
		}
		
		if (ff != null) {
			CORE.setCurrentState(new GameLoader(PATHS.local().SAVE.get(ff.fullName)));
			return true;
		}
		return false;
	}
	
	
}