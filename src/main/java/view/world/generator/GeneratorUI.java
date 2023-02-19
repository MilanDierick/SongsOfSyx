package view.world.generator;

import java.util.Arrays;

import init.D;
import init.biomes.*;
import init.boostable.*;
import init.race.RACES;
import init.race.Race;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.ICON;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.dic.*;
import util.gui.misc.*;
import util.gui.slider.GGauge;
import util.info.GFORMAT;
import world.World;
import world.map.regions.REGIOND;
import world.map.regions.Region;
import world.map.regions.RegionTaxes.RegionIndustry;
import world.map.terrain.WorldTerrainInfo;

class GeneratorUI extends GuiSection{

	WorldTerrainInfo info;
	
	
	GeneratorUI(WorldTerrainInfo info){
		D.gInit(this);
		this.info = info;
		add(region());
		add(terrain(), body().x2()+16, body().y1());
		int x1 = getLastX1();
		int y1 = getLastY2() + 4;
		
		
		add(deposits(), body().x2()+64, body().y1());
		
		add(population(), body().x2()+64, body().y1());
		add(rooms(), body().x2()+64, body().y1());
		add(bonuses(), x1, y1);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (World.REGIONS().getter.get(info.tx, info.ty) == null)
			return;
		super.render(r, ds);
	}
	
	private GuiSection terrain() {
		GuiSection s = new GuiSection();
		int m = 128;
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(info.tx).add(':').add(info.ty);
			}
		}.hh(DicGeo.¤¤Location, m));
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, info.fertility());
			}
		}.hh(DicMisc.¤¤Fertility, m).hoverInfoSet(DicMisc.¤¤FertilityD));
		
		for (TERRAIN t : TERRAINS.ALL()) {
			s.addDown(0, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, info.get(t));
				}
			}.hh(t.name, m).hoverInfoSet(t.desc));
		}
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(climate().name);
			}
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(climate().desc);
			};
			
		}.hh(CLIMATES.INFO().name, m));
		
		return s;
	}
	
	private GuiSection region() {
		GuiSection s = new GuiSection();
		
		s.addDown(0, new GHeader(DicRes.¤¤Taxes).hoverInfoSet(DicRes.¤¤TaxesD));
		
		
		{
			RegionIndustry[] ins = new RegionIndustry[12];
			GuiSection t = new GuiSection() {
				@Override
				public void render(SPRITE_RENDERER re, float ds) {
					Arrays.fill(ins, null);
					Region r = World.REGIONS().getter.get(info.tx, info.ty);
					if (r == null)
						return;
					int i = 0;
					for (RegionIndustry rer : REGIOND.RES().all) {
						if (rer.prospect.getD(r) > 0.3) {
							ins[i++] = rer;
							if (i >= ins.length)
								break;
						}
					}
					super.render(re, ds);
				}
			};
			for (int i = 0; i < ins.length; i++) {
				final int k = i;
				SPRITE o = new SPRITE.Imp(ICON.MEDIUM.SIZE) {
					
					@Override
					public void render(SPRITE_RENDERER rr, int X1, int X2, int Y1, int Y2) {
						Region r = World.REGIONS().getter.get(info.tx, info.ty);
						if (r != null && ins[k] != null)
							ins[k].industry.blue.iconBig().render(rr, X1, X2, Y1, Y2);
						
					}
				};
				
				t.add(new GGauge(32, 12) {
					
					@Override
					public double getD() {
						Region r = World.REGIONS().getter.get(info.tx, info.ty);
						if (r != null && ins[k] != null)
							return ins[k].prospect.getD(r);
						return 0;
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						Region r = World.REGIONS().getter.get(info.tx, info.ty);
						if (r != null && ins[k] != null) {
							b.title(ins[k].industry.blue.info.name);
							b.text(ins[k].industry.blue.info.desc);
							b.NL(8);
							b.add(GFORMAT.perc(b.text(), ins[k].prospect.getD(r)));
						}
					};
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (r != null && ins[k] != null)
							super.render(r, X1, X2, Y1, Y2);
					};
				}.hh(o), (i%2)*64, (i/2)*34);
				
				
				
				
			}
			
			s.addDown(4, t);
		}
		
		return s;
	}
	
	private CLIMATE climate() {
		return World.CLIMATE().getter.get(info.tx, info.ty);
	}
	
	private GuiSection deposits() {
		GuiSection s = new GuiSection();
		
		int i = 0;
		for (Minable m : RESOURCES.minables()) {
			s.add(new GGauge(32, 16) {
				
				@Override
				public double getD() {
					double d = 0;
					for (TERRAIN t : TERRAINS.ALL()) {
						d += m.terrain(t)*info.get(t);
					}
					d = CLAMP.d(d, 0, 1);
					return d;
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(m.resource.name);
					b.text(m.resource.desc);
					b.NL(8);
					b.add(GFORMAT.perc(b.text(), getD()));
				};
				
			}.hh(m.resource.icon()), (i%2)*64, (i/2)*24);
			i++;
		}
		
		s.addRelBody(2, DIR.N, new GHeader(DicRes.¤¤Deposits).hoverInfoSet(D.g("DepositD", "An very rough estimate of how much deposits there will be in this location.")));
		
		return s;
	}
	
	private GuiSection bonuses() {
		BOOSTABLE[] bonuses = new BOOSTABLE[16];
		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				for (int i = 0; i < bonuses.length; i++)
					bonuses[i] = null;
				int i = 0;
				for (BOOSTABLE bo : BOOSTABLES.all()) {
					double d =  CLIMATES.BONUS().mul(bo, climate())*(1+ CLIMATES.BONUS().add(bo, climate()));
					if (d == 1)
						continue;
					if (bo instanceof BOOSTABLERoom) {
						BOOSTABLERoom br = (BOOSTABLERoom) bo;
						if (!br.room.isAvailable(climate()))
							continue;
					}
					
						bonuses[i++] = bo;
					if (i >= bonuses.length)
						break;
					
				}
				
				super.render(r, ds);
			}
			
		};
		

		
		for (int i = 0; i < bonuses.length; i++) {
			final int k = i;
			s.add(new HOVERABLE.HoverableAbs(80, 20) {
				
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						if (bonuses[k] != null) {
							double d = CLIMATES.BONUS().mul(bonuses[k], climate())*(1+ CLIMATES.BONUS().add(bonuses[k], climate()));
							GFORMAT.percInc(text, d-1);
						}
					}
				};
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					if (bonuses[k] == null)
						return;
					bonuses[k].icon().render(r, body().x1(), body().y1());
					s.render(r, body.x1()+24, body().y1());
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (bonuses[k] != null) {
						text.title(bonuses[k].name);
						text.text(bonuses[k].desc);
					}
				}
			}, (i%4)*80, (i/4)*18);
		}
		
		s.addRelBody(2, DIR.N, new GHeader(BOOSTABLES.INFO().name).hoverInfoSet(BOOSTABLES.INFO().desc));
		
		
		return s;
		
	}
	
	private GuiSection rooms() {
		RoomBlueprintImp[] bonuses = new RoomBlueprintImp[8];
		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				for (int i = 0; i < bonuses.length; i++)
					bonuses[i] = null;
				int i = 0;
				for (RoomBlueprint p : SETT.ROOMS().all()) {
					if (p instanceof RoomBlueprintImp) {
						RoomBlueprintImp pp = (RoomBlueprintImp) p;
						if (!pp.isAvailable(climate())) {
							bonuses[i++] = pp;
						}
						if (i > bonuses.length)
							break;
					}
				}
				super.render(r, ds);
			}
			
		};
		
		for (int i = 0; i < bonuses.length; i++) {
			final int k = i;
			s.add(new HOVERABLE.HoverableAbs(48, 48) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					if (bonuses[k] != null)
						bonuses[k].iconBig().render(r, body);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (bonuses[k] != null) {
						text.title(bonuses[k].info.name);
						text.text(bonuses[k].info.desc);
					}
				}
			}, (i%2)*48, (i/2)*48);
		}
		
		
		return s;
		
	}
	
	private GuiSection population() {
		GuiSection s = new GuiSection();
		
		int i = 0;
		for (Race race : RACES.playable()) {
			
			s.add(new GGauge(32, 12) {
				
				@Override
				public double getD() {
					double c = 1;
//					for (TERRAIN t : TERRAINS.ALL()) {
//						c += info.get(t)*race.population().terrain(t);
//					}
					c *= race.population().climate(climate());
					c = CLAMP.d(c, 0, 1);
					return c;
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(race.info.name);
					
					b.textLL(CLIMATES.INFO().name);
					b.tab(5);
					b.add(GFORMAT.f(b.text(), race.population().climate(climate()), 4));
					b.NL();
					
//					double c = 0;
//					for (TERRAIN t : TERRAINS.ALL()) {
//						c += info.get(t)*race.population().terrain(t);
//					}
//					b.textLL(TERRAINS.INFO().name);
//					b.tab(5);
//					b.add(GFORMAT.f(b.text(), c, 4));
//					b.NL();
					
					b.textLL(DicMisc.¤¤Rarity);
					b.tab(5);
					b.add(GFORMAT.f(b.text(), race.population().rarity, 4));
					b.NL();
					
					
					b.text(race.info.desc);
					
					b.NL(8);
					GText t = b.text();
					t.add(DicGeo.¤¤Regional).insert(0, DicMisc.¤¤Population);
					t.lablifySub();
					b.add(t);
					b.add(GFORMAT.i(b.text(), REGIOND.RACE(race).population.get(World.REGIONS().getter.get(info.tx, info.ty))));
				};
				
			}.hh(race.appearance().icon), (i%2)*68, (i/2)*24);
			
			
			i++;
			
		}
		
		s.add(new GHeader(DicMisc.¤¤Population).hoverInfoSet(D.g("PopulationD", "The percentage indicates how each species will thrive in the selected climate. Next to it is the regional population. Different species have different skills that can lead to taxes. It's easier to rule a region that has the same species makeup as your city.")), body().x1(), body().y1()-24);
		
		return s;
	}
	
	
	
	
}
