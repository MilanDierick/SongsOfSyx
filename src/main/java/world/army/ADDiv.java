package world.army;

import java.io.IOException;

import game.faction.Faction;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import world.WORLD;
import world.entity.army.WArmy;

public abstract class ADDiv implements WDIV{
	

	short armyI = -1;
	public final int index;
			
	protected ADDiv(int index) {
		this.index = index;
	}
	
	public int costPerMan() {
		return 0;
	}
	
	public boolean needConscripts() {
		return false;
	}
	
	public abstract int type();
	
	protected void save(FilePutter file) {
		file.s(armyI);
	}

	protected void load(FileGetter file) throws IOException {
		armyI = file.s();
	}
	
//	protected void clear() {
//		armyI = -1;
//	}

	private final void armySet(WArmy e) {
		report(-1);
		
		WArmy old = army();
		if (old != null) {
			for (int i = 0; i < old.divs().size(); i++) {
				if (old.divs().get(i) == this) {
					old.divs().remove(i);
					break;
				}
			}
		}
		
		armyI = e == null ? -1 : e.armyIndex();
		if (e != null) {
			
			int i = army().divs().add();
			long d = WArmyDivs.BType.set(0, type());
			d |= index;
			army().divs().setData(i, d);
		}
		report(1);
	}


	
	public void disband() {
		reassign(null);
	}


	public final WArmy army() {
		if (armyI == -1)
			return null;
		return WORLD.ENTITIES().armies.get(armyI);
	}
	
	protected void report(int i){
		AD.register(this, needSupplies(), needConscripts(), costPerMan(), i);
	}

	
	public final void reassign(WArmy a) {
		if (needSupplies() && army() != null) {
			WArmy oldA = army();
			double sup = AD.supplies().all.get(0).current().get(army());
			if(sup > 0) {
				sup = menTarget()/sup;
			}
			armySet(a);
			AD.supplies().transfer(this, oldA, army());
			
		}else
			armySet(a);
	}


	@Override
	public final DivisionBanner banner() {
		return SETT.ARMIES().banners.get(bannerI());
	}


	@Override
	public final Faction faction() {
		if (army() == null)
			return null;
		return army().faction();
	}
	
	
	public abstract void menSet(int amount);
	
	

}
