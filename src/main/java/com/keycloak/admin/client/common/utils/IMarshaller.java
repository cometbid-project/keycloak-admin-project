/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import java.util.List;

/**
 * @author Gbenga
 *
 */
public interface IMarshaller {

    <T> String encode(final T entity);

    <T> T decode(final String entityAsString, final Class<T> clazz);

    <T> List<T> decodeList(final String entitiesAsString, final Class<T> clazz);

    String getMime();

}