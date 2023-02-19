package world.army;

import game.faction.Faction;
import init.race.Race;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import util.gui.misc.GBox;
import world.army.WINDU.WDivGeneration;
import world.army.WINDU.WInduStored;
import world.entity.army.WArmy;

public interface WDIV {
	
	public int men();
	public int menTarget();
	
	public void resolve(WInduStored[] hs);
	public void resolve(int surviviors, double experiencePerMan);
	public void menSet(int amount);
	
	public Race race();
	
	public double training_melee();
	public double training_ranged();
	public double experience();
	
	public int equipTarget(EQUIPPABLE_MILITARY e);
	public double equip(EQUIPPABLE_MILITARY e);
	
	public default int provess() {
		return WARMYD.boosts().power(this);
	}
	

	
	public int daysUntilMenArrives();
	public int amountOfMenThatWillArrive();
	
	public void disband();
	

	public WDivGeneration generate();
	
//	public Humanoid popAndmakeSoldier(int tx, int ty, HTYPE type);
	
	public  void hover(GBox b);
	
	public int type();

	public void reassign(WArmy a);
	
	public CharSequence name();
	
	public WArmy army();
	
	public Faction faction();
	
	public boolean needSupplies();

	public default DivisionBanner banner() {
		return SETT.ARMIES().banners.get(bannerI());
	}
	public int bannerI();
	public void bannerSet(int bi);
	
}
