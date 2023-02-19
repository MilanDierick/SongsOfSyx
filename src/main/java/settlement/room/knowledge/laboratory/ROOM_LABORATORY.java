package settlement.room.knowledge.laboratory;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import settlement.entity.humanoid.Humanoid;
import settlement.path.finder.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.bit.Bits;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.statistics.HistoryInt;
import view.interrupter.IDebugPanel;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_LABORATORY extends RoomBlueprintIns<LaboratoryInstance> {

	public final static String type = "LABORATORY";
	final Data data = new Data();
	final Job job;
	private final int knowledgePerStation;
	final double degradePerSecond;
	final double workValue;
	private final double workSpeed;
	final BOOSTABLE bonus;
	final Constructor constructor;

	public ROOM_LABORATORY(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		job = new Job(this);
		
		knowledgePerStation = init.data().i("KNOWLEDGE_PER_WORKER", 1, 1000);
		degradePerSecond = init.data().d("KNOWLEDGE_DEGRADE", 0, 10)/TIME.years().bitSeconds();
		workSpeed = init.data().d("KNOWLEDGE_GAIN_SPEED", 0, 1000);
		
		double work = degradePerSecond;
		work *= Humanoid.WORK_PER_DAYI;
		work *= Job.time;
		
		workValue = work;
		
		constructor = new Constructor(this, init);
		bonus = BOOSTABLES.ROOMS().pushRoom(this, init.data(), type);
		IDebugPanel.add("knowledge + 1000", new ACTION() {
			
			@Override
			public void exe() {
				data.current += 1000;
			}
		});
		
	}
	
	@Override
	protected void update(float ds) {
		data.inc(-data.current*ds*degradePerSecond);
	}

	public int knowledgePerStation() {
		return (int) (knowledgePerStation);
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		data.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		data.load(saveFile);
	
	}
	
	@Override
	protected void clearP() {
		this.data.clear();
	}
	
	class Data implements SAVABLE {
		
		final HistoryInt utilizedHistory = new HistoryInt(64, TIME.days(), true);
		private double current = 0;
		private int stations;
		double progress;
		double ws = 1.0;
		byte usedD = 0;
		
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
			utilizedHistory.set((int)(knowledgePerStation*current));
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
		
		public int getProjection() {
			return (int) (knowledgePerStation*getSkill()*employment().employed());
		}

	}
	
	public long knowledgeCapacity() {
		return data.getProjection();
	}
	
	public long knowledge() {
		
		return (long) (knowledgePerStation*data.utilized());
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	public void knowledgeAdd(int i) {
		data.inc(i);
		
	}
	
}
