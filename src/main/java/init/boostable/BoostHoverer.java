package init.boostable;

import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class BoostHoverer {
	
	private BoostHoverer() {

	}


	
	public static void hoverMultiplier(GUI_BOX text, CharSequence name, double value, double min, double max) {
		GBox b = (GBox)text;
		b.text(name);
		b.tab(6);
		b.add(GFORMAT.f1(b.text(), value));
		b.tab(9);
		GText t = b.text();
		GFORMAT.f(t, min);
		t.s().add('-').s();
		GFORMAT.f(t, max);
		t.normalify2();
		b.add(t);
		
	}
	
	public static void hoverAddative(GUI_BOX text, CharSequence name, double value, double min, double max) {
		GBox b = (GBox)text;
		b.text(name);
		b.tab(6);
		b.add(GFORMAT.f0(b.text(), value));
		b.tab(9);
		GText t = b.text();
		GFORMAT.f(t, min);
		t.s().add('-').s();
		GFORMAT.f(t, max);
		t.normalify2();
		b.add(t);
		
	}
	
//	public static void hover(BOOSTABLE boo, GUI_BOX text, Induvidual indu) {
//		
//		GBox b = (GBox) text;
//		
//		if (SETT.ENV().climate().bonus(boo) != 1 || indu.race().bonus().mul(boo) != 1 || indu.faction().bonus().muls().size() > 0 || STATS.BOOST().muls(boo).size() > 0) {
//			b.textLL(DicMisc.¤¤Multipliers);
//			b.NL();
//			if (indu.race().bonus().mul(boo) != 1) {
//				BoostHoverer.hoverMultiplier(text, RACES.name(), indu.race().bonus().mul(boo), RACES.bonus().minMul(boo), RACES.bonus().maxMul(boo));
//				b.NL();
//			}
//			if (SETT.ENV().climate().bonus(boo) != 1) {
//				BoostHoverer.hoverMultiplier(text, CLIMATES.INFO().name, SETT.ENV().climate().bonus(boo), SETT.ENV().climate().bonus(boo), SETT.ENV().climate().bonus(boo));
//				b.NL();
//			}
//			
//			for (BOOSTER_OLD bb : indu.faction().bonus().muls()) {
//				BoostHoverer.hoverMultiplier(text, bb.name(), bb.value(boo), bb.min(boo), bb.max(boo));
//				b.NL();
//			}
//			for (StatBooster bb : STATS.BOOST().muls(boo)) {
//				BoostHoverer.hoverMultiplier(text, bb.name(), bb.value(indu), bb.min(), bb.max());
//				b.NL();
//			}
//			b.NL(4);
//		}
//		
//		if (indu.race().bonus().add(boo) != 0 || indu.faction().bonus().adds().size() > 0 || STATS.BOOST().adders(boo).size() > 0) {
//			b.textLL(DicMisc.¤¤Addative);
//			b.NL();
//			if (indu.race().bonus().add(boo) != 0) {
//				BoostHoverer.hoverMultiplier(text, RACES.name(), indu.race().bonus().add(boo), RACES.bonus().minAdd(boo), RACES.bonus().maxAdd(boo));
//				b.NL();
//			}
//			for (BOOSTER_OLD bb : indu.faction().bonus().adds()) {
//				BoostHoverer.hoverAddative(text, bb.name(), bb.value(boo), bb.min(boo), bb.max(boo));
//				b.NL();
//			}
//			for (StatBooster bb : STATS.BOOST().adders(boo)) {
//				BoostHoverer.hoverAddative(text, bb.name(), bb.value(indu), bb.min(), bb.max());
//				b.NL();
//			}
//			b.NL(4);
//			b.textL(DicMisc.¤¤Total);
//			b.tab(6);
//			b.add(GFORMAT.f1(b.text(), boo.get(indu), boo.max(indu)));
//			b.tab(9);
//			b.add(GFORMAT.f(b.text(), boo.max(indu)));
//		}
//		
//	}
	

	
}
