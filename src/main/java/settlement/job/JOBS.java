package settlement.job;

import static settlement.main.SETT.*;

import java.io.IOException;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.main.*;
import settlement.main.SETT.SettResource;
import settlement.misc.util.TileGetter;
import settlement.tilemap.TGrowable;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.*;
import snake2d.util.sets.*;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;
import view.tool.PLACABLE;

public final class JOBS extends SettResource {

	final static COLOR CRESERVED = new ColorImp(0, 127, 255);

	private final byte[] map = new byte[TAREA];
	private final Bitsmap1D statei = new Bitsmap1D(-1, 2, TAREA);
	public final BOOLEAN_MUTABLE planMode = new BOOLEAN_MUTABLE() {

		private boolean i = false;

		@Override
		public boolean is() {
			return i;
		}

		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			i = b;
			return this;
		}

	};
	private int hoverI = -1;

	final Bitsmap1D progress = new Bitsmap1D(0, 3, TAREA);
	final Bitmap1D wantsRes = new Bitmap1D(TAREA, false);
	final StateManager state = new StateManager(statei);
	int waterTable;

	public final TileGetter<Job> getter;
	public final MAP_INT indexMap = new MAP_INT() {

		@Override
		public int get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx + ty * TWIDTH);
			return Job.NOTHING;
		}

		@Override
		public int get(int tile) {
			return map[tile];
		}
	};
	public final PLACABLE tool_clear = new PlacerDelete();
	public final PLACABLE tool_activate = new PlacerActivate();
	public final PLACABLE tool_dormant = new PlacerDormant();
	public final PLACABLE tool_remove_all = new PlacerRemoveAll();
	public final PLACABLE tool_remove_smartl = new PlacerRemoveSmart();
	public final MAP_OBJECT<Job> jobGetter = new JobGetter();
	final JobRoom room = new JobRoom(null);
	final JobRoom[] rooms = new JobRoom[RESOURCES.ALL().size()];
	{
		for (int i = 0; i < rooms.length; i++)
			rooms[i] = new JobRoom(RESOURCES.ALL().get(i));
	}

	public final PLACABLE tool_repair = new PlacerRepair();

	public final LIST<JobBuildRoad> roads = JobBuildRoad.make();
	public final LIST<JobBuildStructure> build_structure = JobBuildStructure.make();
	public final LIST<Job> build_fort = JobBuildFort.make();
	public final Job build_stairs = new JobBuildFort.Stairs();
	public final LIST<Job> fences = JobBuildFence.make();
	public final JobClears clearss = new JobClears();
	public final LIST<PLACABLE> clears = new ArrayList<>(clearss.placers);

	public JOBS() {

		new Debug();
		getter = new TileGetter<Job>() {

			@Override
			public Job get(int tile) {
				byte i = map[tile];
				if (i != Job.NOTHING) {
					return Job.all.get(i & 0x07F).get(tile % TWIDTH, tile / TWIDTH);
				}

				return null;
			}

			@Override
			public Job get(int tx, int ty) {
				if (!IN_BOUNDS(tx, ty))
					return null;
				int tile = tx + ty * TWIDTH;
				byte i = map[tile];
				if (i != Job.NOTHING) {
					return Job.all.get(i & 0x07F).get(tx, ty);
				}

				return null;
			}

			@Override
			public boolean has(int tile) {
				return map[tile] != Job.NOTHING;
			}

			@Override
			public boolean has(int tx, int ty) {
				return IN_BOUNDS(tx, ty) && map[tx + ty * TWIDTH] != Job.NOTHING;
			}

		};

		new ON_TOP_RENDERABLE() {

			private final COLOR BLOCKED = new ColorImp(50, 50, 127);

			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
				RenderData.RenderIterator i = data.onScreenTiles();
				while (i.has()) {

					if (map[i.tile()] != Job.NOTHING) {

						if (i.tile() == hoverI) {
							COLOR.WHITE2WHITE.bind();
						} else {

							switch (state.get(i.tile())) {
							case DORMANT:
								COLOR.WHITE65.bind();
								break;
							case RESERVABLE:
								SPRITES.cons().color.ok.bind();
								break;
							case RESERVED:
								CRESERVED.bind();
								break;
							case BLOCKED:
								BLOCKED.bind();
								break;
							}

						}
						Job j = Job.all.get(map[i.tile()] & 0x07F);
						
						if (j != null) {
							
							j.renderAbove(r, i.x(), i.y(), 0, i.tx(), i.ty());

							if (CORE.renderer().getZoomout() <= 1) {
								j = j.get(i.tx(), i.ty());
								RESOURCE res = j.resourceCurrentlyNeeded();
								if ((j == clearss.food && !SETT.WEATHER().growthRipe.cropsAreRipe()) || (res != null
										&& !j.jobReservedIs(res) && !PATH().finders.resource.normal.has(i.tx(), i.ty(), res))) {
									COLOR.WHITE702WHITE100.bind();
									SPRITES.cons().ICO.warning.render(r, i.x(), i.y());
									COLOR.unbind();
								}

							}
							// COLOR.unbind();
							// StringReusable.TMP.clear().add(state.getDepth(i.tx(), i.ty()));
							// UI.FONT().H1.render(StringReusable.TMP, i.x(), i.y());
						}
					} else if (SETT.TERRAIN().get(i.tile()) instanceof TGrowable
							&& SETT.TERRAIN().GROWABLES.get(0).job.is(i.tile())) {
						if (i.tile() == hoverI) {
							COLOR.WHITE2WHITE.bind();
						} else {
							COLOR.WHITE65.bind();

						}
						SPRITES.cons().BIG.outline_dashed.render(r, 0, i.x(), i.y());

					}
					i.next();
				}
				COLOR.unbind();

			}
		}.add();

	}

	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles();

		while (i.has()) {
			if (map[i.tile()] != Job.NOTHING) {
				Job.all.get(map[i.tile()] & 0x07F).renderBelow(r, shadowBatch, i, progress.get(i.tile()));
			}
			i.next();
		}

	}

	void set(byte j, int tx, int ty) {
		int i = tx + ty * TWIDTH;
		map[i] = j;
		if (j != Job.NOTHING) {
			update(tx, ty);
		}
		update(tx + 1, ty);
		update(tx - 1, ty);
		update(tx, ty - 1);
		update(tx, ty + 1);
	}

	private void update(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return;
		int i = tx + ty * TWIDTH;
		if (map[i] != Job.NOTHING) {
			if (!isBlocked(tx, ty))
				map[i] &= 0x07F;
			else
				map[i] |= 0x080;
		}
	}

	private boolean isBlocked(int tx, int ty) {

		for (DIR d : DIR.ORTHO) {
			if (!IN_BOUNDS(tx, ty, d))
				continue;
			if (PATH().solidity.is(tx, ty, d))
				continue;
			if (map[tx + d.x() + (ty + d.y()) * TWIDTH] != Job.NOTHING)
				continue;
			return false;
		}
		return true;

	}

	@Override
	protected void save(FilePutter saveFile) {
		saveFile.bs(map);
		statei.save(saveFile);
		progress.save(saveFile);
		wantsRes.save(saveFile);
		saveFile.i(waterTable);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(map);
		statei.load(saveFile);
		progress.load(saveFile);
		wantsRes.load(saveFile);
		waterTable = saveFile.i();

	}

	@Override
	protected void generate(CapitolArea area) {
		int wa = 0;
		for (int i = 0; i < TAREA; i++) {
			if (TERRAIN().WATER.is(i) || TERRAIN().WATER.DEEP.is(i))
				wa++;
			map[i] = Job.NOTHING;
		}
		this.waterTable = wa / 10;
		statei.clear();
		progress.clear();
		wantsRes.clear();
	}
	
	@Override
	protected void init(boolean loaded) {
		clearss.initSpeeds();
	}

	@Override
	protected void update(float ds) {
		hoverI = -1;
	}

	public void hover(int tx, int ty, GBox box) {
		hoverI = tx + ty * SETT.TWIDTH;
		if (getter.has(tx, ty)) {
			Job j = getter.get(tx, ty);
			hoverI = j.tile;
			j.hover(box);

			if (j.resourceCurrentlyNeeded() != null) {
				box.NL();
				box.setResource(j.resourceCurrentlyNeeded(), 1);
			}

			box.NL();
			
			box.add(box.text().add(state.getDepth(tx, ty)));
			
		} else if (SETT.TERRAIN().get(tx, ty) instanceof TGrowable && SETT.TERRAIN().GROWABLES.get(0).job.is(tx, ty)) {
			clearss.HoverEdible(box, tx, ty);
		}
		
		
	}

	public final MAP_SETTER clearer = new MAP_SETTER() {

		@Override
		public MAP_SETTER set(int tx, int ty) {
			PlacerDelete.place(tx, ty);
			return this;
		}

		@Override
		public MAP_SETTER set(int tile) {
			throw new RuntimeException();
		}
	};

}
