package settlement.main;


import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.*;
import init.settings.S;
import init.sound.SOUND;
import settlement.army.ArmyManager;
import settlement.army.Div;
import settlement.army.ai.ARMY_AI;
import settlement.entity.ENTETIES;
import settlement.entity.animal.ANIMALS;
import settlement.entity.humanoid.Humanoids;
import settlement.entry.SENTRY;
import settlement.environment.ENVIRONMENT;
import settlement.invasion.Invador;
import settlement.job.JOBS;
import settlement.maintenance.MAINTENANCE;
import settlement.misc.ParticleRenderer;
import settlement.misc.SettPlacability;
import settlement.misc.placers.ComplexPlacers;
import settlement.overlay.SettOverlay;
import settlement.path.AvailabilityListener;
import settlement.path.PATHING;
import settlement.room.main.ROOMS;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import settlement.stats.health.HEALTH;
import settlement.stats.law.LAW;
import settlement.stats.standing.STANDINGS;
import settlement.thing.THINGS;
import settlement.thing.halfEntity.HalfEnts;
import settlement.thing.pointlight.POINTLIGHTS;
import settlement.thing.projectiles.SProjectiles;
import settlement.tilemap.*;
import settlement.weather.SWEATHER;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.RGB;
import snake2d.util.datatypes.*;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.light.AmbientLight;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import util.rendering.Minimap;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.subview.GameWindow;

public final class SETT extends GameResource{

	public static final int TWIDTH = C.SETTLE_TSIZE;
	public static final int THEIGHT = C.SETTLE_TSIZE;
	public static final int PWIDTH = TWIDTH*C.TILE_SIZE;
	public static final int PHEIGHT = THEIGHT*C.TILE_SIZE;
	public static final int TAREA = TWIDTH*THEIGHT;
	public static final RECTANGLE TILE_BOUNDS = new Rec(0, TWIDTH, 0, THEIGHT);
	public static final RECTANGLE TILE_BOUNDS_I = new Rec(1, TWIDTH-1, 1, THEIGHT-1);
	public static final RECTANGLE PIXEL_BOUNDS = new Rec(0, TWIDTH*C.TILE_SIZE, 0, THEIGHT*C.TILE_SIZE);
	public static final SettlementGrid GRID = new SettlementGrid();
	private static SETT i;
	
	public static boolean IN_BOUNDS(int tx, int ty){
		return TILE_BOUNDS.holdsPoint(tx, ty);
	}
	
	public static boolean IN_BOUNDS(COORDINATE c){
		return TILE_BOUNDS.holdsPoint(c.x(), c.y());
	}
	
	public static boolean IN_BOUNDS(COORDINATE c, DIR d){
		return TILE_BOUNDS.holdsPoint(c.x()+d.x(), c.y()+d.y());
	}
	
	public static boolean IN_BOUNDS(int tx, int ty, DIR d){
		return IN_BOUNDS(tx + d.x(), ty + d.y());
	}
	
	public static boolean PIXEL_IN_BOUNDS(int x, int y){
		return PIXEL_BOUNDS.holdsPoint(x, y);
	}
	
	public static ENTETIES ENTITIES() {
		return i.eHandler;
	}
	
	public static Terrain TERRAIN() {
		return i.terrain.topology;
	}
	
	public static TileMap TILE_MAP() {
		return i.terrain;
	}
	
	public static Ground GROUND() {
		return i.terrain.ground;
	}
	
	public static Floors FLOOR() {
		return i.terrain.floors;
	}
	
	public static Grass GRASS() {
		return i.terrain.grass;
	}
	
	public static Fertility FERTILITY() {
		return i.terrain.fertility;
	}
	
	public static PATHING PATH() {
		return i.path;
	}
	
	public static JOBS JOBS() {
		return i.jobs;
	}
	
	public static THINGS THINGS(){
		return i.things;
	}
	
	public static ROOMS ROOMS(){
		return i.rooms;
	}
	
	public static CapitolArea WORLD_AREA(){
		return i.worldArea;
	}
	
	public static ANIMALS ANIMALS(){
		return i.animals;
	}
	
	public static Humanoids HUMANOIDS(){
		return i.creatures;
	}
	
	public static Minables MINERALS(){
		return GROUND().minerals;
	}
	
	public static MAINTENANCE MAINTENANCE(){
		return i.maintenance;
	}
	
	public static SettPlacability PLACA() {
		return i.placability;
	}
	
//	public static FormationManager BATTLE(){
//		return i.battle;
//	}
	
	public static ArmyManager ARMIES(){
		return i.battle2;
	}
	
	public static ParticleRenderer PARTICLES() {
		return i.particles;
	}
	
	public static POINTLIGHTS LIGHTS() {
		return i.lights;
	}
	
	public static SETT CITY() {
		return i;
	}
	
	public static Faction FACTION() {
		return i.faction();
	}
	
	public static HalfEnts HALFENTS() {
		return i.halfEnts;
	}
	
	public static SProjectiles PROJS() {
		return i.projectiles;
	}
	
	public static ENVIRONMENT ENV() {
		return i.env;
	}
	
	public static SettMaps MAPS() {
		return i.maps;
	}
	
	public static SettOverlay OVERLAY() {
		return i.details;
	}
	
	public static ComplexPlacers PLACERS() {
		return i.complexPlacers;
	}
	
	public static ARMY_AI ARMY_AI() {
		return i.armyAI;
	}
	
	public static SettBorder BORDERS() {
		return i.terrain.borders;
	}
	
	public static Invador INVADOR() {
		return i.invador;
	}
	
	public static Minimap MINIMAP() {
		return i.minimap;
	}
	
	public static SWEATHER WEATHER() {
		return i.weather;
	}
	
	public static SENTRY ENTRY(){
		return i.entry;
	}
	
	{i = this;}

	private final SettMaps maps = new SettMaps();
	private final SettPlacability placability = new SettPlacability();
	private final ParticleRenderer particles = new ParticleRenderer();
	private final ENTETIES eHandler = new ENTETIES();
	private final ENVIRONMENT env = new ENVIRONMENT();
	private final THINGS things = new THINGS();
	private final SProjectiles projectiles = new SProjectiles();
	
	private final settlement.tilemap.TileMap terrain = new settlement.tilemap.TileMap();
	private final ANIMALS animals = new ANIMALS();
	public final ROOMS rooms = new ROOMS();
	private final JOBS jobs = new JOBS();

	private final ArmyManager battle2 = new ArmyManager(this);
	private final SENTRY entry = new SENTRY();

	private final ARMY_AI armyAI = new ARMY_AI();
	
	private boolean exists = false;
	private final ShadowBatch.Real shadowBatch = new ShadowBatch.Real();
	private final ShadowBatch shadowDummy = new ShadowBatch.Dummy();
	
	private final RenderData renData = new RenderData(TWIDTH, THEIGHT);
	
	private final CapitolArea worldArea = new CapitolArea();
	{new LAW();}
	private final Humanoids creatures = new Humanoids();
	private final MAINTENANCE maintenance = new MAINTENANCE();
	private final POINTLIGHTS lights = new POINTLIGHTS();
	private final ComplexPlacers complexPlacers = new ComplexPlacers();
	private final HalfEnts halfEnts = new HalfEnts();
	private final PATHING path = new PATHING();

	private final Invador invador = new Invador();
	private final Minimap minimap = new Minimap(C.SETTLE_TSIZE);
	private final SWEATHER weather = new SWEATHER();
	private final SettOverlay details = new SettOverlay();
	
	public SETT() throws IOException{
		new TUpdater();
		
		STATS.create();
		STANDINGS.create();
		new HEALTH();
		IDebugPanelSett.add("Regenerate settlement", new ACTION() {
			
			@Override
			public void exe() {
				reGenerate();
			}
		});
		
	}
	
	public void CreateFromWorldMap(int wx1, int wy1, SGenerationConfig config){
		D.gInit(getClass());
		this.worldArea.init(wx1, wy1, config);
		VIEW.s().clear();
		RES.loader().init();
		RES.loader().print(D.g("Clearing"));
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			r.clearBeforeGeneration(worldArea);
		}
		
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print(""+D.g("Generating") + ": " + (1+i) + "/" + SettResource.resources.size());
			r.generate(worldArea);
		}
		
		
		SettlementGrid.Tile t = GRID.tile(worldArea.arrivalTile());
		rooms.THRONE.init.markArround(t.coo(DIR.C).x(), t.coo(DIR.C).y());
		VIEW.s().getWindow().centerAt(
				THRONE.coo().x()*C.TILE_SIZE, 
				THRONE.coo().y()*C.TILE_SIZE);
		//events.landingPartys[0].placeSingle(terrain.rooms.THRONE.getThrone().x(), terrain.rooms.THRONE.getThrone().y());
		setExists();
		
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print(""+D.g("Initializing") + ": " + (1+i) + "/" + SettResource.resources.size());
			r.init(false);
		}

		AvailabilityListener.listenAll(true);
		update(0);
		//ArroundPlacer.placeArround(PLACERS().landingParty, terrain.rooms.THRONE.getThrone().x(), terrain.rooms.THRONE.getThrone().y());
		//update(0);
		System.gc();
		for (ACTION a : gHooks)
			a.exe();
		for (Div d : ARMIES().player().divisions())
			d.info.race.set(GAME.player().race());
		
		
		
		
	}
	
	public static int tileRan(int tx, int ty) {
		return i.renData.random(tx, ty);
	}
	
	public static void reGenerate() {
		i.CreateFromWorldMap(i.worldArea.tiles().x1(), i.worldArea.tiles().y1(), i.worldArea.config());
	}
	
	private void setExists(){
		if (GAME.SPEED.speedTarget() > 1)
			GAME.SPEED.speedSet(1);
		exists = true;
	}
	
	@Override
	protected void save(FilePutter saveFile){
		
		saveFile.bool(exists);
		if (!exists)
			return;
		D.gInit(getClass());
		worldArea.saver.save(saveFile);
		
		int k = 0;
		int m = SettResource.resources.size();
		
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print(D.g("Saving Settlement") + ": " + k++ + "/" + m);
			saveFile.mark(r);
			r.save(saveFile);
			saveFile.mark(r);
			
		}
		
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException{

		D.gInit(getClass());
		
		exists = saveFile.bool();
		
		if (!exists)
			return;
	
		worldArea.saver.load(saveFile);
		
		
		int k = 0;
		int m = SettResource.resources.size();
		
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print(D.g("Loading Settlement") + ": " + k++ + "/" + m);
			saveFile.check(r);
			r.load(saveFile);
			saveFile.check(r);
		}
		
		k = 0;
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print(D.g("Initializing") + ": " + k++ + "/" + m);
			r.init(true);
		}
		
		setExists();
		
//		for (HStat s : POPSTATS().all()) {
//			fixStat(s);
//		}
		
		
	}
	
//	private void fixStat(HStat s) {
//		
//		double g = s.global().getPlayer();
//		double b = s.global().get(FACTION().race(), HTYPE.CITIZEN);
//		POPSTATS().clear(s);
//		for(ENTITY e : ENTITIES().getAllEnts())
//			if (e instanceof Humanoid && !e.isRemoved())
//				POPSTATS().reAdd(s, (Humanoid)e);
//		
//		if (g != s.global().getPlayer()) {
//			OUT.err(s.name() + " " + g + " " + s.global().getPlayer() + " " + s.global().get(FACTION().race(), HTYPE.CITIZEN));
//		}
//		
//		if (b != s.global().get(FACTION().race(), HTYPE.CITIZEN)) {
//			OUT.err("b " + g + " " + s.name() + " " + b + " " + s.global().get(FACTION().race(), HTYPE.CITIZEN));
//		}
//		
////		if (s.global().getPlayer() != s.global().get(FACTION().race(), SUBJECT_CAST.CITIZEN))
////			OUT.err("q " + s.name() + " " + s.global().getPlayer() + " " + s.global().get(FACTION().race(), SUBJECT_CAST.CITIZEN));
//		
//	}
	
	public static void init() {
		int k = 0;
		int m = SettResource.resources.size();
		for (int i = 0; i < SettResource.resources.size(); i++) {
			SettResource r = SettResource.resources.get(i);
			RES.loader().print("Initing Settlement " + k++ + "/" + m);
			r.init(false);
		}
	}

	
	public void render(Renderer r, float ds, GameWindow window) {
		render(r, ds, window.zoomout(), window.pixels(), window.view().x1()<<window.zoomout(), window.view().y1()<<window.zoomout());
	}

	private final Rec tmpWin = new Rec();
	
	public void render(Renderer r, float ds, int zoomout, int cx, int cy, RECTANGLE bounds) {
		
		int offX = bounds.x1() << zoomout;
		int offY = bounds.y1() << zoomout;
		
		int w = bounds.width() << zoomout;
		int h = bounds.height() << zoomout;
		
		
		int x1 = cx - w/2;
		int y1 = cy -h/2;
		tmpWin.moveX1Y1(x1, y1);
		tmpWin.setWidth(w);
		tmpWin.setHeight(h);
		render(r, ds, zoomout, tmpWin, offX, offY);
		
	}
	
	private void renderFrame(int zoomout, RECTANGLE renWindow, int offX, int offY) {
		CORE.renderer().newLayer(false, 0);
		
		
		COLOR c = COLOR.WHITE10;
		
		int x1 = offX;
		int y1 = offY;
		int x2 = offX + (renWindow.width());
		int y2 = offY + (renWindow.height());
		AmbientLight.full.register(C.DIM());
		if (renWindow.x1() < 0) {
			x1 = (-renWindow.x1());
			x1 += offX;
		}
		if (renWindow.x2() > SETT.PWIDTH) {
			x2 = renWindow.width();
			x2 -= (renWindow.x2()-SETT.PWIDTH);
			x2 += offX;
		}
		
		if (renWindow.y1() < 0) {
			y1 = (-renWindow.y1());
			y1 += offY;
		}
		if (renWindow.y2() > SETT.PHEIGHT) {
			y2 = renWindow.height();
			y2 -= (renWindow.y2()-SETT.PHEIGHT);
			y2 += offY;
		}
		
		x1 = x1 >> zoomout;
		y1 = y1 >> zoomout;
		x2 = x2 >> zoomout;
		y2 = y2 >> zoomout;

		
		c.render(CORE.renderer(), x1-32, x1, y1-32, y2+32);
		c.render(CORE.renderer(), x2, x2+32, y1-32, y2+32);
		c.render(CORE.renderer(), x1, x2, y1-32, y1);
		c.render(CORE.renderer(), x1, x2, y2, y2+32);
	}
	
	/**
	 * 
	 * @param r
	 * @param ds
	 * @param spec
	 * @param renWindow - window of game world
	 * @param offX - absolute start X
	 * @param offY - absolute start Y;
	 */
	public void render(Renderer r, float ds, int zoomout, RECTANGLE renWindow, int offX, int offY){
		
		if (zoomout > 3) {
			return;
		}
		
		renderFrame(zoomout, renWindow, offX, offY);
		
		if (zoomout == 3) {
			renderSemiMap(r, ds, zoomout, renWindow, offX, offY);
			return;
		}
		
		SOUND.sett().set(1.0/(1 + zoomout*1.5));
		CORE.getSoundCore().set(renWindow.cX()+offX, renWindow.cY()+offY);
		ds *= GAME.SPEED.speedTarget();
		
		ShadowBatch s = shadowDummy;
		if (S.get().shadows.get() > 0){
			shadowBatch.init(zoomout, TIME.light().shadow.sx(), TIME.light().shadow.sy());
			s = shadowBatch;
		}

		renData.init(renWindow, offX, offY);
		weather.renderDownfall(r, ds, renData, zoomout);
		
		projectiles.renderAbove(r, s, ds, zoomout, renData);
		
		details.renderAbove(r, renData, zoomout);
		
		for (int i = ON_TOP_RENDERABLE.renderables.size()-1; i >= 0; i --) {
			ON_TOP_RENDERABLE ren = ON_TOP_RENDERABLE.renderables.get(i);
			r.newLayer(false, zoomout);
			AmbientLight.full.register(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout);
			ren.render(r, s, renData);
		}
		
		halfEnts.renderInit(renWindow);
		
//		r.newLayer(false, zoomout);
//		AmbientLight.full.register(0, C.WIDTH<<zoomout, 0, C.HEIGHT<<zoomout);
//		for (ON_TOP_RENDERABLE ren : ON_TOP_RENDERABLE.renderables) {
//			
//			ren.render(r, s, renData);
//		}
		
		halfEnts.renderAbove(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		terrain.renderAboveEnts(r, s, ds, zoomout, renData);
		
		//RENDER ENTITIES
		
		r.newLayer(false, zoomout);
		halfEnts.render(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		eHandler.renderA(r, s, ds, renWindow, offX, offY);
		
		r.newLayer(false, zoomout);
		things.render(r, s, ds, renWindow, offX, offY);
		r.newLayer(false, zoomout);
		lights.render(r, s, ds, renWindow, offX, offY);
		
		r.newLayer(false, zoomout);
		halfEnts.renderBelow(r, s, ds, renWindow, offX, offY);
		
		r.newLayer(false, zoomout);
		VIEW.current().renderBelowTerrain(r, s, renData);
		
		
		r.newLayer(false, zoomout);
		
		terrain.renderTheRest(r, s, ds, zoomout, renData);
		
		for (SettResource rs : SettResource.resources) {
			rs.postRender(ds);
		}
		
		
		
		double nature = renData.vegitations();
		nature /= renData.area();
		nature /= zoomout+1;
		
		if (nature > 1)
			nature = 1;
		if (nature < 0)
			nature = 0;
		
		if (SETT.WEATHER().rain.getD() > 0 && !SETT.WEATHER().snow.rainIsSnow() && !GAME.SPEED.isPaused()) {
			SOUND.ambience().rain.play(1.0, SETT.WEATHER().rain.getD());
		}else {
			if (TIME.light().nightIs()) {
				SOUND.ambience().night.play(0.5, 1.0/(zoomout+1));
			}else {
				SOUND.ambience().nature.play(0.5, nature);
			}
		}

		weather.thunder.makeSounds(1.0, ds);
		
		{
			double ocean = renData.waters();
			ocean /= renData.area();
			ocean /= zoomout+1;
			if (ocean > 1)
				ocean = 1;
			if (ocean < 0)
				ocean = 0;
			SOUND.ambience().water.play(0.5, ocean);
		}
		
		double cave = renData.caves()* WEATHER().wind.getD();
		cave /= renData.area();
		SOUND.ambience().windhowl.play(0.5, cave*0.3);
		
		
		if (zoomout >= 2) {
			SOUND.ambience().wind.play(0.5, 0.5*(WEATHER().wind.getD()*1.0 - cave));
		}else {
			nature *= 2;
			SOUND.ambience().windTrees.play(0.5, CLAMP.d(WEATHER().wind.getD()*nature, 0, 1));
			SOUND.ambience().wind.play(0.5, 0.5*(WEATHER().wind.getD()*(1.0-nature-cave)));
			
		}
		
	}
	
	public void renderSemiMap(Renderer r, float ds, int zoomout, RECTANGLE renWindow, int offX, int offY) {
		
		
		renData.init(renWindow, offX, offY);
		
		{
			r.newLayer(false, 3);
			AmbientLight.full.register(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout);
			details.renderAbove(r, renData, zoomout);
		}
		for (int i = ON_TOP_RENDERABLE.renderables.size()-1; i >= 0; i --) {
			ON_TOP_RENDERABLE ren = ON_TOP_RENDERABLE.renderables.get(i);
			r.newLayer(false, 3);
			AmbientLight.full.register(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout);
			ren.render(r, shadowBatch, renData);
		}
		r.newLayer(false, 3);
		TIME.light().apply(C.DIM().x1()<<3, C.DIM().x2()<<3, C.DIM().y1()<<3, C.DIM().y2()<<3, RGB.WHITE);
		
		
		THINGS().renderZoomed(r, renWindow, offX, offY);
		
		ENTITIES().renderZoomed(r, shadowBatch, ds, renWindow, offX, offY);
		HALFENTS().renderZoomed(r, shadowBatch, ds, renWindow, offX, offY);
		r.newLayer(true, 3);
		//details.renderOnGround(r, shadowBatch, renData, zoomout);
		
		VIEW.current().renderBelowTerrain(r, shadowBatch, renData);
		
		TILE_MAP().renderSemiMap(r, ds, renData);
		
	}
	
	
	@Override
	protected void update(float ds){

		if (!exists)
			return;
		for (SettResource r : SettResource.resources)
			r.update(ds);
		
	}
	
	@Override
	protected void afterTick() {
		for (SettResource r : SettResource.resources)
			r.afterTick();
	}
	
	public static abstract class SettResource {
		
		private final static LinkedList<SettResource> resources = new LinkedList<SettResource>();

		static {
			new GameDisposable() {
				@Override
				protected void dispose() {
					resources.clear();
				}
			};
		}
		
		
		protected SettResource() {
			resources.add(this);
		}
		
		protected void save(FilePutter file) {
			
		}
		
		protected void load(FileGetter file) throws IOException{
			
		}
		
		protected void clearBeforeGeneration(CapitolArea area){
			
		}
		
		protected void generate(CapitolArea area){
			
		}
		
		protected void update(float ds){
			
		}
		
		/**
		 * Will be called once after the settlement has renderered
		 * @param ds
		 */
		protected void postRender(float ds) {
			
		}
		
		protected void afterTick() {
			
		}
		
		protected void updateTileDay(int tx, int ty, int tile) {
			
		}
		
		protected void init(boolean loaded) {
			
		}

	}
	
	
	public static boolean exists(){
		return i.exists;
	}

	public Faction faction() {
		return FACTIONS.player();
	}
	
	private final ArrayList<ACTION> gHooks = new ArrayList<>(16);
	
	public static void addGeneratorHook(ACTION action) {
		i.gHooks.add(action);
	}
	

	
	
}
