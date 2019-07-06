package info.manuelmayer.licensed.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Licensing {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false, unique=false)
    private Boolean active;

    @Column(nullable=true, unique=false)
    private String device;
    
    @Column(nullable=false, unique=false)
    private String licenseKey;
    
    @Column(nullable=false, unique=false)
    private LocalDateTime modified;
    
    @Column(nullable=true, unique=false)
    private String userLogin;
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getDevice() {
        return device;
    }
    
    public void setDevice(String device) {
        this.device = device;
    }
    
    public String getLicenseKey() {
		return licenseKey;
	}
    
    public void setLicenseKey(String key) {
		this.licenseKey = key;
	}
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getModified() {
        return modified;
    }
    
    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }
    
    public String getUserLogin() {
		return userLogin;
	}
    
    public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
}