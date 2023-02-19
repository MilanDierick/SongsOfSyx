package script;

import java.io.IOException;

import snake2d.util.file.*;
import snake2d.util.sets.ArrayListResize;

public final class ScriptStates implements SAVABLE{

	private final ArrayListResize<ScriptState> states = new ArrayListResize<>(1, 200);
	
	public final void update(double ds) {
		
		for (ScriptState s : states) {
			if (!s.hasPassed) {
				if (s.condition()) {
					s.hasPassed = true;
					ScriptEngine.skipScriptOnce = false;
					s.action();
				}
				
				break;
			}
			
				
		}
	}
	
	public void add(ScriptState state) {
		states.add(state);
	}

	@Override
	public void save(FilePutter file) {
		file.i(states.size());
		for (ScriptState s : states)
			file.bool(s.hasPassed);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		for (ScriptState s : states)
			s.hasPassed = false;
		for (int k = 0; k < i; k++)
			states.get(k).hasPassed = file.bool();
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	public static abstract class ScriptState{
		
		private boolean hasPassed = false;
		
		public ScriptState() {
			
		}
		
		public abstract boolean condition();
		public abstract void action();
		
	}
	
}
