/**
 * 
 */
package com.keycloak.admin.client.common.utils;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.base.Preconditions;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
/**
 * @author Gbenga
 *
 */
@Log4j2
public final class JacksonMarshaller implements IMarshaller {
  //  private final log log = logFactory.getlog(JacksonMarshaller.class);

    private final ObjectMapper objectMapper;

    public JacksonMarshaller() {
        super();

        objectMapper = new ObjectMapper();
    }

    // API

    @Override
    public final <T> String encode(final T resource) {
    	Objects.requireNonNull(resource);
    	
        String entityAsJSON = null;
        try {
            entityAsJSON = objectMapper.writeValueAsString(resource);
        } catch (final IOException ioEx) {
            log.error("", ioEx);
        }

        return entityAsJSON;
    }

    @Override
    public final <T> T decode(final String resourceAsString, final Class<T> clazz) {
    	Objects.requireNonNull(resourceAsString);

        T entity = null;
        try {
            entity = objectMapper.readValue(resourceAsString, clazz);
        } catch (final IOException ioEx) {
            log.error("", ioEx);
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> List<T> decodeList(final String resourcesAsString, final Class<T> clazz) {
    	Objects.requireNonNull(resourcesAsString);

        List<T> entities = null;
        try {
          //  if (clazz.equals(Foo.class)) {
                entities = objectMapper.readValue(resourcesAsString, new TypeReference<List<T>>() {
                    // ...
                });
           /*
            } else {
                entities = objectMapper.readValue(resourcesAsString, List.class);
            }
            */
        } catch (final IOException ioEx) {
            log.error("", ioEx);
        }

        return entities;
    }

    @Override
    public final String getMime() {
        return MediaType.APPLICATION_JSON.toString();
    }

}
