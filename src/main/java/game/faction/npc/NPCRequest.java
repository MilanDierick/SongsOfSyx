package game.faction.npc;

import java.io.IOException;

import game.faction.npc.ruler.ROpinions;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class NPCRequest{

	private boolean has = false;
	private double time = 0;
	private double penalty = 0;
	private final FactionNPC f;
	
	NPCRequest(FactionNPC fa) {
		this.f = fa;
	}
	
	public boolean has() {
		return has;
	}
	
	public void set(double penalty) {
		time = TIME.currentSecond();
		this.penalty = penalty;
	}

	public void clear() {
		has = false;
	}
	
	public void expire() {
		if (has) {
			has = false;
			ROpinions.favour(f, penalty);
		}
	}
	
	void update() {
		if (has & Math.abs(TIME.currentSecond()-time) > TIME.secondsPerDay){
			expire();
		}
	}


	void save(FilePutter file) {
		file.bool(has);
		file.d(time);
		file.d(penalty);
	}

	void load(FileGetter file) throws IOException {
		has = file.bool();
		time = file.d();
		penalty = file.d();
	}
	
}
