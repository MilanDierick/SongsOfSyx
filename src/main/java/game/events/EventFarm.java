package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import settlement.main.SETT;
import settlement.room.food.farm.ROOM_FARM;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

public class EventFarm extends EventResource{

	private static CharSequence ¤¤mBlight = "¤Blight";
	private static CharSequence ¤¤mBlightBody = "¤Terrible news! our {0} have been afflicted by a disease. Religious subjects claim it to be a curse of the gods. Our harvests will be reduced by {1}%";

	private static CharSequence ¤¤mBoutiful = "¤Bountiful Harvest";
	private static CharSequence ¤¤mBoutifulBody = "¤The growth of our {0} is showing much promise this year and, if the gods are willing, the harvest will be extra bountiful.";

	private static final double yearsBetweenEventsPerFarm = 3;
	
	private int day = -1;

	private int year = -1;
	private int yearsUntilNext = (int) (1 + 2*RND.rFloat()*yearsBetweenEventsPerFarm);
	private int nextFarm = RND.rInt();
	
	static {
		D.ts(EventFarm.class);
	}
	
	EventFarm(){
		IDebugPanelSett.add("Event: Farms", new ACTION() {
			
			@Override
			public void exe() {
				nextFarm = RND.rInt();
				int ri = RND.rInt(SETT.ROOMS().FARMS.size());
				for (int i = 0; i < SETT.ROOMS().FARMS.size(); i++) {
					ROOM_FARM f = SETT.ROOMS().FARMS.getC(i+ri);
					if (f.instancesSize() > 0) {
						nextFarm = i+ri;
						break;
					}
					
				}
				yearsUntilNext = 0;
			}
		});
	}
	
	@Override
	protected void update(double ds) {
		
		if (day == -1) {
			reset();
			return;
		}
			
		if (year != TIME.years().bitsSinceStart()) {
			yearsUntilNext --;
			year = TIME.years().bitsSinceStart();
		}
		
		if (yearsUntilNext > 0)
			return;
		
		if (day == TIME.days().bitsSinceStart()) {
			return;
		}
		
		day = TIME.days().bitsSinceStart();
		
		ROOM_FARM f = SETT.ROOMS().FARMS.getC(nextFarm);
		
		if (f.isGoodDayForEvent()) {
			event(f);
			reset();
		}
		
	}

	private void reset() {
		day = TIME.days().bitsSinceStart();
		year = TIME.years().bitsSinceStart();
		yearsUntilNext = (int) (1 + 2*RND.rFloat()*yearsBetweenEventsPerFarm);
		nextFarm = RND.rInt();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(day);
		file.i(year);
		file.i(yearsUntilNext);
		file.i(nextFarm);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		day = file.i();
		year = file.i();
		yearsUntilNext = file.i();
		nextFarm = file.i();
	}

	@Override
	protected void clear() {
		day = -1;
	}	
	
	private void event(ROOM_FARM farm) {
		if (farm.instancesSize() == 0)
			return;
		if (RND.oneIn(4)) {
			double event = 0.8-0.7*RND.rExpo();
			farm.setEvent(event);
			new MessageText(¤¤mBlight).paragraph(Str.TMP.clear().add(¤¤mBlightBody).insert(0, farm.info.names).insert(1, 10*(int)(10*(1-event)))).send();
			 
		}else {
			farm.setEvent(1.25 + 0.75*RND.rExpo());
			new MessageText(¤¤mBoutiful).paragraph(Str.TMP.clear().add(¤¤mBoutifulBody).insert(0, farm.info.names)).send();
		}
	}

}
