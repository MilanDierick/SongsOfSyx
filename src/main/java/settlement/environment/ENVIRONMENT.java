package settlement.environment;

import java.io.IOException;

import init.biomes.CLIMATE;
import init.biomes.CLIMATES;
import settlement.main.CapitolArea;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;

public final class ENVIRONMENT extends SettResource{

	private CLIMATE climate = CLIMATES.COLD();
	public final SettSquareness squareness = new SettSquareness();
	public final SettEnvMap environment = new SettEnvMap();
	public final SettFish fish = new SettFish();
	public final SEService service = new SEService();
	
	private final ArrayList<EnvResource> all = new ArrayList<EnvResource>(
			environment, fish,
			service
			
			);
	
	@Override
	protected void generate(CapitolArea area) {
		this.climate = area.climate();
		for (EnvResource r : all)
			r.generate(area);
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(climate.index());
		for (EnvResource r : all)
			r.save(file);
		
	}
	
	@Override
	protected void update(float ds) {
		for (EnvResource r : all)
			r.update(ds);
		super.update(ds);
	}
	
	@Override
	protected void init(boolean loaded) {
		for (EnvResource r : all)
			r.init();
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		climate = CLIMATES.ALL().get(file.i());
		for (EnvResource r : all)
			r.load(file);
	}
	
	public CLIMATE climate() {
		return climate;
	}
	
	static abstract class EnvResource {
		
		protected abstract void update(double ds);
		protected abstract void generate(CapitolArea area);
		protected abstract void save(FilePutter file);
		protected abstract void load(FileGetter file) throws IOException;
		
		protected void init() {
			
		}
		
	}
	
}
