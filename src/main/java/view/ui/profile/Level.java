package view.ui.profile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PLevels;
import game.faction.player.PTitles.PTitle;
import init.D;
import init.boostable.BBoost;
import init.sprite.SPRITES;
import settlement.entity.humanoid.HCLASS;
import settlement.entity.humanoid.HTYPE;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.gui.misc.*;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import world.World;


final class Level extends GuiSection{

	private final CharSequence ¤¤Title = "¤{0} {1} of {2}";
	
	public Level(int height) {
		D.t(this);
		
		GTextR title = new GTextR(new GText(init.sprite.UI.UI.FONT().H1, 64).lablify()) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				text().clear();
				text().add(¤¤Title);
				text().insert(0, FACTIONS.player().level().current().name());
				text().insert(1, FACTIONS.player().ruler().name);
				text().insert(2, FACTIONS.player().appearence().name());
				text().adjustWidth();
				super.render(r, ds, isHovered);
			};
		}.setAlign(DIR.N);
		add(title);
		
		RENDEROBJ desc = new RENDEROBJ.RenderImp(720, 64) {
			
			private final GText text = new GText(init.sprite.UI.UI.FONT().M, 200);
			private final GText tmp = new GText(init.sprite.UI.UI.FONT().M, 200);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				text.clear();
				
				int y1 = body().y1()+ text.height()/2;
				
				for (PTitle t : FACTIONS.player().titles.all()) {
					if (t.selected()) {
						tmp.set(text);
						if (text.width() > 0 && tmp.width() + text.width() > body().width()) {
							text.renderC(r, body().cX(), y1);
							text.clear();
							text.add(t.name);
							text.adjustWidth();
							y1 += text.height();
						}else {
							if (text.width() > 0)
								text.add(',').s();
							text.add(t.name);
							text.adjustWidth();
						}
					}
					
					
					
						
				}
				
				if (text.width() > 0) {
					text.renderC(r, body().cX(), y1+text.height()/2);
					
				}
				
			}
		};
		
		addDownC(8, desc);
		
		GuiSection stats = new GuiSection();
		
		stats.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(HCLASS.CITIZEN).get(null) + World.ARMIES().cityDivs().total());
			}
		}.hv(HCLASS.CITIZEN.names));
		
		stats.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(HCLASS.NOBLE).get(null));
			}
		}.hv(HCLASS.NOBLE.names));
		
		stats.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text,  FACTIONS.player().kingdom().realm().population().get(null));
			}
		}.hv(HTYPE.SUBJECT.names));
		
		addRelBody(0, DIR.S, stats);
		
		
		RENDEROBJ[] rens = new RENDEROBJ[GAME.player().level().all().size()];
		for (int i = 0; i < rens.length; i++) {
			rens[i] = new TRow(GAME.player().level().all().get(i));
		}
		
		RENDEROBJ r = new GScrollRows(rens, height-getLastY2()-16, 0).view();
		
		addDownC(8, r);
	}
	
	private class TRow extends GuiSection {
		
		PLevels.Level l;
		
		TRow(PLevels.Level l){
			this.l = l;
			add(new GHeader(l.name()));
			HOVERABLE h = new GStat() {
				
				@Override
				public void update(GText text) {
					text.add(l.popNeeded());
				}
			}.hh(SPRITES.icons().s.human);
			h.body().moveX1(250).moveCY(this.body().cY());
			add(h);
			body().incrW(100);
			
			body().incrH(18);
			
			GuiSection s = new GuiSection();
			
			int m = 0;
			for (BBoost b : l.boosts()) {
				s.addRightC(80, b.boostable.icon());
				if (m++ > 4)
					break;
			}
			s.body().moveX1(body().x1());
			s.body().moveY2(body().y2());
			merge(s);
			
			body().incrH(32);
			s = new GuiSection();
			for (RoomBlueprintImp b : l.roomsUnlocks()) {
				s.addRightC(8, b.iconBig());
				
			}
			s.body().moveX1(body().x1());
			s.body().moveY2(body().y2());
			merge(s);
			pad(4);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			super.render(r, ds);
			if (l.index() > GAME.player().level().current().index()) {
				OPACITY.O75.bind();
				COLOR.BLACK.render(r, body(), -1);
				OPACITY.unbind();
			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			l.hoverInfoGet(text);
		}
		
	}
	
}
