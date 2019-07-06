package info.manuelmayer.licensed.model;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class LicenseImpl implements License {

	private String applicationKey = "";
    private String holder = "";
    private String issuer = "";
    private LocalDate issueDate = null;
    private LicenseRestrictionsImpl restrictions = new LicenseRestrictionsImpl();
    
    @Override
    public String getApplicationKey() {
    	return applicationKey;
    }
    
    public void setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
	}

    @Override
    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    @Override
    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public LicenseRestrictionsImpl getRestrictions() {
        return restrictions;
    }

    public static class LicenseRestrictionsImpl implements LicenseRestrictions {

        private Pattern features = Pattern.compile(".*");
        private Pattern hosts = Pattern.compile(".*");
        private Integer numberOfDevices = Integer.MAX_VALUE;
        private Integer numberOfUsers = Integer.MAX_VALUE;
        private LocalDate validFrom = LocalDate.MIN;
        private LocalDate validTill = LocalDate.MAX;
        private Pattern versions = Pattern.compile(".*");

        @Override
        public Pattern getFeatures() {
            return features;
        }

        public void setFeatures(Pattern features) {
            this.features = features;
        }
        
        @Override
        public Pattern getHosts() {
            return hosts;
        }
        
        public void setHosts(Pattern hosts) {
            this.hosts = hosts;
        }

        @Override
        public Integer getNumberOfDevices() {
            return numberOfDevices;
        }

        public void setNumberOfDevices(Integer numberOfDevices) {
            this.numberOfDevices = numberOfDevices;
        }

        @Override
        public Integer getNumberOfUsers() {
            return numberOfUsers;
        }

        public void setNumberOfUsers(Integer numberOfUsers) {
            this.numberOfUsers = numberOfUsers;
        }

        @Override
        public LocalDate getValidFrom() {
            return validFrom;
        }
        
        public void setValidFrom(LocalDate validFrom) {
            this.validFrom = validFrom;
        }

        @Override
        public LocalDate getValidTill() {
            return validTill;
        }
        
        public void setValidTill(LocalDate validTill) {
            this.validTill = validTill;
        }

        @Override
        public Pattern getVersions() {
            return versions;
        }
        
        public void setVersions(Pattern versions) {
            this.versions = versions;
        }

    }

}
