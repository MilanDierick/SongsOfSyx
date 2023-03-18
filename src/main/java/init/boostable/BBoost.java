package init.boostable;

import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public final class BBoost {

	public final BOOSTABLE boostable;

	public final double start;
	public final double end;
	public final double delta;
	private final boolean isMul;
	
	public BBoost(BOOSTABLE bonus, double value, boolean isMul) {
		this.boostable = bonus;
		this.end = value;
		this.isMul = isMul;
		if (isMul) {
			start = 1.0;
			delta = value-1.0;
		}else {
			start = 0.0;
			delta = value;
		}
	}
	
	public BBoost(BOOSTABLE bonus, double start, double end, boolean isMul) {
		if (end < start) {
			double s = start;
			start = end;
			end = s;
		}
		
		this.start = start;
		this.end = end;
		this.delta = end-start;
		this.boostable = bonus;
		this.isMul = isMul;
	}

	public double value() {
		return end;
	}
	
	public void hover(GUI_BOX text) {
		GBox b = (GBox)text;
		b.add(boostable.icon);
		b.text(boostable.name, 24);
		b.tab(6);
		if (!isMul)
			b.add(GFORMAT.f0(b.text(), end));
		else {
			GText t = b.text();
			t.add('*').s();
			b.add(GFORMAT.f1(b.text(), end));
		}
		b.NL();
			
	}
	
	public void hoverValue(GUI_BOX text, double v) {
		GBox b = (GBox)text;
		b.add(boostable.icon);
		b.text(boostable.name, 24);
		b.tab(6);
		
		double start = isMul() ? 1 : 0;
		double delta = isMul() ? end-1 : end;
		
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
			if (end < 1) {
				t.add(end, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(end, 2);
			}
		}else {
			if (end < 0) {
				t.add(end, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(end, 2);
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
			if (end < 1) {
				t.add(end, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(end, 2);
			}
		}else {
			if (end < 0) {
				t.add(end, 2).s().add('-').s().add(1);
			}else {
				t.add(1).s().add('-').s().add(end, 2);
			}
		}
		b.NL();
		
	}

	public boolean isMul() {
		return isMul;
	}
	

	
}
