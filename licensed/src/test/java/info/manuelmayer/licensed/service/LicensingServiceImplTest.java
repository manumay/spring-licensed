package info.manuelmayer.licensed.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.License.LicenseRestrictions;
import info.manuelmayer.licensed.model.Licensing;

@RunWith(MockitoJUnitRunner.class)
public class LicensingServiceImplTest {
	
	private static final Long ID = 1L;
	private static final String LICENSE_KEY = "license";
	private static final String LOGIN = "login";
	
	@Mock
	private License license;
	
	@Mock
	private LicensingCache licensingCache;
	
	@Mock
	private LicenseRestrictions restrictions;
	
	@Mock
	private LicenseManager licenseManager;
		
	@Spy
	private Licensing activeLicensing = new Licensing(),
		inactiveLicensing = new Licensing(),
		savedLicensing = new Licensing();
	
	@Mock
	private LicensingRepository licensingRepo;
	
	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private final LicensingServiceImpl licensingService = new LicensingServiceImpl(Clock.systemDefaultZone());	
	
	@Before
	public void init() {
		Set<Licensing> licensing = new HashSet<>();
		licensing.add(savedLicensing);
		
		activeLicensing.setActive(Boolean.TRUE);
		activeLicensing.setLicenseKey(LICENSE_KEY);
		inactiveLicensing.setActive(Boolean.FALSE);
		inactiveLicensing.setLicenseKey(LICENSE_KEY);
		savedLicensing.setLicenseKey(LICENSE_KEY);
		savedLicensing.setUserLogin(LOGIN);
		
		when(licenseManager.getLicense(LICENSE_KEY)).thenReturn(Optional.of(license));
		when(license.getRestrictions()).thenReturn(restrictions);
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(0L);
		when(licensingRepo.saveAndFlush(any(Licensing.class))).thenReturn(savedLicensing);
		when(restrictions.getNumberOfUsers()).thenReturn(1);
	}
	
	@Test
	public void test_activate_unknown_id() {
		when(licensingRepo.findById(ID)).thenReturn(Optional.empty());
		
		Licensing activated = licensingService.activate(ID);
		assertThat(activated, nullValue());
		
		verify(licensingRepo, never()).saveAndFlush(any(Licensing.class));
		verify(licensingCache, never()).invalidate(any(String.class));
	}
	
	@Test
	public void test_activate_max_users_reached() {
		when(licensingRepo.findById(ID)).thenReturn(Optional.of(inactiveLicensing));
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(1L);
		when(restrictions.getNumberOfUsers()).thenReturn(1);
		
		Licensing activated = licensingService.activate(ID);
		assertThat(activated, sameInstance(inactiveLicensing));
		
		verify(activated, never()).setActive(Boolean.TRUE);
		verify(licensingRepo, never()).saveAndFlush(any(Licensing.class));
		verify(licensingCache, never()).invalidate(any(String.class));
	}
	
	@Test
	public void test_activate_successfull() {
		when(licensingRepo.findById(ID)).thenReturn(Optional.of(inactiveLicensing));
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(LICENSE_KEY)).thenReturn(0L);
		when(restrictions.getNumberOfUsers()).thenReturn(1);
		
		Licensing activated = licensingService.activate(ID);
		assertThat(activated, sameInstance(savedLicensing));
		
		verify(inactiveLicensing).setActive(Boolean.TRUE);
		verify(licensingRepo).saveAndFlush(inactiveLicensing);
		verify(licensingCache).invalidate(LOGIN);
	}
	
	@Test
	public void test_deactivate_unknown_id() {
		when(licensingRepo.findById(ID)).thenReturn(Optional.empty());
		
		Licensing deactivated = licensingService.deactivate(ID);
		assertThat(deactivated, nullValue());
		
		verify(licensingRepo, never()).saveAndFlush(any(Licensing.class));
		verify(licensingCache, never()).invalidate(any(String.class));
	}
	
	@Test
	public void test_deactivate_successfull() {
		when(licensingRepo.findById(ID)).thenReturn(Optional.of(activeLicensing));
		
		Licensing deactivated = licensingService.deactivate(ID);
		assertThat(deactivated, sameInstance(savedLicensing));
		
		verify(activeLicensing).setActive(Boolean.FALSE);
		verify(licensingRepo).saveAndFlush(activeLicensing);
		verify(licensingCache).invalidate(LOGIN);
	}
	
	@Test
	public void test_assignUserLicense_already_licensed() {
		when(licensingRepo.findByUserLogin(LOGIN)).thenReturn(Arrays.asList(activeLicensing));
		
		Licensing assigned = licensingService.assignUserLicense(LICENSE_KEY, LOGIN);
		assertThat(assigned, sameInstance(activeLicensing));
		
		verify(licensingRepo, never()).saveAndFlush(any(Licensing.class));
		verify(licensingCache, never()).invalidate(any(String.class));
	}
	
	@Test
	public void test_assignUserLicense_max_users() {
		when(licensingRepo.findByUserLogin(LOGIN)).thenReturn(Arrays.asList(inactiveLicensing));
		
		Licensing assigned = licensingService.assignUserLicense(LICENSE_KEY, LOGIN);
		assertThat(assigned, sameInstance(inactiveLicensing));
		
		verify(licensingRepo, never()).saveAndFlush(any(Licensing.class));
		verify(licensingCache, never()).invalidate(any(String.class));
	}
	
	@Test
	public void test_assignUserLicense_successfull() {
		when(licensingRepo.findByUserLogin(LOGIN)).thenReturn(Collections.emptyList());
		
		Licensing assigned = licensingService.assignUserLicense(LICENSE_KEY, LOGIN);
		assertThat(assigned, sameInstance(savedLicensing));
		
		verify(licensingRepo).saveAndFlush(any(Licensing.class));
		verify(licensingCache).invalidate(LOGIN);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void test_assignLicenseToCurrentDevice() {
		licensingService.assignLicenseToCurrentDevice(LICENSE_KEY);
	}
	
	@Test
	public void test_assignLicenseToCurrentUser() {
		when(securityContext.getUsername()).thenReturn(LOGIN);
		when(licensingRepo.findByUserLogin(LOGIN)).thenReturn(Collections.emptyList());
		
		Licensing assigned = licensingService.assignLicenseToCurrentUser(LICENSE_KEY);
		assertThat(assigned, sameInstance(savedLicensing));
		
		verify(licensingRepo).findByUserLogin(LOGIN);
		verify(licensingRepo).saveAndFlush(any(Licensing.class));
		verify(licensingCache).invalidate(LOGIN);
	}
	
}
