package settlement.room.main.employment;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.*;
import snake2d.util.sets.*;

public final class RoomEquips {

	public final LIST<RoomEquip> ALL;
	private final ArrayList<ArrayListGrower<RoomEquip>> perRoom;
	
	
	RoomEquips(RoomEmployments emps) {
		
		
		ArrayListGrower<RoomEquip> all = new ArrayListGrower<>();
		
		PATH p = PATHS.INIT().getFolder("resource").getFolder("work");
		
		for (String k : p.getFiles()) {
			
			new RoomEquip(all, emps, new Json(p.get(k)));
			
		}
		
		this.ALL = all;
		
		perRoom = new ArrayList<ArrayListGrower<RoomEquip>>(emps.ALLS().size());
		while(perRoom.hasRoom())
			perRoom.add(new ArrayListGrower<RoomEquip>());
		
		
		for (RoomEquip t : all) {
			for (RoomEmploymentSimple e : emps.ALLS()) {
				if (t.target(e).max() > 0)
					perRoom.get(e.eindex()).add(t);
			}
		}
		
		
	}

	public LIST<RoomEquip> get(RoomEmploymentSimple e){
		return perRoom.get(e.eindex());
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(ALL.size());
			for (RoomEquip t : ALL)
				t.saver.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			int am = file.i();
			if (am != ALL.size()) {
				for (int i = 0; i < am; i++) {
					ALL.get(0).saver.load(file);
				}
				clear();
			}else {
				for (RoomEquip t : ALL)
					t.saver.load(file);
			}
			
		}
		
		@Override
		public void clear() {
			for (RoomEquip t : ALL)
				t.saver.clear();
		}
	};
	
	
}
