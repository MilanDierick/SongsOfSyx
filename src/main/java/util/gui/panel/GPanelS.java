package util.gui.panel;

import init.C;
import init.sprite.UI.UI;
import init.sprite.UI.UICons;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.*;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GText;

public class GPanelS extends RENDEROBJ.RenderImp {

	
	private final static int MW = 5*C.SG;
	private final GText title = new GText(UI.FONT().H2, 32).lablify();
	private LIST<SPRITE> sprite = UI.PANEL().box;
	private boolean centerTitle = false;
	private final int size = sprite.get(0).width();
	private final RecFacade outer = new RecFacade() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int width() {
			if (title.width()+4*size > body.width() + 4*MW)
				return title.width()+4*size;
			return body.width() + 4*MW;
		}
		
		@Override
		public int height() {
			return body.height() + 4*MW + (title.length() > 0 ? 2*MW : 0);
		}
		
		@Override
		public int y1() {
			return body.y1() - 2*MW - (title.length() > 0 ? 2*MW : 0);
		}
		
		@Override
		public int x1() {
			return body.x1()-2*MW;
		}
		
		@Override
		public RECTANGLEE moveY1(double Y1) {
			body.moveY1(Y1 + 2*MW + (title.length() > 0 ? 2*MW : 0));
			return this;
		}
		
		@Override
		public RECTANGLEE moveX1(double X1) {
			body.moveX1(X1 + 2*MW);
			return this;
		}
		
		@Override
		public RecFacade setWidth(double width) {
			body.setWidth(width-4*MW);
			return this;
		}
		
		@Override
		public RecFacade setHeight(double height) {
			body.setHeight(height-4*MW - (title.length() > 0 ? 2*MW : 0));
			return this;
		}
	};
	
	public GPanelS() {
		
	}
	
	public void setButtBg() {
		sprite = UI.PANEL().boxPanel;
	}
	
	@Override
	public RecFacade body() {
		return outer;
	}
	
	public Rec inner() {
		return body;
	}
	
	public GPanelS setTitle(CharSequence title) {
		this.title.set(title);
		return this;
	}
	
	public GPanelS titleCenter() {
		this.centerTitle = true;
		return this;
	}
	
	public GPanelS titleClear() {
		this.title.clear();
		return this;
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		int width = body.width() + 4*MW;
		int height = body.height() + 4*MW;
		
		int x1 = body.x1()-2*MW;
		
		if (title.width()+4*size > width) {
			x1 -= (title.width()+4*size-width)/2;
			width = title.width()+4*size;
		}
		
		int y1 = body.y1()-2*MW;
		
		
		if (title.length() > 0) {
			height += 2*MW;
			y1 -= 2*MW;
		}
		
		int xdec = (width % size);
		int ydec = (height % size);
		
		final int txs = width / size + (xdec > 0 ? 1:0);
		final int tys = height / size + (ydec > 0 ? 1:0);
		
		xdec = xdec != 0 ? size -xdec : 0;
		ydec = ydec != 0 ? size -ydec : 0;

		if (txs == 3 && xdec > size/2)
			xdec  = size/2;
		
		renderRow(r, x1, y1, txs, xdec, 0);


		
		for (int y = 1; y < tys-1; y++) {
			
			if ( y == tys-2) {
				renderRow(r, x1, y1 + (y)*size-ydec, txs, xdec, 3);
				
			}else {
				renderRow(r, x1, y1 + (y)*size, txs, xdec, 3);
			}
		}
		
		renderRow(r, x1, y1 + (tys-1)*size-ydec, txs, xdec, 6);
		
		
		if (title.length() >0)
			renderTitle(r);
		
	}
	
	private void renderRow(SPRITE_RENDERER r, int x1, int y1, int txs,  int xdec, int start) {
		sprite.get(start).render(r, x1, y1);
		for (int i = 1; i < txs-1; i++) {
			if (i == txs-2)
				sprite.get(start+1).render(r, x1+i*size-xdec, y1);
			else
				sprite.get(start+1).render(r, x1+i*size, y1);
		}
		sprite.get(start+2).render(r, x1+(txs-1)*size -xdec, y1);
	}
	
	public void renderTitle(SPRITE_RENDERER r) {

		if (title == null || title.length() == 0)
			return;
		
		UICons sprite = UI.PANEL().panelL;
		int size = sprite.get(DIR.E).width();
		final int width = title.width();
		final int xdec = size - (width % size);
		final int txs = width / size + (xdec > 0 ? 1:0);
		int x1 = body.x1();
		
		if (centerTitle || title.width()+4*size > body.width() + 4*MW) {
			x1 -= (title.width()+4*size-(body.width() + 4*MW))/2;
			x1 += 3*MW;
		}
		
		int y1 = body.y1()-4*MW-size/2 + 3*C.SG;

		sprite.get(DIR.E).render(r, x1, y1);
		for (int i = 0; i < txs-1; i++)
			sprite.get(DIR.E, DIR.W).render(r, x1+(i+1)*size, y1);
		sprite.get(DIR.E, DIR.W).render(r, x1+(txs)*size-xdec, y1);
		sprite.get(DIR.W).render(r, x1+(txs+1)*size-xdec, y1);
		
		x1 += size;
		x1 += ((txs*size-xdec)-title.width())/2;
		y1 += (size-title.height())/2;
		title.render(r,x1, y1);
		
	}
	
	public void moveExit(BODY_HOLDERE e) {
		e.body().moveC(outer.x2()-8, outer.y1()+4);
	}

	public CharSequence title() {
		return title;
	}


}
