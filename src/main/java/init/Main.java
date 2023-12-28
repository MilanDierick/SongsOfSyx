package init;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import game.VERSION;
import init.paths.PATHS;
import launcher.LSettings;
import launcher.Launcher;
import snake2d.LOG;
import snake2d.util.process.Proccesser;
import snake2d.util.sets.LIST;

public class Main {


	
	public static void main(String[] args) {
	
		try {
			
			
			if (args != null && args.length > 0 && args[0].equalsIgnoreCase("launcher")) {
				
				LOG.ln("*************************************");
				LOG.ln("* LAUNCHER " + VERSION.VERSION_STRING);
				LOG.ln("*************************************");

				Process p = Proccesser.executeLwjgl(Launcher.class, new String[] {}, new String[] {}, new String[] {});
				
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
			LOG.ln("*************************************");
			LOG.ln("* STARTING "  + VERSION.VERSION_STRING);
			LOG.ln("*************************************");
			
			LSettings s = new LSettings();

			PATHS.init(s.mods.get(), null, false);
			
			LIST<String> jars = PATHS.SCRIPT().modClasspaths();
			String[] cps = new String[jars.size()];
			
			for (int i = 0; i < jars.size(); i++) {
				cps[i] = jars.get(i);
			}
			Proccesser.executeLwjgl(MainProcess.class, s.jvmArguments.get(), new String[] {}, cps);
			
		}catch(Exception e) {
			e.printStackTrace();
			
			try {
				PrintWriter writer = new PrintWriter("SEVERE_ERROR.txt", "UTF-8");
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime());
				writer.println(timeStamp);
				e.printStackTrace(writer);
				writer.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		
	}

}
