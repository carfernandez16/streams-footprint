package org.streams.footprint;

import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

public class Kafka2ElasticsearchTranslator implements TransformerSupplier<String, String, KeyValue<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(Kafka2ElasticsearchTranslator.class);

    private RestClient esRestClient;

    public Kafka2ElasticsearchTranslator(RestClient esRestClient){
        this.esRestClient = esRestClient;
    }

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
                String newMessage = getFlatPayload(key, message, context);
                if(!newMessage.isEmpty()){
                    logger.info("key: " + key + ", message: " + newMessage + ", topic: " + context.topic());
                    storeToElasticsearch(newMessage);
                }
                return KeyValue.pair(key, newMessage);
            }

            @Override
            public void close() {}
        };
    }

    private void storeToElasticsearch(String payload) {
        String index = "footprint";
        try {
            Request request = new Request("POST", index + "/_doc");
            request.setEntity(new NStringEntity(payload, ContentType.APPLICATION_JSON));
            Response response = esRestClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.debug("statusCode=" + statusCode);
        } catch (IOException e) {
            logger.error("cannot save to Elasticsearch", e);
        }
    }

    private String getFlatPayload(String key, String message, ProcessorContext context){
        JSONObject payload  = new JSONObject();
        payload.put("topic", context.topic());
        payload.put("partition", context.partition());
        payload.put("offset", context.offset());
        payload.put("timestamp", context.timestamp());
        payload.put("key", key);
        payload.put("message", getJSONMessage(message));
        return payload.toString();
    }

    private JSONObject getJSONMessage(String message) {
        JSONObject msg = new JSONObject();
        try {
            msg = new JSONObject(message);
        } catch (Exception e) {
            logger.error("Cannot parse message=" + message, e);
        }
        logger.info("nsg=" + msg);
        return msg;
    }
}

