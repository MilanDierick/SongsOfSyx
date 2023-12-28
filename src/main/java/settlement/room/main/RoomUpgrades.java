package settlement.room.main;

import java.util.Arrays;

import game.GAME;
import game.boosting.*;
import game.faction.Faction;
import game.faction.npc.NPCBonus;
import game.values.GVALUES;
import game.values.Lockable;
import init.race.POP_CL;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.room.main.util.RoomInitData;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import util.dic.DicMisc;
import util.info.GFORMAT;

public final class RoomUpgrades {

	private final int upgrades;
	private final int[][] masks;
	private final double[] boosts;
	public final ArrayListGrower<Lockable<Faction>> reqs = new ArrayListGrower<>();
	
	
	RoomUpgrades(RoomBlueprintImp blue, RoomInitData init) {
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
				double b = j.d("BOOST");
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
			//LOG.ln(blue.key + " " + max());
			
		}else {
			upgrades = 1;
			masks = new int[][] {
				{1}
			};
			boosts = new double[] {
				1
			};
		}

		for (int i = 1; i <= max(); i++) {
			
			final int upAm = i;
			SPRITE up = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					blue.icon.render(r, X1, X2, Y1, Y2);
					int size = Icon.S*(X2-X1)/Icon.L;
					
					UI.icons().s.chevron(DIR.N);
					
					COLOR.BLACK.bind();
					OPACITY.O66.bind();
					int sh = size/8;
					for (int j = 0; j < upAm; j++)
						UI.icons().s.chevron(DIR.N).render(r, X1+sh, X1+size+sh, Y1+sh+j*size/2, Y1+sh+j*size/2+size);
					OPACITY.unbind();
					COLOR.unbind();
					//GCOLOR.T().bronzeGold((upAm-1)/(upgrades-1)).bind();
					
					
					for (int j = 0; j < upAm; j++)
						UI.icons().s.chevron(DIR.N).render(r, X1, X1+size, Y1+j*size/2, Y1+j*size/2+size);
					COLOR.unbind();
				}
			};
			SPRITE icon = blue.icon.twin(up);
			
			reqs.add(GVALUES.FACTION.LOCK.push("ROOM_" + init.key() + "_UPGRADE_" + i, blue.info.name + " (" + DicMisc.¤¤Upgrade + " " + GFORMAT.toNumeral(i) + ")", "", icon));
		}
	}
	
	public void pushBonus(RoomBlueprintIns<?> blue, Boostable bo) {
		
		if (max() <= 0)
			return;
		
		double from = boost(0);
		double to = boost(max());
		BSourceInfo in = new BSourceInfo(DicMisc.¤¤Upgrade, UI.icons().s.chevron(DIR.N));
		Booster bos = new BoosterImp(in, from, to, false) {
			
			@Override
			public double get(Boostable bo, BOOSTABLE_O o) {
				double d = o.boostableValue(bo, this);
				d = CLAMP.d(d, 0, RoomUpgrades.this.max());
				int di = (int) d;
				d -= di;
				double res = RoomUpgrades.this.boost(di)*(1.0-d);
				if (di < RoomUpgrades.this.max())
					res += RoomUpgrades.this.boost(di+1)*d;
				return res;
			}
			
			@Override
			public double vGet(Induvidual indu) {
				RoomInstance ins = STATS.WORK().EMPLOYED.get(indu);
				if (ins != null && ins.blueprint() == blue) {
					return ins.upgrade();
				}
				return 0;
			}
			
			private int ci = -120;
			private double c = 0;
			
			@Override
			public double vGet(POP_CL reg) {
				
				if (Math.abs(GAME.updateI()-ci) >= 120){
					ci = GAME.updateI();
					c = 0;
					int am = 0;
					for (int i = 0; i < blue.instancesSize(); i++) {
						RoomInstance ins = blue.getInstance(i);
						int e = ins.employees().employed();
						c += e*ins.upgrade();
						am += e;
					}
					
					if (am != 0) {
						c /= am;
					}
					
				}
				
				return c;
			}
			
			@Override
			public double vGet(NPCBonus bonus) {
				return RoomUpgrades.this.max()*bonus.get(blue.index());
			}
			
			@Override
			public double vGet(Faction f) {
				return 0;
			}
			
			
			@Override
			public boolean has(Class<? extends BOOSTABLE_O> b) {
				return b == Induvidual.class || b == POP_CL.class || b == NPCBonus.class;
			}
			
		};
		bos.add(bo);
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
	
	public Lockable<Faction> requires(int upgrade){
		return reqs.get(upgrade-1);
	}
	
}
