/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;

//import com.google.common.base.Preconditions;
import com.thoughtworks.xstream.XStream;

/**
 * @author Gbenga
 *
 */
public final class XStreamMarshaller implements IMarshaller {

    private XStream xstream;

    public XStreamMarshaller() {
        super();

        xstream = new XStream();
        xstream.autodetectAnnotations(true);
        //xstream.processAnnotations(Foo.class);
    }

    // API

    @Override
    public final <T> String encode(final T resource) {
    	Objects.requireNonNull(resource);
        return xstream.toXML(resource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T decode(final String resourceAsString, final Class<T> clazz) {
    	Objects.requireNonNull(resourceAsString);
        return (T) xstream.fromXML(resourceAsString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> decodeList(final String resourcesAsString, final Class<T> clazz) {
        return this.decode(resourcesAsString, List.class);
    }

    @Override
    public final String getMime() {
        return MediaType.APPLICATION_XML.toString();
    }

}