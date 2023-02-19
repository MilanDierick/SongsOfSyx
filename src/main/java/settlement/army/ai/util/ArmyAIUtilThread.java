package settlement.army.ai.util;

import java.io.IOException;

import game.GAME;
import init.RES;
import settlement.army.ai.ARMY_AI.ArmyThread;
import snake2d.SlaveThread;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class ArmyAIUtilThread extends ArmyThread{
	
	static ArmyAIUtilThread self;
	private final Updater updater = new Updater();
	private int upI = 0;
	
	private ArmyAIUtil[] util = new ArmyAIUtil[] {
		new ArmyAIUtil(),
		new ArmyAIUtil()
	};
	private boolean swapping = false;
	
	public ArmyAIUtilThread() {
		self = this;
		new Tests(self);
	}
	
	@Override
	public void exe() {
		if (Math.abs(upI-GAME.updateI()) < 30)
			return;
		upI = GAME.updateI();
		if (swapping) {
			ArmyAIUtil a = util[0];
			util[0] = util[1];
			util[1] = a;
			util[0].copy();
			swapping = false;
			return;
		}
		updater.init(util[1]);
		swapping = true;
	}
	
	@Override
	public void init() {
		
		swapping = false;
		updater.init(util[0]);
		util[0].copy();
		upI = 0;
	}
	
	@Override
	public SlaveThread thread() {
		return RES.generalThread3();
	}
	
	ArmyAIUtil current() {
		return util[0];
	}
	

	@Override
	public void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		upI = 0;
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
