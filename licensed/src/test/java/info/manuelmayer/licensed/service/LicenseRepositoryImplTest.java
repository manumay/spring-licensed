package info.manuelmayer.licensed.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.io.LicenseReaderException;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.test.AssertLicense;

public class LicenseRepositoryImplTest {
	
	private static final String APPLICATION_KEY = "application";
	
	@Mock
	private LicensingProperties config;
	
	@InjectMocks
	private final LicenseManager licenseRepo = new LicenseManager();
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test(expected=LicenseReaderException.class)
	public void test_uninitialized() {
		licenseRepo.getLicense(APPLICATION_KEY);
	}
	
	@Test
	public void test_license_fallback() {
		List<Resource> empty = new ArrayList<>();
		when(config.getLicenses()).thenReturn(empty);
		
		licenseRepo.initialize();
		
		Optional<License> license = licenseRepo.getLicense(APPLICATION_KEY);
		AssertLicense.fallback(license);
	}
	
	@Test
	public void test_license_read() {
		List<Resource> licenses = new ArrayList<>();
		licenses.add(new ClassPathResource("test.license"));
		when(config.getLicenses()).thenReturn(licenses);
		
		licenseRepo.initialize();
		
		Optional<License> license = licenseRepo.getLicense(APPLICATION_KEY);
		AssertLicense.test(license);
	}
	
}
