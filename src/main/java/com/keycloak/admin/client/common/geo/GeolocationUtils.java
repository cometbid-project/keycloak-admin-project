/**
 * 
 */
package com.keycloak.admin.client.common.geo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.models.LoginLocation;
import com.maxmind.geoip2.DatabaseReader;

import lombok.extern.log4j.Log4j2;
import ua_parser.Client;
import ua_parser.Parser;

/**
 * @author Gbenga
 *
 */
@Log4j2
public class GeolocationUtils {

	public static final String UNKNOWN_DEVICE = "UNKNOWN-DEVICE";
	public static final String UNKNOWN_COUNTRY = "UNKNOWN-COUNTRY";
	public static final String UNKNOWN_STATE = "UNKNOWN-STATE";
	public static final String UNKNOWN_CITY = "UNKNOWN-CITY";
	private static final String UNKNOWN_STATE_CODE = "UNKNOWN-STATECODE";
	private static final String UNKNOWN_COUNTRY_CODE = "UNKNOWN-COUNTRYCODE";
	private static final Double UNKNOWN_EQUIPOINT = 0.00;

	/**
	 *
	 * @param ipAddress
	 * @param userAgent
	 * @return
	 */
	public static LoginLocation getUserRelativeLocation(String ipAddress, String deviceDetails,
			DatabaseReader cityDbReader) {

		try {
			RawDBDemoGeoIPLocationService locationService = new RawDBDemoGeoIPLocationService(cityDbReader);
			GeoIP geoIP = locationService.getCityLocation(ipAddress);

			String actualIPAddr = geoIP.getIpAddress() == null ? ipAddress : geoIP.getIpAddress();

			Double longitude = geoIP.getLongitude() != null ? Double.valueOf(geoIP.getLongitude()) : UNKNOWN_EQUIPOINT;
			Double latitude = geoIP.getLatitude() != null ? Double.valueOf(geoIP.getLongitude()) : UNKNOWN_EQUIPOINT;

			if (geoIP != null) {
				return LoginLocation.builder().city(geoIP.getCity()).ipAddr(actualIPAddr).latitude(latitude)
						.longitude(longitude).countryCode(geoIP.getCountry())
						.deviceDetails(deviceDetails).loginTime(DateUtil.now()).build();
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
			log.error("Error occured loading GeoIP details for location extraction");
		}

		return LoginLocation.builder().build();
	}

	public static String getClientIP(ServerHttpRequest request) {
		final String xfHeader = request.getHeaders().getFirst("X-Forwarded-For");
		if (xfHeader == null) {
			log.info("X-Forwarded-For not found");
			return request.getRemoteAddress().getAddress().getHostAddress();
		}

		log.info("X-Forwarded-For {}", xfHeader);
		return parseXForwardedHeader(xfHeader);
	}

	private static String parseXForwardedHeader(String header) {
		return header.split(" *, *")[0];
	}

	public static String getDeviceDetails(String userAgent, Parser parser) {
		String deviceDetails = UNKNOWN_DEVICE;

		Client client = parser.parse(userAgent);
		if (Objects.nonNull(client)) {
			String agentFamily = StringUtils.isNotBlank(client.userAgent.family) ? client.userAgent.family : "";
			String agentMajor = StringUtils.isNotBlank(client.userAgent.major) ? client.userAgent.major : "";
			String agentMinor = StringUtils.isNotBlank(client.userAgent.minor) ? client.userAgent.minor : "";
			String agentOSFamily = StringUtils.isNotBlank(client.os.family) ? client.os.family : "";
			String agentOSMajor = StringUtils.isNotBlank(client.os.major) ? client.os.major : "";
			String agentOSMinor = StringUtils.isNotBlank(client.os.minor) ? client.os.minor : "";

			deviceDetails = agentFamily.trim() + " " + agentMajor.trim() + "." + agentMinor.trim() + " - "
					+ agentOSFamily.trim() + " " + agentOSMajor.trim() + "." + agentOSMinor.trim();
		}

		return deviceDetails;
	}

	public static String getIpLocation(LoginLocation loginLocHis) {

		String countryCode = Optional.ofNullable(loginLocHis.getCountryCode()).orElse(UNKNOWN_COUNTRY);
		String stateCode = Optional.ofNullable(loginLocHis.getStateCode()).orElse(UNKNOWN_STATE);
		String city = Optional.ofNullable(loginLocHis.getCity()).orElse(UNKNOWN_CITY);

		return String.format("Country: %s, State: %s, City: %s", countryCode, stateCode, city);
	}

	public static String getFullIpLocation(LoginLocation userLoc) {
		String countryCode = Optional.ofNullable(userLoc.getCountryCode()).orElse(UNKNOWN_COUNTRY);
		String stateCode = Optional.ofNullable(userLoc.getStateCode()).orElse(UNKNOWN_STATE);
		Double longitude = Optional.ofNullable(userLoc.getLongitude()).orElse(UNKNOWN_EQUIPOINT);
		Double latitude = Optional.ofNullable(userLoc.getLatitude()).orElse(UNKNOWN_EQUIPOINT);

		return String.format("State: %s, Country: %s (Longitude %5.2f, Latitude %5.2f)", stateCode, countryCode,
				longitude, latitude);
	}

	public static String getIpLocation(String countryCode, String stateCode) {

		String country = Optional.ofNullable(countryCode).orElse(UNKNOWN_COUNTRY);
		String state = Optional.ofNullable(stateCode).orElse(UNKNOWN_STATE);

		return String.format("New Location: Country: %s, State Code: %s", country, state);
	}

}
