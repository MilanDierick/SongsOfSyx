package view.ui.tech;

import game.faction.FACTIONS;
import init.boostable.BOOSTABLES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;

class Info extends GuiSection{


	Info(){

		body().setWidth(800);

		{
			GGrid g = new GGrid(this, 5);
			g.setMarginY(8);
			g.setAlignment(DIR.C);
			g.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.player().tech().available().get());
					text.normalify2();
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					FACTIONS.player().tech().available().info().hover(b);
				};
				
			}.increase().hv(FACTIONS.player().tech().available().info().name));
			g.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.player().tech().allocated().get());
					text.normalify2();
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					FACTIONS.player().tech().allocated().info().hover(b);
				};
				
			}.increase().hv(FACTIONS.player().tech().allocated().info().name));
			g.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.player().tech().frozen().get());
					text.normalify2();
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					FACTIONS.player().tech().frozen().info().hover(b);
				};
				
			}.increase().hv(FACTIONS.player().tech().frozen().info().name));
			g.add(new GStat() {
				
				@Override
				public void update(GText text) {
					double p = 1.0-FACTIONS.player().tech().penalty().getD();
					GFORMAT.percInv(text, p);
					if (p > 0)
						text.s().add('>').add((int)(100*FACTIONS.player().tech().penaltyNext())).add('%');
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					FACTIONS.player().tech().penalty().info().hover(b);
				};
				
			}.increase().hv(FACTIONS.player().tech().penalty().info().name));
			InfoBonuses bonus = new InfoBonuses();
			g.add(new GButt.ButtPanel(BOOSTABLES.INFO().name) {
				@Override
				protected void clickA() {
					if (VIEW.UI().tech.last().added(bonus))
						VIEW.UI().tech.last().remove(bonus);
					else
						VIEW.UI().tech.last().add(bonus, false);
				}
				@Override
				protected void renAction() {
					selectedSet(VIEW.UI().tech.last().added(bonus));
				}
			});
			
		}
	}
	
}
