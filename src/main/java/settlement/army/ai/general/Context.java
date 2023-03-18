package settlement.army.ai.general;

import java.io.IOException;

import init.C;
import init.config.Config;
import settlement.army.Army;
import settlement.army.Div;
import settlement.army.ai.fire.DivTrajectory;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivDeployer;
import settlement.army.formation.DivFormation;
import settlement.army.order.DivTDataTask;
import settlement.main.SETT;
import settlement.misc.util.TileRayTracer;
import settlement.room.main.throne.THRONE;
import snake2d.CircleCooIterator;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

class Context implements SAVABLE{

	public final Army army = SETT.ARMIES().enemy();
	public final LIST<GDiv> divs;
	{
		
		ArrayList<GDiv> divs = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
		for (int i = 0; i < divs.max(); i++)
			divs.add(new GDiv(i, this));
		this.divs = divs;
	}
	public final PathUtilOnline flooder = new PathUtilOnline(C.SETTLE_TSIZE);
	public final PathingUtil pUtil = new PathingUtil(this);
	public final Pathing pathing = new Pathing(this);
	public final PathingMap pmap = new PathingMap(this);
	public final DivDeployer deployer = new DivDeployer(flooder);
	public final CircleCooIterator circle = new CircleCooIterator(25, flooder.getFlooder());
	public final ArrayList<Div> tmpList = new ArrayList<>(Config.BATTLE.DIVISIONS_PER_ARMY);
	public final DivFormation form = new DivFormation();
	public final DivFinder divFinder = new DivFinder(this);
	public final AbsMap absMap = new AbsMap(1);
	public final Groups groups = new Groups(this);
	public final TileRayTracer tracer = new TileRayTracer(20);
	
	public final DivTDataStatus status = new DivTDataStatus();
	public final DivTrajectory traj = new DivTrajectory();
	public final DivTDataTask task = new DivTDataTask();
	public final ArtilleryTargets artillery = new ArtilleryTargets(this);
	
	public Context() {
		
	}

	@Override
	public void save(FilePutter file) {
		for (GDiv d : divs)
			d.save(file);
		pmap.save(file);
		groups.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (GDiv d : divs)
			d.load(file);
		pmap.load(file);
		groups.load(file);
	}

	@Override
	public void clear() {
		for (GDiv d : divs)
			d.clear();
		pmap.clear();
		groups.clear();
	}
	
	public COORDINATE getDestCoo() {
		return THRONE.coo();
	}
	
}
