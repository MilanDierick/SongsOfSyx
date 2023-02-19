package settlement.army.ai.divs;

import init.C;
import settlement.army.formation.DivDeployer;
import snake2d.CircleCooIterator;
import snake2d.PathUtilOnline;

class Tools {

	public final PathUtilOnline pather = new PathUtilOnline(C.SETTLE_TSIZE);
	public final DivDeployer deployer = new DivDeployer(pather);
	public final ToolMover mover = new ToolMover(pather);
	public final ToolsDiv div = new ToolsDiv(this);
	public final PathCost pathCost = new PathCost();
	public final ToolsWalk walk = new ToolsWalk(this);
	public final ToolsColl coll = new ToolsColl(this);
	public final ToolsBattle battle = new ToolsBattle(this);
	public final CircleCooIterator circle = new CircleCooIterator(25, pather.getFlooder());
}
