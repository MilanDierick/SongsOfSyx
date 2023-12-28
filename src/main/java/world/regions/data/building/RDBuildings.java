package world.regions.data.building;

import java.io.IOException;

import game.boosting.*;
import game.faction.FACTIONS;
import init.D;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.tilemap.terrain.TFortification;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import util.keymap.ResColl;
import world.regions.Region;
import world.regions.data.RD.RDInit;
import world.regions.data.RD.RDUpdatable;
import world.regions.data.RDBoostCache;

public final class RDBuildings implements RDUpdatable{

	public final Boostable points;
	public final LIST<RDBuilding> all;
	public final LIST<RDBuilding> sorted;
	public final LIST<RDBuildingCat> cats;
	public final ResColl<RDBuilding> MAP;

	public final RDBoostCache levelRoad;
	public final RDBoostCache levelFarm;
	public final RDBoostCache levelMine;
	public final RDBoostCache levelWall;
	
	
	private static CharSequence ¤¤points = "¤Build Points";
	private static CharSequence ¤¤pointsD = "¤Build points are used to construct buildings in a region.";
	
	static {
		D.ts(RDBuildings.class);
	}
	
	public RDBuildings(RDInit init) throws IOException{
		
		init.upers.add(this);
		
		levelRoad = new RDBoostCache(init, "VISUAL_ROADS", "", "", null);
		levelFarm = new RDBoostCache(init, "VISUAL_FARMS", "", "", null);
		levelMine = new RDBoostCache(init, "VISUAL_MINE", "", "", null);
		levelWall = new RDBoostCache(init, "VISUAL_WALL", "", "", null) {
			@Override
			protected double pget(Region reg) {
				if (reg == FACTIONS.player().capitolRegion()) {
					double am = 0;
					for (TFortification f : SETT.TERRAIN().FORTIFICATIONS) {
						am += f.count();
					}
					return CLAMP.d(am/(SETT.TWIDTH*4.0), 0, 1);
				}
				return super.pget(reg);
			}
		};
		
		points = BOOSTING.push("BUILD_POINTS", 0, ¤¤points, ¤¤pointsD, UI.icons().s.hammer_lying, BoostableCat.WORLD); 
		init.deficiencies.register(points);
		
		ResFolder f = PATHS.WORLD().folder("building");
		
		Tree<RDBuildingCat> sort = new Tree<RDBuildingCat>(f.init.folders().length) {
			
			@Override
			protected boolean isGreaterThan(RDBuildingCat current, RDBuildingCat cmp) {
				return current.order > cmp.order;
			}
		};
		
		LinkedList<RDBuilding> all = new LinkedList<>();
		
		RDBuildingGeneration gen = new RDBuildingGeneration();
		for (String k : f.init.folders()) {
			sort.add(new RDBuildingCat(all, init, k, f.folder(k), gen));
		}
		
		ArrayListGrower<RDBuildingCat> cats = new ArrayListGrower<>();
		while(sort.hasMore())
			cats.add(sort.pollSmallest());
		
		this.cats = cats;

		ArrayListGrower<RDBuilding> sorted = new ArrayListGrower<>();
		
		for (RDBuildingCat c : cats)
			for (RDBuilding b : c.all)
				sorted.add(b);
		this.sorted = sorted;
		
		
	
		init.connectable.add(new ACTION() {
			
			@Override
			public void exe() {
				for (RDBuilding b : all) {
					b.connect(init);
					for (RDBuildingLevel l : b.levels) {
						l.cost *= RESOURCES.ALL().size();
					}
				}
			}
		});
		
		
		MAP = new ResColl<RDBuilding>("WORLD_BUILDINGS");
		this.all = new ArrayList<RDBuilding>(all);
		for (RDBuilding b : all) {
			MAP.push(b, b.kk);
		}

	}
	
//	private static final double ct = 1.0/(TIME.secondsPerDay*2);

	@Override
	public void update(Region reg, double time) {
//		for (RDBuilding b : all) {
//			if (b.level.get(reg) != b.targetLevel.get(reg)) {
//				double d = time*ct*b.constructionProgress.max(reg);
//				if (b.level.get(reg) > b.targetLevel.get(reg)) {
//					d*= 8;
//				}
//				while(d >= 1 && b.level.get(reg) != b.targetLevel.get(reg)) {
//					d -= 1;
//					construct(b, reg);
//				}
//				if (b.level.get(reg) != b.targetLevel.get(reg) && d > RND.rFloat()) {
//					construct(b, reg);
//					
//				}
//			}
//		}
		

	}
	
//	private void construct(RDBuilding b, Region reg) {
//		if (b.constructionProgress.isMax(reg)) {
//			b.constructionProgress.set(reg, 0);
//			if (b.level.get(reg) < b.targetLevel.get(reg)) {
//				b.level.inc(reg, 1);
//				completed.set(reg, 1);
//			}else {
//				b.level.inc(reg, -1);
//			}
//		}else {
//			b.constructionProgress.inc(reg, 1);
//		}
//	}



	@Override
	public void init(Region reg) {
//		for (RDBuilding b : all) {
//			if (b.level.get(reg) != b.targetLevel.get(reg)) {
//				b.level.set(reg, b.targetLevel.get(reg));
//			}
//		}
	}
	

	
}
