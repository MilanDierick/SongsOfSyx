package world.battle;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.config.Config;
import snake2d.LOG;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import world.army.AD;
import world.army.WDIV;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.regions.Region;
import world.regions.data.RD;

class Side  {
	
	private final SideUnitFactory factory;
	public final ArrayList<SideUnit> us = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);

	private int divisions;
	public int artillery;
	public int men;
	public final Coo coo = new Coo();
	public Faction faction;
	public boolean isPlayer;
	public boolean mustFight;
	
	private final SideUnit unitTmp = new SideUnit();
	private int ui[] = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int di[] = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	private double morale;
	
	
	
	public Side(SideUnitFactory factory){
		this.factory = factory;
	}
	
	void clear() {
		us.clearSloppy();
		isPlayer = false;
		divisions = 0;
		artillery = 0;
		faction = null;
		men = 0;
		mustFight = false;
	}

	public Side copy() {
		Side s = new Side(null);
		for (int ui = 0; ui < us.size(); ui++) {
			SideUnit u = us.get(ui);
			SideUnit n = new SideUnit();
			
			n.copy(u);
			s.us.add(n);
		}
		
		s.divisions = divisions;
		s.artillery = artillery;
		s.men = men;
		s.coo.set(coo);
		s.isPlayer = isPlayer;
		s.faction = faction;
		
		for (int i = 0; i < di.length; i++) {
			s.ui[i] = ui[i];
			s.di[i] = di[i];
		}
		
		
		
		return s;
	}
	
	public void debug() {
		LOG.ln(divs());
		for (int i = 0; i < divs(); i++)
			LOG.ln(div(i).men() + " " + div(i).name() + " " + div(i).bannerI());
	}
	
	public void add(WArmy a, boolean mustFight) {
		int max = Config.BATTLE.DIVISIONS_PER_ARMY-divisions;
		
		unitTmp.set(a, max, mustFight);
		
		int art = a.state() == WArmyState.fortified ? 150 : 300;
		art = AD.men(null).get(a) / art;
		
		

		inited(art, (0.25 + 0.75*AD.supplies().morale(a)), mustFight);
		
		
	}
	
	public double morale() {
		return morale / men;
	}
	
	public void add(Region reg, boolean mustFight) {
		int max = Config.BATTLE.DIVISIONS_PER_ARMY-divisions;
		
		unitTmp.set(reg, max, mustFight);
		inited(RD.MILITARY().garrison.get(reg)/300, 1.0, mustFight);
	}
	
	private void inited(int art, double mor, boolean mustFight) {
		
		if (unitTmp.divs() <= 0)
			return;
		
		for (int i = 0; i < us.size(); i++) {
			if (us.get(i).isSameAs(unitTmp))
				return;
		}
		artillery += art;
		
		SideUnit u = factory.next();
		
		u.copy(unitTmp);
		morale += u.men*mor;
		men += u.men;
		if (us.size() == 0)
			coo.set(u.coo);
		us.add(u);
		isPlayer |= u.faction() == FACTIONS.player();
		if (u.faction() != null) {
			if (faction == null || u.faction() == FACTIONS.player())
				faction = u.faction();
		}
		this.mustFight |= mustFight;
		
		for (int i = 0; i < u.divs(); i++) {
			ui[divisions] = us.size()-1;
			di[divisions] = i;
			divisions++;
		}
	}
	
	public int divs() {
		return divisions;
	}
	
	public WDIV div(int di) {
		SideUnit u = us.get(ui[di]);
		int i = this.di[di];
		if (i < 0 || i >= u.divs())
			return null;
		return u.div(i);
	}
	
	public int ui(int di) {
		return ui[di];
	}
	
	public SideUnit unit(int di) {
		return us.get(ui[di]);
	}

	
}