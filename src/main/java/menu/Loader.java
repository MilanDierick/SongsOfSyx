package menu;

import java.nio.file.Path;

import game.GameLoader;
import init.C;
import init.D;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Font;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;
import util.save.SaveGame;

class Loader implements SC{

	private static CharSequence ¤¤notLoaded = "¤Game could not be loaded!";
	
	static {
		D.ts(Loader.class);
	}
	
	private SC prev;
	private RENDEROBJ s;
	private final Menu menu;
	private int WIDTH = 600;
	private int HEIGHT = C.HEIGHT()-100;
	
	
	
	
	Loader(Menu menu){
		this.menu = menu;
	}
	
	public void load(Path file) {
		load(file, true);
	}
	
	public void load(Path file, boolean achive) {
		
		if (menu.screen() == this)
			return;
		
		prev = menu.screen();
		
		CharSequence e = SaveGame.problem(file, false);
		
		if (e == null) {
			menu.start(new GameLoader(file, achive));
		}else {
			s = makeSection(""+e);
			s.body().centerIn(C.DIM());
			menu.switchScreen(this);
		}
		
	}
	
	
	protected RENDEROBJ makeSection(String body) {
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		Font f = UI.FONT().M;
		
		rows.add(new GText(f, ¤¤notLoaded).lablify().r(DIR.C));
		
		int mw = 0;
		
		int ei = 0;
		while(ei < body.length()) {
			int n = f.getEndIndex(body, ei, WIDTH);
			GTextR t = new GTextR(f, body.subSequence(ei, n));
			mw = Math.max(mw, t.body().width());
			rows.add(new GTextR(f, body.subSequence(ei, n)));
			ei = n+1;
		}
		
		if (rows.size()*f.height() < HEIGHT) {
			GuiSection s = new GuiSection();
			for (RENDEROBJ r : rows)
				s.addDown(0, r);
			return s;
		}
		
		rows.add(new RENDEROBJ.RenderDummy(mw+16, 1));
		return new GScrollRows(rows, HEIGHT).view();
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean click() {
		menu.switchScreen(prev);
		return true;
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		s.render(r, ds);
		
	}

	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(prev);
		return false;
	}
	
}
