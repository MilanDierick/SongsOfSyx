package view.world.panel;

import static world.WORLD.*;

import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import util.colors.GCOLOR;
import view.main.VIEW;
import view.subview.GameWindow;
import world.WORLD;

public final class UIMinimapW extends ClickableAbs{

	private final world.WorldMinimap map = WORLD.MINIMAP();
	private boolean clicked = false;
	private final GameWindow window;
	private Rec inner = new Rec(world.WorldMinimap.WIDTH, world.WorldMinimap.HEIGHT);
	public UIMinimapW(GameWindow window){
		
		this.window = window;
		super.body.setDim(world.WorldMinimap.WIDTH+6, world.WorldMinimap.HEIGHT+6);
	}
	
	private void move(GameWindow window){
		
		clicked = true;
		int x1 = (int) ((VIEW.mouse().x() - body().x1())*(float)(PWIDTH()/world.WorldMinimap.WIDTH));
		int y1 = (int) ((VIEW.mouse().y() - body().y1())*(float)(PHEIGHT()/world.WorldMinimap.HEIGHT));
		window.centerAt(x1, y1);
	}
	
	@Override
	protected void clickA() {
		move(window);
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		inner.centerIn(body());
		RECTANGLE pos = inner;
		GCOLOR.UI().borderH(r, body(), 0);
		
		
//		GFrame.render(r, ds, inner);
		map.render(r, pos.x1(), pos.y1());
		

		
		clicked = clicked && MButt.LEFT.isDown();
		if (clicked)
			move(window);
		
		int x1 = pos.x1() + (int)((world.WorldMinimap.WIDTH)*window.pixels().cX()/(float)PWIDTH());
		int y1 = pos.y1() + (int)((world.WorldMinimap.HEIGHT)*window.pixels().cY()/(float)PHEIGHT());
		
		int miniW = (int) ((world.WorldMinimap.WIDTH)*window.pixels().width()/PWIDTH());
		int miniH = (int) ((world.WorldMinimap.HEIGHT)*window.pixels().height()/PHEIGHT());
		
		x1 -= miniW/2;
		y1 -= miniH/2;
		
		int x2 = x1 + miniW;
		int y2 = y1 + miniH;
		
		if(x1 < pos.x1()){
			x1 = pos.x1();;
		}
		
		if(y1 < pos.y1()){
			y1 = pos.y1();
		}
		
		if (y2 > pos.y2()){
			y2 = pos.y2();
		}
		
		if (x2 > pos.x2()){
			x2 = pos.x2();
		}
		
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, x1, x1+1, y1, y2);
		COLOR.WHITE100.render(r, x1+1, x1+2, y1, y2);
		COLOR.BLACK.render(r, x2-1, x2, y1, y2);
		COLOR.WHITE100.render(r, x2-2, x2-1, y1, y2);
		COLOR.BLACK.render(r, x1, x2, y1, y1+1);
		COLOR.WHITE100.render(r, x1+1, x2-1, y1+1, y1+2);
		COLOR.BLACK.render(r, x1, x2, y2-1, y2);
		COLOR.WHITE100.render(r, x1+1, x2-1, y2-2, y2-1);
		OPACITY.O25.bind();
		COLOR.BLACK.render(r, x1+2, x2-2, y1+2, y2-2);
		OPACITY.unbind();
		
	}

	
}
