package script;

import java.net.URL;
import java.net.URLClassLoader;

public class SyxClassLoader extends URLClassLoader {

	static {
		registerAsParallelCapable();
	}

	public SyxClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	public SyxClassLoader() {
		this(Thread.currentThread().getContextClassLoader());
	}

	void add(URL url) {
		addURL(url);
	}

}
