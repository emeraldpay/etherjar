package org.ethereumclassic.etherjar.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.ethereumclassic.etherjar.rpc.json.RequestJson;
import org.ethereumclassic.etherjar.rpc.json.ResponseJson;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Igor Artamonov
 */
public class JacksonRpcConverter implements RpcConverter {

    private ObjectMapper objectMapper;

    public JacksonRpcConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonRpcConverter() {
        this.objectMapper = createJsonMapper();
    }

    public ObjectMapper createJsonMapper() {
        SimpleModule module = new SimpleModule("EtherJar");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    public String toJson(RequestJson request) throws JsonProcessingException {
        return objectMapper.writer().writeValueAsString(request);
    }

    public <T> T fromJson(InputStream content, Class<T> target) throws IOException {
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, target);
        ResponseJson<T> responseJson = objectMapper.readerFor(type1).readValue(content);
        return responseJson.getResult();
    }

}
