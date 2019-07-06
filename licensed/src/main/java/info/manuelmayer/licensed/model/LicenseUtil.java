package info.manuelmayer.licensed.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class LicenseUtil {

    // cryptic property names to save some bytes as rsa-encryption is limitied to 501 bytes with a 4096 key
	private static final String PROPERTY_APPLICATION_KEY = "applicationkey";
    private static final String PROPERTY_FEATURES = "features";
    private static final String PROPERTY_HOLDER = "holder";
    private static final String PROPERTY_HOSTS = "hosts";
    private static final String PROPERTY_ISSUEDATE = "issuedate";
    private static final String PROPERTY_ISSUER = "issuer";
    private static final String PROPERTY_NUMBER_OF_DEVICES = "devices";
    private static final String PROPERTY_NUMBER_OF_USERS = "users";
    private static final String PROPERTY_VALID_FROM = "fromdate";
    private static final String PROPERTY_VALID_TILL = "tilldate";
    private static final String PROPERTY_VERSIONS = "versions";
    private static final String UNSPECIFIED = "<unspecified>";
    private static final String MATCH_ALL = ".*";
    
    private LicenseUtil() {
    }
    
    public static Properties getProperties(License license) {
        Properties props = new Properties();
        
        String applicationKey = license.getApplicationKey();
        props.setProperty(PROPERTY_APPLICATION_KEY, StringUtils.isNotBlank(applicationKey) ? applicationKey : UNSPECIFIED);
        
        Pattern features = license.getRestrictions().getFeatures();
        props.setProperty(PROPERTY_FEATURES, features != null ? features.pattern() : UNSPECIFIED);
        
        String holder = license.getHolder();
        props.setProperty(PROPERTY_HOLDER, StringUtils.isNotBlank(holder) ? holder : UNSPECIFIED);
        
        Pattern hosts = license.getRestrictions().getHosts();
        props.setProperty(PROPERTY_HOSTS, hosts != null ? hosts.pattern() : UNSPECIFIED);
        
        String issuer = license.getIssuer();
        props.setProperty(PROPERTY_ISSUER, StringUtils.isNotBlank(issuer) ? issuer : UNSPECIFIED);
        
        LocalDate issueDate = license.getIssueDate();
        props.setProperty(PROPERTY_ISSUEDATE, issueDate != null ? issueDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : UNSPECIFIED);
        
        Integer numberOfDevices = license.getRestrictions().getNumberOfDevices();
        props.setProperty(PROPERTY_NUMBER_OF_DEVICES, numberOfDevices != null ? String.valueOf(numberOfDevices) : UNSPECIFIED);
        
        Integer numberOfUsers = license.getRestrictions().getNumberOfUsers();
        props.setProperty(PROPERTY_NUMBER_OF_USERS, numberOfUsers != null ? String.valueOf(numberOfUsers) : UNSPECIFIED);
        
        LocalDate validFrom = license.getRestrictions().getValidFrom();
        props.setProperty(PROPERTY_VALID_FROM, validFrom != null ? validFrom.format(DateTimeFormatter.ISO_LOCAL_DATE) : UNSPECIFIED);
        
        LocalDate validTill = license.getRestrictions().getValidTill();
        props.setProperty(PROPERTY_VALID_TILL, validTill != null ? validTill.format(DateTimeFormatter.ISO_LOCAL_DATE) : UNSPECIFIED);

        Pattern versions = license.getRestrictions().getVersions();
        props.setProperty(PROPERTY_VERSIONS, versions != null ? versions.pattern() : UNSPECIFIED);
        
        return props;
    }

    public static License getLicenseDetails(Properties properties) {
        if (properties == null) {
            return null;
        }
        
        LicenseImpl details = new LicenseImpl();
        
        String applicationKey = properties.getProperty(PROPERTY_APPLICATION_KEY, UNSPECIFIED);
        if (UNSPECIFIED.equals(applicationKey)) {
        	details.setApplicationKey("<?>"); // do not use null at this point, key might be used as key in a map
        } else {
        	details.setApplicationKey(applicationKey);
        }
        
        String features = properties.getProperty(PROPERTY_FEATURES, UNSPECIFIED);
        if (UNSPECIFIED.equals(features)) {
            Pattern featurePattern = Pattern.compile(MATCH_ALL);
            details.getRestrictions().setFeatures(featurePattern);
        } else {
            Pattern featurePattern = Pattern.compile(features);
            details.getRestrictions().setFeatures(featurePattern);
        }
        
        String holder = properties.getProperty(PROPERTY_HOLDER, UNSPECIFIED);
        if (UNSPECIFIED.equals(holder)) {
            details.setHolder(null);
        } else {
            details.setHolder(holder);
        }
        
        String hosts = properties.getProperty(PROPERTY_HOSTS, UNSPECIFIED);
        if (UNSPECIFIED.equals(hosts)) {
            Pattern hostPattern = Pattern.compile(MATCH_ALL);
            details.getRestrictions().setHosts(hostPattern);
        } else {
            Pattern hostPattern = Pattern.compile(hosts);
            details.getRestrictions().setHosts(hostPattern);
        }
        
        String issuer = properties.getProperty(PROPERTY_ISSUER, UNSPECIFIED);
        if (UNSPECIFIED.equals(issuer)) {
            details.setIssuer("");
        } else {
            details.setIssuer(issuer);
        }
        
        String issueDateText = properties.getProperty(PROPERTY_ISSUEDATE, UNSPECIFIED);
        if (UNSPECIFIED.equals(issueDateText)) {
            details.setIssueDate(null);
        } else {
            LocalDate issueDate = LocalDate.parse(issueDateText, DateTimeFormatter.ISO_LOCAL_DATE);
            details.setIssueDate(issueDate);
        }
        
        String numberOfDevices = properties.getProperty(PROPERTY_NUMBER_OF_DEVICES, UNSPECIFIED);
        if (UNSPECIFIED.equals(numberOfDevices)) {
            details.getRestrictions().setNumberOfDevices(Integer.MAX_VALUE);
        } else {
            details.getRestrictions().setNumberOfDevices(Integer.valueOf(numberOfDevices));
        }
        
        String numberOfUsers = properties.getProperty(PROPERTY_NUMBER_OF_USERS, UNSPECIFIED);
        if (UNSPECIFIED.equals(numberOfUsers)) {
            details.getRestrictions().setNumberOfUsers(Integer.MAX_VALUE);
        } else {
            details.getRestrictions().setNumberOfUsers(Integer.valueOf(numberOfUsers));
        }
        
        String validFromText = properties.getProperty(PROPERTY_VALID_FROM, UNSPECIFIED);
        if (UNSPECIFIED.equals(validFromText)) {
            details.getRestrictions().setValidFrom(LocalDate.MIN);
        } else {
            details.getRestrictions().setValidFrom(LocalDate.parse(validFromText, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        String validTillText = properties.getProperty(PROPERTY_VALID_TILL, UNSPECIFIED);
        if (UNSPECIFIED.equals(validTillText)) {
            details.getRestrictions().setValidTill(LocalDate.MIN);
        } else {
            details.getRestrictions().setValidTill(LocalDate.parse(validTillText, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        String versions = properties.getProperty(PROPERTY_VERSIONS, UNSPECIFIED);
        if (UNSPECIFIED.equals(versions)) {
            Pattern versionPattern = Pattern.compile(MATCH_ALL);
            details.getRestrictions().setVersions(versionPattern);
        } else {
            Pattern versionPattern = Pattern.compile(versions);
            details.getRestrictions().setVersions(versionPattern);
        }
        
        return details;
    }
    
}
