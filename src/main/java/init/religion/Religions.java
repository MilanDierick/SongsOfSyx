package init.religion;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.util.sets.*;
import util.keymap.RCollection;

public class Religions {

	private static Religions self;
	private final ArrayListGrower<Religion> all = new ArrayListGrower<>();
	private final RCollection<Religion> MAP;
	
	public Religions() throws IOException{
		self = this;
		KeyMap<Religion> keys = new KeyMap<>();
		
		for (String k : PATHS.INIT().getFolder("religion").getFiles()) {
			Religion r = new Religion(k, all.size());
			all.add(r);
			keys.put(k, r);
		}
		
		
		MAP = new RCollection<Religion>("RELIGION", keys) {

			@Override
			public Religion getAt(int index) {
				return all.get(index);
			}

			@Override
			public LIST<Religion> all() {
				return all;
			}

			
		};
		for (Religion r : all) {
			r.init();
		}
	}
	
	public static void init() throws IOException{
		new Religions();
		
	}
	
	public static LIST<Religion> ALL(){
		return self.all;
	}
	
	public static RCollection<Religion> MAP(){
		return self.MAP;
	}
	
}
