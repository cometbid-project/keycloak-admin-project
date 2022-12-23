/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import org.springframework.web.reactive.function.server.RequestPredicate;

/**
 * @author Gbenga
 *
 */
public interface RouterHelper {

    public static RequestPredicate i(RequestPredicate target) {
        return new CaseInsensitiveRequestPredicate(target);
    }
}

