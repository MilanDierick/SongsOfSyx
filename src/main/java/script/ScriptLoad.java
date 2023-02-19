package script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;

public final class ScriptLoad{
	
	private static KeyMap<LinkedList<ScriptLoad>> cache = new KeyMap<>();
	
	public final SCRIPT script;
	public final String className;
	public final String file;
	
	private static ClassLoader loader = ClassLoader.getSystemClassLoader();
	
	private ScriptLoad(SCRIPT script, String cn, String file){
		this.script = script;
		this.className = cn;
		this.file = file;
	}

	public static LIST<ScriptLoad> get(String script){
		return new Init().compileScripts(script);
	}
	
	public static LIST<ScriptLoad> getAll(){
		return new ArrayList<>(new Init().compileScripts(PATHS.SCRIPT().jar.getFiles()));
	}
	
	public static ScriptLoad get(FileGetter file) throws IOException{
		String cn = file.chars();
		String fileN = file.chars();
		LIST<ScriptLoad> ls = get(fileN);
		
		for (ScriptLoad l : ls) {
			if (l.className.equals(cn))
				return l;
		}
		
		return null;
		
	}
	
	public static LIST<ScriptLoad> load(FileGetter f) throws IOException{
		LinkedList<ScriptLoad> files = new LinkedList<>();
		int am = f.i();
		for (int i = 0; i < am; i++) {
			String cn = f.chars();
			String fileN = f.chars();
			LIST<ScriptLoad> ls = get(fileN);
			for (ScriptLoad l : ls) {
				if (l.className.equals(cn))
					files.add(l);;
			}
		}
		return new ArrayList<ScriptLoad>(files);
		
	}
	
	public static void save(LIST<ScriptLoad> scripts, FilePutter f) {
		f.i(scripts.size());
		for (ScriptLoad l : scripts) {
			f.chars(l.className);
			f.chars(l.file);
		}
	}

	public void save(FilePutter file) {
		file.chars(className);
		file.chars(this.file);
		
	}
	
	public static ClassLoader getLoader() {
		return loader;
	}
	
	private static class Init {
		
		private final PATH pathRoot = PATHS.SCRIPT().jar;
		private final LinkedList<ScriptLoad> all = new LinkedList<>();
		private final LinkedList<URL> urlList = new LinkedList<URL>();
		private final KeyMap<String> classToJar = new KeyMap<>();
		
		Init(){
			forceInit(SCRIPT.class);
		}
		
		private LIST<ScriptLoad> compileScripts(String... files) {
			
			removePhysicalScripts();
			
			files = grabCachedScripts(files);
			
			if (files.length == 0)
				return all;
			
			for (String file : files) {
				
				JarInputStream jarFile = copyAndMakeJarUrl(file);
				if (jarFile == null)
					continue;
				
				try {
					JarEntry je = jarFile.getNextJarEntry();
					
					while (je != null) {
						JarEntry jarEntry = je;
						je = jarFile.getNextJarEntry();
						if (jarEntry.getName().contains("META-INF"))
							continue;
						processJarEntry(jarEntry, file);					
					}

					jarFile.close();
					
				} catch (IOException e) {
					LOG.err("script: " + pathRoot.get() + File.separator + file + " unable to cache!");
					e.printStackTrace();
				}
			}
			
			loadScripts();
			
			removePhysicalScripts();
			
			cache.clear();
			
			for (ScriptLoad l : all) {
				if (!cache.containsKey(l.file))
					cache.put(l.file, new LinkedList<>());
				cache.get(l.file).add(l);
			}
			
			return all;
		}
		
		private String[] grabCachedScripts(String... files) {
			int uninited = 0;
			
			for (String f : files)  {
				if (cache.containsKey(f)) {
					all.add(cache.get(f));
				}else
					uninited++;
			}
			
			String[] nFiles = new String[uninited];
			uninited = 0;
			for (String f : files)  {
				if (!cache.containsKey(f)) {
					nFiles[uninited++] = f;
				}
			}
			
			return nFiles;
			
		}
		
		private void loadScripts() {
			URL[] urls = new URL[urlList.size()];
			for (int i = 0; i < urls.length; i++) {
				urls[i] = urlList.removeFirst();
			}
			
			try (URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader())) {
				ScriptLoad.loader = loader;
				for (String className : classToJar.keys()) {
					Class<?> s;
					try {
						s = loader.loadClass(className);
					} catch (ClassNotFoundException e1) {
						throw new RuntimeException(e1);
					}

					if (SCRIPT.class.isAssignableFrom(s) && !Modifier.isAbstract(s.getModifiers())) {
						try {
							SCRIPT sc = (SCRIPT)s.newInstance();
							all.add(new ScriptLoad(sc, className, classToJar.get(className)));
							LOG.ln(" -script available: : " + sc.name());
						}catch(IllegalAccessException e) {
							throw new Errors.DataError(className + " could not be created. Probably cause would be a non-public constructor, or constructor parameters", classToJar.get(className));
						} catch (InstantiationException e) {
							e.printStackTrace();
							throw new RuntimeException("some weirdness with loading scripts. See std err");
						}
						
						
					}
				}
				

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		private JarInputStream copyAndMakeJarUrl(String jarFile) {
			if (!pathRoot.exists(jarFile)) {
				LOG.err("script: " + pathRoot.get() + File.separator + jarFile + " does not exist and will be ignored.");
				return null;
			}
			LOG.ln("loading script jar " + jarFile);
			
			java.nio.file.Path p = pathRoot.get(jarFile);
			try {
				p = Files.copy(p, PATHS.CACHE_SCRIPT().get().resolve(jarFile), StandardCopyOption.REPLACE_EXISTING);
				urlList.add(p.toUri().toURL());
				return new JarInputStream(Files.newInputStream(p));
			} catch (IOException e) {
				LOG.err("script: " + pathRoot.get() + File.separator + jarFile + " unable to cache! Ignoring.");
				e.printStackTrace();
				
			}
			return null;
			
		}
		
		private void processJarEntry(JarEntry jarEntry, String jarFile) {
			if (jarEntry.getName().endsWith(".class")) {
				String className = jarEntry.getName();
				className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
				className = className.replaceAll("/", "\\.");
				
				if (classToJar.containsKey(className)) {
					throw new Errors.DataError(className + " this class already exists in another script jar and game can't be loaded. Other jar: " + classToJar.get(className), pathRoot.get(jarFile));
				}
				
				try {
					if (Class.forName(className) != null) {
						throw new Errors.DataError(className + " already exist in the game and will clash with the game. This class needs to be renamed", pathRoot.get(jarFile));
					}
				}catch(Exception e) {
					//this is good!
				}
				
				classToJar.put(className, jarFile);
			}
		}
		
		/**
		 * clear old physical scripts.
		 * Scripts might be in a zip archive, and must be temporarily moved to a physical location before process.
		 * Now we'll clear any old scripts laying about.
		 */
		private void removePhysicalScripts() {
			for (String s : PATHS.CACHE_SCRIPT().getFiles()) {
				PATHS.CACHE_SCRIPT().delete(s);
			}
		}
		
		private static <T> Class<T> forceInit(Class<T> klass) {
			try {
				Class.forName(klass.getName(), true, klass.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new AssertionError(e); // Can't happen
			}
			return klass;
		}
	}

}
