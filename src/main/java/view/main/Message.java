package view.main;

import java.io.Serializable;

import game.time.TIME;
import snake2d.util.gui.renderable.RENDEROBJ;

public abstract class Message implements Serializable{

	protected static final int WIDTH = 600;
	protected static final int HEIGHT = 600;
	private static final long serialVersionUID = 1L;
	boolean isRead = false;
	transient RENDEROBJ section;
	double currentSecond = -1;
	private final String title;
	final String key;
	
	public Message(CharSequence title) {
		if (title == null)
			throw new RuntimeException("");
		this.title = ""+title;
		
		key = "" + java.util.Arrays.hashCode(new RuntimeException().getStackTrace());
		
	}
	
	protected abstract RENDEROBJ makeSection();
	
	RENDEROBJ section() {
		return section;
	}
	
	public boolean send() {
		if (currentSecond != -1)
			throw new RuntimeException();
		currentSecond = TIME.currentSecond();
		return VIEW.messages().add(this);
	}
	
	protected final String title() {
		return title;
	}
	
	protected void close() {
		VIEW.messages().hide();
	}
	
}
