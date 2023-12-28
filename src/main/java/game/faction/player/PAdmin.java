package game.faction.player;

import java.io.IOException;

import game.faction.FACTIONS;
import settlement.main.SETT;
import settlement.room.infra.admin.ROOM_ADMIN;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import world.regions.data.RD;

public final class PAdmin {

	PAdmin(){
		
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public int total() {
		int am = 0;
		for (ROOM_ADMIN r : SETT.ROOMS().ADMINS)
			am += r.knowledge();
		
		return am;
	}
	
	public int used() {
		return RD.ADMIN().consumed(FACTIONS.player());
	}
	
	public int available() {
		return total()-used();
	}
	
	public double penalty() {
		double tot = total();
		if (tot == 0)
			return 0;
		double spe = used()-tot;
		return CLAMP.d(1-2*spe/tot, 0, 1); 
	}
	
	
	
}
