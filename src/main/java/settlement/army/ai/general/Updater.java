package settlement.army.ai.general;

import snake2d.Renderer;
import snake2d.util.file.SAVABLE;
import util.rendering.RenderData.RenderIterator;

abstract class Updater implements SAVABLE{

	
	public static class States {
		
		public final StepAttackEnemyNear attackEnemyNear;
		public final StepLineCharge stepLineCharge;
		public final StepMoveToLine stepMoveToLine;
		public final StepMoveToThrone stepMoveToThrone;
		public final StepRunner stepRunner;
		public final StepArtilleryBombard stepBombard;
		
		States(Context context){
			attackEnemyNear = new StepAttackEnemyNear(context);
			stepLineCharge = new StepLineCharge();
			stepMoveToLine = new StepMoveToLine(context);
			stepMoveToThrone = new StepMoveToThrone(context);
			stepRunner = new StepRunner();
			stepBombard = new StepArtilleryBombard(context);
		}
		
	}
	
	public abstract void update();
	public abstract void render(Renderer r, RenderIterator it);
}
