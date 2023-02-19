package settlement.army.ai.general;

import static settlement.main.SETT.*;

import java.io.IOException;

import game.time.TIME;
import init.C;
import settlement.army.Div;
import settlement.army.order.DivTDataTask.DIVTASK;
import snake2d.util.file.*;
import snake2d.util.sets.INDEXED;

final class GDiv implements INDEXED, SAVABLE{

	private short index;
	private final Context context;
	public boolean updated;
	public boolean active;
	private double leaveAloneUntil;
	public int tx, ty;
	
	GDiv(int index, Context context){
		this.index = (short) index;
		this.context = context;
	}
	
	public void init() {
		active = isActive();
	}
	
	public boolean hasPosition() {
		if (!div().order().active())
			return false;
		div().order().status.get(context.status);
		tx = context.status.currentPixelCX() >> C.T_SCROLL;
		ty = context.status.currentPixelCY() >> C.T_SCROLL;
		if (!IN_BOUNDS(tx, ty)) {
			return false;
		}
		return true;
	}
	
	private boolean isActive() {
		if (!hasPosition())
			return false;
		if (!div().order().active())
			return false;
		if (context.status.enemyCollisions() > 0) {
			div().order().next.get(context.form);
			context.pmap.markPos(context.form);
			return false;
		}
		if (leaveAloneUntil > TIME.currentSecond()) {
			div().order().task.get(context.task);
			if (context.task.task() != DIVTASK.STOP) {
				div().order().next.get(context.form);
				context.pmap.markPos(context.form);
				return false;
			}
			leaveAloneUntil = 0;
		}
		
		div().order().trajectory.get(context.traj);
		if (context.traj.hasAny())
			return false;
		
		div().settings.moppingSet(false);
		div().settings.setBestAmmo();
		
		return true;
	}
	
	public void timeout(double time) {
		leaveAloneUntil = TIME.currentSecond()+time;
	}

	@Override
	public void save(FilePutter file) {
		file.bool(updated);
		file.d(leaveAloneUntil);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		updated = file.bool();
		leaveAloneUntil = file.d();
	}

	@Override
	public void clear() {
		updated = false;
		leaveAloneUntil = 0;
	}

	public Div div() {
		return context.army.divisions().get(index);
	}
	
	@Override
	public int index() {
		return index;
	}

	void reset() {
		updated = false;
		div().settings.moppingSet(false);
		div().settings.fireAtWill = true;
		div().settings.musteringSet(true);
		div().settings.guard = true;
		div().settings.setBestAmmo();
		div().settings.running = false;
		context.task.stop();
		div().order().task.set(context.task);
	}
	
}
