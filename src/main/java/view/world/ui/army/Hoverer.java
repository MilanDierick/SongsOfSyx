package view.world.ui.army;

import game.faction.FACTIONS;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicArmy;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import world.army.AD;
import world.entity.army.WArmy;

final class Hoverer {

	GETTER_IMP<WArmy> a = new GETTER.GETTER_IMP<WArmy>();
	private final GuiSection player = new GuiSection();
	private final GuiSection ai = new GuiSection();

	public Hoverer() {
		player.add(ArmyInfo.info(a));
		player.addDown(4, ArmyInfo.supplies(a));
		

		
		ai.add(new GStat() {
			
			@Override
			public void update(GText text) {
				if (a.get().faction() != null) {
					text.color(a.get().faction().banner().colorBGBright()); 
					text.add(a.get().faction().name);
				}else {
					text.color(COLOR.WHITE85);
					text.add(DicArmy.¤¤Rebels);
				}
				
			}
		}, 0, 0);
		
		ai.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				if (FACTIONS.DIP().war.is(FACTIONS.player(), a.get().faction()))
					text.errorify().add(DicArmy.¤¤Enemy);
				else
					text.normalify2().add(DicArmy.¤¤Neutral);
				
			}
		}.hh(UI.icons().s.flag));
		
		ai.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				if (a.get().region() != null) {
					text.add(a.get().region().info.name());
				}
			}
		}.hh(UI.icons().s.crossheir));
		
		ai.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, AD.men(null).get(a.get()), AD.men(null).target().get(a.get()));
				
			}
		}.hh(UI.icons().s.human), 200, 0);
		
		ai.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, (long) AD.UTIL().power.get(a.get()));				
			}
		}.hh(UI.icons().s.fist));
		
		ai.addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				a.get(). state().info(a.get(), text);
				
			}
		}.hh(UI.icons().s.arrow_right));
		

		
		ai.addRelBody(16, DIR.W, new RENDEROBJ.RenderImp(Icon.HUGE) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (a.get().faction() != null) {
					a.get().faction().banner().HUGE.render(r, body.x1(), body.y1());
				}else {
					UI.icons().l.rebel.huge.render(r, body.x1(), body.y1());;
				}
				
			}
		});
		
		ai.body().incrW(160);
		
	}
	
	public void hover(GUI_BOX box, WArmy a) {
		this.a.set(a);
		box.title(a.name);
		if (a.faction() == FACTIONS.player()) {
			box.add(player);
		}else {
			box.add(ai);
		}
	}
	
}
