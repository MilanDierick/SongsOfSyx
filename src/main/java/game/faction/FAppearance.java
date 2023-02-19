package game.faction;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;

public class FAppearance extends FactionResource{

	private final Str name = new Str(48);

	public Str name() {
		return name;
	}
	
	@Override
	protected void save(FilePutter file) {
		name.save(file);
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		name.load(file);
		
	}
	
	@Override
	protected void clear() {
		name.clear();
	}

	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
}
