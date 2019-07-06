package info.manuelmayer.licensed.violation;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.manuelmayer.licensed.test.BaseLicensingIT;
import info.manuelmayer.licensed.test.LicensedObjectImpl;
import info.manuelmayer.licensed.violation.LicenseViolationException;

public class LicensedIT extends BaseLicensingIT {
			
	@Autowired
	private LicensedObjectImpl licensedObject;
	
	@Before
	public void before() {
		reset(code);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void test_current_device_fail() {
		try {
			licensedObject.currentDevice();
			fail("license check should fail for current user");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void test_current_device_ok() {
		licensedObject.currentDevice();
		verify(code).invoked();
	}
	
	@Ignore
	@Test
	public void test_current_user_fail() {
		try {
			licensedObject.currentUser();
			fail("license check should fail for current user");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
	
	@Ignore
	@Test
	public void test_current_user_ok() {		
		licensedObject.currentUser();
		verify(code).invoked();
	}
	
	@Test
	public void test_devices_fail() {
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(APPLICATION_KEY)).thenReturn(2L);
		
		try {
			licensedObject.devices();
			fail("license check should fail for number of devices");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
	
	@Test
	public void test_devices_ok() {
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(APPLICATION_KEY)).thenReturn(1L);
		licensedObject.devices();
		verify(code).invoked();
	}
	
	@Test
	public void test_feature_fail() {
		try {
			licensedObject.featureUnlicensed();
			fail("license check should fail for feature");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
	
	@Test
	public void test_feature_ok() {
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndDeviceNotNull(APPLICATION_KEY)).thenReturn(1L);
		licensedObject.featureLicensed();
		verify(code).invoked();
	}
	
	
	@Test
	public void test_host_ok() {
		when(request.getServerName()).thenReturn("licensed-host");
		licensedObject.host();
		verify(code).invoked();
	}
	
	@Test
	public void test_host_fail() {
		when(request.getServerName()).thenReturn("unlicensed-host");
		try {
			licensedObject.host();
			fail("license check should fail for host");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
//	
//	@Test
//	public void test_period_ok() {
//		testClock.setInstant(LocalDate.of(2001, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
//		licensedObject.period();
//		verify(code).invoked();
//	}
//	
//	@Test
//	public void test_period_fail() {
//		testClock.setInstant(LocalDate.of(2001, 1, 2).atStartOfDay().toInstant(ZoneOffset.UTC));
//		
//		try {
//			licensedObject.period();
//			fail("license check should fail for period");
//		} catch (LicenseViolationException e) {
//			verify(code, never()).invoked();
//		}
//	}
	
	@Test
	public void test_users_ok() {
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(APPLICATION_KEY)).thenReturn(1L);
		licensedObject.users();
		verify(code).invoked();
	}
	
	@Test
	public void test_users_fail() {
		when(licensingRepo.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(APPLICATION_KEY)).thenReturn(2L);
		
		try {
			licensedObject.users();
			fail("license check should fail for number of users");
		} catch (LicenseViolationException e) {
			verify(code, never()).invoked();
		}
	}
	
//	@Test
//	public void test_version_ok() {
//		when(sysInfo.getApplicationVersion()).thenReturn("licensed-version");
//		licensedObject.version();
//		verify(code).invoked();
//	}
//	
//	@Test
//	public void test_version_fail() {
//		when(sysInfo.getApplicationVersion()).thenReturn("unlicensed-version");
//		try {
//			licensedObject.version();
//			fail("license check should fail for version");
//		} catch (LicenseViolationException e) {
//			verify(code, never()).invoked();
//		}
//	}
//	
}
