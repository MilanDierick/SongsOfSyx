package world.army;

import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import world.army.util.DIV_STATS;

public interface WDIV extends DIV_STATS{
	
	public void resolve(Induvidual[] hs);
	public void resolve(int surviviors, double experiencePerMan);
	public WDivGeneration generate();
	
	public int equipTarget(EquipBattle e);
	public double trainingTarget(StatTraining tr);
	
	public default int provess() {
		return (int) AD.UTIL().power.get(this);
	}
	
	public int daysUntilMenArrives();

	public CharSequence name();

	public boolean needSupplies();

	public default DivisionBanner banner() {
		return SETT.ARMIES().banners.get(bannerI());
	}
	public int bannerI();
	public void bannerSet(int bi);
	public int menTarget();
}
