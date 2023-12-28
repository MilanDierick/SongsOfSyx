package init.race;

import java.util.Arrays;
import java.util.Comparator;

import init.biomes.BUILDING_PREF;
import init.biomes.BUILDING_PREFS;
import init.resources.*;
import init.resources.RBIT.RBITImp;
import init.sprite.UI.Icon;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.*;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.keymap.KEY_COLLECTION;
import view.main.VIEW;

public final class RacePreferrence {

	public LIST<ResG> food;
	public final RBIT foodMask;
	private final double[] structure = new double[BUILDING_PREFS.ALL().size()];
	private final double[] work = new double[SETT.ROOMS().employment.ALLS().size()];
	private final double[] others = new double[RACES.all().size()];
	private Json jj;
	private final REN_PREF hov;
	public Json worldBuildingOverride;
	public final LIST<ROOM_RESTHOME> resthomes;
	
	RacePreferrence(Json data, Race race) {

		data = data.json("PREFERRED");
		food = new ArrayList<>(RESOURCES.EDI().MAP.getManyByKeyWarn("FOOD", data));
		if (food.size() == 0)
			data.error("Must have a favorite food!", "FOOD");
		jj = data;
		
		RBITImp m = new RBITImp();
		for (ResG e : food)
			m.or(e.resource);
		foodMask = m;
		{
			
			for (BUILDING_PREF p : BUILDING_PREFS.ALL())
				structure[p.index()] = p.defaultPref[race.index];
			
			BUILDING_PREFS.MAP().fill(structure, data, 0, 1);
		}
		hov = new REN_PREF(race, false, Icon.M*10);
		
		
		{
			
			
			for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS())
				work[e.eindex()] = e.defaultFullfillment;
			
			new RoomsJson("WORK", data) {
				
				@Override
				public void doWithTheJson(RoomBlueprintImp pp, Json j, String key) {
					if (pp != null && pp instanceof RoomBlueprintIns<?> && ((RoomBlueprintIns<?>) pp).employment() != null) {
						work[((RoomBlueprintIns<?>) pp).employment().eindex()] = j.d(key, 0, 1);
					}else if (!key.equals(KEY_COLLECTION.WILDCARD)){
						j.errorGet("Not a workable room", key);
					}
				}
			};
			
			
		}
		
		{
			int rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					rh++;
				}
			}
			ROOM_RESTHOME[] hh = new ROOM_RESTHOME[rh];
			rh = 0;
			for (ROOM_RESTHOME h : SETT.ROOMS().RESTHOMES) {
				if (work[h.employment().eindex()] > 0) {
					hh[rh++] = h;
				}
			}
			
			Arrays.sort(hh, new Comparator<ROOM_RESTHOME>() {

				@Override
				public int compare(ROOM_RESTHOME o1, ROOM_RESTHOME o2) {
					return work[o1.employment().eindex()] > work[o2.employment().eindex()] ? 1 : -1;
				}
			});
			
			resthomes = new ArrayList<>(hh);
		}
		
		Arrays.fill(others, 1);
//		
//		
//		
//		if (data.has("OTHER_RACES")) {
//			RACES.map().fill("OTHER_RACES", others, data, -100000, 1000000);
//		}
//		
//		
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//		
//		for (int i = 0; i < others.length; i++) {
//			min = Math.min(min, others[i]+1);
//			max = Math.max(max, others[i]+1);
//		}
//		
//		double delta = max-min;
//		if (delta == 0)
//			Arrays.fill(othersNor, 1);
//		else {
//			for (int i = 0; i < others.length; i++) {
//				double d = others[i] +1 - min;
//				double dd = max-min;
//				if (dd != 0)
//					d /= dd;
//				othersNor[i] = d;
//			}
//		}
		
		if (data.has("WORLD_BUILDING_OVERRIDE")) {
			worldBuildingOverride = data.json("WORLD_BUILDING_OVERRIDE");
		}
		
	}
	
	static void init() {
		
		boolean[][] hits = new boolean[RACES.all().size()][RACES.all().size()];
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race race = RACES.all().get(ri);
			Json j = race.pref().jj;
			
			RACES.map().new KJson("OTHER_RACES_REVERSE", j) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					double v = j.d(key, 0, 1);
					if (!hits[s.index][race.index()])
						s.pref().others[race.index()] = v;
				}
			};
			
			race.pref().others[race.index()] = 1.0;
		}
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race race = RACES.all().get(ri);
			Json j = race.pref().jj;
			race.pref().jj = null;
			RACES.map().new KJson("OTHER_RACES", j) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					double v = j.d(key, 0, 1);
					if (!isWeak) {
						hits[race.index()][s.index()] = true;
						hits[s.index()][race.index()] = true;
					}
					race.pref().others[s.index()] = v;
				}
			};
			
			race.pref().others[race.index()] = 1.0;
		}

		
		
	}
	
	public ResG prefAllowedFood(Humanoid a) {
		RBIT b = STATS.FOOD().fetchMask(a);
		double ma = 0;
		for (ResG f : food) {
			if (b.has(f.resource))
				ma++;
		}
		if (ma == 0)
			return food.rnd();
		ma *= RND.rFloat();
		for (ResG f : food) {
			if (b.has(f.resource)) {
				if (ma <= 1)
					return f;
				ma -= 1;
			}
		}
		return food.rnd();
	}
	
	public double structure(BUILDING_PREF p) {
		return structure[p.index()];
	}

	
	public double getWork(RoomEmploymentSimple e) {
		return work[e.eindex()];
	}
	
	public double race(Race race) {
		return others[race.index];
	}
	
//	public double other(Race race) {
//		return others[race.index];
//	}
//	
//	public double otherNormalized(Race race) {
//		return othersNor[race.index];
//	}
	

	
	public void hoverOther(GUI_BOX box) {
		GBox b = (GBox) box;
		b.textLL(STATS.ENV().OTHERS.info().name);
		b.add(hov);
	}
	
	private static CharSequence ¤¤preference = "preference";
	
	public static class REN_PREF extends HoverableAbs{
		
		private boolean big;
		private final int dim;
		private final Race rr;
		
		public REN_PREF(Race rr, boolean big, int width) {
			this.big = big;
			this.rr = rr;
			dim = big ? Icon.L+8 : Icon.M+6;
			
			int w = Math.min(dim*(width/dim), dim*RACES.all().size());
			
			body.setWidth(w);
			body.setHeight(dim + RACES.all().size()/(w/dim));
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			int x1 = body().x1();
			int y1 = body().y1();
			for (Race ra : RACES.all()) {
				if (ra == rr)
					continue;
				ColorImp col = ColorImp.TMP;
				double l = rr.pref().race(ra);
				GCOLOR.UI().badToGood(col, l);
				col.render(r, x1+1, x1+dim-1, y1+1, y1+dim-1);
				col.shadeSelf(0.5);
				col.renderFrame(r, x1+1, x1+dim-1, y1+1, y1+dim-1, 0, 1);
				SPRITE s = big ? ra.appearance().iconBig : ra.appearance().icon;
				s.renderC(r, x1+dim/2, y1+dim/2);
				x1 += dim;
				if (x1 >= body.x2()) {
					x1 = body.x1();
					y1 +=dim;
				}
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			int tx = VIEW.mouse().x();
			int ty = VIEW.mouse().y();
			int x1 = body().x1();
			int y1 = body().y1();
			for (Race ra : RACES.all()) {
				if (ra == rr)
					continue;
				if (tx >= x1 && tx < x1+dim && ty >= y1 && ty < y1+dim) {
					b.title(ra.info.name);
					b.textLL(¤¤preference);
					b.tab(7);
					b.add(GFORMAT.perc(b.text(), rr.pref().race(ra)));
					b.NL();
				}
				x1 += dim;
				if (x1 >= body.x2()) {
					x1 = body.x1();
					y1 +=dim;
				}
			}
		}
		
		
	}
	
}
