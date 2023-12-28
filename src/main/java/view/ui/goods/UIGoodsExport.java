package view.ui.goods;

import static util.dic.DicRes.*;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.data.RD;

public class UIGoodsExport extends GuiSection{

	static CharSequence ¤¤name = "Export Settings";
	static CharSequence ¤¤special = "Special Orders";

	private static CharSequence ¤¤Capacity = "¤Export Capacity";
	private static CharSequence ¤¤CapacityDesc = "¤The current stock of your export depots. These goods will be bought by a trade partner that offers the highest price, if one exist.";
	
	private static CharSequence ¤¤priceCapD = "¤The minimum price you are willing to sell this resource for.";

	private static CharSequence ¤¤LevelDesc = "¤When the warehouse stock is below your export level the export workers will haul these resources to an export depot, where they are then sold to the highest bidder.";
	private static CharSequence ¤¤LevelEverything = "Export everything, even resources not in warehouses.";
	private static CharSequence ¤¤LevelNothing = "¤Export nothing.";
	private static CharSequence ¤¤LevelCurrent = "¤Export when warehouse stock is above {0}% of total capacity ({1} items).";
	private static CharSequence ¤¤LevelCurrentE = "¤You will currently export {0} additional items.";

	public static final COLOR color = new ColorImp(100, 90, 70);
	private static final COLOR colorDark = color.shade(0.75); 
	
	static {
		D.ts(UIGoodsExport.class);
	}
	
	public GETTER_IMP<RESOURCE> res = new GETTER_IMP<>(RESOURCES.ALL().get(0));
	
	public UIGoodsExport(){
		
		addDown(12, amount());
		addDown(12, priceH());
		addDown(12, cap());
		addDown(12, price());
		addRelBody(16, DIR.E, new UIGoodsTraders(6) {

			@Override
			protected int price(FactionNPC f) {
				return f.seller().priceBuyP(res.get());
			}

			@Override
			protected int sortValue(FactionNPC f) {
				return -f.seller().priceBuyP(res.get());
			}
			
		});

		addRelBody(8, DIR.S, problem());
		
		
		GuiSection h = new GuiSection();
		
		h.add(new HOVERABLE.HoverableAbs(Icon.M) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				res.get().icon().render(r, body);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(res.get().name);
			}
		});
		h.addRightC(8, new GHeader(¤¤name));
		addRelBody(8, DIR.N, h);
		
		{
			GuiSection s = new GuiSection();
			
			GButt.ButtPanel b = new GButt.ButtPanel(UIGoodsImport.¤¤Best) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					isActive &= FACTIONS.pRel().pricesSell.get(res.get()) > 0;
					super.render(r, ds, isActive, isSelected, isHovered);
				}
				
				@Override
				protected void clickA() {
					FactionNPC f = null;
					int pp = 0;
					for (FactionNPC ff : FACTIONS.pRel().traders()) {
						int p = ff.seller().priceBuyP(res.get());
						if (p > 0 && p > pp) {
							pp = p;
							f = ff;
						}
					}
					if (f != null) {
						VIEW.inters().popup.close();
						VIEW.UI().factions.openSell(f, res.get());
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.NL(8);
					CharSequence p = SETT.ROOMS().EXPORT.tally.problem(res.get(), false);
					if (p != null)
						b.error(p);
				}
			};
			b.hoverInfoSet(UIGoodsImport.¤¤BestD);
			b.icon(UI.icons().s.money);
			b.setDim(180);
			s.addRightC(0, b);
			
			b = new GButt.ButtPanel(UIGoodsImport.¤¤Closest) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					isActive &= FACTIONS.pRel().pricesSell.get(res.get()) > 0;
					super.render(r, ds, isActive, isSelected, isHovered);
				}
				
				@Override
				protected void clickA() {
					FactionNPC f = null;
					int pp = Integer.MAX_VALUE;
					for (FactionNPC ff : FACTIONS.pRel().traders()) {
						int p = RD.DIST().distance(ff);
						if (ff.seller().priceSellP(res.get()) > 0 && p < pp) {
							pp = p;
							f = ff;
						}
					}
					if (f != null) {
						VIEW.inters().popup.close();
						VIEW.UI().factions.openSell(f, res.get());
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.NL(8);
					CharSequence p = SETT.ROOMS().EXPORT.tally.problem(res.get(), false);
					if (p != null)
						b.error(p);
				}
				
			};
			b.hoverInfoSet(UIGoodsImport.¤¤ClosestD);
			b.icon(UI.icons().s.wheel);
			b.setDim(180);
			s.addRightC(0, b);
			
			s.addRelBody(10, DIR.N, new GHeader(¤¤special, UI.FONT().S));
			
			addRelBody(8, DIR.S, s);
		}
		
		
	
		
		
	}
	
	static RENDEROBJ mini(RESOURCE res, UIGoodsExport export) {
		
		GuiSection s = new GuiSection();
		
		GETTER_IMP<RESOURCE> get = new GETTER.GETTER_IMP<RESOURCE>(res);
		
		s.add(UIGoodsExport.capBar(get, 64, 24));
		s.addRelBody(0, DIR.S, UIGoodsExport.slider(get, 64, 24));
		
		if (res != null) {
			GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().s.cog) {
				
				@Override
				protected void clickA() {
					export.res.set(res);
					VIEW.inters().popup.show(export, this);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(UIGoodsExport.¤¤name);
					GBox b = (GBox) text;
					CharSequence p = SETT.ROOMS().EXPORT.tally.problem(res, true);
					if (p != null)
						b.error(p);
					b.NL(4);
					p = SETT.ROOMS().EXPORT.tally.warning(res);
					if (p != null)
						b.add(b.text().warnify().add(p));
					
					super.hoverInfoGet(text);
				}
				
			};	
			b.setDim(s.body().height(), s.body().height());
			
			s.addRelBody(0, DIR.E, b);
		}else {
			s.body().incrW(s.body().height());
		}
		
		if (res != null) {
			RENDEROBJ oo = new RENDEROBJ.RenderImp(s.body().width(), s.body().height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (SETT.ROOMS().EXPORT.tally.capacity.get(res) == 0) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body);
						OPACITY.unbind();
					}else {
						if (SETT.ROOMS().EXPORT.tally.exportWhenUnder.get(res) > 0) {
							if (SETT.ROOMS().EXPORT.tally.problem(res, true) != null) {
								GCOLOR.UI().BAD.hovered.bind();
								UI.icons().s.alert.renderC(r, body().x2(), body().y1());
							}else if (SETT.ROOMS().EXPORT.tally.warning(res) != null) {
								GCOLOR.UI().SOSO.hovered.bind();
								UI.icons().s.alert.renderC(r, body().x2(), body().y1());
							}
							COLOR.unbind();
						}
					}
				}
			};
			
			oo.body().centerIn(s);
			s.add(oo);
		}
		
		
		RENDEROBJ oo = new RENDEROBJ.RenderImp(s.body().width()+16, 60) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().renderFrame(r, body, 0, 1);
			}
		};
		
		oo.body().centerIn(s);
		s.add(oo);
		
		return s;
		
	}
	
	private GuiSection priceH() {
		GuiSection s = new GuiSection();
		s.add(UIGoodsImport.priceChart(FACTIONS.pRel().pricesSell, DicRes.¤¤sellPrice, res, 8, 64));
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.coins));
		return s;
	}
	
	private GuiSection price() {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title( DicRes.¤¤PriceCap);
				text.text(¤¤priceCapD);
			}
			
		};
		
	
		
		
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 1;
			}
			
			@Override
			public int max() {
				return SETT.ROOMS().EXPORT.tally.priceCapsI.max(res.get());
			}
			
			@Override
			public int get() {
				return SETT.ROOMS().EXPORT.tally.priceCapsI.get(res.get());
			}
			
			@Override
			public void set(int t) {
				SETT.ROOMS().EXPORT.tally.priceCapsI.set(res.get(), t);
			}
		};
		
		GInputInt sl = new GInputInt(in, true, true);
		
		s.addRightC(2, sl);
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.coins.twin(UI.icons().s.arrowUp, DIR.NE, 2)));
		return s;
	}
	
	private GuiSection amount() {
		
		GSliderInt sl = slider(res, 260, 32);
		
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				sl.hoverInfoGet(text);
			}
			
		};
		
		s.add(sl);
		s.addDown(0, new HOVERABLE.HoverableAbs(260, 24) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double tot = SETT.ROOMS().STOCKPILE.tally().spaceTotal(res.get());
				if (tot > 0)
					GMeter.render(r, GMeter.C_GREENRED, 1.0-SETT.ROOMS().STOCKPILE.tally().amountTotal(res.get())/tot, body);
				else
					GMeter.render(r, GMeter.C_GRAY, 0, body);
			}
		});
		
		s.addRelBody(8, DIR.W, icon(UI.icons().m.cog_big));
		
		return s;
	}

	
	static GSliderInt slider(GETTER<RESOURCE> res, int width, int height) {
		INTE limit = new INTE() {
			
			@Override
			public int get() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.get(res.get());
			}

			@Override
			public int min() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.min(res.get());
			}

			@Override
			public int max() {
				return SETT.ROOMS().EXPORT.tally.exportWhenUnder.max(res.get());
			}

			@Override
			public void set(int t) {
				SETT.ROOMS().EXPORT.tally.exportWhenUnder.set(res.get(), t);
			}
		};

		return new GSliderInt(limit, width, height, false) {
			
			
			
			@Override
			protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
				
				COLOR col = width != widthFull ? colorDark : color;
				col.render(r, x1, x1+width, y1, y2);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				GBox b = (GBox) text;
				b.title(¤¤ExportLevel);

				int space = (int) SETT.ROOMS().STOCKPILE.tally().spaceTotal(res.get());
				int amount = (int) SETT.ROOMS().STOCKPILE.tally().amountTotal(res.get());
				if (limit.getD() == 1) {
					b.textL(¤¤LevelEverything);
				}else if (limit.getD() == 0) {
					b.textL(¤¤LevelNothing);
				}else {
					GText t = b.text();
					t.add(¤¤LevelCurrent);
					
					double lim = limit.get()/(limit.max()-1.0);
					lim = 1.0-lim;
					t.insert(0, (int)(Math.round(100*lim)));
					t.insert(1, (int)(lim*space));
					b.add(t);
					b.NL();
					t = b.text();
					t.add(¤¤LevelCurrentE);
					
					int export = (int)CLAMP.d(amount - lim*space, 0, amount);
					t.insert(0, export);
					
					b.add(t);
					b.NL(4);
				}
				b.NL(4);
				
				b.NL(8);
				b.textLL(UIGoodsImport.¤¤Stockpile);
				b.tab(7);
				b.add(GFORMAT.iofkNoColor(b.text(), amount, space));
				
				b.NL(8);
				
				b.text(¤¤LevelDesc);
				
				
			}
		};
	}
	
	static HOVERABLE capBar(GETTER<RESOURCE> res, int width, int height) {
		return new HOVERABLE.HoverableAbs(width, height) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double cap = SETT.ROOMS().EXPORT.tally.capacity.get(res.get());
				if (cap == 0) {
					GMeter.render(r, GMeter.C_ORANGE, 0, body());
				
				}else {
					
					double a = SETT.ROOMS().EXPORT.tally.amount.get(res.get());
					double p = a + SETT.ROOMS().EXPORT.tally.promised.get(res.get());
					GMeter.renderDelta(r, a, p, body, GMeter.C_ORANGE);
					
				}
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤Capacity);
				text.text(¤¤CapacityDesc);
				text.NL(8);
				int ou = SETT.ROOMS().EXPORT.tally.promised.get(res.get());
				int am = SETT.ROOMS().EXPORT.tally.amount.get(res.get());
				int ca = SETT.ROOMS().EXPORT.tally.capacity.get(res.get());
				GBox b = (GBox) text;
				b.textL(DicRes.¤¤Storage);
				
				
				b.add(b.text().lablifySub().add(DicRes.¤¤Outbound));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), ou));
				b.NL();
				b.add(b.text().lablifySub().add(DicRes.¤¤Stored));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), am));
				b.NL();
				b.add(b.text().lablifySub().add(DicRes.¤¤Capacity));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), ca));
				b.NL(8);
				b.add(b.text().lablifySub().add(DicRes.¤¤ForSale));
				b.tab(6);
				b.add(GFORMAT.i(b.text(), ca - am - ou));

			}
		};
	}
	
	private GuiSection cap() {
		HOVERABLE cc = capBar(res, 200, 32);
		
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				cc.hoverInfoGet(text);
			}
		};
		s.add(cc);
		
		
		s.addRelBody(8, DIR.W, icon(SETT.ROOMS().EXPORT.icon));
		
		return s;
	}
	
	private HOVERABLE problem() {
		GuiSection s = new GuiSection();
		s.add(new HOVERABLE.HoverableAbs(450, 80) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				GCOLOR.UI().bg().render(r, body);
				GCOLOR.UI().borderH(r, body, 0);
				
				CharSequence p = SETT.ROOMS().EXPORT.tally.problem(res.get(), true);
				
				if (p != null) {
					GCOLOR.UI().BAD.hovered.bind();
					UI.FONT().S.render(r, p, body().x1()+8, body().y1()+8, body().width()-16, 1.0);
					COLOR.unbind();
				}
					
				else {
					p = SETT.ROOMS().EXPORT.tally.warning(res.get());
					GCOLOR.UI().SOSO.hovered.bind();
					if (p != null)
						UI.FONT().S.render(r, p, body().x1()+8, body().y1()+8, body().width()-16, 1.0);
					COLOR.unbind();
				}
				
				
			}
		});
		s.hoverInfoSet(DicMisc.¤¤Problem);
		return s;
	}

	
	private static HOVERABLE icon(SPRITE icon) {
		
		return new HOVERABLE.HoverableAbs(32, 32) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				icon.renderC(r, body());
			}
		};
		
	}
	

	
}
