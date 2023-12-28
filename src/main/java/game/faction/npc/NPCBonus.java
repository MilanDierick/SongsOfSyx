package game.faction.npc;

import java.io.IOException;

import game.boosting.*;
import game.faction.npc.ruler.RTraits;
import snake2d.util.file.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.data.DOUBLE_O;
import world.regions.data.pop.RDRace;

public final class NPCBonus extends NPCResource implements DOUBLE_O<Boostable>, BOOSTABLE_O{

	private final int MM = 128-1;
	private final double[] bos = new double[128];

	public final FactionNPC faction;
	
	public NPCBonus(FactionNPC faction, LISTE<NPCResource> all) {
		super(all);
		randomize();
		this.faction = faction;
	}
	
	@Override
	public double getD(Boostable bo) {
		return get(bo.index());
	}
	
	public double get(int ran) {
		int ii = ran&MM;
		if (faction.court().king() == null || faction.court().king().roy() == null)
			return bos[ii]*0.5;
		return bos[ii]*(0.5 + 0.5 * faction.court().king().roy().trait(RTraits.get().competence));
	}
	
	public void randomize() {
		
		for (int i = 0; i < bos.length; i++) {
			bos[i] = 0.1 + 0.7*i/(bos.length-1);
			bos[i] = CLAMP.d(bos[i], 0, 1);
		}
		
		for (int i = 0; i < bos.length; i++) {
			double d = bos[i];
			int k = RND.rInt(bos.length);
			bos[i] = bos[k];
			bos[k] = d;
		}
		
	}

	@Override
	protected SAVABLE saver() {
		return new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				file.ds(bos);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				file.ds(bos);
			}
			
			@Override
			public void clear() {
				
			}
		};
	}

	@Override
	protected void update(FactionNPC faction, double seconds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void generate(RDRace race, FactionNPC faction, boolean fromScratch) {
		randomize();
	}

	@Override
	public double boostableValue(Boostable bo, BValue v) {
		return v.vGet(this);
	}
	
	
	
}
