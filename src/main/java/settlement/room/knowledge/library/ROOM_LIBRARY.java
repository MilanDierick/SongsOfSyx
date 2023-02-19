package settlement.room.knowledge.library;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.Humanoid;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.bit.Bits;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.statistics.HistoryInt;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_LIBRARY extends RoomBlueprintIns<LibraryInstance> implements INDUSTRY_HASER {

	public final static String type = "LIBRARY";
	final Data data = new Data();
	final Job job;
	private final double boostPerWorker;
	final double degradePerSecond;
	final double workValue;
	final double workSpeed;
	final BOOSTABLE bonus;
	final Industry industry;
	final Constructor constructor;
	
	final LIST<Industry> indus;

	public ROOM_LIBRARY(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		job = new Job(this);
		
		boostPerWorker = init.data().d("BOOST_PER_WORKER", 0.00001, 1);
		degradePerSecond = init.data().d("KNOWLEDGE_DEGRADE", 0, 10)/TIME.years().bitSeconds();
		workSpeed = init.data().d("KNOWLEDGE_GAIN_SPEED", 0, 1000);
		
		double work = degradePerSecond;
		work *= Humanoid.WORK_PER_DAYI;
		work *= Job.time;
		
		workValue = work;
		
		constructor = new Constructor(this, init);
		bonus = BOOSTABLES.ROOMS().pushRoom(this, init.data(), type);
		

		industry = new Industry(this, init.data(), new RoomBoost[] {
			constructor.efficiency
		}, bonus);

		indus = new ArrayList<>(industry);
	}
	
	@Override
	protected void update(float ds) {
		data.inc(-data.current*ds*degradePerSecond);
	}

	public double boostPerStation() {
		return boostPerWorker;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		data.save(saveFile);
		industry.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		data.load(saveFile);
		industry.load(saveFile);
	
	}
	
	@Override
	protected void clearP() {
		this.data.clear();
		industry.clear();
	}
	
	class Data implements SAVABLE {
		
		final HistoryInt utilizedHistory = new HistoryInt(64, TIME.days(), true);
		private double current = 0;
		private int stations;
		double progress;
		byte usedD = 0;
		double ws = 1.0;
		
		private double skill;
		private double skillAmount;
		private int skillUpI = -1;
		
		Data(){
			
		}
		
		void perform(double skill){
			this.skill += skill;
			skillAmount ++;
			if (skillAmount > 100 || (Bits.getDistance(skillUpI, GAME.updateI(), Integer.MAX_VALUE) > 60*10 && skillAmount > 4)) {
				skillUpI = GAME.updateI();
				
				skillAmount /= 2;
				this.skill /= 2;
			}
			
			skill*= workValue*ws;
			inc(skill);
			
		}
		
		private void inc(double utilized){
			current += utilized;
			utilizedHistory.set((int)current*1000);
			setProgress();
		}
		
		void incStations(int stations) {
			this.stations+= stations;
		}
		
		double utilized() {
			return current;
		}

		@Override
		public void save(FilePutter file) {
			utilizedHistory.save(file);
			file.d(current);
			file.i(stations);
			file.d(skill);
			file.d(skillAmount);
		}

		private void setProgress() {
			double e = employment().employed();
			if (e > 0) {
				progress = current/(e);
				ws = 1.0 + workSpeed*Math.sqrt(CLAMP.d(1.0-(current/(e*getSkill())), 0, 1));
			}else {
				progress = current > 0 ? 1.0 : 0;
				ws = workSpeed;
			}
			
			progress = CLAMP.d(progress, 0, 1);
			usedD = (byte) (CLAMP.d(current/(1 + stations), 0, 1)*255);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			utilizedHistory.load(file);
			current = file.d();
			stations = file.i();
			skill = file.d();
			skillAmount = file.d();
			setProgress();
		}

		@Override
		public void clear() {
			utilizedHistory.clear();
			current = 0;
			stations = 0;
			skill = 0;
			skillAmount = 0;
		}
		
		public double getSkill() {
			if (skillAmount == 0)
				return 1;
			return skill/skillAmount;
		}
		
		private double getProjection() {
			return getSkill()*employment().employed();
		}


	}
	
	public double projection() {
		return boostPerWorker*data.getProjection();
	}
	
	public double boost() {
		return data.utilized()*boostPerWorker;
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}

	public void knowledgeAdd(int i) {
		data.inc(i);
		
	}
	
}
