/**
 *
 */
package com.keycloak.admin.client.exceptions;

/**
 * @author Gbenga
 *
 */
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.exception.DefaultExceptionContext;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author Gbenga
 *
 */
@Getter
@Setter
public class ApplicationDefinedRuntimeException extends ResponseStatusException implements ExceptionContext, ErrorCode {

	/**
	 *
	 */
	private static final long serialVersionUID = 8532405745958624402L;

	private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

	/** The context where the data is stored. */
	private final ExceptionContext exceptionContext;

	/**
	 * Instantiates ApplicationDefinedRuntimeException without message or cause.
	 * <p>
	 * The context information is stored using a default implementation.
	 * 
	 * Creates a new instance of <code>ApplicationDefinedExceptions</code> without
	 * detail message.
	 */
	public ApplicationDefinedRuntimeException() {
		super(STATUS, "Unexpected runtime error occured");
		exceptionContext = new DefaultExceptionContext();
	}

	/**
	 * Instantiates ApplicationDefinedRuntimeException without message or cause.
	 * <p>
	 * The context information is stored using a default implementation.
	 * 
	 * Creates a new instance of <code>ApplicationDefinedExceptions</code> without
	 * detail message.
	 */
	public ApplicationDefinedRuntimeException(final String message, final Throwable cause) {
		super(STATUS, message, cause);
		exceptionContext = new DefaultExceptionContext();
	}

	/**
	 * Instantiates ApplicationDefinedRuntimeException with message, but without
	 * cause.
	 * <p>
	 * The context information is stored using a default implementation.
	 *
	 * @param message the exception message, may be null
	 */
	public ApplicationDefinedRuntimeException(final HttpStatus status, final String message) {
		super(status, message);
		exceptionContext = new DefaultExceptionContext();
	}

	/**
	 * Instantiates ApplicationDefinedRuntimeException with cause, but without
	 * message.
	 * <p>
	 * The context information is stored using a default implementation.
	 *
	 * @param cause the underlying cause of the exception, may be null
	 */
	public ApplicationDefinedRuntimeException(final HttpStatus status, final Throwable cause) {
		super(status, cause.getMessage(), cause);
		exceptionContext = new DefaultExceptionContext();
	}

	/**
	 * Instantiates ApplicationDefinedRuntimeException with cause and message.
	 * <p>
	 * The context information is stored using a default implementation.
	 *
	 * @param message the exception message, may be null
	 * @param cause   the underlying cause of the exception, may be null
	 */
	public ApplicationDefinedRuntimeException(final HttpStatus status, final String message, final Throwable cause) {
		super(status, message, cause);
		exceptionContext = new DefaultExceptionContext();
	}

	/**
	 * Instantiates ApplicationDefinedRuntimeException with cause, message, and
	 * ExceptionContext.
	 *
	 * @param message the exception message, may be null
	 * @param cause   the underlying cause of the exception, may be null
	 * @param context the context used to store the additional information, null
	 *                uses default implementation
	 */
	public ApplicationDefinedRuntimeException(final HttpStatus status, final String message, final Throwable cause,
			ExceptionContext context) {
		super(status, message, cause);
		if (context == null) {
			context = new DefaultExceptionContext();
		}
		exceptionContext = context;
	}

	// -----------------------------------------------------------------------
	/**
	 * Adds information helpful to a developer in diagnosing and correcting the
	 * problem. For the information to be meaningful, the value passed should have a
	 * reasonable toString() implementation. Different values can be added with the
	 * same label multiple times.
	 * <p>
	 * Note: This exception is only serializable if the object added is
	 * serializable.
	 * </p>
	 *
	 * @param label a textual label associated with information, {@code null} not
	 *              recommended
	 * @param value information needed to understand exception, may be {@code null}
	 * @return {@code this}, for method chaining, not {@code null}
	 */
	@Override
	public ApplicationDefinedRuntimeException addContextValue(final String label, final Object value) {
		exceptionContext.addContextValue(label, value);
		return this;
	}

	/**
	 * Sets information helpful to a developer in diagnosing and correcting the
	 * problem. For the information to be meaningful, the value passed should have a
	 * reasonable toString() implementation. Any existing values with the same
	 * labels are removed before the new one is added.
	 * <p>
	 * Note: This exception is only serializable if the object added as value is
	 * serializable.
	 * </p>
	 *
	 * @param label a textual label associated with information, {@code null} not
	 *              recommended
	 * @param value information needed to understand exception, may be {@code null}
	 * @return {@code this}, for method chaining, not {@code null}
	 */
	@Override
	public ApplicationDefinedRuntimeException setContextValue(final String label, final Object value) {
		exceptionContext.setContextValue(label, value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> getContextValues(final String label) {
		return this.exceptionContext.getContextValues(label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getFirstContextValue(final String label) {
		return this.exceptionContext.getFirstContextValue(label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<String, Object>> getContextEntries() {
		return this.exceptionContext.getContextEntries();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getContextLabels() {
		return exceptionContext.getContextLabels();
	}

	/**
	 * Provides the message explaining the exception, including the contextual data.
	 *
	 * @see java.lang.Throwable#getMessage()
	 * @return the message, never null
	 */
	@Override
	public String getMessage() {
		return getFormattedExceptionMessage(super.getMessage());
	}

	/**
	 * Provides the message explaining the exception without the contextual data.
	 *
	 * @see java.lang.Throwable#getMessage()
	 * @return the message
	 * @since 3.0.1
	 */
	public String getRawMessage() {
		return super.getMessage();
	}

	/**
	 * 
	 */
	@Override
	public String getErrorCode() {
		return ErrorCode.APP_DEFINED_ERR_CODE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedExceptionMessage(final String baseMessage) {
		return exceptionContext.getFormattedExceptionMessage(baseMessage);
	}
}
