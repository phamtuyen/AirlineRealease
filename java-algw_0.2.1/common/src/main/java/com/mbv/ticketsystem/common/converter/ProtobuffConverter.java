package com.mbv.ticketsystem.common.converter;

import java.util.HashMap;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class ProtobuffConverter {
    private static final int BUFFER_SIZE = 2048;
    private HashMap<String, Schema<Object>> schemas = new HashMap<String, Schema<Object>>();

    @SuppressWarnings("unchecked")
    private Schema<Object> getSchema(String className) throws ClassNotFoundException {
        Schema<Object> schema = schemas.get(className);
        if (schema == null) {
            schema = (Schema<Object>) RuntimeSchema.getSchema(Class.forName(className));
            schemas.put(className, schema);
        }
        return schema;
    }

    public byte[] serialize(Object objectToConvert) throws Exception {
        byte[] bytes = null;
        Schema<Object> schema = getSchema(objectToConvert.getClass().getName());
        LinkedBuffer buffer = LinkedBuffer.allocate(BUFFER_SIZE);
        bytes = ProtobufIOUtil.toByteArray(objectToConvert, schema, buffer);
        buffer.clear();
        return bytes;
    }

	/*public Object deserialize(byte[] bytes, Class<?> clazz) throws Exception {
        Object object = null;
		Schema<Object> schema = getSchema(clazz.getName());
		object = Class.forName(clazz.getName()).newInstance();
		ProtobufIOUtil.mergeFrom(bytes, object, schema);
		return object;
	}*/

    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> valueType) throws Exception {
        Schema<Object> schema = getSchema(valueType.getName());
        Object result = Class.forName(valueType.getName()).newInstance();
        ProtobufIOUtil.mergeFrom(bytes, result, schema);
        return (T) result;
    }
}
