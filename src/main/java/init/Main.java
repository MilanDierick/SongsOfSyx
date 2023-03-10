package init;

import game.VERSION;
import init.error.ErrorHandler;
import init.paths.PATHS;
import init.paths.PATHS.PATHS_BASE;
//import integrations.INTEGRATIONS;
import launcher.LSettings;
import launcher.Launcher;
import menu.Menu;
import snake2d.*;
import snake2d.util.process.Proccesser;

public class Main {

	public static void main(String[] args) {

		try {
			PreLoader.load(VERSION.VERSION_STRING, PATHS_BASE.PRELOADER, PATHS_BASE.ICON_FOLDER + "Icon64.png");
		}catch(Exception e) {
			PreLoader.exit();
			e.printStackTrace();
			new ErrorHandler().handle(e, e.toString());
			return;
		}
		
		if (args != null && args.length > 0 && args[0].equalsIgnoreCase("launcher")) {
			
			LOG.ln("*************************************");
			LOG.ln("* LAUNCHER " + VERSION.VERSION_STRING);
			LOG.ln("*************************************");

			Process p = Proccesser.executeLwjgl(Launcher.class, new String[] {}, new String[] {});
			PreLoader.exit();
			if (p != null) {
				while (p.isAlive())
					try {
						Thread.sleep(0);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				if (p.exitValue() != 0)
					return;
			}
		}

		LOG.ln("*******************************");
		LOG.ln("* GAME " + VERSION.VERSION_STRING);
		LOG.ln("*******************************");

		PreLoader.load(VERSION.VERSION_STRING, PATHS_BASE.PRELOADER, PATHS_BASE.ICON_FOLDER + "Icon64.png");
	
		CORE.init(new ErrorHandler());
		

		LSettings s = new LSettings();
				
		String l = s.lang;
		PATHS.init(s.mods(), l != null && l.length() > 0 ? l : null, s.easy.get() == 1);

		D.init();
//		if (s.rpc.get() == 1) {
//			INTEGRATIONS.init(false, false);
//		}

		// PreLoader.exit();
		Menu.start();
//		INTEGRATIONS.dispose();
		
	}

}
