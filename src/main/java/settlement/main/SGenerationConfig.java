package settlement.main;

import java.io.IOException;

import snake2d.util.file.*;

public class SGenerationConfig implements SAVABLE {

	public boolean animals = true;
	public boolean minables = true;
	
	
	@Override
	public void save(FilePutter file) {
		file.bool(animals);
		file.bool(minables);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		animals = file.bool();
		minables = file.bool();
	}
	@Override
	public void clear() {
		animals = true;
		minables = true;
	}
	
}
