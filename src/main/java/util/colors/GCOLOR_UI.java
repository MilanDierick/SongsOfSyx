package util.colors;

import init.paths.PATHS;
import snake2d.util.color.*;
import snake2d.util.file.Json;

public final class GCOLOR_UI {
	
	private Json d = new Json(PATHS.SPRITE_UI().getLikeHell("Colors.txt")).json("UI");
	
	public final GColorUIModel NORMAL = new GColorUIModel(new ColorImp(d, ("NORMAL")));
	public final GColorUIModel BAD = new GColorUIModel(new ColorImp(d, ("BAD")));
	public final GColorUIModel GOOD = new GColorUIModel(new ColorImp(d, ("GOOD")));
	public final GColorUIModel NEUTRAL = new GColorUIModel(new ColorImp(d, ("NEUTRAL")));
	public final GColorUIModel GOOD2 = new GColorUIModel(new ColorImp(d, ("GOOD2")));
	public final GColorUIModel GREAT = new GColorUIModel(new ColorImp(d, ("GREAT")));
	public final GColorUIModel SOSO = new GColorUIModel(new ColorImp(d, ("SOSO")));
	private static final ColorImp tmp = new ColorImp();
	private final COLOR badShift = new ColorShifting(bg(), new ColorImp(d, ("BAD_SHIFT"))).setSpeed(1);
	private final COLOR goodShift = new ColorShifting(bg(), new ColorImp(d, ("GOOD_SHIFT"))).setSpeed(1);
	GCOLOR_UI() {
		
	}
	public final COLOR gold = new ColorImp(d, ("GOLD"));
	
	public static class GColorUIModel {
		
		public final COLOR normal;
		public final COLOR hovered;
		public final COLOR selected;
		public final COLOR inactive;
		
		private GColorUIModel(COLOR color) {
			this.inactive = color.shade(0.55);
			this.normal = color.shade(0.8);
			this.hovered = color;
			this.selected = color.shade(1.2);
		}
		
		public COLOR get(boolean isActive, boolean isSelected, boolean isHovered) {
			if (!isActive)
				return inactive;
			if (isHovered)
				return hovered;
			if (isSelected)
				return selected;
			return normal;
		}
		
	}
	
	public COLOR border() {
		return COLOR.WHITE35;
	}
	
	public COLOR bg() {
		return COLOR.WHITE10;
	}
	
	public COLOR bg(boolean isActive, boolean isSelected, boolean isHovered) {
		if (!isActive)
			return COLOR.WHITE10;
		if (isSelected)
			return COLOR.WHITE30;
		if (isHovered)
			return COLOR.WHITE25;
		return COLOR.WHITE15;
	}
	
	public static COLOR color(COLOR color, boolean isActive, boolean isSelected, boolean isHovered) {
		
		if (isSelected)
			return tmp.set(color).add(36);
		if (!isActive) {
			tmp.set(color).saturateSelf(0.7);
			return tmp.add(-5);
		}
		if (isHovered)
			return tmp.set(color).add(20);
		return color;
	}
	
	public GColorUIModel bgHov() {
		return NORMAL;
	}
	
	public COLOR badFlash() {
		return badShift;
	}
	
	public COLOR goodFlash() {
		return goodShift;
	}
	
	public GColorUIModel BAD() {
		return BAD;
	}
	
	public GColorUIModel GOOD() {
		return GOOD;
	}
	
	public GColorUIModel SOSO() {
		return SOSO;
	}
	
}
