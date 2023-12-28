package view.ui.goods;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.data.INT;
import util.data.INT.INTE;
import util.dic.DicRes;
import util.dic.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.info.GFORMAT;

final class UIGoodsHistory extends GuiSection {

	private static final int w = 6;
	private static int amount = 32;
	private static final int height = 60;
	
	private final GStaples[] dias;
	private int hi;
	private final RESOURCE res;
	
	UIGoodsHistory(RESOURCE r) {
		this.res = r;
		INTE im = new INT.IntImp();
		im.set(-1);
		
		dias = new GStaples[] {
			 new StorageDiagram(r),
			 new ProductionDiagram(r),
			 new TradeDiagram(r),
			 new PriceDiagram(r)
		};
		
		for (GStaples ss : dias) {
			addRelBody(4, DIR.E, ss);
		}
		
		pad(2, 6);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		hi = -1;
		for (GStaples ss : dias) {
			if (ss.hoveredIs()) {
				hi = ss.hoverI();
			}
		}
		if (hi >= 0) {
			for (GStaples ss : dias) {
				ss.setHovered(hi);
			}
		}
		
		super.render(r, ds);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hi < 0)
			return;
		GBox b = (GBox) text;
		
		int si = amount-hi-1;
		
		{
			GText t = b.text();
			t.lablify();
			DicTime.setAgo(t, si*GAME.player().res().time.bitSeconds());
			b.add(t);
			b.NL(4);
		}
		
		{
			b.textL(DicRes.¤¤Stored);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), SETT.ROOMS().STOCKPILE.tally().amountsSeason().history(res).get(si)));
			b.NL(8);
		}
		{
			FResources rr = FACTIONS.player().res();
			for (RTYPE t : RTYPE.all) {
				b.add(b.text().normalify().add(t.name));
				b.tab(6);
				b.add(GFORMAT.iIncr(b.text(), rr.in(t).history(res).get(si)));
				b.tab(8);
				b.add(GFORMAT.iIncr(b.text(), -rr.out(t).history(res).get(si)));
				b.NL();
			}
			
			b.NL(4);
			b.textL(DicRes.¤¤Net);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), GAME.player().res().total().history(res).get(si)));
		}
		
		{
			b.NL(8);
			si = amount-hi-1;
			b.text(DicRes.¤¤buyPrice);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.pRel().pricesBuy.history(res).get(si)));
			b.NL();
			b.text(DicRes.¤¤sellPrice);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.pRel().pricesSell.history(res).get(si)));
			b.NL();
			
			b.textL(DicRes.¤¤Earnings);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), GAME.player().credits().inExported.history(res).get(si)-GAME.player().credits().outImported.history(res).get(si)));
			b.text(DicRes.¤¤Curr);
		}
		
		

	}
	
	private static class StorageDiagram extends GStaples {

		private final RESOURCE res;
		private DiaText t = new DiaText() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkNoColor(text, SETT.ROOMS().STOCKPILE.tally().amountTotal(res), SETT.ROOMS().STOCKPILE.tally().spaceTotal(res));
			}
		};
		
		StorageDiagram(RESOURCE res){
			super(amount);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {			
			
			super.render(r, ds, hoveredIs());
			t.render(r, body().x1()+w, body().y1()+ w/2);		
		}

		@Override
		protected double getValue(int stapleI) {
			double c = SETT.ROOMS().STOCKPILE.tally().spaceTotal(res);
			double d = SETT.ROOMS().STOCKPILE.tally().amountsSeason().history(res).get(amount-1-stapleI);
			if (c == 0)
				d = d > 0 ? 1 : 0;
			else
				d /= c;
			
			d = CLAMP.d(d, 0, 1);
			return d;
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			c.set(GCOLOR.UI().SOSO.normal);
		}
	}
	
	private static class ProductionDiagram extends GStaples {

		private final RESOURCE res;
		private DiaText t = new DiaText() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, GAME.player().res().total().history(res).get(1));
			}
		};
		
		ProductionDiagram(RESOURCE res){
			super(amount, true);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			super.render(r, ds, hoveredIs());
			t.render(r, body().x1()+w, body().y1()+ w/2);			
		}

		@Override
		protected double getValue(int stapleI) {
			return GAME.player().res().total().history(res).get(amount-1-stapleI);
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			if (value < 0)
				c.set(GCOLOR.UI().BAD.normal);
			else
				c.set(GCOLOR.UI().GOOD.normal);
			
		}
	}
	
	private static class TradeDiagram extends GStaples {

		private final RESOURCE res;
		private DiaText tprofits = new DiaText() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, GAME.player().credits().inExported.history(res).get(1)-GAME.player().credits().outImported.history(res).get(1));
			}
		};
		
		TradeDiagram(RESOURCE res){
			super(amount, true);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			super.render(r, ds, hoveredIs());
			tprofits.render(r, body().x1()+w, body().y1()+ w/2);
		}

		@Override
		protected double getValue(int stapleI) {
			return GAME.player().credits().inExported.history(res).get(amount-1-stapleI)-GAME.player().credits().outImported.history(res).get(amount-1-stapleI);
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			if (value < 0)
				c.set(GCOLOR.UI().BAD.normal);
			else
				c.set(GCOLOR.UI().GOOD.normal);
			
		}
	}
	
	private static class PriceDiagram extends GStaples {

		private COLOR cb = UIGoodsImport.color;
		private COLOR cs = UIGoodsExport.color;
		

		private final RESOURCE res;
		private DiaText tbuy = new DiaText() {
			
			@Override
			public void update(GText text) {
				int b = FACTIONS.pRel().pricesBuy.get(res);
				if (res == null)
					b /= RESOURCES.ALL().size();
				GFORMAT.i(text, b);
				text.color(cb);
			}
		};
		private DiaText tsell = new DiaText() {
			
			@Override
			public void update(GText text) {
				int s = FACTIONS.pRel().pricesSell.get(res);
				if (res == null)
					s /= RESOURCES.ALL().size();
				GFORMAT.i(text,s);
				text.color(cs);
				
			}
		};
		
		
		PriceDiagram(RESOURCE res){
			super(amount, false);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			super.render(r, ds, hoveredIs());
			tbuy.render(r, body().x1()+w, body().y1()+ w/2);
			tsell.render(r, body().x1()+w, body().y2()-18);
		}

		@Override
		protected double getValue(int stapleI) {
			int s = FACTIONS.pRel().pricesSell.history(res).get(amount-1-stapleI);
			int b = FACTIONS.pRel().pricesBuy.history(res).get(amount-1-stapleI);
			
			return Math.max(s, b);
		}
		
		@Override
		protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value, int x1,
				int x2, int y1, int y2) {
			if (value <= 0)
				return;
			double s = FACTIONS.pRel().pricesSell.get(res);
			double b = FACTIONS.pRel().pricesBuy.get(res);
			double ma = Math.max(s, b);
			s /= ma;
			b /= ma;
			int w = x2-x1;
			int h = y2-y1;
			ColorImp.TMP.set(cb);
			if (hovered)
				ColorImp.TMP.shadeSelf(1.2);
			ColorImp.TMP.render(r, x1, x1+w/2, (int) (y2-h*b), y2);
			
			ColorImp.TMP.set(cs);
			if (hovered)
				ColorImp.TMP.shadeSelf(1.2);
			ColorImp.TMP.render(r, x1+w/2, x2, (int) (y2-h*s), y2);
			
			
			
			
			super.renderExtra(r, color, stapleI, hovered, value, x1, x2, y1, y2);
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			c.set(GCOLOR.UI().bg());
			
		}
	}
	
	private static abstract class DiaText extends GStat {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, X1-4, X2+4, Y1-2, Y2+2);
			OPACITY.unbind();
			super.render(r, X1, X2, Y1, Y2);
		}
		
	}

}
