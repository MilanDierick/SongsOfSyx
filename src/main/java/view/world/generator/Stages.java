package view.world.generator;

import init.D;
import snake2d.util.rnd.RND;
import world.World;
import world.WorldGen;

final class Stages {

	static CharSequence ¤¤generate = "¤generate";
	static CharSequence ¤¤regenerate = "¤regenerate";
	static CharSequence ¤¤start = "¤start";
	static CharSequence ¤¤home = "¤home";
	
	
	static {
		D.ts(Stages.class);
	}
	
	final Intr dummy;
	final WorldViewGenerator v;
	
	Stages(WorldViewGenerator v){
		World.GEN().seed = RND.rInt(Integer.MAX_VALUE);
		
		this.v = v;
		dummy = new Intr(v);
		set();
		
	}
	
	public void set() {
		
		WorldGen g = World.GEN();
		
		if (!g.hasGeneratedTerrain) {
			new StagePickRace(this);
		}else if (!g.hasPlacedCapitol) {
			new StageCapitol(this, true);
		}else {
			dummy.add(null);
		}
		
		
	}
	
	void reset() {
		dummy.add(null);
		v.tools.place(null);
	}
	
	void race() {
		new StagePickRace(this);
	}
	
	void titles() {
		new StagePickTitles(this);
	}
	
	void terrain() {
		new StageTerrain(this);
	}
	
	
	void finish() {
		new StageFinish(this);
	}
	
	
}
