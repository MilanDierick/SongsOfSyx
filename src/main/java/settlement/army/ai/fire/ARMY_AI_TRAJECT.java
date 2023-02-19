package settlement.army.ai.fire;

import java.io.IOException;

import game.time.TIME;
import init.RES;
import settlement.army.ai.ARMY_AI.ArmyThread;
import settlement.main.SETT;
import snake2d.SlaveThread;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ARMY_AI_TRAJECT extends ArmyThread{

	private int divI = 0;
	private double currentSecond = TIME.currentSecond();
	private final UpdaterTraj up = new UpdaterTraj();
	private final UpdaterArtillery art = new UpdaterArtillery();
	
	public ARMY_AI_TRAJECT() {
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(divI);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		divI = file.i();
	}

	@Override
	public void clear() {
		divI = 0;
	}

	@Override
	public void exe() {
		
		
		
		double curr = TIME.currentSecond();
		double ds = (curr-currentSecond)*RES.config().BATTLE.DIVISIONS_PER_BATTLE;
		if (ds > 0) {
			int old = divI;
			while(ds > 0) {
				
				if (divI == RES.config().BATTLE.DIVISIONS_PER_BATTLE)
					art.update();
				else {
					up.update(SETT.ARMIES().division((short) divI), 0);
				}
				divI++;
				divI %= RES.config().BATTLE.DIVISIONS_PER_BATTLE+1;
				if (divI == old)
					break;
				ds -= 1;
			}
			currentSecond = TIME.currentSecond();
		}
		
		
		
	}
	
	@Override
	public SlaveThread thread() {
		return RES.generalThread4();
	}

	@Override
	public void init() {
		clear();
		
	}
	
}
