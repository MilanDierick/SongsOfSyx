package view.ui.tech;

import init.sprite.UI.UI;
import init.tech.TECHS;
import init.tech.TechTree;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GText;

final class NodeCreator {

	static RENDEROBJ[][] make(int width){
		
		int maxW = width/Node.WIDTH;

		
		return trees(maxW);
		
	}
	
	private static RENDEROBJ[][] trees(int maxW){
		
		
		LinkedList<RENDEROBJ[][]> rows = new LinkedList<>();
		
		LinkedList<RENDEROBJ[][]> remaining = new LinkedList<>();
		
		for (TechTree tree : TECHS.TREES()) {
			
			RENDEROBJ[][] oo = new RENDEROBJ[tree.nodes.length+1][];
			int mw = 0;
			
			for (int ri = 0; ri < tree.nodes.length; ri++) {
				mw = Math.max(mw, tree.nodes[ri].length);
				
			}
			for (int ri = 0; ri < oo.length; ri++) {
				oo[ri] = new RENDEROBJ[mw];

			}
			for (int ri = 0; ri < tree.nodes.length; ri++) {
				for (int ci = 0; ci < tree.nodes[ri].length; ci++) {
					if (tree.nodes[ri][ci] != null) {
						oo[ri+1][ci] = new Node(tree.nodes[ri][ci]);
					}
				}
			}
			
			final GText tt = new GText(UI.FONT().H2, tree.name).lablify();
			final int x1 = (mw * Node.WIDTH)/2 - tt.width()/2;
			final int w = mw*Node.WIDTH;
			final SPRITE bo = UI.decor().borderTop(w);
			oo[0][0] = new RENDEROBJ.RenderImp(Node.WIDTH, Node.HEIGHT) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					
					tt.render(r, body().x1() + x1, body().y2()-tt.height()-bo.height()-4);
					bo.render(r, body().x1(), body().x1()+w, body().y2()-bo.height(), body().y2());
				}
			};
			
			remaining.add(oo);
		}
		
		
		while(!remaining.isEmpty()) {
			
			RENDEROBJ[][] oo = remaining.removeFirst();
			
			int bestV = Integer.MAX_VALUE;
			RENDEROBJ[][] best = null;
			
			for (RENDEROBJ[][] other : remaining) {
				if (oo[0].length + 1 + other[0].length < maxW) {
					int h = Math.max(oo.length, other.length);
					int w = 1 + oo[0].length + other[0].length;
					int a = w*h;
					
					a -= oo.length*oo[0].length;
					a -= other.length*other[0].length;
					if (a < bestV) {
						bestV = a;
						best = other;
					}
					
				}
			}
			
			if (best == null) {
				rows.add(oo);
			}else {
				remaining.remove(best);
				int h = Math.max(oo.length, best.length);
				int w = 1 + oo[0].length + best[0].length;
				
				RENDEROBJ[][] nn = new RENDEROBJ[h][w];
				
				for (int y = 0; y < oo.length; y++) {
					for (int x = 0; x < oo[0].length; x++) {
						nn[y][x] = oo[y][x];
					}
				}
				
				for (int y = 0; y < best.length; y++) {
					for (int x = 0; x < best[0].length; x++) {
						nn[y][x+1+oo[0].length] = best[y][x];
					}
				}
				remaining.add(nn);
			}
			
		}
		
		int h = 0;
		for (RENDEROBJ[][] row : rows) {
			h+= row.length;
		}
		
		RENDEROBJ[][] res = new RENDEROBJ[h][maxW];
		
		h = 0;
		for (RENDEROBJ[][] row : rows) {
			for (int y = 0; y < row.length; y++) {
				for (int x = 0; x < row[0].length; x++) {
					res[y+h][x] = row[y][x];
				}
			}
			h+= row.length;
		}
		
		for (int y = 0; y < res.length; y++) {
			for (int x = 0; x < res[0].length; x++) {
				if (res[y][x] == null) {
					res[y][x] = new RENDEROBJ.RenderDummy(Node.WIDTH, Node.HEIGHT);
				}

			}
		}
		return res;
	}
	
	
	

	
}
