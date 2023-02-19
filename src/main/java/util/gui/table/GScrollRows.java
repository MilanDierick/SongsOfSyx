package util.gui.table;

import snake2d.MButt;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;

public class GScrollRows {

	private final RENDEROBJ[] rows;
	private final GuiSection srows = new GuiSection();
	private final GuiSection section = new GuiSection() {
		
		@Override
		public void render(snake2d.SPRITE_RENDERER r, float ds) {
			if (hoveredIs()) {
				double d = MButt.clearWheelSpin();
				if (d > 0 && first > 0) {
					first--;
					init();
				}else if (d < 0 && first < last) {
					first++;
					init();
				}
			}
			super.render(r, ds);
		};
	};
	private int first = 0;
	private final int last;
	
	
	public GScrollRows(Iterable<? extends RENDEROBJ> rows, int height){
		this(convert(rows), height, 0);
	}
	
	public GScrollRows(Iterable<RENDEROBJ> rows, int height, int width){
		this(convert(rows), height, width-GSliderVer.WIDTH());
	}
	
	public GScrollRows(Iterable<RENDEROBJ> rows, int height, int width, boolean slide){
		this(convert(rows), height, width-GSliderVer.WIDTH(), slide);
	}
	
	public GScrollRows(RENDEROBJ[] renrows, int height, int width){
		this(renrows, height, width, true);
	}
	
	public GScrollRows(RENDEROBJ[] renrows, int height, int width, boolean slide){
		this.rows = renrows;
		section.body().setHeight(height);
		int w = width;
		for (RENDEROBJ r : rows)
			if (r.body().width() > w)
				w = r.body().width();
		section.body().setWidth(w);
		int h = 0;
		int l = 0;
		for (int i = rows.length-1; i >= 0; i--) {
			h += rows[i].body().height();
			if (h > height) {
				l = i+1;
				break;
			}
		}
		
		if (l < 0)
			l = 0;
		last = l;
		if (slide) {
			GSliderVer slider = new GSliderVer(target, height);
			section.add(slider, section.body().x2(), section.body().y1());
		}
		srows.body().moveX1Y1(section.body());
		section.add(srows);
		init();
	}

	
	private static RENDEROBJ[] convert(Iterable<? extends RENDEROBJ> rows) {
		int size = 0;
		for (@SuppressWarnings("unused") RENDEROBJ r : rows)
			size++;
		RENDEROBJ[] rs = new RENDEROBJ[size];
		size = 0;
		for (RENDEROBJ r : rows)
			rs[size++] = r;
		return rs;
	}


	
	void init() {
		srows.clear();
		srows.body().moveX1Y1(section.body());
		for (int i = first; i < rows.length; i++) {
//			if (i < rows.length-1 && srows.body().height()+rows[i+1].body().height() > section.body().height())
//				break;
			if (srows.body().height()+rows[i].body().height() > section.body().height())
				break;
			srows.add(rows[i], srows.body().x1(), srows.getLastY2());
		}
		
	}
	
	public CLICKABLE view() {
		return section;
	}
	
	public final INTE target = new INTE() {
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return last;
		}
		
		@Override
		public int get() {
			return first;
		}
		
		@Override
		public void set(int t) {
			first = t;
			init();
		}
	};

}
