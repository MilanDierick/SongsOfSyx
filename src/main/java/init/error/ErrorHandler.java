package init.error;

import java.io.*;

import game.VERSION;
import init.paths.PATHS;
import snake2d.Errors.DataError;
import snake2d.Errors.GameError;
import snake2d.LOG;
import snake2d.util.file.FileManager;
import snake2d.util.misc.ERROR_HANDLER;
import snake2d.util.process.Proccesser;

public class ErrorHandler implements ERROR_HANDLER {

	private final static String bugMail = "bugs@songsofsyx.com";
	private final static String pgmname = "Songs of syx";
	
	public ErrorHandler() {
		
		
	}


	@Override
	public void handle(String output, String dump) {
		error(null, 2, "Unhandled error output: " + System.lineSeparator() + output, dump, null);
	}

	@Override
	public void handle(DataError e, String dump) {
		error(e, 0, e.error, dump, e.path);
		
	}

	@Override
	public void handle(GameError e, String dump) {
		error(e, 1, e.error, dump, null);
		
	}
	
	@Override
	public void handle(Throwable e, String dump) {
		error(e, 3, e.getClass().getName() + ": " + e.getMessage(), dump, null);
		
	}
	
	private void error(Throwable ee, int type, String message, String dump, String dataPath) {
		
		
		//save data;
		String p = new File("error.txt").getAbsolutePath();
		try {
			p = FileManager.NAME.timeStampString(PATHS.local().LOGS.get() + File.separator + "error") + ".txt";
		}catch(Exception e) {
			
		}
		
		if (type == 3) {
			try {
				if (new File(p).createNewFile()) {
					PrintWriter out = new PrintWriter(p);
					out.println(dump);
					out.close();
					LOG.ln("saved " + p);
				}
			} catch (IOException e1) {
			
				e1.printStackTrace();
			}
		}
		
		
		String dumpFile = p;
		
		
		if (dataPath == null)
			dataPath = "none";
		if (message == null)
			message = "no message";
		if (dumpFile == null)
			dumpFile = "none";
		
		message = message.replaceAll("\"", "Quote");
		if (message.length() > 16000)
			message = message.substring(0, 16000);
		
		String eee = "unhandled " + System.currentTimeMillis();
		if (ee != null)
			eee = VERSION.VERSION_STRING + " " + ee.toString() + " ";
		if (ee != null && ee.getStackTrace().length > 0) {
			eee += ee.getStackTrace()[0].getClassName() + ":" + ee.getStackTrace()[0].getLineNumber();
		}
		
		String[] args = new String[] {
			pgmname,
			bugMail,
			"" + type,
			message,
			dumpFile,
			dataPath,
			eee
		};
		
		Proccesser.exec(ErrorMessage.class, new String[] {}, args);
		
	}

	
}
