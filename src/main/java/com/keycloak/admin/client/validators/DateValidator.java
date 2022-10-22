/**
 * 
 */
package com.keycloak.admin.client.validators;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.keycloak.admin.client.validators.qualifiers.ValidDate;

/**
 *
 * @author Gbenga 
 */
public class DateValidator implements ConstraintValidator<ValidDate, Object> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm[:ss[.SSS]]]");
  
    static TemporalAccessor parseDate(String dateAsString) {
        return FORMATTER.parseBest(dateAsString, LocalDate::from, YearMonth::from, Year::from);
    }

    @Override
    public void initialize(ValidDate constraint) {
        // nothing to do
    }

    @Override
    public boolean isValid(Object dateObj, ConstraintValidatorContext context) {
        if (dateObj == null) {
            return true;
        } else {
            String dateAsString = dateObj.toString();
            try {
                parseDate(dateAsString);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
    }
}
