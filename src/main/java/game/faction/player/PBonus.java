package game.faction.player;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FBonus;
import init.boostable.*;
import init.boostable.BOOST_LOOKUP.BOOSTER_LOOKUP_IMP;
import snake2d.util.file.*;
import snake2d.util.sets.*;
import util.dic.DicMisc;

public final class PBonus extends BOOSTER_LOOKUP_IMP implements FBonus{

	final Player player;

	private ArrayList<SIMPLE> all = new ArrayList<>(8);
	private double[] cache_add = new double[BOOSTABLES.all().size()];
	private double[] cache_mul = new double[BOOSTABLES.all().size()];
	private int[] cahcheI = new int[BOOSTABLES.all().size()];
	private final PBonusExp exp = new PBonusExp();
	
	PBonus(Player player, KeyMap<Double> boosts) {
		super(DicMisc.¤¤Faction);
		this.player = player;
		Arrays.fill(cahcheI, -1);
		Arrays.fill(cache_mul, 1.0);
		all = new ArrayList<BOOST_LOOKUP.SIMPLE>(
				exp,
				new PBonusSetting(boosts),
				GAME.NOBLE().BOOSTER, 
				player.level().BOOSTER,
				player.titles.BOOSTER,
				player.tech().BOOSTER
				);
		
		for (BOOST_LOOKUP c : all) {
			init(c);
		}
	}
	
	@Override
	public LIST<SIMPLE> subs() {
		return all;
	}
	
	@Override
	public double add(BOOSTABLE tech) {
		setCache(tech);
		return cache_add[tech.index];
	}
	
	@Override
	public double mul(BOOSTABLE tech) {
		setCache(tech);
		return cache_mul[tech.index];
	}

	private void setCache(BOOSTABLE tech) {
		if ((cahcheI[tech.index] & ~0b0011111) != (GAME.updateI() & ~0b0011111)) {
			
			cahcheI[tech.index] = GAME.updateI();
			
			cache_add[tech.index] = 0;
			cache_mul[tech.index] = 1;
			
			for (SIMPLE s : all) {
				
				cache_add[tech.index] += s.add(tech);
				cache_mul[tech.index] *= s.mul(tech);
			}
		}
	}
	
	void update(double ds) {
		exp.update(ds);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			exp.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			exp.load(file);
		}
		
		@Override
		public void clear() {
			exp.clear();
		}
	};

}
