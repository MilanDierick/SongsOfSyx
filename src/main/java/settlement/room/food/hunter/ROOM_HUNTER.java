package settlement.room.food.hunter;

import java.io.IOException;

import game.time.TIME;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.animal.AnimalSpecies;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.*;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_RADIUS;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.*;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule;

public class ROOM_HUNTER extends RoomBlueprintIns<HunterInstance> implements INDUSTRY_HASER, ROOM_RADIUS{

	final Job job;
	final Industry production;
	
	final Constructor constructor;
	final LIST<Industry> indus;
	
	public ROOM_HUNTER(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_HUNTER", cat);
		int am = 0;
		long m = 0;
		for (AnimalSpecies s : SETT.ANIMALS().species) {
			for (RESOURCE r : s.resources()) {
				if ((m & r.bit) == 0) {
					am++;
					m |= r.bit;
				}
			}
		}
		if ((m & RESOURCES.LIVESTOCK().bit) == 0) {
			am ++;
		}
		
		
		RESOURCE[] res = new RESOURCE[am];
		double[] rates = new double[am];
		m = 0;
		am = 0;
		for (AnimalSpecies s : SETT.ANIMALS().species) {
			for (RESOURCE r : s.resources()) {
				if ((m & r.bit) == 0) {
					rates[am] = 0;
					res[am++] = r;
					m |= r.bit;
				}
			}
		}
		if ((m & RESOURCES.LIVESTOCK().bit) == 0) {
			res[am] = RESOURCES.LIVESTOCK();
		}
		
		constructor = new Constructor(this, init);
		BOOSTABLE b = BOOSTABLES.ROOMS().pushRoom(this, init.data(), "HUNTER");
		production = new Industry(this, null, null, res, rates, new RoomBoost[] {}, b);
		job = new Job(this);
		indus = new ArrayList<>(production);
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
		production.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		production.load(saveFile);
	}
	
	@Override
	protected void clearP() {
		production.clear();
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	@Override
	public double industryFormatProductionRate(GText text, IndustryResource i, RoomInstance ins) {
		ROOM_PRODUCER p = (ROOM_PRODUCER) ins;
		double pa = TIME.years().bitPartOf();
		
		double e = (i.year.get(p)*pa + i.yearPrev.get(p)*(1.0-pa));
		e /= TIME.years().bitConversion(TIME.days());
		GFORMAT.f(text, e, 2);
		return e;
	}
	
	@Override
	public void industryHoverProductionRate(GBox b, IndustryResource i, RoomInstance ins) {
		
	}

}
