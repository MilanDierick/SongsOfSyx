package world.army.ai;

import game.faction.Faction;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.army.AD;
import world.army.WDivRegional;
import world.entity.army.WArmy;
import world.regions.Region;

final class Recruiter {

	private final Tree<WArmy> tree = new Tree<WArmy>(100) {

		@Override
		protected boolean isGreaterThan(WArmy current, WArmy cmp) {
			if (AD.men(null).target().get(current) > AD.men(null).target().get(cmp))
				return true;
			return current.armyIndex() > cmp.armyIndex();
		}
	
	};

	public void recruit(Faction f) {
		
		if (f.realm().all().size() == 0)
			return;
		
		int menTarget = (int) (AD.conscripts().available(null).get(f));
		int men = AD.men(null).target().total().get(f);
		int recruits = menTarget-men;
		if (recruits < 100)
			return;
		
		int armies = CLAMP.i(1 + menTarget/5000, 1, 3);

		while(f.armies().all().size() < armies && f.armies().canCreate()) {
			Region r = f.realm().all().rnd();
			COORDINATE c = WORLD.PATH().rnd(r);
			f.armies().create(c.x(), c.y());
		}
		
		
		tree.clear();
		for (int ai = 0; ai < f.armies().all().size(); ai++) {
			WArmy a = f.armies().all().get(ai);
			tree.add(a);
		}
		
		while(tree.hasMore()) {
			WArmy a = tree.pollGreatest();
			int target = menTarget;
			if (tree.hasMore()) {
				target *= 0.75;
			}
			
			target = CLAMP.i(target, 0, Config.BATTLE.MEN_PER_ARMY);
			if (target > AD.men(null).target().get(a)) {
				recruit(f, a, target);
			}
			
			menTarget -= AD.men(null).target().get(a);
			
		}

		
	}
	
	private void recruit(Faction f, WArmy a, int target) {
		
		main:
		while(AD.men(null).target().get(a) < target && a.divs().canAdd()) {
			int ri = RND.rInt(RACES.all().size());
			
			for (int i = 0; i < RACES.all().size(); i++) {
				Race r = RACES.all().get((ri+i)%RACES.all().size());
				
				int am = AD.conscripts().available(r).get(f);
				
				am = CLAMP.i(am, 0, Config.BATTLE.MEN_PER_DIVISION);
				
				
				if (am > 0) {
					
					am = CLAMP.i(am, 20, Config.BATTLE.MEN_PER_DIVISION);
					
					WDivRegional d = WORLD.ARMIES().regional().create(r, (double)am/Config.BATTLE.MEN_PER_DIVISION, a);
					d.randomize(((1+f.index())%10)/10.0, (4 + f.index()%12)/15.0);
					//d.menSet(d.menTarget());
					continue main;
				}
			}
			break;
		}
		
	}
	

}
