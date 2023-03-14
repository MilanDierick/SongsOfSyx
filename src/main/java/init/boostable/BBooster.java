package init.boostable;

import game.GameDisposable;
import init.race.Race;
import settlement.army.Div;
import settlement.entity.humanoid.HCLASS;
import settlement.stats.Induvidual;
import snake2d.util.sets.LinkedList;

public abstract class BBooster {

	static LinkedList<BBooster> PLAYER = new LinkedList<>();
	static LinkedList<BBooster> ENEMY = new LinkedList<>();
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				PLAYER.clear();
				ENEMY.clear();
			}
		};
	}
	
	public final BBoost boost;
	private final CharSequence name;
	final boolean canCache;
	
	protected BBooster(CharSequence name, BBoost boost, boolean player, boolean enemy, boolean canCache){
		this.boost = boost;
		this.name = name;
		this.canCache = canCache;
		
		
		
		if (player)
			PLAYER.add(this);
		if (enemy)
			ENEMY.add(this);
	}
	
	public abstract double value(Induvidual i);
	
	public abstract double value(HCLASS c, Race r);
	
	public abstract double value(Div v);

	public CharSequence name() {
		return name;
	}

	public static abstract class BBoosterSimple extends BBooster{
		
		protected BBoosterSimple(CharSequence name, BBoost boost, boolean player, boolean enemy, boolean canCache) {
			super(name, boost, player, enemy, canCache);
		}

		@Override
		public final double value(Induvidual i) {
			return pvalue();
		}
		
		@Override
		public final double value(HCLASS c, Race r) {
			return pvalue();
		}
		
		@Override
		public final double value(Div v) {
			return pvalue();
		}
		
		public abstract double pvalue();
	}
	
	public static class BBoosterEvenSimpler extends BBooster{
		
		public BBoosterEvenSimpler(CharSequence name, BBoost boost, boolean player, boolean enemy, boolean canCache) {
			super(name, boost, player, enemy, canCache);
		}

		@Override
		public final double value(Induvidual i) {
			return boost.end;
		}
		
		@Override
		public final double value(HCLASS c, Race r) {
			return boost.end;
		}
		
		@Override
		public final double value(Div v) {
			return boost.end;
		}
	}
	
	public static abstract class BBoosterImp extends BBooster{
		
		protected BBoosterImp(CharSequence name, BBoost boost, boolean player, boolean enemy, boolean canCache) {
			super(name, boost, player, enemy, canCache);
		}

		@Override
		public final double value(Induvidual i) {
			return boost.start+boost.delta*pvalue(i);
		}
		
		@Override
		public final double value(HCLASS c, Race r) {
			return boost.start+boost.delta*pvalue(c,r);
		}
		
		@Override
		public final double value(Div v) {
			return boost.start+boost.delta*pvalue(v);
		}
		
		public abstract double pvalue(Induvidual v);

		public abstract double pvalue(HCLASS c, Race r);

		public abstract double pvalue(Div v);
	}
	
}
