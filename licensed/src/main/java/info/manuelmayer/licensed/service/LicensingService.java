package info.manuelmayer.licensed.service;

import info.manuelmayer.licensed.model.Licensing;

public interface LicensingService {
    
    Licensing activate(Long id);
    
    Licensing assignLicenseToCurrentDevice(String key);
    
    Licensing assignLicenseToCurrentUser(String key);

    Licensing assignUserLicense(String key, String userLogin);
    
    Licensing deactivate(Long id);
    
}
