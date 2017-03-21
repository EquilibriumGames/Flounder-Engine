package flounder.fonts;

import flounder.camera.*;
import flounder.devices.*;
import flounder.guis.*;
import flounder.helpers.*;
import flounder.maths.*;
import flounder.maths.vectors.*;
import flounder.profiling.*;
import flounder.renderer.*;
import flounder.resources.*;
import flounder.shaders.*;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * A renderer capable of rendering fonts.
 */
public class FontRenderer extends Renderer {
	private static final MyFile VERTEX_SHADER = new MyFile(FlounderShaders.SHADERS_LOC, "fonts", "fontVertex.glsl");
	private static final MyFile FRAGMENT_SHADER = new MyFile(FlounderShaders.SHADERS_LOC, "fonts", "fontFragment.glsl");

	private ShaderObject shader;

	/**
	 * Creates a new font renderer.
	 */
	public FontRenderer() {
		this.shader = ShaderFactory.newBuilder().setName("fonts").addType(new ShaderType(GL_VERTEX_SHADER, VERTEX_SHADER)).addType(new ShaderType(GL_FRAGMENT_SHADER, FRAGMENT_SHADER)).create();
	}

	@Override
	public void renderObjects(Vector4f clipPlane, Camera camera) {
		if (!shader.isLoaded() || FlounderGuis.getContainer() == null) {
			return;
		}

		prepareRendering();
		FlounderGuis.getContainer().getAll(new ArrayList<>()).forEach(this::renderText);
		endRendering();
	}

	private void prepareRendering() {
		shader.start();

		OpenGlUtils.antialias(FlounderDisplay.isAntialiasing());
		OpenGlUtils.enableAlphaBlending();
		OpenGlUtils.disableDepthTesting();
		OpenGlUtils.cullBackFaces(true);

		shader.getUniformBool("polygonMode").loadBoolean(OpenGlUtils.isInWireframe());
	}

	private void renderText(ScreenObject object) {
		if (!(object instanceof TextObject)) {
			return;
		}

		TextObject text = (TextObject) object;

		if (!text.isLoaded() || !text.isVisible()) {
			return;
		}

		OpenGlUtils.bindVAO(text.getMesh(), 0, 1);
		OpenGlUtils.bindTexture(text.getFont().getTexture(), 0);
		// shader.getUniformVec2("size").loadVec2((POSITION_MAX - POSITION_MIN) / 2.0f, (POSITION_MAX - POSITION_MIN) / 2.0f);
		shader.getUniformVec4("transform").loadVec4(
				text.getScreenPosition().x, text.getScreenPosition().y,
				text.getScreenDimensions().x, text.getScreenDimensions().y
		);
		shader.getUniformFloat("rotation").loadFloat((float) Math.toRadians(text.getRotation()));

		shader.getUniformVec4("colour").loadVec4(text.getColour().r, text.getColour().g, text.getColour().b, text.getAlpha());
		shader.getUniformVec3("borderColour").loadVec3(text.getBorderColour());
		shader.getUniformVec2("edgeData").loadVec2(text.calculateEdgeStart(), text.calculateAntialiasSize());
		shader.getUniformVec2("borderSizes").loadVec2(text.getTotalBorderSize(), text.getGlowSize());
		glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());
		OpenGlUtils.unbindVAO(0, 1);
	}

	private void endRendering() {
		shader.stop();
	}

	@Override
	public void profile() {
		FlounderProfiler.add(FlounderFonts.PROFILE_TAB_NAME, "Render Time", super.getRenderTime());
	}

	@Override
	public void dispose() {
		shader.delete();
	}
}
