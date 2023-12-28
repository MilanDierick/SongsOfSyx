package view.ui.diplomacy;

import java.util.LinkedList;

import game.faction.diplomacy.Deal;
import game.faction.diplomacy.DealSave;
import init.D;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.dic.DicRes;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import world.WORLD;
import world.regions.Region;

public class UIDealListSaved extends GuiSection{

	private static CharSequence ¤¤YouGet = "We Give you";
	private static CharSequence ¤¤FactionGets = "You give us";

	static {
		D.ts(UIDealListSaved.class);
	}
	
	public UIDealListSaved(DealSave deal, int height){
		
		LinkedList<RENDEROBJ> rowsp = new LinkedList<>();
		LinkedList<RENDEROBJ> rowsnpc = new LinkedList<>();
		
		for (int i = 0; i < deal.bools.length; i++) {
			if (deal.bools[i]) {
				rowsp.add(bool(Deal.TMP().bools().get(i)));
				rowsnpc.add(bool(Deal.TMP().bools().get(i)));
			}
		}
		
		party(rowsp, deal.player);
		party(rowsnpc, deal.npc);
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		if (rowsnpc.size() != 0) {
			rows.add(row(new GHeader(¤¤YouGet)));
			for (RENDEROBJ o : rowsnpc)
				rows.add(row(o));
		}
		
		if (rowsp.size() != 0) {
			rows.add(new GHeader(¤¤FactionGets));
			for (RENDEROBJ o : rowsp)
				rows.add(row(o));
		}
		
		add(new GScrollRows(rows, height).view());
		
	}
	
	private RENDEROBJ row(RENDEROBJ o) {
		return new HOVERABLE.HoverableAbs(400, 32) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				
				o.body().moveX1Y1(body);
				o.body().moveCY(body.cY());
				o.render(r, ds);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (o instanceof HOVERABLE) {
					((HOVERABLE) o).hoverInfoGet(text);
				}
			}
		};
	}
	
	private void party(LinkedList<RENDEROBJ> rows, DealSave.Party p) {
		
		if (p.creditsP != 0) {
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, p.creditsP);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(DicRes.¤¤Currs);
				};
				
			}.hh(UI.icons().s.money));
		}
		
		for (int i : p.regsP) {
			
			if (i >= 0 && WORLD.REGIONS().getByIndex(i).active()) {
				final Region reg = WORLD.REGIONS().getByIndex(i);
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						text.add(reg.info.name());
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(DicRes.¤¤Currs);
					};
					
				}.hh(UI.icons().s.world));
			}
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (p.resP[res.index()] != 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, p.resP[res.index()]);
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(res.names);
					};
					
				}.hh(res.icon()));
			}
		}
		
	}
	
	private static RENDEROBJ bool(Deal.DealBool bo) {
		GText t = new GText(UI.FONT().M, bo.info.name);
		GTextR tt = new GTextR(t);
		tt.hoverInfoSet(bo.info.desc);
		return tt;
	}

	
}
