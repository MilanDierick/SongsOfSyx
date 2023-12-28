package world.army.ai;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DWar;
import snake2d.util.file.*;

public final class WArmyAI {

	final War war = new War();
	
	public WArmyAI(){
		new DWar.DWarListener() {
			
			@Override
			protected void exe(Faction a, Faction b, boolean iswar) {
				if (a != FACTIONS.player())
					war.planForWar(a, iswar);
				if (b != FACTIONS.player())
					war.planForWar(b, iswar);
			}
		};
	}
	
	public SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			war.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			war.load(file);
		}
		
		@Override
		public void clear() {
			war.clear();
		}
	};
	
	
	
	public void update(float ds) {
		war.update(ds);
	}

	public void init(Faction f) {
		war.init(f);
	}
	


}
