package view.ui.tech;

import game.faction.FACTIONS;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;

class Info extends GuiSection{

	private final StringInputSprite sp = new StringInputSprite(16, UI.FONT().S);
	private final UITechTree tree;
	Info(UITechTree tree){

		this.tree = tree;
		body().setWidth(1200);

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
			
			
			sp.placeHolder(DicMisc.¤¤Filter);
			
			GInput in = new GInput(sp);
			
			g.add(in);
			
			
			
		}
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (sp.text().length() > 0) {
			for (TECH t : TECHS.ALL()) {
				if (!Str.containsText(t.info.name, sp.text())) {
					tree.filter(t);
				}
			}
			
			
		}
		super.render(r, ds);
	}
	
}
