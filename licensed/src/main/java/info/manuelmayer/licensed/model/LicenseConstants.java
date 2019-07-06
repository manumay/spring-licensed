package info.manuelmayer.licensed.model;

public final class LicenseConstants {

    public static final String DEFAULT_DATA_ALGORITHM = "AES";
    public static final Integer DEFAULT_DATA_LENGTH = Integer.valueOf(128);
    public static final String DEFAULT_DATA_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final String DEFAULT_KEY_ALGORITHM = "RSA";
    public static final String DEFAULT_KEY_DECRYPT_KEY_RESOURCE = "/decrypt.key";
    public static final String DEFAULT_KEY_ENCRYPT_KEY_RESOURCE = "/encrypt.key";
    public static final String DEFAULT_KEY_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    public static final String DEFAULT_ZIP_ENTRY_DATA = "license";
    public static final String DEFAULT_ZIP_ENTRY_KEY = "key";
    
    private LicenseConstants() {
    }
    
}
