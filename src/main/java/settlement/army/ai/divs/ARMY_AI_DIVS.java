package settlement.army.ai.divs;

import java.io.IOException;

import game.time.TIME;
import init.RES;
import init.config.Config;
import settlement.army.Div;
import settlement.army.ai.ARMY_AI;
import settlement.army.ai.ARMY_AI.ArmyThread;
import snake2d.SlaveThread;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ARMY_AI_DIVS extends ArmyThread{

	private int divI = 0;
	private int upI = 0;
	private final AIManager[] ais = new AIManager[Config.BATTLE.DIVISIONS_PER_ARMY*2];
	public final Tools tools = new Tools();
	public final Plans plans;
	static ARMY_AI_DIVS self;
	private double currentSecond = TIME.currentSecond();
	
	public ARMY_AI_DIVS(ARMY_AI a) {
		self = this;
		
		plans = new Plans(tools);
		
		for (int i = 0; i < ais.length; i++) {
			ais[i] = new AIManager(plans.longSize, a.orders.get(i));
		}
	}

	AIManager getOther(Div div) {
		return ais[div.index()];
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(divI);
		for (int i = 0; i < ais.length; i++)
			ais[i].save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		divI = file.i();
		for (int i = 0; i < ais.length; i++)
			ais[i].load(file);
	}

	@Override
	public void clear() {
		divI = 0;
		for (int i = 0; i < ais.length; i++)
			ais[i].clear();
	}

	@Override
	public void exe() {
		double curr = TIME.currentSecond();
		int millis = (int) (1000*(curr-currentSecond));		
		currentSecond = curr;
		PlanWalkAbs.amountOfPaths = 0;
		for (int i = 0; i < ais.length && thread().working(); i++) {
			ais[i].update(upI, millis);
		}
		upI++;
	}
	
	@Override
	public SlaveThread thread() {
		return RES.generalThread2();
	}

	@Override
	public void init() {
		
		
	}
	
}
