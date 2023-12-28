package view.ui.battle;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.*;
import world.WORLD;

public class UIDivCardSett {

	private Div div;
	private GuiSection section = new GuiSection();
	private final GText tmp = new GText(UI.FONT().S, 8);
	
	public final int width;
	public final int height;
	
	
	UIDivCardSett(){

		
		int width = 72;
		
		section.body().setDim(width, 10);
		
		section.add(new SPRITE.Imp(Icon.M + 10 + SETT.ARMIES().banners.get(0).width(), 28) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				div.info.race.get().appearance().icon.render(r, X1, X1+Icon.M, Y1, Y1+Icon.M);
				div.info.banner().render(r, X1+Icon.M+10, 0, Y1, 0);
			}
		}, 0, 10);
		
		section.addRelBody(4, DIR.S, new SPRITE.Imp(width, Icon.M/2) {
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int am = 0;
				for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
					if (e.target(div) > 0) {
						am++;
					}
				}
				
				if (am > 0) {
				
					int d = width/am;
					d = CLAMP.i(d, 1, 14);
					
					int x1 = X1 + ((X2-X1)-am*d)/2;
					
					for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
						if (e.target(div) > 0) {
							e.resource.icon().medium.render(r, x1, x1+height(), Y1, Y2);
							x1 += d;
						}
					}
				}
			}
			
		});
		
		section.addRelBody(4, DIR.S, new SPRITE.Imp(width, STATS.BATTLE().TRAINING_ALL.size()*14) {
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				
				
				for (StatTraining tr : STATS.BATTLE().TRAINING_ALL) {
					
					tr.room.icon.small.render(r, X1, Y1);
					double max =div.info.trainingD(tr.room).getD();
					double cu = tr.div().getD(div);
					
					int x1 = X1 + 14;
					int x2 = (int) (x1 + (X2-x1)*max);
					
					GMeter.renderDelta(r, cu, 1.0, x1, x2, Y1+2, Y1+12, GMeter.C_ORANGE);
					
					Y1+= 14;
					
				}
				
			}
			
		});
		
		section.addRelBody(4, DIR.S, new RENDEROBJ.RenderImp(width, 16) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				double men =div.info.men();
				double n = div.menNrOf();
				
				if (men == 0)
					GMeter.renderDelta(r, 0, 0, body);
				else
					GMeter.renderDelta(r, n/men, (n+STATS.BATTLE().RECRUIT.inDiv(div))/men, body);
				tmp.clear();
				tmp.add(div.info.men.get());
				tmp.adjustWidth();
				tmp.renderC(r, body());
			}
			
		});
		
		section.body().incrH(6);
		
		
		section.pad(8, 0);
		
		this.width = section.body().width();
		this.height = section.body().height();
		
	}
	
	
	public void render(SPRITE_RENDERER r, int x1, int y2, Div div, boolean isActive, boolean isSelected, boolean isHovered) {
		this.div = div;
		section.body().moveX1Y1(x1, y2);
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, section.body());
		section.render(r, 0);
		if (WORLD.ARMIES().cityDivs().attachedArmy(div) != null){
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, section.body(), -4);
			OPACITY.unbind();
			UI.icons().m.arrow_left.renderC(r, section.body());
		}
		GButt.ButtPanel.renderFrame(r, section.body());
		
	}
	
}
