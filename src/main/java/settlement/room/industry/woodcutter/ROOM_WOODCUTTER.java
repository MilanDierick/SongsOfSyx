package settlement.room.industry.woodcutter;

import java.io.IOException;

import init.biomes.TERRAINS;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.BonusExp.RoomExperienceBonus;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import view.sett.ui.room.UIRoomModule;
import world.regions.Region;

public class ROOM_WOODCUTTER extends RoomBlueprintIns<Instance> implements INDUSTRY_HASER{

	final Job job;
	final Industry productionData;
	final Constructor constructor;
	final LIST<Industry> indus;
	
	public ROOM_WOODCUTTER(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_WOODCUTTER", cat);
		
		
		constructor = new Constructor(init, this);
		pushBo(init.data(), null, true);
		productionData = new Industry(this, init.data(), new RoomBoost[] {constructor.efficiency}, bonus()) {
			@Override
			public double getRegionBonus(Region reg) {
				return CLAMP.d(0.2 + reg.info.terrain(TERRAINS.FOREST())*3, 0, 1);
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
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
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

}
