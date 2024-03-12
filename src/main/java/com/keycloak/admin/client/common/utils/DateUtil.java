/**
 *
 */
package com.keycloak.admin.client.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.springframework.util.Assert;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * @author Gbenga
 *
 */
public class DateUtil {

	public static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	public static final String TIME_OFDAY_FORMAT = "dd-MMM-yyyy HH:mm a";
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_OFDAY_FORMAT);
	public static final LocalDateTime FIXED_DATE = LocalDateTime.now();
	public static LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER = new LocalDateTimeSerializer(
			DateTimeFormatter.ofPattern(DATETIME_FORMAT));

	/**
	 * 
	 * @param dateStr
	 * @return
	 */
	public static LocalDateTime getDateTimeFromString(String dateStr) {

		if (StringUtils.isBlank(dateStr)) {
			return null;
		}
		try {

			return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);

		} catch (DateTimeParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param dateStr
	 * @return
	 */
	public static String getAsString(LocalDateTime localDteTime) {

		if (localDteTime == null) {
			return null;
		}

		try {

			return localDteTime.format(DateTimeFormatter.ISO_DATE_TIME);

		} catch (DateTimeParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param dateStr
	 * @return
	 */
	public static LocalDate getDateFromString(String dateStr) {

		if (StringUtils.isBlank(dateStr)) {
			return null;
		}

		try {

			LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);

			return date;
		} catch (DateTimeParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param dateStr
	 * @return
	 */
	public static String getAsString(LocalDate localDte) {

		if (localDte == null) {
			return null;
		}

		try {

			return localDte.format(DateTimeFormatter.ISO_DATE);

		} catch (DateTimeParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Date asDate(LocalDate localDate) {

		// return java.sql.Date.valueOf(localDate);

		return Date.from(localDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
	}

	public static Date asDate(LocalDateTime localDateTime) {

		//return java.sql.Date.valueOf(localDateTime.toLocalDate());
		return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
		
		//return new java.sql.Date(date.getTime()).toLocalDate();
		return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
	}

	public static LocalDateTime asLocalDateTime(Date date) {

		return java.time.Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC).toLocalDateTime();

		// return new java.sql.Date(date.getTime()).toLocalDate().atStartOfDay();
	}

	public static LocalDate convertTimeZones(LocalDate dateToConvert, ZoneId currentTimeZone,
			ZoneId destinationTimeZone) {

		ZonedDateTime currentZonedDate = dateToConvert.atStartOfDay(currentTimeZone);

		ZonedDateTime destZonedDateTime = currentZonedDate.withZoneSameInstant(destinationTimeZone);
		return destZonedDateTime.toLocalDate();
	}

	public static LocalDateTime convertTimeZones(LocalDateTime dateTimeToConvert, ZoneId currentTimeZone,
			ZoneId destinationTimeZone) {

		ZonedDateTime currentZonedDateTime = ZonedDateTime.of(dateTimeToConvert, currentTimeZone);

		ZonedDateTime destZonedDateTime = currentZonedDateTime.withZoneSameInstant(destinationTimeZone);
		return destZonedDateTime.toLocalDateTime();
	}

	public static LocalDateTime convertStringToDate(String dateToConvert, ZoneId currentTimeZone,
			ZoneId destinationTimeZone) {

		ZonedDateTime destZonedDateTime = null;

		if (dateToConvert.length() <= 10) {
			LocalDate myDateTime = getDateFromString(dateToConvert);
			ZonedDateTime currentZonedDate = myDateTime.atStartOfDay(currentTimeZone);

			destZonedDateTime = currentZonedDate.withZoneSameInstant(destinationTimeZone);
		} else {

			DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
			destZonedDateTime = ZonedDateTime.parse(dateToConvert, formatter);
		}
		return destZonedDateTime.toLocalDateTime();
	}

	public static int yearsBetween(LocalDate startDateInclusive, LocalDate endDateExclusive) {
		return Period.between(startDateInclusive, endDateExclusive).getYears();
	}

	public static int monthsBetween(LocalDate startDateInclusive, LocalDate endDateExclusive) {
		return Period.between(startDateInclusive, endDateExclusive).getMonths();
	}

	public static int daysBetween(LocalDate startDateInclusive, LocalDate endDateExclusive) {
		return Period.between(startDateInclusive, endDateExclusive).getDays();
	}

	public static LocalDate getLocalDateFromLongMillisecs(Long longValue) {

		Date date = Instant.ofEpochMilli(longValue).toDate();

		return asLocalDate(date);
	}

	public static LocalDateTime getLocalDateTimeFromLongMillisecs(Long longValue) {
		Assert.notNull(longValue, "Parameter longValue is null");
	
		DateTime dateTime = Instant.ofEpochMilli(longValue).toDateTime();

		LocalDate locaDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
		LocalTime localTime = LocalTime.of(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(),
				dateTime.getSecondOfMinute());

		return LocalDateTime.of(locaDate, localTime);
	}

	public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, ZoneId currentZone, ZoneId destZoneId) {

		ZonedDateTime zonedDateTime = localDateTime.atZone(currentZone);

		return zonedDateTime.withZoneSameInstant(destZoneId);
	}

	public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {

		LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

		return localDateTime;
	}

	public static LocalDateTime now() {
		// TODO Auto-generated method stub
		return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).withNano(0);
	}

	public static long currentTimestamp() {
		return now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
	}

	public static LocalDate today() {

		return LocalDate.now(ZoneOffset.UTC);
	}
}
