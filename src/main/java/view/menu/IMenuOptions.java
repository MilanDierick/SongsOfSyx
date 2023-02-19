package view.menu;

import init.C;
import init.settings.S;
import init.settings.S.Setting;
import menu.screens.Screener;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.GButt;
import util.gui.table.GScrollRows;

class IMenuOptions extends GuiSection{
	
	IMenuOptions(IMenu m, Font font, Font small) {
		
		Screener sc = new Screener(DicMisc.¤¤OPTIONS, GCOLOR.T().H1) {
			
			@Override
			protected void back() {
				m.setMain();
			}
		};
		
		add(sc);
		
		int am = S.get().all().size();
		
		RENDEROBJ[] rs = new RENDEROBJ[am];
		am = 0;
		for (Setting s : S.get().all()){
			
			
			rs[am++] = new OptionLine(s, small);
		}
		
		RENDEROBJ r = new GScrollRows(rs, 300, 0).view();
		
		r.body().centerIn(this.body());
		add(r);
		
		

	}
	
	private class OptionLine extends GuiSection{
		
		private final SPRITE label;
		private final GButt left;
		private final GButt right;
		private final Setting sett;
		private final Font font;
		
		OptionLine(Setting s, Font font) {
			
			this.sett = s;
			this.font = font;
			body().setWidth(600);
			
			label = font.getText(s.name);
			
			left = new GButt.Glow(font.getText("--"));
			left.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					sett.inc(-1);
					S.get().applyRuntimeConfigs();
				}
			});
			//left.moveX1(getX2() + margin/6);
			add(left, body().cX()-left.body.width()-C.SG*4, 0);
			
			right = new GButt.Glow(font.getText("++"));
			right.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					sett.inc(1);
					S.get().applyRuntimeConfigs();
				}
			});
			right.body().moveX1(body().x2() + C.SCALE*5);
			add(right, body().cX()+C.SG*4, 0);
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			GCOLOR.T().NORMAL.bind();
			Str.TMP.clear();
			sett.getValue(Str.TMP);
			font.render(r, Str.TMP, body().x1() + 400, body().y1());
			GCOLOR.T().H2.bind();
			label.render(r, body().cX() - C.SCALE*20 - label.width(), body().y1());
			COLOR.unbind();
		}
		
		
		
	}
	
}
