package view.world.generator.tools;

import static world.World.*;

import init.D;
import init.RES;
import init.biomes.*;
import init.config.Config;
import init.race.Race;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.ICON;
import init.sprite.UI.UI;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.info.GFORMAT;
import world.World;
import world.map.regions.CapitolPlacablity;
import world.map.terrain.WorldTerrainInfo;

public class UIWorldToolCapitolPlaceInfo {

	private static CharSequence ¤¤climate = "¤{0} do not prefer climate: {1}. It will be a bit harder to please them. They like:";
	private static CharSequence ¤¤isolated = "¤This location is isolated. Initially you will be left alone by other factions, but there will be little opportunity for trade.";
	private static CharSequence ¤¤neigh = "¤You will have few of your own species nearby, which might make expanding harder in late game.";
	private static CharSequence ¤¤diff = "¤Ample mineral deposits will stir envy and greed from bandits and rival states. Initial game will be more difficult.";
	
	static {D.ts(UIWorldToolCapitolPlaceInfo.class);}

	private final GuiSection s = new GuiSection();
	private final WorldTerrainInfo info = new WorldTerrainInfo();
	private final WorldTerrainInfo area = new WorldTerrainInfo();
	
	public UIWorldToolCapitolPlaceInfo() {

		int m = 170;
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(World.CLIMATE().getter.get(info.tx, info.ty).name);
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
			
			
			
			int y1 = (i%8)*ICON.MEDIUM.SIZE;
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
	
	public void placeInfo(GBox b, int tx1, int ty1, Race race) {
		
	
		info.initCity(tx1, ty1);
		
		b.add(s);
		b.NL(8);
		
		if (race != null) {
			int cx = tx1+CapitolPlacablity.TILE_DIM/2;
			int cy = ty1+CapitolPlacablity.TILE_DIM/2;
			CLIMATE climate = World.CLIMATE().getter.get(cx, cy);
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
				b.NL(8);
			}
			
			for (Minable m : RESOURCES.minables().all()) {
				if (info.minable(m) > 0) {
					b.add(b.text().warnify().add(¤¤diff));
					b.NL(8);
					break;
				}
			}
			
			
			
			int size = getSize(cx, cy,  Config.WORLD.REGION_SIZE*3);
			
			if (size < Config.WORLD.REGION_SIZE*2) {
				b.add(b.text().warnify().add(¤¤isolated));
				b.NL(8);
			}
			
			double v = 0;
			for (TERRAIN te : TERRAINS.ALL()) {
				double t = area.get(te).getD()*race.population().terrain(te);
				for (CLIMATE c : CLIMATES.ALL())
					v += t * race.population().climate(c)*area.get(c).getD();
			}
			v /= (race.population().maxClimate()*race.population().maxTerrain());
			
			if (v < 0.25) {
				b.add(b.text().warnify().add(¤¤neigh));
				b.NL(8);
			}
			
			
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
			for (DIR d : DIR.ORTHO) {
				int toX = t.x()+d.x();
				int toY = t.y()+d.y();
				if (!World.IN_BOUNDS(toX, toY))
					continue;
				if (WATER().has.is(t.x(), t.y())) {
					if (!WATER().canCrossRiver(t.x(), t.y(), toX, toY))
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
