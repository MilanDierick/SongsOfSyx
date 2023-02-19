package util.info;

import game.time.TIME;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.gui.misc.GText;

public class GFORMAT {

	private GFORMAT() {

	}

	public static GText text(GText text, CharSequence t) {
		text.color(GCOLOR.T().INORMAL);
		text.add(t);
		return text;
	}

	public static GText i(GText text, long i) {
		
		text.color(GCOLOR.T().INORMAL);
		if (i < 0) {
			text.add('-');
			i = -i;
		}
		if (i > 999999999) {
			long d = i % 1000000000;
			i /= 1000000000;
			text.add(i);
			text.add('.');
			text.add(d / 100000000);
			text.add('B');
		} else if (i > 999999) {
			long d = i % 1000000;
			i /= 1000000;
			text.add(i);
			text.add('.');
			text.add(d / 100000);
			text.add('M');
		} else if (i > 999) {
			long d = i % 1000;
			i /= 1000;
			text.add(i);
			text.add('.');
			text.add(d / 100);
			text.add('K');
		} else {
			formatI(text, i);
		}

		return text;
	}

	public static void colorInter(GText text, double i, double k) {
		double d = (double) i / k;
		if (!Double.isFinite(d))
			d = 1.0;
		CLAMP.d(d, 0, 1);
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().INORMAL, GCOLOR.T().IBAD, d));
	}

	public static void colorInterInv(GText text, double i, double k) {
		double d = (double) i / k;
		if (!Double.isFinite(d) || d > 1)
			d = 1.0;
		if (d < 0)
			d = 0;
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().INORMAL, d));
	}

	public static GText iIncr(GText text, long i) {

		if (i >= 0) {
			text.add('+');
		}

		i(text, i);

		if (i > 0) {
			text.color(GCOLOR.T().IGOOD);
		} else if (i < 0) {
			text.color(GCOLOR.T().IBAD);
		} else
			text.color(COLOR.WHITE85);
		return text;
	}

	public static GText iIncrBig(GText text, int i) {

		if (i >= 0) {
			text.add('+');
		}

		iBig(text, i);

		if (i > 0) {
			text.color(GCOLOR.T().IGOOD);
		} else if (i < 0) {
			text.color(GCOLOR.T().IBAD);
		} else
			text.color(COLOR.WHITE85);
		return text;
	}

	private static void formatI(Text text, long i) {
		if (i == 0) {
			text.add(0);
			return;
		}
		formatIR(text, i, 0);
	}

	private static long formatIR(Text text, long i, long r) {
		if (i == 0) {
			return r;
		}

		if (i < 0) {
			text.add('-');
			i = -i;
		}

		long mod = i % 10;
		long k = formatIR(text, i / 10, r + 1);
		if (k != 0 && k % 3 == 0 && i/10 != 0)
			text.s();
		text.add(mod);
		
		return r;

	}

	public static GText iBig(GText text, long i) {
		if (i < 0) {
			text.add('-');
			text.color(GCOLOR.T().IBAD);
			i = -i;
		} else
			text.color(GCOLOR.T().INORMAL);
		formatI(text, i);
		return text;
	}

	public static GText iofkNoColor(GText text, long i, long k) {
		formatI(text, i);
		text.add('/');
		formatI(text, k);
		return text;
	}

	/**
	 * the smaller i, the better
	 */
	public static GText iofk(GText text, long i, long k) {
		double d = (double) i / k;
		if (!Double.isFinite(d))
			d = 1.0;
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().INORMAL, GCOLOR.T().IBAD, d));
		formatI(text, i);
		text.add('/');
		formatI(text, k);
		return text;
	}

	public static GText dofk(GText text, double d, double k) {
		double dd = (double) d / k;
		if (!Double.isFinite(dd))
			dd = 1.0;
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().INORMAL, dd));
		text.add(d, 1);
		text.add('/');
		text.add(k, 1);
		return text;
	}

	/**
	 * the larger i, the better
	 */
	public static GText iofkInv(GText text, long i, long k) {
		double d = (double) i / k;
		if (!Double.isFinite(d))
			d = 1.0;
		if (k == 0)
			d = 1.0;
		d = CLAMP.d(d, 0, 1);
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().INORMAL, d));
		formatI(text, i);
		text.add('/');
		formatI(text, k);
		return text;
	}

	public static GText iofkInv(GText text, double i, long k) {
		double d = (double) i / k;
		if (!Double.isFinite(d))
			d = 1.0;
		if (k == 0)
			d = 1.0;
		d = CLAMP.d(d, 0, 1);
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().INORMAL, d));

		text.add(i, 2);
		text.add('/');
		formatI(text, k);
		return text;
	}
	
	public static GText fofkInv(GText text, double i, double k) {
		double d = (double) i / k;
		if (!Double.isFinite(d))
			d = 1.0;
		if (k == 0)
			d = 1.0;
		d = CLAMP.d(d, 0, 1);
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().INORMAL, d));

		text.add(i, 2);
		text.add('/');
		text.add(k, 2);
		return text;
	}

	public static GText f(GText text, double f) {
		text.color(GCOLOR.T().INORMAL);
		if (Double.isFinite(f))
			text.add(f, 2);
		else
			text.add('-');
		return text;
	}
	
	public static GText f(GText text, double f, int dec) {
		text.color(GCOLOR.T().INORMAL);
		if (Double.isFinite(f))
			text.add(f, dec);
		else
			text.add('-');
		return text;
	}

	public static GText f1(GText text, double f, double ref) {
		if (f < 1.0)
			text.color(ColorImp.TMP.interpolate(GCOLOR.T().IWORST, GCOLOR.T().IBAD, f));
		else if (f > 1.0)
			text.color(ColorImp.TMP.interpolate(GCOLOR.T().IGOOD, GCOLOR.T().IGREAT, f / ref));
		else
			text.color(COLOR.WHITE85);
		text.add(f, 2);
		return text;
	}
	
	public static GText f1(GText text, double f) {
		if (f < 1.0)
			text.color(GCOLOR.T().IBAD);
		else if (f > 1.0)
			text.color(GCOLOR.T().IGOOD);
		else
			text.color(COLOR.WHITE85);
		text.add(f, 2);
		return text;
	}
	
	public static GText f1d(GText text, double f, int dec) {
		if (f < 1.0)
			text.color(GCOLOR.T().IBAD);
		else if (f > 1.0)
			text.color(GCOLOR.T().IGOOD);
		else
			text.color(COLOR.WHITE85);
		text.add(f, dec);
		return text;
	}

	public static GText fRel(GText text, double f, double ref) {
		f(text, f);
		if (f == ref)
			text.color(GCOLOR.T().NORMAL);
		else if (f > ref)
			text.color(GCOLOR.T().IGOOD);
		else
			text.color(GCOLOR.T().IBAD);
		return text;
	}
	
	public static GText f0(GText text, double f, double ref) {
		f0(text, f);
		if (ref == 0)
			ref = f;
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IWORST, GCOLOR.T().IGREAT, f / ref));
		return text;
	}

	public static GText f0Inv(GText text, double f, double ref) {
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IGREAT, GCOLOR.T().IWORST, f / ref));
		text.add(f, 2);
		return text;
	}

	public static GText f0(GText text, double f) {
		if (!Double.isFinite(f))
			f = 0;

		if (f < 0.0)
			text.color(GCOLOR.T().IWORST);
		else if (f > 0.0) {
			text.color(GCOLOR.T().IGREAT);
			text.add('+');
		} else
			text.color(COLOR.WHITE85);

		text.add(f, 2);
		return text;
	}

	/**
	 * takes a double and transforms it to an int if possible.
	 * 
	 * @param text
	 * @param f
	 * @return
	 */
	public static GText increaseAdaptive(GText text, double f) {
		if (!Double.isFinite(f))
			f = 0;

		if (f < 0.0)
			text.color(GCOLOR.T().IWORST);
		else if (f > 0.0) {
			text.color(GCOLOR.T().IGREAT);
			text.add('+');
		} else
			text.color(COLOR.WHITE85);

		if (f == (int) f) {
			text.add((int) f);
		} else if (f * 10 == (int) (f * 10)) {
			text.add(f, 1);
		} else {
			text.add(f, 2);
		}

		return text;
	}

	/**
	 * higher f - better
	 */
	public static GText perc(GText text, double f) {

		
		
		if (!Double.isFinite(f)) {
			text.add('-').add('-').add('-');
			text.color(GCOLOR.T().INACTIVE);
			return text;
		}

		int p = (int) Math.round(f*100);
		
		if (p < 0) {
			text.add('-');
			text.color(GCOLOR.T().IBAD);
			text.add(-p).add('%');
			return text;
		}

		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, f > 1 ? 1 : f));
		text.add(p).add('%');
		return text;
	}
	
	
	public static GText perc(GText text, double f, int decimals) {

		if (!Double.isFinite(f)) {
			text.add('-').add('-').add('-');
			text.color(GCOLOR.T().INACTIVE);
			return text;
		}

		if (f < 0) {
			text.add('-');
			text.color(GCOLOR.T().IBAD);
			text.add((int) (-f * 100)).add('%');
			return text;
		}
		int k = (int) (f * 1000);
		text.add((int) (k/10));
		if (k%10 > 0) {
			text.add('.').add(k%10);
		}
		text.add('%');
		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, f > 1 ? 1 : f));
		return text;
	}

	public static GText percInc(GText text, double f) {

		int t = (int) Math.round(Math.abs(f)*100*100);
		int full = t /100;
		int rem = t-full*100;
		
		if (f < 0) {
			text.color(GCOLOR.T().IBAD);
			text.add('-');
			f = -f;
			text.add(full);
		}else if (f > 0) {
			text.color(GCOLOR.T().IGOOD);
			text.add('+');
			text.add(full);
		}else {
			text.color(GCOLOR.T().INACTIVE);
			text.add('0');
		}
		//int remain = (int)(100*(f*100 - (int)(f*100)));
		if (rem > 0) {
			text.add('.').add(rem);
		}
		text.add('%');
		return text;
	}
	
	public static GText percIncInv(GText text, double f) {

		if (f < 0) {
			text.color(GCOLOR.T().IGOOD);
			text.add('-');
			text.add((int) (-f * 100)).add('%');
			return text;
		}else if (f > 0) {
			text.color(GCOLOR.T().IBAD);
			text.add('+');
			text.add((int) (f * 100)).add('%');
			return text;
		}else {
			text.color(GCOLOR.T().INACTIVE);
			text.add('0');
		}
		return text;
	}
	
	public static GText percBig(GText text, double f) {

		if (!Double.isFinite(f)) {
			text.add('-').add('-').add('-');
			text.color(GCOLOR.T().INACTIVE);
			return text;
		}

		if (f < 0) {
			text.add('-');
			text.color(GCOLOR.T().IBAD);
			text.add((int) (-f * 100)).add('%');
			return text;
		}

		f *= 100;
		int b = (int) (f);

		text.add((int) b).add('.');
		f -= (int) f;

		f *= 10000;
		b = (int) f;

		int d = 1000;
		if (b > 0) {
			while (b > 0) {
				int k = b / d;
				text.add(k);
				b -= k * d;
				d /= 10;
			}
		} else {
			text.add(0);
		}

		// for (int i = 0; i < 4; i++) {
		// f *= 10;
		// int k = (int) f;
		// text.add(k);
		// f -= k;
		// }
		text.add('%');
		return text;
	}
	

	public static GText percGood(GText text, double f) {
		if (!Double.isFinite(f)) {
			if (!Double.isFinite(f))
				text.add('-').add('-').add('-');
			else
				text.add((int) (f * 100)).add('%');
			text.color(GCOLOR.T().INACTIVE);
			return text;
		}
		text.color(GCOLOR.T().INORMAL);
		int k = (int) (f * 1000);
		text.add((int) (k/10));
		if (k%10 > 0) {
			text.add('.').add(k%10);
		}
		text.add('%');
		return text;
	}

	/**
	 * lower f - better
	 */
	public static GText percInv(GText text, double f) {
		if (f < 0) {
			text.add('-').add('-').add('-');
			text.color(GCOLOR.T().INACTIVE);
			return text;
		}

		text.color(ColorImp.TMP.interpolate(GCOLOR.T().IGOOD, GCOLOR.T().IBAD, f));
		int k = (int) (f * 1000);
		text.add((int) (k/10));
		if (k%10 > 0) {
			text.add('.').add(k%10);
		}
		text.add('%');
		return text;
	}

	private static final String sdays = " day";
	private static final String sseason = " season";
	private static final String syears = " year";

	public static void gameDays(GText text, int foodDays) {

		if (foodDays > TIME.years().bitConversion(TIME.days())) {
			double d = foodDays / TIME.years().bitConversion(TIME.days());
			text.add(d, 1).add(syears);
			if (d > 1)
				text.add('s');
		} else if (foodDays > TIME.seasons().bitConversion(TIME.days())) {
			double d = foodDays / TIME.seasons().bitConversion(TIME.days());
			text.add(d, 1).add(sseason);
			if (d > 1)
				text.add('s');
		} else {
			text.add(foodDays).add(sdays);
			if (foodDays > 1)
				text.add('s');
		}
	}

	public static void gameDaysShort(GText text, int foodDays) {

		if (foodDays > TIME.years().bitConversion(TIME.days())) {
			double d = foodDays / TIME.years().bitConversion(TIME.days());
			text.add(d, 1).add('Y');
		} else if (foodDays > TIME.seasons().bitConversion(TIME.days())) {
			double d = foodDays / TIME.seasons().bitConversion(TIME.days());
			text.add(d, 1).add('S');
		} else {

		}
	}

	public static GText bool(GText text, boolean b) {
		text.add(b);
		text.color(b ? GCOLOR.T().IGOOD : GCOLOR.T().IBAD);
		return text;

	}

}