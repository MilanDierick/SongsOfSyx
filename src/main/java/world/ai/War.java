package world.ai;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEAN_OBJECT;
import util.updating.IUpdater;
import view.main.VIEW;
import world.World;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.army.WDivRegional;
import world.entity.WEntity;
import world.entity.WPathing;
import world.entity.WPathing.RegPath;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.regions.Region;

final class War extends IUpdater{

	
	
	private final Tree<WArmy> recruitTree = new Tree<WArmy>(100) {

		@Override
		protected boolean isGreaterThan(WArmy current, WArmy cmp) {
			if (WARMYD.men(null).target().get(current) > WARMYD.men(null).target().get(cmp))
				return true;
			return current.armyIndex() > cmp.armyIndex();
		}
	
	};
	private final Bitsmap1D hasSentMessage = new Bitsmap1D(0, 4, FACTIONS.MAX);
	
	private final ArrayList<WArmy> armies = new ArrayList<>(16);
	
	private static CharSequence ¤¤mess = "¤We, the people of {0} offer to lay down our arms and surrender. Make of us a subject and we shall pay you annually a hefty sum. Do you accept?";
	
	public War() {
		super(FACTIONS.MAX, TIME.secondsPerDay/2);
	}

	@Override
	protected void update(int i, double timeSinceLast) {
		Faction f = FACTIONS.all().get(i);
		if (f.capitolRegion() == null)
			return;
		if (f == FACTIONS.player())
			return;
		
		int menTarget = WARMYD.conscriptable(null).get(f);
		int men = WARMYD.men(null).target().total().get(f);
		
		if (menTarget-men > 100) {
			recruit(f);
			
		}
		planForWar(f);
		
	}
	
	@Override
	public void save(FilePutter file) {
		hasSentMessage.save(file);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		hasSentMessage.load(file);
		super.load(file);
	}
	
	@Override
	public void clear() {
		hasSentMessage.clear();
		super.clear();
	}
	
	private void recruit(Faction f) {
		int menTarget = (int) (WARMYD.conscriptable(null).get(f)*0.75);
		int men = WARMYD.men(null).target().total().get(f);
		int recruits = menTarget-men;
		if (recruits <= 0)
			return;
		
		int armies = CLAMP.i(1 + menTarget/5000, 1, 3);

		while(f.kingdom().armies().all().size() < armies && f.kingdom().armies().canCreate()) {
			Region r = f.kingdom().realm().regions().rnd();
			COORDINATE c = WPathing.random(r);
			f.kingdom().armies().create(c.x(), c.y());
		}
		
		
		recruitTree.clear();
		for (int ai = 0; ai < f.kingdom().armies().all().size(); ai++) {
			WArmy a = f.kingdom().armies().all().get(ai);
			recruitTree.add(a);
		}
		
		while(recruitTree.hasMore()) {
			WArmy a = recruitTree.pollGreatest();
			int target = menTarget;
			if (recruitTree.hasMore()) {
				target *= 0.75;
			}
			
			target = CLAMP.i(target, 0, Config.BATTLE.MEN_PER_ARMY);
			if (target > WARMYD.men(null).target().get(a)) {
				recruit(f, a, target);
			}
			
			menTarget -= WARMYD.men(null).target().get(a);
			
		}

		
	}
	
	private void recruit(Faction f, WArmy a, int target) {
		
		main:
		while(WARMYD.men(null).target().get(a) < target && a.divs().canAdd()) {
			int ri = RND.rInt(RACES.all().size());
			
			for (int i = 0; i < RACES.all().size(); i++) {
				Race r = RACES.all().get((ri+i)%RACES.all().size());
				
				int am = WARMYD.conscriptable(r).get(f)-WARMYD.men(r).target().get(a);
				
				am = CLAMP.i(am, 0, Config.BATTLE.MEN_PER_DIVISION);
				
				
				if (am > 0) {
					
					am = CLAMP.i(am, 20, Config.BATTLE.MEN_PER_DIVISION);
					
					WDivRegional d = World.ARMIES().regional().create(r, (double)am/Config.BATTLE.MEN_PER_DIVISION, 0,0, a);
					d.randomize(((1+f.index())%10)/10.0, 4 + f.index()%12);
					//d.menSet(d.menTarget());
					continue main;
				}
			}
			break;
		}
		
	}
	
	void planForWar(Faction f) {
		
		if (f == null || !f.isActive())
			return;
		
		if (FACTIONS.rel().war.get(f, FACTIONS.player()) == 0) {
			
			for (int ai = 0; ai < f.kingdom().armies().all().size(); ai++) {
				WArmy a = f.kingdom().armies().all().get(ai);
				if (a.state() == WArmyState.fortified || a.state() == WArmyState.fortifying) {
					if (a.region() == null || a.region().faction() != f) {
						allyFaction = f;
						Region r = WPathing.findRegion(a.ctx(), a.cty(), ally);
						if (r == null) {
							a.disband();
							ai--;
						}else {
							COORDINATE c = WPathing.random(r);
							a.setDestination(c.x(), c.y());
						}
					}
				}else if (a.state() == WArmyState.intercepting) {
					a.stop();
				}
			}
			
			return;
			
		}
		
		
		Region c = f.capitolRegion();
		for (WEntity e : World.ENTITIES().fillTiles(c.cx()-4, c.cx()+4, c.cy()-4, c.cy()+4)) {
			
			if (e instanceof WArmy) {
				WArmy a = (WArmy) e;
				if (FACTIONS.rel().enemy(a.faction(), f)) {
					if (a.state() == WArmyState.besieging &&  (a.willConquer(c) || WARMYD.qualityF().get(FACTIONS.player())*0.25 > WARMYD.qualityF().get(f))) {
						if (hasSentMessage.get(f.index()) == 0) {
							hasSentMessage.set(f.index(), 4);
							Str t = Str.TMP;
							t.clear().add(¤¤mess).insert(0, f.appearence().name());
							VIEW.inters().yesNo.activate(t, surrenderYes, ACTION.NOP, false);
							surrenderFaction = f;
							
						}else {
							hasSentMessage.set(f.index(), hasSentMessage.get(f.index())-1);
						}
						break;
					}
				}
			}
		}

		
		armies.clear();
		armies.add(f.kingdom().armies().all());

		if (armies.size() == 0)
			return;
		
		for (int ei = 0; ei < FACTIONS.player().kingdom().armies().all().size() && armies.size() > 0; ei++) {
			WArmy e = FACTIONS.player().kingdom().armies().all().get(ei);
			if (e.region() != null && e.region().faction() == f)
				attackInvador(e);
		}
		
		for (int ai = 0; ai < armies.size(); ai++) {
			WArmy a = armies.get(ai);
			if (doOffensive(a))
				ai--;
		}
		
	}

	
	private boolean doOffensive(WArmy a) {
		
		if (a.region() != null && a.region().faction() == a.faction() && WARMYD.supplies().health(a) < 1) {
			a.stop();
			return false;
		}
		
		if (WARMYD.men(null).get(a) == WARMYD.men(null).target().get(a) && WARMYD.men(null).get(a) > 4000) {
			Region cap = FACTIONS.player().capitolRegion();
			RegPath p = WPathing.regPath(a.ctx(), a.cty(), cap.cx(), cap.cy(), Integer.MAX_VALUE);
			if (p == null)
				return false;
			for (Region r : p) {
				if (r.faction() == FACTIONS.player()) {
					a.besiege(r);
					armies.remove(a);
					return true;
				}
			}
		}
		return false;
	}
	
	private void attackInvador(WArmy e) {
		
		
		
		double bestValue = Integer.MAX_VALUE;
		WArmy best = null;
		
		for (WArmy a : armies) {
			
			RegPath p = WPathing.regPath(e.ctx(), e.cty(), a.ctx(), a.cty(), Integer.MAX_VALUE);
			if (p == null)
				continue;
			
			if (WARMYD.quality().get(a)*0.75 > WARMYD.quality().get(e) && p.distance < bestValue) {
				if (a.region() != null && a.region().faction() == a.faction() && WARMYD.supplies().health(a) < 1) {
					a.stop();
					continue;
				}
				
				best = a;
				bestValue = p.distance;
			}
		}
		
		if (best != null) {
			armies.remove(best);
			best.intercept(e);
			return;
		}
		
		for (WArmy a : armies) {
			if (a.region() != null && a.region().faction() == a.faction() && WARMYD.supplies().health(a) < 1) {
				a.stop();
				continue;
			}
			a.intercept(e);
		}
		armies.clear();
		
	}
	
	private Faction allyFaction;
	private BOOLEAN_OBJECT<Region> ally = new BOOLEAN_OBJECT<Region>() {

		@Override
		public boolean is(Region t) {
			return t.faction() == allyFaction;
		}
		
	};
	
	private Faction surrenderFaction;
	private ACTION surrenderYes = new ACTION() {
		
		@Override
		public void exe() {
			FACTIONS.rel().overlord.set(FACTIONS.player(), surrenderFaction, 1);
		}
	};

	public void init(Faction f) {
		for (int i = 0; i < FACTIONS.MAX; i++)
			update(i, 0);
		
		for (WArmy a : f.kingdom().armies().all()) {
			for (int i = 0; i < a.divs().size(); i++) {
				a.divs().get(i).menSet(a.divs().get(i).menTarget());
			}
			
			for (WArmySupply s : WARMYD.supplies().all) {
				s.current().set(a, s.used().get(a));
			}
			
		}
		
	}
}
