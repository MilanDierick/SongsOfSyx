package world.army.util;

import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import init.D;
import init.race.*;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import world.army.AD;
import world.entity.army.WArmy;

public final class Power {
	
	private static CharSequence ¤¤desc = "The overall power of a battle unit. Divided into different attack and defence types. The total power is an indication of how well the unit will perform in a fight, but in practice each type determine the outcome.";
	static {
		D.ts(Power.class);
	}
	private final PowerSpec res = new PowerSpec();
	private final double rangeMax;
	
	private double maxPI = -1.0;
	
	Power(){

		double m = 0;
		for (EquipRange rr : STATS.EQUIP().RANGED()) {
			m = Math.max(m, rangedPower(rr, 1.0, 1.0));
		}
		rangeMax = m;
		
		
		

	}
	
	public double maxRanged() {
		return rangeMax;
	}
	
	private final PowerSpec rRanged = new PowerSpec();
	
	public double rangedPower(EquipRange a, double equip, double dskill) {
		
		double ref = a.ref(equip, dskill);
		rRanged.clear();
		rRanged.addRange(ref, 1, a);
		
		double res = 0;
		for (int i = 0; i < rRanged.ranged.length; i++)
			res += rRanged.ranged[i];
		return res*0.5;
		
		
	}

	private static EquipRange best(DIV_STATS div) {
		double max = 0;
		EquipRange b = null;
		for (EquipRange rr : STATS.EQUIP().RANGED()) {
			if (div.equip(rr) > 0) {
				double m = AD.UTIL().power.rangedPower(rr, div.equip(rr), AD.UTIL().boost(div, rr.boostable)/rr.boostable.max(POP_CL.class));
				if (m > max) {
					max = m;
					b = rr;
				}
			}
				
		}
		return b;
	}
	
	private void init() {
		if (maxPI >= 0)
			return;
		double mm = 0;
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			
			Race r = RACES.all().get(ri);
			DIV_STATS s = new DIV_STATS() {
				
				@Override
				public double training(StatTraining tr) {
					return 1.0;
				}
				
				@Override
				public double equip(EquipBattle e) {
					return 1.0;
				}
				
				@Override
				public Race race() {
					return r;
				}
				
				@Override
				public int men() {
					return 1;
				}
				
				@Override
				public Faction faction() {
					return FACTIONS.player();
				}
				
				@Override
				public double experience() {
					return 1.0;
				}
			};
			res.clear();
			res.add(s);
			mm = Math.max(mm, res.total());
			
			
		}
		maxPI = 99.0/mm;
	}
	
	public double get(DIV_STATS div) {
		init();
		res.clear();
		res.add(div);
		
		return div.men() + res.total()*maxPI;
	}
	
	private Div sDiv;
	private final DIV_STATS dstats = new DIV_STATS() {
		
		@Override
		public double training(StatTraining tr) {
			return tr.div().getD(sDiv);
		}
		
		@Override
		public double equip(EquipBattle e) {
			return e.stat().div().getD(sDiv);
		}
		
		@Override
		public Race race() {
			return sDiv.info.race();
		}
		
		@Override
		public int men() {
			return sDiv.menNrOf();
		}
		
		@Override
		public Faction faction() {
			return sDiv.army().faction();
		}
		
		@Override
		public double experience() {
			return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(sDiv);
		}
	};
	
	public double get(Div div) {
		sDiv = div;
		return get(dstats);
	}
	
	public double get(WArmy a) {
		init();
		res.clear();
		int am = 0;
		for (int di = 0; di < a.divs().size(); di++) {
			res.add(a.divs().get(di));
			am += a.divs().get(di).men();
		}
		return am + res.total()*maxPI;
	}
	
	public void hover(WArmy a, GUI_BOX b) {
		get(a);
		res.hover(b);
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return -1 to 1. -1 a is destroyed, 1 = b is destroyed
	 */
	public double getBalance(DIV_STATS a, DIV_STATS b) {
		return 0;
	}
	
	public static class PowerSpec {
		
		public double totalAttack;
		public double totalDef;
		public double speed;
		public double range;
		public final double[] ranged = new double[BOOSTABLES.BATTLE().DAMAGES.size()+1];
		public final double[] attack = new double[BOOSTABLES.BATTLE().DAMAGES.size()+1];
		public final double[] defence = new double[BOOSTABLES.BATTLE().DAMAGES.size()+1];
		
		public PowerSpec() {
			
		}
		
		public double attack(BDamage p) {
			return attack[p.index()];
		}
		
		public double def(BDamage p) {
			return defence[p.index()];
		}
		
		public void clear() {
			totalAttack = 0;
			totalDef = 0;
			speed = 0;
			range = 0;
			Arrays.fill(ranged, 0);
			Arrays.fill(attack, 0);
			Arrays.fill(defence, 0);
		}
		
		public void add(PowerSpec other) {
			totalAttack += other.totalAttack;
			totalDef += other.totalDef;
			speed += other.speed;
			range += other.range;
			for (int i = 0; i < ranged.length; i++) {
				ranged[i] += other.ranged[i];
				attack[i] += other.attack[i];
				defence[i] += other.defence[i];
				
			}
		}
		
		public void add(DIV_STATS div) {
			
			double mA = div.men();
			double mD = div.men();
			
			mA *= (1.0+AD.UTIL().boost(div, BOOSTABLES.BATTLE().BLUNT_ATTACK))/(1.0+BOOSTABLES.BATTLE().BLUNT_ATTACK.baseValue);
			mD *= (1.0+AD.UTIL().boost(div, BOOSTABLES.BATTLE().BLUNT_DEFENCE))/(1.0+BOOSTABLES.BATTLE().BLUNT_DEFENCE.baseValue);
			
			
			
			mA *= 0.2 + (0.8*AD.UTIL().boost(div, BOOSTABLES.BATTLE().OFFENCE));;
			mD *= 0.2 + (0.8*AD.UTIL().boost(div, BOOSTABLES.BATTLE().DEFENCE));
			
			double mor = Math.sqrt((AD.UTIL().boost(div, BOOSTABLES.BATTLE().MORALE)));
			mA *= mor;
			mD *= mor;
			
			double speed = (1.0 + AD.UTIL().boost(div, BOOSTABLES.PHYSICS().SPEED));
			this.speed += div.men()*speed;
			
			mA *= speed/(1.0+BOOSTABLES.PHYSICS().SPEED.baseValue); 

			double weigh = (1.0+AD.UTIL().boost(div, BOOSTABLES.PHYSICS().MASS))/(1.0+BOOSTABLES.PHYSICS().MASS.baseValue);
			mD += weigh;
			
			double sta = (1.0+AD.UTIL().boost(div, BOOSTABLES.PHYSICS().STAMINA))/(1.0+BOOSTABLES.PHYSICS().STAMINA.baseValue);
			mA *= sta;
			mD *= sta;
			
			
			attack[0] += mA*4;
			defence[0] += mD*4;
			
			for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
				attack[di+1] += mA*AD.UTIL().boost(div, BOOSTABLES.BATTLE().DAMAGES.get(di).attack);
				defence[di+1] += mD*AD.UTIL().boost(div, BOOSTABLES.BATTLE().DAMAGES.get(di).defence);
			}
			EquipRange rr = best(div);
			range = 0;
			if (rr != null) {
				double ref = CLAMP.d(AD.UTIL().boost(div, rr.boostable)/rr.boostable.max(Div.class), 0, 1);
				addRange(ref, div.men(), rr);
			}
			
			for (int i = 0; i < attack.length; i++) {
				totalAttack += (attack[i] + ranged[i]);
				totalDef += defence[i];
			}
			
		
		}
		
		public double total() {
			return (totalAttack + totalDef)*0.5;
		}
		
		public void addRange(double ref, double men, EquipRange rr) {
			
			
			double range = rr.projectile.range(ref)/(C.TILE_SIZE*rr.projectile.reloadSeconds(ref));
			this.range = range;
			
			double mul = rr.projectile.bluntDamage(ref)/(1.0+BOOSTABLES.BATTLE().BLUNT_DEFENCE.baseValue);
			
			mul *=  range;
			mul *= 1 + rr.projectile.areaAttack(ref);
			mul *=  0.2 + 0.8*rr.projectile.accuracy(ref);
			mul *= men;
			
			ranged[0] += mul*4;
			
			for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
				ranged[di+1] += mul*rr.projectile.damage(di, ref);
			}
		}
		
		public void hover(GUI_BOX box) {
			GBox b = (GBox) box;
			b.textLL(DicArmy.¤¤Power);
			b.NL();
			b.text(¤¤desc);
			
			b.NL(0);
			b.textL(DicMisc.¤¤Total);
			b.tab(6);
			b.add(GFORMAT.f(b.text(), total()*AD.UTIL().power.maxPI, 1));
			
			b.sep();
			
			b.tab(6);
			b.add(UI.icons().s.sword);
			b.tab(9);
			b.add(UI.icons().s.armour);
			b.NL(4);
			
			for (BDamage pp : BOOSTABLES.BATTLE().DAMAGES) {
				b.add(pp.attack.icon);
				b.text(pp.name);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), attack(pp)*AD.UTIL().power.maxPI, 1));
				b.tab(9);
				b.add(GFORMAT.f(b.text(), def(pp)*AD.UTIL().power.maxPI, 1));
				b.NL();
			}
			
			b.textL(DicMisc.¤¤Total);
			
			b.tab(6);
			b.add(GFORMAT.f(b.text(), totalAttack*AD.UTIL().power.maxPI, 1));
			b.tab(9);
			b.add(GFORMAT.f(b.text(), totalDef*AD.UTIL().power.maxPI, 1));
			b.NL();
		}

		
	}
	
	public static class BoI implements INDEXED {
		
		public final Boostable bo;
		public final int bIndex;
		
		BoI(Boostable bo, int index){
			this.bo = bo;
			this.bIndex = index;
		}

		@Override
		public int index() {
			return bo.index();
		}
	}
	
}
