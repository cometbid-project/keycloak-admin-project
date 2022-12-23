/**
 * 
 */
package com.keycloak.admin.client.common.utils;

import org.springframework.http.server.RequestPath;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;

import java.net.URI;

/**
 * @author Gbenga
 *
 */
public class CaseInsensitiveRequestPredicate implements RequestPredicate {

	private final RequestPredicate target;

	public CaseInsensitiveRequestPredicate(RequestPredicate target) {
		this.target = target;
	}

	@Override
	public boolean test(ServerRequest request) {
		return this.target.test(new LowerCaseUriServerRequestWrapper(request));
	}

	@Override
	public String toString() {
		return this.target.toString();
	}
}

class LowerCaseUriServerRequestWrapper extends ServerRequestWrapper {

	LowerCaseUriServerRequestWrapper(ServerRequest delegate) {
		super(delegate);
	}

	@Override
	public URI uri() {
		return URI.create(super.uri().toString().toLowerCase());
	}

	@Override
	public String path() {
		return uri().getRawPath();
	}

	@Override
	public RequestPath requestPath() {
		return super.requestPath();
	}
}
