package view.sett.ui.army;

import static settlement.main.SETT.*;

import game.faction.FACTIONS;
import init.*;
import init.boostable.BOOSTABLE;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.army.DivisionBanners.DivisionBanner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsBoosts.StatBooster;
import settlement.stats.StatsEquippables.StatEquippableBattle;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.*;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.panel.GFrame;
import util.gui.slider.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.common.ArmyPicker;
import view.common.BitmapSpriteEditor;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import world.World;
import world.army.WARMYD;
import world.entity.army.WArmy;

public final class UIArmy extends ISidePanel{

	private static CharSequence ¤¤SoldiersLevelD = "¤The amount of soldiers this division will conscript. If training room spots are available alongside empty division spots, subjects will begin training and eventually join the division. Subjects will prioritize training before jobs.";
	private static CharSequence ¤¤TrainingLevelD = "¤The more soldiers are trained, the deadlier they are on the field. A soldier must gain a minimum training level of 1 to be able to join a division. They will then train until the desired level is reached. training recruits and soldiers do not work. If Training is set to max, soldiers will train in perpetuity and thus be professional soldiers.";
	private static CharSequence ¤¤LowSupplies = "¤Not enough supplies to send this division. Fill up your Army Supply depots!";
	private static CharSequence ¤¤Recall = "¤Recall";
	private static CharSequence ¤¤RecallD = "¤Recall this division from its army and have it return to the city.";
	private static CharSequence ¤¤SendOut = "¤Send Out";
	private static CharSequence ¤¤SendOutD = "¤Send this division to join an army on the world map.";
	private static CharSequence ¤¤NotTrained = "¤Some of the soldiers of this division are not fully trained to specification and will continue to train before they join an army.";
	private static CharSequence ¤¤NoArmies = "¤there are no armies to send this division to. Recruit one on the world map.";
	private static CharSequence ¤¤RecruitD = "¤The amount of men currently training to be able to join a division.";
	
	private static CharSequence ¤¤SoldierD = "The amount of soldiers that are ready to be deployed in your city.";
	private final Banner appearence = new Banner();
	private static int equipwidth = 90;
	
	public UIArmy(ArmyManager m){
		D.t(this);
		titleSet(DicArmy.¤¤Conscripts);
		int y = summery(m);
		table(y, m);
		IDebugPanelSett.add(new FormationDebugPlacer(ARMIES().player()));
		IDebugPanelSett.add(new FormationDebugPlacer(ARMIES().enemy()));
		
		
	}
	
	private int summery(ArmyManager m) {
		//title().add("Army");
		
		GuiSection section = new GuiSection();
		this.section.add(section);
		
		
		return this.section.body().y2();

		
	}
	
	private void table(int y1, ArmyManager m) {

		RENDEROBJ[] rows = new RENDEROBJ[ RES.config().BATTLE.DIVISIONS_PER_ARMY];
		for (int i = 0; i < RES.config().BATTLE.DIVISIONS_PER_ARMY; i++) {
			rows[i] = new DivSection(m.player().divisions().get(i), m, appearence);
		}
		
		
		GuiSection s = new GuiSection();
		s.body().incrW(1).incrH(1);
		
		s.addCentredX(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iBig(text, (int)World.ARMIES().cityDivs().total());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Campaigning);
				b.text(DicArmy.¤¤CampaigningD);
				b.NL();
				b.add(GFORMAT.iBig(b.text(), (int)World.ARMIES().cityDivs().total()));
			};
		}.hv(SPRITES.icons().m.arrow_left), equipwidth/2);
		
		s.addCentredX(new GStat() {
		
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, (int)STATS.BATTLE().DIV.stat().data().get(null, 0), ARMIES().info.targetMen());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(DicArmy.¤¤Soldiers);
				b.text(¤¤SoldierD);
				b.NL();
				b.add(GFORMAT.iBig(b.text(), (int)STATS.BATTLE().DIV.stat().data().get(null, 0)));
				b.NL(4);
				
				b.textLL(DicArmy.¤¤Recruits);
				b.add(GFORMAT.iBig(b.text(), (int)STATS.BATTLE().RECRUIT.stat().data().get(null, 0)));
				b.NL();
				b.text(¤¤RecruitD);
			
			};
		}.hv(SPRITES.icons().m.sword), 180);
		
		s.addCentredX(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, SETT.ROOMS().BARRACKS.employment().employed(), SETT.ROOMS().BARRACKS.employment().neededWorkers());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(STATS.BATTLE().TRAINING_MELEE.info().name);
				b.text(STATS.BATTLE().TRAINING_MELEE.info().desc);
				b.NL(4);
				b.textLL(SETT.ROOMS().BARRACKS.info.names);
				b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().BARRACKS.employment().employed(), SETT.ROOMS().BARRACKS.employment().neededWorkers()));
				
				b.NL(8);
				b.NL();
				for (StatBooster bo : STATS.BATTLE().TRAINING_MELEE.boosts()) {
					bo.boost.hover(b);
					b.NL();
				}
			};
		}.hv(SETT.ROOMS().BARRACKS.iconBig().nomal), 418);
		
		s.addRightCAbs(equipwidth, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, SETT.ROOMS().ARCHERY.employment().employed(), SETT.ROOMS().ARCHERY.employment().neededWorkers());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(STATS.BATTLE().TRAINING_ARCHERY.info().name);
				b.text(STATS.BATTLE().TRAINING_ARCHERY.info().desc);
				b.NL(4);
				b.textLL(SETT.ROOMS().ARCHERY.info.names);
				b.add(GFORMAT.iofk(b.text(), SETT.ROOMS().ARCHERY.employment().employed(), SETT.ROOMS().ARCHERY.employment().neededWorkers()));
				
				b.NL(8);
				b.NL();
				for (StatBooster bo : STATS.BATTLE().TRAINING_ARCHERY.boosts()) {
					bo.boost.hover(b);
					b.NL();
				}
			};
		}.hv(SETT.ROOMS().ARCHERY.iconBig().nomal));
		
		
		for (StatEquippableBattle e : STATS.EQUIP().military()){
			
			GGauge g = new GGauge(40, 16, GMeter.C_REDGREEN) {
				
				@Override
				public double getD() {
					int needs = 0;
					double has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men.get()*e.target(d);
					}
					return CLAMP.d(has/needs, 0, 1);
				}
			};
			s.addRightCAbs(equipwidth, new CLICKABLE.Pair(new RENDEROBJ.Sprite(e.resource.icon()), new RENDEROBJ.Sprite(g), DIR.S, 4) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.stat().info().name);
					b.text(e.stat().info().desc);
					b.NL(4);
					int needs = 0;
					int has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men.get()*e.target(d);
					}
					b.add(GFORMAT.iofkInv(b.text(), has, needs));
					b.NL(4);
					b.textLL(BOOSTABLES.INFO().name);
					b.NL(4);
					for (StatBooster bo : e.boosts()) {
						bo.boost.hover(b);
						b.NL();
					}
				}
			});
			
			
		}
		
		for (StatEquippableRange e : STATS.EQUIP().ammo()){
			GGauge g = new GGauge(48, 16, GMeter.C_REDGREEN) {
				
				@Override
				public double getD() {
					int needs = 0;
					double has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men.get()*e.target(d);
					}
					return CLAMP.d(has/needs, 0, 1);
				}
			};
			
			s.addRightCAbs(equipwidth, new CLICKABLE.Pair(new RENDEROBJ.Sprite(e.resource.icon()), new RENDEROBJ.Sprite(g), DIR.S, 4) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.stat().info().name);
					b.text(e.stat().info().desc);
					b.NL(4);
					int needs = 0;
					int has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men.get()*e.target(d);
					}
					b.add(GFORMAT.iofkInv(b.text(), has, needs));
					b.NL(4);
					e.hover(text);
				}
			});
			
		}
		
		s.add(GFrame.separator(rows[0].body().width()), s.body().x1(), s.body().y1()-10);
		s.add(GFrame.separator(rows[0].body().width()), s.body().x1(), s.body().y2()+4);
//		s.add(new GText(UI.FONT().H2, DicArmy.¤¤Division).lablifySub(), 0, 0);
//		s.addCentredX(new GText(UI.FONT().H2, DicArmy.¤¤Soldiers).lablifySub().r(DIR.C).hoverInfoSet(¤¤SoldiersLevelD), 313);
//		s.addCentredX(new GText(UI.FONT().H2, RACES.name()).lablifySub().r(DIR.C).hoverInfoSet(¤¤SpeciesD), 463);
//		s.addCentredX(new HOVERABLE.Sprite(SPRITES.icons().m.stength).hoverInfoSet(¤¤TrainingLevelD), 566);
//		s.addCentredX(new HOVERABLE.Sprite(RESOURCES.BATTLEGEAR().icon()).hoverInfoSet(¤¤GearLevelD), 661);
//		s.body().moveY1(section.getLastY2()+4);
		this.section.add(s, 0, this.section.body().y2());
		
		GScrollRows scrolls = new GScrollRows(rows, HEIGHT-this.section.body().y2()-8, 0);
		CLICKABLE sc = scrolls.view();
		sc.body().moveY1(this.section.body().y2()-8);
		this.section.add(sc);
		
	}
	private static Font f = UI.FONT().S;
	private static GText tmp = new GText(UI.FONT().S, 10);
	
	static class DivSection extends GuiSection{
		
		private final static int width = (C.SG*340-C.SG*30);
		private final Div div;
		
		public DivSection(Div div, ArmyManager m, Banner b) {
			this.div = div;
			
			body().setWidth(width);
			
			{
				ArmyPicker p = new ArmyPicker() {
					
					@Override
					protected void pick(WArmy a) {
						World.ARMIES().cityDivs().attach(a, div);
					}
					
					@Override
					protected boolean canBePicked(WArmy a) {
						if (a == null)
							return false;
						return a.divs().canAdd();
					}
				};
				CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
					
					@Override
					protected void clickA() {
						if (World.ARMIES().cityDivs().attachedArmy(div) != null) {
							World.ARMIES().cityDivs().attach(null, div);							
						}else if (World.ARMIES().army(FACTIONS.player()).all().size() > 0 && canSendSupplies(div)) {
							VIEW.inters().popup.show(p, this);
						}
					}
					
					@Override
					protected void renAction() {
						activeSet(true);
						if (World.ARMIES().cityDivs().attachedArmy(div) != null) {
							replaceLabel(SPRITES.icons().m.arrow_right, DIR.C);
							bg(GCOLOR.UI().SOSO.normal);
						}else if (World.ARMIES().army(FACTIONS.player()).all().size() > 0 && canSendSupplies(div)) {
							replaceLabel(SPRITES.icons().m.arrow_left, DIR.C);
							bg(GCOLOR.UI().GOOD.normal);
						}else {
							replaceLabel(SPRITES.icons().m.arrow_left, DIR.C);
							activeSet(false);
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						
						if (World.ARMIES().cityDivs().attachedArmy(div) != null) {
							b.title(¤¤Recall);
							b.text(¤¤RecallD);
							
							if (div.info.men.get() > 0 && (div.info.training.toStat() > STATS.BATTLE().TRAINING_MELEE.div().getD(div) || div.info.trainingR.toStat() > STATS.BATTLE().TRAINING_ARCHERY.div().getD(div))) {
								b.NL();
								b.add(b.text().warnify().add(¤¤NotTrained));
							}
							
						}else if (World.ARMIES().army(FACTIONS.player()).all().size() > 0) {
							b.title(¤¤SendOut);
							b.text(¤¤SendOutD);
							b.NL();
							for (ArmySupply s : RESOURCES.SUP().ALL()) {
								b.add(s.resource.icon());
								b.add(GFORMAT.iIncr(b.text(), -needed(s, div)));
								b.space();
							}
							b.NL();
							if (!canSendSupplies(div)) {
								b.error(¤¤LowSupplies);
							}
							
							if (div.info.training.toStat() > STATS.BATTLE().TRAINING_MELEE.div().getD(div) || div.info.trainingR.toStat() > STATS.BATTLE().TRAINING_ARCHERY.div().getD(div)) {
								b.NL();
								b.add(b.text().warnify().add(¤¤NotTrained));
							}
							
							
							
						}else {
							b.title(¤¤SendOut);
							b.text(¤¤SendOutD);
							b.NL();
							b.error(¤¤NoArmies);
						}
						
						
						super.hoverInfoGet(text);
					}
					
				}.pad(8, 0);
				
				add(c);
			}
			
			{
				SPRITE sp = new SPRITE.Imp(SETT.ARMIES().banners.get(0).width()) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						SETT.ARMIES().banners.get(div.info.symbolI()).render(r, X1, Y1);
					}
				};
				CLICKABLE butt = new GButt.Panel(sp) {
					@Override
					protected void clickA() {
						b.open(div, this);
					}
				};
				
				addRightC(8, butt);
			}
			
			INTE men = new INTE() {
				
				int dmen = 10;
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return (int)Math.ceil((double)RES.config().BATTLE.MEN_PER_DIVISION/dmen);
				}
				
				@Override
				public int get() {
					return (int) Math.ceil(div.info.men.get()/dmen);
				}
				
				@Override
				public void set(int t) {
					div.info.men.set(CLAMP.i(t*dmen, 0, RES.config().BATTLE.MEN_PER_DIVISION));
				}
			};
			
			{
				
				if (f != UI.FONT().S) {
					tmp = new GText(UI.FONT().S, 10);
				}
				
				RENDEROBJ o = new RENDEROBJ.RenderImp(16*5, 20) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						double men = div.info.men.get();
						
						int n = STATS.BATTLE().DIV.stat().div().get(div) + World.ARMIES().cityDivs().get(div).men();
						
						if (men == 0)
							GMeter.renderDelta(r, 0, 0, body);
						else
							GMeter.renderDelta(r, n/men, (n+STATS.BATTLE().RECRUIT.inDiv(div))/men, body);
						tmp.clear();
						tmp.add(div.info.men.get());
						tmp.adjustWidth();
						tmp.renderC(r, body());
					}
				};
				
				
				GTarget t = new GTarget(4, true, true, o, men) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.clear();
						b.textL(DicMisc.¤¤Target);
						b.add(GFORMAT.i(b.text(), div.info.men.get()));
						b.NL();
						b.textL(DicArmy.¤¤Campaigning);
						b.add(GFORMAT.i(b.text(), World.ARMIES().cityDivs().get(div).men()));
						b.NL();
						b.textL(DicArmy.¤¤Soldiers);
						b.add(GFORMAT.i(b.text(), STATS.BATTLE().DIV.stat().div().get(div)));
						b.NL();
						b.textL(DicArmy.¤¤Recruits);
						b.add(GFORMAT.i(b.text(), STATS.BATTLE().RECRUIT.inDiv(div)));

						b.NL();
						b.text(¤¤SoldiersLevelD);
						
					}
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						activeSet(World.ARMIES().cityDivs().attachedArmy(div) == null);
						super.render(r, ds);
					}
					
				};
				
				addRightC(8, t);
				
			
			}
			
			{
				RENDEROBJ  stat = new RENDEROBJ.Sprite(ICON.MEDIUM.SIZE) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						div.info.race().appearance().icon.render(r, body());
					}
				}; 
				INTE in = new INTE() {
					
					@Override
					public int min() {
						return -1;
					}
					
					@Override
					public int max() {
						return RACES.all().size();
					}
					
					@Override
					public int get() {
						return div.info.race().index;
					}
					
					@Override
					public void set(int t) {
						div.info.race.set(RACES.all().get((t+RACES.all().size())%RACES.all().size()));
					}
				};
				CLICKABLE target = new GTarget(C.SG*32, 
						false, true, 
						stat, in) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(div.info.race().info.name);
						GBox b = (GBox) text;
						for (BOOSTABLE bo : BOOSTABLES.military()) {
							if (div.info.race().bonus().add(bo) != 0) {
								b.add(bo.icon());
								b.textL(bo.name);
								b.tab(6);
								b.add(GFORMAT.f0(b.text(),div.info.race().bonus().add(bo)));
								b.NL();
							}
							if (div.info.race().bonus().mul(bo) != 1) {
								b.add(bo.icon());
								b.textL(bo.name);
								b.tab(6);
								GText t = b.text();
								t.add('*');
								b.add(GFORMAT.f1(t,div.info.race().bonus().mul(bo)));
								b.NL();
							}
						}
					}
					
				};
				addRightC(24, target);
			}
			
			{
				GAllocator title = new GAllocator(COLOR.RED100.makeSaturated(0.7), div.info.training, 5, 16, 4) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(STATS.BATTLE().TRAINING_MELEE.info().name);
						text.text(¤¤TrainingLevelD);
						text.NL(4);
						for (StatBooster bo : STATS.BATTLE().TRAINING_MELEE.boosts()) {
							bo.boost.hoverValue(text, div.info.training.getD());
							text.NL();
						}
					}
					@Override
					protected void renAction() {
						activeSet(World.ARMIES().cityDivs().attachedArmy(div) == null);
					}
				};
				title.hoverTitleSet(STATS.BATTLE().TRAINING_MELEE.info().name);
				addRightC(24, title);
			}
			
			{
				GAllocator title = new GAllocator(COLOR.RED100.makeSaturated(0.7), div.info.trainingR, 5, 16, 4) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(STATS.BATTLE().TRAINING_ARCHERY.info().name);
						text.text(STATS.BATTLE().TRAINING_ARCHERY.info().desc);
						text.NL(4);
						for (StatBooster bo : STATS.BATTLE().TRAINING_ARCHERY.boosts()) {
							bo.boost.hoverValue(text, div.info.trainingR.getD());
							text.NL();
						}
					}
					@Override
					protected void renAction() {
						activeSet(World.ARMIES().cityDivs().attachedArmy(div) == null);
					}
				};
				
				addRightCAbs(equipwidth, title);
			}
			
			for (StatEquippableBattle e : STATS.EQUIP().military()){
				INTE t = new INTE() {
					@Override
					public void set(int t) {
						e.targetSet(div, t);
					}
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return e.max();
					}
					
					@Override
					public int get() {
						return e.target(div);
					}
				};
				GAllocator title = new GAllocator(COLOR.ORANGE100.makeSaturated(0.7), t, 5, 16) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(e.stat().info().name);
						text.text(e.stat().info().desc);
						text.NL(4);
					
						for (StatBooster bo : e.boosts()) {
							bo.boost.hoverValue(text, t.getD());
							text.NL();
						}
					}
					
					@Override
					protected void renAction() {
						activeSet(World.ARMIES().cityDivs().attachedArmy(div) == null);
					}
				};
				addRightCAbs(equipwidth, title);
			}
			
			for (StatEquippableRange e : STATS.EQUIP().ammo()){
				INTE t = new INTE() {
					@Override
					public void set(int t) {
						e.targetSet(div, t);
					}
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return e.equipMax;
					}
					
					@Override
					public int get() {
						return e.target(div);
					}
				};
				GAllocator title = new GAllocator(COLOR.YELLOW100.makeSaturated(0.7), t, 5, 16) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(e.stat().info().name);
						text.text(e.stat().info().desc);
						
						text.NL(4);
						
						for (StatBooster bo : e.boosts()) {
							bo.boost.hoverValue(text, t.getD());
							text.NL();
						}
					}
					
					@Override
					protected void renAction() {
						activeSet(World.ARMIES().cityDivs().attachedArmy(div) == null);
					}
				};
				
				addRightCAbs(equipwidth, title);
			}
			
			pad(4, 4);
			

		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GButt.BSection.renderBG(r, body(), true, false, false);
			super.render(r, ds);
			if (div.info.men.get() == 0) {
				OPACITY.O25.bind();
				COLOR.BLACK.render(r, body(), -1);
				OPACITY.unbind();
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (isHoveringAHoverElement())
				super.hoverInfoGet(text);
			else
				div.hoverInfo((GBox)text);
		}
		
	}

	private static boolean canSendSupplies(Div div) {
		
		for (ArmySupply s : RESOURCES.SUP().ALL()) {
			if (needed(s, div) > SETT.ROOMS().SUPPLY.reservable(WARMYD.supplies().get(s)))
				return false;
		}
		return true;
	}
	
	private static int needed(ArmySupply s, Div div) {
		return (int) Math.ceil(div.menNrOf()*(WARMYD.supplies().get(s).minimumPerMan + (WARMYD.supplies().get(s).usedPerDay*2)));
	}
	
	private class Banner {
		
		private final GuiSection s = new GuiSection();
		private Div div = null;
		private DivisionBanner b = SETT.ARMIES().banners.get(0);
		private final BitmapSpriteEditor appearence = new BitmapSpriteEditor();
		
		Banner(){
			
			
			
			
			for (int i = 0; i < SETT.ARMIES().banners.size(); i++) {
				final int k = i;
				final DivisionBanner bb = SETT.ARMIES().banners.get(i);
				s.add(new GButt.ButtPanel(bb) {
					
					@Override
					protected void clickA() {
						div.info.symbolSet(k);
						appearence.spriteSet(bb.sprite);
						b = bb;
					}
					
					@Override
					protected void renAction() {
						selectedSet(appearence.spriteGet() == bb.sprite);
					}
					
				}.pad(2, 2), (i%8)*40, (i/8)*40);
			}
			
			{
				CLICKABLE title = new GInput(new  StringInputSprite(20, UI.FONT().H2) {
					@Override
					public void renAction() {
						text().clear().add(div.info.name());
					}
					
					@Override
					protected void change() {
						div.info.name().clear().add(text());
					};
				});
				s.addRelBody(8, DIR.N, title);
			}
			
			s.addRelBody(8, DIR.S, appearence);
			
			s.addRelBody(8, DIR.S, new GColorPicker(false, DicMisc.¤¤Color) {
				
				@Override
				public ColorImp color() {
					return b.col;
				}
			});
			
			s.addRelBody(8, DIR.S, new GColorPicker(false, DicMisc.¤¤Color + " 2") {
				
				@Override
				public ColorImp color() {
					return b.bg;
				}
			});
			
			
			
			
		}
		
		void open(Div div, CLICKABLE trig) {
			this.div = div;
			this.b = SETT.ARMIES().banners.get(div.info.symbolI());
			
			appearence.spriteSet(b.sprite);
			
			VIEW.inters().popup.show(s, trig);
		}
		
	}
	
	
}
