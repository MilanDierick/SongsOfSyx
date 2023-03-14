package init.boostable;

import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.gui.GUI_BOX;
import util.dic.DicMisc;
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
		b.tab(8);
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
		b.tab(8);
		GText t = b.text();
		GFORMAT.f(t, min);
		t.s().add('-').s();
		GFORMAT.f(t, max);
		t.normalify2();
		b.add(t);
		
	}

	public static void hover(GUI_BOX text, BOOSTABLE boost, HCLASS cl, Race r) {
		
		GBox b = (GBox) text;
		
		if (BOOSTABLES.player().muls(boost).size() > 0) {
			
			b.textLL(DicMisc.¤¤Multipliers);
			b.NL();
			for (BBooster bb : BOOSTABLES.player().muls(boost)) {
				hoverMultiplier(text, bb.name(), bb.value(cl, r), bb.boost.start, bb.boost.end);
				b.NL();
			}
		}
		
		b.NL(4);
		
		b.textLL(DicMisc.¤¤Addative);
		b.NL();
		b.text(DicMisc.¤¤Base);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), boost.defAdd));
		b.NL();
		for (BBooster bb : BOOSTABLES.player().adders(boost)) {
			hoverAddative(text, bb.name(), bb.value(cl, r), bb.boost.start, bb.boost.end);
			b.NL();
		}

		b.NL(12);
		
		b.textLL(DicMisc.¤¤Total);
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), boost.get(cl, r), boost.defAdd));
		b.tab(8);
		GText t = b.text();
		GFORMAT.f(t, BOOSTABLES.player().min(boost));
		t.s().add('-').s();
		GFORMAT.f(t, BOOSTABLES.player().max(boost));
		t.normalify2();
		b.add(t);
		
	}
	
	public static void hover(GUI_BOX text, BOOSTABLE boost, Induvidual i) {
		
		GBox b = (GBox) text;
		
		
		
		
		BBoosters bbb = i.player() ? BOOSTABLES.player() : BOOSTABLES.enemy();
		
		if (BOOSTABLES.player().muls(boost).size() > 0) {
			b.textLL(DicMisc.¤¤Multipliers);
			b.NL();
			for (BBooster bb : bbb.muls(boost)) {
				if (!bb.boost.isMul()) {
					break;
				}
				
				hoverMultiplier(text, bb.name(), bb.value(i), bb.boost.start, bb.boost.end);
				b.NL();
			}
		}
		
		
		b.NL(4);
		
		b.textLL(DicMisc.¤¤Addative);
		b.NL();
		b.text(DicMisc.¤¤Base);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), boost.defAdd));
		b.NL();
		for (BBooster bb : BOOSTABLES.player().adders(boost)) {
			hoverAddative(text, bb.name(), bb.value(i), bb.boost.start, bb.boost.end);
			b.NL();
		}

		b.NL(12);
		
		b.textLL(DicMisc.¤¤Total);
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), boost.get(i), boost.defAdd));
		b.tab(8);
		GText t = b.text();
		GFORMAT.f(t, bbb.min(boost));
		t.s().add('-').s();
		GFORMAT.f(t, bbb.max(boost));
		t.normalify2();
		b.add(t);
		
	}
	
	public static void hover(GUI_BOX text, BOOSTABLE boost, Div i) {
		
		GBox b = (GBox) text;
		

		
		BBoosters bbb = i.army() == SETT.ARMIES().player() ? BOOSTABLES.player() : BOOSTABLES.enemy();
		if (bbb.muls(boost).size() > 0) {
			b.textLL(DicMisc.¤¤Multipliers);
			b.NL();
			for (BBooster bb : bbb.muls(boost)) {
				if (!bb.boost.isMul()) {
					break;
				}
				
				hoverMultiplier(text, bb.name(), bb.value(i), bb.boost.start, bb.boost.end);
				b.NL();
			}
		}
		
		b.NL(4);
		
		b.textLL(DicMisc.¤¤Addative);
		b.NL();
		b.text(DicMisc.¤¤Base);
		b.tab(6);
		b.add(GFORMAT.f(b.text(), boost.defAdd));
		b.NL();
		for (BBooster bb : BOOSTABLES.player().adders(boost)) {
			hoverAddative(text, bb.name(), bb.value(i), bb.boost.start, bb.boost.end);
			b.NL();
		}

		b.NL(12);
		
		b.textLL(DicMisc.¤¤Total);
		b.tab(6);
		b.add(GFORMAT.fRel(b.text(), boost.get(i), boost.defAdd));
		b.tab(8);
		GText t = b.text();
		GFORMAT.f(t, bbb.min(boost));
		t.s().add('-').s();
		GFORMAT.f(t, bbb.max(boost));
		t.normalify2();
		b.add(t);
		
	}

	
	
}
