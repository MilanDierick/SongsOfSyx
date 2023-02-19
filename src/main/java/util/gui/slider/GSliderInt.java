package util.gui.slider;

import init.D;
import init.sprite.ICON;
import init.sprite.SPRITES;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import view.main.VIEW;

public class GSliderInt extends GuiSection{

	private final INTE in;
	private static final int midWidth = 8;
	private static CharSequence ¤¤setAmount = "¤Set amount";
	private static CharSequence ¤¤setAmountD = "¤Set amount {0}-{1}";
	
	static {
		D.ts(GSliderInt.class);
	}
	
	public GSliderInt(INTE in, int width, boolean input){
		this(in, width, 24, input);
		
		
	}
	
	public GSliderInt(INTE in, int width, int height, boolean input){
		this.in = in;
		
		if (input) {
			width -= (ICON.SMALL.SIZE+2)*3;
		}
		
		width -= 4;
		
		if (width < 0)
			width = 0;
		
		if (input) {
			addRightC(0, new GButt.ButtPanel(SPRITES.icons().s.minifier) {
				private double clickSpeed;
				
				@Override
				protected void clickA() {
					in.inc(-1);
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					if (isHovered &&  MButt.LEFT.isDown()) {
						clickSpeed += ds;
						if (clickSpeed > 10)
							clickSpeed = 10;
						in.inc(-(int)clickSpeed);
						
					}else {
						clickSpeed = 0;
					}
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			});
			
		}
		
		addRightC(4, new Mid(width, height));
		
		if (input) {
			addRightC(4, new GButt.ButtPanel(SPRITES.icons().s.magnifier) {
				private double clickSpeed;
				
				@Override
				protected void clickA() {
					in.inc(1);
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					if (isHovered &&  MButt.LEFT.isDown()) {
						clickSpeed += ds*2;
						if (clickSpeed > 10)
							clickSpeed = 10;
						in.inc((int)clickSpeed);
						
					}else {
						clickSpeed = 0;
					}
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			});
			
			addRightC(0, new GButt.ButtPanel(SPRITES.icons().s.pluses) {
				
				@Override
				protected void clickA() {
					Str.TMP.clear().add(¤¤setAmountD).insert(0, in.min()).insert(1, in.max());
					VIEW.inters().input.requestInput(rec, Str.TMP);
				}
				
				
			}.hoverInfoSet(¤¤setAmount));
			
		}
		
		
	}
	
	private final STRING_RECIEVER rec = new STRING_RECIEVER() {
		
		@Override
		public void acceptString(CharSequence string) {
			String s = ""+string;
			try {
				int i = Integer.parseInt(s);
				i = CLAMP.i(i, in.min(), in.max());
				in.set(i);
			}catch(Exception e) {
				
			}
			
		}
	};
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		activeSet(in.max() > 0);
		super.render(r, ds);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		b.add(GFORMAT.i(b.text(), in.get()));
	}
	
	protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
		COLOR.WHITE65.render(r, x1, x1+width, y1, y2);
	}
	
	private class Mid extends CLICKABLE.ClickableAbs {

		private boolean clicked = false;
		
		Mid(int width, int height){
			super(width, height-4);
		}
		
		@Override
		protected void clickA() {
			clicked = true;
			double x = (VIEW.mouse().x() - body().x1())/(double)body().width();
			in.setD(CLAMP.d(x, 0, 1));
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			clicked &= MButt.LEFT.isDown();
			if (clicked) {
				double x = (VIEW.mouse().x() - body().x1())/(double)body().width();
				in.setD(CLAMP.d(x, 0, 1));
			}
			

			
			GCOLOR.UI().border().render(r, body, 2);
			GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, 1);
			
			
			int x2 = body().x1() + (int) (in.getD()*body().width());
			renderMidColor(r, body().x1(), x2-body().x1(), body().width(), body().y1(), body().y2());
			
			int cx = (int) (body.x1()+body().width()*in.getD());
			
			if (!isHovered) {
				COLOR.WHITE85.render(r, cx-1, cx+1, body().y1()+1, body().y2()-1);
			}else {
				GCOLOR.UI().border().render(r, cx-midWidth/2, cx+midWidth/2, body().y1(), body().y2());
				COLOR c = isHovered || clicked ? GCOLOR.T().H1 : GCOLOR.T().H2;
				c.render(r, cx-midWidth/2+1, cx+midWidth/2-1, body().y1()+1, body().y2()-1);
				COLOR.BLACK.render(r, cx-1, cx+2, body().y1()+2, body().y2()-2);
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				double d = MButt.clearWheelSpin();
				if (d < 0)
					in.inc(-1);
				else if (d > 0)
					in.inc(1);
				return true;
			}
			return false;
		}
		
		
	}
	
	public static void renderMid(SPRITE_RENDERER r, int x1, int x2, int y1, int y2, double d, boolean isActive, boolean isSelected, boolean isHovered) {

		
		GCOLOR.UI().border().render(r, x1-2, x2+2, y1-2, y2+2);
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, x1-1, x2+1, y1-1, y2+1);
		
		int width = x2-x1;
		
		x2 = x1 + (int) (d*width);
		COLOR.WHITE65.render(r, x1, x2, y1, y2);
		
	}
	
}
