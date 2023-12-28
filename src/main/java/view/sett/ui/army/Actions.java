package view.sett.ui.army;

import game.faction.FACTIONS;
import init.D;
import init.resources.ArmySupply;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.army.Div;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.dic.DicArmy;
import util.dic.DicMisc;
import util.gui.common.UIPickerArmy;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;

final class Actions extends GuiSection{

	private static CharSequence ¤¤LowSupplies = "¤Not enough supplies to send out. Fill up your Army Supply depots!";
	private static CharSequence ¤¤LowSuppliesSome = "¤Not enough supplies to send out some of these divisions. Fill up your Army Supply depots!";
	private static CharSequence ¤¤NoValid = "¤The selected division are already attached to a world army. You must recall them first.";
	private static CharSequence ¤¤Recall = "¤Recall";
	private static CharSequence ¤¤RecallD = "¤Recall these divisions from its world armies and have them return to the city.";
	private static CharSequence ¤¤RecallProblem = "¤No divisions are selected that are currently attached to a world army.";
	private static CharSequence ¤¤SendOut = "¤Send Out";
	private static CharSequence ¤¤SendOutD = "¤Send this division to join an army on the world map. These soldiers will have to be supplied through your army depot.";
	private static CharSequence ¤¤NotTrained = "¤Some of the soldiers are not fully trained to specification yet, and will continue to train before they join an army.";
	private static CharSequence ¤¤NoArmies = "¤There are no armies to send this division to. Recruit one on the world map.";
	private static CharSequence ¤¤NoDivs = "No divisions are selected.";
	private static CharSequence ¤¤DisbandD = "Are you sure you wish to disband {0} divisions?";
	
	static {
		D.ts(Actions.class);
	}
	
	Actions(ArrayList<Div> list){
		
		int width = 180;
		int height = 32;
		GButt.ButtPanel c;
		
		GuiSection f = new GuiSection();
		
		f.addRightC(0, new GButt.Glow(SPRITES.icons().m.questionmark) {
			
			@Override
			protected void clickA() {
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Str tmp = Str.TMP.clear().add(DicArmy.¤¤Unitinfo);
				tmp.insert(0, KEYS.MAIN().UNDO.repr());
				tmp.insert(1, KEYS.MAIN().MOD.repr());
				text.text(tmp);
			};
			
			
		});
		
		c = new GButt.ButtPanel(DicMisc.¤¤Create) {

			
			@Override
			protected void clickA() {
				Div n = SETT.ARMIES().player().getNextEmptyOrdered();
				if (n == null)
					return;
				n.info.race.set(FACTIONS.player().race());
				n.info.men.set(50);
				for (EquipBattle e : STATS.EQUIP().BATTLE_ALL())
					e.targetSet(n, 0);
				for (StatTraining e : STATS.BATTLE().TRAINING_ALL)
					n.info.trainingD(e.room).setD(0);
				clicked = null;
			}
			
			@Override
			protected void renAction() {
				activeSet(false);
				for (Div d : SETT.ARMIES().player().divisions()) {
					if (d.info.men() == 0) {
						activeSet(true);
						return;
					}
				}
				
			}
			
		};
		c.icon(UI.icons().m.plus);
		c.setDim(width, height);
		f.addRightC(0, c);
		
		c = new GButt.ButtPanel(DicMisc.¤¤Edit) {
			
			private final Edit edit = new Edit();
			
			@Override
			protected void clickA() {
				for (int di = 0; di < list.size(); di++) {
					if (WORLD.ARMIES().cityDivs().attachedArmy(list.get(di)) != null) {
						list.remove(di);
						di--;
					}
				}
				if (list.size() > 0)
					VIEW.inters().popup.show(edit.get(list, this), this);
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				isActive = false;
				for (Div d : list) {
					if (WORLD.ARMIES().cityDivs().attachedArmy(d) == null)
						isActive = true;
				}
				super.render(r, ds, isActive, isSelected, isHovered);
			}
			
		};
		c.icon(UI.icons().m.menu);
		c.setDim(width, height);
		f.addRightC(0, c);
		
		c = new GButt.ButtPanel(¤¤SendOut) {
			
			UIPickerArmy p = new UIPickerArmy(new GETTER.GETTER_IMP<>(FACTIONS.player()), 400) {
				
				@Override
				protected void pick(WArmy a) {
					for (Div div : list) {
						if (a.divs().canAdd() && WORLD.ARMIES().cityDivs().attachedArmy(div) == null && canSendSupplies(div)) {
							WORLD.ARMIES().cityDivs().attach(a, div);
						}
					}
					VIEW.inters().popup.close();
				}
				
				@Override
				protected boolean canBePicked(WArmy a) {
					if (a == null)
						return false;
					return a.divs().canAdd();
				}
			};
			
			@Override
			protected void clickA() {
				if (sendProblem(list) != null)
					return;
				
				VIEW.inters().popup.show(p, this);
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				isActive = sendProblem(list) == null;
				super.render(r, ds, isActive, isSelected, isHovered);
			}
			
			@Override
			protected void renAction() {
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				b.title(¤¤SendOut);
				b.text(¤¤SendOutD);
				b.NL(8);
				
				hoverSendOutProblem(list, b);
			}
			
		
			
		};
		c.icon(UI.icons().m.arrow_left);
		c.setDim(width, height);
		addRightC(0, c);
		
		
		c = new GButt.ButtPanel(¤¤Recall) {
			
			@Override
			protected void clickA() {
				if (hardProblem(list) != null)
					return;
				for (Div div : list) {
					if (WORLD.ARMIES().cityDivs().attachedArmy(div) != null)
						WORLD.ARMIES().cityDivs().attach(null, div);	
				}
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				isActive = can();
				super.render(r, ds, isActive, isSelected, isHovered);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				b.title(¤¤Recall);
				b.text(¤¤RecallD);
				b.NL(8);
				
				if (!can())
					b.error(¤¤RecallProblem);
				
			}
			
			private boolean can() {
				if (list.size() == 0)
					return false;
				for (Div div : list) {
					if (WORLD.ARMIES().cityDivs().attachedArmy(div) != null) {
						return true;
					}
				}
				return false;
			}
			
			public CharSequence hardProblem(LIST<Div> divs) {
				if (divs.size() == 0)
					return ¤¤NoDivs;
				if (WORLD.ARMIES().army(FACTIONS.player()).all().size() <= 0)
					return ¤¤NoArmies;
				for (Div div : divs) {
					if (WORLD.ARMIES().cityDivs().attachedArmy(div) != null)
						return null;
				}
				return ¤¤NoValid;
			}
			
			
		};
		c.icon(UI.icons().m.arrow_right);
		c.setDim(width, height);
		addRightC(0, c);
		
		c = new GButt.ButtPanel(DicArmy.¤¤Disband) {
			
			final ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					for (Div div : list) {
						div.info.men.set(0);
					}
				}
			};
			
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤DisbandD).insert(0, list.size()), a, ACTION.NOP, true);
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				isActive = list.size() > 0;
				super.render(r, ds, isActive, isSelected, isHovered);
			}
			
		};
		c.icon(UI.icons().m.cancel);
		c.setDim(width, height);
		addRightC(0, c);
		
		addRelBody(0, DIR.N, f);
		
	}
	
	public static CharSequence sendProblem(LIST<Div> divs) {
		if (divs.size() == 0)
			return ¤¤NoDivs;
		if (WORLD.ARMIES().army(FACTIONS.player()).all().size() <= 0)
			return ¤¤NoArmies;
		for (Div div : divs) {
			if (WORLD.ARMIES().cityDivs().attachedArmy(div) == null && canSendSupplies(div))
				return null;
		}
		for (Div div : divs) {
			if (!canSendSupplies(div))
				return ¤¤LowSupplies;
		}
		return ¤¤NoValid;
	}

	
	public static boolean canSendSupplies(Div div) {
		
		for (ArmySupply s : RESOURCES.SUP().ALL()) {
			if (needed(s, div) > SETT.ROOMS().SUPPLY.reservable(AD.supplies().get(s)))
				return false;
		}
		return true;
	}
	
	public static int needed(ArmySupply s, Div div) {
		return (int) Math.ceil(div.menNrOf()*(AD.supplies().get(s).minimumPerMan + (AD.supplies().get(s).usedPerDay*2)));
	}
	
	public static boolean hoverSendOutProblem(LIST<Div> divs, GUI_BOX box) {
		GBox b = (GBox) box;
		
		CharSequence h = sendProblem(divs);
		if (h != null) {
			b.error(h);
			b.NL();
		}
		
		for (Div div : divs) {
			if (VIEW.s().ui.army.spec.needsTraining(div) > 0) {
				b.add(b.text().warnify().add(¤¤NotTrained));
				b.NL();
				break;
			}
		}
		
		boolean problem = false;
		for (ArmySupply s : RESOURCES.SUP().ALL()) {
			int need = 0;
			
			for (Div div : divs) {
				need += needed(s, div);
				if (need > SETT.ROOMS().SUPPLY.reservable(AD.supplies().get(s))) {
					problem = true;
				}
			}
			
			b.add(s.resource.icon());
			b.add(GFORMAT.iofkInv(b.text(), need, SETT.ROOMS().SUPPLY.reservable(AD.supplies().get(s))));
			
			
		}
		if (h != null && problem) {
			b.add(b.text().warnify().add(¤¤LowSuppliesSome));
			b.NL();
		}
		
		return true;
	}
	
}
