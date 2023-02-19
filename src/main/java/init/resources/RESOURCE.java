package init.resources;

import java.io.IOException;

import init.C;
import init.paths.PATH;
import init.sprite.ICON;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.*;
import snake2d.util.sprite.TILE_SHEET;
import util.info.INFO;
import util.rendering.ShadowBatch;
import util.spritecomposer.*;
import util.spritecomposer.ComposerThings.ITileSheet;

public final class RESOURCE extends INFO implements INDEXED{
	
	public final String key;
	private final byte index;
	public final long bit;
	private final boolean edible;
	public final boolean edibleServe;
	private final double degradeSpeed;
	private final Sprite sprite;
	private final TILE_SHEET debris;
	private final COLOR tint; 
	private final COLOR miniC;
	public final int category;
	LIST<RESOURCE> tradeSameAs = new ArrayList<>(0);
	
	RESOURCE(LISTE<RESOURCE> all, String key, PATH gData, PATH gText, PATH gSprite, PATH gDebris, KeyMap<RESOURCE> map, KeyMap<Sprite> spriteMap, KeyMap<TILE_SHEET> debrisMap) throws IOException{
		super(new Json(gText.get(key)));
		Json data = new Json(gData.get(key));
		this.key = key;
		index = (byte) all.add(this);
		map.put(key, this);
		bit = 1l << index;
		edible = data.bool("EDIBLE");
		if (data.has("EDIBLE_DONT_SERVE"))
			edibleServe = !data.has("EDIBLE_DONT_SERVE");
		else
			edibleServe = true;
		degradeSpeed = data.d("DEGRADE_RATE", 0, 1);
		tint = new ColorImp(data);
		miniC = new ColorImp(data, "MINIMAP_COLOR");
		category = data.i("CATEGORY_DEFAULT", 0, 3);
		
		String vSprite = data.value("SPRITE");
		if (!spriteMap.containsKey(vSprite)) {
			spriteMap.put(vSprite, new Sprite(gSprite.get(vSprite)));
		}
		this.sprite = spriteMap.get(vSprite);
		
		String vDebris = data.value("SPRITE_DEBRIS");
		if (!debrisMap.containsKey(vDebris)) {
			TILE_SHEET s = new ITileSheet(gDebris.get(vDebris), 716, 28) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 16, 1, d.s16);
					s.singles.setVar(0).paste(1, true);
					return d.s16.saveGame();
				}
			}.get();
			debrisMap.put(vDebris, s);
		}
		this.debris = debrisMap.get(vDebris);
		
		
	}
	
	public final LIST<RESOURCE> tradeSameAs(){
		return tradeSameAs;
	}
	
	public double degradeSpeed() {
		return degradeSpeed;
	}
	
	public final byte bIndex() {
		return index;
	}
	
	@Override
	public int index() {
		return index;
	}

	public ICON.MEDIUM icon() {
		return sprite.icon;
	}

	private final static int max = 3 + 4*2 + 9 + 16;
	
	public void renderLaying(SPRITE_RENDERER r, int x, int y, int random, double amount) {
		
		tint.bind();
		
		if (amount > max)
			amount = max;
		
		if (amount >= 16) {
			amount -= 16;
			int ra = random & 0b0011;
			random = random >> 2;
			random &= 0x7FFFFFFF;
			random |= ra << 30;
			sprite.lay.render(r, (ra&0b011) + 12, x, y);
		}
		
		if (amount >= 9) {
			amount -= 9;
			int ra = random & 0b0011;
			random = random >> 2;
			random &= 0x7FFFFFFF;
			random |= ra << 30;
			int d = -1 + ra;
			sprite.lay.render(r, (ra&0b011) + 8, x+d, y+d);
		}
		
		while(amount >= 4) {
			amount -= 4;
			int ra = random & 0b0111;
			random = random >> 3;
			random &= 0x7FFFFFFF;
			random |= ra << 29;
			int d = -4 + ra;
			sprite.lay.render(r,  (ra&0b011) + 4, x+d, y+d);
		}
		
		while(amount > 0) {
			amount --;
			int ra = random & 0b0111;
			random = random >> 3;
			random &= 0x7FFFFFFF;
			random |= ra << 29;
			int d = -4 + ra;
			sprite.lay.render(r, (ra&0b011), x+d, y+d);
		}
		
		COLOR.unbind();
		
	}
	
	public void renderLayingRel(SPRITE_RENDERER r, int x, int y, int random, double amount) {

		amount*= max;
		renderLaying(r, x, y, random, amount);
		
		
	}
	
	public void renderOne(SPRITE_RENDERER r, int x, int y, int random) {
		
		tint.bind();
		sprite.lay.render(r, (random&0b011), x, y);
		COLOR.unbind();
		
	}

	public void renderCarried(SPRITE_RENDERER r, int cx, int cy, DIR d) {
		tint.bind();
		int dd = sprite.carry.size()/2;
		cx -= dd;
		cy -= dd;
		
		int i = d.id();
		cx += 10*d.x();
		cy += 10*d.y();
		sprite.carry.render(r, i, cx, cy);
		COLOR.unbind();
	}
	
	public void renderDebris(SPRITE_RENDERER r, ShadowBatch s, int x, int y, int ran, int amount) {
		int start = 0;
		
		ran = ran & 0x01F;
		
		amount = CLAMP.i(amount, 0, 5);
		
		for (int i = 0; i < amount; i++) {
			x += -C.SCALE*(ran&0b011) + C.SCALE*((ran>>2)&0b011);
			y += -C.SCALE*((ran>>4)&0b011) + C.SCALE*((ran>>6)&0b011);
			
			debris.render(r, start+(ran&0x01F), x, y);
			ran = ran >> 1;
			
		}
	}
	
	public boolean isEdible() {
		return edible;
	}
	
	public boolean isInMask(long fetchMask) {
		return (bit & fetchMask) != 0;
	}

	public COLOR miniC() {
		return miniC;
	}
	
	@Override
	public String toString() {
		return "" + key + "[" + index + "]";
	}
	
}
