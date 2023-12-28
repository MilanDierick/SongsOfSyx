package game.values;

import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public abstract class Locker<T> {

	public final CharSequence name;
	public final SPRITE icon;
	
	public Locker(CharSequence name, SPRITE icon) {
		this.name = name;
		this.icon = icon;
	}
	
	public abstract boolean inUnlocked(T t);
	
	public double progress(T t) {
		return inUnlocked(t) ? 1 : 0;
	}
	
	public void hover(GUI_BOX text, T t) {
		GBox b = (GBox) text;
		b.add(icon);
		if (inUnlocked(t)) {
			b.add(b.text().normalify2().add(name));
		}else {
			b.add(b.text().warnify().add(name));
		}
		b.NL();
	}
	
	final static class LockerValue<T> extends Locker<T>{

		public final Value<T> getter;
		public final COMPARATOR comp;
		public final double value;
		
		LockerValue(COMPARATOR comp, Value<T> getter, double value, SPRITE icon){
			super(getter.name, icon);
			this.getter = getter;
			this.comp = comp;
			this.value = value;
		}
		
		@Override
		public boolean inUnlocked(T t) {
			return comp.passes(getter.d.getD(t), value);
		}
		
		@Override
		public void hover(GUI_BOX text, T t) {
			GBox b = (GBox) text;
			GText na = b.text();
			na.add(name);
			GText va = b.text();
			GText cu = b.text();
			cu.add('(');
			
			if (getter.isBool) {
				GFORMAT.bool(va, value == 1);
				GFORMAT.bool(cu, getter.d.getD(t) == 1);
			}else if (getter.percentage) {
				GFORMAT.perc(va, value);
				GFORMAT.perc(cu, getter.d.getD(t));
			}else {
				if ((int) value == value) {
					GFORMAT.i(va, (int)value);
				}else {
					GFORMAT.f(va, value);
				}
				if ((int) getter.d.getD(t) == getter.d.getD(t)) {
					GFORMAT.i(cu, (int)getter.d.getD(t));
				}else {
					GFORMAT.f(cu, getter.d.getD(t));
				}
			}
			
			
				
			GText co = b.text();
			co.add(comp.rep);
			
			if (inUnlocked(t)) {
				na.normalify2();
				va.normalify2();
				co.normalify2();
			}else {
				na.errorify();
				va.errorify();
				co.errorify();
			}
			
			b.add(na);
			b.tab(6);
			b.add(co);
			b.tab(7);
			b.add(va);
			b.tab(9);
			cu.add(')');
			cu.normalify();
			b.add(cu);
			
		}

		@Override
		public double progress(T t) {
			return comp.progress(getter.d.getD(t), value	);
		}

	}
}
