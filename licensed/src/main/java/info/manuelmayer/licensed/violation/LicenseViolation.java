package info.manuelmayer.licensed.violation;

public class LicenseViolation {

    private static final String DEVICE_LICENSE_MISSING = "device %s is not licensed or license is inactive";
    private static final String MAX_DEVICES_REACHED = "maximum devices reached (%s/%s)";
    private static final String MAX_USERS_REACHED = "maximum users reached (%s/%s)";
    private static final String PERIOD_EXPIRED = "period expired";
    private static final String UNLICENSED_FEATURE = "feature %s is not licensed";
    private static final String UNLICENSED_HOST = "host %s is not licensed";
    private static final String UNLICENSED_VERSION = "version %s is not licensed";
    private static final String USER_LICENSE_MISSING = "user %s is not licensed or license is inactive";
    
    private final String message;
    
    private LicenseViolation(String message, Object ... replace) {
        this.message = String.format(message, replace);
    }
    
    public String getMessage() {
        return message;
    }
    
    public static final LicenseViolation deviceLicenseMissing(String deviceId) {
        return new LicenseViolation(DEVICE_LICENSE_MISSING, deviceId);
    }
    
    public static final LicenseViolation maxDevicesReached(long current, long max) {
        return new LicenseViolation(MAX_DEVICES_REACHED, current, max);
    }
    
    public static final LicenseViolation maxUsersReached(long current, long max) {
        return new LicenseViolation(MAX_USERS_REACHED, current, max);
    }
    
    public static final LicenseViolation periodExpired() {
        return new LicenseViolation(PERIOD_EXPIRED);
    }
    
    public static final LicenseViolation unlicensedFeature(String feature) {
        return new LicenseViolation(UNLICENSED_FEATURE, feature);
    }
    
    public static final LicenseViolation unlicensedHost(String host) {
        return new LicenseViolation(UNLICENSED_HOST, host);
    }
    
    public static final LicenseViolation unlicensedVersion(String version) {
        return new LicenseViolation(UNLICENSED_VERSION, version);
    }
    
    public static final LicenseViolation userLicenseMissing(String user) {
        return new LicenseViolation(USER_LICENSE_MISSING, user);
    }
    
}
