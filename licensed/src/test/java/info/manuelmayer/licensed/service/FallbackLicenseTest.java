package info.manuelmayer.licensed.service;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import info.manuelmayer.licensed.io.LicenseReader;
import info.manuelmayer.licensed.io.LicenseReaderImpl;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.test.AssertLicense;

public class FallbackLicenseTest {

	@Test
	public void test_fallback_license() {
		LicenseReader reader = new LicenseReaderImpl();
		
		try (InputStream in = FallbackLicenseTest.class.getResourceAsStream("/fallback.license")) {
			License license = reader.read(in);
			AssertLicense.fallback(license);
		} catch (IOException e) {
			fail("fallback.license should be readable");
		}
	}
	
}
