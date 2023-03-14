package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

class _FBODebug extends CORE_RESOURCE {

	private int width;
	private int height;
	private _FBOBlitter blitter;

	private final int ID;
	private int textureID;
	private final int stencilID;

	_FBODebug(SETTINGS sett) {

		blitter = new _FBOBlitter(sett);
		this.width = sett.getNativeWidth();
		this.height = sett.getNativeHeight();

		ID = glGenFramebuffers();
		stencilID = glGenRenderbuffers();
		generateTextures();
	}

	private void generateTextures() {

		GlHelper.checkErrors();
		glBindFramebuffer(GL_FRAMEBUFFER, ID);

		textureID = GlHelper.getFBTexture(width, height);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);

		glBindRenderbuffer(GL_RENDERBUFFER, stencilID);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
		;
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, stencilID);

		glDrawBuffers(GL_COLOR_ATTACHMENT0);

		if (GL_FRAMEBUFFER_COMPLETE != glCheckFramebufferStatus(GL_FRAMEBUFFER))
			throw new RuntimeException("Could not create fbo");

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		GlHelper.checkErrors();
	}

	private void deleteTextures() {
		glDeleteTextures(textureID);
		glDeleteRenderbuffers(stencilID);
	}

	@Override
	public void dis() {
		GlHelper.checkErrors();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		deleteTextures();
		glDeleteFramebuffers(ID);
		blitter.dis();
		GlHelper.checkErrors();
	}

	public void applySettings(SETTINGS sett) {
		if (this.width != sett.getNativeWidth() || this.height != sett.getNativeHeight()) {
			this.width = sett.getNativeWidth();
			this.height = sett.getNativeHeight();
			deleteTextures();
			generateTextures();
		}
	}

	public void bindAndClear() {
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, ID);
		glDrawBuffers(GL_COLOR_ATTACHMENT0);
		GlHelper.ViewPort.set(width, height);
		glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

	public void blitTexture() {

		blitter.blit(ID);

	}

}
