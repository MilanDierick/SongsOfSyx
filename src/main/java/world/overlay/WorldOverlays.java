package world.overlay;

import init.C;
import settlement.main.RenderData;
import settlement.main.RenderData.RenderIterator;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.RECTANGLE;
import util.colors.GCOLORS_MAP;
import util.rendering.ShadowBatch;
import world.World;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.map.landmark.WorldLandmark;
import world.map.regions.REGIOND;
import world.map.regions.Region;

public class WorldOverlays {

	private final OverlayRegion regions = new OverlayRegion();
	private final OverlayLandmark landmarks = new OverlayLandmark();
	private Region regionH = null;
	private WorldLandmark landmarkH = null;
	private final OverlayEdger edger = new OverlayEdger(World.TWIDTH(), World.THEIGHT());
	private final OverlayThings things = new OverlayThings();
	private final OverlayArmyMovement army = new OverlayArmyMovement();
	
	public WorldOverlays() {
		regions.add();
	}
	
	public void render(Renderer r, ShadowBatch s, RenderData data, int zoomout){
		RenderIterator it = data.onScreenTiles(0,0,0,0);
		
		
		
		
		s.setHard();
		s.setHeight(0).setDistance2GroundUI(6);
		
		if (army.added())
			army.renderInit();
		
		while(it.has()) {
			if (regions.added())
				regions.render(r, s, it, regionH);
			if (landmarks.added())
				landmarks.render(r, s, it, landmarkH);
			if (army.added())
				army.render(r, s, it);

			it.next();
		}

		things.render(r, s, data);
		army.remove();
		landmarks.remove();
		regionH = null;
		landmarkH = null;
		COLOR.unbind();
		s.setSoft();
		
		edger.render(r, data, zoomout);
		
	}
	
	public void clear() {
		landmarks.remove();
		regionH = null;
		landmarkH = null;
		things.clear();
	}
	
	public WorldOverlayer regions() {
		return regions;
	}
	
	public void hoverRegion(Region region) {
		regionH = region;
		
		if (region == null)
			return;
		
		int x1 = region.cx()*C.TILE_SIZE;
		int y1 = region.cy()*C.TILE_SIZE;
		
		if (REGIOND.isCapitol(region)) {
			hover(x1-C.TILE_SIZE-C.TILE_SIZEH, y1-C.TILE_SIZE-C.TILE_SIZEH, C.TILE_SIZE*4, C.TILE_SIZE*4, REGIOND.faction(region).banner().colorBG(), true);
		}else if (REGIOND.faction(region) != null) {
			hover(x1-C.TILE_SIZE+C.TILE_SIZEH/2, y1-C.TILE_SIZE+C.TILE_SIZEH/2, C.TILE_SIZE*2+C.TILE_SIZEH, C.TILE_SIZE*2+C.TILE_SIZEH, REGIOND.faction(region).banner().colorBG(), false);
		}else {
			hover(x1-C.TILE_SIZE+C.TILE_SIZEH/2, y1-C.TILE_SIZE+C.TILE_SIZEH/2, C.TILE_SIZE*2+C.TILE_SIZEH, C.TILE_SIZE*2+C.TILE_SIZEH, COLOR.WHITE65, false);
		}
	}
	
	public WorldOverlayer landmarks() {
		return landmarks;
	}
	
	public void hoverLandmark(WorldLandmark landmark) {
		landmarkH = landmark;
		landmarks.add();
	}
	
	public void hover(RECTANGLE body, COLOR color, boolean thick, int margin) {
		things.add(body.x1()-margin, body.y1()-margin, body.width()+margin*2, body.height()+margin*2, color, thick);
	}
	
	public void hover(int x1, int y1, int w, int h, COLOR color, boolean thick) {
		things.add(x1, y1, w, h, color, thick);
	}
	
	public void moveArmy(WArmy army) {
		this.army.add(army);
	}
	
	public void hover(WEntity e) {
		hover(e.body(), GCOLORS_MAP.get(e.faction()), true, 6);
	}
	

	
}
