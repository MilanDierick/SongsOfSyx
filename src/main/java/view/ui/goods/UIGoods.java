package view.ui.goods;

import game.faction.Faction;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.gui.table.GScrollRows;
import view.main.VIEW;
import view.ui.manage.IFullView;

public final class UIGoods extends IFullView {

	private final GScrollRows rows;
	
	public final Icon icon = SPRITES.icons().s.urn;
	public static CharSequence ¤¤Name = "¤Goods";
	public static CharSequence ¤¤Desc = "¤Goods and Trade";
	private final UIGoodsTax tax = new UIGoodsTax();
	private RESOURCE flashRes;
	private double flashTime;
	private final GInput filter = new GInput(new StringInputSprite(10, UI.FONT().S).placeHolder(DicMisc.¤¤Search));
	
	static {
		D.ts(UIGoods.class);
	}
	
	
	public void detail(RESOURCE res, Faction f) {
		
		activate();
		
		rows.target.set(res.index());
		flashRes = res;
		flashTime = VIEW.renderSecond();
	}
	
	@Override
	public void activate() {
		flashRes = null;
		filter.text().clear();
		filter.focus();
		rows.init();
		super.activate();
	}
	
	public UIGoods() {
		super(¤¤Name);
		
		GuiSection headers = new GuiSection();
		
		headers.addRightCAbs(0, new GHeader(DicRes.¤¤Stored));
		headers.addRightCAbs(198, new GHeader(DicRes.¤¤Produced));
		headers.addRightCAbs(198, new GHeader(DicRes.¤¤Earnings));
		headers.addRightCAbs(198, new GHeader(DicRes.¤¤Price));
		
		
		headers.addRightCAbs(198, new HOVERABLE.Sprite(SETT.ROOMS().IMPORT.icon).hoverTitleSet(UIGoodsImport.¤¤name));
		headers.addRightCAbs(130, new HOVERABLE.Sprite(SETT.ROOMS().EXPORT.icon).hoverTitleSet(UIGoodsExport.¤¤name));

		
		
		headers.addRightCAbs(100, filter);

		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(RESOURCES.ALL().size() + RESOURCES.CATEGORIES()+2);
		
		rows.add(new Entry(null));
		
		for (RESOURCE res : RESOURCES.ALL()) {
			
			rows.add(new Entry(res));
		}
		
		int hi = HEIGHT-16-headers.body().height();
		
		hi = rows.get(0).body().height()*(hi/ rows.get(0).body().height());
		
		
		this.rows = new GScrollRows(rows, hi) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (filter.text().length() == 0)
					return true;
				Entry e = (Entry) o;
				if (e.res == null)
					return false;
				return (Str.containsText(e.res.name, filter.text()) || Str.containsText(e.res.names, filter.text()));

			}
			
		};
		
		GuiSection section = new GuiSection();
		
		section.add(this.rows.view());

		section.add(headers, 60, -headers.body().height()-4);
		
		this.section.add(section, 0, TOP_HEIGHT+16);
		
	}

	private class Entry extends GuiSection{
		
		private final RESOURCE res;
		
		Entry(RESOURCE r){
			this.res = r;
			
			SPRITE n = r == null ? SPRITES.icons().m.urn.big : r.icon().big;
			CharSequence desc = r == null ? DicMisc.¤¤All : r.desc;
			
			add(new HOVERABLE.Sprite(n).hoverInfoSet(desc));
			addRightCAbs(48, new UIGoodsHistory(r));
			
			UIGoodsImport im = new UIGoodsImport();
			
			addRelBody(8, DIR.E, UIGoodsImport.miniControl(r, im));
			
			UIGoodsExport ex = new UIGoodsExport();
			
			addRelBody(8, DIR.E, UIGoodsExport.mini(r, ex));
			
			addRelBody(8, DIR.E, tax.butt(r));
			
			addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(body().width(), 6) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().cY(), body().cY()+1);
					
				}
			});
			
			
			
			pad(6, 0);
			
		}
		
		
		@Override
		public void render(SPRITE_RENDERER rr, float ds) {
			if (res != null && flashRes == res && VIEW.renderSecond()-flashTime < 3) {
				COLOR.WHITE2WHITE.render(rr, body(), -1);
				
				
			}else if (hoveredIs()) {
				OPACITY.O012.bind();
				COLOR.WHITE100.render(rr, body(), -1);
				OPACITY.unbind();
			} 
			super.render(rr, ds);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (res != null)
				text.title(res.name);
			super.hoverInfoGet(text);
		}
		
	}


}
