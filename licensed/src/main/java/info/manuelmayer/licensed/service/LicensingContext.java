package info.manuelmayer.licensed.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import info.manuelmayer.licensed.model.Licensing;

public class LicensingContext {

	@Autowired
	private LicensingCache licensingCache;
	
	public boolean isLicensed(String username, String key) {
		Map<String,Licensing> licensings = licensingCache.get(username);
		return licensings != null 
				&& licensings.containsKey(key)
				&& licensings.get(key).getActive();
	}

}
