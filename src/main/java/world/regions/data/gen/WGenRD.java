package world.regions.data.gen;

import static world.WORLD.*;

import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import world.WorldGen;
import world.regions.Region;
import world.regions.data.RD;
import world.regions.data.RD.RDInit;

public abstract class WGenRD {

	private final RDInit init;
	
	public WGenRD(RDInit init) {
		this.init = init;
	}
	
	public abstract void clear();
	
	public void generate(ACTION printer) {
		printer.exe();
		clear();
		for (Region r : REGIONS().active()) {
			for (ACTION_O<Region> g : init.gens) {
				g.exe(r);
			}
			RD.UPDATER().BUILD(r);
			
		}
		printer.exe();
	}
	
	public void makeFactions(WorldGen gen, ACTION printer) {

		new GeneratorFactions().generate(init, gen, printer);
		
	}
	
	public void finish(WorldGen gen) {
		new GeneratorFinish(init, gen);
	}
	
	
	
}
