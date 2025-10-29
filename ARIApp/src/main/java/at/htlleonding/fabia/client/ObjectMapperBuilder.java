package at.htlleonding.fabia.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class ObjectMapperBuilder {

    private final ObjectMapper mapper;

    public ObjectMapperBuilder() {
        this.mapper = new ObjectMapper();
    }

    public ObjectMapperBuilder registerJodaModule() {
        mapper.registerModule(new JodaModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return this;
    }

    public ObjectMapperBuilder useIndentation() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return this;
    }

    public ObjectMapperBuilder disableTimestamps() {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return this;
    }

    public ObjectMapper build() {
        return mapper;
    }
}