package flounder.camera;

import flounder.framework.*;
import flounder.logger.*;
import flounder.maths.vectors.*;
import flounder.profiling.*;

/**
 * A module used for managing cameras in 2D and 3D worlds.
 */
public class FlounderCamera extends IModule {
	private static final FlounderCamera instance = new FlounderCamera();

	private ICamera camera;
	private Vector3f focusPosition;
	private Vector3f focusRotation;

	/**
	 * Creates a new camera manager.
	 */
	public FlounderCamera() {
		super(ModuleUpdate.UPDATE_POST, FlounderLogger.class, FlounderProfiler.class);
	}

	@Override
	public void init() {
		this.camera = null;
		this.focusPosition = new Vector3f();
		this.focusRotation = new Vector3f();
	}

	@Override
	public void run() {
		ICamera newCamera = (ICamera) FlounderFramework.getExtensionMatch(ICamera.class, (IExtension) camera, true);

		if (newCamera != null) {
			if (camera != null) {
				((IExtension) camera).setInitialized(false);
			}

			camera = newCamera;
		}

		if (camera != null) {
			if (!((IExtension) camera).isInitialized()) {
				camera.init();
				((IExtension) camera).setInitialized(true);
			}

			camera.update(focusPosition, focusRotation);
		}
	}

	@Override
	public void profile() {
		FlounderProfiler.add("Camera", "Selected", camera == null ? "NULL" : camera.getClass());
	}

	public static ICamera getCamera() {
		return instance.camera;
	}

	public static Vector3f getFocusPosition() {
		return instance.focusPosition;
	}

	public static void setFocusPosition(Vector3f focusPosition) {
		instance.focusPosition.set(focusPosition);
	}

	public static Vector3f getFocusRotation() {
		return instance.focusRotation;
	}

	public static void setFocusRotation(Vector3f focusRotation) {
		instance.focusRotation.set(focusRotation);
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		if (camera != null) {
			((IExtension) camera).setInitialized(false);
		}
	}
}
