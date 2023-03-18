package settlement.army.ai.general;

import java.io.IOException;

import game.time.TIME;
import init.config.Config;
import settlement.army.ai.general.GroupsIniter.GroupDiv;
import settlement.army.ai.util.ArmyAIUtil;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.*;
import snake2d.util.sets.*;

final class Groups implements SAVABLE{

	private final LIST<GroupLine> all;
	private final Context c;
	Bitmap1D mark = new Bitmap1D(SETT.TAREA, false);
	private ArrayList<GroupLine> active = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private ArrayList<GroupLine> waits = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	private final GroupMover mover;
	private final GroupsIniter initer;
	private final GroupCharger charger;
	
	Groups(Context c){
		this.c = c;
		
		ArrayList<GroupLine> all = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		while(all.hasRoom())
			all.add(new GroupLine());

		this.all = all;
		mover = new GroupMover(c);
		initer = new GroupsIniter(c);
		charger = new GroupCharger(c);
	}
	
	public void init() {
		
		mark.clear();
		LIST<GroupDiv> groups = initer.init();
		
		active.clear();
		waits.clear();
		{
			int i = 0;
			for (GroupDiv d : groups) {
				GroupLine l = all.get(i++);
				l.clear();
				
				active.add(mover.setLine(d, l));
			}
		}
		
	
		
		for (GroupLine g: active) {

			double sx = g.start.x();
			double sy = g.start.y();
			for (int i = 0; i < g.width; i++) {
				int x = (int) (sx + g.v.nX()*i);
				int y = (int) (sy + g.v.nY()*i);
				if (SETT.IN_BOUNDS(x, y)) {
					mark.set(x+y*SETT.TWIDTH, true);
				}
			}
		}
		
		
	}


	
	public boolean moveToLine() {
		
		if (active.size() == 0)
			return false;
		
		GroupLine d = active.removeLast();
		if (mover.moveToLine(d)) {
			active.add(d);
		}else {
			d.waitUntil = TIME.currentSecond()+80;
			waits.add(d);
		}
		return active.size() > 0;
		
	}
	
	public boolean waitForMovement() {
		
		if (waits.size() == 0)
			return false;
		GroupLine d = waits.removeLast();
		if (d.firstRowDeployed == 0)
			return true;
		double tx = d.start.x();
		double ty = d.start.y();
		int am = 0;
		for (int i = 0; i <=d.width; i++) {
			if (ArmyAIUtil.map().hasAlly.is((int)tx, (int)ty, c.army))
				am++;
		}

		if (am > d.firstRowDeployed*0.9 && d.waitUntil - TIME.currentSecond() > 6) {
			d.waitUntil = TIME.currentSecond() + 5.5;
		}
		if (d.waitUntil > TIME.currentSecond())
			waits.add(d);
		else
			charger.charge(d);
		return true;
	}


	final static class GroupLine implements SAVABLE{
		
		private GroupLine() {
			
		}
		
		public Bitmap1D divsAll = new Bitmap1D(Config.BATTLE.DIVISIONS_PER_ARMY, false);
		public Bitmap1D divsTmp = new Bitmap1D(Config.BATTLE.DIVISIONS_PER_ARMY, false);
		public final  VectorImp v = new VectorImp();
		public int width;
		public final Coo start = new Coo();

		public int firstRowDeployed;
		public int left = 0;
		public int right = 0;
		public int extraRows;
		public int biggestH;
		public boolean shouldRun;
		
		private double waitUntil;
		
		@Override
		public void save(FilePutter file) {
			divsAll.save(file);
			divsTmp.save(file);
			v.save(file);
			start.save(file);
			file.i(width);

			file.i(firstRowDeployed);
			file.i(left);
			file.i(right);
			file.i(extraRows);
			file.i(biggestH);
			file.bool(shouldRun);
			file.d(waitUntil);
			
		}
		@Override
		public void load(FileGetter file) throws IOException {
			divsAll.load(file);
			divsTmp.load(file);
			v.load(file);
			start.load(file);
			width = file.i();
			
			firstRowDeployed = file.i();
			left = file.i();
			right = file.i();
			extraRows = file.i();
			biggestH = file.i();
			shouldRun = file.bool();
			waitUntil = file.d();
		}
		@Override
		public void clear() {
			divsAll.clear();
			divsTmp.clear();
			
			width = 0;
			firstRowDeployed = 0;
			left = 0;
			right = 0;
			extraRows = 0;
			biggestH =0;
			shouldRun = false;
			waitUntil = 0;
		}
		
		public int startX() {
			return (int) (start.x() + extraRows*-v.nY());
		}
		
		public int startY() {
			return (int) (start.y() + extraRows*v.nX());
		}
		
	}
	
	public void debug(GroupLine l) {
		LOG.ln(l + " " + l.start);
		for (GDiv d : c.divs) {
			if (l.divsAll.get(d.index()))
				LOG.ln(" " + d.div().index() + " " + l.divsTmp.get(d.index()));
		}
		LOG.ln();
	}

	@Override
	public void save(FilePutter file) {
		file.i(active.size());
		for (GroupLine l : active)
			l.save(file);
		file.i(waits.size());
		for (GroupLine l : waits)
			l.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		waits.clear();
		active.clear();
		int k = 0;
		for (int i = file.i(); i > 0; i--) {
			all.get(k).load(file);
			active.add(all.get(k++));
		}
		for (int i = file.i(); i > 0; i--) {
			all.get(k).load(file);
			waits.add(all.get(k++));
		}	
		
	}

	@Override
	public void clear() {
		waits.clear();
		active.clear();
	}

	
}
