package settlement.army.order;

import java.io.IOException;

import settlement.army.Div;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class DivTDataTask implements Copyable<DivTDataTask>{

	public static final DivTDataTask TMP = new DivTDataTask();
	private static final Coo coo = new Coo();
	private DIVTASK task = DIVTASK.MOVE;
	private DIVTASK prev = DIVTASK.STOP;
	
	public static enum DIVTASK {
		
		ATTACK_BUILDING(false, true),
		ATTACK_MELEE(false, true),
		ATTACK_RANGED(false, true),
		MOVE(true, true),
		CHARGE(false, false),
		STOP(false, false),
		FIGHTING(false, false),
		;
		
		public final boolean showDest;
		public final boolean showPath;
		
		private DIVTASK(boolean dest, boolean path) {
			showDest = dest;
			showPath = path;
		}
		
		public static final LIST<DIVTASK> all = new ArrayList<DIVTASK>(values());
	}
	
	private int target;
	
	public DIVTASK task() {
		return task;
	}
	
	public DIVTASK taskPrev() {
		return prev;
	}
	
	public void interruptAttack() {
		prev = task;
		task = DIVTASK.FIGHTING;
	}
	
	public void setPrev() {
		if (prev == DIVTASK.CHARGE || prev == DIVTASK.FIGHTING)
			prev = DIVTASK.STOP;
		task = prev;
	}
	
	public void move() {
		task = DIVTASK.MOVE;
	}
	
	public void stop() {
		task = DIVTASK.STOP;
	}
	
	public void attack(int tx, int ty) {
		target = tx | ty <<16;
		task = DIVTASK.ATTACK_BUILDING;
	}
	
	public void attackMelee(Div other) {
		target = other.index();
		task = DIVTASK.ATTACK_MELEE;
	}
	
	public void attackRanged(Div other) {
		target = other.index();
		task = DIVTASK.ATTACK_RANGED;
	}
	
	public void charge() {
		task = DIVTASK.CHARGE;
	}

	@Override
	public void save(FilePutter file) {
		file.i(task.ordinal());
		file.i(prev.ordinal());
		file.i(target);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		
		task = DIVTASK.all.get(file.i());
		prev = DIVTASK.all.get(file.i());
		target = file.i();
	}

	@Override
	public void clear() {
		task = DIVTASK.MOVE;
		prev = DIVTASK.STOP;
		target = 0;
	}

	@Override
	public void copy(DivTDataTask toBeCopied) {
		task = toBeCopied.task;
		target = toBeCopied.target;
		prev = toBeCopied.prev;
	}
	
	public Div targetDiv() {
		if (task != DIVTASK.ATTACK_MELEE && task != DIVTASK.ATTACK_RANGED)
			return null;
		return SETT.ARMIES().division((short) target);
	}
	
	public COORDINATE targetTile() {
		if (task != DIVTASK.ATTACK_BUILDING)
			return null;
		int x = target &0x0FFFF;
		int y = (target >> 16)&0x0FFFF;
		coo.set(x, y);
		return coo;
	}
	

	
	
	
}
