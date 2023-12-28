package world.army.util;

import game.GAME;
import game.boosting.*;
import init.race.RACES;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.MapIndexed;


final class Boosts {

	private final MapIndexed<Entry> map = new MapIndexed<>();
	
	public double get(DIV_STATS div, Boostable bo) {
		if (!map.contains(bo.index()))
			map.add(new Entry(bo));
		
		Entry e = map.get(bo.index());
		return e.get(div);
		
	}
	
	private class Entry implements INDEXED{
		
		
		private int checkI = GAME.updateI()-1201;
		private final Boostable bo;
		final TmpBoost badd = new TmpBoost();
		final TmpBoost bmul = new TmpBoost();
		
		private double add = 0;
		private double sub = 0;
		private double mul = 0;
		
		Entry(Boostable bo){
			this.bo = bo;
		}
		
		double get(DIV_STATS div) {
			
			if (Math.abs(checkI-GAME.updateI()) > 1200) {
				checkI = GAME.updateI();
				badd.set(bo, false);
				bmul.set(bo, true);
			}
			
			add = bo.baseValue;
			sub = 0;
			mul = 1;

			
			
			for (BoostSpec ss : bo.muls()) {
				mul *= ss.get(div.race());
			}
			
			for (BoostSpec ss : bo.adds()) {
				add(ss.get(div.race()));
			}
			
			if (div.faction() != null) {
				for (BoostSpec ss : bo.muls()) {
					mul *= ss.get(div.faction());
				}
				
				for (BoostSpec ss : bo.adds()) {
					add(ss.get(div.faction()));
				}
			}
			
			add(div.experience(), badd.experience, bmul.experience);
			
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				add(div.training(STATS.BATTLE().TRAINING_ALL.get(i)), badd.training[i], bmul.training[i]);
			}
			
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				
				add(div.equip(STATS.EQUIP().BATTLE_ALL().get(i))/STATS.EQUIP().BATTLE_ALL().get(i).max(), badd.equip[i], bmul.equip[i]);
			}

			
			return  CLAMP.d(mul * add + sub, 0, Double.MAX_VALUE);
		}
		
		void add(double v, double a, double m) {
			a *= v;
			m = 1+(m-1)*v;
			add(a);
			mul *= m;
			
		}
		
		void add(double a) {
			if (a < 0)
				sub += a;
			else
				add += a;
		}

		@Override
		public int index() {
			return bo.index();
		}
		
		
	}
	
	private final class TmpBoost {
		
		
		private double[] training = new double[STATS.BATTLE().TRAINING_ALL.size()];
		private double[] equip = new double[STATS.EQUIP().all().size()];
		private double[] race = new double[RACES.all().size()];
		private double experience;
		
		void set(Boostable bo, boolean isMul) {
		
			
			experience = get(STATS.BATTLE().COMBAT_EXPERIENCE.boosters, bo, isMul);
			for (int ri = 0; ri < RACES.all().size(); ri++)
				race[ri] = get(RACES.all().get(ri).boosts, bo, isMul);
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				StatTraining t = STATS.BATTLE().TRAINING_ALL.get(i);
				training[i] = get(t.boosters, bo, isMul);
			}
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				EquipBattle t = STATS.EQUIP().BATTLE_ALL().get(i);
				equip[i] = get(t.stat().boosters, bo, isMul);
			}
		}
		
		private double get(BoostSpecs bos, Boostable bo, boolean isMul) {
			if (isMul) {
				double res = 1;
				for (int si = 0; si < bos.all().size(); si++) {
					BoostSpec s = bos.all().get(si);
					if (s.booster.isMul == isMul && s.boostable == bo) {
						res *= s.booster.to();
						
					}
				}
				return res;
			}else {
				double res = 0;
				for (int si = 0; si < bos.all().size(); si++) {
					BoostSpec s = bos.all().get(si);
					if (s.booster.isMul == isMul && s.boostable == bo) {
						res += s.booster.to();
						
					}
				}
				return res;
			}
			
		}

		
	}
	
}
