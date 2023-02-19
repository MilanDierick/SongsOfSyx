package world.map.terrain;

import static world.World.*;

import java.util.Collections;
import java.util.LinkedList;

import snake2d.util.datatypes.Coo;
import snake2d.util.rnd.RND;
import world.World;

class GeneratorRiver {

	private static int dX;
	private static int dY;
	private static int trials = 0;
	private static int maxLength;
	private static boolean fromOcean = false;

	GeneratorRiver() {
		riveride();
	}

	static int riveride() {

		LinkedList<Coo> coo = new LinkedList<Coo>();

		for (int y = 0; y < THEIGHT() - 1; y++) {
			for (int x = 0; x < TWIDTH() - 1; x++) {
				if (isDeltable(x, y) && RND.rInt(9) == 0) {
					coo.add(new Coo(x, y));
				}
			}
		}

		Collections.shuffle(coo);

		int x;
		int y;

		while (!coo.isEmpty()) {

			Coo v = coo.removeFirst();

			y = v.y();
			x = v.x();

			if (WATER().RIVER.is(x, y))
				continue;

			maxLength = 7 + RND.rInt(TWIDTH() - 1);

			if (WATER().has.is(x, y - 1)) {
				dX = 0;
				dY = 1;
			} else if (WATER().has.is(x, y + 1)) {
				dX = 0;
				dY = -1;
			} else if (WATER().has.is(x - 1, y)) {
				dX = 1;
				dY = 0;
			} else if (WATER().has.is(x + 1, y)) {
				dX = -1;
				dY = 0;
			} else {
				continue;
			}

			fromOcean = WATER().bordersCount(x, y, WATER().OCEAN) == 1;

			trials = 0;

			if (start(y, x, 0, false, false, 0)) {
				placeDelta(x, y);
			}

		}

		for (int i = 0; i < 75; i++) {
			x = RND.rInt(TWIDTH());
			y = RND.rInt(THEIGHT());
			dX = -1 + RND.rInt(3);
			dY = dX == 0 ? -1 + RND.rInt(3) : 0;
			trials = 0;
			maxLength = 7 + RND.rInt(TWIDTH() - 1);
			branch(y, x, 0, false, false, 0);
		}
		
		return 0;
	}

	private static void placeDelta(int x, int y) {
		if (WATER().DELTA_LAKE.isPlacable(x, y, null, null) == null) {
			WATER().DELTA_LAKE.placeRaw(x, y);
		} else {
			WATER().DELTA_OCEAN.placeRaw(x, y);
		}
	}

	private static boolean isDeltable(int x, int y) {
		if (WATER().DELTA_LAKE.isPlacable(x, y, null, null) == null || WATER().DELTA_OCEAN.isPlacable(x, y, null, null) == null) {
			return true;
		}
		return false;
	}

	private static boolean start(int y, int x, int straight, boolean left, boolean right, int length) {

		trials++;

		if (length > maxLength)
			return true;

		if (WATER().has.is(x, y))
			return false;

		if (trials > 1000)
			return false;

		if (length > 0 && isDeltable(x, y)) {
			if (length < 3)
				return false;
			if (fromOcean && WATER().OCEAN.is(x, y))
				return false;
			if (straight <= 1)
				return false;
			if (WATER().borders(x, y, WATER().RIVER))
				return false;
			if (dY == 1 && (WATER().has.is(x, y + 1))) {
				placeDelta(x, y);
				return true;
			} else if (dY == -1 && (WATER().has.is(x, y - 1))) {
				placeDelta(x, y);
				return true;
			} else if (dX == 1 && WATER().has.is(x + 1, y)) {
				placeDelta(x, y);
				return true;
			} else if (dX == -1 && WATER().has.is(x - 1, y)) {
				placeDelta(x, y);
				return true;
			}
			return false;
		}

		if (fromOcean && length > 2 && WATER().borders(x, y, WATER().OCEAN))
			return false;

		if ((x == TWIDTH() - 1 || y == THEIGHT() - 1 || x == 0 || y == 0)) {
			if (length < 3) {
				return false;
			}
			WATER().RIVER.placeRaw(x, y);
			return true;
		}

		if (World.MOUNTAIN().is(x, y) && length > 3 && RND.rInt(8) == 0)
			return true;

		if (WATER().borders(x, y, WATER().RIVER)) {
			if (length > 4 ) {
				WATER().RIVER.placeRaw(x, y);
				return true;
			}
			return false;
		}

		if (straight == 2) {
			straight = 0;
			left = true;
			right = true;
		}

		if (dX == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y, x + 1, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y, x - 1, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y + dY, x, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y, x + 1, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y, x - 1, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		if (dY == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y + 1, x, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y - 1, x, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y, x + dX, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y + 1, x, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y - 1, x, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		return false;

	}
	
	private static boolean branch(int y, int x, int straight, boolean left, boolean right, int length) {

		trials++;

		if (WATER().RIVER.is(x, y))
			return true;
		
		if (WATER().has.is(x, y))
			return false;

		if (trials > 1000)
			return false;

		if ((x == TWIDTH() - 1 || y == THEIGHT() - 1 || x == 0 || y == 0)) {
			if (length < 3) {
				return false;
			}
			WATER().RIVER.placeRaw(x, y);
			return true;
		}

		if (World.MOUNTAIN().is(x, y) && length > 3 && RND.rInt(8) == 0)
			return true;

		if (WATER().borders(x, y, WATER().RIVER)) {
			if (length > 4 ) {
				WATER().RIVER.placeRaw(x, y);
				return true;
			}
			return false;
		}

		if (straight == 2) {
			straight = 0;
			left = true;
			right = true;
		}

		if (dX == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y, x + 1, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y, x - 1, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y + dY, x, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y, x + 1, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y, x - 1, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		if (dY == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y + 1, x, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y - 1, x, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y, x + dX, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y + 1, x, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y - 1, x, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		return false;

	}
	

}
