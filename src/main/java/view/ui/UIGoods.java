package view.ui;

import game.faction.Faction;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.SPRITES;
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
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.dic.DicRes;
import util.gui.misc.GHeader;
import util.gui.table.GScrollRows;
import view.interrupter.ISidePanel;

public final class UIGoods extends ISidePanel {

	private final GScrollRows rows;
	
	public final ICON.SMALL icon = SPRITES.icons().s.urn;
	public final CharSequence ¤¤Name = "¤Goods";
	public final CharSequence ¤¤Desc = "¤Goods and Trade";
	private final UIGoodsTradeSpecial special = new UIGoodsTradeSpecial();
	

	
	public ISidePanel detail(RESOURCE res, Faction f) {
		rows.target.set(res.index() + res.category);
		return this;
	}
	
	public UIGoodsTradeSpecial specialTrade(RESOURCE res) {
		return special.get(res);
	}
	
	UIGoods() {

		D.t(this);
		titleSet(¤¤Name);
		
		GuiSection headers = new GuiSection();
		
		headers.add(new GHeader(DicRes.¤¤Stored));
		headers.addRightCAbs(164, new GHeader(DicRes.¤¤Produced));
		headers.addRightCAbs(164, new GHeader(DicRes.¤¤Earnings));
		headers.addRightCAbs(172, new GHeader(DicRes.¤¤Buy));
		headers.addRightCAbs(114, new GHeader(DicRes.¤¤Sell));
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(RESOURCES.ALL().size() + RESOURCES.CATEGORIES()+2);
		
		rows.add(entry(null));
		rows.add(new RENDEROBJ.RenderImp(rows.last().body().width(), 32) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				
			}
		});
		
		int lastCat = RESOURCES.ALL().get(0).category;
		
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (lastCat != res.category) {
				rows.add(new RENDEROBJ.RenderImp(rows.last().body().width(), 32) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						
						
					}
				});
				lastCat = res.category;
			}
			
			rows.add(entry(res));
		}
		this.rows = new GScrollRows(rows, HEIGHT-48-headers.body().height());
		section.add(this.rows.view());

		section.add(headers, 60, -headers.body().height()-4);
		
	}

	
	GuiSection entry(RESOURCE r) {
		
		SPRITE n = r == null ? SPRITES.icons().m.urn.huge : r.icon().huge;
		CharSequence desc = r == null ? DicMisc.¤¤All : r.desc;
		
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (hoveredIs()) {
					OPACITY.O012.bind();
					COLOR.WHITE100.render(r, body(), -1);
					OPACITY.unbind();
				}
				super.render(r, ds);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (r != null)
					text.title(r.name);
				super.hoverInfoGet(text);
			}
		};
		s.add(new HOVERABLE.Sprite(n).hoverInfoSet(desc));
		

		s.addRightCAbs(48, new UIGoodsHistory(r));
		s.addRelBody(8, DIR.E, new UIGoodsTrade(r));
		
		s.addRelBody(2, DIR.S, new RENDEROBJ.RenderImp(s.body().width(), 6) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().cY(), body().cY()+1);
						
					}
				});
		
		s.pad(6, 0);
		return s;
	}


}
