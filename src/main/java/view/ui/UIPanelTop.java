package view.ui;

import game.faction.FACTIONS;
import game.time.TIME;
import init.C;
import init.settings.S;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.dic.*;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GFrame;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.main.VIEW;

public class UIPanelTop extends Interrupter {

	public static final int WIDTH = C.WIDTH();
	public static final int HEIGHT = ICON.MEDIUM.SIZE*2+3;

	private final GuiSection section = new GuiSection();
	private final GuiSection time;
	private final GuiSection right = new GuiSection();
	public UIPanelTop(InterManager manager) {

		this(manager, false);
	}

	public UIPanelTop(InterManager manager, boolean battleview) {

		pin();
		section.body().setDim(WIDTH, HEIGHT );
		section.body().moveX2(C.WIDTH());
		section.body().moveY1(0);

		time = SPRITES.specials().buildTimeThing(battleview);
		time.body().centerX(section.body());
		time.body().moveY1(battleview ? 6 : 0);

		

		if (!battleview) {
			right.addRightC(0, new Butt(FACTIONS.player().banner().MEDIUM) {
				@Override
				protected void clickA() {
					VIEW.UI().level.activate();
				}

				@Override
				protected void renAction() {
					selectedSet(VIEW.UI().level.isActivated());
					if (!selectedIs() && !hoveredIs()) {
						if (FACTIONS.player().titles.hasNew()) {
							bg(COLOR.WHITE2WHITE);
						}else {
							bgClear();
						}
					}
					activeSet(SETT.exists());
				};
			}.hoverInfoSet(VIEW.UI().level.¤¤Name));

			right.addRightC(0, getButt());
		}
		COLOR cView = new ColorImp(0, 47, 47);

		if (!battleview) {
			right.addRightC(8, new Butt(SPRITES.icons().m.city) {
				@Override
				protected void clickA() {
					VIEW.s().activate();
				}

				@Override
				protected void renAction() {
					selectedSet(VIEW.s().isActive());
					activeSet(SETT.exists());
				};
			}.bg(cView).hoverInfoSet(DicGeo.¤¤Capitol));
			right.addRightC(0, new Butt(SPRITES.icons().m.shield) {
				@Override
				protected void clickA() {
					VIEW.s().battle.activate();
				}

				@Override
				protected void renAction() {
					selectedSet(VIEW.s().battle.isActive());
					activeSet(SETT.exists());
				};
			}.bg(cView).hoverInfoSet(DicArmy.¤¤Battle));
			right.addRightC(0, new Butt(SPRITES.icons().m.map) {
				@Override
				protected void clickA() {
					VIEW.world().activate();
				}

				@Override
				protected void renAction() {
					selectedSet(VIEW.world().isActive());
					activeSet(SETT.exists());
				};
			}.bg(cView).hoverInfoSet(DicGeo.¤¤World));
		}
		right.addRightC(0, new Butt(SPRITES.icons().m.questionmark) {
			@Override
			protected void clickA() {
				VIEW.inters().wiki.activate();
			};
		}.bg(cView).hoverInfoSet(view.wiki.WIKI.¤¤name));
		

		if (S.get().developer) {
			right.addRightC(0, new Butt(SPRITES.icons().s.cog) {
				@Override
				protected void clickA() {
					VIEW.inters().debugpanel.show();
				}

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					selectedSet(VIEW.inters().debugpanel.isActivated());
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			}.hoverInfoSet("developer-tools"));
			
		}
		right.addRightC(8, new Butt(SPRITES.icons().m.menu) {
			@Override
			public void clickA() {
				VIEW.inters().menu.show();
			}
		}.hoverInfoSet(DicMisc.¤¤Menu));

		right.body().centerIn(section);
		right.body().moveX2(C.WIDTH() - 4);
		right.body().moveY1(1);

		section.add(right);

		show(manager);
	}

	public void addNoti() {
		UINotifications noti = new UINotifications();
		noti.body().moveX1Y1(section.body().cX() + 110, 0);
		noti.body().centerY(0, HEIGHT);
		section.add(noti);
	}

	@Override
	public boolean render(Renderer r, float ds) {

		manager().viewPort().moveY1(section.body().y2());
		if (manager().viewPort().y2() > C.HEIGHT()) {
			manager().viewPort().setHeight(C.HEIGHT() - section.body().height());
		}

		COLOR.WHITE15.render(r, section.body());
		section.render(r, ds);
		GFrame.renderHorizontal(r, 0, C.WIDTH(), section.body().y2() - 3);
		time.render(r, ds);
		return true;
	}

	public void hide(boolean yes) {
		section.visableSet(yes);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo) | time.hover(mCoo) || mCoo.touchesRec(section);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			section.click();
			time.click();
		}
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
		time.hoverInfoGet(text);
	}

	@Override
	protected boolean update(float ds) {

		return true;
	}

	public static int y2() {
		return 48 - 2;
	}

	private static class Butt extends GButt.ButtPanel{
		
		public Butt(SPRITE label) {
			super(label);
			body.setHeight(HEIGHT);
			body.setWidth(38);
		}
	}
	
	private static CLICKABLE getButt() {
		CLICKABLE b = new Butt(SPRITES.icons().m.openscroll) {
			private Text nr = new Text(UI.FONT().M, 10);

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				if (VIEW.b().isActive())
					return;
				isActive = VIEW.messages().size() > 0;
				activeSet(isActive);
				isSelected = VIEW.messages().activated();

				if (VIEW.messages().unread() > 0) {
					bg(GCOLOR.UI().goodFlash());
					super.render(r, ds, isActive, isSelected, isHovered);
					nr.clear().add(VIEW.messages().unread()).adjustWidth();
					if (!isHovered && TIME.currentSecond() - VIEW.messages().currentSecond() < 3) {
						COLOR.WHITE2WHITE.bind();
						bg(COLOR.BLUE2BLUE);

					}

					int x = body().x1() + (body.width() - nr.width()) / 2;
					int y = body().y1() + (body.height() - nr.height()) / 2;

					COLOR.WHITE100.bind();
					nr.render(r, x - 1, y - 1);
					COLOR.RED50.bind();
					nr.render(r, x, y);
					COLOR.unbind();
				} else {
					bgClear();
					super.render(r, ds, isActive, isSelected, isHovered);
				}

			}

			@Override
			protected void clickA() {
				if (!VIEW.b().isActive())
					VIEW.inters().messages.activate();
			}
		};
		b.hoverInfoSet(DicMisc.¤¤Messages);
		return b;
	}

	public void addLeft(GuiSection s) {
		s.body().moveX1(4);
		s.body().centerY(section.body());
		section.add(s);
	}
	
	public void addRightDown(GuiSection s) {
		s.body().moveX2(C.WIDTH()-4);
		s.body().moveY1(right.body().y2());
		section.add(s);
	}

	public void addRight(GuiSection s) {
		s.body().moveX1(time.body().x2() + 32);
		s.body().centerY(section.body());
		section.add(s);
	}

}
