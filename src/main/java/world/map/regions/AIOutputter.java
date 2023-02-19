package world.map.regions;

import game.faction.Faction;
import snake2d.util.sets.Tree;
import world.map.regions.RegionTaxes.RegionResource;

public final class AIOutputter {

	private final Node[] nodes = new Node[REGIOND.RES().res.size()];
	private final Tree<Node> sorter = new Tree<Node>(nodes.length) {

		@Override
		protected boolean isGreaterThan(Node current,
				Node cmp) {
			return current.value > cmp.value;
		}
	
	};
	
	private int[] result = new int[REGIOND.RES().res.size()];
	
	AIOutputter() {
		for (int i = 0; i< nodes.length; i++)
			nodes[i] = new Node(REGIOND.RES().res.get(i), i);
	}
	
	public int[] getAmounts(Faction f, Region reg) {
		
		sorter.clear();
		for (int i = 0; i < nodes.length; i++) {
			result[i] = 0;
			Node n = nodes[i];
			n.init(f, reg);
			sorter.add(n);
		}
		
		double v = 1.0;
		while (v > 0 && sorter.hasMore()) {
			Node n = sorter.pollGreatest();
			result[n.index] = (int) Math.ceil(v*n.production);
			v -= 0.2;
		}
		
		return result;
		
	}

	
	private static class Node {
		
		final RegionResource res;
		final int index;
		
		public double price;
		public double production;
		public double value;
		
		Node(RegionResource res, int index){
			this.res = res;
			this.index = index;
		}
		
		void init(Faction f, Region reg){
			price = f.buyer().buyPrice(res.resource, 1);
			production = res.maxOutput(reg);
			value = price*production;
		}
		
	}

	
}
