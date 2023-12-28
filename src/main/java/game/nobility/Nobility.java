package game.nobility;

import java.io.IOException;

import game.boosting.BoostSpecs;
import game.time.TIME;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
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
	private LIST<RoomBlueprintIns<?>> rooms;
	public final BoostSpecs boosters;

	
	Nobility(String key, Init init){
		
		index = init.all.add(this);
		Json jdata = new Json(init.pData.get(key));
		Json jtext = new Json(init.pText.get(key));
		info = new INFO(jtext);
		boosters = new BoostSpecs(HCLASS.NOBLE.name + ": " + info.name, UI.icons().s.noble, false);
		boosters.push(jdata, null);
		
		color = new ColorImp(jdata);
		
		
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
		if (rooms == null) {
			ArrayListGrower<RoomBlueprintIns<?>> rooms = new ArrayListGrower<>();
			for (Industry ii : SETT.ROOMS().INDUSTRIES) {
				if (ii.blue instanceof RoomBlueprintIns<?>) {
					RoomBlueprintIns<?> i = (RoomBlueprintIns<?>) ii.blue;
					if (!rooms.contains(i))
						rooms.add(i);
				}
			}
			this.rooms = rooms;
		}
		
		return rooms;
	}
	
}
