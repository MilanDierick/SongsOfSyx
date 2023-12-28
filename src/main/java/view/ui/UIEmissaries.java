package view.ui;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.emissary.EMission;
import game.faction.player.emissary.Emissaries;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public class UIEmissaries extends ISidePanel {
	

	public UIEmissaries() {
		
		titleSet(Emissaries.¤¤names);
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return FACTIONS.player().emissaries.all().size();
			}
		};
		
		bu.column(null, 240, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Butt(ier);
			}
		});
		
		bu.column(null, 48, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GButt.ButtPanel(UI.icons().s.arrow_left) {
					@Override
					protected void clickA() {
						EMission e = FACTIONS.player().emissaries.all().get(ier.get());
						if (e == null)
							return;
						if (e.mission() == null)
							return;
						e.recall();
					}
					
					@Override
					protected void renAction() {
						activeSet(false);
						EMission e = FACTIONS.player().emissaries.all().get(ier.get());
						if (e == null)
							return;
						if (e.mission() == null)
							return;
						activeSet(true);
					}
				};
			}
		});
		
		section.add(bu.createHeight(HEIGHT-64, false));
		
		section.addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, FACTIONS.player().emissaries.available(), FACTIONS.player().emissaries.emissaries());
			}
		}.hv(DicMisc.¤¤Available));
		
	}
	
	private class Butt extends ClickableAbs {

		private final GETTER<Integer> ier;
		
		Butt(GETTER<Integer> ier){
			this.ier = ier;
			body.setDim(240, UI.FONT().S.height()+8);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			EMission e = FACTIONS.player().emissaries.all().get(ier.get());
			if (e == null)
				return;
			GBox b = (GBox) text;
			b.textLL(e.mission().name);
			b.NL();
			
			GText t = b.text();
			e.mission().edesc(t, e);
			b.add(t);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			EMission e = FACTIONS.player().emissaries.all().get(ier.get());
			if (e == null)
				return;
			GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
			CharSequence n = e.mission().name;
			if (e.mission() == null)
				OPACITY.O50.bind();
			UI.FONT().S.render(r, body().x1()+4, body().y1()+4, DIR.W, n, 0, n.length(), body.width()-8, UI.FONT().S.height(), 1.0);
			OPACITY.unbind();
			if (e.mission() != null)
				e.mission().icon.render(r, body().x2()-24, body().y1()+2);
			GButt.ButtPanel.renderFrame(r, true, false, isHovered, body);
		}
		
		@Override
		protected void clickA() {
			EMission e = FACTIONS.player().emissaries.all().get(ier.get());
			if (e == null)
				return;
			if (e.mission() == null)
				return;
			if (e.mission().reg(e) != null){
				VIEW.world().activate();
				VIEW.world().panels.addDontRemove(UIEmissaries.this, VIEW.world().UI.regions.get(e.mission().reg(e)));
			}else if (e.mission().faction(e) != null) {
				VIEW.UI().factions.open((FactionNPC) e.mission().faction(e));
			}
		}
		
		
	}
	
	
}
