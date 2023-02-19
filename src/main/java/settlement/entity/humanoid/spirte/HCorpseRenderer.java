package settlement.entity.humanoid.spirte;

import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.spirte.HSpriteConst.CLAY;
import settlement.stats.*;
import snake2d.Renderer;
import snake2d.util.color.*;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.rendering.ShadowBatch;

public final class HCorpseRenderer {

	private HCorpseRenderer() {
		
	}
	
	private static final COLOR decayC = new ColorImp(48,24,12);
	private static final ColorImp inter = new ColorImp();
	
	public static void renderSkelleton(Race race, boolean adult,
			int direction, boolean inWater,  
			Renderer r, ShadowBatch s, int ran, int x, int y) {
		x += CLAY.off;
		y += CLAY.off;
		int dir = direction;
		if (inWater)
			return;
		s.setHeight(2).setDistance2Ground(0);
		TILE_SHEET sheet = race.appearance().skelleton(adult);
		int tile = 8*(ran&1) + dir;
		sheet.render(r, tile, x, y);
		sheet.render(s, tile, x, y);
		tile = 8*2 + 8*(ran&3) + dir;
		sheet.render(r, tile, x, y);
		sheet.render(s, tile, x, y);
		return;
	}
	
	public static void renderCorpse(Induvidual indu,
			int direction, boolean inWater, 
			double decay, 
			Renderer r, ShadowBatch s, int x, int y) {
		
		x += CLAY.off;
		y += CLAY.off;
		int dir = direction;
		
		
		
		TILE_SHEET sheet = indu.race().appearance().sheet(indu).lay;
		

		if (!inWater) {
			s.setHeight(3).setDistance2Ground(0);
			sheet.render(s, CLAY.SHADOW + dir, x, y);
		}
		
		Appearance ap = STATS.APPEARANCE();
		inter.interpolate(CLAY.pantsC, decayC, decay).bind();
		sheet.render(r, CLAY.PANTS + dir, x, y);
		inter.interpolate(ap.colorSkin(indu), decayC, decay).bind();
		sheet.render(r, CLAY.ARMS + dir, x, y);
		if (ap.hasHair(indu))
			inter.interpolate(ap.colorHair(indu), decayC, decay).bind();
		sheet.render(r, CLAY.HEAD + dir, x, y);
		inter.interpolate(ap.colorClothes(indu), decayC, decay).bind();
		sheet.render(r, CLAY.TORSO + dir, x, y);
		if (STATS.EQUIP().BATTLEGEAR.stat().indu().get(indu) > 0) {
			ap.colorArmour(indu).bind();
			sheet.render(r, CLAY.ARMOR + dir, x, y);
		}
		COLOR.unbind();

		OPACITY.O99.bind();		
		inter.interpolate(COLOR.WHITE100, decayC, decay).bind();

		CLAY.blood(indu, dir, x, y);
		CLAY.filth(indu, dir, x, y);
		
		if (inWater) {
			CLAY.water(indu, dir, x, y);
		}
		
		OPACITY.unbind();
	}
	
	public static void renderDump(Race race, 
			double decay, int dir,
			Renderer r, ShadowBatch s, int ran, int x, int y) {
		
		x += CLAY.off;
		y += CLAY.off;
		
		
		TILE_SHEET sheet = race.appearance().adult().lay;
		
		s.setHeight(3).setDistance2Ground(0);
		sheet.render(s, CLAY.SHADOW + dir, x, y);
		
		decayC.bind();
		sheet.render(r, CLAY.PANTS + dir, x, y);
		sheet.render(r, CLAY.ARMS + dir, x, y);
		sheet.render(r, CLAY.HEAD + dir, x, y);
		sheet.render(r, CLAY.TORSO + dir, x, y);
		COLOR.unbind();

		OPACITY.O99.bind();		
		inter.interpolate(COLOR.WHITE100, decayC, decay).bind();

		CLAY.filth(race, true, decay,dir, ran,x,y);	
		
		OPACITY.unbind();
		COLOR.unbind();
	}
	
	public static void renderGore(Induvidual indu,
			int direction, boolean inWater, 
			double decay, 
			Renderer r, ShadowBatch s, int x, int y) {
		
		int ran = (int) ((indu.randomness2() >> 16) & 7);
		
		x += CLAY.off;
		y += CLAY.off;
		int dir = direction;
		
		TILE_SHEET stencil = RACES.sprites().gore_stencil;
		
		COLOR blood = indu.race().appearance().colors.blood;
		
		TILE_SHEET sheet = indu.race().appearance().sheet(indu).lay;
		

		if (!inWater) {
			s.setHeight(3).setDistance2Ground(0);
			sheet.render(s, CLAY.SHADOW + dir, x, y);
		}
		
		Appearance ap = STATS.APPEARANCE();
		inter.interpolate(CLAY.pantsC, decayC, decay).bind();
		stencil.renderTextured(sheet.getTexture(CLAY.PANTS + dir), ran, x, y);
		

		inter.interpolate(ap.colorSkin(indu), decayC, decay).bind();
		stencil.renderTextured(sheet.getTexture(CLAY.ARMS + dir), ran, x, y);
		if (ap.hasHair(indu))
			inter.interpolate(ap.colorHair(indu), decayC, decay).bind();
		
		stencil.renderTextured(sheet.getTexture(CLAY.HEAD + dir), ran, x, y);
		
		inter.interpolate(ap.colorClothes(indu), decayC, decay).bind();
		stencil.renderTextured(sheet.getTexture(CLAY.TORSO + dir), ran, x, y);
		
		if (STATS.EQUIP().BATTLEGEAR.stat().indu().get(indu) > 0) {
			ap.colorArmour(indu).bind();
			stencil.renderTextured(sheet.getTexture(CLAY.ARMOR + dir), ran, x, y);
		}
		COLOR.unbind();
//
//		OPACITY.O99.bind();		
//		inter.interpolate(COLOR.WHITE100, decayC, decay).bind();

		inter.interpolate(blood, decayC, decay).bind();
		TextureCoords overlay = RACES.sprites().gore_overlay.getTexture(ran);
		sheet.renderTextured(overlay, CLAY.SHADOW+dir, x, y);
		
		
//		CLAY.blood(indu, dir, x, y);
		CLAY.filth(indu, dir, x, y);
//		
		if (inWater) {
			CLAY.water(indu, dir, x, y);
		}
		
		OPACITY.unbind();
		COLOR.unbind();
	}
	
	
}
