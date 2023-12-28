package world.battle;

import game.battle.BattleState;
import game.battle.PlayerBattleSpec;
import game.battle.PlayerBattleSpec.SpecSide;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.dic.DicMisc;
import view.main.VIEW;
import view.tool.PlacableSimpleTile;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.army.WDivGeneration;
import world.battle.spec.*;
import world.battle.spec.WBattleResult.RTYPE;
import world.regions.data.RD;

class Tests {
	
	
	public Tests(){
		
		PlacableSimpleTile pl = new PlacableSimpleTile("battle create") {
			
			@Override
			public void place(int tx, int ty) {
				WBattleSpec s = new WBattleSpec() {
					
					@Override
					public void retreat() {
						WBattleResult res = new WBattleResult() {

							@Override
							public void accept(int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}
						
						};
						
						res.player = player;
						res.enemy = enemy;
						res.result = RTYPE.RETREAT;
						init(res, player, -1);
						VIEW.world().UI.battle.result(res, false);
						
					}
					
					@Override
					public void engage() {
						PlayerBattleSpec s = new PlayerBattleSpec() {
							
							@Override
							public void finish() {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void conclude(boolean timer, boolean retreat) {
								// TODO Auto-generated method stub
								
							}
						};
						s.player = new SpecSide();
						s.player.divs = new ArrayList<>(1+ RND.rInt(25));
						s.player.artillery = player.artilleryPieces;
						s.player.moraleBase = 1.0;
						s.player.wCoo.set(player.coo);
						for (int i = 0; i < s.player.divs.max(); i++)
							s.player.divs.add(new WDivGeneration());
						s.enemy = new SpecSide();
						s.enemy.divs = new ArrayList<>(1+RND.rInt(25));
						s.enemy.artillery = enemy.artilleryPieces;
						s.enemy.moraleBase = 1.0;
						s.enemy.wCoo.set(enemy.coo);
						while(s.enemy.divs.hasRoom())
							s.enemy.divs.add(new WDivGeneration());
							
						new BattleState(s);
					}
					
					@Override
					public void auto() {
						WBattleResult res = new WBattleResult() {

	
							@Override
							public void accept(int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}
						
						};
						if (!victory)
							init(res, player, -1);
						else
							init(res, enemy, 1);
						res.player = player;
						res.enemy = enemy;
						res.result = res.result;
						VIEW.world().UI.battle.result(res, false);
						
					}
				};
				
				double power = RND.rFloat();
				s.player = side(power);
				s.player.coo.set(tx, ty);
				s.enemy = side(1.0-power);
				s.enemy.coo.set(s.player.coo);
				s.enemy.coo.increment(DIR.ALL.rnd());
				s.victory = power >= 0.5;
				
				VIEW.world().UI.battle.prompt(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.IN_BOUNDS(tx, ty) ? null : E;
			}
		};
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				
				
				
				
			}
		};
		IDebugPanelWorld.add(pl);
		
		a = new ACTION() {
			
			@Override
			public void exe() {
				
				WBattleSiege s = new WBattleSiege() {
					
					@Override
					public void lift() {
						
						
					}
					
					@Override
					public void auto() {
						Result s = new Result() {


							@Override
							public void occupy(double plunderAmount, int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void abandon(double plunderAmount, int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void puppet(double plunderAmount, int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void accept(int[] enslave, int[] resources) {
								// TODO Auto-generated method stub
								
							}
						};
						
						s.besiged = besiged;
						s.player = player;
						s.enemy = enemy;
						s.result = player.powerBalance > 0.5 ? RTYPE.VICTORY : RTYPE.DEFEAT;
						if (!victory)
							init(s, player, -1);
						else
							init(s, enemy, 1);
						VIEW.world().UI.battle.result(s, false);
						
					}
				};
				
				s.besiged = WORLD.REGIONS().active().rnd();
				double power = RND.rFloat();
				s.victory = power >= 0.5;
				s.player = side(power);
				s.player.coo.set(RND.rInt(1 + WORLD.TBOUNDS().width()-2), RND.rInt(1 + WORLD.TBOUNDS().height())-2);
				s.enemy = new WBattleSide();
				s.enemy.coo.set(s.player.coo);
				s.enemy.coo.increment(DIR.ALL.rnd());
				s.fortifications = RND.rFloat(50);
				
				WBattleUnit u = new WBattleUnit() {
		
					@Override
					public void hover(GUI_BOX box) {
						VIEW.world().UI.regions.hoverGarrison(s.besiged, box);
					}
				};
				
				
				u.men = RD.MILITARY().garrison.get(s.besiged);
				u.losses = RND.rInt(1 + u.men);
				u.lossesRetreat = RND.rInt(1 + u.losses);
				u.icon = FACTIONS.NPCs().getC(RND.rInt()).banner().MEDIUM;
				s.enemy.units.add(u);
				
				
				
				if (RND.oneIn(5)) {
					WBattleSiege.Result ss = new WBattleSiege.Result() {
						
						@Override
						public void occupy(double plunderAmount, int[] enslave, int[] resources) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void abandon(double plunderAmount, int[] enslave, int[] resources) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void puppet(double plunderAmount, int[] enslave, int[] resources) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void accept(int[] enslave, int[] resources) {
							// TODO Auto-generated method stub
							
						}
					};
					ss.besiged = s.besiged;
					ss.player = s.player;
					ss.enemy = s.enemy;
					init(ss, s.enemy, 1);
					VIEW.world().UI.battle.result(ss, true);
				}else {
					VIEW.world().UI.battle.prompt(s);
				}
				
				
				
				
				
			}
		};
		IDebugPanelWorld.add("Battle test siege", a);
		

	}
	
	
	
	private WBattleSide side(double power) {
		WBattleSide a = new WBattleSide();
		a.artilleryPieces =  4 + (int) (RND.rFloatP(2)*50);
		a.powerBalance = power;
		
		for (int i = 1+RND.rInt(10); i > 0; i--) {
			WBattleUnit u = new WBattleUnit() {
				
				@Override
				public void hover(GUI_BOX box) {
					box.text(DicMisc.¤¤Babies);
					box.NL();
					box.add(box.text().add(men));
				}
			};
			
			
			u.name.clear().add(RACES.all().rnd().info.armyNames.rnd());
			u.men = 1 + RND.rInt(20000);
			u.losses = 1+ RND.rInt(u.men);
			u.lossesRetreat = RND.rInt(u.losses);
			int ri = RND.rInt();
			u.icon = FACTIONS.NPCs().getC(ri).banner().MEDIUM;
			a.units.add(u);
		}

		return a;
		
		
		
	}
	
	private void init(WBattleResult a, WBattleSide s, int i) {
		int captured = RND.rInt(1 + s.men);
		
		for (Race race : RACES.all()) {
			if (captured == 0)
				break;
			a.capturedRaces[race.index()] = i*RND.rInt(captured);
			captured -= a.capturedRaces[race.index()];
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (RND.oneIn(5)) {
				a.lostResources[res.index()] = i*RND.rInt(5000);
			}
		}
	}
	

	
}
