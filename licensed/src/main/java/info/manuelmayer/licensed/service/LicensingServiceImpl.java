package info.manuelmayer.licensed.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.Licensing;

public class LicensingServiceImpl implements LicensingService {
	
	@Autowired
	private LicensingCache licensingCache;
    
    @Autowired
    private LicenseManager licenseRepository;

    @Autowired
    private LicensingRepository licensingRepository;
        
    @Autowired
    private SecurityContext securityContext;
        
    private Clock clock;
    
    private final Object lock = new Object();
    
    @Autowired
    public LicensingServiceImpl(Clock clock) {
		this.clock = Objects.requireNonNull(clock);
	}
    
    @Override
    public Licensing activate(Long id) {
        Optional<Licensing> optLic = licensingRepository.findById(id);
        
        if (optLic.isPresent()) {
        	Licensing l = optLic.get();
            if (Boolean.FALSE.equals(l.getActive())) {
            	String key = l.getLicenseKey();
                long current = licensingRepository.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(key);
                
                Optional<License> optLicense = licenseRepository.getLicense(key);
                if (!optLicense.isPresent()) {
                	throw new RuntimeException("no license named " + key);
                }
                
                long max = optLicense.get().getRestrictions().getNumberOfUsers();
                if (current >= max) {
                    return l;
                }
                
                l.setActive(Boolean.TRUE);
                l.setModified(LocalDateTime.now(clock));
                l = licensingRepository.saveAndFlush(l);
                invalidateLicensingCache(l.getUserLogin());
                return l;
            }
            
        }

        return optLic.orElse(null);
    }
    
    @Override
    public Licensing assignLicenseToCurrentDevice(String key) {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    @Override
    public Licensing assignLicenseToCurrentUser(String key) {
        return assignUserLicense(key, getUsername());
    }
    
    @Override
    public Licensing assignUserLicense(String key, String userLogin) {
        synchronized (lock) {
            List<Licensing> existingLicensing = getExistingLicensings(userLogin);
            Map<String,Licensing> byKey = existingLicensing.stream()
            		.collect(Collectors.toMap(l -> l.getLicenseKey(), l -> l));
            if (byKey.containsKey(key)) {
            	// it has been created in the meantime (possibly while waiting for the lock)
                return byKey.get(key); 
            }
            
            Optional<License> optLicense = licenseRepository.getLicense(key);
            if (!optLicense.isPresent()) {
            	throw new RuntimeException("no license named " + key);
            }
            
            long licensedUsers = licensingRepository.countByLicenseKeyAndActiveTrueAndUserLoginNotNull(key);
            long maxUsers = optLicense.get().getRestrictions().getNumberOfUsers();
            if (licensedUsers >= maxUsers) {
                return null;
            }
            
            Licensing licensing = new Licensing();
            licensing.setActive(Boolean.TRUE);
            licensing.setDevice(null);
            licensing.setLicenseKey(key);
            licensing.setModified(LocalDateTime.now(clock));
            licensing.setUserLogin(userLogin);
            licensing = licensingRepository.saveAndFlush(licensing);
            invalidateLicensingCache(userLogin);
            return licensing;
        }
    }
    
    @Override
    public Licensing deactivate(Long id) {
        Optional<Licensing> existingLicensing = licensingRepository.findById(id);
        if (existingLicensing.isPresent()) {
        	Licensing l = existingLicensing.get();
        	if (Boolean.TRUE.equals(l.getActive())) {
	            l.setActive(Boolean.FALSE);
	            l.setModified(LocalDateTime.now(clock));
	            l = licensingRepository.saveAndFlush(l);
	            invalidateLicensingCache(l.getUserLogin());
	            return l;
        	}
        }
        return existingLicensing.orElse(null);
    }
    
    private String getUsername() {
    	return securityContext.getUsername();
    }
    
    private List<Licensing> getExistingLicensings(String userLogin) {
    	return licensingRepository.findByUserLogin(userLogin);
    }

    private void invalidateLicensingCache(String userLogin) {
    	licensingCache.invalidate(userLogin);
    }
    
}
