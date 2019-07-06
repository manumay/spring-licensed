package info.manuelmayer.licensed.test;


import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;
import java.util.Optional;

import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.License.LicenseRestrictions;

public final class AssertLicense {

	private AssertLicense() {
	}
	
	public static void fallback(Optional<License> optLicense) {
		assertThat(optLicense, isPresent());
		fallback(optLicense.get());
	}
	
	public static void fallback(License license) {
		assertThat(license, notNullValue());
		assertThat(license.getHolder(), equalTo("<Unbekannt>"));
		assertThat(license.getIssueDate(), equalTo(LocalDate.of(2019, 7, 7)));
		assertThat(license.getIssuer(), equalTo("Manuel Mayer"));
		
		LicenseRestrictions restrictions = license.getRestrictions();
		assertThat(restrictions, notNullValue());
		assertThat(restrictions.getFeatures(), notNullValue());
		assertThat(restrictions.getFeatures().pattern(), equalTo(".*"));
		assertThat(restrictions.getHosts(), notNullValue());
		assertThat(restrictions.getHosts().pattern(), equalTo("^(localhost|127\\.0\\.0\\.1)$"));
		assertThat(restrictions.getNumberOfDevices(), equalTo(1));
		assertThat(restrictions.getNumberOfUsers(), equalTo(1));
		assertThat(restrictions.getValidFrom(), equalTo(LocalDate.of(2016, 1, 1)));
		assertThat(restrictions.getValidTill(), equalTo(LocalDate.of(2099, 1, 1)));
		assertThat(restrictions.getVersions(), notNullValue());
		assertThat(restrictions.getVersions().pattern(), equalTo(".*"));
	}
	
	public static void test(Optional<License> optLicense) {
		assertThat(optLicense, isPresent());
		test(optLicense.get());
	}
	
	public static void test(License license) {
		assertThat(license, notNullValue());
		assertThat(license.getApplicationKey(), equalTo("application"));
		assertThat(license.getHolder(), equalTo("holder"));
		assertThat(license.getIssueDate(), equalTo(LocalDate.of(2001, 1, 1)));
		assertThat(license.getIssuer(), equalTo("issuer"));
		
		LicenseRestrictions restrictions = license.getRestrictions();
		assertThat(restrictions, notNullValue());
		assertThat(restrictions.getFeatures(), notNullValue());
		assertThat(restrictions.getFeatures().pattern(), equalTo("licensed-feature"));
		assertThat(restrictions.getHosts(), notNullValue());
		assertThat(restrictions.getHosts().pattern(), equalTo("licensed-host"));
		assertThat(restrictions.getNumberOfDevices(), equalTo(1));
		assertThat(restrictions.getNumberOfUsers(), equalTo(1));
		assertThat(restrictions.getValidFrom(), equalTo(LocalDate.of(2001, 1, 1)));
		assertThat(restrictions.getValidTill(), equalTo(LocalDate.of(2001, 1, 1)));
		assertThat(restrictions.getVersions(), notNullValue());
		assertThat(restrictions.getVersions().pattern(), equalTo("licensed-version"));
	}
	
}
