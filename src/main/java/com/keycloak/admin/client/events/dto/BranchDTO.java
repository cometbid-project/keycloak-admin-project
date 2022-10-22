/**
 * 
 */
package com.keycloak.admin.client.events.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Gbenga
 *
 */
@Builder
@Getter
public class BranchDTO {
	
	private String phoneNo;

	private String name;

    private String email;

    private boolean enabled;
    
	private String rcNo;	

	private String branchCode;
	
	private String category;
	
	private String subscription;

}
