package org.streams.footprint;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MessageTransformer2 implements TransformerSupplier<String, String, KeyValue<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(App2.class);

    @Override
    public Transformer<String, String, KeyValue<String, String>> get() {
        return new Transformer<String, String, KeyValue<String, String>>() {

            ProcessorContext context;

            @Override
            public void init(ProcessorContext processorContext) {
                this.context = processorContext;
            }

            @Override
            public KeyValue<String, String> transform(String key, String message) {
                String newMessage = processMessage(key, message, context);
                if(newMessage != null){
                    logger.info("key: " + key + ", newMessage: " + newMessage + ", topic: " + context.topic());
                    return KeyValue.pair(key, newMessage);
                }
                return KeyValue.pair(key, message);
            }

            @Override
            public void close() {}
        };
    }

    private String processMessage(String key, String message, ProcessorContext context) {
        JSONObject currentMessage = messageToJson(message);
        JSONObject newMessage = new JSONObject();
        if(currentMessage != null){
            newMessage = getNewMessage(currentMessage);
            return newMessage.toString();
        }
        return null;
    }

    private JSONObject messageToJson(String message){
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (Exception e) {
            logger.error("Cannot parse message=" + message, e);
        }
        logger.info("nsg=" + msg);
        return msg;
    }

    private JSONObject getNewMessage(JSONObject currentMessage) {
        JSONObject newMessage = new JSONObject();
        newMessage.put("date", new Date().toString());
        newMessage.put("temperature_avg", currentMessage.get("temperature"));
        newMessage.put("humidity_avg", currentMessage.get("humidity"));
        newMessage.put("clouds", true);
        newMessage.put("rain", false);
        return newMessage;
    }
}

