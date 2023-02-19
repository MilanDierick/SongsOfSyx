package view.interrupter;

import java.util.TreeMap;

import init.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.StringInputSprite;
import snake2d.util.sprite.text.StringInputSprite.InputClickable;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import view.keyboard.KEYS;

public class IDebugPanelAbs extends Interrupter{

	private final GuiSection section = new GuiSection();
	private final Type current;

	private final GPanelL panel;
	private final StringInputSprite filter = new StringInputSprite(20, UI.FONT().M){
		@Override
		protected void change() {
			current.init(0);
		};
	}.placeHolder("Search");
	private final InputClickable fc = filter.c(DIR.W).colors(COLOR.WHITE65, COLOR.WHITE2WHITE);
	private final InterManager manager;
	
	public void show() {
		super.show(manager);
		fc.focus();
	}
	
	public IDebugPanelAbs(InterManager manager, TreeMap<CharSequence, CLICKABLE> hash) {
		desturberSet();
		this.manager = manager;
		addMisc();
		panel = new GPanelL(0.5, 0.7);
		panel.setCloseAction(new ACTION() {
			@Override
			public void exe() {
				hide();
				
			}
		});
		
		section.add(panel);
		
		
		current = new Type(hash);

		section.body().centerIn(C.DIM());
		
		RECTANGLE bounds = panel.getInnerArea();
		fc.body().moveX1Y1(bounds.x1(), bounds.y1()+20);
		section.add(fc);
		RENDEROBJ title = new GText(UI.FONT().H2, "Debugger Panel").lablify().r(DIR.C);
		panel.centreTitle(title);
		section.add(title);
		hash.clear();
		current.init(0);
		
	}
	
	protected void addMisc() {
		
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		current.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			section.click();
			current.click();
		}
		else if (button == MButt.RIGHT) {
			hide();
		}
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		current.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.MAIN().ESCAPE.consumeClick())
			hide();
		
		return false;
	}
	
	private class Type extends GuiSection {
		
		private final TreeMap<String, CLICKABLE> items;
		private int itemCount = 0;
		private int itemLast = 0;
		private CLICKABLE next;
		private CLICKABLE prev;
		private final int maxrows =  (int) (25.0*C.HEIGHT()/1000);
		
		@SuppressWarnings("unchecked")
		Type(TreeMap<CharSequence, CLICKABLE> items){
			this.items = (TreeMap<String, CLICKABLE>) items.clone();
			next = new GButt.Icon(SPRITES.icons().m.arrow_right) {
				@Override
				protected void clickA() {
					prev.activeSet(true);
					init(itemLast);
				};
			};
			prev = new GButt.Icon(SPRITES.icons().m.arrow_left) {
				@Override
				protected void clickA() {
					next.activeSet(true);
					init(itemLast-itemCount-maxrows*2);
				};
			};
		}
		
		void init(int first) {
			this.itemCount = 0;
			this.itemLast = first;
			super.clear();
			RECTANGLE bounds = panel.getInnerArea();
			body().moveX1Y1(bounds.x1(), fc.body().y2()+10);
			int x1 = body().x1();
			int y1 = body().y1();
			prev.activeSet(first != 0);
			next.activeSet(false);
			int i = 0;
			int rows = 0;
			int cols = 0;
			
			for (java.util.Map.Entry<String, CLICKABLE> c : items.entrySet()) {
				if (filter.text().length() == 0 || c.getKey().contains(filter.text())) {
					i++;
					if (i < first)
						continue;
					rows++;
					if (rows > maxrows) {
						cols++;
						if (cols == 2) {
							next.activeSet(true);
							break;
						}
						
						rows = 1;
						x1 += bounds.width()/2;
						y1 = body().y1();
					}
					itemLast++;
					itemCount++;
					CLICKABLE cl = c.getValue();
					cl.body().moveX1Y1(x1, y1);
					add(cl);
					y1 += cl.body().height();
				}
			}
			prev.body().moveX1(bounds.x1());
			prev.body().moveY2(bounds.y2());
			next.body().moveX2(bounds.x2());
			next.body().moveY2(bounds.y2());
			add(prev);
			add(next);
		}
	}
	
}
