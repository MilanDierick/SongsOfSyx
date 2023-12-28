package view.sett.ui.standing;

import game.time.TIME;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StandingSlave;
import settlement.stats.standing.StandingSlave.SlaveFactor;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import view.interrupter.ISidePanel;
import view.sett.ui.standing.Cats.Cat;
import view.sett.ui.standing.decree.UIDecreeButt;

public final class UISlaves extends ISidePanel {

	private final INT.IntImp hov = new INT.IntImp();
	
	public UISlaves() {
		titleSet(HCLASS.SLAVE.names);
		//section.add(numbers());
		section.addDownC(8, obidience());
		
		Cats cats = new Cats(HCLASS.SLAVE);
		
		ArrayList<RENDEROBJ> rens = new ArrayList<>(STATS.COLLECTIONS().size());
		for (Cat c : cats.all)
			rens.add(new CatButt(cats, c, HCLASS.SLAVE, hov));
		
		GScrollRows r = new GScrollRows(rens, HEIGHT - section.body().y2()-4, 0);
		section.addRelBody(16, DIR.S, r.view());
		
		//section.addDownC(32, new GScrollRows(stats(), HEIGHT-section.getLastY2()-32, 0).view());
	}


	private RENDEROBJ obidience() {
		GuiSection s = new GuiSection();

		int width = 180;

		StandingSlave h = STANDINGS.SLAVE();

		{
			GuiSection ss = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					if (!text.emptyIs())
						return;
					
					GBox b = (GBox) text;
					b.title(h.info().name);
					b.text(h.info().desc);
					b.NL(8);
					
					b.textLL(DicMisc.¤¤Current);
					b.add(GFORMAT.perc(b.text(), h.submission.getD()));
					b.add(SPRITES.icons().s.arrow_right);
					b.textLL(DicMisc.¤¤Target);
					b.add(GFORMAT.perc(b.text(), h.targetSubmission.getD()));
					b.NL(8);
					
					for (SlaveFactor f : h.factors) {
						b.textLL(f.info().name);
						b.tab(5);
						b.add(GFORMAT.f1d(b.text(), f.getD(0), 3));
						b.NL();
						b.text(f.info().desc);
						b.NL(4);
					}
					
					b.NL(8);
					
					UIDecreeButt.hover(b, HCLASS.SLAVE, null);
					
					
				}
			};
			ss.add(new GHeader(h.info().name));
			ss.addRightCAbs(124, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, h.submission.getD());
				}
			});
			ss.addRightC(32, new RENDEROBJ.RenderImp(Icon.M) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double now = h.submission.getD();
					double t = h.targetSubmission.getD();
					int am = (int) CLAMP.d(Math.abs(now - t) * 10, 0, 1);
					SPRITE a = SPRITES.icons().m.arrow_right;
					GCOLOR.UI().goodFlash().bind();
					if (now > t) {
						GCOLOR.UI().badFlash().bind();
						a = SPRITES.icons().m.arrow_left;
					}
					for (int i = 0; i < am; i++) {
						a.render(r, body().x1() + i * Icon.M, body().y1());
					}
				}
			});

			RENDEROBJ r = new RENDEROBJ.RenderImp(width, 24) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double now = h.submission.getD();
					double t = h.targetSubmission.getD();
					GMeter.renderDelta(r, now, t, body);
				}
			};

			ss.add(r, 0, ss.body().y2()+4);
			
			GStaples st = new GStaples(STATS.DAYS_SAVED) {

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

					isHovered = true;
					setHovered(hov.get());
					super.render(r, ds, isHovered);
				}

				@Override
				protected void hover(GBox box, int stapleI) {
					box.title(h.info().name);
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					box.add(box.text().lablify().add(-fromZero).s().add(TIME.days().cycleName()));
					box.NL();
					double d = h.submission.getD(fromZero);
					box.add(GFORMAT.perc(box.text(), d));
					box.NL(8);
					
					for (SlaveFactor f : h.factors) {
						box.textLL(f.info().name);
						box.tab(5);
						box.add(GFORMAT.f1d(box.text(), f.getD(fromZero), 3));
						box.NL();
						
					}
				}

				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						hov.set(hoverI());
						return true;
					}
					return false;
				}
				
				@Override
				protected double getValue(int stapleI) {
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					return h.submission.getD(fromZero);
				}

				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					c.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD2.hovered, value);
				}
			};
			st.normalize(false);
			st.body().setWidth(7*STATS.DAYS_SAVED);
			st.body().setHeight(64);
			st.body().centerY(ss);
			st.body().moveX1(188);
			ss.add(st);

			s.addDown(2, ss);


		}
		
		{
			GETTER<Race> gg = new GETTER<Race>() {

				@Override
				public Race get() {
					return null;
				}
				
			};
			s.addRelBody(8, DIR.S, new UIDecreeButt(HCLASS.SLAVE, gg));
		}

		return s;
	}
	
	@Override
	protected void update(float ds){
		CitizenMain.current = null;
	}

}
