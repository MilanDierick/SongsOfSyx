package util.gui.misc;

import game.GAME;
import init.C;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Text;
import util.gui.panel.GPanelS;
import util.info.GFORMAT;
import util.info.INFO;

public class GBox implements SPRITE, GUI_BOX{

	private final static GBox dummy = new GBox();
	
	private final static int MARGIN = 4;
	private int dx,dy;
	private int dHeight;
	private int width,height;
	
	private ArrayList<Ren> rens = new ArrayList<>(64*16);
	private ArrayList<Ren> rensFree = new ArrayList<>(64*16);
	private ArrayList<GText> texts = new ArrayList<>(64*16);
	private int rensFreeI = 0;
	private int textsFreeI = 0;
	private final GPanelS box = new GPanelS();
	
	private RENDEROBJ.RenderImp object = new RENDEROBJ.RenderImp() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GBox.this.render(r, body);
		}
	};
	
	public GBox() {
		while(rensFree.hasRoom()) {
			rensFree.add(new Ren());
			texts.add(new GText(UI.FONT().S, 256));
		}
	}
	
	public void clear() {
		rens.clear();
		dx = 0;
		dy = 0;
		dHeight = 0;
		rensFreeI = 0;
		textsFreeI = 0;
		width = 0;
		height = 0;
		box.titleClear();
	}
	
	private final class Ren {
		
		private int x,y;
		private SPRITE renderable;
		private RENDEROBJ ren;
		private final ColorImp col = new ColorImp();
		
//		private void init(SPRITE o) {
//			init(o, o.width()+MARGIN);
//		}
		
		int height() {
			if (renderable != null)
				return renderable.height();
			return ren.body().height();
		}
		
		private void init(SPRITE o, int fixed) {
			renderable = o;
			x = dx;
			y = dy;
			dx += fixed; 
			if (o.height() > dHeight)
				dHeight = o.height();
			if (x + o.width() > width)
				width = x+o.width();
			
			col.set(COLOR.WHITE100);
			rensFreeI ++;
			rens.add(this);
			ren = null;
		}
		
		private void init(RENDEROBJ o, int fixed) {
			renderable = null;
			ren = o;
			x = dx;
			y = dy;
			dx += fixed; 
			if (o.body().height() > dHeight)
				dHeight = o.body().height();
			if (x + o.body().width() > width)
				width = x+o.body().width();
			
			rensFreeI ++;
			rens.add(this);
			col.set(COLOR.WHITE100);
		}
		
	}
	
	@Override
	public GBox title(CharSequence title) {
		box.setTitle(title);
		return this;
	}
	
	@Override
	public GBox NL() {
		return NL(0);
	}
	
	@Override
	public GBox NL(int m) {
		
		for (int i = rens.size()-1; i >= 0; i--) {
			Ren r = rens.get(i);
			if (r.y != dy)
				break;
			if (r.height() != dHeight) {
				r.y += (dHeight-r.height())/2;
			}
		}
		
		dHeight += m;
		
		dy += dHeight;
		dx = 0;
		height += dHeight;
		dHeight = 0;
		return this;
	}
	
	public GBox tab(int tabs) {
		dx = 10*MARGIN*tabs;
		return this;
	}
	
	@Override
	public GBox space() {
		dx += 3*MARGIN;
		return this;
	}
	
	public GBox space(int size) {
		dx += size;
		return this;
	}
	
	@Override
	public GText text() {
		GText t;
		if (textsFreeI >= texts.size()) {
			t = texts.get(0);
		}else {
			t = texts.get(textsFreeI);
			textsFreeI ++;
		}
		
		t.clear();
		t.setFont(UI.FONT().S);
		t.setMaxWidth(C.SG*200);
		t.normalify();
		return t;
	}
	
//	private void increase() {
//		ArrayList<Ren> rens = new ArrayList<>(this.rens.size()*2);
//		ArrayList<Ren> rensFree = new ArrayList<>(this.rensFree.size()*2);
//		ArrayList<GText> texts = new ArrayList<>(this.texts.size()*2);
//		
//		rens.add(this.rens);
//		rensFree.add(this.rensFree);
//		texts.add(this.texts);
//		
//		while(rensFree.hasRoom()) {
//			rensFree.add(new Ren());
//			texts.add(new GText(UI.FONT().S, 256));
//		}
//		
//		this.rens = rens;
//		this.rensFree = rensFree;
//		this.texts = texts;
//		
//	}
	
	public GBox textLL(CharSequence t) {
		GText tt = text();
		if (tt == null)
			return this;
		tt.lablify().add(t);
		add(tt);
		return this;
	}
	
	public GBox textL(CharSequence t) {
		GText tt = text();
		tt.lablifySub().add(t);
		add(tt);
		return this;
	}
	
	public GText textS(CharSequence te) {
		if (textsFreeI >= texts.size())
			return null;
		GText t = texts.get(textsFreeI);
		textsFreeI ++;
		
		t.clear();
		t.setFont(UI.FONT().S);
		t.setMaxWidth(C.SG*200);
		t.normalify();
		t.add(te);
		add(t);
		return t;
	}
	
	public GBox textSLL(CharSequence t) {
		GText tt = textS(t);
		tt.lablify();
		return this;
	}
	
	public GBox textSL(CharSequence t) {
		GText tt = textS(t);
		tt.lablifySub();
		return this;
	}

	@Override
	public GBox add(SPRITE s) {
		if (s instanceof Text) {
			((Text) s).setMaxWidth(500);
			((Text) s).adjustWidth();
		}
		return add(s, s.width()+MARGIN);
	}
	
	public GBox add(SPRITE s, COLOR col) {
		add(s);
		rens.get(rens.size()-1).col.set(col);
		return this;
	}
	
	@Override
	public GBox add(SPRITE s, int width) {
		
		if (rensFreeI >= rensFree.size()) {
			GAME.Notify("" + rensFreeI);
			return this;
		}
		rensFree.get(rensFreeI).init(s, width);
		rensFreeI++;
		return this;
	}
	
	public void rewind() {
		if (rens.size() == 0)
			return;
		dx = rens.get(rens.size()-1).x;
	}
	
	public void rewind(int am) {
		dx -= am;
	}
	
	public void debug() {
		LOG.ln(rensFree);
	}
	
	@Override
	public GBox add(RENDEROBJ obj) {
		if (rensFreeI >= rensFree.size()) {
			GAME.Notify("" + rensFreeI);
			return this;
		}
		rensFree.get(rensFreeI).init(obj, obj.body().width()+MARGIN);
		rensFreeI++;
		return this;
	}
	
	public GBox add(INFO info) {
		title(info.name);
		text(info.desc);
		NL(4);
		return this;
	}
	
	public GBox setArea(RECTANGLE b) {
		if (b.width() > 1 || b.height() > 1) {
			GText t = text();
			t.add(b.width()).add('x').add(b.height()).adjustWidth();
			add(t);
			space();	
		}
		return this;
	}
	
	public GBox setResource(RESOURCE r, double amount) {
		if (rensFree.isEmpty()) {
			GAME.Notify(r.name);
			return this;
		}
		if (amount == 0)
			return this;
		add(r.icon().small);
		GText t = text();
		if (amount - (int) amount == 0)
			t.add((int)amount).adjustWidth();
		else
			t.add(amount).adjustWidth();
		if (!SETT.PATH().finders.resource.normal.has(r))
			t.errorify();
		add(t);
		return this;
	}
	
	public GBox resLine(RESOURCE r, double amount) {
		if (rensFree.isEmpty()) {
			GAME.Notify(r.name);
			return this;
		}
		if (amount == 0)
			return this;
		add(r.icon().small);
		text(r.names);
		tab(6);
		GFORMAT.f0(text(), amount);
		NL();
		return this;
	}
	
	public GBox setResource(RESOURCE r, int amount, int of) {
		add(r.icon().small);
		GText t = text();
		GFORMAT.iofkInv(t, amount, of);
		if (!SETT.PATH().finders.resource.normal.has(r) && amount < of)
			t.errorify();
		else
			t.normalify();
		add(t);
		return this;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height + dHeight;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		
		
		if (dHeight != 0)
			NL();

		if (width == 0 || height == 0) {
			box.inner().moveX1(X1);
			box.inner().moveY1(Y1);
			box.renderTitle(r);
			return;
			
		}
			
		
		box.inner().setWidth(width).setHeight(height);
		box.inner().moveX1(X1);
		box.inner().moveY1(Y1);
		box.render(r, 0);
		
		renderWithout(r, X1, Y1);
		
	}
	
	public void renderWithout(SPRITE_RENDERER r, int X1, int Y1){
		for (Ren ren : rens) {
			if (ren.renderable != null) {
				ren.col.bind();
				ren.renderable.render(r, X1+ren.x, Y1+ren.y);
				COLOR.unbind();
			}
				
			else {
				RENDEROBJ o = ren.ren;
				
				o.body().moveX1Y1(X1+ren.x, Y1+ren.y);
				o.render(r, 0);
				
			}
		}
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		throw new RuntimeException();
	}
	
	public RENDEROBJ asRenObj() {
		if (dHeight != 0)
			NL();
		object.body().setWidth(width).setHeight(height);
		return object;
	}
	
	public static GBox Dummy() {
		dummy.clear();
		return dummy;
	}

	public void error(CharSequence s) {
		if (s.length() > 0) {
			add(text().errorify().add(s));
			NL();
		}
	}

	@Override
	public boolean emptyIs() {
		return rensFreeI == 0 && box.title().length() == 0;
	}
	
	public boolean emptyIs2() {
		return rensFreeI == 0;
	}
	
	public static interface HovInfo {
		
		void hover(GBox b);
		
	}


}
