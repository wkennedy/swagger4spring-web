package com.knappsack.swagger4springweb.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableValues;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ScalaObjectMapper extends ObjectMapper {

    public ScalaObjectMapper() {
        registerModule(new DefaultScalaModule());
    }

    class AllowableValuesDeserializer extends StdScalarDeserializer<AllowableValues> {

        protected AllowableValuesDeserializer() {
            super(AllowableValues.class);
        }

        @Override
        public AllowableValues deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

            String currentName = jp.getCurrentName();
            if (currentName.equalsIgnoreCase("AllowableValues")) {
                if (jp.getText().isEmpty() || jp.getText().equals("{}") || jp.getText().equals("{")) {
                    return new AllowableListValues(JavaToScalaUtil.toScalaList(new ArrayList<String>()), "");
                }
            }
            return new AllowableListValues(JavaToScalaUtil.toScalaList(new ArrayList<String>()), "");
        }
    }

}
