package game.faction;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;

public final class FRuler extends FactionResource{

	public final Str name = new Str(24);
	
	public FRuler() {
		name.clear().add("Bob");
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void update(double ds, Faction f) {
		// TODO Auto-generated method stub
		
	}

}
