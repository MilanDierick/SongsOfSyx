package game.battle;

import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.C;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HTYPE;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O;
import util.dic.DicArmy;
import util.gui.misc.*;
import util.gui.panel.GPanelL;
import view.interrupter.Interrupter;
import view.main.VIEW;
import world.World;
import world.army.WARMYD;
import world.army.WARMYD.WArmySupply;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.regions.RegionTaxes.RegionResource;

final class PromptConquer extends Interrupter implements Prompt{

	private final GuiSection s = new GuiSection();

	private static CharSequence ¤¤Title = "¤Region Conquered";
	private static CharSequence ¤¤surrenderD = "¤Region of {0} has surrendered and opened their gates. Its people trust you will be lenient. What will you do with them?";
	private static CharSequence ¤¤victoryD = "¤Region of {0} is ours. What will do with it?";

	
	private int[] enslave = new int[RACES.all().size()];
	private double multiplier = 0;
	
	private Conflict conflict;
	private Region reg;
	private boolean surrender;
	private DOUBLE_O<RESOURCE> spoils;
	private DOUBLE_O<Race> slaves;
	
	PromptConquer(PromptUtil ui){
		
		D.t(this);
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.lablifySub();
				if (surrender) {
					text.add(¤¤surrenderD);
					
				}else{
					text.add(¤¤victoryD);
				}
				
				text.insert(0, reg.name());
				text.setMaxWidth(500);
				text.adjustWidth();
			}
		}.r(DIR.N), 0, 0);
		
		spoils = new DOUBLE_O<RESOURCE>() {

			@Override
			public double getD(RESOURCE t) {
				RegionResource rr = REGIOND.RES().map(t);
				if (rr == null)
					return 0;
				return rr.maxOutput(reg)*0.1*multiplier;
			}
		
		};
		
		slaves = new DOUBLE_O<Race>(){

			@Override
			public double getD(Race t) {
				return REGIOND.RACE(t).population.get(reg)*0.25*multiplier;
			}
			
		};
		
		{
			GuiSection ss = new GuiSection();
			
			ss.add(new GButt.ButtPanel(D.g("Mercy")) {
				
				double mul = 0;
				
				@Override
				protected void clickA() {
					multiplier = mul;
				};
				
				@Override
				protected void renAction() {
					selectedSet(multiplier == mul);
				};
				
			}.setDim(150, 30).hoverInfoSet(D.g("MercyD", "Show mercy. Neither plunder, death nor slaves will be had, but the region will be in your debt.")));
			
			ss.addRightC(2, new GButt.ButtPanel(D.g("Sack")) {
				
				double mul = 0.25;
				
				@Override
				protected void clickA() {
					multiplier = mul;
				};
				
				@Override
				protected void renAction() {
					selectedSet(multiplier == mul);
				};
				
			}.setDim(150, 30).hoverInfoSet(D.g("SackD", "Let your men have their way, resulting in spoils and prisoners, but the region will despise you for it.")));
			
			ss.addRightC(2, new GButt.ButtPanel(DicArmy.¤¤Annihilation) {
	
				double mul = 0.95;
				
				@Override
				protected void clickA() {
					multiplier = mul;
				};
				
				@Override
				protected void renAction() {
					selectedSet(multiplier == mul);
				};
			}.setDim(150, 30).hoverInfoSet(D.g("annihilateD", "Spare none, leave no stone unturned and teach this settlement a lesson that will be remembered for generations.")));
			
			s.addRelBody(64, DIR.S, ss);
			
		}
		
		s.addRelBody(8, DIR.S, ui.spoils(spoils));

		s.addRelBody(8, DIR.S, ui.slaves(enslave, slaves));
		
		GPanelL panel = new GPanelL();
		
		s.pad(8);
		
		panel.getInnerArea().set(s);
		panel.setTitle(¤¤Title);
		
		GuiSection butts = new GuiSection();
		butts.add(new GButt.Panel(FACTIONS.player().banner().MEDIUM) {
			
			@Override
			protected void clickA() {
				accept(FACTIONS.player());
			}
			
		}.hoverInfoSet(D.g("OccupyD", "Occupy this settlement and incorporate it into your realm")));
		
		butts.addRightC(0, new GButt.Panel(SPRITES.icons().m.rebellion) {
			
			@Override
			protected void clickA() {
				accept(null);
			}
			
		}.hoverInfoSet(D.g("LiberateD", "Liberate this settlement. It will turn to, or remain, a rebel settlement")));
		
		
		panel.centreNavButts(butts);
		
		s.add(butts);
		
		s.add(panel);
		s.moveLastToBack();
		
		s.body().centerIn(C.DIM());
		s.body().moveX2(C.DIM().x2()-50);
		
		pin();
		persistantSet();

		
		
		
	}

	Prompt open(Conflict conflict, Region region, boolean surrender) {
		Arrays.fill(enslave, 0);
		VIEW.world().window.centererTile.set(region.cx(), region.cy());
		VIEW.world().activate();
		this.surrender = surrender;
		this.conflict = conflict;
		this.reg = region;
		VIEW.world().uiManager.clear();
		VIEW.world().panels.clear();
		show(VIEW.world().uiManager);
		return this;
		
	}
	
	void accept(Faction faction) {

		if (multiplier > 0) {
			Shipment ship = World.ENTITIES().caravans.createSpoils(reg.cx(), reg.cy(), FACTIONS.player().capitolRegion());
			
			if (ship != null) {
				for (RESOURCE r : RESOURCES.ALL()) {
					ship.load(r, (int)spoils.getD(r));
				}
				
				for (WArmySupply s : WARMYD.supplies().all) {
					double needed = 0;
					double has = ship.loadGet(s.res);
					for (WArmy a : conflict.sideA) {
						if (a.faction() == FACTIONS.player()) {
							needed += CLAMP.i(s.max(a)-s.current().get(a), 0, Integer.MAX_VALUE);
						}
					}
					if (needed == 0)
						continue;
					double d = CLAMP.d(has/needed, 0, 1);
					for (WArmy a : conflict.sideA) {
						if (a.faction() == FACTIONS.player()) {
							int n = (int)d*CLAMP.i(s.max(a)-s.current().get(a), 0, Integer.MAX_VALUE);
							s.current().inc(a, n);
							ship.load(s.res, -n);
						}
					}
					
				}
			}
			
			
			for (Race r : RACES.all()) {
				if (enslave[r.index] > 0) {
					SETT.ENTRY().add(r, HTYPE.PRISONER, enslave[r.index]);
				}
			}
			
		}
		
		for (WArmy a : conflict.sideA) {
			a.stop();
		}
		
		Resolver.conquer(reg, faction, multiplier);
		Resolver.retreat(conflict.sideB, conflict.sideA);
		
		hide();
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		s.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			s.click();
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		s.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		s.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		GAME.SPEED.tmpPause();
		return false;
	}
	
	@Override
	protected void deactivateAction() {
		
	}

	@Override
	public boolean isActive() {
		return isActivated();
	}

	@Override
	public boolean canSave() {
		return false;
	}

	
}
