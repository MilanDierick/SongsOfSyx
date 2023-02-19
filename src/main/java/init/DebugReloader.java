package init;

import java.io.IOException;
import java.nio.file.Path;

import game.GameLoader;
import game.GameSaver;
import snake2d.CORE;
import snake2d.util.misc.ACTION;
import view.interrupter.IDebugPanel;

final class DebugReloader{

	
	public DebugReloader() {
		IDebugPanel.add("Reload Assets", new ACTION() {
			
			@Override
			public void exe() {
				try {
					Path p = new GameSaver().save("debugReload");
					CORE.setCurrentState(new GameLoader(p));
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
			}
			
		});
	}

	


}
