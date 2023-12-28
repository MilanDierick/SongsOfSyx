package settlement.army.ai;

import java.io.IOException;

import game.GameDisposable;
import game.Profiler;
import settlement.army.ai.divs.ARMY_AI_DIVS;
import settlement.army.ai.fire.ARMY_AI_TRAJECT;
import settlement.army.ai.general.Strategos2000;
import settlement.army.ai.util.ArmyAIUtilThread;
import settlement.army.order.DivTDatas;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import snake2d.SlaveThread;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;

public final class ARMY_AI extends SettResource{

	public final DivTDatas orders = new DivTDatas();
	private final ArmyThread[] threads = new ArmyThread[] {
		new ARMY_AI_DIVS(this),
		new ArmyAIUtilThread(),
		new ARMY_AI_TRAJECT(),
		new Strategos2000(),
	};
	
	public ARMY_AI(){
		
	}
	
	@Override
	protected void update(float ds, Profiler profiler) {
		start();
		for (ArmyThread t : threads)
			t.doInMainThread();
		for (ArmyThread t : threads)
			t.thread().checkForUnresponsiveness();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.mark(this);
		stop();
		file.mark(orders);
		orders.save(file);
		for (ArmyThread t : threads) {
			file.mark(t);
			t.save(file);
			file.mark(t);
		}
		file.mark(this);
	}
	
	private void stop() {
		for (ArmyThread t : threads) {
			t.thread().setStopFlag();
		}
		
		for (ArmyThread t : threads) {
			t.thread().waitUntilStopped();
		}
	}
	
	private void start() {
		for (ArmyThread t : threads) {
			if (!t.thread().working()) {
				t.thread().start(t);
			}
		}
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		file.check(this);
		stop();
		file.check(orders);
		orders.load(file);
		init(false);
		for (ArmyThread t : threads) {
			file.check(t);
			t.load(file);
			file.check(t);
		}
		file.check(this);
	}
	
	@Override
	protected void clearBeforeGeneration(CapitolArea area) {
		stop();
		orders.clear();
		for (ArmyThread t : threads)
			t.clear();
	}
	
	@Override
	protected void init(boolean loaded) {
		for (ArmyThread t : threads)
			t.init();
	}
	
	public static abstract class ArmyThread implements ACTION, SAVABLE{
		
		public abstract SlaveThread thread();
		public void doInMainThread() {
			
			
		}
		public abstract void init();
		
	}

	public void pause() {
		stop();
		
	}
	
	public void unpause() {
		start();
		
	}
	
}
