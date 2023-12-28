package view.world.ui.camps;

import game.faction.FACTIONS;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.WORLD;
import world.map.buildings.camp.WCampInstance;
import world.map.buildings.camp.WCampType;

final class CampInfo extends GuiSection{

	private final WCampType type;
	
	private static CharSequence ¤¤notFull = "¤Fulfill this species requirements to unlock the help of the havens that are within your realm.";
	private static CharSequence ¤¤full = "¤The requirements have been met and the havens on your lands are at your service.";
	private static CharSequence ¤¤Replenish = "Replenish";
	
	private static CharSequence ¤¤unlocked = "This haven is on your lands and at your service.";
	private static CharSequence ¤¤onLands = "This haven is on your lands, but the requirements are not met for them to join your cause.";
	private static CharSequence ¤¤distant = "This haven is not on your lands and can not serve you.";
	
	static {
		D.ts(CampInfo.class);
	}
	
	public CampInfo(WCampType type) {
		this.type = type;
		add(type.race.appearance().iconBig, 0, 0);
		add(new GHeader(type.race.info.names, UI.FONT().S), getLastX2()+8, 0);
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, WORLD.camps().factions.max(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.human), getLastX1(), getLastY2());
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, WORLD.camps().factions.replenishPerDay(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.clock));
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, WORLD.camps().factions.camps(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.house));
		
		body().incrW(48);
		
		addRelBody(4, DIR.S, new SPRITE.Imp(body().width(), 12) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double d = type.reqs.progress(null);
				if (d >= 1) {
					GMeter.render(r, GMeter.C_BLUE, 1.0, X1, X2, Y1, Y2);
				}else  {
					GMeter.render(r, GMeter.C_ORANGE, d, X1, X2, Y1, Y2);
				}
			}
		});
		
		pad(16, 8);
		
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		b.title(type.race.info.names);
		b.text(type.race.info.desc);
		
		b.NL();
		b.textLL(DicMisc.¤¤havens);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), WORLD.camps().factions.camps(FACTIONS.player(), type)));
		
		b.NL();
		b.textLL(DicMisc.¤¤Population);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), WORLD.camps().factions.max(FACTIONS.player(), type)));
		
		b.NL();
		b.textLL(¤¤Replenish);
		b.tab(6);
		b.add(GFORMAT.f0(b.text(), WORLD.camps().factions.replenishPerDay(FACTIONS.player(), type)));
		
		
		b.NL(8);
		if (type.reqs.passes(null))
			b.add(b.text().normalify2().add(¤¤full));
		else
			b.add(b.text().warnify().add(¤¤notFull));
		
		
		b.NL(8);
		type.reqs.hover(text, null);
		super.hoverInfoGet(text);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		GButt.ButtPanel.renderBG(r, true, false, hoveredIs(), body());
		super.render(r, ds);
		GButt.ButtPanel.renderFrame(r, body());
	}
	
	static void hover(GUI_BOX box, WCampInstance ins) {
		GBox b = (GBox) box;
		b.title(ins.name);
		b.NL();
		b.textL(ins.race().info.names);
		b.tab(5);
		b.add(GFORMAT.i(b.text(), ins.max));
		b.NL(8);
		
		if (ins.regionFaction() != FACTIONS.player())
			b.add(b.text().warnify().add(¤¤distant));
		else if (ins.type().reqs.passes(null))
			b.add(b.text().normalify2().add(¤¤unlocked));
		else
			b.add(b.text().warnify().add(¤¤onLands));
	}
	
}
