package settlement.room.food.fish;

import java.io.IOException;

import game.time.TIME;
import init.D;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import settlement.main.SETT;
import settlement.path.finder.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.dic.DicMisc;
import util.info.INFO;
import view.main.MessageText;
import view.sett.IDebugPanelSett;
import view.sett.ui.room.UIRoomModule;

public class ROOM_FISHERY extends RoomBlueprintIns<FishInstance> implements INDUSTRY_HASER{

	public final static String type = "FISHERY";
	final Job job;
	final Industry productionData;
	final Constructor constructor;
	final LIST<Industry> indus;
	private final Event event = new Event();
	
	
	private static CharSequence ¤¤mTitleGood = "Good catch!";
	private static CharSequence ¤¤mTitleBad = "Bad Catch!";
	private static CharSequence ¤¤mBodyGood = "Our {0} are reporting big catches today. Make sure we can store all that extra {1}.";
	private static CharSequence ¤¤mBodyBad = "Our {0} are reporting bad catches today. Lets hope the catch of {1} of tomorrow will be better.";
	
	static {
		D.ts(ROOM_FISHERY.class);
	}
	
	
	public ROOM_FISHERY(RoomInitData init, String key, int index, RoomCategorySub cat) throws IOException {
		super(index, init, key, cat);
		
		
		
		constructor = new Constructor(init, this);
		BOOSTABLE skill = BOOSTABLES.ROOMS().pushRoom(this, init.data(), type);
		
		
		
		productionData = new Industry(
				this, init.data(), 
				new RoomBoost[] {constructor.efficiency, constructor.fish, new RoomBoost() {
					
					INFO info = new INFO(DicMisc.¤¤Event , "");
					
					@Override
					public INFO info() {
						return info;
					}
					
					@Override
					public double get(RoomInstance r) {
						return event.value;
					}
				}}, 
				skill);
		
		job = new Job(this);
		indus = new ArrayList<>(productionData);
		
		
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	
	
	@Override
	protected void update(float ds) {
		event.update(ds);
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.d(event.timer);
		saveFile.d(event.value);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		event.timer = saveFile.d();
		event.value = saveFile.d();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		event.timer = 0;
		event.value = 1;
	}
	
	@Override
	public boolean makesDudesDirty() {
		return true;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(constructor.fish.applier(this));
	}
	
	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	private class Event {
		
		private double value = 1;
		private double timer = 0;
		
		public Event() {
			IDebugPanelSett.add(key + " Event", new ACTION() {
				
				@Override
				public void exe() {
					spawn();
				}
			});
		}
		
		void update(double ds) {
			
			timer -= ds;
			if (timer > 0)
				return;
			if (value != 1) {
				value = 1;
				timer = TIME.years().bitSeconds() + RND.rFloat()*TIME.years().bitSeconds()*4*SETT.ROOMS().FISHERIES.size();
			}else if (employment().employed() > 20){
				spawn();
				
			}else {
				timer = TIME.years().bitSeconds() + RND.rFloat()*TIME.years().bitSeconds()*4*SETT.ROOMS().FISHERIES.size();
			}
			
			
		}
		
		void spawn() {
			timer = TIME.days().bitSeconds();
			value = 0.25 + 0.75*RND.rFloat(1);
			if (RND.rBoolean()) {
				value = 1-value;
			}else
				value = 1+value;
			
			CharSequence t = ¤¤mTitleGood;
			CharSequence b = ¤¤mBodyGood;
			
			
			if (value < 1) {
				t = ¤¤mTitleBad;
				b = ¤¤mBodyBad;
			}
			
			Str.TMP.clear().add(b).insert(0, employment().title).insert(1, industries().get(0).outs().get(0).resource.names);
			
			new MessageText(t).paragraph(Str.TMP).send();
			
		}
		
	}
	
}
