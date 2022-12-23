/**
 * 
 */
package com.cometbid.api.handlers.test;

import java.security.InvalidParameterException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.cometbid.api.common.enums.PhoneNoType;
import com.cometbid.api.common.enums.ProfileStatus;
import com.cometbid.api.common.enums.SubscriptionType;
import com.cometbid.api.common.enums.VerificationStatus;
import com.cometbid.api.common.models.AccountTypeIdAggregator;
import com.cometbid.api.common.models.AddressAggregator;
import com.cometbid.api.common.models.BranchRegModel;
import com.cometbid.api.common.models.ChangeBranchEmailModel;
import com.cometbid.api.common.models.EditEmailModel;
import com.cometbid.api.common.models.ContactModel;
import com.cometbid.api.common.models.UserCriteriaModel;
import com.cometbid.api.common.models.MerchantAccountTypeIdModel;
import com.cometbid.api.common.models.MerchantAddressIdAggregator;
import com.cometbid.api.common.models.MerchantAppSettingsAggregator;
import com.cometbid.api.common.models.MerchantCriteriaModel;
import com.cometbid.api.common.models.MerchantModel;
import com.cometbid.api.common.models.MerchantPhoneIdAggregator;
import com.cometbid.api.common.models.MerchantRegModel;
import com.cometbid.api.common.models.Model;
import com.cometbid.api.common.models.PhoneNoAggregator;
import com.cometbid.api.common.models.MerchantAppSettingsAggregator.SettingsLabel;
import com.cometbid.api.core.auth.audit.Audit;
import com.cometbid.api.core.auth.audit.AuditVO;
import com.cometbid.api.core.auth.audit.Username;
import com.cometbid.api.core.merchant.models.EditNameModel;
import com.cometbid.api.core.merchant.models.MerchantOptionsModel;
import com.cometbid.api.core.merchant.models.MutableAppSettings;
import com.cometbid.api.core.merchant.vo.MerchantAddressVO;
import com.cometbid.api.core.merchant.vo.MerchantLogoModel;
import com.cometbid.api.core.merchant.vo.MerchantLogoVO;
import com.cometbid.api.core.merchant.vo.MerchantPhoneVO;
import com.cometbid.api.core.merchant.vo.MerchantProfileResponse;
import com.cometbid.api.core.user.Address;
import com.cometbid.api.core.user.PhoneNo;
import com.cometbid.api.core.user.models.UserRegModel;
import com.cometbid.api.core.user.vo.MemberVO;
import com.cometbid.api.core.user.vo.UserPhonesVO;
import com.cometbid.api.handlers.data.MerchantBuilder;
import com.cometbid.api.merchant.handler.ChangeAddressRestHandler;
import com.cometbid.api.merchant.handler.ChangeAppSettingsHandler;
import com.cometbid.api.merchant.handler.ChangeBranchEmailRestHandler;
import com.cometbid.api.merchant.handler.ChangeMerchantAcctTypesHandler;
import com.cometbid.api.merchant.handler.ChangePhoneRestHandler;
import com.cometbid.api.merchant.handler.CreateBranchRestHandler;
import com.cometbid.api.merchant.handler.CreateMerchantRestHandler;
import com.cometbid.api.merchant.handler.ReadOnlyMerchantRestHandler;
import com.cometbid.api.merchant.handler.UpdateBranchLogoRestHandler;
import com.cometbid.api.merchant.handler.UpdateBranchNameRestHandler;
import com.cometbid.api.merchant.handler.UpdateMerchantPropRestHandler;
import com.cometbid.api.merchant.rest.router.MerchantRouter;
import com.cometbid.api.merchant.security.filters.MerchantAuthFilter;
import com.cometbid.merchant.base.config.AppConfig;
import com.cometbid.merchant.base.config.UtilProfile;
import com.cometbid.merchant.base.entities.Merchant;
import com.cometbid.merchant.base.services.MerchantService;
import com.cometbid.project.common.exceptions.handler.GlobalControllerExceptionHandler;
import com.cometbid.project.common.utils.DateUtil;

import lombok.extern.log4j.Log4j2;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WithMockUser
@WebFluxTest
@DisplayName("Merchant service endpoints API")
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@Import({ UtilProfile.class, 
	AppConfig.class, 
	MerchantService.class,
	MerchantRouter.class,
	ChangeAddressRestHandler.class, 
	ChangeAppSettingsHandler.class,
	ChangeMerchantAcctTypesHandler.class,
	ChangePhoneRestHandler.class,  
	CreateBranchRestHandler.class,
	CreateMerchantRestHandler.class,
	UpdateBranchLogoRestHandler.class,
	ReadOnlyMerchantRestHandler.class,
	ChangeBranchEmailRestHandler.class,
	MerchantAuthFilter.class,
	UpdateMerchantPropRestHandler.class,
	UpdateBranchNameRestHandler.class,
	GlobalControllerExceptionHandler.class})
@ContextConfiguration(classes = { MerchantRouter.class, AppConfig.class })
public class MerchantBaseProfileEndpointsTest {

	@Autowired
	protected WebTestClient webTestClient;

	@Autowired
	private ApplicationContext applicationContext;

	@MockBean
	protected MerchantService profileService;

	protected String PATH = "/v1/merchants";

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient()
				.baseUrl("http://localhost:9091")
				.filter(
					documentationConfiguration(restDocumentation)
						.operationPreprocessors()
						.withRequestDefaults(prettyPrint())
						.withResponseDefaults(prettyPrint()))
				.build();					
	}

	@BeforeAll
	protected static void initializeData() {

		// BlockHound.install(new ReactorBlockHoundIntegration(), new MyIntegration());
		// ===========================================================================================

	}

	protected MultiValueMap<String, String> buildParameters(Map<String, String> params) {

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.setAll(params);

		return parameters;
	}
	
	
	private MerchantModel createMerchantModel(Merchant merchant) {
		
		ContactModel contacts = MerchantBuilder.createContactModel(merchant);
		
		Address address = contacts.getAddresses().get(0);
		address.setAddressId(null);
		
		PhoneNo phoneNo = contacts.getPhoneList().get(0);
		phoneNo.setPhoneId(null);
		
		contacts.setAddresses(Arrays.asList(address));
		contacts.setPhoneList(Arrays.asList(phoneNo));
		
		MerchantOptionsModel optionsModel = new MerchantOptionsModel();
		optionsModel.setCountry(merchant.getMutableSettings().getCountry());
		optionsModel.setCurrencyCode(merchant.getMutableSettings().getCurrencyCode());
		optionsModel.setSubscription(merchant.getSubscription());
		optionsModel.setDuesSettings(merchant.getMutableSettings().getDuesSettings());
		
		return MerchantModel.builder()
				.email(merchant.getEmail())
				.name(merchant.getName())
				.abbrName(merchant.getAbbrName())  
				.mediaChannel(merchant.getChannel())
				.contact(contacts)
				.category(merchant.getCategory())  
				.optionsModel(optionsModel)
				.build();
	}
	
	private BranchRegModel createBranchRegModel(Merchant merchant) {
		
		ContactModel contacts = MerchantBuilder.createContactModel(merchant);
		
		Address address = contacts.getAddresses().get(0);
		address.setAddressId(null);
		
		PhoneNo phoneNo = contacts.getPhoneList().get(0);
		phoneNo.setPhoneId(null);
		
		contacts.setAddresses(Arrays.asList(address));
		contacts.setPhoneList(Arrays.asList(phoneNo));
		contacts.setWebsiteUrl(merchant.getWebsiteUrl());
		
		return BranchRegModel.builder()
				.email(merchant.getEmail())
				.name(merchant.getName())
				.abbrName(merchant.getAbbrName())
				.contact(contacts)
				.category(merchant.getCategory())
				.build();
	}
	
	private EditNameModel createEditBranchModel(Merchant merchant) {
		
		return EditNameModel.builder()
				.branchCode(merchant.getBranchCode())
				.rcNo(merchant.getRcNo())
				.name(merchant.getName())
				.websiteUrl(merchant.getWebsiteUrl())
				.abbrName(merchant.getAbbrName())
				.mediaChannel(merchant.getChannel()) 
				.category(merchant.getCategory())
				.build();
	}
	
	protected MerchantProfileResponse createMerchantVO(Merchant merchant) {
		
		ContactModel contacts = MerchantBuilder.createContactModel(merchant);
		contacts.setWebsiteUrl(merchant.getWebsiteUrl());
		
		Audit audit = merchant.getAudit();
		String creationTime = DateUtil.toZonedDateTime(audit.getCreationDate(), ZoneOffset.UTC).toString();
		String lastModifiedTime = DateUtil.toZonedDateTime(audit.getLastModifiedDate(), ZoneOffset.UTC).toString();
		
		AuditVO auditVo = AuditVO.builder()
				.creationDate(creationTime)
				.lastModifiedDate(lastModifiedTime)
				.creator(audit.getCreatedBy())
				.modifier(audit.getLastModifiedBy())
				.build();
		
		return MerchantProfileResponse.builder()
				.branchCode(merchant.getBranchCode())
				.rcNo(merchant.getRcNo())
				.email(merchant.getEmail())
				.name(merchant.getName())
				.abbrName(merchant.getAbbrName())
				.mediaChannel(merchant.getChannel()) 
				.contact(contacts)
				.category(merchant.getCategory())
				.subscription(merchant.getSubscription())
				.enabled(merchant.isEnabled())
				.audit(auditVo)
				.build();
	}

	/**  
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to change merchant's address, replace all or add to list")
	@Test
	public void changeMerchantAddress() {
		log.info("running  " + this.getClass().getName() + ".changeMerchantAddress()");
		
		// MemberCriteriaModel criteria = new MemberCriteriaModel("1", "rcn019090");
		Mono<String> profiles = Mono.just("Success");

		Mockito.when(this.profileService
					.addAddress(any(MerchantAddressVO.class),
							any(Boolean.class)))
					.thenReturn(profiles);	

		String endPointURI = "/{rc_no}/{branch_code}/addresses";

		final Map<String, String> params = new HashMap<>();
		params.put("add", "true");

		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		
		boolean includeAddrId = false;
		
		AddressAggregator merchantAddress = new AddressAggregator();
		merchantAddress.addAddress(MerchantBuilder.merchant().getAddress(includeAddrId));
		merchantAddress.addAddress(MerchantBuilder.merchant().getAddress(includeAddrId));
		
		Mono<AddressAggregator> addressVO = Mono.just(merchantAddress);

		this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(addressVO, AddressAggregator.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()				
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success")
				.consumeWith(
			            document(
			                    "add-address-to-merchant", 			                   
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

		// ======================================================================================

		params.clear();
		params.put("add", "false");

		this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(addressVO, AddressAggregator.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success").consumeWith(
			            document(
			                    "set-address-to-merchant", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to change branch's primary Address")
	@Test
	public void changeBranchPrimaryAddress() {
		log.info("running  " + this.getClass().getName() + ".changeBranchPrimaryAddress()");
		
		// MemberCriteriaModel criteria = new MemberCriteriaModel("1", "rcn019090");
		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.changePrimaryAddress(any(MerchantCriteriaModel.class), any(String.class)))
				.thenReturn(profiles);

		String endPointURI = "/{rc_no}/{branch_code}/pry_addr";

		final Map<String, String> params = new HashMap<>();
		params.put("addr_id", UUID.randomUUID().toString());
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		
		this.webTestClient
				.mutateWith(csrf())
				.patch()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()				
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success")
				.consumeWith(
			            document(
			                    "change-primary-address-of-branch", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to change branch's email")
	@Test
	public void changeBranchEmail() {
		log.info("running  " + this.getClass().getName() + ".changeBranchEmail()");
		
		// MemberCriteriaModel criteria = new MemberCriteriaModel("1", "rcn019090");
		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.changeBranchEmail(any(ChangeBranchEmailModel.class)))
				.thenReturn(profiles);

		String endPointURI = "/email/{rc_no}";
		
		MerchantBuilder merchantBuilder = MerchantBuilder.merchant();				
		MerchantCriteriaModel merchantCriteria = merchantBuilder.createMerchant();
		
		ChangeBranchEmailModel emailModel = new ChangeBranchEmailModel();
		emailModel.setMerchantCriteria(merchantCriteria);
		emailModel.setNewEmail(merchantBuilder.getEmail());
		emailModel.setOldEmail(merchantBuilder.getEmail()); 
		
		Mono<ChangeBranchEmailModel> producer = Mono.just(emailModel);

		final Map<String, String> params = new HashMap<>();		

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		
		this.webTestClient
				.mutateWith(csrf())
				.patch()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(producer, ChangeBranchEmailModel.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()				
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success")
				.consumeWith(
			            document(
			                    "change-email-of-branch", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

	}
	

	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to change merchant's phone, replace all or add to list")
	@Test
	public void changeMerchantPhone() {
		log.info("running  " + this.getClass().getName() + ".changeMerchantPhone()");
		
		// MemberCriteriaModel criteria = new MemberCriteriaModel("1", "rcn019090");
		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.addPhoneNo(any(MerchantPhoneVO.class),
							any(Boolean.class)))
				.thenReturn(profiles);

		String endPointURI = "/{rc_no}/{branch_code}/phones";

		final Map<String, String> params = new HashMap<>();
		params.put("add", "true");
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		
		boolean includePhoneId = false;
		
		PhoneNoAggregator merchantPhone = new PhoneNoAggregator();				
		merchantPhone.addPhoneNo(MerchantBuilder.merchant().createPhoneNo(PhoneNoType.HOME, includePhoneId));
		merchantPhone.addPhoneNo(MerchantBuilder.merchant().createPhoneNo(PhoneNoType.CELL, includePhoneId));
		
		Mono<PhoneNoAggregator> phoneVO = Mono.just(merchantPhone);

		this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(phoneVO, UserPhonesVO.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()				
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success")
				.consumeWith(
			            document(
			                    "add-phone-to-merchant", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

		// =======================================================================================

		params.clear();
		params.put("add", "false");

		this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(phoneVO, UserPhonesVO.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success").consumeWith(
			            document(
			                    "set-phone-to-merchant", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to change branch's primary phone")
	@Test
	public void changeBranchPrimaryPhone() {
		log.info("running  " + this.getClass().getName() + ".changeBranchPrimaryPhone()");
		
		// MemberCriteriaModel criteria = new MemberCriteriaModel("1", "rcn019090");
		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.changePrimaryPhone(any(MerchantCriteriaModel.class), any(String.class)))
				.thenReturn(profiles);

		String endPointURI = "/{rc_no}/{branch_code}/pry_phone";

		final Map<String, String> params = new HashMap<>();
		params.put("phone_id", UUID.randomUUID().toString());
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		
		this.webTestClient
				.mutateWith(csrf())
				.patch()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody()				
				.jsonPath("$.reason")
				.isEqualTo("OK")
				.jsonPath("$.message")
				.isEqualTo("Success")
				.consumeWith(
			            document(
			                    "change-primary-phone-of-branch", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

	}

	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to create merchant")
	@Test
	public void createMerchant() {  
		log.info("running  " + this.getClass().getName() + ".createMerchant()");
		
		String endPointURI = "/registration";
		
		boolean includeAddrId = true;
		boolean includePhoneId = true;
		
		MerchantCriteriaModel criteriaModel = MerchantBuilder.merchant().createMerchant();
		Merchant merchant = MerchantBuilder.merchant().createActualMerchant(criteriaModel.getRcNo(), null, includeAddrId, includePhoneId);		 
		
		MerchantModel merchantModel = createMerchantModel(merchant);
		log.info("Request Model {}", merchantModel);
		
		//MerchantRegModel regModel = createMerchantRegModel(merchantModel, userModel);			
		MerchantProfileResponse merchantVo = createMerchantVO(merchant);	
		merchantVo.setContact(null);
		
		Mono<MerchantProfileResponse> profiles = Mono.just(merchantVo);
		log.info("Expected Response {}", merchantVo);
		
		Mockito.when(this.profileService
					.registerMerchant(any(MerchantModel.class)))
				.thenReturn(profiles);

		Map<String, Object> templateVar = new HashMap<>();
		
		restPostCallResponseOne(merchantVo, merchantModel, endPointURI, templateVar)
				.consumeWith(
			            document(
			                    "create-merchant-profile", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint()),
			                    relaxedRequestFields( 
			                    		fieldWithPath("category")
			                			.description("The category of Business Merchant's tends towards in catering to it's Members. "
			                					+ "These are the possible values: Cooperative, Thrift and Credit, Microfinance, Financial Institutions, Govermental Agencies, Non-Govermental Agencies,"
			                					+ " Developmental Agencies, Trade Groups, Professional Institutions, ARTISANS,Educational Institutions, Personal, Family Bond, OTHERS"), 
			                			fieldWithPath("media_channel")
			                			.description("The media platform through which intended merchant first heard about us. "
			                					+ "These are the possible values: EMAIL, AGENT, REGISTERED MERCHANT, SOCIAL MEDIA, ADVERTS, RECOMMENDATIONS, OTHERS"), 
			                			fieldWithPath("options.merchant_dues.dues_rate")
			                			.description("Specified in cases when merchant have dues payment process in-place for it's members. The rate depends on the `dues_type`"),
			                			fieldWithPath("options.merchant_dues.dues_type")
			                			.description("Type of dues. These are the possible values: FIXED, PERCENTAGE, DEFAULT_RATE"),
			                			fieldWithPath("options.merchant_dues.charges_interval")
			                			.description("The recurrent interval at which dues are deducted. "
			                					+ "These are the possible values: Daily, Twice_Weekly, Weekly, Bi_Weekly, Twice_Monthly, Monthly, Bi_Monthly, Quarterly, Yearly"),
			                			fieldWithPath("options.currency_code")
			                			.description("The currently supported currencies on the platform. "
			                					+ "These are the possible values: CAD, CNY, INR, USD, NGN, GHS"))
			                    ));

		// ============================================================================================
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to create branch")
	@Test
	public void createBranch() {  
		log.info("running  " + this.getClass().getName() + ".createBranch()");
		
		String endPointURI = "/{rc_no}/branch/registration";
		
		boolean includeAddrId = true;
		boolean includePhoneId = true;
		
		MerchantCriteriaModel criteriaModel = MerchantBuilder.merchant().createMerchant();
		Merchant merchant = MerchantBuilder.merchant().createActualMerchant(criteriaModel.getRcNo(), null, includeAddrId, includePhoneId);
		 
		BranchRegModel regModel = createBranchRegModel(merchant);	
		
		MerchantProfileResponse merchantVo = createMerchantVO(merchant);	
		merchantVo.setContact(null);
		
		Mono<MerchantProfileResponse> profiles = Mono.just(merchantVo);
		
		log.info("Request Model {}", regModel);
		log.info("Expected Response {}", merchantVo);

		Mockito.when(this.profileService
					.registerBranch(any(String.class), 
							any(BranchRegModel.class)))
				.thenReturn(profiles);
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", criteriaModel.getRcNo());
		
		restPostCallResponseOne(merchantVo, regModel, endPointURI, templateVar)
				.consumeWith(
			            document(
			                    "create-branch-profile", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));

		// =====================================================================================
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update branch profile")
	@Test
	public void updateBranchProfile() {
		log.info("running  " + this.getClass().getName() + ".updateBranchProfile()");
		
		String endPointURI = "/{rc_no}";
		
		boolean includeAddrId = true;
		boolean includePhoneId = true;
		
		MerchantCriteriaModel criteriaModel = MerchantBuilder.merchant().createMerchant();
		Merchant merchant = MerchantBuilder.merchant().createActualMerchant(criteriaModel.getRcNo(), 
				null, includeAddrId, includePhoneId);
		 		
		EditNameModel branchModel = createEditBranchModel(merchant);	
		
		MerchantProfileResponse merchantVo = createMerchantVO(merchant);	
		Mono<MerchantProfileResponse> profiles = Mono.just(merchantVo);

		Mockito.when(this.profileService
					.editBranchDetails(any(EditNameModel.class)))
				.thenReturn(profiles);
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", criteriaModel.getRcNo());
	
		this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(branchModel), EditNameModel.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.rc_no")
				.isEqualTo(merchantVo.getRcNo())
				.jsonPath("$.branch_code")
				.isEqualTo(merchantVo.getBranchCode())
				.jsonPath("$.email")
				.isEqualTo(merchantVo.getEmail())
				.jsonPath("$.name")
				.isEqualTo(merchantVo.getName())
				.jsonPath("$.abbr_name")
				.isEqualTo(merchantVo.getAbbrName())
				.jsonPath("$.subscription")
				.isEqualTo(merchantVo.getSubscription())
				.jsonPath("$.media_channel")
				.isEqualTo(merchantVo.getMediaChannel())
				.jsonPath("$.category")
				.isEqualTo(merchantVo.getCategory())
				.jsonPath("$.enabled")
				.isBoolean().jsonPath("$.enabled")
				.isEqualTo(merchantVo.isEnabled())
				.jsonPath("$.contact.website_url")
				.isEqualTo(merchantVo.getContact().getWebsiteUrl())
				.consumeWith(
			            document(
			                    "update-branch-profile", 
			                    preprocessRequest(prettyPrint()), 
			                    preprocessResponse(prettyPrint())));
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant property settings")
	@Test
	public void updateMerchantPropertySettings() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantPropertySettings()");
		
		String endPointURI = "/{rc_no}/{branch_code}/settings";
		
		boolean includeAddrId = true;
		boolean includePhoneId = true;
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();
		Merchant merchant = MerchantBuilder.merchant().createActualMerchant(merchantCriteria.getRcNo(), null, includeAddrId, includePhoneId);
		 		
		Mono<String> profiles = Mono.just("Success");
		
		MutableAppSettings appSettings = merchant.getMutableSettings();
		
		MerchantAppSettingsAggregator properties = new MerchantAppSettingsAggregator();	
		properties.setMerchantCriteria(merchantCriteria); 
		properties.addSettings(SettingsLabel.MUTABLE_SETTINGS_LABEL, appSettings);

		Mockito.when(this.profileService
					.changeMutableProperties(any(MerchantAppSettingsAggregator.class)))
				.thenReturn(profiles);
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
					.build(templateVar))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(appSettings), MutableAppSettings.class)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		            		"update-merchant-settings",  
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));

		// =================================================================================
	}
	  
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant account types")
	@Test
	public void updateMerchantAcctTypes() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantAcctTypes()");
		
		String endPointURI = "/{rc_no}/{branch_code}/account_types";
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();
		 		
		Mono<String> profiles = Mono.just("Success");
				
		AccountTypeIdAggregator properties = new AccountTypeIdAggregator();	
		properties.addAccountTypeId(UUID.randomUUID().toString());
		properties.addAccountTypeId(UUID.randomUUID().toString());
		properties.addAccountTypeId(UUID.randomUUID().toString());
		properties.addAccountTypeId(UUID.randomUUID().toString());
		properties.addAccountTypeId(UUID.randomUUID().toString());
		
		MerchantAccountTypeIdModel accountTypes = new MerchantAccountTypeIdModel();
		accountTypes.setMerchantCriteria(merchantCriteria);
		accountTypes.setTypeIdAggregator(properties);

		Mockito.when(this.profileService
					.changeAccountTypes(any(MerchantAccountTypeIdModel.class)))
				.thenReturn(profiles);
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
					.build(templateVar))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just(properties), AccountTypeIdAggregator.class)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		            		"update-merchant-account-types",  
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));

		// =================================================================================
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant logo")
	@Test
	public void updateMerchantLogo() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantLogo()");

		String endPointURI = "/{rc_no}/{branch_code}/logo";
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();
		 		
		Mono<String> profiles = Mono.just("Success");
		
		Mockito.when(this.profileService
				.replaceImage(any(MerchantCriteriaModel.class), 
						any(MerchantLogoModel.class)))
			.thenReturn(profiles);

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
				
		MerchantLogoModel merchantPhoto = MerchantLogoModel.builder()
										.logoImage(MerchantBuilder.merchant().createAvatar()) 
										.cascadeToAll(true)
										.build();
		
		log.info("Merchant Logo model {}", merchantPhoto);
		
		Mono<MerchantLogoModel> imageVO = Mono.just(merchantPhoto);
				
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			//.queryParams(buildParameters(params))
			.build(templateVar))
			.contentType(MediaType.APPLICATION_JSON)
			.body(imageVO, MerchantLogoModel.class)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		                    "update-merchant-logo", 
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));

		// ========================================================================
	}
	
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to delete Merchant address by id")
	@Test
	public void deleteMerchantAddressById() {
		log.info("running  " + this.getClass().getName() + ".deleteMerchantAddressById()");

		final String endPointURI = "/{rc_no}/{branch_code}/addresses";

		// Mono<String> profiles = Mono.just("Success");
		Mono<Long> profiles = Mono.just(6L);		
		Mockito.when(this.profileService
					.removeAddress(any(MerchantAddressIdAggregator.class)))
				.thenReturn(profiles);
		
		final Map<String, String> params = new HashMap<>();
		params.put("addr_ids", "121,424,354,352,2546,532");
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
								
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.consumeWith(
		            document(
		                    "delete-merchant-address-by-id-success", 
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));
		
		// =================================================================================
		
		profiles = Mono.just(3L);		
		Mockito.when(this.profileService
					.removeAddress(any(MerchantAddressIdAggregator.class)))
				.thenReturn(profiles);
								
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("Partial Content")
			.consumeWith(
		            document(
		               "delete-merchant-address-by-id-partial", 
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));
		
		// =================================================================================
		
		profiles = Mono.just(0L);		
		Mockito.when(this.profileService
					.removeAddress(any(MerchantAddressIdAggregator.class)))
				.thenReturn(profiles);
										
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("Not Modified")
			.consumeWith(
			     document(
			        "delete-merchant-address-by-id-failed", 
				       preprocessRequest(prettyPrint()), 
				       preprocessResponse(prettyPrint())));
	}

	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to delete Merchant phones by id")
	@Test
	public void deleteMerchantPhoneById() {
		log.info("running  " + this.getClass().getName() + ".deleteMerchantPhoneById()");

		final String endPointURI = "/{rc_no}/{branch_code}/phones";

		// Mono<String> profiles = Mono.just("Success");
		Mono<Long> profiles = Mono.just(6L);		
		Mockito.when(this.profileService
				.removePhoneNo(any(MerchantPhoneIdAggregator.class)))
			.thenReturn(profiles);
		
		final Map<String, String> params = new HashMap<>();
		params.put("phone_ids", "121,424,354,352,2546,532");
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
								
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.consumeWith(
		            document(
		                    "delete-merchant-phone-by-id-success", 
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));
		
		// ========================================================================================
		
		profiles = Mono.just(3L);		
		Mockito.when(this.profileService
				.removePhoneNo(any(MerchantPhoneIdAggregator.class)))
			.thenReturn(profiles);
								
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("Partial Content")
			.consumeWith(
		            document(
		                    "delete-merchant-phone-by-id-partial", 
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));
		
		// ===============================================================================================
		
		profiles = Mono.just(0L);		
		Mockito.when(this.profileService
					.removePhoneNo(any(MerchantPhoneIdAggregator.class)))
				.thenReturn(profiles);
										
		this.webTestClient
			.mutateWith(csrf())
			.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("Not Modified")
			.consumeWith(
			       document(
				      "delete-merchant-phone-by-id-failed", 
				          preprocessRequest(prettyPrint()), 
				          preprocessResponse(prettyPrint())));
	}
	
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant profile status")
	@Test
	public void updateMerchantProfileStatus() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantProfileStatus()");

		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.changeProfileStatus(any(MerchantCriteriaModel.class), any(ProfileStatus.class)))
				.thenReturn(profiles);

		String endPointURI = "/{rc_no}/{branch_code}/status";
		
		final Map<String, String> params = new HashMap<>();
		params.put("activate", "true");
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
								
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		                "activate-merchant-profile", 
		                preprocessRequest(prettyPrint()), 
		                preprocessResponse(prettyPrint())));

		// ====================================================================================================

		params.clear();
		params.put("activate", "false");

		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success").consumeWith(
			      document(
			           "deactivate-merchant-profile", 
					        preprocessRequest(prettyPrint()), 
					        preprocessResponse(prettyPrint())));
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant verification status")
	@Test
	public void updateMerchantVerificationStatus() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantVerificationStatus()");

		Mono<String> profiles = Mono.just("Success");
		Mockito.when(this.profileService
					.changeVerificationStatus(any(MerchantCriteriaModel.class), any(VerificationStatus.class)))
				.thenReturn(profiles);

		String endPointURI = "/{rc_no}/{branch_code}/verification";
		
		final Map<String, String> params = new HashMap<>();
		params.put("verified", "true");
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();

		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		templateVar.put("rc_no", merchantCriteria.getRcNo());
								
		this.webTestClient
			.mutateWith(csrf())
			.patch()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		                "set-verified-branch-profile", 
		                preprocessRequest(prettyPrint()), 
		                preprocessResponse(prettyPrint())));

		// ====================================================================================================

		params.clear();
		params.put("verified", "false");

		this.webTestClient
			.mutateWith(csrf())
			.patch()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success").consumeWith(
			      document(
			           "set-unverified-branch-profile", 
					        preprocessRequest(prettyPrint()), 
					        preprocessResponse(prettyPrint())));
	}
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant subscription")
	@Test
	public void updateMerchantSubscription() {
		log.info("running  " + this.getClass().getName() + ".updateMerchantSubscription()");
		
		String endPointURI = "/{rc_no}/subscription";
		
		boolean includeAddrId = true;
		boolean includePhoneId = true;
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();
		Merchant merchant = MerchantBuilder.merchant().createActualMerchant(merchantCriteria.getRcNo(), null, includeAddrId, includePhoneId);
		 		
		Mono<String> profiles = Mono.just("Success");

		Mockito.when(this.profileService
					.changeSubscription(any(String.class), any(SubscriptionType.class)))
				.thenReturn(profiles);
		
		final Map<String, String> params = new HashMap<>();
		params.put("subscription", merchant.getSubscription());
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		            		"update-merchant-subscription",  
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));

		// ======================================================================
	}
	
	
	/**
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to update merchant parent branch")
	@Test
	public void updateParentBranch() {
		log.info("running  " + this.getClass().getName() + ".updateParentBranch()");
		
		String endPointURI = "/{rc_no}/{branch_code}/parent_branch";
		
		MerchantCriteriaModel merchantCriteria = MerchantBuilder.merchant().createMerchant();
		//Merchant merchant = MerchantBuilder.merchant().createActualMerchant(merchantCriteria.getRcNo(), 
			//	MerchantBuilder.merchant().createActualMerchant(merchantCriteria.getRcNo(), null));  
		 		
		Mono<String> profiles = Mono.just("Success");

		Mockito.when(this.profileService
					.changeParentMerchant(any(MerchantCriteriaModel.class), any(String.class)))
				.thenReturn(profiles);
		
		final Map<String, String> params = new HashMap<>();
		params.put("parent_branch", MerchantBuilder.merchant().generateBranchCode());
		
		Map<String, Object> templateVar = new HashMap<>();
		templateVar.put("rc_no", merchantCriteria.getRcNo());
		templateVar.put("branch_code", merchantCriteria.getBranchCode());
		
		this.webTestClient
			.mutateWith(csrf())
			.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.jsonPath("$.reason")
			.isEqualTo("OK")
			.jsonPath("$.message")
			.isEqualTo("Success")
			.consumeWith(
		            document(
		            		"update-parent-branch",  
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint())));

		// =================================================================================
	}	
	
	
	protected BodyContentSpec restPostCallResponseOne(MerchantProfileResponse merchantVo1, Model regModel, String endPointURI,
			Map<String, Object> templateVar) {
		
		ResponseSpec respSpec = null;
		if(regModel instanceof BranchRegModel) {			
			respSpec = this.webTestClient
					.mutateWith(csrf())
					.post()
					.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.build(templateVar))
					.contentType(MediaType.APPLICATION_JSON)
					.body(Mono.just(regModel), BranchRegModel.class)
					.exchange();			
		} else if(regModel instanceof MerchantModel) {
			
			respSpec = this.webTestClient
					.mutateWith(csrf())
					.post()
					.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
							.build(templateVar))
					.contentType(MediaType.APPLICATION_JSON)
					.body(Mono.just(regModel), MerchantRegModel.class)
					.exchange();
		} else {
			throw new InvalidParameterException();
		}
		
		return respSpec		
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.rc_no")
				.isEqualTo(merchantVo1.getRcNo())
				.jsonPath("$.branch_code")
				.isEqualTo(merchantVo1.getBranchCode())
				.jsonPath("$.email")
				.isEqualTo(merchantVo1.getEmail())
				.jsonPath("$.name")
				.isEqualTo(merchantVo1.getName())
				.jsonPath("$.abbr_name")
				.isEqualTo(merchantVo1.getAbbrName())
				.jsonPath("$.subscription")
				.isEqualTo(merchantVo1.getSubscription())
				.jsonPath("$.media_channel")
				.isEqualTo(merchantVo1.getMediaChannel())
				.jsonPath("$.category")
				.isEqualTo(merchantVo1.getCategory());
				//.jsonPath("$.enabled")
				//.isBoolean().jsonPath("$.enabled")
				//.isEqualTo(merchantVo1.isEnabled());
							
	}
	
	protected BodyContentSpec restPutCallResponseOne(MerchantProfileResponse merchantVo1, MerchantProfileResponse regModel, String endPointURI,
			Map<String, Object> templateVar) {

		return this.webTestClient
				.mutateWith(csrf())
				.put()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.build(templateVar))
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(regModel), MerchantRegModel.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.rc_no")
				.isEqualTo(merchantVo1.getRcNo())
				.jsonPath("$.branch_code")
				.isEqualTo(merchantVo1.getBranchCode())
				.jsonPath("$.email")
				.isEqualTo(merchantVo1.getEmail())
				.jsonPath("$.name")
				.isEqualTo(merchantVo1.getName())
				.jsonPath("$.abbr_name")
				.isEqualTo(merchantVo1.getAbbrName())
				.jsonPath("$.subscription")
				.isEqualTo(merchantVo1.getSubscription())
				.jsonPath("$.media_channel")
				.isEqualTo(merchantVo1.getMediaChannel())
				.jsonPath("$.category")
				.isEqualTo(merchantVo1.getCategory())
				//.jsonPath("$.enabled")
				//.isBoolean().jsonPath("$.enabled").isEqualTo(merchantVo1.isEnabled())
				.jsonPath("$.contact.website_url")
				.isEqualTo(merchantVo1.getContact().getWebsiteUrl());				
	}


	protected BodyContentSpec restCallPhotoResponseOne(MerchantLogoVO imageVO, String endPointURI, Map<String, Object> templateVar,
			Map<String, String> params) {

		return this.webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
				.accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.branch.branch_code")
				.isEqualTo(imageVO.getMerchantCriteria().getBranchCode())
				.jsonPath("$.branch.rc_no")
				.isEqualTo(imageVO.getMerchantCriteria().getRcNo())
				.jsonPath("$.image")
				.exists()
				.jsonPath("$.image.img_name")
				.isEqualTo(imageVO.getImageModel().getImgName())
				.jsonPath("$.image.img_height")
				.isEqualTo(imageVO.getImageModel().getPhotoHeight())
				.jsonPath("$.image.img_width")
				.isEqualTo(imageVO.getImageModel().getPhotoWidth())
				.jsonPath("$.image.mime_type")
				.isEqualTo(imageVO.getImageModel().getContentType())
				.jsonPath("$.image.img_size")
				.isEqualTo(imageVO.getImageModel()
						.getDocSize());
	}

	protected BodyContentSpec restCallPhoneResponseMultiple(MerchantPhoneVO phoneNoVo, String endPointURI,
			Map<String, Object> templateVar, Map<String, String> params, MediaType mediaType) {

		return this.webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
				.accept(mediaType).exchange()
				.expectStatus().isOk()
				.expectHeader()
				.contentType(mediaType).expectBody()
				.jsonPath("$.merchant.branch_code")
				.isEqualTo(phoneNoVo.getMerchantCriteria().getBranchCode())
				.jsonPath("$.merchant.rc_no")
				.isEqualTo(phoneNoVo.getMerchantCriteria().getRcNo())
				.jsonPath("$.contact_phone.record_count")
				.isEqualTo(phoneNoVo.getContactPhoneNos().getCount())
				.jsonPath("$.contact_phone.record_count")
				.isEqualTo(phoneNoVo.getContactPhoneNos().getCount())
				.jsonPath("$.contact_phone.phone_nos[0].phone_id")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[0].getPhoneId())
				.jsonPath("$.contact_phone.phone_nos[0].phone_no")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[0].getPhoneNo())
				.jsonPath("$.contact_phone.phone_nos[0].phone_type")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[0].getPhoneType())
				.jsonPath("$.contact_phone.phone_nos[0].dial_code")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[0].getDialCode())
				.jsonPath("$.contact_phone.phone_nos[1].phone_id")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[1].getPhoneId())
				.jsonPath("$.contact_phone.phone_nos[1].phone_no")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[1].getPhoneNo())
				.jsonPath("$.contact_phone.phone_nos[1].phone_type")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[1].getPhoneType())
				.jsonPath("$.contact_phone.phone_nos[1].dial_code")
				.isEqualTo(phoneNoVo.getContactPhoneNos()
						.getPhoneNos().toArray(new PhoneNo[2])[1].getDialCode());
	}

	protected BodyContentSpec restCallAddressResponseMultiple(MerchantAddressVO addressVo, String endPointURI,
			Map<String, Object> templateVar, Map<String, String> params, MediaType mediaType) {

		return this.webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI).queryParams(buildParameters(params))
						.build(templateVar))
				.accept(mediaType).exchange()
				.expectStatus().isOk()
				.expectHeader()
				.contentType(mediaType).expectBody()
				.jsonPath("$.merchant.branch_code")
				.isEqualTo(addressVo.getMerchantCriteria().getBranchCode())
				.jsonPath("$.merchant.rc_no")
				.isEqualTo(addressVo.getMerchantCriteria().getRcNo())
				.jsonPath("$.contact_address.record_count")
				.isEqualTo(addressVo.getContactAddr().getCount())
				.jsonPath("$.contact_address.addresses[0].addr_id")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getAddressId())
				.jsonPath("$.contact_address.addresses[0].city")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getCity())
				.jsonPath("$.contact_address.addresses[0].street1")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getStreetAddr1())
				.jsonPath("$.contact_address.addresses[0].street2")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getStreetAddr2())
				.jsonPath("$.contact_address.addresses[0].postal_code")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getPostalCode())
				.jsonPath("$.contact_address.addresses[0].district")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getDistrict())
				.jsonPath("$.contact_address.addresses[0].country")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[0].getCountry())
				.jsonPath("$.contact_address.addresses[1].addr_id")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getAddressId())
				.jsonPath("$.contact_address.addresses[1].city")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getCity())
				.jsonPath("$.contact_address.addresses[1].street1")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getStreetAddr1())
				.jsonPath("$.contact_address.addresses[1].street2")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getStreetAddr2())
				.jsonPath("$.contact_address.addresses[1].postal_code")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getPostalCode())
				.jsonPath("$.contact_address.addresses[1].district")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getDistrict())
				.jsonPath("$.contact_address.addresses[1].country")
				.isEqualTo(addressVo.getContactAddr().getAddresses().toArray(new Address[2])[1].getCountry());
	}

	protected BodyContentSpec restCallResponseMultiple(MerchantProfileResponse merchantVo1, String endPointURI, Map<String, Object> templateVar,
			Map<String, String> params, MediaType mediaType) {

		return this.webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
				.accept(mediaType).exchange()
				.expectStatus().isOk()
				.expectHeader()
				.contentType(mediaType)
				.expectBody()
				.jsonPath("$.branches[0].rc_no")
				.isEqualTo(merchantVo1.getRcNo())
				.jsonPath("$.branches[0].branch_code")
				.isEqualTo(merchantVo1.getBranchCode())
				.jsonPath("$.branches[0].email")
				.isEqualTo(merchantVo1.getEmail())
				.jsonPath("$.branches[0].name")
				.isEqualTo(merchantVo1.getName())
				.jsonPath("$.branches[0].abbr_name")
				.isEqualTo(merchantVo1.getAbbrName())
				.jsonPath("$.branches[0].subscription")
				.isEqualTo(merchantVo1.getSubscription())
				.jsonPath("$.branches[0].media_channel")
				.isEqualTo(merchantVo1.getMediaChannel())
				.jsonPath("$.branches[0].category")
				.isEqualTo(merchantVo1.getCategory())
				//.jsonPath("$.branches[0].enabled")
				//.isBoolean()
				.jsonPath("$.branches[0].enabled")
				.isEqualTo(merchantVo1.isEnabled())
				.jsonPath("$.branches[0].contact.website_url")
				.isEqualTo(merchantVo1.getContact().getWebsiteUrl());
		
	}

	protected BodyContentSpec restCallResponseOne(MerchantProfileResponse merchantVo1, String endPointURI, Map<String, Object> templateVar,
			Map<String, String> params, MediaType mediaType) {

		return this.webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
						.queryParams(buildParameters(params))
						.build(templateVar))
				.accept(mediaType).exchange()
				.expectStatus().isOk().expectHeader()
				.contentType(mediaType)
				.expectBody()
				.jsonPath("$.rc_no")
				.isEqualTo(merchantVo1.getRcNo())
				.jsonPath("$.branch_code")
				.isEqualTo(merchantVo1.getBranchCode())
				.jsonPath("$.email")
				.isEqualTo(merchantVo1.getEmail())
				.jsonPath("$.name")
				.isEqualTo(merchantVo1.getName())
				.jsonPath("$.abbr_name")
				.isEqualTo(merchantVo1.getAbbrName())
				.jsonPath("$.subscription")
				.isEqualTo(merchantVo1.getSubscription())
				.jsonPath("$.media_channel")
				.isEqualTo(merchantVo1.getMediaChannel())
				.jsonPath("$.category")
				.isEqualTo(merchantVo1.getCategory())
				.jsonPath("$.enabled")
				.isBoolean().jsonPath("$.enabled")
				.isEqualTo(merchantVo1.isEnabled())
				.jsonPath("$.contact.website_url")
				.isEqualTo(merchantVo1.getContact().getWebsiteUrl())
				.jsonPath("$.contact.addresses[0].addr_id")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getAddressId())
				.jsonPath("$.contact.addresses[0].city")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getCity())
				.jsonPath("$.contact.addresses[0].street1")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getStreetAddr1())
				.jsonPath("$.contact.addresses[0].street2")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getStreetAddr2())
				.jsonPath("$.contact.addresses[0].postal_code")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getPostalCode())
				.jsonPath("$.contact.addresses[0].district")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getDistrict())
				.jsonPath("$.contact.addresses[0].country")
				.isEqualTo(merchantVo1.getContact().getAddresses().toArray(new Address[2])[0].getCountry())
				.jsonPath("$.contact.phone_nos[0].phone_id")
				.isEqualTo(merchantVo1.getContact()
						.getPhoneList().toArray(new PhoneNo[2])[0].getPhoneId())
				.jsonPath("$.contact.phone_nos[0].phone_no")
				.isEqualTo(merchantVo1.getContact()
						.getPhoneList().toArray(new PhoneNo[2])[0].getPhoneNo())
				.jsonPath("$.contact.phone_nos[0].phone_type")
				.isEqualTo(merchantVo1.getContact()
						.getPhoneList().toArray(new PhoneNo[2])[0].getPhoneType())
				.jsonPath("$.contact.phone_nos[0].dial_code")
				.isEqualTo(merchantVo1.getContact()
						.getPhoneList().toArray(new PhoneNo[2])[0].getDialCode());
	}

}
