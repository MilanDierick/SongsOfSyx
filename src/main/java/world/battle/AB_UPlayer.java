package world.battle;

import game.battle.BattleState;
import game.battle.PlayerBattleSpec;
import init.config.Config;
import init.race.RACES;
import settlement.army.Div;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import world.battle.spec.WBattleResult.RTYPE;

final class AB_UPlayer {

	private Induvidual[][] divs = new Induvidual[Config.BATTLE.DIVISIONS_PER_ARMY][];
	private int[] enemyLosses = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
	private int[] enemyCaptured = new int[RACES.all().size()];
	private RTYPE result;
	public final Side player;
	public final Side enemy;
	private final AC_Resolver resolver;
	
	AB_UPlayer(Side player, Side enemy, AC_Resolver resolver){
		this.player = player.copy();
		this.enemy = enemy.copy();
		this.resolver = resolver;
		
		new BattleState(new Spec(player, enemy));
	
//		this.player.debug();
//		this.enemy.debug();
		
		
	}
	
	
	public void init(boolean timer, boolean retreat){
		if (retreat)
			result = RTYPE.RETREAT;
		else
			result = (timer || SETT.ARMIES().player().men() == 0) ? RTYPE.DEFEAT : RTYPE.VICTORY; 
		
		
		int[] count = new int[Config.BATTLE.DIVISIONS_PER_ARMY];
		
		if (retreat || timer) {
			int losses = (int) Math.ceil(SETT.ARMIES().enemy().men()*WBattles.retreatPenalty);
			double dlosses = (double)losses/SETT.ARMIES().player().men();
			for (Div d : SETT.ARMIES().player().divisions()) {
				int am = (int) (STATS.BATTLE().DIV.stat().div().get(d)*dlosses);
				if (d.settings.isFighting())
					am += (STATS.BATTLE().DIV.stat().div().get(d)*0.75);
				am = CLAMP.i(am, 0, STATS.BATTLE().DIV.stat().div().get(d));
				count[d.index()] = am;
			}
			for (Div d : SETT.ARMIES().player().divisions()) {
				divs[d.index()] = new Induvidual[STATS.BATTLE().DIV.stat().div().get(d) - count[d.index()]];
				count[d.index()] = 0;
			}
		}else {
			for (Div d : SETT.ARMIES().player().divisions()) {
				divs[d.index()] = new Induvidual[STATS.BATTLE().DIV.stat().div().get(d)];
			}
		}
		
		
		
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				Div d = STATS.BATTLE().DIV.get(h);
				if (d == null) {
					if (h.indu().hType() == HTYPE.ENEMY)
						enemyCaptured[h.race().index]++;
				
				} else {
					
					if (d.index() >= Config.BATTLE.DIVISIONS_PER_ARMY) {
						
						enemyLosses[d.index() - Config.BATTLE.DIVISIONS_PER_ARMY] ++;
					}else {
						if (count[d.index()] >= divs[d.index()].length) {
							
						}else {
							divs[d.index()][count[d.index()]++] = h.indu();
						}
						
					}
				}
			}
		}

		for (int i = 0; i < enemy.divs(); i++) {
			enemyLosses[i] =  enemy.div(i).men() - enemyLosses[i];
		}
	}
	
	void apply() {
		resolver.manualBattle(player, divs, enemy, enemyLosses, enemyCaptured, result);
	}

	private class Spec extends PlayerBattleSpec{
		
		Spec(Side player, Side enemy){
			
			set(player, this.player);
			set(enemy, this.enemy);
		}

		private void set(Side side, SpecSide spec) {
			spec.wCoo.set(side.coo);
			spec.artillery = side.artillery; 
			spec.moraleBase = side.morale();
			
			for (int di = 0; di < side.divs(); di++) {
				spec.divs.add(side.div(di).generate());
			}
			
		}
		
		@Override
		public void conclude(boolean timer, boolean retreat) {
			init(timer, retreat);
			
			
		}

		@Override
		public void finish() {
			apply();
		}
		

	}
	
}
