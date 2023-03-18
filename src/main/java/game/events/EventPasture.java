package game.events;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.D;
import settlement.main.SETT;
import settlement.room.food.pasture.PastureInstance;
import settlement.room.food.pasture.ROOM_PASTURE;
import snake2d.util.MATH;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import view.main.MessageText;
import view.main.VIEW;
import view.sett.IDebugPanelSett;

public class EventPasture extends EventResource{

	private static CharSequence ¤¤mTitle = "¤Livestock Dying";
	private static CharSequence ¤¤mBody = "¤Terrible news! our {0} have been afflicted by a disease. As a countermeasure, our herders have culled the sick animals, {1}% of them.";
	
	private double time;
	private int nextPasture = RND.rInt();
	
	static {
		D.ts(EventPasture.class);
	}
	
	EventPasture(){
		IDebugPanelSett.add("Event: Livestock", new ACTION() {
			
			@Override
			public void exe() {
				int ri = RND.rInt(SETT.ROOMS().PASTURES.size());
				for (int i = 0; i < SETT.ROOMS().PASTURES.size(); i++) {
					ROOM_PASTURE f = SETT.ROOMS().PASTURES.getC(i+ri);
					if (f.instancesSize() > 0) {
						nextPasture = i+ri;
						break;
					}
					
				}
				event();
			}
		});
	}
	
	@Override
	protected void update(double ds) {
		
		if (VIEW.b().isActive())
			return;
		
		time -= ds;
		if (time > 0)
			return;
		
		event();
		reset();
		
	}

	private void reset() {
		time += (1 + RND.rFloat())*8*TIME.years().bitSeconds();
		nextPasture = RND.rInt();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(time);
		file.i(nextPasture);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		time = file.d();
		nextPasture = file.i();
	}

	@Override
	protected void clear() {
		time = -1;
	}	
	
	private void event() {
		
		ROOM_PASTURE p = SETT.ROOMS().PASTURES.getC(nextPasture);
		
		if (p.instancesSize() == 0)
			return;
		
		if (p.employment().employed() < 5)
			return;
		
		double death = 0.2 + MATH.pow15.pow(RND.rFloat())*0.8;
		int tot = 0;
		
		for (int i = 0; i < p.instancesSize(); i++) {
			PastureInstance ins = p.getInstance(i);
			int d = (int) Math.ceil(Math.ceil(ins.animalsCurrent()*death)); 
			tot += ins.kill(d);
		}
		
		if (tot > 0) {
			new MessageText(¤¤mTitle).paragraph(Str.TMP.clear().add(¤¤mBody).insert(0, p.species.names).insert(1, (int)(100*(death)))).send();
		}
	}

}
