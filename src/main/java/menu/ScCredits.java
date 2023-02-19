package menu;

import init.D;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.screens.Screener;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.gui.table.GScrollable;

class ScCredits implements SCREEN{

	
	private final GuiSection current;	
	final CharSequence ¤¤name = "¤credits";
	private final ArrayList<CredCollection> all = new ArrayList<>(16);
	
	ScCredits(Menu menu) {
		
		D.t(this);
		GuiSection main = new GuiSection();
		
		main.add(new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		});
		
		Json json = new Json(PATHS.BASE().DATA.get("Credits"));
		
		for (String s : json.keys()) {
			Json jj = json.json(s);
			new CredCollection(jj.text("TITLE"), 
					jj.texts("CREDS"));
		}
		
		int size = 0;
		for (CredCollection c : all)
			size += c.creds.length+2;
		
		final int z = size;
		
		ScrollRow[] butts = new ScrollRow[] {
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
			new Savebutt(),
		};
		
		Scrollable s = new GScrollable(butts) {
			
			@Override
			public int nrOFEntries() {
				return z;
			}
		};
		
		CLICKABLE c = s.getView();
		c.body().centerIn(main.body());
		main.add(c);
		
		current = main;
		
	}

	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (current.hover(mCoo))
			return true;
		return false;
	}

	@Override
	public boolean click() {
		current.click();
		return false;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		current.render(r, ds);
		
	}
	
	
	
	private class CredCollection {
		
		private final CharSequence title;
		private final CharSequence[] creds;
		
		
		CredCollection(CharSequence title, CharSequence... creds){
			this.title = title;
			this.creds = creds;
			all.add(this);
		}
		
	}
	
	private class Savebutt extends RENDEROBJ.RenderImp implements ScrollRow {

		int index = -1;
		
		public Savebutt() {
			body.setWidth(1000);
			body.setHeight(32);
		}
		
		@Override
		public void init(int index) {
			this.index = index;
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int i = index;
			if (index < 0)
				return;
			CredCollection cr = null;
			
			for (CredCollection c : all) {
				if (i - c.creds.length-2 > 0) {
					i -= c.creds.length+2;
					
				}else {
					cr = c;
					break;
				}
			}
			
			if (cr == null)
				return;
			
			if (i == 0)
				return;
			
			if (i == 1) {
				GUI.COLORS.copper.bind();
				UI.FONT().H2.renderCX(r, body().cX(), body().y1(),cr.title);
			}else if (i-2 < cr.creds.length){
				GUI.COLORS.label.bind();
				UI.FONT().H2.renderCX(r, body().cX(), body().y1(),cr.creds[i-2]);
				//UI.FONT().M.render(r, cr.creds[i-2], body().cX(), body().y1());
			}
			COLOR.unbind();
			
			
		}
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	
}
