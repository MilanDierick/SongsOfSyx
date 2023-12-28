package game.faction.diplomacy;


import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import snake2d.util.file.*;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.interrupter.IDebugPanel;
import world.log.WLogger;

public class Diplomacy implements SAVABLE{

	private final ArrayList<Faction> tmp = new ArrayList<Faction>(FACTIONS.MAX);
	public final DWar war = new DWar(tmp);
	private final DiplomacyData trade = new DiplomacyData();
	

	public Diplomacy() {
		new Faction.FactionActivityListener() {
			
			@Override
			public void remove(Faction f) {
				war.clear(f);
			}

			@Override
			public void add(Faction f) {
				
				if (f instanceof FactionNPC) {
					for (Faction f2 : FACTIONS.active()) {
						if (f2.isActive() && f2 != FACTIONS.player()) {
							trade.set(f2, f, true);
						}else {
							trade.set(f2, f, false);
						}
						if (f != f2)
							war.set(f, f2, false);
					}
				}
				
			}
		};
		
		IDebugPanel.add("Total War", new ACTION() {
			
			@Override
			public void exe() {
				for (Faction f : FACTIONS.NPCs()) {
					if (f != GAME.player())
						war.set(GAME.player(), f, true);
				}
			}
		});
	}
	
	public boolean trades(Faction a, Faction b) {
		if (a == null || b == null)
			return false;
		return trade.get(a, b);
	}
	
	public boolean trades(FactionNPC a) {
		return trades(a, FACTIONS.player());
	}
	
	public void trade(Faction a, Faction b, boolean trade) {
		tradeSilent(a, b, trade);
		if (trade) {
			WLogger.trade(a, b);
		}
	}
	
	public LIST<Faction> tradePartners(Faction fa){
		tmp.clear();
		for (Faction f : FACTIONS.active())
			if (f != fa && trades(f, fa))
				tmp.add(f);
		return tmp;
	}
	
	void tradeSilent(Faction a, Faction b, boolean trade) {
		if (this.trade.get(a, b) == trade)
			return;
		this.trade.set(a, b, trade);
	}
	


	@Override
	public void save(FilePutter file) {
		war.saver.save(file);
		trade.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		war.saver.load(file);
		trade.load(file);
	}

	@Override
	public void clear() {
		war.saver.clear();
		trade.clear();
	}
	
	
	
}
