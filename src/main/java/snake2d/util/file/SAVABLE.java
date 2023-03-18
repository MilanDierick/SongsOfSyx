package snake2d.util.file;

import java.io.IOException;

public interface SAVABLE {

	public void save(FilePutter file);
	public void load(FileGetter file) throws IOException;
	public void clear();
}
