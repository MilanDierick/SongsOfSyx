package game.battle;

import java.io.IOException;

import game.battle.Conflict.Side;
import game.faction.FACTIONS;
import game.faction.Faction;
import settlement.invasion.InvadorDiv;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListIntegerResize;
import world.World;
import world.army.WARMYD;
import world.army.WDIV;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.Region;

final class PollerSieges implements Poller{

	private final ArrayListIntegerResize armySieges = new ArrayListIntegerResize(16, 1024);
	private final PromptBesige siege;
	private final PromptConquer conquer;
	private final Conflict conflict;
	
	public PollerSieges(Conflict conflict, PromptUtil util) {
		this.conflict = conflict;
		siege = new PromptBesige(conflict, util);
		conquer = new PromptConquer(util);
	}
			
	@Override
	public Prompt poll() {
		if (armySieges.size() == 0)
			return null;
		
		while(!armySieges.isEmpty()) {
			WArmy a = World.ENTITIES().armies.get(armySieges.get(armySieges.size()-4));
			Region r = World.REGIONS().getByIndex(armySieges.get(armySieges.size()-3));
			double timer = armySieges.get(armySieges.size()-2);
			boolean first = armySieges.get(armySieges.size()-1) == 1;
			armySieges.remove(armySieges.size()-1);
			armySieges.remove(armySieges.size()-1);
			armySieges.remove(armySieges.size()-1);
			armySieges.remove(armySieges.size()-1);
			
			if (a == null || !a.added() || a.state() != WArmyState.besieging || !Util.enemy(a.faction(), r.faction()))
				continue;
			
			if (!conflict.make(a, r, timer))
				continue;
			
			if (first) {
				if (conflict.isPlayer()) {
					if (conflict.sideB.garrison() != null) {
						if (conflict.sideA.losses == 0) {
							return conquer.open(conflict, r, true);
						}else {
							return siege.activate(timer);
						}
					}else {
						if (conflict.sideB.losses == 0) {
							conquer(r, conflict.sideB, RND.rExpo());
						}else {
							new Messages.MessageSiege(r, conflict.sideB.get(0).faction());
						}
					}
				}else {
					if (conflict.sideB.garrison() != null) {
						if (conflict.sideB.power <= 0 || conflict.sideA.power / conflict.sideB.power > 2) {
							conquer(r, conflict.sideA, RND.rExpo());
						}
					}else {
						if (conflict.sideA.power <= 0 || conflict.sideB.power / conflict.sideA.power > 2) {
							conquer(r, conflict.sideB, RND.rExpo());
						}
					}
				}
			}else {
				if (conflict.isPlayer()) {
					if (conflict.sideB.garrison() != null) {
						if (conflict.sideA.losses == 0) {
							return conquer.open(conflict, r, true);
						}
					}else {
						
						if (conflict.sideB.losses == 0) {
							conquer(r, conflict.sideB, RND.rExpo());
						}
					}
				}else {
					if (conflict.sideB.garrison() != null) {
						if (conflict.sideA.power / (1+conflict.sideB.power) > 2) {
							conquer(r, conflict.sideA, RND.rExpo());
						}
					}else {
						if (conflict.sideB.power / (1+conflict.sideA.power) > 2) {
							conquer(r, conflict.sideB, RND.rExpo());
						}
					}
				}
			}
			
			
			
		}
		
		return null;
		
	}
	
	private void conquer(Region r, Side side, double kills) {
		
		if (r == FACTIONS.player().capitolRegion()) {
			if (!SETT.PATH().entryPoints.hasAny())
				return;
			
			int am = 0;
			for (WArmy a : side) {
				am += a.divs().size();
			}
			
			InvadorDiv[] divs = new InvadorDiv[am];
			int k = 0;
			for (WArmy a : side) {
				for (int di = 0; di < a.divs().size(); di++) {
					WDIV d = a.divs().get(di);
					InvadorDiv dd = new InvadorDiv();
					dd.men = d.men();
					dd.race = d.race();
					dd.experience = d.experience();
					dd.trainingM = d.training_melee();
					dd.trainingR = d.training_ranged();
					for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
						int equip = d.equipTarget(m);
						if (d.needSupplies()) {
							equip *= WARMYD.supplies().get(m).getD(d.army());
						}
						dd.equipment[m.indexMilitary()] = equip;
					}
					divs[k++] = dd;
				}
			}
			
			Faction f = side.get(0).faction();
			DIR d = DIR.get(side.get(0).ctx(), side.get(0).cty(), SETT.WORLD_AREA().tiles().cX(), SETT.WORLD_AREA().tiles().cY());
			
			SETT.INVADOR().invade(d, divs, f);
			
			
			for (WArmy a : side) {
				a.disband();
			}
			
		}else {
			Resolver.conquer(r, side.get(0).faction(), kills);
		}
		
	}
	
	public void besiegeFirst(WArmy a, Region r, double timer) {
		
		if (a.faction() == r.faction())
			throw new RuntimeException();
		if (r.faction() != null && a.faction() != null) {
			FACTIONS.rel().war.set(a.faction(), r.faction(), 1);
		}
		
		armySieges.add(a.armyIndex());
		armySieges.add(r.index());
		armySieges.add((int) timer);
		armySieges.add(1);
	}
	
	public void besiegeContinous(WArmy a, Region r, double timer) {
		armySieges.add(a.armyIndex());
		armySieges.add(r.index());
		armySieges.add((int) (timer));
		armySieges.add(0);
	}

	@Override
	public void save(FilePutter file) {
		armySieges.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		armySieges.load(file);
		
	}

	@Override
	public void clear() {
		armySieges.clear();
	}

}
