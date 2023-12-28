package settlement.room.industry.module;

import java.util.Arrays;

import game.boosting.*;
import init.race.RACES;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import settlement.entity.humanoid.Humanoid;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class IndustryUtil {

	private IndustryUtil() {
		
	}
	
	public static double calcConsumptionRate(double base, Humanoid h, IndustryRate rate, RoomInstance ins, RESOURCE res) {
		return calcProductionRate(base, h, rate, rate.bonus(), ins)/res.conBoost(h.indu());
	}
	
	public static double calcConsumptionRate(double base, IndustryRate rate, RoomInstance ins) {
		return calcProductionRate(base, rate, rate.bonus(), ins);
	}
	
	public static double roomBonus(RoomInstance ins, IndustryRate rate) {
		
		double r = 1;
		r *= 0.25 + 0.75-0.75*(ins).getDegrade();
		if (rate != null)
			for (RoomBoost b : rate.boosts())
				r *= b.get(ins);
		
		return r;
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(base, h, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double base, Humanoid h, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		r*= base;
		if (bonus != null)
			r*= bonus.get(h.indu());
		return r;
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, RoomInstance ins) {
		
		return calcProductionRate(baseRate, rate, rate.bonus(), ins);
	}
	
	public static double calcProductionRate(double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		
		double r = roomBonus(ins, rate);
		
		
		double am = 0;
		double mul = 0;
		
		if (bonus != null) {
			for (Humanoid a : ins.employees().employees()) {
				mul += bonus.get(a.indu());
				am++;
			}
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
	
	public static void hoverProductionRate(GUI_BOX text, double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins) {
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
	
	public static void hoverBoosts(GUI_BOX text, double baseRate, IndustryRate rate, Boostable bonus, RoomInstance ins) {
		GBox b = (GBox) text;
		
		double mul = 1.0;
		double add = 0;
		
		int tot = 0;
		Arrays.fill(values, 0);
		for (Humanoid a : ins.employees().employees()) {

			tot ++;
			
			int vi = 0;
			
			if (STATS.WORK().EMPLOYED.get(a) == ins) {
				for (BoostSpec s : bonus.adds()) {
					values[vi] += s.booster.get(s.boostable, a.indu());
					vi++;
				}
				for (BoostSpec s : bonus.muls()) {
					values[vi] += s.booster.get(s.boostable, a.indu())-1.0;
					vi++;
				}
			}
		}
		
		if (tot > 0) {
			int vi = 0;
			
			for (int i = 0; i < bonus.adds().size(); i++) {
				values[vi]/=tot;
				add += values[vi];
				vi++;
			}
			
			for (int i = 0; i < bonus.muls().size(); i++) {
				values[vi]/=tot;
				values[vi] += 1;
				mul *= values[vi];
				vi++;
			}
		}
		
		
		{
			b.textLL(DicMisc.¤¤Multipliers);
			b.NL();
			
			if (ins.degrader(ins.mX(), ins.mY()) != null) {
				
				double v = (1.0-0.75*ins.getDegrade());
				Booster.hover(b, UI.icons().s.degrade, DicMisc.¤¤Degrade, v, 0.25, 1.0, true);
				mul *= v;
				b.NL();
			}
			
			
			if (rate != null) {
				for (RoomBoost bo : rate.boosts()) {
					double bb = bo.get(ins);
					mul *= bb;
					Booster.hover(b, UI.icons().s.chevron(DIR.W), bo.info().name, bb, bo.min(), bo.max(), true);
					b.NL();
				}
			}
			
			int vi = bonus.adds().size();
			
			for (BoostSpec s : bonus.muls()) {
				Booster bb = s.booster;
				Booster.hover(b, bb.info.icon, bb.info.name, values[vi], bb.from(), bb.to(), true);
				vi++;
				b.NL();
			}
			
			b.NL(4);
			b.tab(6);
			b.add(GFORMAT.f1(b.text(), mul));
			
			b.NL(8);

		}

		{
			b.textLL(DicMisc.¤¤Addative);
			b.NL();
			
			int vi = 0;
			
			for (BoostSpec s : bonus.adds()) {
				Booster bb = s.booster;
				Booster.hover(b, bb.info.icon, bb.info.name, values[vi], bb.from(), bb.to(), false);
				vi++;
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
	
	public static void hoverConsumptionRate(GUI_BOX text, double baseRate, IndustryRate rate, RoomInstance ins, RESOURCE res) {
		GBox b = (GBox) text;

		b.NL(4);
		b.textL(DicMisc.¤¤Base);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), -baseRate));
		b.NL();
		
		double rr = calcConsumptionRate(1, rate, ins);
		
		b.NL(4);
		b.textL(DicMisc.¤¤Efficiency);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), -rr));
		b.NL();
		
		b.NL();
		b.textL(DicMisc.¤¤Boosts);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), res.conBoost(RACES.clP())));
		b.NL();
		
		b.NL();
		b.textLL(DicMisc.¤¤Total);
		b.tab(6);
		b.add(b.text().add('(').add(baseRate).s().add('*').s().add(rr).add(')').s().add('/').s().add(res.conBoost(RACES.clP())).s().add('=').s().add(baseRate*rr/res.conBoost(RACES.clP())));
		b.NL();
		
//		b.tab(6);
//		b.add(GFORMAT.fRel(b.text(), baseRate*ins.employees().employed()*ins.employees().efficiency(), baseRate*ins.employees().employed()));
		b.NL(8);
		
	}
	
}
