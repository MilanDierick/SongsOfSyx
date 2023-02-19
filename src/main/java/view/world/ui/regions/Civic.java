package view.world.ui.regions;

import static view.world.ui.regions.UIRegion.*;

import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.map.regions.REGIOND;
import world.map.regions.RegionCivics.RegionDecreeCivic;

final class Civic extends GuiSection {
	
	public Civic() {
		
		
		int y1 = getLastY2()+8;
		int x1 = body().x1();
		int i = 0;
		
		for (RegionDecreeCivic d : REGIOND.CIVIC().all) {
			add(new CivicButton(d), x1 + (i%2)*124, y1 + (i/2)*32);
			i++;
		}
		
		add(new Rebellion(), body().x2()+16, body().y1());
		add(new Sanitation(), getLastX1(), getLastY2()+8);
		add(new Knowledge(), getLastX1(), getLastY2()+8);
		
		
	}
	
	private final class Rebellion extends GuiSection {
		
		Rebellion(){
			add(SPRITES.icons().m.rebellion, 0, 0);
			
			addRightC(8, new RENDEROBJ.RenderImp(120, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GMeter.renderDelta(r, REGIOND.OWNER().order.getD(reg), REGIOND.OWNER().order.next(reg), body());
				}
			});
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(REGIOND.OWNER().order.info().name);
			b.text(REGIOND.OWNER().order.info().desc);
			b.NL(8);
			UIRegion.hover(b, REGIOND.OWNER().order);
		}
		
	}
	
	private final class Sanitation extends GuiSection {
		
		Sanitation(){
			add(SPRITES.icons().m.sanitation, 0, 0);
			
			addRightC(8, new RENDEROBJ.RenderImp(120, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GMeter.renderDelta(r, REGIOND.CIVIC().health.getD(reg), REGIOND.CIVIC().health_target.getD(reg), body());
				}
			});
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(REGIOND.CIVIC().health.info().name);
			b.text(REGIOND.CIVIC().health.info().desc);
			b.NL(8);
			UIRegion.hover(b, REGIOND.CIVIC().health_target);
		}
		
	}
	
	private final class Knowledge extends GuiSection{
		
		Knowledge(){
			
			add(SPRITES.icons().m.book, 0, 0);
			addRightC(8, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, (int)REGIOND.CIVIC().knowledge.next(reg));
					text.color(col());
				}
			});
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(REGIOND.CIVIC().knowledge.info().name);
			b.text(REGIOND.CIVIC().knowledge.info().desc);
			b.NL();
			b.add(GFORMAT.i(b.text(), (int)REGIOND.CIVIC().knowledge.getD(reg)));
			b.NL(8);
			UIRegion.hover(b, REGIOND.CIVIC().knowledge);
		}
		
		private COLOR col() {
			int n = (int) REGIOND.CIVIC().knowledge.getD(reg);
			int t = (int) REGIOND.CIVIC().knowledge.next(reg);
			if (n < t) {
				return GCOLOR.T().IGOOD;
			}else if(t > n) {
				return GCOLOR.T().IBAD;
			}else {
				return GCOLOR.T().NORMAL;
			}
		}
		
		
	}
	
	private class CivicButton extends GuiSection{
		
		private final COLOR col = new ColorImp(0, 127, 127);
		private final COLOR col_no = new ColorImp(20, 60, 60);
		private final COLOR puls = new ColorImp(0, 60, 127);
		private final RegionDecreeCivic d;
		
		public CivicButton(RegionDecreeCivic d) {
			this.d = d;
			body().setHeight(ICON.BIG.SIZE);
			
			add(new SPRITE.Imp(ICON.BIG.SIZE, ICON.BIG.SIZE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					d.icon.renderC(r, X1+(X2-X1)/2, Y1+(Y2-Y1)/2);
				}
			},0,0);
			
			CLICKABLE c = new GButt.Glow(SPRITES.icons().s.minifier) {
				
				@Override
				protected void clickA() {
					d.inc(reg, -1);
				}
				
				@Override
				protected void renAction() {
					activeSet(d.get(reg) > 0);
				}
				
			};
			addRightC(0, c);
			
			addRightC(2, new SPRITE.Imp(6*d.max(null), 16) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					for (int i = 0; i < d.max(null); i++) {
						COLOR c = null;
						if (i >= d.get(reg)) {
							c = col_no;
						}else if(i >= d.get(reg))
							c = puls;
						else
							c = col;
						c.render(r, X1+i*6, X1+i*6+4, Y1, Y2);
					}
				}
			});
			
			c = new GButt.Glow(SPRITES.icons().s.magnifier) {
				
				@Override
				protected void clickA() {
					d.inc(reg, 1);
				}
				
				@Override
				protected void renAction() {
					activeSet(d.get(reg) < d.max(null));
				}
				
			};
			addRightC(2, c);
			
			pad(2);
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(),-1);
			super.render(r, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;			
			UIRegion.hover(b, d);
			
		}
		
		
		
	}
	
}
