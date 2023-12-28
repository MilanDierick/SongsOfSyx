package settlement.room.infra.admin;

import java.io.IOException;

import game.faction.FACTIONS;
import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.info.INFO;
import util.statistics.HistoryInt;
import view.sett.ui.room.UIRoomModule;
import world.regions.data.RD;

public final class ROOM_ADMIN extends RoomBlueprintIns<AdminInstance> implements INDUSTRY_HASER {

	public final static String type = "ADMIN";
	final Data data = new Data();
	final Job job;
	private final int knowledgePerStation;
	final double degradeValue;
	final double workValue;
	final Industry industry;
	final Constructor constructor;
	final double workSpeed;
	final LIST<Industry> indus;


	public ROOM_ADMIN(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
		job = new Job(this);
		
		knowledgePerStation = init.data().i("ADMIN_PER_STATION", 1, 100);
		double degrade = init.data().d("ADMIN_DEGRADE_PER_YEAR", 0, 10);
		degradeValue = degrade/TIME.years().bitSeconds();
		workSpeed = init.data().d("WORK_SPEED", 0, 1000);
	
		double work = knowledgePerStation*degrade;
		work /= TIME.years().bitSeconds();
		work *= Humanoid.WORK_PER_DAYI;
		work *= Job.time;
		
		workValue = work;
		
		constructor = new Constructor(this, init);
		pushBo(init.data(), type, true);
		
		

		industry = new Industry(this, init.data(), new RoomBoost[] {
			new RoomBoost() {
				
				@Override
				public INFO info() {
					return industry.ins().get(0).resource;
				}
				
				@Override
				public double get(RoomInstance r) {
					if (r instanceof AdminInstance) {
						AdminInstance ins = (AdminInstance) r;
						return 0.25 + 0.75*ins.paper/ins.jobs.size();
					}
					return 1;
				}
			},
			constructor.efficiency
		}, bonus());
		
		indus = new ArrayList<>(industry);
	}
	
	@Override
	protected void update(float ds) {
		data.inc(-data.current*ds*degradeValue);
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
		
		final HistoryInt utilizedHistory = new HistoryInt(64, TIME.years(), true);
		private double current = 0;
		private int stations;
		byte usedD = 0;
		double progress = 0;
		Data(){
			
		}
		
		void inc(double utilized){
			
			RD.ADMIN().factionSource.inc(FACTIONS.player(), -(int)current);
			current += utilized;
			RD.ADMIN().factionSource.inc(FACTIONS.player(), (int)current);
			utilizedHistory.set((int)current);
			setProgress();
		}
		
		void incStations(int stations) {
			this.stations+= stations;
		}
		
		double utilized() {
			return current;
		}

		private void setProgress() {
			progress = current/(1 + employment().employed()*knowledgePerStation);
			usedD = (byte) (CLAMP.d(current/(1 + stations*knowledgePerStation), 0, 1)*255);
		}
		
		@Override
		public void save(FilePutter file) {
			utilizedHistory.save(file);
			file.d(current);
			file.i(stations);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			utilizedHistory.load(file);
			current = file.d();
			stations = file.i();
			setProgress();
		}

		@Override
		public void clear() {
			utilizedHistory.clear();
			current = 0;
			stations = 0;
		}

	}
	
	public long knowledge() {
		
		return (long) (data.utilized());
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
	
}
