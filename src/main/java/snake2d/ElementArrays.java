package snake2d;

import static org.lwjgl.opengl.GL15.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

abstract class ElementArrays {

	abstract void bind();
	abstract void dis();
	
	private static ElementArrays quad;
	private static ElementArrays point;

	static void quadBind() {
		if (quad != null) {
			quad.bind();
			return;
		}
		
		int[] indices = new int[(1<<16)*6];
		int tmp = 0;
		for (int i = 0; i < indices.length; i+=6){
        	indices[i] = tmp++;    //0
        	indices[i+1] = tmp++;  //1	
        	indices[i+2] = tmp--;  //1
        	indices[i+3] = tmp++;  //2
        	indices[i+4] = tmp++;  //3
        	indices[i+5] = tmp++;  //4
        }
		
		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
		indicesBuffer.put(indices).flip();
		GlHelper.checkErrors();
        final int vertexFixID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexFixID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_READ);
        MemoryUtil.memFree(indicesBuffer);
        
        quad =  new ElementArrays() {
			
			@Override
			void dis() {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	            glDeleteBuffers(vertexFixID);
				
			}
			
			@Override
			void bind() {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexFixID);
				
			}
		};
		quad.bind();
		GlHelper.checkErrors();
	}
	
	static void pointBind() {
		
		if (point != null) {
			point.bind();
			return;
		}
		
		int[] indices = new int[(1<<16)];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i;
		}

		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
		indicesBuffer.put(indices).flip();
		GlHelper.checkErrors();
		final int vertexFixID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexFixID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_READ);
		MemoryUtil.memFree(indicesBuffer);
		
		point =  new ElementArrays() {
			
			@Override
			void dis() {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	            glDeleteBuffers(vertexFixID);
				
			}
			
			@Override
			void bind() {
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexFixID);
				
				
			}
		};
		point.bind();
		GlHelper.checkErrors();
	}
	
	static void dispose() {
		GlHelper.checkErrors();
		if (quad != null) {
			quad.dis();
			GlHelper.checkErrors();
		}
		if (point != null) {
			point.dis();
			GlHelper.checkErrors();
		}
		quad = null;
		point = null;
	}
	
}
