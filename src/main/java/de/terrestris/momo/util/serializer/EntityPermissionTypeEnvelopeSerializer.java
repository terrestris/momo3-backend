package de.terrestris.momo.util.serializer;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.terrestris.momo.model.security.EntityPermissionTypeEnvelope;
import de.terrestris.shogun2.model.PersistentObject;

/**
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 05.04.2017
 *
 * Custom serializer for instances of {@link EntityPermissionTypeEnvelope}
 */
public class EntityPermissionTypeEnvelopeSerializer extends StdSerializer<EntityPermissionTypeEnvelope>{

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public EntityPermissionTypeEnvelopeSerializer(){
		this(null);
	}

	/**
	 *
	 * @param t
	 */
	public EntityPermissionTypeEnvelopeSerializer(Class<EntityPermissionTypeEnvelope> t) {
		super(t);
	}


	@Override
	public void serialize(EntityPermissionTypeEnvelope permissionTypeEnvelope, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeStartObject();

		HashMap<String, Object> targetEntityMap = new HashMap<String, Object>();
		PersistentObject targetEntity = permissionTypeEnvelope.getTargetEntity();

		targetEntityMap.put("id", targetEntity.getId());
		targetEntityMap.put("type", targetEntity.getClass().getName());

		generator.writeObjectField("targetEntity",targetEntityMap);
		generator.writeObjectField("permissions", permissionTypeEnvelope.getPermissions());

		generator.writeEndObject();
	}

}
