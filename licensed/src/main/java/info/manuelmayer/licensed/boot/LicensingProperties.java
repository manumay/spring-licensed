package info.manuelmayer.licensed.boot;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "licensing")
@Validated
public class LicensingProperties {

	/**
	 * Enable automatic assignment of device licenses.
	 */
	@NotNull
    private boolean autoAssignDeviceLicenses;
    
	/**
	 * Enable automatic assignment of user licenses.
	 */
    @NotNull
    private boolean autoAssignUserLicenses;
	
	/**
	 * Expire licensing cache for if not access since X seconds.
	 */
    @NotNull
	private long cacheExpireAfterAccessInS;
    
    /**
	 * Get maximum cache size.
	 */
    @NotNull
	private int cacheMaximumSize;
	
	/**
	 * Refresh licensing cache automatically after X seconds.
	 */
    @NotNull
	private long cacheRefreshAfterWriteInS;
    
    /**
     * The license resource to use.
     */
    @NotNull
    private List<Resource> licenses;
    
    /**
     * Get the licensing version.
     * @return version
     */
    @NotNull
    private String version;

    
    public boolean isAutoAssignDeviceLicenses() {
        return autoAssignDeviceLicenses;
    }
    
    public void setAutoAssignDeviceLicenses(boolean autoAssignDeviceLicenses) {
        this.autoAssignDeviceLicenses = autoAssignDeviceLicenses;
    }
    
    public boolean isAutoAssignUserLicenses() {
        return autoAssignUserLicenses;
    }
    
    public void setAutoAssignUserLicenses(boolean autoAssignUserLicenses) {
        this.autoAssignUserLicenses = autoAssignUserLicenses;
    }
    
	public long getCacheExpireAfterAccessInS() {
		return cacheExpireAfterAccessInS;
	}
	
	public void setCacheExpireAfterAccessInS(long cacheExpireAfterAccessInS) {
		this.cacheExpireAfterAccessInS = cacheExpireAfterAccessInS;
	}
	
	public int getCacheMaximumSize() {
		return cacheMaximumSize;
	}
	
	public void setCacheMaximumSize(int cacheMaximumSize) {
		this.cacheMaximumSize = cacheMaximumSize;
	}
	
	public long getCacheRefreshAfterWriteInS() {
		return cacheRefreshAfterWriteInS;
	}
	
	public void setCacheRefreshAfterWriteInS(long cacheRefreshAfterWriteInS) {
		this.cacheRefreshAfterWriteInS = cacheRefreshAfterWriteInS;
	}
    
    public List<Resource> getLicenses() {
		return licenses;
	}
    
    public void setLicenses(List<Resource> licenses) {
		this.licenses = licenses;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

}
