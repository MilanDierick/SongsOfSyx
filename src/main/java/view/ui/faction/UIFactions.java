package view.ui.faction;

import game.faction.Faction;
import game.faction.diplomacy.Deal;
import game.faction.diplomacy.DealDrawfter;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER.GETTER_IMP;
import util.dic.DicMisc;
import view.main.VIEW;
import view.ui.manage.IFullView;

public final class UIFactions extends IFullView{

	private final Deal deal = new Deal();
	private final UIFaction ff;
	private final GETTER_IMP<FactionNPC> getter = new GETTER_IMP<FactionNPC>() {
		
		@Override
		public void set(FactionNPC t) {
			super.set(t);
			if (get() != null)
				deal.setFactionAndClear(t);
		};
		
	};
	final Hoverer hov = new Hoverer();
	
	public UIFactions(){
		super(DicMisc.¤¤Factions);
		section.add(new UIFactionList(getter, HEIGHT));
		ff = new UIFaction(getter, deal, WIDTH-section.body().width()-16, HEIGHT);
		CLICKABLE.ClickWrap cl = new CLICKABLE.ClickWrap(WIDTH-section.body().width()-16, HEIGHT) {


			@Override
			protected RENDEROBJ pget() {
				if (getter.get() != null && getter.get().isActive())
					return ff;
				return null;
			}
		};
		
		section.addRelBody(8, DIR.E, cl);
	}
	
	public void open(FactionNPC r) {
		VIEW.UI().manager.show(this);
		getter.set(r);
	}
	
	public void openPeace(FactionNPC other) {
		open(other);
		deal.setFactionAndClear(other);
		DealDrawfter.draftPeace(deal, other);
		ff.dip();
		
	}
	
	public void openTrade(FactionNPC other) {
		open(other);
		deal.setFactionAndClear(other);
		deal.trade.setOn();
		DealDrawfter.draft(deal);
		ff.dip();
		
	}
	
	
	public void openBuy(FactionNPC other, RESOURCE res) {
		open(other);
		deal.setFactionAndClear(other);
		deal.npc.resources.set(res, 1);
		ff.dip();
		
	}
	
	
	public void openSell(FactionNPC other, RESOURCE res) {
		open(other);
		deal.setFactionAndClear(other);
		deal.player.resources.set(res, 1);
		ff.dip();
		
	}
	
	public void openDip(FactionNPC other) {
		open(other);
		deal.setFactionAndClear(other);
		ff.dip();
		
	}
	
	public void hover(GUI_BOX b, Faction r) {
		hov.hover(b, r);
	}

	
	
}
