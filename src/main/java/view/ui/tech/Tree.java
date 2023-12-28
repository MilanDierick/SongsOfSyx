package view.ui.tech;

import java.util.Arrays;

import game.boosting.Boostable;
import game.faction.FACTIONS;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;
import view.main.VIEW;

final class Tree extends GuiSection{

	private final RENDEROBJ[][] nodes;
	private GuiSection content = new GuiSection();
	final INTE ii;
	
	private final Coo[] nodeCoos = new Coo[TECHS.ALL().size()]; 
	private int LINE_DIM = 4;
	boolean[] hoverededTechs = new boolean[TECHS.ALL().size()];
	boolean[] filteredTechs = new boolean[TECHS.ALL().size()];
	Boostable hoveredBoost = null;
	final Prompt prompt = new Prompt();
	
	Tree(int height, int width){
		
		width -= 24;
		height = Node.HEIGHT*(height/Node.HEIGHT);
		
		nodes = NodeCreator.make(width);
		for (TECH t : TECHS.ALL())
			nodeCoos[t.index()] = new Coo();
		
		int lr = 0;
		int h = 0;
		
		int w = 0;
		
		for (int i = nodes.length-1; i >= 0; i--) {
			
			h += height(nodes[i]);
			if (h > height)
				break;
			lr = i;
			w = Math.max(w, width(nodes[i]));
		}
		
		content.body().setWidth(w).setHeight(height);
		
		add(content);
		final int last = lr;
		ii = new INTE() {
			
			int c = 0;
			
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
				return c;
			}
			
			@Override
			public void set(int t) {
				c = CLAMP.i(t, 0, last);
				adjust(c);
			}
		};
		
		
		
		addRelBody(8, DIR.E, new GSliderVer(ii, height));
		
		adjust(0);
		
	}
	
	@Override
	protected void moveCallback() {
		if (ii != null)
			ii.inc(0);
	}
	
	public void filter(TECH t) {
		filteredTechs[t.index()] = true;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (body().holdsPoint(VIEW.mouse())) {
			double d = MButt.clearWheelSpin();
			if ( d != 0) {
				ii.inc(-(int)d);
			}
		}
		
		for (TECH t : TECHS.ALL()) {
			renderLine(r, t, true, false, false);
		}
		for (TECH t : TECHS.ALL()) {
			renderLine(r, t, false, false, true);
		}
		for (TECH t : TECHS.ALL()) {
			renderLine(r, t, false, true, false);
		}
		
		RENDEROBJ rr = content.getHovered();
		if (rr != null && rr instanceof Node) {
			renderHovered(r, ((Node)(rr)).tech);
		}
		
		super.render(r, ds);
		Arrays.fill(filteredTechs, false);
		Arrays.fill(hoverededTechs, false);
		hoveredBoost = null;
	}
	
	private void renderHovered(SPRITE_RENDERER r, TECH t) {
		hoverededTechs[t.index()] = true;
		Coo from = nodeCoos[t.index()];
		if (from == null)
			return;
		for (int ti = 0; ti < t.requiresNodes().size(); ti++) {
			TechRequirement c = t.requiresNodes().get(ti);
			if (c.level <= 0)
				continue;
			Coo to = nodeCoos[c.tech.index()];
			hoverededTechs[c.tech.index()] = true;
			renderArrow(r, from, to, Node.Chovered);
			renderHovered(r, c.tech);
		}
	}
	
	private void renderLine(SPRITE_RENDERER r, TECH t, boolean dormant, boolean unlocked, boolean unlockable) {
		Coo from = nodeCoos[t.index()];
		if (from == null)
			return;
		for (TechRequirement c : t.requiresNodes()) {
			if (c.level <= 0)
				continue;
			Coo to = nodeCoos[c.tech.index()];
			COLOR col = Node.Cdormant;
			if (FACTIONS.player().tech.level(c.tech) > 0) {
				if (!unlocked)
					continue;
				col = Node.Callocated;
			}
			else if (FACTIONS.player().tech.costOfNextWithRequired(c.tech) <= FACTIONS.player().tech().available().get()) {
				if (!unlockable)
					continue;
				col = Node.CUnlockable;
			}else if (!dormant)
				continue;
			renderArrow(r, from, to, col);
			
		}
		
	}
	
	private void renderArrow(SPRITE_RENDERER r, Coo from, Coo to, COLOR c) {
		
		
		
//		if (from.y() < to.y())
//			renderArrow(r, to, from, c);
		
		if (from.x() == to.x() && to.y()-from.y() < Node.HEIGHT*2) {
			renderLine(r, c, from.x()-LINE_DIM, from.x()+LINE_DIM, from.y(), to.y());
			return;
		}
		
		renderLine(r, c, from.x()-LINE_DIM, from.x()+LINE_DIM, from.y(), from.y()-Node.HEIGHT/2);
		renderLine(r, c, to.x()-LINE_DIM, to.x()+LINE_DIM, to.y()-LINE_DIM, to.y()+Node.HEIGHT/2+LINE_DIM);
		
		int x = 0;
		int dx = 0;
		int fx = from.x();
		if (to.x() <= from.x()) {
			x = to.x()+Node.WIDTH/2;
			dx = -LINE_DIM;
			fx += LINE_DIM;
		}else {
			x = to.x()-Node.WIDTH/2;
			dx = LINE_DIM;
			fx -= LINE_DIM;
		}
		
		renderLine(r, c, fx, x+dx, from.y()-Node.HEIGHT/2-LINE_DIM, from.y()-Node.HEIGHT/2+LINE_DIM);
		
		renderLine(r, c, x-LINE_DIM, x+LINE_DIM, from.y()-Node.HEIGHT/2, to.y()+Node.HEIGHT/2-LINE_DIM);
		
		renderLine(r, c, x-LINE_DIM, to.x()+LINE_DIM, to.y()+Node.HEIGHT/2-LINE_DIM, to.y()+Node.HEIGHT/2+LINE_DIM);
		
	}
	
	private void renderLine(SPRITE_RENDERER r, COLOR c, int x1, int x2, int y1, int y2) {
		

		
		if (x2 < x1) {
			int x =x1;
			x1 = x2;
			x2 = x;
		}
		
		if (y2 < y1) {
			int y = y1;
			y1 = y2;
			y2 = y;
		}
		
		if (x2 < content.body().x1() && x1 < content.body().x1())
			return;
		if (y2 < content.body().y1() && y1 < content.body().y1())
			return;
		
		if (x2 > content.body().x2() && x1 > content.body().x2())
			return;
		if (y2 > content.body().y2() && y1 > content.body().y2())
			return;
		
		if (x2 > content.body().x2())
			x2 = content.body().x2();
		if (y2 > content.body().y2())
			y2 = content.body().y2();
	
		if (x1 < content.body().x1())
			x1 = content.body().x1();
		if (y1 < content.body().y1())
			y1 = content. body().y1();
		
		if (x2 == x1 || y2 == y1)
			return;
		
		c.render(r, x1, x2, y1, y2);
	}
	
	private void adjust(int fr){
		
		
		int x1 = content.body().x1();
		int y1 = content.body().y1();
		int w = content.body().width();
		int h = content.body().height();
		content.clear();
		content.body().setDim(w, h);
		content.body().moveX1Y1(x1, y1);
		int y = y1;
		
		{
			int dy = 0;
			for (int i = 0; i < fr; i++) {
				dy += height(nodes[i]);
			}
			y -= dy;
		}

		
		for (int i = 0; i < nodes.length; i++) {
			
			RENDEROBJ[] rr = nodes[i];
			
			int hi = height(rr);
			
			
			int x = x1;
			
			for (RENDEROBJ r : rr) {
				r.body().moveX1(x);
				r.body().moveY1(y);
				
				if (r instanceof Node) {
					Node n = (Node) r;
					nodeCoos[n.tech.index()].set(r.body().cX(), r.body().cY());
				}
				x += r.body().width();
				if (y >= y1 && y + hi <= content.body().y2())
					content.add(r);
			}
			
			y+= hi;
		}
		
	}
	
	private int height(RENDEROBJ[] rr) {
		int h = 0;
		for (RENDEROBJ r : rr) {
			h = Math.max(h, r.body().height());
		}
		return h;
	}
	
	private int width(RENDEROBJ[] rr) {
		int w = 0;
		for (RENDEROBJ r : rr) {
			w += r.body().width();
		}
		return w;
	}
	
}
