package org.ethereumclassic.etherjar.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.ethereumclassic.etherjar.rpc.json.RequestJson;
import org.ethereumclassic.etherjar.rpc.json.ResponseJson;
import org.ethereumclassic.etherjar.rpc.json.TraceItemJson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    public <T> T fromJson(InputStream content, Class<T> target) throws IOException {
        if (TraceList.class.isAssignableFrom(target)) {
            return (T) fromJsonList(content, TraceItemJson.class);
        }
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, target);
        ResponseJson<T> responseJson = objectMapper.readerFor(type1).readValue(content);
        return responseJson.getResult();
    }

    public <T> List<T> fromJsonList(InputStream content, Class<T> target) throws IOException {
        JavaType type1 = objectMapper.getTypeFactory().constructParametricType(List.class, target);
        JavaType type2 = objectMapper.getTypeFactory().constructParametricType(ResponseJson.class, type1);
        ResponseJson<List<T>> responseJson = objectMapper.readerFor(type2).readValue(content);
        return responseJson.getResult();
    }

}
