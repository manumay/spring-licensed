package info.manuelmayer.licensed.violation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import info.manuelmayer.licensed.boot.LicensingProperties;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.Licensing;
import info.manuelmayer.licensed.service.LicenseManager;
import info.manuelmayer.licensed.service.LicensingContext;
import info.manuelmayer.licensed.service.LicensingRepository;
import info.manuelmayer.licensed.service.SecurityContext;

public class LicenseChecker {
       
	@Autowired
	private LicensingProperties config;
	
    @Autowired
    private LicenseManager licenseRepository;
    
    @Autowired
    private LicensingContext licensingContext;
    
    @Autowired
    private LicensingRepository licensingRepository;
    
    @Autowired(required=false)
    private HttpServletRequest request;
    
    @Autowired
    private SecurityContext securityContext;
    
    private Clock clock;
    
    @Autowired
    public LicenseChecker(Clock clock) {
    	this.clock = Objects.requireNonNull(clock);
	}
    
    public Map<String, List<LicenseCheck>> checkAll() {
    	return licenseRepository.getLicenseKeys().stream()
    			.collect(Collectors.toMap(key -> key, key -> check(key)));
    }
    
    public List<LicenseCheck> check(String key) {
        List<LicenseCheck> checks = new ArrayList<>();
        checks.add(checkDevices(key));
        checks.add(checkDuration(key));
        checks.add(checkHost(key));
        checks.add(checkUsers(key));
        checks.add(checkVersion(key));
        return checks;
    }

    public LicenseCheck checkDuration(String key) {
        LocalDate now = LocalDate.now(clock);
        LocalDate validFrom = getLicense(key).getRestrictions().getValidFrom();
        LocalDate validTill = getLicense(key).getRestrictions().getValidTill();
        
        String scope = "valid";
        boolean exhausted = !(validFrom.isBefore(now) && validTill.isAfter(now));
        
        String details = exhausted ? getInvalidSince(validFrom.until(now)) : getValidFor(now.until(validTill));
        return new LicenseCheck(scope, exhausted, details);
    }

    public LicenseCheck checkFeature(String key, String feature) {
        String scope = "feature";
        boolean exhausted = !getLicense(key).getRestrictions().getFeatures()
                .matcher(feature).matches();
        String details = exhausted ? "not licensed" : "licensed";
        return new LicenseCheck(scope, exhausted, details); 
    }
    
    private String getInvalidSince(Period period) {
        return String.format(LicenseViolationMessages.TIMEPERIOD_UNLICENSED, period.getYears(), period.getMonths(), period.getDays());
    }
    
    private String getValidFor(Period period) {
        return String.format(LicenseViolationMessages.TIMEPERIOD_LICENSED, period.getYears(), period.getMonths(), period.getDays());
    }
    
    public LicenseCheck checkDevice(String key, String deviceId) {
        Licensing licensing = licensingRepository.findByLicenseKeyAndActiveTrueAndDevice(key, deviceId);
        
        String scope = "device-" + deviceId;
        boolean exhausted = licensing == null || Boolean.FALSE.equals(licensing.getActive());
        String details = String.format(exhausted ? LicenseViolationMessages.DEVICE_UNLICENSED : LicenseViolationMessages.DEVICE_LICENSED, deviceId);
        return new LicenseCheck(scope, exhausted, details);
    }
    
    public LicenseCheck checkDevices(String key) {
        long devices = licensingRepository.countByLicenseKeyAndActiveTrueAndDeviceNotNull(key);
        long maxDevices = getLicense(key).getRestrictions().getNumberOfDevices();
        
        String scope = "devices";
        boolean exhausted = devices >= maxDevices;
        String details = exhausted ? String.format(LicenseViolationMessages.DEVICE_UNLICENSED, devices, maxDevices) :
            String.format(LicenseViolationMessages.DEVICE_LICENSED, devices, maxDevices);
        return new LicenseCheck(scope, exhausted, details);
    }
    
    public LicenseCheck checkHost(String key) {
        String scope = "host";
        boolean exhausted = request != null && !getLicense(key).getRestrictions().getHosts()
                .matcher(request.getServerName()).matches();
        String details = String.format(exhausted ? LicenseViolationMessages.HOST_UNLICENSED : LicenseViolationMessages.HOST_LICENSED, request != null ? request.getServerName() : "?");
        return new LicenseCheck(scope, exhausted, details); 
    }

    public LicenseCheck checkUser(String key, String userLogin) {
        Licensing licensing = licensingRepository.findByLicenseKeyAndActiveTrueAndUserLogin(key, userLogin);
        
        String scope = "user-" + userLogin;
        boolean exhausted = licensing == null || Boolean.FALSE.equals(licensing.getActive());
        String details = String.format(exhausted ? LicenseViolationMessages.USER_UNLICENSED : LicenseViolationMessages.USER_LICENSED, userLogin);
        return new LicenseCheck(scope, exhausted, details);
    }

    public LicenseCheck checkUsers(String key) {
        long users = licensingRepository.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(key);
        long maxUsers = getLicense(key).getRestrictions().getNumberOfUsers();
        
        String scope = "users";
        boolean exhausted = users > maxUsers;
        String details = exhausted ? String.format(LicenseViolationMessages.USERS_UNLICENSED, users, maxUsers) :
            String.format(LicenseViolationMessages.USERS_LICENSED, users, maxUsers);
        return new LicenseCheck(scope, exhausted, details);
    }

    public LicenseCheck checkVersion(String key) {
        return checkVersion(key, config.getVersion());
    }
    
    public Optional<LicenseViolation> getViolationCurrentDevice(String key) {
        throw new UnsupportedOperationException("not supported yet");
    }
    
    public Optional<LicenseViolation> getViolationCurrentUser(String key) {
        return licensingContext.isLicensed(getUsername(), key) ? Optional.empty()
                : Optional.of(LicenseViolation.userLicenseMissing(getUsername()));
    }
    
    public Optional<LicenseViolation> getViolationMaxDevices(String key) {
        long devices = licensingRepository.countByLicenseKeyAndActiveTrueAndDeviceNotNull(key);
        long maxDevices = getLicense(key).getRestrictions().getNumberOfDevices();
        return devices <= maxDevices ? Optional.empty()
                : Optional.of(LicenseViolation.maxDevicesReached(devices, maxDevices));
    }
    
    public Optional<LicenseViolation> getViolationPeriod(String key) {
        LocalDate now = LocalDate.now(clock);
        LocalDate validFrom = getLicense(key).getRestrictions().getValidFrom();
        LocalDate validTill = getLicense(key).getRestrictions().getValidTill();
        return (validFrom.isBefore(now) || validFrom.isEqual(now)) 
        		&& (validTill.isAfter(now) || validTill.isEqual(now)) 
        		? Optional.empty() : Optional.of(LicenseViolation.periodExpired());
    }
    
    public Optional<LicenseViolation> getViolationFeature(String key, String feature) {
        return getLicense(key).getRestrictions().getFeatures()
                .matcher(feature).matches() ? Optional.empty()
                        : Optional.of(LicenseViolation.unlicensedFeature(feature));
    }
    
    public Optional<LicenseViolation> getViolationHost(String key) {
        if (request == null) {
            return Optional.empty();
        }
        
        String serverName = request.getServerName();
        return getLicense(key).getRestrictions().getHosts()
                .matcher(serverName).matches() ? Optional.empty()
                        : Optional.of(LicenseViolation.unlicensedHost(serverName));
    }
    
    public Optional<LicenseViolation> getViolationMaxUsers(String key) {
        long users = licensingRepository.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(key);
        long maxUsers = getLicense(key).getRestrictions().getNumberOfUsers();
        return users <= maxUsers ? Optional.empty()
                : Optional.of(LicenseViolation.maxUsersReached(users, maxUsers));
    }
    
    public Optional<LicenseViolation> getViolationVersion(String key) {
        return getLicense(key).getRestrictions().getVersions()
                .matcher(config.getVersion()).matches() ? Optional.empty()
                        : Optional.of(LicenseViolation.unlicensedVersion(config.getVersion()));
    }
    
    private LicenseCheck checkVersion(String key, String version) {
        String scope = "version";
        boolean exhausted = !getLicense(key).getRestrictions().getVersions()
                .matcher(version).matches();
        String details = String.format(exhausted ? LicenseViolationMessages.VERSION_UNLICENSED : LicenseViolationMessages.DEVICE_LICENSED, version);
        return new LicenseCheck(scope, exhausted, details); 
    }
    
    private String getUsername() {
    	return securityContext.getUsername();
    }
    
    private License getLicense(String key) {
    	return licenseRepository.getLicense(key).orElseThrow(RuntimeException::new);
    }
    
    public static class LicenseCheck {

        private final String scope;
        private final boolean exhausted;
        private final String details;
        
        private LicenseCheck(String scope, boolean exhausted, String details) {
            this.scope = scope;
            this.exhausted = exhausted;
            this.details = details;
        }
        
        public String getScope() {
            return scope;
        }

        public boolean isExhausted() {
            return exhausted;
        }

        public String getDetails() {
            return details;
        }
        
    }

}
