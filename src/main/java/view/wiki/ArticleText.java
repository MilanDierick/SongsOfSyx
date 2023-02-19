package view.wiki;

import java.util.regex.Pattern;

import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Font;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;
import view.main.VIEW;

class ArticleText extends Article{
	
	private static String pat = Pattern.compile("[\\s]+|\\n|\\r]+").pattern();
	//private final String[] othercats;
	private final CharSequence text;
	private final Link[] links;
	
	ArticleText(Json json){
		super(json.text("NAME"), json.text("CATEGORY"));
//		if (json.has("CATEGORIES"))
//			othercats = json.texts("CATEGORIES");
//		else
//			othercats = new String[0];
		String t = json.text("TEXT");
		
		String [] ss = t.split("\\{");
		links = new Link[ss.length-1];
		String res = ss[0];
		
		{
			int li = 0;
			for (int k = 1; k < ss.length; k++) {
				String[] ls = ss[k].split("\\}");
				if (ls.length == 1) {
					res += ls[0];
					break;
				}
				
				String[] sss = ss[k-1].split(pat);
				
				links[li++] = new Link(res.length(), ls[0], sss[sss.length-1 >= 0 ? sss.length-1 : 0]);
				res += ls[1];
			}
		}
		text = res;
		
	}
	
	@Override
	GuiSection makeSection(LIST<Article> all, int width) {
		return new WikiArticle(this, width, all);
	}

	private static class Link {
		final int position;
		final String key;
		final String name;
		
		Link(int p, String k, String name){
			this.position = p;
			this.key = k;
			this.name = name;
		}
	}
	
	final static class WikiArticle extends GuiSection{

		private static LinkButt hovered;
		
		private int row;
		private final int max;
		private final LinkButt[] linkButts;
		private final ArticleText e;
		
		WikiArticle(ArticleText e, int width, LIST<Article> all){
			this.e = e;
			Font f = UI.FONT().M;
			
			body().setWidth(width);
			
			int maxRows = (HEIGHT)/f.height();
			int rows = UI.FONT().M.getRowAmount(e.text, width-32);
			
			int m = rows-maxRows;
			
			if (m < 0)
				m = 0;
			max = m;
			
			
			INTE tar = new INTE() {
				
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
					return row;
				}
				
				@Override
				public void set(int t) {
					row = t;
				}
			};
			GSliderVer sl = new GSliderVer(tar, HEIGHT);
			add(sl, body().x2()-sl.body().width(),0);
			
			body().moveX1Y1(0,0);
			
			linkButts = new LinkButt[e.links.length];
			
			for (int li = 0; li < linkButts.length; li++) {
				linkButts[li] = new LinkButt(f, width-32, e, e.links[li], all);
			}
			
			
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;
			
			f.renderFromRow(e.text, x, y, body().width()-32, row, body().height()-8);
			if (hoveredIs()) {
				double m = MButt.clearWheelSpin();
				if (m < 0) {
					row += Math.ceil(-m); 
				}else if(m > 0) {
					row -= Math.ceil(m);
				}
				row = CLAMP.i(row, 0, max);
			}
			super.render(r, ds);
			for (LinkButt b : linkButts) {
				
				int y1 = y -row*f.height() + b.offY;
				if (y1 >= y && y1 + f.height() <= body().y2()) {
					if (hovered == b)
						COLOR.WHITE100.bind();
					else if (b.entry == null)
						GCOLOR.T().IBAD.bind();
					else
						GCOLOR.T().IGOOD.bind();
					int x1 = x + b.offX;
					f.render(r, b.name, x1, y1);
				}
				
			}
			hovered = null;
			COLOR.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			super.hover(mCoo);
			hovered = null;
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;
			for (LinkButt b : linkButts) {
				
				int y1 = y -row*f.height() + b.offY;
				int x1 = x + b.offX;
				if (mCoo.x() >= x1 && mCoo.x() <= x1+b.width)
					if (mCoo.y() >= y1 && mCoo.y() <= y1+f.height()) {
						hovered = b;
						return true;
					}
				
			}
			
			return hoveredIs();
		}
		
		@Override
		public boolean click() {
			hovered = null;
			int x = body().x1();
			int y = body().y1();
			Font f = UI.FONT().M;
			COORDINATE mCoo = VIEW.mouse();
			for (LinkButt b : linkButts) {
				
				int y1 = y -row*f.height() + b.offY;
				int x1 = x + b.offX;
				if (mCoo.x() >= x1 && mCoo.x() <= x1+b.width)
					if (mCoo.y() >= y1 && mCoo.y() <= y1+f.height()) {
						if (b.entry != null)
							VIEW.inters().wiki.set(b.entry);
						return true;
					}
				
			}
			return super.click();
		}
		

		
	}
	
	private static class LinkButt {
		
		private final int offX;
		private final int offY;
		private final int width;
		private final Article entry;
		private final CharSequence name;
		
		LinkButt(Font f, int width, ArticleText e, Link l, LIST<Article> all){
			name = l.name;
			
			this.width = f.getDim(name).x();
			COORDINATE c = f.getLastPosition(e.text, 0, l.position, width, 1.0);
			
			offY = c.y();
			offX = c.x() -this.width;
			
			Article aa = null;
			for (Article ee : all) {
				if (l.key.equalsIgnoreCase(ee.key)) {
					aa = ee;
					break;
				}
			}
			entry = aa;
			
		}

		
	}
	
}
