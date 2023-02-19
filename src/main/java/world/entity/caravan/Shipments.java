package world.entity.caravan;

import java.io.IOException;

import init.RES;
import init.paths.PATHS;
import init.sprite.ICON;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;
import view.main.VIEW;
import world.World;
import world.entity.WEntityConstructor;
import world.entity.caravan.Shipment.Type;
import world.map.regions.Region;

public final class Shipments extends WEntityConstructor<Shipment> {

	final Stack<Shipment> free = new Stack<>(1024);
	public final SPRITE icon;
	
	final TILE_SHEET caravan = (new ITileSheet(PATHS.SPRITE().getFolder("world").getFolder("entity").get("Tribute"), 100, 224) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			
			s.singles.init(0, 0, 1, 8, 2, 1, d.s16);
			for (int i = 0; i < 8; i++) {
				s.singles.setVar(i);
				s.singles.paste(3, true);
			}
			
			return d.s16.saveGame();
			
		}
	}).get();

	public Shipments(LISTE<WEntityConstructor<?>> tot) throws IOException{
		super(tot);
		icon = new SPRITE.Imp(ICON.BIG.SIZE, ICON.BIG.SIZE) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int i = (int) (VIEW.renderSecond()*2)%3;
				i*= 8;
				i+= 8*4;
				COLOR.WHITE150.bind();
				World.ENTITIES().caravans.caravan.render(r, i+DIR.SE.id(), X1, X2, Y1, Y2);
				COLOR.unbind();
				
			}
		};
	}

	public Shipment create(Region start, Region dest) {
		Shipment c = create();

		int i = 9 + RND.rInt(start.area());
		while (i-- > 0) {
			int tx = RES.circle().get(i).x() + start.cx();
			int ty = RES.circle().get(i).y() + start.cy();
			if (World.REGIONS().getter.is(tx, ty, start)) {
				c.add(tx, ty, dest.faction(), Type.tax);
				if (c.added())
					return c;
				return null;
			}
		}
		free.push(c);
		return null;
	}
	
	public Shipment createTrade(int fx, int fy, Region dest) {
		Shipment c = create();
		c.add(fx, fy, dest.faction(), Type.trade);
		if (c.added())
			return c;
		return null;
	}
	
	public Shipment createSpoils(int tx, int ty, Region dest) {
		Shipment c = create();
		c.add(tx, ty, dest.faction(), Type.spoils);
		if (c.added())
			return c;
		return null;
	}

	@Override
	protected Shipment create() {
		if (!free.isEmpty()) {
			return free.pop();
		}
		return new Shipment();
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

}
