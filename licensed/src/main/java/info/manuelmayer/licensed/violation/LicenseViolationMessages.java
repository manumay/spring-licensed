package info.manuelmayer.licensed.violation;

public final class LicenseViolationMessages {

	public static final String DEVICE_LICENSED = "device %s licensed";
	public static final String DEVICE_UNLICENSED = "device %s not licensed";
	public static final String HOST_LICENSED = "host %s licensed";
	public static final String HOST_UNLICENSED = "host %s not licensed";
	public static final String TIMEPERIOD_UNLICENSED = "invalid since %s years %s months %s days";
	public static final String TIMEPERIOD_LICENSED = "valid for %s years %s months %s days";
	public static final String USER_LICENSED = "user %s licensed";
	public static final String USER_UNLICENSED = "user %s not licensed";
	public static final String USERS_LICENSED = "user licenses ok (%s/%s)";
	public static final String USERS_UNLICENSED = "no more user licenses available (%s/%s)";
	public static final String VERSION_LICENSED = "version %s is licensed";
	public static final String VERSION_UNLICENSED = "version %s is not licensed";
	
	private LicenseViolationMessages() {
	}

}
