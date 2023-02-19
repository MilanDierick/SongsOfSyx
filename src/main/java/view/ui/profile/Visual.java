package view.ui.profile;

import game.faction.*;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.*;
import view.common.BitmapSpriteEditor;
import world.World;
import world.map.regions.Region;

class Visual extends GuiSection{

	final FBanner b = FACTIONS.player().banner();
	Visual(int height){
		GColorPicker.class.getClass();
		D.gInit(this);
		add(bannerHeader());
		addRelBody(8, DIR.S, banner());
		addRelBody(16, DIR.E, colors());
		
		addRelBody(16, DIR.N, info());
		addRelBody(8, DIR.S, loadButts());
		
		addRelBody(16, DIR.E, new Colors(body().height()));
	}
	
	private static GuiSection loadButts() {
		GuiSection s = new GuiSection();
		
		s.add(new GButt.Glow(D.g("Save-Profile", "Save as Default")) {
			@Override
			protected void clickA() {
				FactionProfileFlusher.flush(FACTIONS.player());
			}
		});
		
		s.addRightC(32, new GButt.Glow(D.g("Load-Profile", "Load as Default")) {
			
			@Override
			protected void renAction() {
				activeSet(FactionProfileFlusher.canLoad(FACTIONS.player()));
			}
			
			@Override
			protected void clickA() {
				FactionProfileFlusher.load(FACTIONS.player());
			}
			
		});
	
		return s;
	}
	
	private static GuiSection info() {
		GuiSection s = new GuiSection();
		StringInputSprite t = new StringInputSprite(28, UI.FONT().H2) {
			@Override
			protected void change() {
				FACTIONS.player().appearence().name().clear().add(text());
			}
		};
		GInput in = new GInput(t);
		t.text().clear().add(FACTIONS.player().appearence().name());
		s.add(new GHeader(D.g("fname", "Faction Name")));
		s.addRightC(8, in);
		
		t = new StringInputSprite(28, UI.FONT().H2) {
			@Override
			protected void change() {
				FACTIONS.player().ruler().name.clear().add(text());
			}
		};
		in = new GInput(t);
		t.text().clear().add(FACTIONS.player().ruler().name);
		s.add(new GHeader(D.g("rname", "Ruler Name")), 0, s.getLastY2()+12);
		s.addRightC(8, in);
		
		return s;
	}
	
	private static GuiSection banner() {
		return new BitmapSpriteEditor(FACTIONS.player().banner().sprite);
	}
	
	private static GuiSection bannerHeader() {
		final FBanner b = FACTIONS.player().banner();
		GuiSection s = new GuiSection();
		s.add(b.HUGE, 0, 0);
		s.addRightC(8, b.BIG);
		s.addRightC(8, b.MEDIUM);
		
		s.addRightC(20, new GHeader(D.g("flag")));
		s.addRightC(16, new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				b.bannerTypeSet(b.bannerType()-1);
			}
		});
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				b.bannerTypeSet(b.bannerType()+1);
			}
		});
		return s;
	}
	
	private static GuiSection colors() {
		final FBanner b = FACTIONS.player().banner();
		GuiSection s = new GuiSection();
		s.add(new GColorPicker(false, D.g("background")) {
			
			@Override
			public ColorImp color() {
				
				return b.colorBG();
			}
			
			@Override
			public void change() {
				for (Region r : FACTIONS.player().kingdom().realm().regions())
					World.MINIMAP().updateRegion(r);
			}
		});
		s.addDownC(8, new GColorPicker(false, D.g("foreground")) {
			
			@Override
			public ColorImp color() {
				return b.colorFG();
			}
		});
		s.addDownC(8, new GColorPicker(false, D.g("border")) {
			
			@Override
			public ColorImp color() {
				return b.colorBorder();
			}
		});
		s.addDownC(8, new GColorPicker(false, D.g("pole")) {
			
			@Override
			public ColorImp color() {
				return b.colorPole();
			}
		});
		return s;
	}
	

	
}
