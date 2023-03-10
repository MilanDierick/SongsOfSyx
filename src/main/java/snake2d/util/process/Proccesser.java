package snake2d.util.process;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Locale;

public final class Proccesser {

	private Proccesser() {
	}

	public static Process executeLwjgl(Class<?> clazz, String[] jvmArgs, String[] args) {
		if (isMac()) {
			String[] ja = new String[jvmArgs.length+1];
			for (int i = 0; i < jvmArgs.length; i++)
				ja[i] = jvmArgs[i];
			ja[ja.length-1] = "-XstartOnFirstThread";
			jvmArgs = ja;
		}
		return exec(clazz, jvmArgs, args);
	}
	
	public static Process exec(Class<?> clazz, String[] jvmArgs, String[] args) {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		if (classpath == null || classpath.length() == 0)
			classpath = "SongsOfSyx.jar";
		String className = clazz.getName();

		ArrayList<String> command = new ArrayList<>();
		command.add(javaBin);
		for (String a : jvmArgs)
			command.add(a);
		command.add("-cp");
		command.add(classpath);
		command.add(className);
		for (String a : args)
			command.add(a);

		ProcessBuilder builder = new ProcessBuilder(command);
		Process process;
		try {
			process = builder.redirectOutput(Redirect.INHERIT).redirectErrorStream(true).start();
			return process;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isMac() {
		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			return true;
		}
		return false;
	}

}