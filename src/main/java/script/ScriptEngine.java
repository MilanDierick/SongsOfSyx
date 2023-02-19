package script;

import java.io.*;

import game.GAME.GameResource;
import script.SCRIPT.SCRIPT_INSTANCE;
import snake2d.*;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import util.gui.misc.GBox;
import view.interrupter.IDebugPanel;

public class ScriptEngine extends GameResource {

	private LIST<Script> scripts = new ArrayList<Script>(0);
	public static boolean skipScriptOnce = false;

	public ScriptEngine() {
		IDebugPanel.add("Skip tut", new ACTION() {
			@Override
			public void exe() {
				skipScriptOnce = true;
			}
		});
	}

	public void initBefore(LIST<ScriptLoad> scripts) {
		
		for (ScriptLoad l : scripts) {
			try {
				l.script.initBeforeGameCreated();
			}catch(Exception e) {
				error(l, e);
			}
		}
	}
	
	private void error(ScriptLoad l, Exception e) {
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.append("error in script " + l.className);
		out.append(System.lineSeparator());
		e.printStackTrace(out);
		
		throw new Errors.DataError(writer.toString(), l.file);
		
	}
	
	public void set(LIST<ScriptLoad> scripts) {
		LinkedList<Script> res = new LinkedList<>();
		for (ScriptLoad l : scripts) {
			LOG.ln("adding script : " + l.script.name());
			res.add(new Script(l, l.script.initAfterGameCreated()));
		}
		this.scripts = new ArrayList<Script>(res);
	}
	
	public LIST<ScriptLoad> makeCurrent(){
		LinkedList<ScriptLoad> res = new LinkedList<>();
		for (Script s : scripts)
			if (s.load.file != null)
				res.add(s.load);
		return res;
	}

	@Override
	protected void save(FilePutter file) {
		file.mark(this);
		file.i(scripts.size());
		for (Script s : scripts) {
			
			
			s.load.save(file);
			
			int pos = file.getPosition();
			file.i(0);
			s.ins.save(file);
			int size = (file.getPosition()-pos)-4;
			file.setAtPosition(pos, size);
		}
		
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.check(this);
		int am = file.i();
		LinkedList<Script> res = new LinkedList<>();
		while(am > 0) {
			am--;
			ScriptLoad ld = ScriptLoad.get(file);
			int size = file.i();
			int position = file.getPosition();
			if (ld != null) {
				SCRIPT_INSTANCE ins = ld.script.initAfterGameCreated();
				ins.load(file);
				if (size != (file.getPosition()-position)) {
					LOG.ln("Unable to load script. Was saved with + " + size + " bytes, but read " + (file.getPosition()-position) + " " + ins.hashCode());
					file.setPosition(position+size);
					if (ins.handleBrokenSavedState()) {
						LOG.ln("Script wants to carry on anyway, so be it.");
					}else
						continue;
				}
				
				Script s = new Script(ld, ins);
				res.add(s);
			}else {
				LOG.ln("Unable to find script. Skipping. " + size );
				for (int i = 0; i < size; i++)
					file.b();
			}
			
		}
		this.scripts = new ArrayList<Script>(res);
		
		
	}

	@Override
	protected void update(float ds) {
		
		for(Script s : scripts)
			try {
				s.ins.update(ds);
			}catch(Exception e) {
				error(s.load, e);
			}
		skipScriptOnce = false;
	}

	public void hoverTimer(double mouseTimer, GBox text) {
		for(Script s : scripts)
			try {
				s.ins.hoverTimer(mouseTimer, text);
			}catch(Exception e) {
				error(s.load, e);
			}
			
	}

	public void render(Renderer r, float ds) {
		for(Script s : scripts)
			try {
				s.ins.render(r, ds);
			}catch(Exception e) {
				error(s.load, e);
			}
			
	}

	

	public void mouseClick(MButt button) {
		for(Script s : scripts)
			try {
				s.ins.mouseClick(button);
			}catch(Exception e) {
				error(s.load, e);
			}
			
	}

	public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		for(Script s : scripts)
			try {
				s.ins.hover(mCoo, mouseHasMoved);
			}catch(Exception e) {
				error(s.load, e);
			}
			
	}
	
	private static class Script {
		
		private final ScriptLoad load;
		private final SCRIPT_INSTANCE ins;
		
		Script(ScriptLoad load, SCRIPT_INSTANCE ins){
			this.load = load;
			this.ins = ins;
		}
		
	}


}
