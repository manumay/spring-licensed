package info.manuelmayer.licensed.test;

import info.manuelmayer.licensed.annotation.Licensed;

public class LicensedObjectImpl implements LicensedObject {
	
	private LicensedCode code;
	
	public LicensedObjectImpl(LicensedCode code) {
		this.code = code;
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, currentDevice=true)
	public void currentDevice() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, currentUser=true)
	public void currentUser() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, devices=true)
	public void devices() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, feature="licensed-feature")
	public void featureLicensed() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, feature="feature")
	public void featureUnlicensed() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, host=true)
	public void host() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, period=true)
	public void period() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, users=true)
	public void users() {
		code.invoked();
	}
	
	@Override
	@Licensed(key=LicensingTestConstants.APPLICATION_KEY, version=true)
	public void version() {
		code.invoked();
	}
	
}
