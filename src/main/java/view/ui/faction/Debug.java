package view.ui.faction;

import java.util.LinkedList;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.table.GScrollRows;
import view.main.VIEW;

final class Debug extends GButt.ButtPanel{

	private final GuiSection s = new GuiSection(); 
	
	public Debug(GETTER<FactionNPC> g) {
		super(UI.icons().s.cog);

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		rows.add(new GButt.ButtPanel("War + 1") {
			@Override
			protected void clickA() {
				for (FactionNPC f : FACTIONS.NPCs()) {
					if (f != g.get() && !FACTIONS.DIP().war.is(f, g.get())) {
						FACTIONS.DIP().war.set(f, g.get(), true);
						return;
					}
				}
				super.clickA();
			}
		});
		
		rows.add(new GButt.ButtPanel("Peace + 1") {
			@Override
			protected void clickA() {
				for (FactionNPC f : FACTIONS.NPCs()) {
					if (f != g.get() && FACTIONS.DIP().war.is(f, g.get())) {
						FACTIONS.DIP().war.set(f, g.get(), false);
						return;
					}
				}
				super.clickA();
			}
		});
		
		rows.add(new GButt.ButtPanel("War player") {
			@Override
			protected void clickA() {
				FACTIONS.DIP().war.set(FACTIONS.player(), g.get(), !FACTIONS.DIP().war.is(FACTIONS.player(), g.get()));
			}
		});
		
		s.add(new GScrollRows(rows, 500).view());
		
	}
	
	@Override
	protected void clickA() {
		VIEW.inters().popup.show(s, this);
	}

}
