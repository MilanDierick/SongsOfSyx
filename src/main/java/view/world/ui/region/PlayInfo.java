package view.world.ui.region;

import init.D;
import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.misc.GHeader.HeaderVertical;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.map.RegionInfo;

final class PlayInfo extends GuiSection {

	private static CharSequence ¤¤abandon = "Abandon Region";
	private static CharSequence ¤¤abandonQ = "Do you wish to abandon this region? It will be turned into the hands of rebels.";
	
	static {
		D.ts(PlayInfo.class);
	}
	
	private final GETTER_IMP<Region> g;
	StringInputSprite name = new StringInputSprite(RegionInfo.nameSize, UI.FONT().H2) {
		@Override
		protected void change() {
			g.get().info.name().clear().add(text());
		};
	};
	
	PlayInfo(GETTER_IMP<Region> g, int WIDTH) {
		this.g = g;
		int i = 0;
		int cols = 7;
		int width = 78;
		int height = 48;
		DIR align = DIR.C;
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, g.get().info.fertility());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤Fertility);
				b.text(MiscBasics.¤¤fertilityD);
			};
			
		}.hv(UI.icons().m.agriculture), i++, cols, width, height, align);
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().info.area());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicMisc.¤¤Area);
				b.text(MiscBasics.¤¤areaD);
			};
			
		}.hv(UI.icons().m.expand), i++, cols, width, height, align);
		
		{
			SPRITE s = new SPRITE.Imp(16) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					GCOLOR.UI().border().render(r, X1, X2, Y1, Y2);
					double c = g.get().info.climateI();
					ColorImp.TMP.interpolate(CLIMATES.ALL().get((int)c).color, CLIMATES.ALL().get((int)Math.ceil(c)).color, c-(int)c);
					ColorImp.TMP.render(r, X1+2, X2-2, Y1+2, Y2-2);
					ColorImp.TMP.shadeSelf(0.5);
					ColorImp.TMP.renderFrame(r, X1, X2, Y1, Y2, -1, 2);
				}
			};
			
			GHeader.HeaderVertical h = new HeaderVertical(UI.icons().s.temperature, s) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(CLIMATES.INFO().name);
					GBox b = (GBox) text;
					for (CLIMATE c : CLIMATES.ALL()) {
						b.textLL(c.name);
						b.tab(6);
						b.add(GFORMAT.perc(b.text(), g.get().info.climate(c)));
						b.NL();
					}
				}
			};
			
			addGridD(h, i++, cols, width, height, align);
		}
		
		{
			SPRITE s = new SPRITE.Imp(48, 16) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					double c = (1 + RD.HEALTH().getD(g.get())/5.0)/2;
					double t = (1 + RD.HEALTH().boostablee.get(g.get())/5.0)/2;
					
					if (t > 0.5)
						GMeter.renderDelta(r, c, t, X1, X2, Y1, Y2, GMeter.C_BLUE);
					else
						GMeter.renderDelta(r, c, t, X1, X2, Y1, Y2, GMeter.C_RED);
					
					if (RD.HEALTH().outbreak.get(g.get()) == 1) {
						Y1-= 24;
						OPACITY.O25TO100.bind();
						UI.icons().m.disease.render(r, X1, Y1);
						OPACITY.unbind();
					}
						
					
				}
			};
			
			GHeader.HeaderVertical h = new HeaderVertical(UI.icons().m.heart, s) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(RD.HEALTH().boostablee.name);
					b.text(RD.HEALTH().boostablee.desc);
					b.NL(4);
					
					b.textLL(DicMisc.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), RD.HEALTH().getD(g.get())));
					
					b.sep();
					
					RD.HEALTH().boostablee.hover(b, g.get(), DicMisc.¤¤Target, true);
					
					if (RD.HEALTH().outbreak.get(g.get()) == 1) {
						b.sep();
						b.error(RD.HEALTH().outbreak.name);
						b.NL();
						b.error(RD.HEALTH().eDesc(g.get()));
					}
				}
			};
			
			addGridD(h, i++, cols, width, height, align);
		}
		
		{
			RENDEROBJ h = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, CLAMP.d(RD.DIST().boostable.get(g.get()), 0, 1));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(RD.DIST().boostable.name);
					b.text(RD.DIST().boostable.desc);
					b.sep();
					RD.DIST().boostable.hover(b, g.get(), null, true);
				};
				
			}.hv(UI.icons().m.wheel);
			
			addGridD(h, i++, cols, width, height, align);
		}
		
		{
			SPRITE s = new SPRITE.Imp(48, 16) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					GMeter.render(r, GMeter.C_REDGREEN, RD.OWNER().affiliation.getD(g.get()), X1,X2,Y1,Y2);
				}
			};
			
			GHeader.HeaderVertical h = new HeaderVertical(UI.icons().m.flag, s) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.textLL(DicMisc.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), RD.OWNER().affiliation.getD(g.get())));
					b.NL(8);
					b.add(RD.OWNER().affiliation.info());
				}
			};
			
			addGridD(h, i++, cols, width, height, align);
		}
		
		{
			addRightCAbs(width, new MiscDistances(g));
			
			addRightC(0, new GButt.ButtPanel(UI.icons().s.cancel.createColored(GCOLOR.UI().BAD.hovered)) {
				
				ACTION aa = new ACTION() {
					
					@Override
					public void exe() {
						RD.setFaction(g.get(), null);
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().yesNo.activate(¤¤abandonQ, aa, ACTION.NOP, true);
				}
				
			}.hoverTitleSet(¤¤abandon));
			
			if (S.get().developer) {
				
				addRightC(0, new GButt.ButtPanel(UI.icons().s.cog) {
					
					PlayDebug dd = new PlayDebug();
					
					@Override
					protected void clickA() {
						dd.reg = g.get();
						VIEW.inters().popup.show(dd, this);
					}
					
				});
				
			}
		}
		
		
		
		GInput nn = new GInput(name);
		nn.body().moveY2(-6);
		add(nn);
		
		
		
		
		pad((WIDTH-body().width())/2, 0);
		
	
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		name.text().clear().add(g.get().info.name());
		super.render(r, ds);
	}
	
	
	
}
