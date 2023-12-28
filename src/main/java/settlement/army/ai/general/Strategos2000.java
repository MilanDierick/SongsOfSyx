package settlement.army.ai.general;

import java.io.IOException;

import init.RES;
import settlement.army.ai.ARMY_AI.ArmyThread;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.SlaveThread;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

public class Strategos2000 extends ArmyThread {

	private final Context context;
	private Updater current;
	private final Updater offense;
	private int oldDivs;
	private boolean debug = false;

	public Strategos2000() {
		this.context = new Context();

		Updater.States st = new Updater.States(context);
		offense = new UpdaterOffense(context, st);
		current = offense;
		
		IDebugPanel.add("Battle General Debug", new ACTION() {
			ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {

				@Override
				public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
					RenderIterator it = data.onScreenTiles();

					if (current == null)
						return;
					
					while (it.has()) {
						current.render(r, it);
						it.next();
					}
					if (!debug)
						remove();
				}
			};
			@Override
			public void exe() {
				debug = !debug;
				ren.add();
			}
		});
	}

	@Override
	public void clear() {
		offense.clear();
	}

	@Override
	public void save(FilePutter file) {
		file.mark(context);
		context.save(file);
		file.mark(offense);
		offense.save(file);
		file.mark(offense);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.check(context);
		context.load(file);
		file.check(offense);
		offense.load(file);
		file.check(offense);
	}


	public enum State {

		ATTACK

	}

	@Override
	public void exe() {
		int newDivs = 0;
		for (int di = 0; di < context.army.divisions().size(); di++) {
			if (context.army.divisions().get(di).order().active())
				newDivs++;
		}
		if (newDivs == 0)
			return;
		if (newDivs != oldDivs) {
			clear();
			oldDivs = newDivs;
		}

		offense.update();

	}

	@Override
	public SlaveThread thread() {
		return RES.generalThread1();
	}

	@Override
	public void init() {
		offense.clear();
	}

}
