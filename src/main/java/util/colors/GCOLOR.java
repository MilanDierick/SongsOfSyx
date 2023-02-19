package util.colors;

public class GCOLOR {

	private static GCOLOR_TEXT text;
	private static GCOLOR_UI ui;
	
	public static GCOLOR_TEXT T() {
		return text;
	}
	
	public static GCOLOR_UI UI() {
		return ui;
	}

	public static void read() {
		text = new GCOLOR_TEXT();
		ui = new GCOLOR_UI();
	}
	
}
