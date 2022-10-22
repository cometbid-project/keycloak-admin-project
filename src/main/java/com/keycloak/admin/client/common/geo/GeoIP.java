/**
 * 
 */
package com.keycloak.admin.client.common.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Setter
@Getter
@ToString(includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class GeoIP {
    private String ipAddress;
    private String city;
    private String latitude;
    private String longitude;
    private String country;
    private String continent;
    
    // constructors, getters and setters... 
}
