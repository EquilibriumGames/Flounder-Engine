package flounder.materials;

import flounder.framework.*;
import flounder.logger.*;
import flounder.maths.*;
import flounder.resources.*;

import java.io.*;
import java.lang.ref.*;
import java.util.*;

/**
 * A module used for loading materials for meshes.
 */
public class FlounderMaterials extends IModule {
	private static final FlounderMaterials instance = new FlounderMaterials();

	private Map<String, SoftReference<List<Material>>> loaded;

	/**
	 * Creates a new material loader class.
	 */
	public FlounderMaterials() {
		super(ModuleUpdate.UPDATE_PRE, FlounderLogger.class);
	}

	@Override
	public void init() {
		this.loaded = new HashMap<>();
	}

	@Override
	public void update() {
	}

	@Override
	public void profile() {
	}

	/**
	 * Loads a MTL file into a list of Material objects.
	 *
	 * @param file The file to be loaded.
	 *
	 * @return Returns a loaded list of MTLMaterials.
	 */
	public static List<Material> loadMTL(MyFile file) {
		SoftReference<List<Material>> ref = instance.loaded.get(file.getPath());
		List<Material> data = ref == null ? null : ref.get();

		if (data == null) {
			BufferedReader reader = null;

			try {
				reader = file.getReader();
			} catch (Exception e) {
				e.printStackTrace();
			}

			data = new ArrayList<>();

			String line;
			String parseMaterialName = "";
			Material parseMaterial = new Material();

			if (reader == null) {
				FlounderLogger.error("Error creating reader the MTL: " + file);
				return new ArrayList<>();
			}

			try {
				while ((line = reader.readLine()) != null) {
					String prefix = line.split(" ")[0];

					if (line.startsWith("#")) {
						continue;
					}

					switch (prefix) {
						case "newmtl":
							if (!parseMaterialName.equals("")) {
								data.add(parseMaterial);
							}

							parseMaterialName = line.split(" ")[1];
							parseMaterial = new Material();
							parseMaterial.name = parseMaterialName;
							break;
						case "Ns":
							parseMaterial.specularCoefficient = Float.valueOf(line.split(" ")[1]);
							break;
						case "Ka":
							String[] rgbKa = line.split(" ");
							parseMaterial.ambientColour = new Colour(Float.valueOf(rgbKa[1]), Float.valueOf(rgbKa[2]), Float.valueOf(rgbKa[3]));
							break;
						case "Kd":
							String[] rgbKd = line.split(" ");
							parseMaterial.diffuseColour = new Colour(Float.valueOf(rgbKd[1]), Float.valueOf(rgbKd[2]), Float.valueOf(rgbKd[3]));
							break;
						case "Ks":
							String[] rgbKs = line.split(" ");
							parseMaterial.specularColour = new Colour(Float.valueOf(rgbKs[1]), Float.valueOf(rgbKs[2]), Float.valueOf(rgbKs[3]));
							break;
						case "map_Kd":
							String fileNameKd = line.split(" ")[1].trim();
							// FlounderLogger.error("File Kd: " + fileNameKd);

							//	if (!fileNameKd.isEmpty() && !fileNameKd.equals(".")) {
							//		parseMaterial.texture = Texture.newTexture(new MyFile(MyFile.RES_FOLDER, "entities", file.getName().replace(".mtl", ""), fileNameKd)).create();
							//	}
							break;
						case "map_Bump":
							String fileNameBump = line.split(" ")[1].trim();
							// FlounderLogger.error("File Bump: " + fileNameBump);

							//	if (!fileNameBump.isEmpty() && !fileNameBump.equals(".")) {
							//		parseMaterial.normalMap = Texture.newTexture(new MyFile(MyFile.RES_FOLDER, "entities", file.getName().replace(".mtl", ""), fileNameBump)).create();
							//	}
							break;
						default:
							FlounderLogger.warning("[MTL " + file.getName() + "] Unknown Line: " + line);
							break;
					}
				}

				reader.close();
				data.add(parseMaterial);
			} catch (IOException | NullPointerException e) {
				FlounderLogger.error("Error reading the MTL: " + file);
				FlounderLogger.exception(e);
			}

			instance.loaded.put(file.getPath(), new SoftReference<>(data));
		}

		return data;
	}

	/**
	 * Gets a list of loaded models.
	 *
	 * @return A list of loaded models.
	 */
	public static Map<String, SoftReference<List<Material>>> getLoaded() {
		return instance.loaded;
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		//	loaded.keySet().forEach(key -> loaded.get(key).get().delete());
		loaded.clear();
	}
}
