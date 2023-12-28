package settlement.overlay;

import settlement.environment.SettEnvMap.SettEnv;
import snake2d.Renderer;
import snake2d.util.sets.LISTE;
import util.rendering.RenderData.RenderIterator;

final class Env extends Addable{

	final SettEnv envThing;
	
	Env(LISTE<Addable> all, SettEnv env) {
		super(all, env.key, env.name, env.desc, true, false);
		this.envThing = env;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		renderUnder(envThing.getView(it.tx(), it.ty()), r, it);
	}


	
}
