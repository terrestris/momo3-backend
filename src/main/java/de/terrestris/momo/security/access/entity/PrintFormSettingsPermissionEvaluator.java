/**
 *
 */
package de.terrestris.momo.security.access.entity;

import de.terrestris.momo.model.state.PrintFormSettingsState;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class PrintFormSettingsPermissionEvaluator<E extends PrintFormSettingsState> extends MomoPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public PrintFormSettingsPermissionEvaluator() {
		this((Class<E>) PrintFormSettingsState.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected PrintFormSettingsPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E settings, Permission permission) {

		// all logged-in users are allowed to create settings
		if (permission.equals(Permission.CREATE) && (settings == null || settings.getId() == null)) {
			return true;
		}

		// always allow read on settings...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		// always allow update on settings...
		if (permission.equals(Permission.UPDATE)) {
			return true;
		}

		// always allow delete on settings...
		if (permission.equals(Permission.DELETE)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultMomoPermission(user, settings, permission);
	}

}
