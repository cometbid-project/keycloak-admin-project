/**
 * 
 */
package com.keycloak.admin.client.validators.qualifiers;

/**
 * @author Gbenga
 *
 */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation for the user-defined constraint annotation @VerifyValue This
 * is a general purpose validator which verifies the value for any enum If an
 * Enum object has a getValue() method it will validate based on the value of
 * the Enum else will use the EnumConstant
 *
 * @author Gbenga Adebowale
 */
@Log4j2
@Setter
public class VerifyEnumValidator implements ConstraintValidator<VerifyValue, Object> {

	Class<? extends Enum<?>> enumClass;

	/**
	 *
	 * @param enumObject
	 */
	@Override
	public void initialize(final VerifyValue enumObject) {
		enumClass = enumObject.value();
	}

	/**
	 * Checks if the value specified is valid
	 *
	 * @param myval                      The value for the object
	 * @param constraintValidatorContext
	 * @return
	 */
	@Override
	public boolean isValid(final Object myval, final ConstraintValidatorContext constraintValidatorContext) {

		log.debug("***** Verifying enum String value *****");
		
		if ((myval != null) && (enumClass != null)) {
			Enum[] enumValues = enumClass.getEnumConstants();
			Object enumValue = null;

			for (Enum enumerable : enumValues) {				
				if (myval.toString().equalsIgnoreCase(enumerable.toString())) {
					return true;
				}
				enumValue = getEnumValue(enumerable);
				if ((enumValue != null) && (myval.toString().equalsIgnoreCase(enumValue.toString()))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Invokes the getValue() method for enum if present
	 *
	 * @param enumerable The Enum object
	 * @return returns the value of enum from getValue() or enum constant
	 */
	private Object getEnumValue(Enum<?> enumerable) {
		try {
			for (Method method : enumerable.getClass().getDeclaredMethods()) {
				if (method.getName().equals("getValue")) {
					return method.invoke(enumerable);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
