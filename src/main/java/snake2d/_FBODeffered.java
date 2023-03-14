package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

class _FBODeffered extends CORE_RESOURCE{

	private int width;
	private int height;
	private _FBOBlitter blitter;

	
	private final int fbID;
	private int iddiffuse;
	private int idNormal;
	private int idLight;
	private final int stencilID;
	
	private final IntBuffer diffuseNormalBuffer;
	

	_FBODeffered(SETTINGS sett){
		
		this.width = sett.getNativeWidth();
		this.height = sett.getNativeHeight();
		blitter = new _FBOBlitter(sett);	
		diffuseNormalBuffer = BufferUtils.createIntBuffer(2);
		for (int i = 0; i < 2; i++)
			diffuseNormalBuffer.put(GL_COLOR_ATTACHMENT1+i);
		diffuseNormalBuffer.flip();
		
		fbID = glGenFramebuffers();
		stencilID = glGenRenderbuffers();
		generateTextures();
	}
	
	void applySettings(SETTINGS sett){
		
		if (this.width != sett.getNativeWidth() || this.height != sett.getNativeHeight()){
			this.width = sett.getNativeWidth();
			this.height = sett.getNativeHeight();
			deleteTextures();
			generateTextures();
		}
	}
	

	
	private void generateTextures(){
		
		GlHelper.checkErrors();
		glBindFramebuffer(GL_FRAMEBUFFER, fbID);
		
		iddiffuse = GlHelper.getFBTexture(width, height);
		idNormal = GlHelper.getFBTexture(width, height);
		idLight = GlHelper.getFBTexture(width, height);
		
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, iddiffuse, 0);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, idNormal, 0);
		glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, idLight, 0);
		
		glBindRenderbuffer(GL_RENDERBUFFER, stencilID);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8 , width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, stencilID);
		
		glDrawBuffers(diffuseNormalBuffer);
		GlHelper.checkErrors();
		
		if (GL_FRAMEBUFFER_COMPLETE != glCheckFramebufferStatus(GL_FRAMEBUFFER))
			throw new RuntimeException("Could not create fbo");
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, iddiffuse);
		
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, idNormal);
		
        GlHelper.checkErrors();
	}
	
	private void deleteTextures(){
		glDeleteTextures(iddiffuse);
		glDeleteTextures(idNormal);
		glDeleteTextures(idLight);
	}
	
	@Override
	public void dis() {
		GlHelper.checkErrors();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);    
		deleteTextures();
		glDeleteRenderbuffers(stencilID);
		glDeleteFramebuffers(fbID);
		blitter.dis();
		GlHelper.checkErrors();
	}
	
	public void bindDiffAndNorForTarget(){
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbID);
		glDrawBuffers(diffuseNormalBuffer);
		GlHelper.ViewPort.set(width, height);
		glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void bindLightTextureForTarget(){
		glDrawBuffers(GL_COLOR_ATTACHMENT0);
		GlHelper.ViewPort.set(width, height);
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	public void blitTexture(){
		blitter.blit(fbID);
	}
	
}
