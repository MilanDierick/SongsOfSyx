package view.sett.ui.bottom;

import static settlement.main.SETT.*;

import game.GAME;
import init.D;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.room.infra.gate.ROOM_GATE;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.category.RoomCategories.RoomCategoryMain;
import settlement.room.main.category.RoomCategorySub;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.bottom.Popup.Expansion;
import view.sett.ui.room.construction.UIRoomPlacer;

final class PopupRooms{
	
	private static CharSequence ¤¤move = "¤Move";
	private static CharSequence ¤¤Construct = "¤Construct";
	private static CharSequence ¤¤Build = "¤Build";

	static {
		D.ts(PopupRooms.class);
	}
	
	public static Popup civic(UIRoomPlacer placer, GETTER_IMP<ACTION> last) {
		Popup pp = new Popup();
		for (RoomCategoryMain main : SETT.ROOMS().CATS.MAINS) {
			Popup p = new Popup();
			for (RoomCategorySub s : main.subs) {
				if (s == SETT.ROOMS().CATS.MILITARY)
					continue;
				
				Expansion ex = new Popup.Expansion();

				for (RoomBlueprintImp b : s.rooms()) {
					if (b.isAvailable(SETT.ENV().climate()))
						ex.add(butt(b, placer, last));
				}
				p.add(s.icon(), s.name(), ex);
			}
			for (RoomBlueprintImp s : main.misc.rooms()) {
				if (s.isAvailable(SETT.ENV().climate()))
					p.add(butt(s, placer, last));
			}
			pp.add(main.icon, main.name, p);
		}
		
		{
			Popup p = new Popup();
			for (RoomBlueprintImp s : SETT.ROOMS().CATS.MILITARY.rooms()) {
				if (s instanceof ROOM_GATE)
					p.add(butt(s, placer, last));
			}
			
			p.add(JobsWrapper.forts());
			p.add(butt(JOBS().build_stairs, "BUILD_STAIRS"));
			
			for (RoomBlueprintImp s : SETT.ROOMS().CATS.MILITARY.rooms()) {
				if (!(s instanceof ROOM_GATE))
					p.add(butt(s, placer, last));
			}
			
			
			pp.add(SETT.ROOMS().CATS.MILITARY.icon(), SETT.ROOMS().CATS.MILITARY.name(), p);

			
		}
		
		{
			Popup p = new Popup();
			p.add(JobsWrapper.struct());
			p.add(JobsWrapper.fences());
			p.add(JobsWrapper.roads());
			p.add(buttCat("DECOR", SETT.ROOMS().CATS.DECOR, placer, last));
			pp.add(SPRITES.icons().m.repair, ¤¤Construct, p);

			
		}
		
		
		{
			
			ACTION a = new ACTION() {
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(ROOMS().THRONE.placer);
				}
			};
			String name = ¤¤move + " " + ROOMS().THRONE.info.name;
			GButt.ButtPanel c = new GButt.ButtPanel(¤¤move + " " + ROOMS().THRONE.info.name) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					ROOMS().THRONE.placer.hoverDesc((GBox)text);
				}
				
				@Override
				protected void clickA() {
					a.exe();
					last.set(a);
				}
				
				@Override
				protected void renAction() {
					selectedSet(VIEW.s().tools.placer.getCurrent() == ROOMS().THRONE.placer);
				}
			}.icon(ROOMS().THRONE.icon());
			c.setDim(Popup.width, Popup.bh);
			SearchToolPanel.add(c, name);
			pp.add(KeyButt.wrap(a, c, KEYS.SETT(), "MOVE_THRONE", name, ""));
		}
		
		return pp;
	}

	
	private static CLICKABLE butt(Job j, String key) {
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.close();
				VIEW.s().tools.place(j.placer());
			}
		};
		GButt.ButtPanel b = new GButt.ButtPanel(j.placer().name()) {
			
			@Override
			protected void clickA() {
				super.clickA();
				a.exe();
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.NL();
				if (j.res() != null)
					((GBox) text).setResource(j.res(), j.resAmount());
			}
			
		}.icon(j.placer().getIcon());
		b.setDim(Popup.width, Popup.bh);
		
		
		CLICKABLE c = (KeyButt.wrap(a, b, KEYS.SETT(), key, j.placer().name(), j.placer().desc()));
		SearchToolPanel.add(c, j.placer().name());
		return c;
	}
	
	private static CLICKABLE butt(RoomBlueprintImp b, UIRoomPlacer placer, GETTER_IMP<ACTION> last){
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				if (GAME.player().locks.unlockText(b) == null) {
					VIEW.inters().popup.close();
					placer.init(b, -1,-1);
				}
			}
		};
		
		SPRITE ss = new Text(UI.FONT().H2, b.info.name).setMaxWidth(Popup.width-50).setMultipleLines(false);
		
		GButt.ButtPanel c = new GButt.ButtPanel(ss){
			
			
			
			@Override
			protected void clickA() {
				a.exe();
				last.set(a);
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				UIRoomBuild.hoverRoomBuild(b, text);
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (GAME.player().locks.unlockText(b) != null) {
					OPACITY.O66.bind();
					COLOR.BLACK.render(r, body(),-1);
					OPACITY.unbind();
				}
			};
			
		}.icon(b.iconBig());
		
		c.setDim(Popup.width, Popup.bh);
		
		CLICKABLE cc = KeyButt.wrap(a, c, KEYS.SETT(), "BUILD_" + b.key, b.info.name, ¤¤Build + " " + b.info.name);
		SearchToolPanel.add(cc, b.info.name);
		return cc;
	}
	
	private static CLICKABLE buttCat(String key, RoomCategorySub cat, UIRoomPlacer placer, GETTER_IMP<ACTION> last){
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				for (RoomBlueprintImp b : cat.rooms())
					if (GAME.player().locks.unlockText(b) == null) {
						VIEW.inters().popup.close();
						placer.init(b, cat);
						return;
					}
				
			}
		};
		
		GButt.ButtPanel c = new GButt.ButtPanel(cat.name()){
			
			
			
			@Override
			protected void clickA() {
				a.exe();
				last.set(a);
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox box = (GBox) text;
				text.title(cat.name());
				for (RoomBlueprintImp b : cat.rooms())
					if (GAME.player().locks.unlockText(b) == null) {
						return;
					}
				for (RoomBlueprintImp b : cat.rooms())
					box.NL().error(GAME.player().locks.unlockText(b));
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				for (RoomBlueprintImp b : cat.rooms())
					if (GAME.player().locks.unlockText(b) == null) {
						return;
					}
				OPACITY.O66.bind();
				COLOR.BLACK.render(r, body(),-1);
				OPACITY.unbind();
			};
			
			
		}.icon(cat.icon());
		
		c.setDim(Popup.width, Popup.bh);
		
		CLICKABLE cc = KeyButt.wrap(a, c, KEYS.SETT(), "BUILD_" + key, cat.name(), ¤¤Build + " " + cat.name());
		SearchToolPanel.add(cc, cat.name());
		return cc;
	}
	

}
