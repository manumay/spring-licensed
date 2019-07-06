package info.manuelmayer.licensed.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.io.LicenseReaderException;
import info.manuelmayer.licensed.io.LicenseReaderImpl;
import info.manuelmayer.licensed.model.License;

public class LicenseManager {

	@Autowired
	private LicensingProperties config;

	private License fallbackLicense;
	private Map<String,License> licenses;

	private final Log log = LogFactory.getLog(getClass());
	
	public Set<String> getLicenseKeys() {
		return licenses.keySet();
	}
	
	public Map<String, License> getLicenses() {
		return licenses;
	}

	public Optional<License> getLicense(String key) {
		License license = getLicenseWithFallback(key);
		if (license instanceof LicenseImmutableImpl == false) {
			throw new LicenseReaderException();
		}
		return Optional.ofNullable(license);
	}
	
	private License getLicenseWithFallback(String key) {
		if (key != null && this.licenses != null && this.licenses.containsKey(key)) {
			return this.licenses.get(key);
		}
		return this.fallbackLicense;
	}

	@PostConstruct
	public void initialize() {
		readFallbackLicense();
		readConfiguredLicenses();
	}
	
	private License readFallbackLicense() {
		Resource fallback = new ClassPathResource("fallback.license");
		if (fallback.exists()) {
			this.fallbackLicense = read(fallback);
			if (this.fallbackLicense == null) {
				throw new LicenseReaderException("fallback license could not be read");
			}
			return this.fallbackLicense;
		} else {
			throw new LicenseReaderException("fallback license could not be found");
		}
	}

	private void readConfiguredLicenses() {
		List<Resource> licenseResources = config.getLicenses();
		
		Map<String,License> licenses = new HashMap<>();
		for (Resource licenseResource : licenseResources) {
			if (licenseResource.exists()) {
				LicenseImmutableImpl license = read(licenseResource);
				String applicationKey = license.getApplicationKey();
				if (licenses.containsKey(applicationKey)) {
					log.warn("duplicate licensed application detected for key " + applicationKey);
				}
				licenses.put(license.getApplicationKey(), license);
				log.info("license was read successfully: " + licenseResource.toString());
			} else {
				log.warn("license was not found at " + licenseResource.toString());
			}
		}
		this.licenses = Collections.unmodifiableMap(licenses);
	}

	private LicenseImmutableImpl read(Resource res) {
		try (InputStream in = res.getInputStream()) {
			LicenseReaderImpl r = new LicenseReaderImpl();
			License l = r.read(in);
			return new LicenseImmutableImpl(l);
		} catch (IOException | RuntimeException e) {
			log.warn("could not read license file " + res.toString());
			return null;
		}
	}

	private static final class LicenseImmutableImpl implements License {

		private final String applicationKey;
		private final String holder;
		private final String issuer;
		private final LocalDate issueDate;
		private final LicenseRestrictionsImmutableImpl restrictions;

		private LicenseImmutableImpl(License other) {
			this.applicationKey = other.getApplicationKey();
			this.holder = other.getHolder();
			this.issueDate = other.getIssueDate();
			this.issuer = other.getIssuer();
			this.restrictions = new LicenseRestrictionsImmutableImpl(other.getRestrictions());
		}
		
		@Override
		public String getApplicationKey() {
			return applicationKey;
		}

		@Override
		public String getHolder() {
			return holder;
		}

		@Override
		public String getIssuer() {
			return issuer;
		}

		@Override
		public LocalDate getIssueDate() {
			return issueDate;
		}

		@Override
		public LicenseRestrictions getRestrictions() {
			return restrictions;
		}

		private static final class LicenseRestrictionsImmutableImpl implements LicenseRestrictions {

			private final Pattern features;
			private final Pattern hosts;
			private final Integer numberOfDevices;
			private final Integer numberOfUsers;
			private final LocalDate validFrom;
			private final LocalDate validTill;
			private final Pattern versions;

			private LicenseRestrictionsImmutableImpl(LicenseRestrictions other) {
				this.features = Pattern.compile(other.getFeatures().pattern());
				this.hosts = Pattern.compile(other.getHosts().pattern());
				this.numberOfDevices = other.getNumberOfDevices();
				this.numberOfUsers = other.getNumberOfUsers();
				this.validFrom = other.getValidFrom();
				this.validTill = other.getValidTill();
				this.versions = Pattern.compile(other.getVersions().pattern());
			}

			@Override
			public Pattern getFeatures() {
				return features;
			}

			@Override
			public Pattern getHosts() {
				return hosts;
			}

			@Override
			public Integer getNumberOfDevices() {
				return numberOfDevices;
			}

			@Override
			public Integer getNumberOfUsers() {
				return numberOfUsers;
			}

			@Override
			public LocalDate getValidFrom() {
				return validFrom;
			}

			@Override
			public LocalDate getValidTill() {
				return validTill;
			}

			@Override
			public Pattern getVersions() {
				return versions;
			}

		}

	}
}
