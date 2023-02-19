package init.sound;

import init.RES;
import init.sound.SoundSettlement.Sound;
import init.sprite.UI.UI;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GButt;
import util.gui.misc.GDropDown;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;

public final class SOUND {
	
	private static SOUND s;
	public final SoundGui gui = new SoundGui();
	public final SoundSettlement settlement = new SoundSettlement();
	public final SoundAmbience ambience = new SoundAmbience();
	public final Music music = new Music();
	
	public SOUND(RES.Data key){
		s = this;
		new Debug(settlement);
		
		
		
		
	}
	
	public static SoundSettlement sett() {
		return s.settlement;
	}
	
	public static SoundGui gui() {
		return s.gui;
	}
	
	public static Music music() {
		return s.music;
	}
	
	public static SoundAmbience ambience() {
		return s.ambience;
	}
	
	public void update(double gamespeed, float ds, float gds) {
		ambience.update(ds);
		music.update(ds);
	}
	
	private static class Debug extends PlacableSimple implements ACTION{
		
		private final Rec b = new Rec();
		private Sound ss;
		private final ArrayList<CLICKABLE> cl;
		
		@Override
		public void place(int x, int y) {
			b.moveC(x, y);
			ss.rnd(b);
		}
		
		@Override
		public CharSequence isPlacable(int x, int y) {
			return null;
		}
		
		Debug(SoundSettlement settlement){
			super("sound test");
			GDropDown<CLICKABLE> drop = new GDropDown<CLICKABLE>("sound");
			
			for (Sound s : settlement.action.all()) {
				if (ss == null)
					ss = s;
				drop.add(new GButt.ButtPanel(UI.FONT().S.getText(s.key)) {
					@Override
					protected void clickA() {
						ss = s;
						super.clickA();
					}
				});
			}
			
			drop.init();
			
			cl = new ArrayList<CLICKABLE>(drop);
			IDebugPanelSett.add(this);
		}

		@Override
		public void exe() {
			VIEW.s().tools.place(this);
		}
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			return cl;
		}

		
	}
	
}
