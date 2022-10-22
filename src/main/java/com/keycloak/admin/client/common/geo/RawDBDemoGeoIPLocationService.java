/**
 * 
 */
package com.keycloak.admin.client.common.geo;

import java.io.IOException;
import java.net.InetAddress;

import org.springframework.util.ResourceUtils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *   
 */  
@Log4j2
public class RawDBDemoGeoIPLocationService {

	private final DatabaseReader cityDbReader;
	// private final DatabaseReader countryDbReader;

	@Getter
	private final GeoIP geoIP;

	public RawDBDemoGeoIPLocationService() throws IOException {
		// geoIP = new GeoIP();

		// File database = ResourceUtils
		// .getFile("classpath:maxmind/GeoLite2-City.mmdb");
		this(new DatabaseReader.Builder(ResourceUtils.getFile("classpath:maxmind/GeoLite2-City.mmdb")).build());
	}

	public RawDBDemoGeoIPLocationService(DatabaseReader cityDbReader) throws IOException {

		this(cityDbReader, new GeoIP());
	}

	public RawDBDemoGeoIPLocationService(DatabaseReader cityDbReader, GeoIP geoIP) throws IOException {
		this.geoIP = geoIP;

		this.cityDbReader = cityDbReader;
	}

	public GeoIP getCityLocation(String ip) {
		try {
			InetAddress ipAddress = InetAddress.getByName(ip);
			CityResponse response = cityDbReader.city(ipAddress);

			String cityName = response.getCity().getName();
			String latitude = response.getLocation().getLatitude().toString();
			String longitude = response.getLocation().getLongitude().toString();
			String country = response.getCountry().getName();
			String continent = response.getContinent().getName();

			geoIP.setIpAddress(ip);
			geoIP.setCity(cityName);
			geoIP.setLatitude(latitude);
			geoIP.setLongitude(longitude);
			geoIP.setCountry(country);
			geoIP.setContinent(continent);

		} catch (IOException | GeoIp2Exception ex) {
			log.info("City Database file for GeoIP failed to load...", ex);
		}
		return geoIP;
	}

}
