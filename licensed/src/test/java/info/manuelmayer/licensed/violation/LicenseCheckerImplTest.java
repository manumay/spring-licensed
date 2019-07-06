package info.manuelmayer.licensed.violation;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.License.LicenseRestrictions;
import info.manuelmayer.licensed.model.Licensing;
import info.manuelmayer.licensed.service.LicenseManager;
import info.manuelmayer.licensed.service.LicensingContext;
import info.manuelmayer.licensed.service.LicensingRepository;
import info.manuelmayer.licensed.service.SecurityContext;
import info.manuelmayer.licensed.violation.LicenseChecker.LicenseCheck;

public class LicenseCheckerImplTest {
	
	private static final String LICENSE_KEY = "license";
	private static final String USERNAME = "username";
	
	private Clock clock = Clock.fixed(LocalDate.of(2016, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC),
			ZoneId.systemDefault());
	
	@Mock
	private License license;
	
	@Mock
	private LicenseRestrictions restrictions;
	
	@Mock
	private LicenseManager licenseRepository;
	
	@Mock
	private Licensing licensingActive;
	
	@Mock
	private Licensing licensingInactive;
	
	@Mock
	private LicensingContext licensingContext;
	
	@Mock
	private LicensingRepository licensingRepo;
	
	@Mock
	private LicensingProperties licensingConfig;
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private LicenseChecker licenseChecker = new LicenseChecker(clock);
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(licensingConfig.getVersion()).thenReturn("matching");
		when(licenseRepository.getLicense(LICENSE_KEY)).thenReturn(Optional.of(license));
		when(license.getRestrictions()).thenReturn(restrictions);
		when(licensingActive.getActive()).thenReturn(Boolean.TRUE);
		when(licensingInactive.getActive()).thenReturn(Boolean.FALSE);
		when(securityContext.getUsername()).thenReturn(USERNAME);
	}
	
	@Test
	public void test_check_ok() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(restrictions.getNumberOfDevices()).thenReturn(2);
		when(restrictions.getNumberOfUsers()).thenReturn(2);
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(2100, 1, 1));
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(1L);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(1L);
		when(request.getServerName()).thenReturn("matching");
		
		List<LicenseCheck> checks = licenseChecker.check(LICENSE_KEY);
		assertThat(checks, iterableWithSize(5));
		
		for (LicenseCheck check : checks) {
			assertThat(check, isNotExhausted());
		}
	}
	
	@Test
	public void test_check_fail_all() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(restrictions.getNumberOfDevices()).thenReturn(1);
		when(restrictions.getNumberOfUsers()).thenReturn(1);
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(2L);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(2L);
		when(request.getServerName()).thenReturn("not-matching");
		when(licensingConfig.getVersion()).thenReturn("not-matching");
		
		List<LicenseCheck> checks = licenseChecker.check(LICENSE_KEY);
		assertThat(checks, iterableWithSize(5));
		
		for (LicenseCheck check : checks) {
			assertThat(check, isExhausted());
		}
	}
	
	@Test
	public void test_checkDuration_exhausted() {
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(1970, 1, 1));
		
		LicenseCheck check = licenseChecker.checkDuration(LICENSE_KEY);
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkDuration_not_exhausted() {
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(2900, 1, 1));
		
		LicenseCheck check = licenseChecker.checkDuration(LICENSE_KEY);
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkDevices_exhausted() {
		when(restrictions.getNumberOfDevices()).thenReturn(1);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(1L);
		
		LicenseCheck check = licenseChecker.checkDevices(LICENSE_KEY);
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkDevices_not_exhausted() {
		when(restrictions.getNumberOfDevices()).thenReturn(2);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(1L);
		
		LicenseCheck check = licenseChecker.checkDevices(LICENSE_KEY);
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkDevice_exhausted_no_license() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndDevice(eq(LICENSE_KEY), any(String.class))).thenReturn(null);
		
		LicenseCheck check = licenseChecker.checkDevice(LICENSE_KEY, "");
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkDevice_exhausted_inactive_license() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndDevice(eq(LICENSE_KEY), any(String.class))).thenReturn(licensingInactive);
		
		LicenseCheck check = licenseChecker.checkDevice(LICENSE_KEY, "");
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkDevice_not_exhausted() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndDevice(eq(LICENSE_KEY), any(String.class))).thenReturn(licensingActive);
		
		LicenseCheck check = licenseChecker.checkDevice(LICENSE_KEY, "");
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkHost_exhausted() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(request.getServerName()).thenReturn("not-matching");
		
		LicenseCheck check = licenseChecker.checkHost(LICENSE_KEY);
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkHost_not_exhausted() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(request.getServerName()).thenReturn("matching");
		
		LicenseCheck check = licenseChecker.checkHost(LICENSE_KEY);
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkUser_exhausted_no_license() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndUserLogin(eq(LICENSE_KEY), any(String.class))).thenReturn(null);
		
		LicenseCheck check = licenseChecker.checkUser(LICENSE_KEY, "");
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkUser_exhausted_inactive_license() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndUserLogin(eq(LICENSE_KEY), any(String.class))).thenReturn(licensingInactive);
		
		LicenseCheck check = licenseChecker.checkUser(LICENSE_KEY, "");
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkUser_not_exhausted() {
		when(licensingRepo.findByLicenseKeyAndActiveTrueAndUserLogin(eq(LICENSE_KEY), any(String.class))).thenReturn(licensingActive);
		
		LicenseCheck check = licenseChecker.checkUser(LICENSE_KEY, "");
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkFeature_exhausted() {
		when(restrictions.getFeatures()).thenReturn(Pattern.compile("matching"));
		
		LicenseCheck check = licenseChecker.checkFeature(LICENSE_KEY, "not-matching");
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkFeature_not_exhausted() {
		when(restrictions.getFeatures()).thenReturn(Pattern.compile("matching"));
		
		LicenseCheck check = licenseChecker.checkFeature(LICENSE_KEY, "matching");
		assertThat(check, isNotExhausted());
	}
	
	@Test
	public void test_checkVersion_exhausted() {
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		when(licensingConfig.getVersion()).thenReturn("not-matching");
		
		LicenseCheck check = licenseChecker.checkVersion(LICENSE_KEY);
		assertThat(check, isExhausted());
	}
	
	@Test
	public void test_checkVersion_not_exhausted() {
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		when(licensingConfig.getVersion()).thenReturn("matching");
		
		LicenseCheck check = licenseChecker.checkVersion(LICENSE_KEY);
		assertThat(check, isNotExhausted());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void test_getViolationCurrentDevice() {
		licenseChecker.getViolationCurrentDevice(LICENSE_KEY);
	}
	
	@Test
	public void test_getViolationCurrentUser_violated() {
		when(licensingContext.isLicensed(USERNAME, LICENSE_KEY)).thenReturn(Boolean.FALSE);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationCurrentUser(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationCurrentUser_ok() {
		when(licensingContext.isLicensed(USERNAME, LICENSE_KEY)).thenReturn(Boolean.TRUE);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationCurrentUser(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationFeature_violated() {
		when(restrictions.getFeatures()).thenReturn(Pattern.compile("matching"));
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationFeature(LICENSE_KEY, "not-matching");
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationFeature_ok() {
		when(restrictions.getFeatures()).thenReturn(Pattern.compile("matching"));
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationFeature(LICENSE_KEY, "matching");
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationHost_violated() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(request.getServerName()).thenReturn("not-matching");
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationHost(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationHost_ok() {
		when(restrictions.getHosts()).thenReturn(Pattern.compile("matching"));
		when(request.getServerName()).thenReturn("matching");
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationHost(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationMaxDevices_violated() {
		when(restrictions.getNumberOfDevices()).thenReturn(1);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(2L);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationMaxDevices(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationMaxDevices_ok() {
		when(restrictions.getNumberOfDevices()).thenReturn(1);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(LICENSE_KEY)).thenReturn(1L);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationMaxDevices(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationMaxUsers_violated() {
		when(restrictions.getNumberOfUsers()).thenReturn(1);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(2L);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationMaxUsers(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationMaxUsers_ok() {
		when(restrictions.getNumberOfUsers()).thenReturn(1);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(1L);
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationMaxUsers(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationPeriod_violated() {
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(1970, 1, 1));
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationPeriod(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationPeriod_ok() {
		when(restrictions.getValidFrom()).thenReturn(LocalDate.of(1970, 1, 1));
		when(restrictions.getValidTill()).thenReturn(LocalDate.of(2070, 1, 1));
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationPeriod(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	@Test
	public void test_getViolationVersion_violated() {
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		when(licensingConfig.getVersion()).thenReturn("not-matching");
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationVersion(LICENSE_KEY);
		assertThat(violation, isPresent());
	}
	
	@Test
	public void test_getViolationVersion_ok() {
		when(restrictions.getVersions()).thenReturn(Pattern.compile("matching"));
		when(licensingConfig.getVersion()).thenReturn("matching");
		
		Optional<LicenseViolation> violation = licenseChecker.getViolationVersion(LICENSE_KEY);
		assertThat(violation, isEmpty());
	}
	
	private static Matcher<LicenseCheck> isExhausted() {
		return new ExhaustedMatcher(true);
	}
	
	private static Matcher<LicenseCheck> isNotExhausted() {
		return new ExhaustedMatcher(false);
	}
	
	private static class ExhaustedMatcher extends BaseMatcher<LicenseCheck> {
		
		private final boolean exhausted;
		
		private ExhaustedMatcher(boolean exhausted) {
			this.exhausted = exhausted;
		}

		@Override
		public boolean matches(Object item) {
			return item instanceof LicenseCheck 
					&& ((LicenseCheck)item).isExhausted() == exhausted;
		}

		@Override
		public void describeTo(Description description) {
			description
            	.appendText(description(exhausted));
		}
		
		@Override
		public void describeMismatch(Object item, Description description) {
			description
				.appendText(description(((LicenseCheck)item).isExhausted()));
		}
		
		private String description(boolean exhausted) {
			return (exhausted ? "not" : "") + " exhausted";
		}

	}
	
}
