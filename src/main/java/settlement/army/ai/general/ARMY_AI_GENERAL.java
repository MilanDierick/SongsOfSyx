package settlement.army.ai.general;

import java.io.IOException;

import init.RES;
import init.sprite.SPRITES;
import settlement.army.ai.ARMY_AI.ArmyThread;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.SlaveThread;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

public final class ARMY_AI_GENERAL extends ArmyThread{

	private final Context t = new Context();
	private final Updater up = new Updater(t);
	
	private final ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
		
		@Override
		public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			RenderIterator it = data.onScreenTiles();

			 boolean add = false;
			 while(it.has()) {
				
				for (Debug d : debugs)
					if (d.isOn) {
						add = true;
						d.render(r, it);
					}
				
				
				it.next();
			}
			if (!add)
				remove();
		}
	};
	
	private final LIST<Debug> debugs = new ArrayList<>(
			new Debug("Battle: Lines") {
				
				@Override
				public void render(Renderer r, RenderIterator it) {
					if (t.groups.mark.get(it.tile())) {
						COLOR.YELLOW100.bind();
						SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
					}
					
				}
			},
			new Debug("Battle: Path") {
				
				@Override
				public void render(Renderer r, RenderIterator it) {
					if (t.pmap.path.abs.get(it.tx()>>2, it.ty()>>2) > 0) {
					if (t.pmap.path.is(it.tile()))
						COLOR.YELLOW100.bind();
					else
						COLOR.ORANGE100.bind();
					SPRITES.cons().ICO.smallup.render(r, it.x(), it.y());
				}
					
				}
			},
			new Debug("Battle: Targets") {
				
				@Override
				public void render(Renderer r, RenderIterator it) {
					if (t.artillery.targeted.get(AbsMap.getI(it.tx(), it.ty())) == 1) {
						COLOR.RED100.bind();
						SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
					}
				}
			}
	);
	
	public ARMY_AI_GENERAL() {
		
	}
	
	private abstract class Debug {
		
		private boolean isOn;
		
		Debug(String name){
			IDebugPanel.add(name, new ACTION() {
				
				@Override
				public void exe() {
					isOn = !isOn;
					ren.add();
				}
			});
		}
		
		public abstract void render(Renderer r, RenderIterator it);
		
	}
	
	
	@Override
	public void exe() {
		up.update();
	}

	@Override
	public void save(FilePutter file) {
		t.save(file);
		up.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		t.load(file);
		up.load(file);
	}

	@Override
	public void clear() {
		t.clear();
		up.clear();
	}

	@Override
	public SlaveThread thread() {
		return RES.generalThread1();
	}

	@Override
	public void init() {

	}
	
	@Override
	public void doInMainThread() {
		// TODO Auto-generated method stub
		super.doInMainThread();
	}

}
