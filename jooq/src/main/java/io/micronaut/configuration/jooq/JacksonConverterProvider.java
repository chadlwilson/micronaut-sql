/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.jooq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import org.jooq.Converter;
import org.jooq.ConverterProvider;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.jooq.exception.DataTypeException;
import org.jooq.impl.DefaultConverterProvider;


/**
 * jOOQ ConverterProvider integrating Jackson ObjectMapper to convert JSON and JSONB types.
 *
 * @author Lukas Moravec
 * @since 3.2.1
 * @deprecated Use {@link JsonConverterProvider} instead
 */
@Singleton
@Requires(beans = {ObjectMapper.class})
@Bean(typed = JacksonConverterProvider.class) // don't provide ConverterProvider
@Deprecated
public class JacksonConverterProvider implements ConverterProvider {

    private final ConverterProvider delegate = new DefaultConverterProvider();
    private final ObjectMapper objectMapper;

    public JacksonConverterProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T, U> Converter<T, U> provide(Class<T> tType, Class<U> uType) {
        if (tType == JSON.class) {
            return Converter.ofNullable(tType, uType,
                    t -> {
                        try {
                            return objectMapper.readValue(((JSON) t).data(), uType);
                        } catch (Exception e) {
                            throw new DataTypeException("JSON mapping error", e);
                        }
                    },
                    u -> {
                        try {
                            return (T) JSON.valueOf(objectMapper.writeValueAsString(u));
                        } catch (Exception e) {
                            throw new DataTypeException("JSON mapping error", e);
                        }
                    }
            );
        } else if (tType == JSONB.class) {
            return Converter.ofNullable(tType, uType,
                    t -> {
                        try {
                            return objectMapper.readValue(((JSONB) t).data(), uType);
                        } catch (Exception e) {
                            throw new DataTypeException("JSON mapping error", e);
                        }
                    },
                    u -> {
                        try {
                            return (T) JSONB.valueOf(objectMapper.writeValueAsString(u));
                        } catch (Exception e) {
                            throw new DataTypeException("JSON mapping error", e);
                        }
                    }
            );
        } else {
            return delegate.provide(tType, uType);
        }
    }
}
