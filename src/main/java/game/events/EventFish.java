package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

public class EventFish extends EventResource{

	private static CharSequence ¤¤mTitleGood = "Good catch!";
	private static CharSequence ¤¤mTitleBad = "Bad Catch!";
	private static CharSequence ¤¤mBodyGood = "Reports from our {0} are indicating the catch is unusually good today. Make sure we can store all that extra {1}.";
	private static CharSequence ¤¤mBodyBad = "Reports from our {0} are indicating the catch is unusually low today. Lets hope the catch of {1} of tomorrow will be better.";
	
	static {
		D.ts(EventFish.class);
	}
	
	private double value = 1;
	private double timer = 0;
	private int fishery = RND.rInt();
	
	EventFish(){
		IDebugPanelSett.add("Event Fishery", new ACTION() {
			
			@Override
			public void exe() {
				spawn();
			}
		});
	}
	
	@Override
	protected void update(double ds) {
		if (value != 1) {
			SETT.ROOMS().FISHERIES.getC(fishery).eventSet(value);
			timer -= ds;
			if (timer < 0)
				reset();
		}else {
			timer -= ds;
			if (timer > 0)
				return;
			spawn();
			reset();
		}
	}

	private void reset() {
		timer = TIME.years().bitSeconds() + RND.rFloat()*TIME.years().bitSeconds()*4;
		SETT.ROOMS().FISHERIES.getC(fishery).eventSet(1.0);
		fishery = RND.rInt();
		value = 1;
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(value);
		file.d(timer);
		file.i(fishery);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		value = file.d();
		timer = file.d();
		fishery = file.i();
	}

	@Override
	protected void clear() {
		timer = TIME.years().bitSeconds() + RND.rFloat()*TIME.years().bitSeconds()*4;
		fishery = RND.rInt();
		value = 1;
	}	
	
	void spawn() {
		timer = TIME.days().bitSeconds();
		value = 0.25 + 0.75*RND.rFloat(1);
		if (RND.rBoolean()) {
			value = 1-value;
		}else
			value = 1+value;
		
		ROOM_FISHERY f = SETT.ROOMS().FISHERIES.getC(fishery);
		if (f.employment().employed() == 0) {
			return;
		}
		if (STATS.POP().POP.data().get(null)-100 < RND.rInt(1000))
			return;
		
		f.eventSet(value);
		
		CharSequence t = ¤¤mTitleGood;
		CharSequence b = ¤¤mBodyGood;
		
		
		if (value < 1) {
			t = ¤¤mTitleBad;
			b = ¤¤mBodyBad;
		}
		
		Str.TMP.clear().add(b).insert(0, f.employment().blueprint().info.names).insert(1, f.industries().get(0).outs().get(0).resource.names);
		
		new MessageText(t).paragraph(Str.TMP).send();
		
	}

}
