package snake2d;

import snake2d.util.process.Proccesser;

public final class PreLoader {

	private static Process preloader;
	
	public static void load(String version, String pathToImage, String pathToIcon) {
		exit();
		preloader = Proccesser.exec(PreLoaderSwing.class, new String[] {}, new String[] {version, pathToImage, pathToIcon});
	}
	
	public static void exit() {
		
		
		
		Process p = preloader;
		preloader = null;
		if (p == null)
			return;
		
		
		if (p.isAlive()) {
			try {
				p.getOutputStream().write('s');
				p.destroy();
				//p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		preloader = null;
		
		
	}

}
