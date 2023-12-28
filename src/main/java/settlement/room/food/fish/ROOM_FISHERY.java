package settlement.room.food.fish;

import java.io.IOException;

import init.biomes.TERRAINS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.BonusExp.RoomExperienceBonus;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.dic.DicMisc;
import util.info.INFO;
import view.sett.ui.room.UIRoomModule;
import world.regions.Region;

public class ROOM_FISHERY extends RoomBlueprintIns<FishInstance> implements INDUSTRY_HASER{

	public final static String type = "FISHERY";
	final Job job;
	final Industry productionData;
	final Constructor constructor;
	final LIST<Industry> indus;
	private double event = 1;
	
	
	public ROOM_FISHERY(RoomInitData init, String key, int index, RoomCategorySub cat) throws IOException {
		super(index, init, key, cat);
		
		
		
		constructor = new Constructor(init, this);
		pushBo(init.data(), type, true);
		
		
		productionData = new Industry(
				this, init.data(), 
				new RoomBoost[] {constructor.efficiency, new RoomBoost() {
					
					INFO info = new INFO(DicMisc.¤¤Event , "");
					
					@Override
					public INFO info() {
						return info;
					}
					
					@Override
					public double get(RoomInstance r) {
						return event;
					}
				}}, 
				bonus()) {
			@Override
			public double getRegionBonus(Region reg) {
				return CLAMP.d(0.25 + (reg.info.terrain(TERRAINS.OCEAN()) + reg.info.terrain(TERRAINS.WET()))*2, 0, 1);
			}
		};
		
		job = new Job(this);
		indus = new ArrayList<>(productionData);
		new RoomExperienceBonus(this, init.data(), bonus());
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	
	
	@Override
	protected void update(float ds) {
		
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.d(event);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		event = saveFile.d();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		event = 1.0;
	}
	
	@Override
	public boolean makesDudesDirty() {
		return true;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}
	
	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	public double event() {
		return event;
	}
	
	public void eventSet(double event) {
		this.event = event;
	}
	
	public void performFishingTrip(Humanoid h, int tx, int ty, double time) {
		if (is(tx, ty)){
			
			SETT_JOB s = job.init(tx, ty, get(tx, ty));
			if (s != null)
				job.secretPerform(h, time);
		}
	}
	
	public boolean launchFishingExpedition(Humanoid h, int tx, int ty) {
		if (is(tx, ty)){
			if (Job.isShip.is(SETT.ROOMS().data.get(tx,ty))) {
				job.init(tx, ty, get(tx, ty)).jobStartPerforming();
				if (SETT.HALFENTS().dingy.make(h, tx, ty, industries().get(0).outs().get(0).resource, get(tx, ty).upgrade(), DIR.ALL.get(Job.shipDir.get(SETT.ROOMS().data.get(tx,ty)))))
					return true;
			}
		}
		return false;
	}
	
}
