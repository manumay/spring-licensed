package info.manuelmayer.licensed.service;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import info.manuelmayer.licensed.test.AssertLicense;
import info.manuelmayer.licensed.test.BaseLicensingIT;

@Ignore
public class LicenseRepositoryImplIT extends BaseLicensingIT {

	@Autowired
	private LicenseManager licenseRepo;
	
	@Test
	public void test_license_auto_initialized() {
		AssertLicense.test(licenseRepo.getLicense(APPLICATION_KEY));
	}
	
	@Test
	public void test_license_fallback() {
		AssertLicense.fallback(licenseRepo.getLicense("bla"));
	}
	
}
