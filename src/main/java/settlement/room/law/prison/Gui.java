package settlement.room.law.prison;

import game.GAME;
import init.D;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.*;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<PrisonInstance, ROOM_PRISON> {
	
	private static CharSequence ¤¤setAll = "¤Sentence all prisoners to be: {0}";
	private static CharSequence ¤¤setSure = "¤Are you sure you wish to {0} all prisoners?";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_PRISON s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<PrisonInstance> g, int x1, int y1) {
		
		
		
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().prisoners(), g.get().prisonersMax());
			}
		}.hh(blueprint.constructor.prisoners.name()).hoverInfoSet(blueprint.constructor.prisoners.desc());
		section.addRelBody(16, DIR.S, r);
		
		{
			GuiSection ss = new GuiSection();
			for (Punishment p : LAW.process().punishments) {
				GButt.ButtPanel b = new GButt.ButtPanel(p.icon) {
					
					ACTION a = new ACTION() {
						
						@Override
						public void exe() {
							makePrisoners(g.get());
							for (Humanoid h : list) {
								AIModule_Prisoner.DATA().punishment.set(h.ai(), p);
								h.interrupt();
							}
						}
					};
					
					@Override
					protected void clickA() {						
						VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤setSure).insert(0, p.name), a, ACTION.NOP, true);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						GText t = b.text();
						t.add(¤¤setAll).insert(0, p.action);
						b.add(t);
					}
					
				};
				
				b.setDim(40, 40);
				
				ss.addGrid(b, p.index(), 6, 2, 2);
			}
			
			section.addRelBody(8, DIR.S, ss);
		}
		
		GTableBuilder b = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				makePrisoners(g.get());
				return list.size();
			}
		};
		
		b.column(null, 280, new GRowBuilder() {
			
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new CLICKABLE.ClickableAbs(280, 54) {
					
					
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						int k = ier.get();
						if (k >= list.size())
							return;
						Humanoid h = list.get(k);
						h.hover((GBox) text);

					}

					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						GCOLOR.UI().border().render(r, body,-1);
						GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
						
						int k = ier.get();
						if (k >= list.size())
							return;
						Humanoid h = list.get(k);
						int x1 = body().x1();
						STATS.APPEARANCE().portraitRender(r, h.indu(), body().x1(), body().y1(), 1);
						
						Str t = Str.TMP;
						
						t.clear();
						t.add(STATS.LAW().prisonerType.get(h.indu()).name);
						GCOLOR.T().H1.bind();
						UI.FONT().M.render(r, t, x1+50, body().y1()+8);
						
						t.clear();
						t.add(AIModule_Prisoner.DATA().punishment.get(h.ai()).action);
						GCOLOR.T().H2.bind();
						UI.FONT().S.render(r, t, x1+50, body().y1()+32);
						
					}
					
					@Override
					protected void clickA() {
						int k = ier.get();
						if (k >= list.size())
							return;
						Humanoid h = list.get(k);
						h.click();
					}
				};
			}
		});
		
		section.addRelBody(8, DIR.S, b.create(8, false));
		
	}
	
	private final ArrayListResize<Humanoid> list = new ArrayListResize<>(164, 1024*2);
	private int upI = -1;
	
	private void makePrisoners(PrisonInstance ins) {
		if (upI == GAME.updateI())
			return;
		list.clearSoft();
		if (ins == null)
			return;
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				if (AIModule_Prisoner.isPrisoner(a, ins)) {
					list.add(a);
				}		
			}
		}
		upI = GAME.updateI();
	}
	
	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, blueprint.punishUsed(), blueprint.punishTotal());
			}
		}.hh(blueprint.constructor.prisoners.name()).hoverInfoSet(blueprint.constructor.prisoners.desc());
		text.add(r);
	}
	
	@Override
	protected void hover(GBox box, PrisonInstance i) {
		box.NL();
		box.text(blueprint.constructor.prisoners.name());
		box.add(GFORMAT.iofk(box.text(), i.prisoners(), i.prisonersMax()));
	}
	


}
