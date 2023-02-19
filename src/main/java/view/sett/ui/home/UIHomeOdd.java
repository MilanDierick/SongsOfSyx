package view.sett.ui.home;

import java.util.Arrays;

import game.GAME;
import init.D;
import init.race.RACES;
import init.race.Race;
import init.sprite.ICON;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.*;
import settlement.main.SETT;
import settlement.room.home.HOME_TYPE;
import settlement.room.home.house.HomeHouse;
import settlement.stats.STATS;
import snake2d.util.datatypes.AREA;
import util.gui.misc.GBox;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class UIHomeOdd extends PlacableMulti{

	private static CharSequence ¤¤name = "O-Mover";
	private static CharSequence ¤¤desc = "Odd jobbers will automatically move out if an employed subject needs their home. This tool manually moves random oddjobbers to desired housing.";
	private static CharSequence ¤¤prob = "Must be placed on a house with vacancies.";
	private static CharSequence ¤¤odd = "No oddjobbers to move!";
	private static CharSequence ¤¤oddNo = "No oddjobbers for specific house. Change assignment for it.";
	
	static {
		D.ts(UIHomeOdd.class);
	}
	
	private int updateTick;
	private int[][] oddjobbers = new int[HCLASS.ALL.size()][RACES.all().size()]; 
	private int total;
	private int[] totalC = new int[HCLASS.ALL.size()];
	
	public UIHomeOdd() {
		super(¤¤name, ¤¤desc, new ICON.MEDIUM.Twin(SPRITES.icons().m.workshop, SPRITES.icons().s.arrow_right));
		
		
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		
		if (updateTick != GAME.updateI()) {
			updateTick = GAME.updateI();
			total = 0;
			for (int[] i : oddjobbers) {
				Arrays.fill(i, 0);
			}
			Arrays.fill(totalC, 0);
			for (HTYPE c : HTYPE.ALL()) {
				if (!c.works)
					continue;
				for (Race r : RACES.all()) {
					int am = STATS.POP().pop(r, c);
					
					total += am;
					oddjobbers[c.CLASS.index()][r.index()] += am;
					totalC[c.CLASS.index()] += am;
				}
			}
			
			for (HCLASS c : HCLASS.ALL()) {
				for (Race r : RACES.all()) {
					int am = STATS.WORK().EMPLOYED.stat().data(c).get(r);
					
					total -= am;
					oddjobbers[c.index()][r.index()] -= am;
					totalC[c.index()] -= am;
				}
				
			}
		}
		
		if (total <= 0)
			return ¤¤odd;
		
		HomeHouse h = SETT.ROOMS().HOMES.HOME.house(tx, ty, this);
		if (h == null)
			return ¤¤prob;
		
		if (h.occupants() >= h.occupantsMax()) {
			h.done();
			return ¤¤prob;
		}
		
		HOME_TYPE a = h.availability();
		h.done();
		
		if (a == null)
			return ¤¤prob;
		
		if (a.clas() != null) {
			if (totalC[a.clas().index()] < 0)
				return ¤¤oddNo;
		}
		
		if (a.race() != null) {
			if (oddjobbers[a.clas().index()][a.race().index()] < 0)
				return ¤¤oddNo;
		}
		
		return null;
	}
	
	@Override
	public void placeInfo(GBox b, int oktiles, AREA a) {
		
		super.placeInfo(b, oktiles, a);
	}
	

	int ie = 0;
	
	
	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		
		HomeHouse h = SETT.ROOMS().HOMES.HOME.house(tx, ty, this);
		
		if (h == null)
			return;
		
		if (h.service().isSameAs(tx, ty)) {
			HOME_TYPE t = h.availability();
			if (t != null && h.occupants() < h.occupantsMax()) {
				ENTITY[] ee = SETT.ENTITIES().getAllEnts();
				for (int i = 0; i < ee.length; i++) {
					if (ie >= ee.length)
						ie = 0;
					ENTITY e = ee[ie];
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (STATS.WORK().EMPLOYED.get(a) == null && t.isValid(a)) {
							STATS.HOME().GETTER.set(a, h);
							if (h.availability() == null || h.occupants() >= h.occupantsMax()) {
								break;
							}
						}
					}
					ie ++;
					
				}
			}
		}
		h.done();
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return SETT.ROOMS().HOMES.HOME.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
}
