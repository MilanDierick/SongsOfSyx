package game.battle;

import static settlement.main.SETT.*;

import init.C;
import init.RES;
import init.race.RACES;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.CAUSE_ARRIVE;
import settlement.stats.STATS;
import settlement.stats.StatsEquippables.EQUIPPABLE_MILITARY;
import snake2d.util.datatypes.*;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;
import world.army.WINDU.WDivGeneration;

final class BattleStateGenArmy {

	private final static Tree<WDivId> sort = new Tree<WDivId>(RES.config().BATTLE.DIVISIONS_PER_ARMY) {

		@Override
		protected boolean isGreaterThan(WDivId current, WDivId cmp) {
			return !current.gen.isRange;
		}
		
		
	};
	
	public static void genArmy(ArrayList<WDivGeneration> ss, boolean player, DIR d, int art, Race artrace) {
		
		sort.clear();
		int di = 0;
		for (WDivGeneration s : ss)
			if (sort.hasRoom())
				sort.add(new WDivId(di++, s));
		
		int w = 8;
		int cx = SETT.TILE_BOUNDS.cX()+d.x()*48 + d.next(2).x()*w/2;
		int cy = SETT.TILE_BOUNDS.cY()+d.y()*48 + d.next(2).x()*w/2;
		
		int depth = 0;
		for (; depth < 256 && sort.hasMore(); depth += w+1) {
			for (int width = 0; width < 220; width++) {
				if (!sort.hasMore())
					break;
				int x1 = cx + depth*d.x() + d.next(2).x()*width;
				int y1 = cy + depth*d.y() + d.next(2).y()*width;
				WDivId div = sort.greatest();
				if (divPlace(div, player, w, d, x1, y1)) {
					width -= w;
					if (width < 0)
						width = 0;
					sort.pollGreatest();
				}
					
				
				if (!sort.hasMore())
					break;
				
				
				x1 = cx + depth*d.x() - d.next(2).x()*width;
				y1 = cy + depth*d.y() - d.next(2).y()*width;
				div = sort.greatest();
				if (divPlace(div, player, w, d, x1, y1)) {
					width -= w;
					if (width < 0)
						width = 0;
					sort.pollGreatest();
				}
			}
		}
		
		depth += BattleStateArt.placeArt(cx, cy, depth, d, art, artrace, player);
		
		if (player)
			placeThrone(cx, cy, depth, d);
		SETT.ARMIES().player().initMorale();
		SETT.ARMIES().enemy().initMorale();
	}
	
	private static void placeThrone(int cx, int cy, int depth, DIR d) {
		depth += 15;
		int x1 = cx + depth*d.x();
		int y1 = cy + depth*d.y();
		int dd = 0;
		for (DIR ddd : DIR.ORTHO) {
			if (ddd == d.perpendicular())
				break;
			dd++;
		}
		SETT.ROOMS().THRONE.init.place(x1, y1, dd);
	}
	
	static int men;
	private static final Rec tmp = new Rec();
	private static final ArrayList<Div> tmpDivs = new ArrayList<>(1);
	
	private static boolean divPlace(WDivId wdiv, boolean player, int widthMax, DIR d, int tx1, int ty1) {
		
		WDivGeneration div = wdiv.gen;
		
		if (div.indus.length == 0)
			return true;
		
		
		int depthMax = (int) Math.ceil((double)div.indus.length/widthMax);
		if (d.x() != 0) {
			int bi = depthMax;
			depthMax = widthMax;
			widthMax = bi;
		}
		
		d = d.next(-1);
		
		
		
		
		
		tmp.set(tx1, tx1+d.x()*depthMax, ty1,  ty1+d.y()*widthMax);
		tmp.makePositive();
		
		int men = 0;
		
		for (COORDINATE c : tmp) {
			if (SETT.PATH().availability.get(c).player <= 0 || SETT.ENTITIES().hasAtTile(c.x(), c.y())) {
				return false;
			}
		}
		Div adiv = (player ? SETT.ARMIES().player() : SETT.ARMIES().enemy()).divisions().get(wdiv.di);
		
		
		adiv.settings.musteringSet(true);
		adiv.settings.fireAtWill = true;
		
		Race race = RACES.all().get(div.race);
		adiv.info.race.set(race);
		adiv.info.men.set(div.indus.length);
		adiv.info.symbolSet(div.bannerI);
		adiv.info.name().clear().add(div.name);
		adiv.morale.init();
		HTYPE type = player ? HTYPE.SOLDIER : HTYPE.ENEMY;
		
		int am = div.indus.length;
		
		if (am > RES.config().BATTLE.MEN_PER_DIVISION)
			throw new RuntimeException(div + " " + am);
		
		for (COORDINATE c : tmp) {
			if (men >= div.indus.length)
				break;
			
			Humanoid h = SETT.HUMANOIDS().create(race, c.x(), c.y(), type, CAUSE_ARRIVE.SOLDIER_RETURN);
			if (!h.isRemoved()) {
				div.indus[men].paste(h);
				men ++;
				for (EQUIPPABLE_MILITARY m : STATS.EQUIP().military_all()) {
					double dd = div.supplies[m.indexMilitary()];
					int amm = (int) dd;
					if (dd-amm > RND.rFloat())
						amm++;
					m.set(h.indu(), amm);
				}
				h.setDivision(adiv);
			}
		}

		
		if (men == 0)
			return false;
		
		d = d.next(-1);
		
		int x1 = tx1*C.TILE_SIZE+C.TILE_SIZEH;
		int y1 = ty1*C.TILE_SIZE+C.TILE_SIZEH;
		int x2 = x1 + d.x()*depthMax*C.TILE_SIZE;
		int y2 = y1 + d.y()*widthMax*C.TILE_SIZE;
		tmpDivs.clear();
		tmpDivs.add(adiv);
		ARMIES().placer.deploy(tmpDivs, x1, x2, y1, y2);
		
		adiv.initPosition();
		return true;
		
		
	}
	
	final static class WDivId {
		final int di;
		final WDivGeneration gen;
		
		public WDivId(int id, WDivGeneration gen) {
			this.di = id;
			this.gen = gen;
		}
		
	}
	
}
