package settlement.room.industry.module;

import java.util.Arrays;

import init.boostable.*;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.LIST;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class IndustryUtil {

	private IndustryUtil() {
		
	}
	
	public static double calcConsumptionRate(double baseRate, IndustryRate rate, RoomInstance ins) {
		return baseRate;
	}
	
	public static double roomBonus(RoomInstance ins, IndustryRate rate) {
		
		double r = 1;
		r *= 0.25 + 0.75-0.75*(ins).getDegrade();
		r *= ins.blueprintI().upgrades().boost(ins.upgrade());
		if (rate != null)
			for (RoomBoost b : rate.boosts())
				r *= b.get(ins);
		
		return r;
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(base, h, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, BOOSTABLE bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		r*= base*bonus.get(h);
		
		return r;
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(baseRate, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, BOOSTABLE bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		
		
		double am = 0;
		double mul = 0;
		
		for (Humanoid a : ins.employees().employees()) {
			mul += bonus.get(a);
			am++;
		}
		
		if (am > 0) {
			mul /= am;
		}else {
			mul = 1;
		}
		return r*mul*baseRate;
	}
	
	private static double[] values = new double[100];
	
	public static void hoverProductionRate(GUI_BOX text, double baseRate, IndustryRate rate, RoomInstance ins) {
		hoverProductionRate(text, baseRate, rate, rate.bonus(), ins);	
	}
	
	public static void hoverProductionRate(GUI_BOX text, double baseRate, IndustryRate rate, BOOSTABLE bonus, RoomInstance ins) {
		GBox b = (GBox) text;
		

		b.NL(4);
		
		b.textLL(DicMisc.¤¤Base);
		b.NL();
		b.text(DicMisc.¤¤Rate);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), baseRate));
		b.NL();
		
		b.text(DicMisc.¤¤Employees);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), ins.employees().employed()));
		b.NL();
		
		b.text(DicMisc.¤¤Workload);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), ins.employees().efficiency()));
		b.NL();
		
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), baseRate*ins.employees().employed()*ins.employees().efficiency(), baseRate*ins.employees().employed()));
		b.NL(8);
		
		hoverBoosts(text, baseRate*ins.employees().employed()*ins.employees().efficiency(), rate, bonus, ins);
		
		b.NL();
		
	}
	
	public static void hoverBoosts(GUI_BOX text, double baseRate, IndustryRate rate, BOOSTABLE bonus, RoomInstance ins) {
		GBox b = (GBox) text;

		
		double mul = (1.0-0.75*ins.getDegrade());
		
		LIST<BBooster> allBoosts = BOOSTABLES.player().muls(bonus);
		{
			Arrays.fill(values, 0);
			double am = 0;
			
			for (Humanoid a : ins.employees().employees()) {
				int i = 0;
				am++;
				for (BBooster boost : allBoosts) {
					values[i] += boost.value(a.indu());
					i++;
				}
			}
			if (am > 0) {
				for (int i = 0; i <= allBoosts.size(); i++) {
					values[i]/=am;
				}
			}
			
		}
		
		
		{
			b.textLL(DicMisc.¤¤Multipliers);
			b.NL();
			
			if (ins.degrader(ins.mX(), ins.mY()) != null) {
				BoostHoverer.hoverMultiplier(text, DicMisc.¤¤Degrade, mul, 0.25, 1);
				b.NL();
			}
	
			if (ins.blueprintI().upgrades().max() > 0) {
				BoostHoverer.hoverMultiplier(text, DicMisc.¤¤Upgrade, ins.blueprintI().upgrades().boost(ins.upgrade()), 1, ins.blueprintI().upgrades().boost(ins.blueprintI().upgrades().max()));
				mul *= ins.blueprintI().upgrades().boost(ins.upgrade());
				b.NL();
			}
			
			
			if (rate != null) {
				for (RoomBoost bo : rate.boosts()) {
					double bb = bo.get(ins);
					mul *= bb;
					BoostHoverer.hoverMultiplier(b, bo.info().name, bb, bo.min(), bo.max());
					b.NL();
				}
			}
			
			for (int i = 0; i < allBoosts.size(); i++) {
				mul *= values[i];
				BBooster bb = allBoosts.get(i);
				BoostHoverer.hoverMultiplier(b, bb.name(), values[i], bb.boost.start, bb.boost.end);
				b.NL();
			}
			
			b.NL(4);
			b.tab(6);
			b.add(GFORMAT.f1(b.text(), mul));
			
			b.NL(8);

		}
		
		allBoosts = BOOSTABLES.player().adders(bonus);
		{
			Arrays.fill(values, 0);
			double am = 0;
			
			for (ENTITY e : ins.employees().employees()) {
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					if (STATS.WORK().EMPLOYED.get(a) == ins) {
						int i = 0;
						am++;
						for (BBooster boost : allBoosts) {
							values[i] += boost.value(a.indu());
							i++;
						}
					}
					
				}
			}
			if (am > 0) {
				for (int i = 0; i <= allBoosts.size(); i++) {
					values[i]/=am;
				}
			}
			
		}
		
		double add = 0;
		{
			b.textLL(DicMisc.¤¤Addative);
			b.NL();
			
			
			for (int i = 0; i < allBoosts.size(); i++) {
				add += values[i];
				BBooster bb = allBoosts.get(i);
				BoostHoverer.hoverAddative(b, bb.name(), values[i], bb.boost.start, bb.boost.end);
				b.NL();
			}
			
			
			b.NL(4);
			
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), add));

			
			
		}
		

		b.NL(16);
		b.textLL(DicMisc.¤¤Total);
		b.tab(6);
		GText t = b.text();
		if (baseRate != 1) {
			GFORMAT.f1(t, baseRate);
			t.s().add('*').s();
		}
		
		GFORMAT.f1(t, mul);
		t.s().add('*').s();
		t.add('(').add('1').s().add('+').s();
		GFORMAT.f1(t, add).add(')').s().add('=').s();
		double tott = calcProductionRate(baseRate, rate, bonus, ins);
		GFORMAT.f0(t, tott, baseRate);
		b.add(t);
				
	}
	
	public static void hoverConsumptionRate(GUI_BOX text, double baseRate, IndustryRate rate, RoomInstance ins) {
		GBox b = (GBox) text;

		b.NL(4);
		
		b.textLL(DicMisc.¤¤Base);
		b.NL();
		b.text(DicMisc.¤¤Rate);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), -baseRate));
		b.NL();
		
		b.text(DicMisc.¤¤Employees);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), ins.employees().employed()));
		b.NL();
		
		b.text(DicMisc.¤¤Workload);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), ins.employees().efficiency()));
		b.NL();
		
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), baseRate*ins.employees().employed()*ins.employees().efficiency(), baseRate*ins.employees().employed()));
		b.NL(8);
		
	}
	
}
