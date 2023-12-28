package settlement.entity.humanoid.ai.danger;

import settlement.entity.humanoid.ai.main.AIModule;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public final class AIModuleDanger {

	public final LIST<AIModule> all;
	
	public AIModuleDanger(){
		ArrayListGrower<AIModule> all = new ArrayListGrower<>();
		all.add(new AIModule_Exposure());
		all.add(new AIModule_Health());
		all.add(new AIModule_Starvation());
		this.all = all;
	}
	
}
