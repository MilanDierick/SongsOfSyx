package view.world.generator;

import game.faction.FACTIONS;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.dic.DicMisc;
import util.gui.misc.*;

class StagePickRace extends GuiSection{

	private static CharSequence ¤¤title = "Select species";
	private static CharSequence ¤¤desc = "All species have unique play styles, excel at different works, and have different likings and dislikes.";

	private static CharSequence ¤¤challenge = "Initial Challenge";
	static {
		D.ts(StagePickRace.class);
	}
	
	StagePickRace(Stages stages){
	
		stages.reset();
		
		int i = 0;
		for (Race r : RACES.all()) {
			if (r.playable) {
				addGrid(rButt(r), i++, 6, 4, 4);
			}
		}
		
		addRelBody(16, DIR.N, new GText(UI.FONT().M, ¤¤desc).setMaxWidth(600));
		addRelBody(4, DIR.N, new GHeader(¤¤title));
		
		addRelBody(16, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				text.lablify();
				text.add(FACTIONS.player().race().info.name);
			}
			
		}.r(DIR.N));
		
		addRelBody(4, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				text.add(FACTIONS.player().race().info.desc);
				text.setMaxWidth(600);
			}
			
		}.r(DIR.N));
		
		add(new RENDEROBJ.RenderImp() {
			
			private final GText t = new GText(UI.FONT().M, 64);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				Race rr = FACTIONS.player().race();
				int x = body.x1();
				int y = body.y1();
				t.clear();
				t.lablifySub();
				t.add(¤¤challenge).add(':');
				t.adjustWidth();
				t.render(r, x, y);
				int x2 = x + t.width()+48;
				t.clear().add(rr.info.initialChallenge);
				t.render(r, x2, y);
				
				y += t.height()*2;
				
				for (String s : rr.info.pros) {
					t.clear().add('+').s().add(s);
					t.normalify2();
					t.render(r, x, y);
					y+= t.height();
				}
				y+= t.height();
				for (String s : rr.info.cons) {
					t.clear().add('-').s().add(s);
					t.warnify();
					t.render(r, x, y);
					y+= t.height();
				}
				
			}
		}, body().x1()+80, body().y2()+80);
		
		
		body().setHeight(700);
		
		int p = 650-body().width();
		if (p > 0)
			pad(p/2, 0);
		
		addRelBody(16, DIR.S, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				
				stages.titles();
			}
			
		}.hoverInfoSet(DicMisc.¤¤confirm));
		stages.dummy.add(this);
		
	}

	private CLICKABLE rButt(Race r) {
		GButt.ButtPanel b = new GButt.ButtPanel(r.appearance().iconBig.huge) {
			
			@Override
			protected void clickA() {
				FACTIONS.player().setRace(r);
			}
			
			@Override
			protected void renAction() {
				selectedSet(FACTIONS.player().race() == r);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(r.info.name);
			}
			
		};
		b.pad(10, 10);
		return b;
		
	}
	
	
	
}
