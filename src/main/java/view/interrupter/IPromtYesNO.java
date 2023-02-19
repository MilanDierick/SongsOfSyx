package view.interrupter;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import view.keyboard.KEYS;

/**
 * centered medium sized panel. Can be persistent or dismissable. Composes of a
 * question and a yes and no button.
 * 
 * @author mail__000
 *
 */
public class IPromtYesNO extends Interrupter {

	private final GTextR text = new GTextR(UI.FONT().M, 1000, DIR.C);
	private final GuiSection section = new GuiSection();
	private final GPanelL box = new GPanelL(0.4, 0.3);
	private final ACTION close = new ACTION() {
		@Override
		public void exe() {
			deactivate();
		}

	};

	private final CLICKABLE yes = new GButt.Panel(SPRITES.icons().m.ok) {
		@Override
		protected void clickA() {
			hide();
		};
	};
	private final CLICKABLE no = new GButt.Panel(SPRITES.icons().m.cancel) {
		@Override
		protected void clickA() {
			hide();
		};
	};
	private boolean dismissable;
	private final InterManager m;

	public IPromtYesNO(InterManager manager) {
		this.m = manager;
		text.text().lablify();
		section.add(box);
		section.body().centerIn(C.DIM());

		GuiSection butts = new GuiSection();
		yes.hoverInfoSet(DicMisc.¤¤Yes);
		no.hoverInfoSet(DicMisc.¤¤No);
		butts.add(yes).addRight(0, no);
		box.centreNavButts(butts);
		section.merge(butts);

		text.text().setMaxWidth(box.getInnerArea().width());
		;
		section.add(text);

	}

	static boolean tmp = false;
	
	public void activate(CharSequence message, ACTION yesAction, ACTION noAction, boolean dismissable) {
		show(m);

		this.dismissable = dismissable;

		box.setCloseAction(dismissable ? close : null);

		text.text().set(message);
		text.adjust();
		text.body().centerIn(box.getInnerArea());

		yes.clickActionSet(yesAction);
		no.clickActionSet(noAction);
		no.visableSet(noAction != null);

	}

	public void deactivate() {
		hide();
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		else if (dismissable && button == MButt.RIGHT)
			deactivate();
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.MAIN().ESCAPE.consumeClick()) {
			deactivate();
			return true;
		}
		KEYS.clear();
		return false;
	}

	@Override
	public boolean canSave() {
		return dismissable;
	}

}
