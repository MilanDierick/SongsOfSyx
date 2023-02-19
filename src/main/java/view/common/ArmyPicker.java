package view.common;

import game.faction.FACTIONS;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import world.entity.army.WArmy;

public abstract class ArmyPicker extends GuiSection{

	protected abstract boolean canBePicked(WArmy a);
	protected abstract void pick(WArmy a);
	
	public ArmyPicker() {
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				
				return FACTIONS.player().kingdom().armies().all().size();
			}
		};
		
		builder.column("", 200, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		add(builder.create(5, false));
		
		
	}
	
	private class Button extends ClickableAbs{

		private final GETTER<Integer> ier;
		
		public Button(GETTER<Integer> ier) {
			this.ier = ier;
			body.setWidth(200);
			body.setHeight(40);
		}
		
		private WArmy a() {
			return FACTIONS.player().kingdom().armies().all().get(ier.get());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body);
			GCOLOR.UI().bg(canBePicked(a()), false, isHovered).render(r, body, -1);
			GCOLOR.T().H1.bind();
			if (!canBePicked(a()))
				GCOLOR.T().INACTIVE.bind();
			UI.FONT().H2.render(r, a().name, body().x1()+6, body().y1()+4);
			
			SPRITES.icons().s.sword.renderCY(r, body().x1()+6, body().cY()+8);
			
		}
		
		@Override
		protected void clickA() {
			if (canBePicked(a()))
				pick(a());
		}
		
		
		
	}
	
}
