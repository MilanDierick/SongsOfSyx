package menu;

import static menu.GUI.*;

import game.GameConRandom;
import game.GameConSpec;
import init.D;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import menu.GUI.Button;
import menu.GUI.COLORS;
import menu.screens.Screener;
import script.ScriptLoad;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.Scrollable;
import snake2d.util.gui.clickable.Scrollable.ScrollRow;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Font;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.data.INT.IntImp;
import util.gui.misc.GText;
import util.gui.table.GScrollable;
import util.info.GFORMAT;
import util.info.INFO;

class ScRandom extends GuiSection implements SCREEN{

	private final GameConRandom spec;
	
	final CharSequence ¤¤name = "¤random game";
	
	private final MapType[] types;
	private final ScripSelector script = new ScripSelector();
	
	ScRandom(Menu menu){
		
		D.t(this);
		
		Screener screen = new Screener(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		
		{
			PATH p = PATHS.SPRITE().getFolder("world").getFolder("generatorMaps");
			String[] files = p.getFiles(1);
			types = new MapType[files.length];
			for (int i = 0; i < files.length; i++)
				types[i] = new MapType(p.get(files[i]));
		}
		
		spec = new GameConRandom(types[0]);
		
		CLICKABLE b = new Screener.ScreenButton(D.g("go!")) {
			@Override
			protected void clickA() {
				LinkedList<ScriptLoad> scc = new LinkedList<>();
				for (int i = 0; i < script.sc.size(); i++)
					if (script.selected[i])
						scc.add(script.sc.get(i));
				spec.scripts = new ArrayList<ScriptLoad>(scc);
				menu.start(spec);
			}
		};
		
		screen.addButt(b);
		
		GuiSection options = map();
		
		options.addRelBody(20, DIR.S, race());
		
		options.addRelBody(20, DIR.W, advantage());
		
		options.addRelBody(20, DIR.E, scripts());
		

		//options.add(race, options.getLastX2()+100, options.body().y1());
		

		

		options.body().centerIn(body());
		
		add(options);
		
	}
	
	private GuiSection scripts() {
		GuiSection s = new GuiSection();
		
		
		ScrollRow[] butts = new ScrollRow[] {
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
			new ScripButt(script),
		};
		
		Scrollable sc = new GScrollable(butts) {
			
			@Override
			public int nrOFEntries() {
				return script.sc.size();
			}
		};
		
		s.add(sc.getView());
		s.addRelBody(8, DIR.N, new HOVERABLE.Sprite(UI.FONT().H2.getText(D.g("Scripts")), COLORS.label));
		s.addRelBody(8, DIR.S, new RENDEROBJ.RenderImp(300, 32) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				COLOR.WHITE150.bind();
				CharSequence s = script.hover;
				script.hover = null;
				if (s != null) {
					UI.FONT().M.render(r, s, body().x1(), body().y1(), body().width(), 1);
				}
				COLOR.unbind();
				
			}
		});
		
		return s;
	}
	
	private GuiSection race() {
		GuiSection race = new GuiSection();
		race.add(new SpecButt(spec.race));
		race.addDownC(8, new RENDEROBJ.RenderImp(400, 100) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				COLOR.WHITE150.bind();
				int i = 0;
				for (Race ra: RACES.all())
					if (ra.playable) {
						if (i == spec.race.get())
							UI.FONT().M.render(r, ra.info.desc, body().x1(), body().y1(), body().width(), 1);
						i++;
					}
				COLOR.unbind();
				
			}
		});
		return race;
	}
	
	private GuiSection map() {
		GuiSection options = new GuiSection();
		
		SpecButt sb;
		
		
		for (GameConSpec s : spec.getSpecs()){
			if (s == spec.race)
				continue;
			sb = new SpecButt(s);
			options.addDownC(2, sb);
		}
		
		{
			INTE ii = new INTE() {
				
				int i = 0;
				
				@Override
				public int min() {
					return -1;
				}
				
				@Override
				public int max() {
					return types.length;
				}
				
				@Override
				public int get() {
					return i;
				}
				
				@Override
				public void set(int t) {
					if (t < 0)
						t = types.length-1;
					if (t >= types.length)
						t = 0;
					i = t;
					spec.mapTypeset(types[i]);
					
				}
			};
			
			options.addDownC(2, getSmallText(D.g("map")));
			RENDEROBJ rr = new RENDEROBJ.RenderImp(MapType.DIM*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					spec.map().render(r, body.x1(), body.y1(), 2);
				}
			};
			options.addDownC(2, rr);
			
			
			CLICKABLE left = new Button(getBigTexts("<<")) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					activeSet(ii.get() > ii.min());
					super.render(r, ds, activeIs(), isSelected, isHovered);
				}
			};
			left.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					ii.inc(-1);
				}
			});
			//left.bodyM().moveX1(bodyM().gX2() + margin/6);
			options.add(left, options.getLastX1()-left.body().width()-16, options.getLastY1());
			
			CLICKABLE  right = new Button(getBigTexts(">>")) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					activeSet(ii.get() < ii.max());
					super.render(r, ds, activeIs(), isSelected, isHovered);
				}
			};
			right.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					ii.inc(1);
				}
				
			});
			options.add(right, rr.body().x2()+16, options.getLastY1());
			
			
			
			
		}
		return options;
	}
	
	private GuiSection advantage() {
		GETTER_IMP<CharSequence> hText = new GETTER_IMP<>();
		GuiSection s = new GuiSection();
		
		String[] boos = new String[] {
			"FURNITURE",
			"RAIDING",
			"MAINTENANCE",
			"SPOILAGE",
		};
		final double[] values = new double[] {
			-0.5, -0.25, 0, 0.25, 0.5, 1, 2
		};
		
		Json jText = new Json(PATHS.TEXT_CONFIG().get("BOOSTABLE")).json("CIVIC");
		
		s.add(new HOVERABLE.Sprite(UI.FONT().H2.getText(D.g("Advantage")), COLORS.label), 0, 0);
		
		for (String bo : boos) {
			IntImp intt = new IntImp(0, values.length-1) {
				@Override
				public void set(int t) {
					super.set(t);
					spec.BOOSTS.put(bo, values[t]);
				}
			};
			intt.set(2);
			INFO info = new INFO(jText.json(bo));
			CLICKABLE c = new GUI.OptionLine(intt, info.name) {
				private CharSequence desc = info.desc;
				@Override
				protected void setValue(GText str) {
					GFORMAT.percInc(str, values[intt.get()]);
					
				}
				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						hText.set(desc);
						return true;
					}
					return false;
				}
			};
			s.addDown(2, c);
		}
		
		s.add(new RENDEROBJ.RenderImp(300, 100) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				COLOR.WHITE150.bind();
				CharSequence s = hText.get();
				hText.set(null);
				if (s != null) {
					UI.FONT().M.render(r, s, body().x1(), body().y1(), body().width(), 1);
				}
				COLOR.unbind();
				
			}
		}, s.getLastX1()-120, s.getLastY2()+16);
		return s;
	}
	
	private class SpecButt extends GUI.OptionLine{

		private final GameConSpec s;
		
		protected SpecButt(GameConSpec s) {
			super(s, s.getLabel());
			this.s = s;
		}

		@Override
		protected void setValue(GText str) {
			str.add(s.getOptions()[s.get()]);
		}
		
		
	}
	
	private static class ScripSelector {
		
		public final LIST<ScriptLoad> sc = ScriptLoad.getAll();
		public final boolean[] selected = new boolean[sc.size()];
		public CharSequence hover;
		
	}
	
	private static class ScripButt extends CLICKABLE.ClickableAbs implements ScrollRow {

		int index = -1;
		private final ScripSelector ss;
		
		public ScripButt(ScripSelector ss) {
			body.setWidth(300);
			body.setHeight(28);
			this.ss = ss;
		}
		
		@Override
		public void init(int index) {
			this.index = index;
		}
		
		private Font font() {
			return UI.FONT().M;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (ss.selected[index])
				COLORS.selected.bind();
			else if (isHovered)
				COLORS.hover.bind();
			else
				COLORS.inactive.bind();
			font().render(r, ss.sc.get(index).script.name(), body().x1(), body().y1());
			if (isHovered) {
				ss.hover = ss.sc.get(index).script.desc();
			}
			COLOR.unbind();
		}
		
		@Override
		protected void clickA() {
			ss.selected[index] = !ss.selected[index];
		}
		
		
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}
	

	
}
