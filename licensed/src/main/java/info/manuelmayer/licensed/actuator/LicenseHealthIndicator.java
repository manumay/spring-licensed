package info.manuelmayer.licensed.actuator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import info.manuelmayer.licensed.violation.LicenseChecker;
import info.manuelmayer.licensed.violation.LicenseChecker.LicenseCheck;

import org.springframework.boot.actuate.health.Status;;

public class LicenseHealthIndicator extends AbstractHealthIndicator {
    
    @Autowired
    private LicenseChecker licenseChecker;
    
    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        Map<String,List<LicenseCheck>> checks = licenseChecker.checkAll();
        boolean unhealthy = checks.entrySet().stream()
        		.flatMap(e -> e.getValue().stream())
                .map(LicenseCheck::isExhausted)
                .collect(Collectors.reducing((b1,b2) -> b1 || b2 ))
                .orElse(Boolean.TRUE);
        
        Builder b = builder.status(unhealthy ? Status.DOWN : Status.UP);
        
        for (Entry<String,List<LicenseCheck>> licenseCheckEntry : checks.entrySet()) {
        	String licenseKey = licenseCheckEntry.getKey();
        	List<LicenseCheck> checkResults = licenseCheckEntry.getValue();
        	b.withDetail(licenseKey, toMap(checkResults));
        }
    }
    
    private Map<String,String> toMap(List<LicenseCheck> checks) {
    	Map<String,String> details = new HashMap<>();
    	 for (LicenseCheck check : checks) {
             details.put(check.getScope(), check.getDetails());
         }
    	return details;
    }

}
