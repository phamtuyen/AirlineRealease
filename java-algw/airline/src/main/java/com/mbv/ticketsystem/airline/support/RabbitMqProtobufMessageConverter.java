package com.mbv.ticketsystem.airline.support;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.mbv.ticketsystem.common.converter.ProtobuffConverter;

public class RabbitMqProtobufMessageConverter extends AbstractMessageConverter {

    private ProtobuffConverter converter;

    public RabbitMqProtobufMessageConverter() {
        converter = new ProtobuffConverter();
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] bytes = null;
        try {
            bytes = converter.serialize(object);
        } catch (Exception e) {
            throw new MessageConversionException("Failed to convert Message content", e);
        }
        messageProperties.setContentType("appication/protobuf");
        if (bytes != null) {
            messageProperties.setContentLength(bytes.length);
        }
        messageProperties.getHeaders().put("__TypeId__", object.getClass().getName());
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.contains("protobuf")) {
                try {
                    String classId = (String) message.getMessageProperties().getHeaders().get("__TypeId__");
                    if (classId == null) throw new Exception("no classId found");
                    Class<?> targetClass = Class.forName(classId);
                    content = converter.deserialize(message.getBody(), targetClass);
                } catch (Exception e) {
                    throw new MessageConversionException("Failed to convert json-based Message content", e);
                }
            }
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }

}
