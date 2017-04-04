package de.terrestris.momo.util.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.terrestris.shogun2.util.config.ConfigHolder;

@Component("momoConfigHolder")
public class MomoConfigHolder extends ConfigHolder {

	/**
	 * The name of the editor role. If the property configured in the
	 * {@link Value} annotation is not present, the empty string "" will be used
	 * as a fallback.
	 */
	@Value("${role.editorRoleName:}")
	private String editorRoleName;

	/**
	 * The name of the default subAdmin role. If the property configured in the
	 * {@link Value} annotation is not present, the empty string "" will be used
	 * as a fallback.
	 */
	@Value("${role.subAdminRoleName:}")
	private String subAdminRoleName;

	/**
	 * @return the editorRoleName
	 */
	public String getEditorRoleName() {
		return editorRoleName;
	}

	/**
	 * @return the subAdminRoleName
	 */
	public String getSubAdminRoleName() {
		return subAdminRoleName;
	}

}
