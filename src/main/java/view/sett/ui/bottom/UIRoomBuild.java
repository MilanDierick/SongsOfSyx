package view.sett.ui.bottom;

import game.GAME;
import game.faction.FACTIONS;
import init.D;
import init.boostable.BOOSTABLES;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.furnisher.*;
import settlement.room.service.module.RoomServiceDataAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.*;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;

public final class UIRoomBuild{
	
	
	private static CharSequence ¤¤cost = "¤Costs";
	private static CharSequence ¤¤production = "¤Production";
	private static CharSequence ¤¤optional = "¤(Optional)";
	private static CharSequence ¤¤Emits = "¤Emits";
	private static CharSequence ¤¤CurrentRooms = "¤Current Rooms";
	
	static {
		D.ts(UIRoomBuild.class);
	}
	
	private UIRoomBuild() {
		
	}
	
	private static SPRITE sep = new SPRITE.Imp(260, 2) {
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			GCOLOR.UI().border().render(r, X1+32, X2, Y1, Y2);
		}
	};
	
	public static void hoverRoomBuild(RoomBlueprintImp b, GUI_BOX text) {
		
		
		GBox box = (GBox) text;
		box.title(b.info.name);
		box.text(b.info.desc);
		
		CharSequence p = GAME.player().locks.unlockText(b);
		
		if (p != null) {
			box.NL();
			GText t = box.text();
			t.add(p);
			t.errorify();
			box.add(t);
			box.NL();
		}else if (b instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> ins = (RoomBlueprintIns<?>) b;
			box.NL(2);
			box.textLL(¤¤CurrentRooms);
			box.add(GFORMAT.i(box.text(), ins.instancesSize()));
		}
		
		box.NL(8);
		
		boolean e = false;
		for (SettEnv en : SETT.ENV().environment.all()) {
			if (b.constructor().envValue(en)) {
				if (!e) {
					box.textLL(¤¤Emits);
					e = true;
				}
				box.text(en.name);

			}
		}
		box.NL();
		
		if (b.employment() != null) {
			box.textLL(DicMisc.¤¤AccidentRate);
			box.add(GFORMAT.perc(box.text(), b.employment().accidentsPerYear*100 / (1+BOOSTABLES.CIVICS().ACCIDENT.get(null, null))));
			box.NL();
		}
		
		if (b.constructor().resources() > 0) {
			box.NL(8);
			box.textLL(¤¤cost);
			int o = 0;
			for (int ri = 0; ri < b.constructor().resources(); ri++) {
				if (b.upgrades().resMask(0,ri) == 0)
					continue;
				if (optional(b.constructor(), ri)) {
					o++;
					continue;
				}
				box.add(b.constructor().resource(ri).icon());
			}
			
			if (o > 0) {
				box.space().space();
				box.add(box.text().lablifySub().add(¤¤optional));
				for (int ri = 0; ri < b.constructor().resources(); ri++) {
					if (b.upgrades().resMask(0, ri) == 0)
						continue;
					if (optional(b.constructor(), ri)) {
						box.add(b.constructor().resource(ri).icon());
					}
					
				}
			}
			box.NL();
		}
		
		if (b instanceof INDUSTRY_HASER) {
			box.NL(8);
			box.textLL(¤¤production);
			box.NL();
			
			for (Industry i : ((INDUSTRY_HASER)b).industries()) {
				if (i.outs().size() == 0) {
					for (IndustryResource r : i.ins()) {
						box.add(r.resource.icon()).add(GFORMAT.f0(box.text(),  -r.rate));
						box.space();
					}
				}else {
					
					for (int ri = 0; ri < i.ins().size(); ri++) {
						IndustryResource r = i.ins().get(ri);
						box.add(r.resource.icon()).add(GFORMAT.f0(box.text(), -r.rate));
						if (ri < i.ins().size()-1)
							box.add(box.text().add('&'));
					}
					
					box.add(SPRITES.icons().s.arrow_right);
					
					double add = FACTIONS.player().bonus().add(i.bonus());;
					double mul = FACTIONS.player().bonus().mul(i.bonus());
					
					for (int ri = 0; ri < i.outs().size(); ri++) {
						IndustryResource r = i.outs().get(ri);
						
						box.add(r.resource.icon()).add(GFORMAT.fRel(box.text(), (r.rate+add)*mul, r.rate));
						if (ri < i.outs().size()-1)
							box.add(SPRITES.icons().s.plus);
					}
					
					
				}
				
				p = GAME.player().locks.unlockText(i);
				
				if (p != null) {
					box.add(SPRITES.icons().m.lock);
					box.NL();
					GText t = box.text();
					t.add(p);
					t.errorify();
					box.add(t);
				}
				
				box.NL(2);
				box.add(sep);
				box.NL(8);
			}
			
			if (BOOSTABLES.ROOMS().boosts(b).size() > 0) {
				int tab = 0;
				for (Race r : RACES.all()) {
					box.tab(tab*2);
					box.add(r.appearance().icon);
					double d = RACES.bonus().priorityCapped(r, b.employment());
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					int am = (int) Math.ceil(0.1 + d*3);
					am = CLAMP.i(am, 0, 3);
					box.rewind(4);
					for (int i = 0; i < am; i++) {
						
						box.add(SPRITES.icons().s.hammer, ColorImp.TMP);
						box.rewind(8);
					}
					
					tab++;
					if (tab > 6) {
						box.NL();
						tab = 0;
					}
				}
				box.NL();
				
				
			}
		}
		
		STAT stat = null;
		if (b instanceof ROOM_SERVICE_ACCESS_HASER)
			stat = ((ROOM_SERVICE_ACCESS_HASER) b).service().stats().total();
		else if (b instanceof ROOM_MONUMENT)
			stat = STATS.ACCESS().MONUMENTS.all().get(b.typeIndex());
		if (stat != null) {
			box.NL(4);
			box.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
			box.NL();
			int tab = 0;
			
			double min = 0;
			double max = 0;
			for (Race r : RACES.all()) {
				STANDING d =  stat.standing();
				if (d.definition(r).inverted)
					min = Math.max(min, d.definition(r).get(HCLASS.CITIZEN).max);
				else
					max = Math.max(max, d.definition(r).get(HCLASS.CITIZEN).max);
			}
			for (Race r : RACES.all()) {
				STANDING d =  stat.standing();
				COLOR c = GCOLOR.UI().GOOD.hovered;
				SPRITE sp = SPRITES.icons().s.arrowUp;
				int am = (int) (3*Math.ceil(CLAMP.d(d.definition(r).get(HCLASS.CITIZEN).max/max, 0, max)));
				if (d.definition(r).inverted) {
					sp = SPRITES.icons().s.arrowDown;
					c = GCOLOR.UI().BAD.hovered;
					am = (int) (4*Math.round(CLAMP.d(d.definition(r).get(HCLASS.CITIZEN).max/min, 0, min)));
				}
				box.tab(tab*2);
				box.add(r.appearance().icon);
				box.rewind(4);
				for (int i = 0; i < am; i++) {
					
					box.add(sp, c);
					box.rewind(8);
				}
				
				tab++;
				if (tab > 8) {
					box.NL();
					tab = 0;
				}
			}
			
			
		}
		
	}
	
	private static boolean optional(Furnisher f, int ri) {
		if (f.areaCost(ri, 0) > 0)
			return false;
		if (!f.usesArea())
			return false;
		for (FurnisherItemGroup g : f.groups()) {
			if (needed(f, g) && g.cost(ri, 0) > 0) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean needed(Furnisher f, FurnisherItemGroup g) {
		if (g.min > 0)
			return true;
		
		for (FurnisherStat s : f.stats()) {
			if (g.stat(s.index()) > 0 && s.min > 0)
				return true;
		}
		return false;
	}
	
}
