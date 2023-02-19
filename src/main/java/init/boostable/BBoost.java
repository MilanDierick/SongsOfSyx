package init.boostable;

import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class BBoost {

	public final BOOSTABLE boost;
	private final double value;
	private final boolean isMul;
	
//	public BBoost(BOOSTABLE bonus, double boost) {
//		this(bonus, boost, false);
//	}
	
	public BBoost(BOOSTABLE bonus, double value, boolean isMul) {
		this.boost = bonus;
		this.value = value;
		this.isMul = isMul;
	}

	public double value() {
		return value;
	}
	
	public void hover(GUI_BOX text) {
		GBox b = (GBox)text;
		b.add(boost.icon);
		b.text(boost.name, 24);
		b.tab(6);
		if (!isMul)
			b.add(GFORMAT.f0(b.text(), value));
		else {
			GText t = b.text();
			t.add('*').s();
			b.add(GFORMAT.f1(b.text(), value));
		}
		b.NL();
			
	}
	
	public void hoverValue(GUI_BOX text, double v) {
		GBox b = (GBox)text;
		b.add(boost.icon);
		b.text(boost.name, 24);
		b.tab(6);
		
		double start = isMul() ? 1 : 0;
		double delta = isMul() ? value-1 : value;
		
		if (!isMul)
			b.add(GFORMAT.f0(b.text(), start+delta*v));
		else {
			GText t = b.text();
			t.add('*').s();
			b.add(GFORMAT.f1(b.text(), start+delta*v));
		}
		b.tab(9);
		GText t = b.text();
		if (isMul) {
			if (value < 1) {
				t.add(value, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(value, 2);
			}
		}else {
			if (value < 0) {
				t.add(value, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(value, 2);
			}
		}
		b.NL();
		
	}
	
	public void hoverValue(GUI_BOX text, CharSequence title, double v) {
		GBox b = (GBox)text;
		b.tab(6);
		
		if (!isMul)
			b.add(GFORMAT.f0(b.text(), v));
		else {
			GText t = b.text();
			t.add('*').s();
			b.add(GFORMAT.f1(b.text(), v));
		}
		b.tab(9);
		GText t = b.text();
		if (isMul) {
			if (value < 1) {
				t.add(value, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(value, 2);
			}
		}else {
			if (value < 0) {
				t.add(value, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(value, 2);
			}
		}
		b.NL();
		
	}
	
//	
//	public static void hoverTitle(GUI_BOX text) {
//		GBox b = (GBox)text;
//		b.textLL(BOOSTABLES.INFO().name);
//		b.tab(6);
//		b.textL(DicMisc.¤¤Current);
//		b.tab(9);
//		b.textL(DicMisc.¤¤Max);
//	}
	


	public boolean isMul() {
		return isMul;
	}

//	public void hover(GUI_BOX box, LIST<BBoost> boosts) {
//		GBox b = (GBox) box;
//		b.add(bb.boost.icon);
//		b.text(bb.boost.name);
//		b.tab(5);
//		GText t = b.text();
//		if (bb.isMul()) {
//			t.add('*');
//			b.add(GFORMAT.f1(b.text(), bb.value()));
//		}else {
//			b.add(GFORMAT.f0(b.text(), bb.value()));
//		}
//		b.NL();
//		
//	}
	

	
}
