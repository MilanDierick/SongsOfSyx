package game.events.tutorial;

import java.io.IOException;
import java.io.Serializable;

import game.events.EVENTS.EventResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListGrower;

public class Tutorial extends EventResource {

	private final ArrayListGrower<Goal> goals;
	

	public Tutorial() {

		goals = new Goals().all;	
		
	}
	
	public boolean enabled = false;
	
	@Override
	public void save(FilePutter file) {	
		file.bool(enabled);
		file.i(goals.size());
		
		for (Goal g : goals) {
			file.bool(g.isActive);
			file.bool(g.isClosed);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		clear();
		enabled = file.bool();
		int am = file.i();
		for (int i = 0; i < am; i++) {
			if (i < goals.size()) {
				goals.get(i).isActive = file.bool();
				goals.get(i).isClosed = file.bool();
			}else {
				file.bool();
				file.bool();
			}
		}
	}

	@Override
	public void update(double ds) {
		if (!enabled)
			return;

		for (Goal g : goals) {
			if (g.isClosed)
				continue;
			if (!g.isActive) {
				
				if (g.isActive()) {
					g.isActive = true;
					g.activateAction();
					new MessTut(g.info.name, g.info.desc, g.info.mission, g.hilight).send();
				}
			}else if (!g.isClosed) {
				g.isClosed = g.isAccomplished();
			}
		}
		

	}

	@Override
	protected void clear() {
		for (Goal g : goals) {
			g.isActive = false;
			g.isClosed = false;
		}
	}

	public static interface bHov extends Serializable {
		
		void hov();
		
	}
	
}
