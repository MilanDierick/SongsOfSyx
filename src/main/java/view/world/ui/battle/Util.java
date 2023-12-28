package view.world.ui.battle;

import init.config.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.*;
import util.data.INT.INTE;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.misc.GButt.ButtPanel;
import util.gui.slider.GSliderInt;
import util.gui.table.*;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import world.battle.spec.WBattleSide;
import world.battle.spec.WBattleUnit;

class Util {

	public static final int width = 600;

	public static RENDEROBJ confict(GETTER<WBattleSide> player, GETTER<WBattleSide> enemy, DOUBLE_O<WBattleUnit> losses,
			DOUBLE_O<WBattleUnit> el) {

		GuiSection s = new GuiSection();
		s.add(cSide(player, losses));
		s.add(cSide(enemy, el), width / 2, 0);
		return s;
	}

	private static RENDEROBJ cSide(GETTER<WBattleSide> player, DOUBLE_O<WBattleUnit> losses) {
		GTableBuilder bu = new GTableBuilder() {

			@Override
			public int nrOFEntries() {
				if (player.get() == null)
					return 0;
				return player.get().units.size();
			}
		};

		bu.column(null, Unit.width, new GRowBuilder() {

			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Unit(losses, player, ier);
			}
		});

		return bu.create(5, false);
	}

	public static RENDEROBJ result(WBattleSide player, WBattleSide enemy) {

		GuiSection s = new GuiSection();
		s.add(rSide(player));
		s.add(rSide(enemy), width / 2, 0);
		return s;
	}

	private static RENDEROBJ rSide(WBattleSide player) {

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		for (int i = 0; i < player.units.size(); i++) {
			rows.add(new UnitLoss(player.units.get(i)));
		}

		return new GScrollRows(rows, rows.get(0).body().height() * 5).view();
	}

	public static GuiSection balance(GETTER<WBattleSide> player, GETTER<WBattleSide> enemy) {

		GuiSection s = new GuiSection();

		RENDEROBJ gg = new HOVERABLE.HoverableAbs(200, Icon.M) {

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

				double d = player.get().powerBalance;
				if (d < 0.5)
					GMeter.render(r, GMeter.C_RED, d, body);
				else
					GMeter.render(r, GMeter.C_BLUE, d, body);
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(DicArmy.¤¤Balance);
			}
		};

		s.addDownC(4, gg);
		s.addC(UI.icons().l.rebel, s.body().cX(), s.body().cY());

		int y1 = s.body().cY() - 12;

		s.add(new GStat(UI.FONT().M) {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, player.get().men);
			}
		}.r(DIR.NE), s.body().x1() - 80, y1);

		s.add(new GStat(UI.FONT().M) {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, enemy.get().men);
			}
		}.r(DIR.NW), s.body().x2() + 80, y1);

		return s;

	}

	private static class Unit extends GuiSection {

		private static int width = 300 - 50;
		private final GETTER<WBattleUnit> ier;

		public Unit(DOUBLE_O<WBattleUnit> losses, GETTER<WBattleSide> side, GETTER<Integer> ii) {
			this.ier = new GETTER<WBattleUnit>() {

				@Override
				public WBattleUnit get() {

					return side.get().units.get(ii.get());
				}

			};
			addRightC(8, new SPRITE.Imp(Icon.M) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WBattleUnit u = ier.get();
					if (u == null || u.icon == null)
						return;
					u.icon.render(r, X1, X2, Y1, Y2);

				}
			});

			addRightC(8, new SPRITE.Imp(200, 16) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WBattleUnit u = ier.get();
					if (u == null)
						return;
					double dmen = Math.sqrt((double) u.men / Config.BATTLE.MEN_PER_ARMY);
					X2 = (int) (X1 + (X2 - X1) * dmen);

					double d = (double) (u.men - losses.getD(u)) / u.men;
					;
					GMeter.render(r, GMeter.C_REDGREEN, d, X1, X2, Y1, Y2);
				}
			});

			body().setWidth(width);

		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WBattleUnit u = ier.get();
			if (u == null)
				return;

			u.hover(text);
			//
			// if (u.a() != null) {
			// VIEW.world().UI.armies.hover(text, u.a());
			// }else if (u.r() != null) {
			// VIEW.world().UI.regions.hoverGarrison(u.r(), text);
			// }else {
			// GBox b = (GBox) text;
			// b.title(u.name());
			// b.add(UI.icons().s.human);
			// b.add(GFORMAT.i(b.text(), u.men()));
			// }
		}

	}

	private static class UnitLoss extends GuiSection {

		private static int width = 300 - 50;
		private final WBattleUnit u;

		public UnitLoss(WBattleUnit u) {
			this.u = u;

			addRightC(8, new SPRITE.Imp(Icon.M) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					u.icon.render(r, X1, X2, Y1, Y2);
				}
			});

			addRightC(8, new SPRITE.Imp(200, 16) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					double dmen = Math.sqrt((double) u.men / Config.BATTLE.MEN_PER_ARMY);
					X2 = (int) (X1 + (X2 - X1) * dmen);

					double d = (double) (u.men - u.losses) / u.men;
					GMeter.render(r, GMeter.C_ORANGE, d, X1, X2, Y1, Y2);
				}
			});

			body().setWidth(width);

		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			u.hover(text);
			GBox b = (GBox) text;
			b.NL(16);
			b.text(DicArmy.¤¤Losses);
			b.add(GFORMAT.iofk(b.text(), u.losses, u.men));
		}

	}

	public static class Spoils extends GuiSection {

		private int[] accepted = new int[RESOURCES.ALL().size()];
		private final int[] available;
		private final DOUBLE mul;
		
		
		Spoils(int[] resources, DOUBLE mul) {
			this.available = resources;
			this.mul = mul;
			int am = 4;
			GRows rows = new GRows(am);
			GText t = new GText(UI.FONT().S, 16);
			for (RESOURCE res : RESOURCES.ALL()) {
				if (resources[res.index()] != 0) {

					rows.add(new HOVERABLE.HoverableAbs(width / am - 12, 28) {

						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							res.icon().medium.renderCY(r, body().x1() + 8, body().cY());
							t.clear();
							GFORMAT.iIncr(t, (long) (resources[res.index()] * mul.getD()));
							t.renderCY(r, body().x1() + 40, body().cY());
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.title(res.names);
						}
					});

				}
			}
			add(new GScrollRows(rows.rows(), 28 * 4).view());
		}

		public int[] accepted() {
			for (int i = 0; i < accepted.length; i++) {
				accepted[i] = CLAMP.i((int) (available[i] * mul.getD()), 0, available[i]);
			}
			return accepted;
		}
	}

	public static class Slaves extends GuiSection {

		private int[] accepted = new int[RACES.all().size()];
		private final DOUBLE mul;
		private final int[] available;

		public Slaves(int[] available, DOUBLE mul) {
			this.mul = mul;
			this.available = available;
			// add(new GHeader(DicArmy.¤¤Captives).hoverInfoSet(DicArmy.¤¤CaptivesD));
			//
			int am = 2;
			GRows rows = new GRows(am);

			for (Race race : RACES.all()) {

				accepted[race.index] = available[race.index];

				INTE in = new INTE() {

					@Override
					public int min() {
						return 0;
					}

					@Override
					public int max() {
						return (int) (available[race.index] * mul.getD());
					}

					@Override
					public int get() {
						return CLAMP.i(accepted[race.index], 0, max());
					}

					@Override
					public void set(int t) {
						accepted[race.index] = t;

					}
				};

				GSliderInt t = new GSliderInt(in, 200 - 24, active) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(race.info.names);
						super.hoverInfoGet(text);
						text.NL();
						text.text(DicArmy.¤¤CaptivesD);
					}
				};

				t.addRelBody(4, DIR.W, race.appearance().icon.medium);
				t.body().incrW(24);
				t.pad(0, 2);
				rows.add(t);
			}

			addRelBody(4, DIR.S, new GScrollRows(rows.rows(), 28 * 4).view());
		}

		public int[] accepted() {
			for (int i = 0; i < accepted.length; i++) {
				accepted[i] = CLAMP.i(accepted[i], 0, (int) (available[i] * mul.getD()));
			}
			return accepted;
		}

	}

	public static class BButt extends ButtPanel {

		BButt(SPRITE icon, CharSequence name) {
			super(name);
			icon(icon);
			setDim(200);
		}

	}

}
