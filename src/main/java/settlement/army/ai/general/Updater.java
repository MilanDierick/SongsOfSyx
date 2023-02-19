package settlement.army.ai.general;

import java.io.IOException;

import game.time.TIME;
import snake2d.util.file.*;

final class Updater implements SAVABLE{

	private final Context c;
	private double waitUntil = 0;
	private double lastInit = 0;
	private final UpdaterDivs divs;
	private int oldActive = 0;
	private double artilleryTime;
	
	private boolean inited = false;
	
	
	Updater(Context context){
		this.c = context;
		divs = new UpdaterDivs(context);
	}
	
	void update() {
		if (waitUntil >= TIME.currentSecond())
			return;
		if (!inited) {
			if (oldActive == 0) {
				artilleryTime = TIME.currentSecond();
				for (GDiv d : c.divs) {
					d.reset();
				}
			}
			
			oldActive = 0;
			c.pmap.init();
			for (GDiv d : c.divs) {
				d.updated = false;
				d.init();
				if (d.div().order().active())
					oldActive++;
			}
			
			if (c.artillery.bombard()) {
				if (TIME.currentSecond()-artilleryTime < TIME.secondsPerDay) {
					waitt(20);
					return;
				}
			}else {
				artilleryTime -= TIME.secondsPerDay/10;
			}
			
			c.groups.init();
			inited = true;
			lastInit = TIME.currentSecond();
			return;
		}else {
			int am = 0;
			for (GDiv d : c.divs) {
				if (d.div().order().active())
					am++;
			}
			if (am != oldActive) {
				inited = false;
				waitt(4);
				return;
			}
		}
		
		if (c.groups.moveToLine()) {
			return;
		}
		
		if (divs.update())
			return;
		
		if (c.groups.waitForMovement())
			return;
		
		waitt(lastInit-TIME.currentSecond()+10);
		inited = false;
		
	}
	
	private void waitt(double seconds) {
		waitUntil = TIME.currentSecond()+seconds;
	}
	
	
	@Override
	public void save(FilePutter file) {
		file.bool(inited);
		file.d(waitUntil);
		file.d(lastInit);
		file.i(oldActive);
		file.d(artilleryTime);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		inited = file.bool();
		waitUntil = file.d();
		lastInit = file.d();
		oldActive = file.i();
		artilleryTime = file.d();
	}

	@Override
	public void clear() {
		waitUntil = 0;
		oldActive = 0;
		lastInit = 0;
		inited = false;
		artilleryTime = 0;
	}
	
}
