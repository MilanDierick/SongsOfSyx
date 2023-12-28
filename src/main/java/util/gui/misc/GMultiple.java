package util.gui.misc;

import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import view.main.VIEW;

public abstract class GMultiple<T extends INDEXED> extends HoverableAbs{

	private final int maDim;
	private final int miDim;
	private final int max;
	private final LIST<T> tt;
	
	public GMultiple(int sDim, int width, int maxAm, LIST<T> all){
		
		maDim = sDim;
		miDim = sDim/2;
		max = maxAm;
		width = sDim*(width/sDim);
		int height = sDim + maxAm/(width/miDim);
		body.setDim(width, height);
		this.tt = all;
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
		double tot = 0;
		for (T t : tt) {
			tot += getValue(t);
		}
		if (tot == 0)
			return;
		
		int am = (int) Math.round(max*tot);
		
		int x1 = body.x1();
		int y1 = body.y1();
		
		if (am == 0) {
			T big = tt.get(0);
			double ma = 0;
			for (T t : tt) {
				if (getValue(t) > ma) {
					big = t;
					ma = getValue(t);
				}
			}
			render(r, big, x1+maDim/2, y1+maDim/2);
			return;
		}

		int dd = maDim*max/(am);
		dd = CLAMP.i(dd, miDim, maDim);
		
		int ti = 0;
		double vv = getValue(tt.get(ti))*am;
		for (int i = 0; i < am; i++) {
			if (vv > i+1) {
				render(r, tt.get(ti), x1+maDim/2, y1+maDim/2);
			}else {
				int biggest = ti;
				double bv = i-vv;
				while(vv < i+1 && (ti+1) < tt.size()) {
					ti++;
					double nv = vv+getValue(tt.get(ti))*am;
					double dv = CLAMP.d(nv, 0, i+1)-vv;
					if (dv > bv) {
						biggest = ti;
						bv = dv;
					}
					vv = nv;
				}
				render(r, tt.get(biggest), x1+maDim/2, y1+maDim/2);
			}
			x1 += dd;
			if (x1 + maDim > body.x2()) {
				x1 = body.x1();
				y1 += maDim;
			}
			
		}

		
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		
		super.hoverInfoGet(text);
		
		GBox b = (GBox) text;
		double tot = 0;
		for (T t : tt) {
			tot += getValue(t);
		}
		if (tot == 0)
			return;
		
		int am = (int) Math.round(max*tot);
		
		int x1 = body.x1();
		int y1 = body.y1();
		
		int tx = VIEW.mouse().x();
		int ty = VIEW.mouse().y();
		
		if (am == 0) {
			T big = tt.get(0);
			double ma = 0;
			for (T t : tt) {
				if (getValue(t) > ma) {
					big = t;
					ma = getValue(t);
				}
			}
			
			if (tx > x1 && tx < x1+maDim && ty > y1 && ty < y1+maDim) {
				hover(b, big);
				return;
			}
			return;
		}

		int dd = maDim*max/(am);
		dd = CLAMP.i(dd, miDim, maDim);
		
		int ti = 0;
		double vv = getValue(tt.get(ti))*am;
		for (int i = 0; i < am; i++) {
			if (vv > i+1) {
				if (tx > x1 && tx < x1+maDim && ty > y1 && ty < y1+maDim) {
					hover(b, tt.get(ti));
					return;
				}
			}else {
				int biggest = ti;
				double bv = i-vv;
				while(vv < i+1 && (ti+1) < tt.size()) {
					ti++;
					double nv = vv+getValue(tt.get(ti))*am;
					double dv = CLAMP.d(nv, 0, i+1)-vv;
					if (dv > bv) {
						biggest = ti;
						bv = dv;
					}
					vv = nv;
				}
				if (tx > x1 && tx < x1+maDim && ty > y1 && ty < y1+maDim) {
					hover(b, tt.get(biggest));
					return;
				}
			}
			x1 += dd;
			if (x1 + maDim > body.x2()) {
				x1 = body.x1();
				y1 += maDim;
			}
			
		}
		
	}
	
	protected abstract void hover(GBox b, T t);
	protected abstract double getValue(T t);
	protected abstract void render(SPRITE_RENDERER r, T t, int cx, int cy);
}
