package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import settlement.main.SETT;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

public class EventOrchard extends EventResource{

	private static CharSequence ¤¤mBlight = "¤Narworm Infection";
	private static CharSequence ¤¤mBlightBody = "¤Terrible news! one of our {0} have been infested by Narworms. There is nothing left to do than to chop the infected trees down and burn them.";

	private static final double yearsBetweenEventsPerFarm = 12;
	
	private int day = -1;

	private int year = -1;
	private int yearsUntilNext = (int) (1 + 2*RND.rFloat()*yearsBetweenEventsPerFarm);
	private int nextFarm = RND.rInt();
	
	static {
		D.ts(EventOrchard.class);
	}
	
	EventOrchard(){
		IDebugPanelSett.add("Event: Orchard", new ACTION() {
			
			@Override
			public void exe() {
				nextFarm = RND.rInt();
				int ri = RND.rInt(SETT.ROOMS().ORCHARDS.size());
				for (int i = 0; i < SETT.ROOMS().ORCHARDS.size(); i++) {
					ROOM_ORCHARD f = SETT.ROOMS().ORCHARDS.getC(i+ri);
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
		
		ROOM_ORCHARD f = SETT.ROOMS().ORCHARDS.getC(nextFarm);
		
		event(f);
		reset();
		
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
	
	private void event(ROOM_ORCHARD farm) {
		int r = RND.rInt()&Integer.MAX_VALUE;
		if (STATS.POP().POP.data().get(null)-100 < RND.rInt(1000))
			return;
		
		
		for (int i = 0; i < farm.instancesSize(); i++) {
			RoomInstance ro = farm.getInstance((i+r)%farm.instancesSize());
			if (farm.event(ro.mX(), ro.mY(), 1.0)) {
				new MessageText(¤¤mBlight).paragraph(Str.TMP.clear().add(¤¤mBlightBody).insert(0, farm.info.names)).send();
				return;
			}
		}
	}

}
