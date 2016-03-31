package flounder.textures;

import flounder.maths.*;
import flounder.processing.*;
import flounder.processing.glProcessing.*;
import flounder.resources.*;

import java.lang.ref.*;
import java.util.*;

public class TextureBuilder {
	private static Map<String, SoftReference<Texture>> loaded = new HashMap<>();

	private boolean clampEdges;
	private boolean clampToBorder;
	private boolean mipmap;
	private boolean anisotropic;
	private boolean nearest;
	private Colour borderColour;
	private MyFile file;

	protected TextureBuilder(final MyFile textureFile) {
		clampEdges = false;
		clampToBorder = false;
		mipmap = true;
		anisotropic = true;
		nearest = false;
		borderColour = new Colour(0, 0, 0, 0);
		file = textureFile;
	}

	public TextureBuilder clampEdges() {
		clampEdges = true;
		clampToBorder = false;
		return this;
	}

	public TextureBuilder clampToBorder(final Colour colour) {
		clampEdges = false;
		clampToBorder = true;
		borderColour = colour;
		return this;
	}

	public TextureBuilder nearestFiltering() {
		nearest = true;
		return noMipmap();
	}

	public TextureBuilder noMipmap() {
		mipmap = true;
		anisotropic = false;
		return this;
	}

	public TextureBuilder noFiltering() {
		anisotropic = false;
		return this;
	}

	/**
	 * Creates a new texture, carries out the CPU loading, and loads to OpenGL.
	 *
	 * @return The texture that has been created.
	 */
	public Texture create() {
		SoftReference<Texture> ref = loaded.get(file.getPath());
		Texture data = ref == null ? null : ref.get();

		if (data == null) {
			System.out.println(file.getPath() + " is being loaded into builder memory!");
			loaded.remove(file.getPath());
			data = new Texture();
			TextureLoadRequest request = new TextureLoadRequest(data, this);
			request.doResourceRequest();
			request.executeGlRequest();
			loaded.put(file.getPath(), new SoftReference<>(data));
		}

		return data;
	}

	/**
	 * Creates a new texture and sends it to be loaded by the loader thread.
	 *
	 * @return The texture.
	 */
	public Texture createInBackground() {
		SoftReference<Texture> ref = loaded.get(file.getPath());
		Texture data = ref == null ? null : ref.get();

		if (data == null) {
			loaded.remove(file.getPath());
			data = new Texture();
			RequestProcessor.sendRequest(new TextureLoadRequest(data, this));
			loaded.put(file.getPath(), new SoftReference<>(data));
		}

		return data;
	}

	/**
	 * Creates a new texture, carries out the CPU loading, and sends to the main thread for GL loading.
	 *
	 * @return The texture.
	 */
	public Texture createInSecondThread() {
		SoftReference<Texture> ref = loaded.get(file.getPath());
		Texture data = ref == null ? null : ref.get();

		if (data == null) {
			loaded.remove(file.getPath());
			data = new Texture();
			TextureLoadRequest request = new TextureLoadRequest(data, this);
			request.doResourceRequest();
			GlRequestProcessor.sendRequest(request);
			loaded.put(file.getPath(), new SoftReference<>(data));
		}

		return data;
	}

	public boolean isClampEdges() {
		return clampEdges;
	}

	public boolean isClampToBorder() {
		return clampToBorder;
	}

	public boolean isMipmap() {
		return mipmap;
	}

	public boolean isAnisotropic() {
		return anisotropic;
	}

	public boolean isNearest() {
		return nearest;
	}

	public Colour getBorderColour() {
		return borderColour;
	}

	public MyFile getFile() {
		return file;
	}
}
