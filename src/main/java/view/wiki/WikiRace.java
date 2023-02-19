package view.wiki;

import java.util.Arrays;
import java.util.Comparator;

import game.time.TIME;
import init.D;
import init.biomes.*;
import init.boostable.*;
import init.race.RACES;
import init.race.Race;
import init.resources.Edible;
import init.sprite.ICON;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HCLASS;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomEmploymentSimple;
import settlement.stats.STAT;
import settlement.stats.STATS;
import settlement.stats.StatsBoosts.StatBooster;
import settlement.stats.StatsReligion.Religion;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.*;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.DicMisc;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;

final class WikiRace extends Article{
	
	private static CharSequence ¤¤liking = "¤Liking";
	private static CharSequence ¤¤likingD = "¤Different species likes/dislikes different aspects of layout and management. Green bars indicate that the species likes the aspect, a red one represents dislike.";
	
	private static CharSequence ¤¤Likes = "¤This is liked by the race. Higher value = more fulfillment.";
	private static CharSequence ¤¤Dislikes = "¤This is disliked the race. Higher value = less fulfillment.";
	private static CharSequence ¤¤Mixed = "¤Different classes have mixed feelings about this.";
	private static CharSequence ¤¤DontCare = "¤This race doesn't care about this value.";
	
	private static CharSequence ¤¤ReligionD = "¤Inclination towards different religions.";
	private static CharSequence ¤¤PopulationD = "¤Determines a species population and location on the world map.";
	private static CharSequence ¤¤PopulationNo = "¤This species is not available for immigration.";
	private final Race race;
	
	static {
		D.ts(WikiRace.class);
	}
	
	WikiRace(Race race){
		super(race.info.names, RACES.name());
		this.race = race;
	}
	
	@Override
	GuiSection makeSection(LIST<Article> all, int width) {
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		width -= 24;
		
		for (CharSequence s : UI.FONT().M.getRows(race.info.desc_long, width)) {
			GTextR r = new GTextR(UI.FONT().M, s);
			r.setColor(GCOLOR.T().NORMAL);
			rows.add(r);
		}
	
		
		
		
		{
			
			rows.add(seperator(DicMisc.¤¤Population, ¤¤PopulationD, width));
			
			
			if (race.population().immigrantsPerDay == 0) {
				rows.add(new GText(UI.FONT().M, ¤¤PopulationNo).r(DIR.C));
			}else {
				LinkedList<RENDEROBJ> ee = new LinkedList<>();
				
				double m = 0;
				for (int rii = 0; rii < RACES.all().size(); rii++) {
					Race r = RACES.all().get(rii);
					for (TERRAIN c : TERRAINS.ALL()) {
						m = Math.max(m, r.population().rarity*r.population().terrain(c));
					}
				}
				final double maxAm = m;
				
				for (TERRAIN c : TERRAINS.ALL()) {
					
					
					HOVERABLE h = new RGauge(c.icon()) {
						
						@Override
						double getValue() {
							return race.population().rarity*race.population().terrain(c)/maxAm;
						}
						
						@Override
						SPRITE get(double value) {
							return SPRITES.icons().s.human;
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							
							GBox b = (GBox) text;
							
							b.title(c.name);
							b.text(c.desc);
							
							b.NL(8);
							
							b.textLL(race.info.names);
							b.add(GFORMAT.percBig(b.text(), race.population().rarity*race.population().terrain(c)));
						}
					};
					ee.add(h);
					
				}
				RGauge.add(rows, ee);
				ee = new LinkedList<>();
				
				for (CLIMATE c : CLIMATES.ALL()) {
					
					HOVERABLE h = new RGauge(c.icon()) {
						
						@Override
						double getValue() {
							return race.population().climate(c);
						}
						
						@Override
						SPRITE get(double value) {
							return SPRITES.icons().s.human;
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							
							GBox b = (GBox) text;
							
							b.title(c.name);
							b.text(c.desc);
							
							b.NL(8);
							
							b.textLL(race.info.names);
							b.add(GFORMAT.percBig(b.text(), race.population().climate(c)));
						}
					};
					ee.add(h);
					
				}
				RGauge.add(rows, ee);
			}
			
			
		}
	
		
		{
			rows.add(seperator(STATS.ENV().OTHERS.info().name, STATS.ENV().OTHERS.info().desc, width));
			
			LinkedList<RENDEROBJ> ee = new LinkedList<>();
			for (int ri = 0; ri < RACES.all().size(); ri ++) {
				Race rr = RACES.all().get(ri);
				if (rr == race)
					continue;
				
				ee.add(new RGauge(rr.appearance().icon) {
					
					@Override
					double getValue() {
						return Math.abs(race.pref().other(rr));
					}
					
					@Override
					SPRITE get(double value) {
						return race.pref().other(rr) < 0 ? SPRITES.icons().s.arrowDown : SPRITES.icons().s.arrowUp;
					}
					
					@Override
					COLOR color(double value) {
						return race.pref().other(rr) < 0 ? GCOLOR.UI().BAD.normal : GCOLOR.UI().GOOD.normal;
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(rr.info.names);
						b.text(rr.info.desc);
						b.NL(8);
						b.add(GFORMAT.f1(b.text(), race.pref().other(rr)));
					};
					
					
				});
				
			}
			RGauge.add(rows, ee);
			
			
		}
		
		{
			rows.add(seperator(STATS.RELIGION().info.name, ¤¤ReligionD, width));
			LinkedList<RENDEROBJ> ee = new LinkedList<>();
			
			
			int i = 1;
			GuiSection s = new GuiSection();
			s.body().incrW(40);
			rows.add(s);
			for (int ri = 0; ri < STATS.RELIGION().ALL.size(); ri ++) {
				Religion rr = STATS.RELIGION().ALL.get(ri);

				if (i == 0) {
					rows.add(s);
					s = new GuiSection();
					s.body().incrW(40);
				}
				ee.add(new RGauge(rr.temple.iconBig()) {
					
					@Override
					double getValue() {
						return race.stats().religion(rr);
					}
					
					@Override
					SPRITE get(double value) {
						return SPRITES.icons().s.plus;
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(rr.info.names);
						b.text(rr.info.desc);
						b.NL(8);
						
						b.add(GFORMAT.f(b.text(), race.stats().religion(rr)));
						
						b.NL(8);
						for (StatBooster bb : rr.bonuses) {
							bb.boost.hover(b);
							b.NL();
						}
					};
				});
			}
			
			RGauge.add(rows, ee);
		}
		
		{
			rows.add(seperator(STATS.FOOD().FOOD_PREFFERENCE.info().name, STATS.FOOD().FOOD_PREFFERENCE.info().desc, width));
			GuiSection s = new GuiSection();
			for (Edible r : race.pref().food) {
				s.addRightC(8, new HOVERABLE.Sprite(r.resource.icon()).hoverTitleSet(r.resource.name).hoverInfoSet(r.resource.desc));
			}
			rows.add(s);
		}
		
		{
			rows.add(seperator(STATS.ENV().BUILDING_PREF.info().name, STATS.ENV().BUILDING_PREF.info().desc, width));
			
			LinkedList<RENDEROBJ> ee = new LinkedList<>();
			
			for (BUILDING_PREF b : BUILDING_PREFS.ALL()) {
				ee.add(new RGauge(b.icon()) {
					
					@Override
					double getValue() {
						
						return race.pref().structure(b);
					}
					
					@Override
					SPRITE get(double value) {
						return SPRITES.icons().s.arrowUp;
					}
					
					@Override
					COLOR color(double value) {
						return GCOLOR.UI().GOOD.normal;
					}
				}.hoverTitleSet(b.name));
			}
			
			RGauge.add(rows, ee);
		}
		
		{
			rows.add(seperator(¤¤liking, ¤¤likingD, width));
			GuiSection s = new GuiSection() {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					super.render(r, ds);
					COLOR.WHITE25.render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
				}
			};
			s.body().setWidth(width);
			int i = 0;
			
			STAT[] ll;
			{
				LIST<STAT> lll = STATS.createThoseThatMatters(null);
				ll = new STAT[lll.size()];
				for (int k = 0; k < ll.length; k++)
					ll[k] = lll.get(k);
				
				Arrays.sort(ll, new Comparator<STAT>() {

					@Override
					public int compare(STAT o1, STAT o2) {
						return Dictionary.compare(o1.info().name, o2.info().name);
					}
				});
			}
			
			
			for (STAT ss : ll) {
				if(ss.key() == null || ss.key().length() == 0)
					continue;
				
				SPRITE m = new SPRITE.Imp(80, 16) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						double d = ss.standing().normalized(HCLASS.CITIZEN, race);
						SPRITE s = ss.standing().definition(race).inverted ? SPRITES.icons().s.arrowDown : SPRITES.icons().s.arrowUp;
						COLOR c = (ss.standing().definition(race).inverted ? GCOLOR.UI().BAD.normal : GCOLOR.UI().GOOD.normal);
						c.bind();
						int am = (int) Math.ceil(6*d);
						for (int i = 0; i < am; i++)
							s.render(r, X1+10*i, Y1);
					}
				};
				
				GHeader h = new GHeader.HeaderHorizontal(ss.info().name, m, 180) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(ss.info().name);
						b.text(ss.info().desc);
						b.NL(8);
						{
							boolean liked = false;
							boolean dislikes = false; 
							for (HCLASS cl : HCLASS.ALL) {
								if (cl.player) {
									if (ss.standing().definition(race).get(cl).max > 0) {
										if (ss.standing().definition(race).inverted)
											dislikes = true;
										else
											liked = true;
									}
								}
							}
							
							if (liked && dislikes) {
								b.text(¤¤Mixed);
							}else if (liked)
								b.text(¤¤Likes);
							else if (dislikes)
								b.text(¤¤Dislikes);
							else
								b.text(¤¤DontCare);
							
						}
						b.NL(8);
						
						b.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
						b.NL();
						for (HCLASS cl : HCLASS.ALL) {
							if (cl.player) {
								b.textLL(cl.names);
								b.tab(4);
								b.add(GFORMAT.f0(b.text(), ss.standing().definition(race).inverted ? -ss.standing().definition(race).get(cl).max : ss.standing().definition(race).get(cl).max));
								b.NL();
							}
						}
					}
				}.subify();
				h.hoverInfoSet(ss.info().desc);
				
				
				s.add(h, (i)*300, 0);
				
				i++;
				if (i > 1) {
					i = 0;
					s.body().incrH(1);
					rows.add(s);
					s = new GuiSection() {
						@Override
						public void render(SPRITE_RENDERER r, float ds) {
							super.render(r, ds);
							COLOR.WHITE25.render(r, body().x1(), body().x2(), body().y2()-1, body().y2());
						}
					};
					s.body().setWidth(width);
				}
			}
			if (i != 0) {
				s.body().incrH(1);
				rows.add(s);
			}
		}
		
		{
			
			
			int max = 7;
			
			for (BoostableCollection coll : BOOSTABLES.colls()) {
				if (coll == BOOSTABLES.ROOMS() || coll == BOOSTABLES.START())
					continue;
				
				rows.add(seperator(coll.name, null, width));
				
				GuiSection s = null;
				int i = 0;
				for (BOOSTABLE b : coll.all()) {
					if (i == 0) {
						s = new GuiSection();
						rows.add(s);
					}
					
					s.addRightC(64, new GStat() {
						
						@Override
						public void update(GText text) {
							double bb = b.race(race);
							GFORMAT.f(text,  bb);
							if (bb > b.defValue)
								text.color(GCOLOR.T().IGREAT);
							else if(bb < b.defValue)
								text.errorify();
							else
								text.normalify();
						}
						
						@Override
						public void hoverInfoGet(GBox bb) {
							bb.title(b.name);
							bb.text(b.desc);
						};
						
					}.hh(b.icon()));
					
					
					i++;
					i %= max;
					if (i == 0) {
						s.body().incrW(48);
						s.pad(4, 2);
					}
				}
				
				if (coll == BOOSTABLES.PHYSICS())
				{
					GuiSection ss = new GuiSection();
					
					ss.addRightC(64, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, race.physics.adultAt/TIME.years().bitConversion(TIME.years()));
						}
					}.hh(DicMisc.¤¤AdultAge));
					
					rows.add(ss);
				}
				
			}
			
			
		}
		
		{
			rows.add(seperator(DicMisc.¤¤Employment, "", width));
			LinkedList<RENDEROBJ> ee = new LinkedList<>();
			
			for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
				ee.add(new RoomGauge(e.blueprint(), race));
			}
			RGauge.add(rows, ee);
			
		}
		
		GScrollRows sc = new GScrollRows(rows, HEIGHT-8, width+24);
		
		GuiSection section = new GuiSection();
		
		section.add(sc.view());
		
		
		return section;
	}
	
	RENDEROBJ seperator(CharSequence name, CharSequence desc, int width) {
		GuiSection s = new GuiSection();
		s.hoverInfoSet(desc);
		s.body().setWidth(width-8);
		s.body().setHeight(16);
		GHeader t = new GHeader(name);
		s.addDownC(0, t);
		s.pad(4);
		return s;
	}
	
	private static abstract class RGauge extends HoverableAbs{

		private int max = 5;
		private final SPRITE icon;
		
		
		public RGauge(SPRITE icon) {
			this.icon = icon;
			body().setDim(ICON.BIG.SIZE*3+4, icon.height());
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			double v = getValue();
			icon.render(r, body().x1(), body().y1());
			
			int am = (int) Math.ceil(max*v);
			if (v > 1) {
				am = max;
				am += CLAMP.i((int) (2*v/3.0), 0, 1);
			}
				
			int x1 = body().x1() + ICON.BIG.SIZE;
			
		
			color(v).bind();
			for (int i = 0; i < am; i++) {
				get(v).renderCY(r, x1, body().cY());
				x1 += 9;
			}
			COLOR.unbind();
		}
		
		abstract double getValue();
		abstract SPRITE get(double value);
		COLOR color(double value) {
			return GCOLOR.UI().GOOD.normal;
		}
		
		static void add(LISTE<RENDEROBJ> rows, LIST<RENDEROBJ> all) {
			
			int ri = 5;
			GuiSection s = null;
			
			for (RENDEROBJ h : all) {
				ri++;
				if (ri >= 5) {
					ri = 0;
					if (s != null)
						s.pad(2, 8);
					s = new GuiSection();
					rows.add(s);
				}
				s.addRightC(16, h);
				
			}
			
			
		}
	}
	
	private static class RoomGauge extends HoverableAbs{

		private final RoomBlueprintIns<?> blue;
		private final Race race;
		
		public RoomGauge(RoomBlueprintIns<?> blue, Race race) {
			this.blue = blue;
			body().setDim(ICON.BIG.SIZE*3+4, ICON.BIG.SIZE);
			this.race = race;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			blue.iconBig().render(r, body().x1(), body().y1());
			
			int x1 = body().x1() + ICON.BIG.SIZE;
			
			GMeter.render(r, GMeter.C_REDGREEN, race.pref().getWork(blue.employment()), x1, body().x2()-8, body().y1(), body().y1()+10);
			
			double b = RACES.bonus().priorityCapped(race, blue.employment())*2;
			int am = 0;
			COLOR c = GCOLOR.UI().NEUTRAL.hovered;
			SPRITE sp = SPRITES.icons().s.arrowUp;
			if (b < 1) {
				c = GCOLOR.UI().BAD.normal;
				am = (int) ((1.0-b)*7);
				sp = SPRITES.icons().s.arrowDown;
			}else if(b > 1){
				c = GCOLOR.UI().GREAT.normal;
				am = CLAMP.i(1+(int) ((b-1)*6), 0, 7);
			}

				
			int cy = body().cY() + ICON.BIG.SIZE/4;
			
			COLOR.WHITE15.render(r, x1, x1+9*7+2, cy-8, cy+8);
			
			c.bind();
			for (int i = 0; i < am; i++) {
				sp.renderCY(r, x1, cy);
				x1 += 9;
			}
			COLOR.unbind();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(blue.info.names);
			b.text(blue.info.desc);
			b.NL(8);
			
			b.textL(STANDINGS.CITIZEN().fullfillment.info().name);
			b.tab(5);
			b.add(GFORMAT.perc(b.text(), race.pref().getWork(blue.employment())));
			b.NL();
			b.textL(DicMisc.¤¤Skill);
			b.tab(5);
			double add = 1;
			double mul = 1;
			for (BOOSTABLE bo : BOOSTABLES.ROOMS().boosts(blue)) {
				add += race.bonus().add(bo);
				mul *= race.bonus().mul(bo);
			}
			if (BOOSTABLES.ROOMS().boosts(blue).size() > 0)
				add = add*mul/BOOSTABLES.ROOMS().boosts(blue).size();
			
			b.add(GFORMAT.perc(b.text(), add));
			
			
			
			super.hoverInfoGet(text);
		}
		

		
	}
	
}
