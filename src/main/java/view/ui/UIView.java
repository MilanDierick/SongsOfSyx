package view.ui;

import view.sett.ui.health.UIHealth;
import view.ui.battle.UIBattle;
import view.ui.credits.UICredits;
import view.ui.faction.UIFactions;
import view.ui.goods.UIGoods;
import view.ui.log.UILog;
import view.ui.manage.IManager;
import view.ui.profile.UIProfile;
import view.ui.tech.UITechTree;
import view.ui.tourism.UITourists;
import view.ui.wiki.WIKI;

public class UIView {

	public final UICredits trade;
	public final UITourists tourists;
	public final UIGoods goods;
	public final UITechTree tech;
	public final UIFactions factions;
	
	public final UIProfile level;
	public final UIHealth health;
	public final UILog log = new UILog(null);
	public final WIKI wiki = new WIKI();
	public final UIBattle battle = new UIBattle();
	public final IManager manager;
	public UIView() {
		trade = new UICredits();
		goods = new UIGoods();
		tech = new UITechTree();
		level = new UIProfile();
		health = new UIHealth();
		tourists = new UITourists();
		factions = new UIFactions();
		manager = new IManager(this);
	}
	
}
