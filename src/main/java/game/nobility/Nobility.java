package game.nobility;

import java.io.IOException;

import game.time.TIME;
import init.boostable.*;
import init.boostable.BOOSTER.BOOSTER_IMP_DATA;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.info.INFO;

public final class Nobility implements INDEXED{

	private final INFO info;
	private final int index;
	private final COLOR color;
	private double currentSkill = 0;
	private int subjectID = -1;
	private double rep;
	private double repInc = 1;
	private static final double upI = 1.0/(TIME.secondsPerDay*32);
	private final LIST<RoomBlueprintIns<?>> rooms;
	public final BOOSTER_IMP_DATA BOOSTER;
	
	Nobility(String key, Init init){
		index = init.all.add(this);
		Json jdata = new Json(init.pData.get(key));
		Json jtext = new Json(init.pText.get(key));
		info = new INFO(jtext);


		BOOSTER = new BOOSTER.BOOSTER_IMP_DATA(info.name, jdata);
		
		color = new ColorImp(jdata);
		{
			ArrayList<RoomBlueprintIns<?>> rooms = new ArrayList<>(SETT.ROOMS().all().size());
			for (BBoost bo : BOOSTER.boosts()) {
				if (bo.boost instanceof BOOSTABLERoom) {
					RoomBlueprintImp p = ((BOOSTABLERoom)bo.boost).room;
					RoomBlueprintIns<?> b = (RoomBlueprintIns<?>) p;
					rooms.add(b);
				}
			}
			this.rooms = new ArrayList<>(rooms);
		}
		
		
	}
	
	public INFO info() {
		return info;
	}

	@Override
	public int index() {
		return index;
	}
	
	public COLOR color() {
		return color;
	}
	
	public Humanoid subject() {
		if (subjectID == -1)
			return null;
		ENTITY e = SETT.ENTITIES().getByID(subjectID);
		if (e != null && e instanceof Humanoid) {
			return (Humanoid) e;
		}else {
			subjectID = -1;
			return null;
		}
	}
	
	void assign(Humanoid h) {
		saver.clear();
		subjectID = h.id();
		update(0);
	}
	
	void update(double ds) {
		if (subject() != null) {
			//double rep = STATS.HAPPINESS().current(subject().indu(), GAME.NOBLE().active(), GAME.NOBLE().ALL().size());
			repInc = rep - this.rep;
			this.rep = 1.0;
			currentSkill += ds*upI;
			if (currentSkill > 1)
				currentSkill = 1;
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.d(currentSkill);
			file.d(repInc);
			file.d(rep);
			file.i(subjectID);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			currentSkill = file.d();
			repInc = file.d();
			rep = file.d();
			subjectID = file.i();
		}
		
		@Override
		public void clear() {
			currentSkill = 0;
			rep = 0;
			subjectID = -1;
			repInc = 1;
		}
	};
	
	public double skill() {
		return currentSkill;
	}
	
	public double happiness() {
		return rep;
	}
	
	public LIST<RoomBlueprintIns<?>> rooms(){
		return rooms;
	}
	
}
