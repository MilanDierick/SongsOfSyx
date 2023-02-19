package util.gui.panel;

import init.C;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GText;

public class GPanelL extends CLICKABLE.ClickableAbs {

	private final static int MW = 32;
	private final static String closeHover = "close";
	private final TILE_SHEET sprite = UI.PANEL().panelLarge;
	private final int size = sprite.size();
	//private GText title = new GText(SPRITES.fonts().L, 64).lablify();
	private int bottomButtSize = 0;
	private int titleSize = 0;
	private final GText ttitle = new GText(UI.FONT().H1S, 48);
	
	private final RecFacade outer = new RecFacade() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int width() {
			return body.width() + 2*MW;
		}
		
		@Override
		public int height() {
			return body.height() + 2*MW + (titleSize > 0 ? MW/2 : 0) + (bottomButtSize > 0 ? MW/2 : 0);
		}
		
		@Override
		public int y1() {
			return body.y1() - MW - (titleSize > 0 ? MW/2 : 0);
		}
		
		@Override
		public int x1() {
			return body.x1()-MW;
		}
		
		@Override
		public RECTANGLEE moveY1(double Y1) {
			body.moveY1(Y1 + MW + (titleSize > 0 ? MW/2 : 0));
			return this;
		}
		
		@Override
		public RECTANGLEE moveX1(double X1) {
			body.moveX1(X1 + MW);
			return this;
		}
		
		@Override
		public RecFacade setWidth(double width) {
			body.setWidth(width-2*MW);
			return this;
		}
		
		@Override
		public RecFacade setHeight(double height) {
			body.setHeight(height-2*MW - (titleSize > 0 ? MW/2 : 0) - (bottomButtSize > 0 ? MW/2: 0));
			return this;
		}
	};
	
	public GPanelL() {
		
	}
	
	public GPanelL(RECTANGLE r) {
		body.set(r);
	}
	
	public void set(RECTANGLE r) {
		body.set(r);
	}
	
	public GPanelL(double width, double height) {
		body.setWidth(width*C.WIDTH()).setHeight(height*C.HEIGHT());
	}
	
	@Override
	public RecFacade body() {
		return outer;
	}
	
	public GPanelL centreTitle(BODY_HOLDERE title) {
		titleSize = title.body().width();
		int x1 = outer.x1() + (outer.width()-titleSize)/2;
		int y1 = outer.y1() + (size-title.body().height())/2;
		title.body().moveX1Y1(x1, y1);
		return this;
	}

	public GPanelL centreNavButts(BODY_HOLDERE buttons) {
		bottomButtSize = buttons.body().width();
		buttons.body().centerX(outer);
		buttons.body().moveY2(outer.y2());
		return this;
	}

	public GPanelL setCloseAction(ACTION action) {
		this.clickAction = action;
		hoverInfoSet(closeHover);
		return this;
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		if (clickAction != null) {
			if (mCoo.x() > outer.x2()-size && mCoo.x() < outer.x2())
				if (mCoo.y() < outer.y1()+size && mCoo.y() > outer.y1())
					return super.hover(mCoo);
		}
		return false;
	}

	static void render(SPRITE_RENDERER r, int x1, int y1, int width, int height, int topArea, int bottomArea) {
		
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

		int width = outer.width();
		int height = outer.height();
		
		if (titleSize+2*size > width)
			width = titleSize+2*size;
		final int x1 = outer.x1();
		int y1 = outer.y1();
		
		int xdec = (width % size);
		int ydec = (height % size);
		
		final int txs = width / size + (xdec > 0 ? 1:0);
		final int tys = height / size + (ydec > 0 ? 1:0);
		
		xdec = xdec != 0 ? size -xdec : 0;
		ydec = ydec != 0 ? size -ydec : 0;

		if (txs == 3 && xdec > size/2)
			xdec  = size/2;
		
		

		for (int y = 1; y < tys-1; y++) {
			
			if ( y == tys-2) {
				renderMid(r, x1, y1 + (y)*size-ydec, txs, xdec, 3);
				
			}else {
				renderMid(r, x1, y1 + (y)*size, txs, xdec, 3);
			}
		}
		
		renderTop(r, x1, y1, txs, xdec, 0);
		
		renderBottom(r, x1, y1 + (tys-1)*size-ydec, txs, xdec, 6);
		
		
		
//		if (title.length() >0)
//			renderTitle(r);
		
	}
	
	private void renderTop(SPRITE_RENDERER r, int x1, int y1, int txs,  int xdec, int start) {
		final int LEFT = 0;
		final int C = LEFT + 1;
		final int DECORATION_L = C + 1;
		final int DECORATION_C = DECORATION_L + 2;
		final int DECORATION_R = DECORATION_C + 1;
		final int RIGHT_CLOSE = DECORATION_R + 2;
		final int RIGHT = RIGHT_CLOSE + 1;
		
		sprite.render(r, LEFT, x1, y1);
		for (int i = 1; i < txs-1; i++) {
			if (i == txs-2)
				sprite.render(r, C, x1+i*size-xdec, y1);
			else
				sprite.render(r, C, x1+i*size, y1);
		}
		
		if (clickAction != null) {
			sprite.render(r, RIGHT_CLOSE, x1+(txs-1)*size -xdec, y1);
			if (hoveredIs()) {
				COLOR.WHITE150.bind();
				
			}
			SPRITES.icons().s.cancel.render(r, 
					x1+(txs-1)*size -xdec + (size-ICON.SMALL.SIZE)/2, 
					y1 + (size-ICON.SMALL.SIZE)/2);
			COLOR.unbind();
		}else {
			sprite.render(r, RIGHT, x1+(txs-1)*size -xdec, y1);
		}
		
		if (titleSize != 0) {
			int tw = 4 + titleSize/size;
			int dec = titleSize%size;
			
			x1 = outer.cX()-(tw*size + dec)/2;
			sprite.render(r, DECORATION_L, x1, outer.y1());
			sprite.render(r, DECORATION_L+1, x1+size, outer.y1());
			for (int i = 2; i < tw-2; i++) {
				sprite.render(r, DECORATION_C, x1+(i)*size, outer.y1());
			}
			sprite.render(r, DECORATION_C, x1+(tw-3)*size+dec, outer.y1());
			sprite.render(r, DECORATION_R, x1+(tw-2)*size+dec, outer.y1());
			sprite.render(r, DECORATION_R+1, x1+(tw-1)*size+dec, outer.y1());

			if (ttitle.length() != 0) {
				int x = body.cX() - ttitle.width()/2;
				int y = outer.y1() + (size - ttitle.height())/2;
				ttitle.render(r, x, y);
			}
//			x1 = outer.x1() + (outer.width()-titleSize)/2;
//			y1 = outer.y1() + (size-titleSize)/2;
//			title.render(r, x1, y1);
		}
	}
	
	private void renderMid(SPRITE_RENDERER r, int x1, int y1, int txs,  int xdec, int start) {
		final int LEFT = 9;
		final int C = LEFT + 1;
		final int RIGHT = C + 1;
		
		sprite.render(r, LEFT, x1, y1);
		for (int i = 1; i < txs-1; i++) {
			if (i == txs-2)
				sprite.render(r, C, x1+i*size-xdec, y1);
			else
				sprite.render(r, C, x1+i*size, y1);
		}
		sprite.render(r, RIGHT, x1+(txs-1)*size -xdec, y1);
	}
	
	private void renderBottom(SPRITE_RENDERER r, int x1, int y1, int txs,  int xdec, int start) {
		
		final int LEFT = 12;
		final int C = LEFT + 1;
		final int DECORATION_L = C + 1;
		final int DECORATION_C = DECORATION_L + 2;
		final int DECORATION_R = DECORATION_C + 1;
		final int RIGHT = DECORATION_R + 2;
		
		sprite.render(r, LEFT, x1, y1);
		for (int i = 1; i < txs-1; i++) {
			if (i == txs-2)
				sprite.render(r, C, x1+i*size-xdec, y1);
			else
				sprite.render(r, C, x1+i*size, y1);
		}
		sprite.render(r, RIGHT, x1+(txs-1)*size -xdec, y1);
		
		if (bottomButtSize != 0) {
			int tw = 4 + bottomButtSize/size;
			int dec = bottomButtSize%size;
			
			x1 = outer.cX()-(tw*size + dec)/2;
			sprite.render(r, DECORATION_L, x1, y1);
			sprite.render(r, DECORATION_L+1, x1+size, y1);
			for (int i = 2; i < tw-2; i++) {
				sprite.render(r, DECORATION_C, x1+(i)*size, y1);
			}
			sprite.render(r, DECORATION_C, x1+(tw-3)*size+dec, y1);
			sprite.render(r, DECORATION_R, x1+(tw-2)*size+dec, y1);
			sprite.render(r, DECORATION_R+1, x1+(tw-1)*size+dec, y1);

			
		}
		
	}

	public Rec getInnerArea() {
		return body;
	}
	
	

	public void setTitle(CharSequence string) {
		ttitle.set(string).toLower().adjustWidth();
		ttitle.lablify();
		titleSize = ttitle.width();
		
		body().setDim(body());
		
		int ss = titleSize;
		if (clickAction != null)
			ss+= 48;
		
		if (ss > getInnerArea().width()) {
			int d = ss-getInnerArea().width();
			body().incrX(-d);
			body().incrW(2*d);
		}
	}

}
