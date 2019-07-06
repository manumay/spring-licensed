package info.manuelmayer.licensed.service;

import java.util.List;
import java.util.Optional;

import info.manuelmayer.licensed.model.Licensing;

public interface LicensingRepository {
	
	Long countByLicenseKeyAndActiveTrueAndDeviceNotNull(String licenseKey);
	
	Long countByLicenseKeyAndActiveTrueAndUserLoginNotNull(String licenseKey);
	
	Optional<Licensing> findById(Long id);
	
	Licensing findByLicenseKeyAndActiveTrueAndDevice(String licenseKey, String device);

	Licensing findByLicenseKeyAndActiveTrueAndUserLogin(String licenseKey, String userLogin);
	
	List<Licensing> findByActiveTrueAndUserLogin(String userLogin);
	
	List<Licensing> findByUserLogin(String userLogin);
	
	Licensing saveAndFlush(Licensing licensing);

}
