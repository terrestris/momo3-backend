package de.terrestris.momo.util.serializer;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.momo.model.security.EntityPermissionEnvelope;
import de.terrestris.shogun2.model.PersistentObject;

/**
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 05.04.2017
 *
 * Custom serializer for instances of {@link EntityPermissionEnvelope}
 */
public class EntityPermissionEnvelopeSerializer extends StdSerializer<EntityPermissionEnvelope>{

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public EntityPermissionEnvelopeSerializer(){
		this(null);
	}

	/**
	 *
	 * @param t
	 */
	public EntityPermissionEnvelopeSerializer(Class<EntityPermissionEnvelope> t) {
		super(t);
	}


	@Override
	public void serialize(EntityPermissionEnvelope permissionEnvelope, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();

		HashMap<String, Object> targetEntityMap = new HashMap<String, Object>();
		PersistentObject targetEntity = permissionEnvelope.getTargetEntity();

		targetEntityMap.put("id", targetEntity.getId());
		targetEntityMap.put("type", targetEntity.getClass().getName());

		generator.writeObjectField("targetEntity",targetEntityMap);
		generator.writeStringField("displayTitle", permissionEnvelope.getDisplayTitle());
		generator.writeObjectField("permissions", permissionEnvelope.getPermissions());

		generator.writeEndObject();
	}

}
