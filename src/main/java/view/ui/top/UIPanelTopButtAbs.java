package view.ui.top;

import game.time.Intervals;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.*;
import util.info.GFORMAT;
import view.main.VIEW;

abstract class UIPanelTopButtAbs extends GButt {

	private final GStat stat = new GStat() {
		@Override
		public void update(GText text) {
			GFORMAT.i(text, getNumber());
			text.lablify();
		}
	}.decrease();

	private static final COLOR worse = new ColorImp(60, 20, 0);
	private static final COLOR badbg = new ColorImp(30, 10, 10);
	private static final COLOR full = new ColorImp(21, 43, 18);
	private static final COLOR notFull = new ColorImp(61, 61, 30);
	private static final COLOR fuller = new ColorImp(20, 120, 60);

	
	public UIPanelTopButtAbs(SPRITE icon, int width, int height) {
		super(icon);
		body.setWidth(width);
		body.setHeight(height);
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

		renAction();
		
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
		

		
		boolean active = isActive();
		if (active) {
			badbg.render(r, body, -4);
			double cu = CLAMP.d(value(), 0, 1);
			double ta = CLAMP.d(valueNext(), 0, 1);

			ColorImp col = ColorImp.TMP;

			
			
			if (cu == ta && cu == 1) {
				col.set(full);
			} else if (ta > cu) {
				col.interpolate(notFull, fuller, Intervals.circlePow(VIEW.renderSecond(), 1));
			} else if (ta < cu) {
				col.interpolate(notFull, worse, Intervals.circlePow(VIEW.renderSecond(), 1));
			} else {
				col.set(notFull);
			}
			GMeter.renderDelta(r, cu, ta, body.x1()+2, body.x2()-2, body.y1()+2, body.y2()-2, true, false);
			//col.render(r, body.x1() + 4, (int) (body.x1() + 4 + (body.width() - 8) * cu), body.y1() + 4, body.y2() - 4);
		}
		
		

		
		stat.adjust();
		render(r, label, stat, active);
		

	}
	
	abstract void render(SPRITE_RENDERER r, SPRITE label, GStat stat, boolean active);

	protected abstract int getNumber();

	protected abstract double value();

	protected abstract double valueNext();

	protected abstract boolean isActive();

}