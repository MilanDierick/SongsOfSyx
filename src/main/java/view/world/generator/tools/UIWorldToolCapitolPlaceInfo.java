package view.world.generator.tools;

import static world.WORLD.*;

import game.boosting.BoostSpec;
import game.boosting.BoostableCat;
import init.D;
import init.RES;
import init.biomes.*;
import init.race.Race;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.BOOLEANO;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.WConfig;
import world.WORLD;
import world.map.terrain.WorldTerrainInfo;
import world.regions.centre.WCentre;

public class UIWorldToolCapitolPlaceInfo {

	private static CharSequence ¤¤climate = "¤{0} do not prefer climate: {1}. It will be a bit harder to please them. They like:";
	private static CharSequence ¤¤isolated = "¤This location is isolated. Initially you will be left alone by other factions, but there will be little opportunity for trade.";
	private static CharSequence ¤¤neigh = "¤You will have few of your own species nearby, which might make expanding harder in late game.";
	private static CharSequence ¤¤Water = "¤A spot with little fresh water will be more difficult.";
	private static CharSequence ¤¤Forest = "¤A spot with little forest will be more difficult.";
	private static CharSequence ¤¤Minerals = "¤A spot without any minable resources will be more difficult.";
	private static CharSequence ¤¤Fertility = "¤A spot with low fertility will be more difficult.";
	

	static {D.ts(UIWorldToolCapitolPlaceInfo.class);}

	private final GuiSection s = new GuiSection();
	private final WorldTerrainInfo info = new WorldTerrainInfo();
	private final WorldTerrainInfo area = new WorldTerrainInfo();
	
	public UIWorldToolCapitolPlaceInfo() {

		int m = 170;
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(WORLD.CLIMATE().getter.get(info.tx, info.ty).name);
			}
			
		}.hh(CLIMATES.INFO().name, m));
		
		s.addDown(2, new GHeader.HeaderHorizontal(DicMisc.¤¤Fertility, new GMeter.GMeterSprite(GMeter.C_REDGREEN, info.fertility(), 64, 12), m));
		
		s.body().incrH(14);
		
		for (TERRAIN t : TERRAINS.ALL()) {
			s.add(t.icon(), 0, s.body().y2()-2);
			s.addRightC(4, new GText(UI.FONT().S, t.name).lablifySub());
			s.addCentredY(new GMeter.GMeterSprite(GMeter.C_ORANGE, info.get(t), 64, 12), m);
		}
		
		GuiSection ss = new GuiSection();
		int i = 0;
		for (Minable min : RESOURCES.minables().all()) {
			
			
			
			int y1 = (i%8)*Icon.M;
			int x1 = (i/8)*(m+64);
			
			
			ss.add(min.resource.icon(), x1, y1);
			ss.addRightC(4, new GText(UI.FONT().S, min.resource.name).lablifySub());
			ss.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, info.minable(min));
				}
			}, x1+m);
			i++;
		}
		
		s.addRelBody(32,  DIR.E, ss);

		s.body().incrW(48);
		
	}
	
	private final BOOLEANO<BoostSpec> filter = new BOOLEANO<BoostSpec>() {

		@Override
		public boolean is(BoostSpec t) {
			return (t.boostable.cat.typeMask & BoostableCat.TYPE_SETT) != 0;
		}
		
	};
	
	public void placeInfo(GBox b, int tx1, int ty1, Race race) {
		
	
		info.initCity(tx1, ty1);
		int cx = tx1+WCentre.TILE_DIM/2;
		int cy = ty1+WCentre.TILE_DIM/2;
		
		{
			CLIMATE climate = WORLD.CLIMATE().getter.get(cx, cy);
			
			b.textLL(CLIMATES.INFO().name);
			b.tab(6);
			b.text(WORLD.CLIMATE().getter.get(cx, cy).name);
			b.NL();
			
			climate.boosters.hover(b, 1.0, null, filter, -1);
			
			b.NL();
			
			double cl = race.population().climate(climate);
			if (cl < race.population().maxClimate()) {
				GText t = b.text();
				t.add(¤¤climate);
				t.insert(0, race.info.names);
				t.insert(1, climate.name);
				for (CLIMATE c : CLIMATES.ALL()) {
					if (race.population().climate(c) >= race.population().maxClimate())
						t.s().add(c.name).add(',');
				}
				t.warnify();
				b.add(t);
				
			}
			b.sep();
		}
		
		{
			
			b.textLL(DicMisc.¤¤Fertility);
			b.tab(6);
			b.add(GFORMAT.perc(b.text(), info.fertility().getD()));
			b.NL(8);
			
			double v = 0;
			for (TERRAIN te : TERRAINS.ALL()) {
				double t = area.get(te).getD()*race.population().terrain(te);
				if (info.get(te).getD() > 0) {
					b.add(te.icon());
					b.textL(te.name);
					b.tab(6);
					b.add(GFORMAT.percGood(b.text(), info.get(te).getD()));
					b.NL();
				}
				
				for (CLIMATE c : CLIMATES.ALL())
					v += t * race.population().climate(c)*area.get(c).getD();
			}
			v /= (race.population().maxClimate()*race.population().maxTerrain());
			
			if (v < 0.25) {
				b.add(b.text().warnify().add(¤¤neigh));
				
			}
			b.sep();
		}
		
		{
			boolean mi = false;
			for (Minable min : RESOURCES.minables().all()) {
				
				if (info.minable(min) > 0) {
					b.add(min.resource.icon());
					b.textL(min.resource.name);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), info.minable(min)));
					b.NL();
					mi = true;
				}
			}
			
			if (!mi) {
				b.add(b.text().warnify().add(¤¤Minerals));
				b.NL();
			}
			
		}
		
		b.NL(8);
		
		int size = getSize(cx, cy,  WConfig.data.REGION_SIZE);
		
		if (size < WConfig.data.REGION_SIZE/2) {
			b.add(b.text().warnify().add(¤¤isolated));
			b.NL(8);
		}
		
		if (info.fertility().getD() < 0.3) {
			b.add(b.text().warnify().add(¤¤Fertility));
			b.NL();
		}
		
		
		if (info.get(TERRAINS.FOREST()).getD() < 0.1) {
			b.add(b.text().warnify().add(¤¤Forest));
			b.NL();
		}
		
		if (info.get(TERRAINS.WET()).getD() <= 0.1) {
			b.add(b.text().warnify().add(¤¤Water));
			b.NL();
		}
	
	}
	
	private int getSize(int sx, int sy, int max) {
		RES.flooder().init(this);
		RES.flooder().pushSloppy(sx, sy, 0);
		area.clear();
		int size = 0;
		while(RES.flooder().hasMore() && size < max) {
			PathTile t = RES.flooder().pollSmallest();
			size ++;
			area.add(t.x(), t.y());
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int toX = t.x()+d.x();
				int toY = t.y()+d.y();
				if (!WORLD.IN_BOUNDS(toX, toY))
					continue;
				if (WATER().has.is(t.x(), t.y())) {
					if (!WATER().canCrossByLand(t.x(), t.y(), toX, toY))
						continue;
				}
				if (MOUNTAIN().coversTile(toX, toY))
					continue;
				
				if (FOREST().amount.get(t.x(), t.y()) == 1.0 && FOREST().amount.get(toX, toY) == 1)
					continue;
				
				RES.flooder().pushSmaller(toX,  toY, t.getValue()+d.tileDistance());
				
			}
		}
		area.divide(size);
		RES.flooder().done();
		return size;
		
	}
	
	
}
