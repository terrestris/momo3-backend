package de.terrestris.momo.util.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.momo.dao.UserGroupRoleDao;
import de.terrestris.momo.model.MomoUser;
import de.terrestris.momo.model.security.UserGroupRole;

/**
 *
 * terrestris GmbH & Co. KG
 * @author ahenn
 * @date 31.03.2017
 *
 * TODO documentation
 */
public class MomoUserSerializer extends StdSerializer<MomoUser>{

	private static final long serialVersionUID = 1L;

	@Autowired
	@Qualifier("userGroupRoleDao")
	private UserGroupRoleDao<UserGroupRole> userGroupRoleDao;

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

		List<UserGroupRole> listRolesPerGroup = this.userGroupRoleDao.findUserRoles(momoUser);
		ArrayList<String> groupRoleNames = new ArrayList<>(listRolesPerGroup.size());
		for (UserGroupRole ugr : listRolesPerGroup) {
			String roleName = ugr.getRole().getName();
			String groupName = ugr.getGroup().getName();

			groupRoleNames.add(roleName+"_"+groupName);
		}

		generator.writeObjectField("groupRoles", groupRoleNames);
		generator.writeEndObject();
	}

}
