package game.faction.player;

import game.GAME;
import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.race.Race;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;

public abstract class BoostCompound<T> implements ACTION {

	private final BoostSpecs bos;
	private final LIST<T> all;
	private final ArrayListGrower<Boo> boos = new ArrayListGrower<>();
	
	public BoostCompound(BoostSpecs bos, LIST<T> all) {
		this.bos = bos;
		this.all = all;
		
		BOOSTING.connecter(this);
		
	}

	protected abstract BoostSpecs bos(T t);
	
	protected abstract double getValue(T t);
	protected double get(Boostable bo, FactionNPC f, boolean isMul) {
		return ((FactionNPC)f).bonus.getD(bo);
	}
	
	public void clearChache() {
		for (Boo b : boos)
			b.cacheI = GAME.updateI()-100;
	}
	
	@Override
	public void exe() {
		
		KeyMap<LinkedList<Value>> map = new KeyMap<LinkedList<Value>>();
		
		for (T t : all) {
			
			BoostSpecs bos = bos(t);
			
			for (BoostSpec s : bos.all()) {
				
				String k = s.boostable.key + s.booster.isMul;
				if (!map.containsKey(k)) {
					map.put(k, new LinkedList<>());
				}
				map.get(k).add(new Value(t, s));
			}
		}
		
		for (LinkedList<Value> l : map.all()) {
			Boo b = new Boo(l);
			bos.push(b, b.bo);
		}
		
		
		
	}
	
	private class Boo extends BoosterSimple {

		final Boostable bo;
		final LIST<Value> all;
		private int cacheI;
		private double cache;
		
		private final double from;
		private final double to;
		
		public Boo(LIST<Value> all) {
			super(bos.info, all.get(0).bo.booster.isMul);
			this.bo = all.get(0).bo.boostable;
			this.all = all;
			
			double from = 0;
			double to = 0;
			if (isMul) {
				for (Value v : all) {
					double d = (v.bo.booster.getValue(1.0)-1);
					if (d < 0)
						from += d;
					else
						to += d;
				}
			}else {
				for (Value v : all) {
					double d = (v.bo.booster.getValue(1.0));
					if (d < 0)
						from += d;
					else
						to += d;
				}
			}
			
			this.from = from;
			this.to = to;
			
			
		}
		
		
		@Override
		public double get(Boostable b, BOOSTABLE_O o) {
			return o.boostableValue(b, this);
		}
		
		@Override
		public double vGet(Faction f) {
			if (f != FACTIONS.player()) {
				return from + (to-from)*BoostCompound.this.get(bo, (FactionNPC) f, isMul);
			}
			int ci = GAME.updateI();
			if (((ci - cacheI)&0x010) != 0x010) {
				cacheI = GAME.updateI();
				cache = 0;
				if (isMul) {
					for (Value v : all) {
						cache += (v.bo.booster.getValue(BoostCompound.this.getValue(v.t))-1);
					}
					cache += 1;
					cache = Math.max(0, cache);
				}else {
					for (Value v : all) {
						cache += v.bo.booster.getValue(BoostCompound.this.getValue(v.t));
					}
				}
			}
			return cache;
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}

		@Override
		public boolean has(Class<? extends BOOSTABLE_O> b) {
			return b != Race.class;
		}

		
		
	}
	
	private class Value {
		
		public final T t;
		public final BoostSpec bo;
		
		Value(T t, BoostSpec bo){
			this.t = t;
			this.bo = bo;
		}
	}
	
}
