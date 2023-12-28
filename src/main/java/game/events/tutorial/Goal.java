package game.events.tutorial;

import snake2d.util.datatypes.Rec;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

abstract class Goal {

	public boolean isActive;
	public boolean isClosed;
	public final GoalInfo info;
	public Rec hilight;
	
	Goal(LISTE<Goal> all, Json json, String key){
		all.add(this);
		info = new GoalInfo(json, key);
	}
	
	protected abstract boolean isActive();
	protected abstract boolean isAccomplished();
	
	protected void activateAction() {
		
	}
	
	public static abstract class GoalPrev extends Goal {

		private final Goal prev;
		
		GoalPrev(LISTE<Goal> all, Json json, String key) {
			super(all, json, key);
			prev = all.get(all.size()-2);
		}
		
		@Override
		protected boolean isActive() {
			return prev.isClosed;
		}
		
	}
	
	public static abstract class GoalBeforeEnd extends Goal {

		private final LISTE<Goal> all;
		
		GoalBeforeEnd(LISTE<Goal> all, Json json, String key) {
			super(all, json, key);
			this.all = all;
		}
		
		@Override
		protected final boolean isActive() {
			if (all.get(all.size()-1).isClosed)
				return false;
			return pIsActive();
		}
		
		protected abstract boolean pIsActive();
		
	}
	
}
