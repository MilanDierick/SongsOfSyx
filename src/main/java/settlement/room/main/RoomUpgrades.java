package settlement.room.main;

import java.util.Arrays;

import settlement.room.main.util.RoomInitData;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;

public final class RoomUpgrades {

	private final int upgrades;
	private final int[][] masks;
	private final double[] boosts;
	
	RoomUpgrades(RoomInitData init) {
		if (init.data().has("UPGRADES")) {
			Json[] jj = init.data().jsons("UPGRADES", 1);
			masks = new int[jj.length][];
			boosts = new double[jj.length];
			upgrades = jj.length;
			int i = 0;
			int ll = 0;
			for (Json j : jj) {
				int[] mask = j.is("RESOURCE_MASK");
				
				ll = Math.max(ll, mask.length);
				double b = 1.0 + j.d("BOOST");
				masks[i] = mask;
				
				
				boosts[i] = b;
				i++;
			}
			
			
			for (i = 0; i < boosts.length; i++) {
				if (masks[i].length < ll) {
					int[] nn = new int[ll];
					Arrays.fill(nn, 1);
					for (int k = 0; k < masks[i].length; k++)
						nn[k] = masks[i][k];
					masks[i] = nn;
				}
			}
			
			
		}else {
			upgrades = 1;
			masks = new int[][] {
				{1}
			};
			boosts = new double[] {
				1
			};
		}
		
		
	}
	
	public int max() {
		return upgrades-1;
	}
	
	public int resMask(int upgrade, int ri) {
		upgrade = CLAMP.i(upgrade, 0, max());
		ri = CLAMP.i(ri, 0, masks[upgrade].length-1);
		return masks[upgrade][ri];
	}
	
	public double boost(int upgrade) {
		return boosts[CLAMP.i(upgrade, 0, boosts.length-1)];
	}
}
