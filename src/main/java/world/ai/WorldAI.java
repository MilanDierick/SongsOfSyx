package world.ai;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.World.WorldResource;

public class WorldAI extends WorldResource{

	public final War war = new War();
	
	@Override
	protected void save(FilePutter file) {
		war.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		war.load(file);
	}
	
	@Override
	protected void clear() {
		war.clear();
	}
	
	@Override
	protected void update(float ds) {
		war.update(ds);
	}

	public void init(Faction f) {
		war.init(f);
	}
	
	public void initiateWar(Faction a, Faction b) {
		if (a != FACTIONS.player())
			war.planForWar(a);
		if (b != FACTIONS.player())
			war.planForWar(b);
	}

}
