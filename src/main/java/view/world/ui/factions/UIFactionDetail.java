package view.world.ui.factions;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.C;
import init.D;
import init.settings.S;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.entity.WPathing;
import world.entity.WPathing.FactionDistance;
import world.map.regions.Region;

final class UIFactionDetail extends ISidePanel{

	FactionNPC faction;

	
	UIFactionDetail(){
		
		D.gInit(this);
		GGrid grid = new GGrid(section, 300*C.SG, 2, 0, 0);
		grid.setAlignment(DIR.C);
		
		{
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, faction.kingdom().realm().population().total().get());
					
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Subjects"), pop) {
				GChart chart = new GChart().legend();
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
					chart.body().setWidth(150).setHeight(100);
					chart.clear();
					chart.add(faction.kingdom().realm().population().total());
					text.add(chart);
					super.hoverInfoGet(text);
				}
			};
			
			grid.add(h);

		}		
		
		{
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, faction.capitol().population.total().get());
					
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Citizens"), pop);
			grid.add(h);

		}
		
		{
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					int am = 0;
					for (Region r : faction.kingdom().realm().regions())
						am += r.area();
					GFORMAT.i(text, am);
					
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Area"), pop);
			grid.add(h);

		}
		
		{
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, (int)faction.credits().trueCredits());
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Credits"), pop);
			grid.add(h);

		}
		
		{
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f(text, faction.credits().trueCredits()/(double)faction.capitol().population.total().get());
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("BNP"), pop);
			grid.add(h);

		}
		
		if (S.get().developer){
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, faction.index());
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Index"), pop);
			grid.add(h);
			
		}
		
		if (S.get().developer){
			GStat pop = new GStat() {
				
				@Override
				public void update(GText text) {
					for (FactionDistance d : WPathing.getFactions(faction)){
						text.add(d.f.appearence().name());
						text.add(' ');
					}
				}
			};
			
			GHeader.HeaderVertical h = new GHeader.HeaderVertical(D.g("Reachable"), pop);
			grid.NL();
			grid.add(h);
			
		}
		
		section.addRelBody(16, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				if (FACTIONS.rel().war.get(FACTIONS.player(), faction) == 1)
					text.add(DicArmy.¤¤Enemy);
				else if (FACTIONS.rel().overlord.get(FACTIONS.player(), faction) == 1)
					text.add(DicArmy.¤¤Puppet);
				else if (FACTIONS.rel().overlord.get(faction, FACTIONS.player()) == 1)
					text.add(DicArmy.¤¤Puppet);
				else
					text.add(DicArmy.¤¤Neutral);
			}
		}.r(DIR.N));
		
		CLICKABLE buySell = new UIFactionDetailMarket(this, HEIGHT-section.body().height());
		section.addRelBody(0, DIR.S, buySell);
	
	}
	
	void activate(FactionNPC faction) {
		this.faction = faction;
		titleSet(faction.appearence().name());
		VIEW.world().panels.add(this, false);
		
		//list.open(f, m);
	}

	public boolean isShowing(Faction f) {
		return VIEW.world().panels.added(this) && this.faction == f;
	}
	
	
}
