package game.battle;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.time.TIME;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.World;
import world.army.WARMYD;
import world.army.WDIV;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.regions.REGIOND;
import world.map.regions.Region;

class Conflict {

	public static final double retreatPenalty = 0.1;
	
	Side sideA = new Side();
	Side sideB = new Side();
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			sideA.saver.save(file);
			sideB.saver.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			sideA.saver.load(file);
			sideB.saver.load(file);
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	boolean test(WArmy a) {
		
		if (WARMYD.men(null).get(a) == 0)
			return false;
		
		for (WEntity e : World.ENTITIES().fill(a.body())) {
			if (e != a && e instanceof WArmy) {
				if (Util.enemy(a, (WArmy) e)) {
					return true;
				}
			}
		}
		
		if (Util.regionWillAttack(a)) {
			return true;
		}
		
		
		return false;
	}
	
	boolean make(WArmy a) {
		if (WARMYD.men(null).get(a) == 0)
			return false;
		
		for (WEntity e : World.ENTITIES().fill(a.body())) {
			if (e != a && e instanceof WArmy) {
				if (test(a, (WArmy) e)) {
					return true;
				}
			}
		}
		
		if (Util.regionWillAttack(a)) {
			sideA.clear();
			sideA.add(a);
			sideB.clear();
			sideB.ri = a.region().index();
			init(1, 1);
			return true;
		}
		
		return false;
	}
	
	boolean make(Region reg, WArmy a) {
		
		if (!Util.enemy(a.faction(), reg.faction()))
			return false;
		
		if (WARMYD.men(null).get(a) == 0)
			return false;
		
		
		
		
		sideA.clear();
		sideA.add(a);
		
		
		sideB.clear();
		sideB.ri = reg.index();
		
		sideA.mustFight[0] = true;
		sideB.mustFight[0] = true;
		
		LIST<WEntity> es = World.ENTITIES().fillTiles(
				a.ctx()-WArmy.reinforceTiles*2, 
				a.ctx()+WArmy.reinforceTiles*2, 
				a.cty()-WArmy.reinforceTiles*2, 
				a.cty()+WArmy.reinforceTiles*2);
		
		for (WEntity e : es) {
			if (e == a)
				continue;
			if (!(e instanceof WArmy))
				continue;
			
			WArmy a2 = (WArmy) e;
			if (Util.ally(a, a2) && Util.enemy(reg.faction(), a2.faction())) {
				if (Util.reinforces(a, e.ctx(), e.cty()) && sideA.hasRoom()) {
					sideA.add(a2);
					sideA.mustFight[sideA.size()-1] = false;
				}
			}
		}
		
		if (sideB.isPlayer()) {
			Side s = sideA;
			sideA = sideB;
			sideB = s;
		}
		init(1, 1);
		return true;
		
	}
	
	boolean make(WArmy a, Region reg, double besigeTimer) {
		
		if (!Util.enemy(a.faction(), reg.faction()))
			return false;
		
		if (WARMYD.men(null).get(a) == 0)
			return false;
		
		sideA.mustFight[0] = true;
		sideB.mustFight[0] = true;
		
		
		sideA.clear();
		sideA.add(a);
		sideB.clear();
		sideB.ri = reg.index();
		
		double mul = TIME.years().bitSeconds()/(2*besigeTimer+1)-1;
		mul = CLAMP.d(mul, 0, 50);
		
		double mulA = 1;
		double mulB = mul;
		
		if (sideB.isPlayer()) {
			Side s = sideA;
			sideA = sideB;
			sideB = s;
			mulB = 1;
			mulA = mul;
		}
		
		
		
		
		init(mulA, mulB);
		
		return true;
		
	}
	
	private boolean test(WArmy a, WArmy b) {
		if (b.faction() == a.faction())
			return false;
		
		if (!Util.enemy(a, b))
			return false;
		
		if (WARMYD.men(null).get(a) == 0 || WARMYD.men(null).get(b) == 0)
			return false;
		
		sideA.clear();
		sideA.add(a);
		sideB.clear();
		sideB.add(b);
		
		sideA.mustFight[0] = true;
		sideB.mustFight[0] = true;
		
		LIST<WEntity> es = World.ENTITIES().fillTiles(
				a.ctx()-WArmy.reinforceTiles*2, 
				a.ctx()+WArmy.reinforceTiles*2, 
				a.cty()-WArmy.reinforceTiles*2, 
				a.cty()+WArmy.reinforceTiles*2);
		
		for (WEntity e : es) {
			if (e == a || e == b)
				continue;
			if (!(e instanceof WArmy))
				continue;
			
			WArmy a2 = (WArmy) e;
			if (Util.ally(a, a2) && Util.enemy(b, a2)) {
				if (Util.reinforces(a, e.ctx(), e.cty()) && sideA.hasRoom()) {
					sideA.add(a2);
					if (b.body().touches(a2)) {
						sideA.mustFight[sideA.size()-1] = true;
					}
				}
			}else if (Util.ally(b, a2) && Util.enemy(a, a2)) {
				if (Util.reinforces(b, e.ctx(), e.cty()) && sideB.hasRoom()) {
					sideB.add(a2);
					if (a.body().touches(a2)) {
						sideB.mustFight[sideA.size()-1] = true;
					}
				}
			}
		}
		
		Region r = a.region();
		
		if (r != null) {
			if (Util.regionCanAttack(b)) {
				sideA.ri = r.index();
			}else if (Util.regionCanAttack(a)){
				if (sideB.isPlayer()) {
					sideB.ri = r.index();
				}else {
					calc(sideA, 1);
					calc(sideB, 1);
					if (sideA.power < sideB.power + REGIOND.MILITARY().power.get(r))
						sideB.ri = r.index();
				}
				
			}
		}
		
		
		
		if (sideB.isPlayer()) {
			Side s = sideA;
			sideA = sideB;
			sideB = s;
		}
		
		
		
		init(1, 1);
		
		return true;
	}
	
	void randomize() {
		init(1.0 + RND.rExpo()*RND.rSign(), 1.0 + RND.rExpo()*RND.rSign());
	}
	
	private void init(double mulA, double mulB) {
		
		calc(sideA, mulA);
		calc(sideB, mulB);
		sideA.dPower = (1.0+sideB.power) /(1.0+sideA.power); 
		sideB.dPower = (1.0+sideA.power) /(1.0+sideB.power); 

		sideA.victory = sideA.power >= sideB.power;
		sideB.victory = !sideA.victory;
		
		double d = (double) (sideB.power+1.0)/(sideA.power+1.0);
		d = Math.pow(d, 1.2);
		
		sideA.losses =  CLAMP.i((int) (d * sideB.men * (sideA.victory ? 0.05 : 1)), 0, sideA.men);
		sideB.losses =  CLAMP.i((int) ((1.0/d) * sideA.men * (sideB.victory ? 0.05 : 1)), 0, sideB.men);
		
		sideA.retreatLosses =  CLAMP.i((int) (sideB.mustFightMen*retreatPenalty), 0, sideA.men);
		sideB.retreatLosses =  CLAMP.i((int) (sideA.mustFightMen*retreatPenalty), 0, sideB.men);
		
	}
	
	private void calc(Side side, double mul) {
		
		side.men = 0;
		side.power = 0;
		side.mustFightMen = 0;
		side.moraleBase = 1;
		int dd = 0;
		for (WArmy e : side) {
			side.men += WARMYD.men(null).get(e);
			side.power += WARMYD.quality().get(e);
			if (side.mustFight[dd]) {
				side.mustFightMen += WARMYD.men(null).get(e);
			}
			dd++;
			side.moraleBase += WARMYD.supplies().morale(e)* WARMYD.men(null).get(e);
		}
		
		if (side.garrison() != null) {
			for (WDIV d : REGIOND.MILITARY().divisions(side.garrison())) {
				side.men += d.men();
				side.power += d.provess();
			}
			side.moraleBase += REGIOND.MILITARY().soldiers.get(side.garrison());
		}
		side.moraleBase /= side.men;
		side.power*= mul;
	}
	

	
	public boolean isPlayer() {
		return sideA.isPlayer();
	}
	
	public final class Side extends ArrayList<WArmy>{
		
		private static final long serialVersionUID = 1L;

		public boolean victory = false;
		public int losses;
		public int retreatLosses;
		public int men;
		public int power;
		public double dPower;
		private int ri;
		public boolean[] mustFight = new boolean[16];
		public int mustFightMen;
		public double moraleBase;
		
		public Side() {
			super(16);
		}
		
		public boolean isPlayer() {
			for (WArmy a : this)
				if (a.faction() != null && a.faction() == FACTIONS.player())
					return true;
			if (garrison() != null && garrison().faction() == FACTIONS.player())
				return true;
			return false;
		}
		
		public Region garrison() {
			if (ri >= 0)
				return World.REGIONS().getByIndex(ri);
			return null;
		}
		
		@Override
		public void clear() {
			ri = -1;
			Arrays.fill(mustFight, false);
			super.clear();
		}
		
		private final SAVABLE saver = new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.bool(victory);
				file.i(losses);
				file.i(retreatLosses);
				file.i(men);
				file.i(power);
				file.i(ri);
				file.d(dPower);
				for (int i = 0; i < mustFight.length; i++)
					file.bool(mustFight[i]);
				file.i(mustFightMen);
				file.i(size());
				for (WArmy a : Side.this)
					file.i(a == null ? -1 : a.armyIndex());
				file.d(moraleBase);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				Side.this.clear();
				victory = file.bool();
				losses = file.i();
				retreatLosses = file.i();
				men = file.i();
				power = file.i();
				ri = file.i();
				dPower = file.d();
				for (int i = 0; i < mustFight.length; i++)
					mustFight[i] = file.bool();
				mustFightMen = file.i();
				int gi = file.i();
				for (int i = 0; i < gi; i++) {
					int ai = file.i();
					if (ai >= 0)
						add(World.ENTITIES().armies.get(ai));
				}
				moraleBase = file.d();
			}
			
			@Override
			public void clear() {
				// TODO Auto-generated method stub
				
			}
		};
		
	}
	
}
