package info.manuelmayer.licensed.model;

import java.time.LocalDate;
import java.util.regex.Pattern;

public interface License {
	
	String getApplicationKey();
    
    String getHolder();

    String getIssuer();
    
    LocalDate getIssueDate();
    
    LicenseRestrictions getRestrictions();
    
    
    interface LicenseRestrictions {
        
        Pattern getFeatures();
        
        Pattern getHosts();
        
        Integer getNumberOfDevices();
        
        Integer getNumberOfUsers();
        
        LocalDate getValidFrom();
        
        LocalDate getValidTill();
        
        Pattern getVersions();
        
    }
    
}
