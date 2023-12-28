package view.ui.faction;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.npc.FactionNPC;
import game.faction.npc.ruler.ROpinions;
import game.faction.npc.stockpile.NPCStockpileDebugUI;
import game.faction.trade.TradeManager;
import init.D;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.GETTER;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import world.regions.Region;
import world.regions.data.RD;

final class Goods extends GuiSection{

	private static CharSequence ¤¤productionD = "The rate at this faction produces the resource.";
	private static CharSequence ¤¤priorityD = "The priority of the manpower the faction allocates to the production of this resource.";
	
	private static CharSequence ¤¤priceSell = "The base buy price is proportional to the amount the resource a faction has stored, and the money it has available.";
	private static CharSequence ¤¤priceBuy = "The base sell price is proportional to the amount the resource a faction has stored, and the money it has available. If a faction has scant use for a resource, the buy price will be significantly lower than the sell price.";
	private static CharSequence ¤¤penaltyD1 = "Toll is the distance to this faction. This penalty can be decreased by building roads in your kingdom. The tariff penalty is based on the factions opinion of you.";
	private static CharSequence ¤¤penaltyD2 = "The tariff penalty is based on the factions opinion of you. Increase their opinion for better prices";
	
	static {
		D.ts(Goods.class);
	}
	
	final GETTER<FactionNPC> f;
	
	private ArrayList<RESOURCE> ress = new ArrayList<>(RESOURCES.ALL().size());
	private final StringInputSprite filter = new StringInputSprite(12, UI.FONT().M).placeHolder(DicMisc.¤¤Search);
	
	Goods(GETTER<FactionNPC> f, int height){
		this.f = f;
		
		{
			GuiSection s = new GuiSection();
			SPRITE ss = new SPRITE.Imp(150, 14) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					GMeter.render(r, GMeter.C_RED, TradeManager.tollPlayer(f.get(), 1000, RD.DIST().distance(f.get()))/1000.0, X1, X2, Y1, Y2);
				}
			};
			GHeader h = new GHeader.HeaderHorizontal(UI.icons().s.wheel, ss) {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicRes.¤¤Toll);
					b.text(DicRes.¤¤TollD);
					b.sep();
					RD.DIST().boostable.hover(b, RACES.clP(), true);
				}
			};
			s.addRightC(64, h);
			
			ss = new SPRITE.Imp(150, 14) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					GMeter.render(r, GMeter.C_RED, ROpinions.tradeCost(f.get()), X1, X2, Y1, Y2);
				}
			};
			h = new GHeader.HeaderHorizontal(UI.icons().s.angry, ss) {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(DicRes.¤¤Tariff);
					b.text(DicRes.¤¤TariffD);
					b.sep();
					ROpinions.GET().hover(b, f.get().court().king().roy(), true);
				}
			};
			s.addRightC(64, h);
			
			s.addRightC(64, new GInput(filter));
			
			if (S.get().developer)
				s.addRightC(64, new GButt.ButtPanel("debug") {
					@Override
					protected void clickA() {
						VIEW.inters().popup.show(new NPCStockpileDebugUI(f.get()), this, true);
					}
				});
			
			addRelBody(8, DIR.S, s);
			
		}
		
		
		
		
		GTableBuilder builder = new GTableBuilder() {
			
			
			@Override
			public int nrOFEntries() {
				return ress.size();
			}

		};
		
		builder.column(Icon.M*2, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				return new HOVERABLE.Sprite(Icon.M) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						g(ier).icon().render(r, body());
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(g(ier).names);
					}
					
				};
			}
		}, DIR.C);
		
		int W = Icon.M*5+12;
		
		builder.column(DicRes.¤¤Price, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().priceSell(g(ier), 1));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						int p = f.get().seller().priceSell(g(ier), 1);
						//f.get().stockpile.debug(g(ier));
						
						b.title(DicRes.¤¤Price);
						
						b.add(UI.icons().s.money);
						b.textL(DicRes.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						b.text(DicMisc.¤¤ProductionRate);
						b.tab(6);
						b.add(GFORMAT.f(b.text(), f.get().stockpile.prodRate(g(ier))));
						b.NL();
						b.text(¤¤productionD);
						
					};
					
				};
				return new Cell(W, s, DIR.E);
			}
			
			
			
		}, DIR.E);
		
		builder.column(DicRes.¤¤Sell, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().priceSellP(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						int p = f.get().seller().priceSell(g(ier), 1);
						//f.get().stockpile.debug(g(ier));
						
						b.title(DicRes.¤¤sellPrice);
						b.NL();
						b.text(¤¤priceSell);
						b.NL(8);
						
						b.add(UI.icons().s.money);
						b.textL(DicRes.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						int t = (int) TradeManager.tollPlayer(f.get(), p, RD.DIST().distance(f.get()));
						int o = (int) Math.ceil(p*ROpinions.tradeCost(f.get()));
						
						b.add(UI.icons().s.wheel);
						b.textL(DicRes.¤¤Toll);
						b.tab(6);
						b.add(GFORMAT.iIncr(b.text(), t));
						b.NL();
						
						b.add(UI.icons().s.angry);
						b.textL(DicRes.¤¤Tariff);
						b.tab(6);
						b.add(GFORMAT.iIncr(b.text(), o));
						b.NL();
						
						b.add(UI.icons().s.arrow_right);
						b.textLL(DicMisc.¤¤Total);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p+toll(p)));
						
						b.NL(8);
						
						b.text(¤¤penaltyD1);
						b.NL(4);
						b.text(¤¤penaltyD2);
					};
					
				};
				return new Cell(W, s, DIR.E);
			}
			
			
			
		}, DIR.E);
		
		builder.column(DicRes.¤¤Buy, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().priceBuyP(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						
						b.title(DicRes.¤¤buyPrice);
						b.NL();
						b.text(¤¤priceBuy);
						b.NL(8);
						
						int p = f.get().buyer().buyPrice(g(ier), 1);
						b.add(UI.icons().s.money);
						b.textL(DicRes.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						int t = (int) TradeManager.tollPlayer(f.get(), p, RD.DIST().distance(f.get()));
						int o = (int) Math.ceil(p*ROpinions.tradeCost(f.get()));
						
						b.add(UI.icons().s.wheel);
						b.textL(DicRes.¤¤Toll);
						b.tab(6);
						b.add(GFORMAT.iIncr(b.text(), -t));
						b.NL();
						
						b.add(UI.icons().s.angry);
						b.textL(DicRes.¤¤Tariff);
						b.tab(6);
						b.add(GFORMAT.iIncr(b.text(), -o));
						b.NL();
						
						b.tab(6);
						b.add(UI.icons().s.arrow_right);
						b.textLL(DicMisc.¤¤Total);
						b.add(GFORMAT.i(b.text(), p-toll(p)));
						
						b.NL(8);
						
						b.text(¤¤penaltyD1);
						b.NL(4);
						b.text(¤¤penaltyD2);
					};
				};
				return new Cell(W, s, DIR.E);
			}
		}, DIR.E);
		
		builder.column(DicMisc.¤¤Priority, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().stockpile.prio(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(DicMisc.¤¤Priority);
						b.text(¤¤priorityD);
						
					};
					
				};
				return new Cell(W, s, DIR.E);
			}
		}, DIR.E);
		
		builder.column(RTYPE.TAX.name, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						RESOURCE res = g(ier);
						int am = 0;
						for (int ri = 0; ri < f.get().realm().regions(); ri++) {
							Region reg = f.get().realm().region(ri);
							am += RD.OUTPUT().get(res).getDelivery(reg);
							
							
						}
						GFORMAT.i(text, am);
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(DicMisc.¤¤taxes);
						RESOURCE res = g(ier);
						for (int ri = 0; ri < f.get().realm().regions(); ri++) {
							Region reg = f.get().realm().region(ri);
							int out = RD.OUTPUT().get(res).getDelivery(reg);
							b.add(UI.icons().s.world);
							b.textL(reg.info.name());
							b.tab(7);
							b.add(GFORMAT.iIncr(b.text(), out));
							b.NL();
							
						}
						
						if (S.get().developer) {
							for (int i = 0; i < 3; i++) {
								b.add(GFORMAT.i(b.text(), f.get().res().in(RTYPE.TAX).history(res).get(i)));
								b.NL();
							}
						}
						
					};
					
				};
				return new Cell(W, s, DIR.E);
			}
		}, DIR.E);
		
		builder.column(DicRes.¤¤Stored, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().forSale(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(DicRes.¤¤Stored);
						RESOURCE res = g(ier);
						for (RTYPE t : RTYPE.all) {
							b.textL(t.name);
							b.tab(6);
							b.add(GFORMAT.iIncr(b.text(), f.get().res().in(t).get(res)));
							b.tab(8);
							b.add(GFORMAT.iIncr(b.text(), -f.get().res().out(t).get(res)));
							b.NL();
						}
					};
					
					
					
				};
				return new Cell(W, s, DIR.E);
			}
		}, DIR.E);
		
		addRelBody(8, DIR.S, builder.createHeight(height-16-body().height(), true));
		

	}

	private RESOURCE g(GETTER<Integer> ier) {
		return ress.get(ier.get());
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		ress.clearSloppy();
		if (filter.text().length() == 0) {
			ress.add(RESOURCES.ALL());
		}else {
			for (RESOURCE res : RESOURCES.ALL()) {
				if (Str.containsText(res.name, filter.text()) || Str.containsText(res.names, filter.text()))
					ress.add(res);
			}
		}
		
		super.render(r, ds);
	}
	
	private class Cell extends HOVERABLE.HoverableAbs{
		
		private final GStat st;
		private final DIR d;
		Cell(int width, GStat st, DIR d){
			this.st = st;
			this.d = d;
			body.setDim(width, Icon.M);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (hoveredIs())
				COLOR.WHITE50.render(r, body);
			st.adjust();
			int dx = (body.width()-st.width())/2;
			int dy = (body.height()-st.height())/2;
			st.renderC(r, body.cX()+ dx*d.x(), body.cY()+ dy*d.y());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			st.hoverInfoGet((GBox)text);
		}
		
	}
	

	public int toll(int price) {
		return TradeManager.toll(FACTIONS.player(), f.get(), RD.DIST().distance(f.get()), price);
	}

}
