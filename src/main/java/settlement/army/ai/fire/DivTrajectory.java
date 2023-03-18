package settlement.army.ai.fire;

import java.io.IOException;
import java.util.Arrays;

import init.config.Config;
import settlement.army.order.Copyable;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class DivTrajectory implements Copyable<DivTrajectory>{

	private final float[] data = new float[Config.BATTLE.MEN_PER_DIVISION*3];
	private boolean any;
	
	public DivTrajectory() {
		
	}
	
	void set(int pos, Trajectory t) {
		int i = pos*3;
		data[i] = (float) t.vx();
		data[i+1] = (float) t.vy();
		data[i+2] = (float) t.vz();
		any = true;
	}

	public boolean has(int pos) {
		return any && !Float.isNaN(data[pos*3]);
	}
	
	public boolean hasAny() {
		return any;
	}
	
	public Trajectory get(int pos) {
		if (!any)
			return null;
		pos*= 3;
		if (Float.isNaN(data[pos]))
			return null;
		tra.set(data[pos], data[pos+1], data[pos+2]);
		return tra;
	}
	
	@Override
	public void save(FilePutter file) {
		file.bool(any);
		file.fs(data);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		any = file.bool();
		file.fs(data);
	}
	
	@Override
	public void clear() {
		any = false;
		Arrays.fill(data, Float.NaN);
	}

	@Override
	public void copy(DivTrajectory toBeCopied) {
		for (int i = 0; i < data.length; i++)
			data[i] = toBeCopied.data[i];
		any = toBeCopied.any;
	}
	
	private static Trajectory tra = new Trajectory();
	
}
