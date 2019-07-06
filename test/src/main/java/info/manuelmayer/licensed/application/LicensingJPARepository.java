package info.manuelmayer.licensed.application;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import info.manuelmayer.licensed.model.Licensing;
import info.manuelmayer.licensed.service.LicensingRepository;

public interface LicensingJPARepository extends JpaRepository<Licensing, Long>,LicensingRepository {

	@Override
    Long countByLicenseKeyAndActiveTrueAndDeviceNotNull(String licenseKey);
    
	@Override
    Long countByLicenseKeyAndActiveTrueAndUserLoginNotNull(String licenseKey);
    
	@Override
    List<Licensing> findByUserLogin(String userLogin);
    
    @Override
    Licensing findByLicenseKeyAndActiveTrueAndDevice(String licenseKey,  String device);

    @Override
	Licensing findByLicenseKeyAndActiveTrueAndUserLogin(String licenseKey, String userLogin);

    @Override
    List<Licensing> findByActiveTrueAndUserLogin(String userLogin);
    
}
