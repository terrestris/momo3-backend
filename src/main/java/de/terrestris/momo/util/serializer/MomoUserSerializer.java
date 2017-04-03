package de.terrestris.momo.util.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.security.UserGroupRole;
import de.terrestris.momo.service.UserGroupRoleService;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @author Daniel Koch
 * @date 31.03.2017
 *
 * Custom serializer for instances of {@link MomoUser}
 */
public class MomoUserSerializer extends StdSerializer<MomoUser>{

	private static final long serialVersionUID = 1L;

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 */
	public MomoUserSerializer(){
		this(null);
	}

	public MomoUserSerializer(Class<MomoUser> t) {
		super(t);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}


	@Override
	public void serialize(MomoUser momoUser, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();
		generator.writeNumberField("id", momoUser.getId());
		generator.writeStringField("firstName", momoUser.getFirstName());
		generator.writeStringField("lastName", momoUser.getLastName());
		generator.writeStringField("email", momoUser.getEmail());
		generator.writeObjectField("birthday", momoUser.getBirthday());
		generator.writeObjectField("language", momoUser.getLanguage());
		generator.writeStringField("accountName", momoUser.getAccountName());
		generator.writeBooleanField("active", momoUser.isActive());

		/**
		 * serializes role / groups in the following way [{{ROLENAME}}_GROUP_{{GROUP_ID}}], eg.
		 * ["ROLE_SUBADMIN_GROUP_13", "ROLE_EDITOR_GROUP_14", "ROLE_USER_GROUP_15"]
		 */
		List<UserGroupRole> listRolesPerGroup = this.userGroupRoleService.findUserGroupRolesBy(momoUser);
		ArrayList<String> groupRoleNames = new ArrayList<>(listRolesPerGroup.size());
		for (UserGroupRole ugr : listRolesPerGroup) {
			String roleName = ugr.getRole().getName();
			Integer groupId = ugr.getGroup().getId();
			String[] parts = {roleName, "GROUP", Integer.toString(groupId)};

			groupRoleNames.add(StringUtils.join(parts, '_'));
		}

		generator.writeObjectField("groupRoles", groupRoleNames);
		generator.writeEndObject();
	}

}
