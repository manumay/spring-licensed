package info.manuelmayer.licensed.actuator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.service.LicenseManager;

@Endpoint(id="license")
public class LicenseEndpoint {
    
    @Autowired
    private LicenseManager licenseRepository;
    
    @ReadOperation
    public List<LicenseInfo> invoke() {
    	Map<String,License> licenses = licenseRepository.getLicenses();
    	return licenses.values().stream()
    			.map(l -> new LicenseInfo(l))
    			.sorted(Comparator.comparing(LicenseInfo::getApplicationKey))
    			.collect(Collectors.toList());
    }
    
    public static class LicenseInfo {

        private final License license;
        
        public LicenseInfo(License license) {
            if (license == null) {
                throw new IllegalArgumentException("license must not be null");
            }
            this.license = license;
        }
        
        public String getApplicationKey() {
        	return license.getApplicationKey();
        }
        
        public String getFeatures() {
            return license.getRestrictions().getFeatures().pattern();
        }
        
        public String getHolder() {
            return license.getHolder();
        }
        
        public String getHosts() {
            return license.getRestrictions().getHosts().pattern();
        }
        
        public String getIssuer() {
            return license.getIssuer();
        }
        
        public LocalDate getIssueDate() {
            return license.getIssueDate();
        }
        
        public Integer getNumberOfDevices() {
            return license.getRestrictions().getNumberOfDevices();
        }
        
        public Integer getNumberOfUsers() {
            return license.getRestrictions().getNumberOfUsers();
        }
        
        public LocalDate getValidFrom() {
            return license.getRestrictions().getValidFrom();
        }
        
        public LocalDate getValidTill() {
            return license.getRestrictions().getValidTill();
        }
        
        public String getVersions() {
            return license.getRestrictions().getVersions().pattern();
        }
        
    }
}
