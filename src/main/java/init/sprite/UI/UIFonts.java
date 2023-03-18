package init.sprite.UI;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Font;
import util.spritecomposer.ComposerFonter;
import util.spritecomposer.ComposerThings.IFont;
import util.spritecomposer.ComposerUtil;

public final class UIFonts{
	
	public final Font H2;
	public final Font H1;
	public final Font H1S;
	public final Font S;
	public final Font M;
	public final LIST<Font> all;
	
	UIFonts() throws IOException{

		Font.setCharset(new Json(PATHS.CONFIG().get("Charset")).text("CHARS"));
		PATH g = PATHS.SPRITE().getFolder("font");
		
		KeyMap<Boolean> map = new KeyMap<>();
		
		for (String s : g.getFiles()) {
			map.put(s, true);
		}
		
		S = new IFont(g.get("Small")) {
			@Override
			protected Font init(ComposerUtil c, ComposerFonter f) {
				return f.save(0, 0);
			}
		}.get();
		
		if (map.containsKey("Header1")) {
			H1 = get(g, "Header1"); 
		}else {
			H1 = new Font(S, 2);
		}
		
		if (map.containsKey("Medium")) {
			M = get(g, "Medium"); 
		}else {
			M = new Font(H1, 0.75);
		}
		
		if (map.containsKey("Header2")) {
			H2 = get(g, "Header2"); 
		}else {
			H2 = M; 
		}
		
		H1S = new Font(H1, 0.75);
				
		all = new ArrayList<Font>(H2,H1,H1S,M,
				S);
		
		
	}
	
	private Font get(PATH g, String name) throws IOException {
		if (g.exists(name))
			return new IFont(g.get(name)) {
			@Override
			protected Font init(ComposerUtil c, ComposerFonter f) {
				
				return f.save(0, 0);
			}
		}.get();
		return M;
	}
	
}