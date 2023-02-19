package util.gui.misc;

import init.C;
import init.D;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.INT.INTE;
import util.gui.slider.GSliderInt;

public abstract class GColorPicker extends  GuiSection{

	private static CharSequence ¤¤color = "¤color";
	private static CharSequence ¤¤red = "¤red";
	private static CharSequence ¤¤green = "¤green";
	private static CharSequence ¤¤blue = "¤blue";
	static {
		D.ts(GColorPicker.class);
	}
	
	public GColorPicker(boolean glow) {
		this(glow, ¤¤color);
	}
	
	public GColorPicker(boolean glow, CharSequence name) {
		final int max = glow ? 255 : 127;
		int w = 120;
		GSliderInt r = new GSliderInt(new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return max;
			}
			
			@Override
			public int get() {
				return color().red() & 0x0FF;
			}
			
			@Override
			public void set(int t) {
				
				color().setRed(t);
				change();
			}
			
		}, w, true);
		r.addRelBody(C.SG*8, DIR.W, new GText(UI.FONT().S, ¤¤red).lablify());
		GSliderInt g = new GSliderInt(new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return max;
			}
			
			@Override
			public int get() {
				return color().green() & 0x0FF;
			}
			
			@Override
			public void set(int t) {
				color().setGreen(t);
				change();
			}
		}, w, true);
		g.addRelBody(C.SG*8, DIR.W, new GText(UI.FONT().S, ¤¤green).lablify());
		GSliderInt b = new GSliderInt(new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return max;
			}
			
			@Override
			public int get() {
				return color().blue() & 0x0FF;
			}
			
			@Override
			public void set(int t) {
				color().setBlue(t);
				change();
			}
		}, w, true);
		b.addRelBody(C.SG*8, DIR.W, new GText(UI.FONT().S, ¤¤blue).lablify());
		
		add(r);
		g.body().moveX2(r.body().x2());
		g.body().moveY1(r.body().y2());
		add(g);
		b.body().moveX2(g.body().x2());
		b.body().moveY1(g.body().y2());
		add(b);
		
		addRelBody(C.SG*4, DIR.N, new GText(UI.FONT().H2, name).lablify());
		
	}
	
	public void change() {
		
	}
	
	public abstract ColorImp color();
}
