package settlement.room.food.farm;

import java.io.IOException;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.resources.Growable;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.sett.ui.room.UIRoomModule;

public class ROOM_FARM extends RoomBlueprintIns<FarmInstance> implements INDUSTRY_HASER{

	final static double WORKERPERTILE = 64;
	final static double WORKERPERTILEI = 1.0/WORKERPERTILE;
	
	
	public final Growable crop;
	public final static String type = "FARM";
	
	final Constructor constructor;
	
	final Industry productionData;
	public final BOOSTABLE bonus2;
	
	final LIST<Industry> indus;

	final Tile tile;
	final Time time;
	double moisture = 1.0;
	double event = 1.0;
	private final Event eventer = new Event();
	private static final double mSpeed = 1.0/(TIME.days().bitSeconds()*3);
	
	private static CharSequence ¤¤mBlight = "¤Blight";
	private static CharSequence ¤¤mBlightBody = "¤Terrible news! our {0} have been afflicted by a disease. Religious subjects claim it to be a curse of the gods. Our harvests will be reduced by {1}%";

	private static CharSequence ¤¤mBoutiful = "¤Bountiful Harvest";
	private static CharSequence ¤¤mBoutifulBody = "¤The growth of our {0} is showing much promise this year and, if the gods are willing, the harvest will be extra bountiful.";

	final double yearMul = TIME.years().bitSeconds()/(16*TIME.secondsPerDay);
	
	static {
		D.ts(ROOM_FARM.class);
	}
	
	public ROOM_FARM(RoomInitData data, String key, RoomCategorySub cat, int index) throws IOException {
		super(index, data, key, cat);
		crop = RESOURCES.growable().get(data.data());
		
		constructor = new Constructor(this, data);
		bonus2 = BOOSTABLES.ROOMS().pushRoom(this, data.data(), type);
		productionData = new Industry(this, data.data(), new RoomBoost[] {constructor.fertility, }, bonus2);
		
		indus = new ArrayList<>(productionData);
		
		time = new Time(this);
		tile = new Tile(this);
		
	}
	
	Tile tile(int tx, int ty) {
		return tile.get(tx, ty);
	}
	
	@Override
	protected void update(float ds) {
		
		if (SETT.WEATHER().moisture.growthValue() < 1) {
			moisture -= ds*mSpeed;
			moisture = CLAMP.d(moisture, 0.20, 1);
		}
		
		if (time.dayI() == time.dayDeath) {
			moisture = 1;
			event = 1;
		}
		eventer.update();
		
	}
	
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.d(moisture);
		saveFile.d(event);
		saveFile.b(eventer.year);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		moisture = saveFile.d();
		event = saveFile.d();
		eventer.year = saveFile.b();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		moisture = 1.0;
		event = 1.0;
		eventer.year = -1;
	}
	
	@Override
	public boolean degrades() {
		return false;
	}
	
	double Fertility(int tx, int ty) {
		if (constructor.mustBeIndoors()) {
			if (SETT.TERRAIN().CAVE.is(tx, ty))
				return 1.0;
			return 0.7;
		}else {
			return SETT.FERTILITY().baseD.get(tx, ty);
		}
	}
	
	double Fertility(int tile) {
		if (constructor.mustBeIndoors()) {
			if (SETT.TERRAIN().CAVE.is(tile))
				return 1.0;
			return 0.7;
		}else {
			return SETT.FERTILITY().baseD.get(tile);
		}
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	private final class Event {
		
		private byte year=-1;
		
		Event(){
			IDebugPanelSett.add("Event: " + key, new ACTION() {
				
				@Override
				public void exe() {
					event();
				}
			});
		}
		
		void update() {
			
			
			if (VIEW.b().isActive())
				return;
			
			
			if (year == (TIME.years().bitCurrent() & 0x0F)) {
				return;
			}
			if (time.dayI() == time.dayEvent && ((int)TIME.currentSecond()%SETT.ROOMS().FARMS.size() >= index())) {
				if (TIME.playedGame() > TIME.years().bitSeconds() && employment().employed() > 15) {
					if (SETT.WEATHER().moisture.growthValue() == 1) {
						if (RND.oneIn(SETT.ROOMS().FARMS.size()*4)) {
							event();
						}
					}
				}
				year = (byte) (TIME.years().bitCurrent() & 0x0F);
			}
			
		}
		
		private void event() {
			if (instancesSize() == 0)
				return;
			if (RND.oneIn(4)) {
				event = 0.8-0.7*RND.rExpo();
				new MessageText(¤¤mBlight).paragraph(Str.TMP.clear().add(¤¤mBlightBody).insert(0, info.names).insert(1, (int)(100*(1-event)))).send();
				 
			}else {
				event = 1.25 + 0.75*RND.rExpo();
				new MessageText(¤¤mBoutiful).paragraph(Str.TMP.clear().add(¤¤mBoutifulBody).insert(0, info.names)).send();
			}
		}
		
	}

	@Override
	public boolean industryIgnoreUI() {
		return true;
	}
	
}
