package game.events;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import init.D;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.stats.CAUSE_LEAVE;
import settlement.stats.STATS;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.sett.IDebugPanelSett;

final class EventCitizenRiot implements SAVABLE{

	private static CharSequence ¤¤riot = "¤Riot!";
	private static CharSequence ¤¤riotD = "¤Your subjects have had enough of you and your rule. They have risen up, determined to show their displeasure by murdering and vandalizing. The only way to quell these rebels is by calling in the military, set them to mopping up. Guards will also do their fair share. Riots will also subdue naturally without action om time.";
	private static CharSequence ¤¤amount = "¤We have reports of {0} rioters. The following species have joined: ";
	private static CharSequence ¤¤success = "¤The rioters have had a taste of your might and have laid down their arms. The ring leaders will be processed in your justice system and be made an example of.";
	private static CharSequence ¤¤Over = "¤Riot Over!";
	private static CharSequence ¤¤OverD = "¤The rioters have had enough of murdering and pillaging for now. They have returned to be law abiding citizens, until they feel it's time again.";

	private double timer = 0;

	private double secondsToRiot;
	private int currentRioteers = 0;
	
	static {
		D.ts(EventCitizenRiot.class);
	}
	
	EventCitizenRiot(){
		
		IDebugPanelSett.add("Event: Riot", new ACTION() {
			
			@Override
			public void exe() {
				int[] races = new int[RACES.all().size()];
				for (int i = 0; i < races.length; i++) {
					races[i] = (int) Math.ceil(STATS.POP().POP.data(HCLASS.CITIZEN).get(RACES.all().get(i))*RND.rExpo());
				}
				riot(races);
			}
		});
		
		clear();

	}
	
	@Override
	public void save(FilePutter file) {
		file.d(timer);
		file.d(secondsToRiot);
		file.i(currentRioteers);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		timer = file.d();
		secondsToRiot = file.d();
		currentRioteers = file.i();
	}
	
	@Override
	public void clear() {
		timer = 0;
		secondsToRiot = 0;
		currentRioteers = 0;
	}
	
	void update(double ds) {
		
		timer += ds;
		if (timer < 10)
			return;
		timer -= 10;
		
		
		if (STATS.POP().pop(HTYPE.RIOTER) > 0) {
			if ((double)STATS.POP().pop(HTYPE.RIOTER)/currentRioteers < 0.3) {
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (a.indu().hType() == HTYPE.RIOTER) {
							a.HTypeSet(RND.oneIn(5) ? HTYPE.PRISONER : HTYPE.SUBJECT, CAUSE_LEAVE.PUNISHED, null);
						}
					}
				}
				new MessageText(¤¤Over, ¤¤success).send();;
			}
			
			secondsToRiot -= 10;
			if (secondsToRiot <= 0) {
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (a.indu().hType() == HTYPE.RIOTER) {
							a.HTypeSet(HTYPE.SUBJECT, null, null);
						}
					}
				}
				new MessageText(¤¤Over, ¤¤OverD).send();
			}
			
		}
		
		
		
	}
	
	void riot(int[] races) { 
		
		currentRioteers = 0;
		
		
		secondsToRiot = TIME.secondsPerDay*0.25 + RND.rFloat()*TIME.secondsPerDay*0.75;
		int[] tot = new int[RACES.all().size()];
		
		
		for (int i = 0; i < races.length; i++) {
			tot[i] = races[i];
		}
		
		Humanoid first = null;
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (a.indu().hType() == HTYPE.SUBJECT && races[a.race().index] > 0) {
					first = a;
					break;
				}
			}
		}
		
		if (first == null)
			return;
		
		RES.flooder().init(this);
		RES.flooder().pushSloppy(first.tc(), 0);
		while(RES.flooder().hasMore()) {
			PathTile c = RES.flooder().pollSmallest();
			for (ENTITY e : SETT.ENTITIES().getAtTile(c.x(), c.y())) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					if (a.indu().hType() == HTYPE.SUBJECT && races[a.race().index] > 0) {
						races[a.race().index] --;
						currentRioteers ++;
						a.HTypeSet(HTYPE.RIOTER, null, null);
						
					}
				}
			}
			for (DIR d : DIR.ALL) {
				if (!SETT.PATH().solidity.is(c, d) && SETT.IN_BOUNDS(c, d))
					RES.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
			}
		}
		RES.flooder().done();
		
		
		if (currentRioteers > 0) {
			
			Str t = Str.TMP;
			t.clear();
			
			t.add(¤¤amount).insert(0, currentRioteers);
			t.NL();
			for (Race r : RACES.all()) {
				if (tot[r.index] - races[r.index] > 0) {
					t.NL();
					t.add(r.info.names);
				}
			}
			
			new MessageText(¤¤riot, ¤¤riotD).paragraph(t).send();
			
			GAME.stats().RIOTS.inc(1);
			
		}
	}

	


	


	
}
