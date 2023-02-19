package view.battle;

import java.util.Arrays;

import game.time.TIME;
import init.C;
import init.D;
import init.sprite.SPRITES;
import settlement.army.Div;
import settlement.army.formation.DIV_FORMATION;
import settlement.army.order.DivTDataTask;
import settlement.army.order.DivTDataTask.DIVTASK;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.StatEquippableRange;
import snake2d.*;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.panel.GPanelS;
import util.info.GFORMAT;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;

public final class UISelection extends Interrupter{
	
	private final DivSelection selection;
	private final GuiSection section = new GuiSection();
	
	private int iFormationTight;
	private int iFormationLoose;
	private int iRunning;
	private int iCanCharge;
	private int iCharging;
	private int iMustered;

	private int iMopping;
	private int iRangedHasAny;
	private int[] iRangedHas = new int[STATS.EQUIP().ammo().size()];
	private int[] iAmmoHas = new int[STATS.EQUIP().ammo().size()];
	private int[] iRangedSelected = new int[STATS.EQUIP().ammo().size()];
	private int iInGuard;
	private int ifiresAtWill;
	private final DivTDataTask task = new DivTDataTask();
	private int men;
	
	private static CharSequence ¤¤dPosition = "¤To position your troops, click and hold the left mouse button.";
	private static CharSequence ¤¤dAdd = "¤To add troops to your selection, hold {0}, then click and drag.";
	private static CharSequence ¤¤dMove = "¤To reposition your troops, use the arrow keys ({0})";
	private static CharSequence ¤¤dSpin = "¤To rotate your selection, hold  {0}, click and hold where the center should be.";
	private static CharSequence ¤¤dSelectAll = "¤To select all divisions, press {0}.";
	private static CharSequence ¤¤dStopAll = "¤Stop all divisions and clear targets ({0})";
	private static CharSequence ¤¤dFireAtWill = "¤Toggle fire at will. Allows soldiers and artillery to fire at enemies within reach.";
	private static CharSequence ¤¤dGuard = "¤In guard mode soldiers will stay in formation when fighting. Increases defense, but decreases offence.";
	private static CharSequence ¤¤Attack = "¤To attack an enemy division, left click on it. If ranged, to attack an enemy division melee, hold {0} and left click.";
	private static CharSequence ¤¤MopUp = "¤When soldiers are in position, they'll break it and go chasing enemy soldiers or rioteers. Morale will be very weak while doing this.";
	private static CharSequence ¤¤FormationLoose = "¤Loose formation. Increases offence, but decreases defence.";
	private static CharSequence ¤¤FormationTight = "¤Tight formation. Increases defence, but decreases offence.";
	private static CharSequence ¤¤Run = "¤makes soldiers move faster, but will also fatigue them faster";
	private static CharSequence ¤¤Charge = "¤Soldiers will start running in their current direction until they reach an enemy or obstacle. Soldiers will not have a lot of defense, but gain a lot of momentum when colliding, as well as scaring the enemy force.";
	static {
		D.t(UISelection.class);
	}
	
	public UISelection(InterManager m, DivSelection s, boolean muster) {
		this.selection = s;
		
		section.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, men);
			}
		}.decrease().hh(SPRITES.icons().s.sword));
		
		
		section.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, s.artillery.selection().size());
			}
		}.decrease().hh(SPRITES.icons().s.crossheir));
		
		
		section.body().incrW(C.SG*60);
		
		section.addRelBody(C.SG*5, DIR.S, makeCommands(muster));
		
		GPanelS f = new GPanelS();
		f.inner().set(section);
		
		f.body().centerX(C.DIM());
		f.body().moveY1(C.SG*80);
		section.body().centerIn(f.inner());
		f.setButtBg();
		section.add(f);
		section.moveLastToBack();
		
		CLICKABLE c = new GButt.Panel(SPRITES.icons().m.exit) {
			@Override
			protected void clickA() {
				selection.clear();
			};
		};
		f.moveExit(c);
		section.add(c);
		
		pin();
		show(m);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return false;
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return;
		if (button == MButt.LEFT)
			section.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return;
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return true;
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		iCanCharge = 0;
		iFormationLoose = 0;
		iFormationTight = 0;
		iRunning = 0;
		iCharging = 0;
		iMustered = 0;

		iInGuard = 0;
		ifiresAtWill = 0;
		iMopping = 0;
		men = 0;
		
		iRangedHasAny = 0;
		Arrays.fill(iAmmoHas, 0);
		Arrays.fill(iRangedHas, 0);
		Arrays.fill(iRangedSelected, 0);
		
		for (Div d : selection.selection()) {
			d.order().task.get(task);
			if (d.settings.running)
				iRunning ++;
			if (d.settings.formation == DIV_FORMATION.LOOSE)
				iFormationLoose++;
			if (d.settings.formation == DIV_FORMATION.TIGHT)
				iFormationTight++;
//			if (!d.charging() && d.orders().canCharge())
//				iCanCharge++;
			if (task.task() == DIVTASK.CHARGE)
				iCharging ++;
			if (d.settings.mustering())
				iMustered++;
			if (d.settings.moppingUp())
				iMopping++;
			if (d.settings.guard)
				iInGuard ++;
			for (StatEquippableRange a : STATS.EQUIP().ammo()) {
				if (a.stat().div().get(d) > 0) {
					iRangedHas[a.tIndex] ++;
					iRangedHasAny ++;
					if (d.settings.ammo() == a)
						iRangedSelected[a.tIndex] ++;
					if (a.ammunition.div().get(d) > 0) {
						iAmmoHas[a.tIndex] ++;
					}
				}
				if (d.settings.fireAtWill) {
					ifiresAtWill ++;
				}
				
				
			}
			
			
			men += d.menNrOf();
		}
		for (ArtilleryInstance c : selection.artillery.selection()) {
			if (c.mustered())
				iMustered ++;
			if (c.fireAtWill()) {
				ifiresAtWill ++;
			}
			iRangedHasAny ++;
				
		}
		
		return true;
	}
	
	private GuiSection makeCommands(boolean muster) {

		GuiSection main = new GuiSection();
		
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new HOVERABLE.Sprite(SPRITES.icons().m.questionmark) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤dPosition);
				text.NL(4);
				Text t;
				
				t = text.text();
				t.add(¤¤dAdd);
				t.insert(0, KEYS.MAIN().UNDO.repr());
				text.add(t);
				text.NL(4);
				
				t = text.text();
				t.add(¤¤dMove);
				t.insert(0, KEYS.BATTLE().UP.repr());
				text.add(t);
				text.NL(4);
				
				t = text.text();
				t.add(¤¤dSpin);
				t.insert(0, KEYS.MAIN().MOD.repr());
				text.add(t);
				text.NL(4);
				
				t = text.text();
				t.add(¤¤Attack);
				t.insert(0, KEYS.MAIN().UNDO.repr());
				text.add(t);
				text.NL(4);
				
				t = text.text();
				t.add(¤¤dSelectAll);
				t.insert(0, KEYS.BATTLE().SELECT_ALL.repr());
				text.add(t);
				text.NL(4);
				
			};
			
		
		});
		
		if (muster) {
			s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.for_muster) {
				@Override
				protected void clickA() {
					
					boolean shouldMuster = iMustered < selection.allSelected();
					for (Div d : selection.selection())
						if (shouldMuster && d.menNrOf() > 0 && d.position().deployed() > 0)
							d.settings.musteringSet(shouldMuster);
						else if (!shouldMuster)
							d.settings.musteringSet(false);
					for (ArtilleryInstance c : selection.artillery.selection()) {
						c.muster(shouldMuster);
					}
				};
				
				@Override
				protected void renAction(){
					boolean mustered = iMustered == selection.allSelected();
					selectedSet(mustered);
					if (!mustered) {
						activeSet(positions() > 0 || selection.artillery.selection().size() > 0);
					}else {
						activeSet(true);
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox)text;
					b.title(DicArmy.¤¤Muster);
					b.text(DicArmy.¤¤MusterDesc);
					b.NL(8);
					
					int p = positions();
					if (p == 0) {
						b.error(DicArmy.¤¤MusterProblem);
					}else if (p < selection.selection().size()) {
						b.error(DicArmy.¤¤MusterOneProblem);
					}
				}
				
				private int positions() {
					int i = 0;
					for (Div d : selection.selection()) {
						if (d.menNrOf() > 0 && d.position().deployed() > 0) {
							i++;
						}
					}
					return i;
				}
			
			});
		}
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.expand) {
			@Override
			protected void clickA() {
				boolean shouldMuster = iMopping < selection.selection().size();
				for (Div d : selection.selection())
					if (d.menNrOf() > 0 && d.position().deployed() > 0)
						d.settings.moppingSet(shouldMuster);
			};
			
			@Override
			protected void renAction(){
				activeSet(false);
				if (selection.selection().size() > 0) {
					activeSet(true);
					boolean mustered = iMopping == selection.selection().size();
					selectedSet(mustered);
				}
				
			}
			
		
		}.hoverInfoSet(¤¤MopUp));
		
		s.addRightC(16, new GButt.ButtPanel(SPRITES.icons().m.for_loose) {
			@Override
			protected void clickA() {
				for (Div d : selection.selection())
					d.settings.formation = DIV_FORMATION.LOOSE;
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iFormationLoose == selection.selection().size());
			}
			
			
		
		}.hoverInfoSet(¤¤FormationLoose));
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.for_tight) {
			@Override
			protected void clickA() {
				for (Div d : selection.selection())
					d.settings.formation = DIV_FORMATION.TIGHT;
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs()&& iFormationTight == selection.selection().size());
			}
		}.hoverInfoSet(¤¤FormationTight));
		
		s.addRightC(16, new GButt.ButtPanel(SPRITES.icons().m.bow) {
			@Override
			protected void clickA() {
				
				
				boolean charge = ifiresAtWill == 0;
				for (Div d : selection.selection()) {
					d.settings.fireAtWill = charge;
				}
				for (ArtilleryInstance c : selection.artillery.selection()) {
					c.fireAtWill(charge);
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(iRangedHasAny > 0);
				selectedSet(ifiresAtWill > 0);
			}
		}.hoverInfoSet(¤¤dFireAtWill));
		
		for (StatEquippableRange a : STATS.EQUIP().ammo()) {
			s.addRightC(0, new GButt.ButtPanel(a.resource.icon()) {
				@Override
				protected void clickA() {
					for (Div d : selection.selection()) {
						if (a.stat().div().get(d) > 0)
							d.settings.ammoI = a.tIndex;
					}
				};
				
				@Override
				protected void renAction(){
					activeSet(iRangedHas[a.tIndex] > 0);
					selectedSet(activeIs() && iRangedSelected[a.tIndex] > 0);
				
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					super.render(r, ds, isActive, isSelected, isHovered);
					if (iAmmoHas[a.tIndex] == 0) {
						OPACITY.O0To25.bind();
						COLOR.BLACK.render(r, body);
						OPACITY.unbind();
						
					}
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(a.resource.name);
					if (iAmmoHas[a.tIndex] == 0) {
						Str.TMP.clear().add(DicArmy.¤¤ReloadingXX);
						Str.TMP.insert(0, a.ammoReplenishHours*TIME.secondsPerHour, 4);
						text.text(Str.TMP);
					}
				};
				
				
			}.hoverInfoSet(a.resource.name));
			
		}
		
		main.add(s);
		
		s = new GuiSection();
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.fast_forw) {
			@Override
			protected void clickA() {
				if (iRunning == selection.selection().size()) {
					for (Div d : selection.selection())
						d.settings.running = false;
				}else {
					for (Div d : selection.selection())
						d.settings.running = true;
				}
				
				
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iRunning == selection.selection().size());
			}
		}.hoverInfoSet(¤¤Run));
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.horn) {
			@Override
			protected void clickA() {
				
				
				if (iCanCharge == selection.selection().size())
					task.stop();
				else
					task.charge();
				for (Div d : selection.selection()) {
					d.order().task.set(task);
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iCharging == selection.selection().size());
			}
		}.hoverInfoSet(¤¤Charge));
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.shield) {
			@Override
			protected void clickA() {
				
				
				boolean charge = iInGuard < selection.selection().size();
				for (Div d : selection.selection()) {
					d.settings.guard = charge;
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iInGuard == selection.selection().size());
			}
		}.hoverInfoSet(¤¤dGuard));
		

		
		
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
			
			
			@Override
			protected void clickA() {
				for (Div d : selection.selection()) {
					task.stop();
					d.order().task.set(task);
				}
				for (ArtilleryInstance c : selection.artillery.selection()) {
					c.clearTarget();
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(¤¤dStopAll);
				t.insert(0, KEYS.MAIN().BACKSPACE.repr());
				text.add(t);
			};
			
		});
		
		main.addRelBody(0, DIR.S, s);

		
		return main;
	}

}
