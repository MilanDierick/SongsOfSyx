package view.sett.ui.minimap;

import java.io.IOException;

import game.GAME;
import game.nobility.Nobility;
import init.C;
import init.boostable.BBoost;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.INT.INTE;
import util.gui.misc.*;
import util.gui.panel.GFrame;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimap.Expansion;

final class UIMiniNobles extends Expansion{

	private final INTE t;
	
	public UIMiniNobles(int index, int y1){
		super(index);
		
		RENDEROBJ[] rows = new RENDEROBJ[GAME.NOBLE().ALL().size()];
		for (Nobility n : GAME.NOBLE().ALL()){
			
			rows[n.index()] = noble(n);
		}
		
		int width = rows[0].body().width();

		
		GFrame f = new GFrame();
		f.body().setWidth(width);
		f.body().setHeight(C.HEIGHT()-y1);
		f.body().moveX2(C.WIDTH());
		f.body().moveY1(y1);
		add(f);
		
		RENDEROBJ c;
		c = new GButt.Glow(UI.decor().up) {
			@Override
			protected void renAction() {
				activeSet(t.get() > 0);
			}
			@Override
			protected void clickA() {
				t.inc(-1);
			}
		};
		c.body().centerX(this);
		c.body().moveY1(body().y1());
		add(c);
		
		GScrollRows sc = new GScrollRows(rows, C.HEIGHT()-y1-c.body().height()*2, 0, false);
		addDownC(0, sc.view());
		
		c = new GButt.Glow(UI.decor().down) {
			@Override
			protected void renAction() {
				activeSet(t.get() != t.max());
			}
			@Override
			protected void clickA() {
				t.inc(1);
			}
		};
		addDownC(0, c);
		
		t = sc.target;
		

		
	}
	

	
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (visableIs()) {
			COLOR.WHITE15.render(r, body());
			super.render(r, ds);
		}
	}
	
	private final static CharSequence sExpla = "A nobility is elevated by clicking a subject and pushing the nobility button on the subject's panel. You need 1000 total standing per nobility you appoint. A nobleman holds office for life.";
	
	private static RENDEROBJ noble(Nobility res) {
		
		
		
		return new CLICKABLE.ClickableAbs(C.SG*40+4, C.SG*68) {
			private int yoff = 52;
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				COLOR.WHITE30.render(r, body());
				if (isHovered || (res.subject() != null && VIEW.s().ui.subjects.shows(res.subject())))
					ColorImp.TMP.set(res.color()).shadeSelf(1.5).render(r, body(), -1);
				else
					ColorImp.TMP.set(res.color()).shadeSelf(1.2).render(r, body(), -1);
				
				if (res.subject() != null) {
					STATS.APPEARANCE().portraitRender(r, res.subject().indu(), body().x1()+2, body().y1(), 1);
					
					GMeter.render(r, GMeter.C_GREENRED, res.happiness(), body().x1()+4, body().x2()-4, body().y1()+yoff, body().y1()+yoff+6);
					GMeter.render(r, GMeter.C_BLUE, res.skill(), body().x1()+4, body().x2()-4, body().y1()+yoff+7, body().y1()+yoff+13);
				}else {
					SPRITES.icons().l.mysteryman.renderC(r, body().cX(), body().y1() + C.SG*24);
				}
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(res.info().name);
				b.text(res.info().desc);
				if (res.subject() == null) {
					b.NL();
					b.error(sExpla);
				}else {
					b.NL(8);
					res.subject().hover(b);
				}
				
				b.NL(8);
				
				for (BBoost bo : res.BOOSTER.boosts()) {
					bo.hover(text);
					
				}
				
				b.NL(8);
//				
//				b.add(b.text().lablifySub().add(STANDINGS.CITIZEN().info().name));
//				b.add(GFORMAT.perc(b.text(),res.happiness()));
//				
				b.add(GFORMAT.perc(b.text(),res.skill()));
//				
				
				
			}
			
			@Override
			protected void clickA() {
				if (res.subject() != null)
				VIEW.s().ui.subjects.showSingle(res.subject());
				super.clickA();
			}
		};
		
		
	}

	@Override
	public void save(FilePutter file) {
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		
	}
	

}
