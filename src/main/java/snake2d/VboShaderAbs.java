package snake2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * 
 * @author mail__000
 *
 */
abstract class VboShaderAbs {

	protected int programID;
	private int vertexShaderID;
	private int fragmentShaderID;
	private int geometryShaderID = -1;

	/**
	 * Compile with generic vertex shader
	 * 
	 * @param fragment
	 * @param screenString
	 */
	protected void compile(String fragment) {

		String VERTEX = "#version 330 core" + "\n"

				+ "uniform vec2 screen;" + "\n" + "const vec2 trans = vec2(-1.0,1.0);" + "\n"

				+ "layout(location = 0) in vec2 in_position;" + "\n" + "layout(location = 1) in uvec2 in_texCoo;" + "\n"
				+ "layout(location = 2) in uvec2 in_texCoo2;" + "\n" + "layout(location = 3) in vec4 in_color;" + "\n"

				+ "out vec2 vTexCoo;" + "\n" + "out vec2 vTexCoo2;" + "\n" + "out vec4 vColor;" + "\n"

				+ "void main(){" + "\n" + "vTexCoo = in_texCoo;" + "\n" + "vTexCoo2 = in_texCoo2;" + "\n"
				+ "vColor = vec4(in_color.xyz*2.0, in_color.w);" + "\n"
				+ "gl_Position = vec4((in_position * screen)+trans, 0.0, 1.0);" + "\n" + "}";

		compile(VERTEX, fragment, null);
		
	}

	protected void compile(String vertex, String fragment) {
		compile(vertex, fragment, null);
	}
	
	protected void compile(String vertex, String fragment, String geometry) {
		programID = glCreateProgram();

		vertexShaderID = attachShader(vertex, programID, GL_VERTEX_SHADER);
		fragmentShaderID = attachShader(fragment, programID, GL_FRAGMENT_SHADER);
		if (geometry != null)
			geometryShaderID = attachShader(geometry, programID, GL_GEOMETRY_SHADER);
	
		link();
		bind();
		Printer.ln(" " + this.getClass() + ": " + programID + ", ");

	}

	private static int attachShader(String source, int programID, int type) {

		int id = glCreateShader(type);
		if (id == 0) {
			throw new RuntimeException("shader didn't compile");
		}
		glShaderSource(id, source);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Error creating shader\n"
					+ glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH)));

		glAttachShader(programID, id);
		return id;

	}

	/**
	 * Links this program in order to use.
	 */
	private void link() {

		glLinkProgram(programID);

		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Unable to link shader program:");
	}

	/**
	 * Bind this program to use.
	 */
	public void bind() {
		glUseProgram(programID);
	}

	protected void bindAttribute(int possition, String name) {
		glBindAttribLocation(programID, possition, name);
	}

	/**
	 * Unbind the shader program.
	 */
	public void unbind() {
		glUseProgram(0);
	}


	public void dis() {

		unbind();

		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);
		if (geometryShaderID != -1) {
			glDetachShader(programID, geometryShaderID);
		}

		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		if (geometryShaderID != -1) {
			glDeleteShader(geometryShaderID);
		}
		
		glDeleteProgram(programID);
	}

	/**
	 * @return The ID of this program.
	 */
	protected int getID() {
		return programID;
	}

	protected int getUniformLocation(String name) throws RuntimeException {

		int id = glGetUniformLocation(programID, name);
		if (id == -1 || glGetProgrami(programID, GL_ACTIVE_UNIFORMS) == 0)
			throw new RuntimeException("not able to find shader uniform: " + name);
		return id;
	}

	public String getScreenVec(float width, float height) {
		float w =  2f / width;
		float h = -2f / height;
		return "const vec2 screen = vec2(" + w + "," + h + ");" + "\n";
	}

	protected void setUniform2f(int loc, float a, float b) {
		glUniform2f(loc, a, b);
	}
	
	protected void setUniform(int loc, float a) {
		glUniform1f(loc, a);
	}
	
	protected void setUniform(int loc, float a, float b, float c) {
		glUniform3f(loc, a, b, c);
	}

	protected void setUniform1i(int loc, int a) {
		glUniform1i(loc, a);
	}

}
