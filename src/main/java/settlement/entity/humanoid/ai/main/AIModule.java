package settlement.entity.humanoid.ai.main;

import game.GameDisposable;
import game.GameDisposable.Counter;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.main.ROOMA;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;

public abstract class AIModule implements INDEXED {
	
	static Counter c = new GameDisposable.Counter();
	static final ArrayList<AIModule> all = new ArrayList<AIModule>(200);
	{
		all.add((AIModule)null);
	}
	final byte index;
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
				all.add((AIModule)null);
			}
		};
	}
	
	public AIModule() {
		index = (byte) all.add(this);
		if (index < 0)
			throw new RuntimeException();
	}
	
	public abstract AiPlanActivation getPlan(Humanoid a, AIManager d);
	protected void init(Humanoid a, AIManager d) {
		
	}
	protected void cancel(Humanoid a, AIManager d) {
		
	}
	protected abstract void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay);
	public abstract int getPriority(Humanoid a, AIManager d);

	public AiPlanActivation resume(Humanoid a, AIManager d, int timesResumedBefore) {
		return null;
	}
	
	public final boolean is(Humanoid a, AIManager d) {
		return AIModules.current(d) == this;
	}
	
	public final boolean moduleCanContinue(Humanoid a, AIManager d) {
		AIModule m = AIModules.next(d);
		return m == null || m == this;
	}
	
	public void evictFromRoom(Humanoid a, AIManager d, ROOMA r) {
		
	}

	@Override
	public int index() {
		return index;
	}

	
	
}