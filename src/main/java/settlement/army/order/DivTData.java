package settlement.army.order;

import java.io.IOException;

import init.config.Config;
import settlement.army.Div;
import settlement.army.ai.divs.PathDiv;
import settlement.army.ai.fire.DivTrajectory;
import settlement.army.ai.util.DivTDataStatus;
import settlement.army.formation.DivFormation;
import settlement.army.formation.DivPosition;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class DivTData {

	private volatile boolean active;
	
	public final DivTDataResource<DivPosition> current;
	public final DivTDataResource<DivFormation> next;
	public final DivTDataResource<DivFormation> dest;
	public final DivTDataResource<PathDiv> path;

	public final DivTDataResource<DivTDataInfo> info;
	public final DivTDataResource<DivTDataStatus> status;
	public final DivTDataResource<DivTDataTask> task;
	public final DivTDataResource<DivTrajectory> trajectory;
	public final int index;
	
	private final static DivTDataInfo tmp = new DivTDataInfo();
	
	DivTData(int index) {
		this.index = index;
		
		next = new DivTDataResource<DivFormation>(new DivFormation(Config.BATTLE.MEN_PER_DIVISION));
		
		dest = new DivTDataResource<DivFormation>(new DivFormation(Config.BATTLE.MEN_PER_DIVISION));
		path = new DivTDataResource<PathDiv>(new PathDiv());
		current = new DivTDataResource<DivPosition>(new DivPosition());
		status = new DivTDataResource<DivTDataStatus>(new DivTDataStatus());
		info = new DivTDataResource<DivTDataInfo>(new DivTDataInfo());
		task = new DivTDataResource<DivTDataTask>(new DivTDataTask());
		trajectory = new DivTDataResource<DivTrajectory>(new DivTrajectory());
	}
	
	public void update(Div info) {
		tmp.set(info);
		this.info.set(tmp);
		current.set(info.current());
		active = info.current().deployed() > 0 && info.settings.mustering() && info.menNrOf() > 0;
	}
	
	void save(FilePutter file) {
		file.bool(active);
		current.save(file);
		next.save(file);
		dest.save(file);
		path.save(file);
		info.save(file);
		status.save(file);
		task.save(file);
		trajectory.save(file);
	}
	
	void load(FileGetter file) throws IOException {
		active = file.bool();
		current.load(file);
		next.load(file);
		dest.load(file);
		path.load(file);
		info.load(file);
		status.load(file);
		task.load(file);
		trajectory.load(file);
	}
	
	void clear() {
		active = false;
		current.clear();
		next.clear();
		dest.clear();
		path.clear();
		info.clear();
		status.clear();
		task.clear();
		trajectory.clear();
	}
	
	public boolean active() {
		return active;
	}

	
}
