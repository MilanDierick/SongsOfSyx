package view.world.ui;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.dic.DicRes;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.World;
import world.entity.WEntity;
import world.entity.caravan.Shipment;
import world.entity.caravan.Shipment.Type;

public class UICaravanList extends GButt.ButtPanel{

	private final ArrayList<Shipment> all = new ArrayList<>(100);
	private final ISidePanel p;
	public UICaravanList() {
		super(World.ENTITIES().caravans.icon);
		hoverInfoSet(DicRes.¤¤Inbound);
		
		GTableBuilder b = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				fill();
				return all.size();
			}
		};
		
		b.column(null, 300, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		p = new ISidePanel(b.createHeight(ISidePanel.HEIGHT, true));
		p.titleSet(DicRes.¤¤Inbound);
		
		
	}
	
	@Override
	protected void clickA() {
		VIEW.world().panels.add(p, true);
	}
	
	@Override
	protected void renAction() {
		selectedSet(VIEW.world().panels.added(p));
	}
	
	private class Row extends HOVERABLE.HoverableAbs {

		private final GETTER<Integer> g;
		
		Row(GETTER<Integer> g){
			body.setDim(300, 32);
			this.g = g;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (g.get() >= all.size())
				return;
			Shipment s = all.get(g.get());
			SPRITE icon = SPRITES.icons().m.urn;
			if (s.type() == Type.spoils)
				icon = SPRITES.icons().m.shield;
			else if (s.type() == Type.tax)
				icon = SPRITES.icons().m.raw_materials;
			icon.renderCY(r, 10, body().cY());
			
			int m = 0;
			int x1 = body().x1()+64;
			for (RESOURCE res : RESOURCES.ALL()) {
				if (s.loadGet(res) > 0) {
					m++;
					res.icon().renderCY(r, x1, body().cY());
					x1 += ICON.MEDIUM.SIZE;
					if (m > 12)
						break;
						
				}
			}
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			if (super.hover(mCoo)) {
				if (g.get() >= all.size())
					return true;
				Shipment s = all.get(g.get());
				World.OVERLAY().hover(s);
				VIEW.world().window.centererTile.set(s.ctx(), s.cty());
				return true;
			}
			return false;
		};
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (g.get() >= all.size())
				return;
			
			Shipment s = all.get(g.get());
			WorldHoverer.hover(text, s);
		}
		
		
	}
	
	private void fill() {
		all.clearSloppy();
		for (WEntity e : World.ENTITIES().all()) {
			if (e != null && e.added() && e instanceof Shipment) {
				Shipment s = (Shipment) e;
				if (s.destination() != null && s.destination() == FACTIONS.player().capitol().r()) {
					all.add(s);
					if (!all.hasRoom())
						return;
				}
			}
			
		}
		
	}
	
}
