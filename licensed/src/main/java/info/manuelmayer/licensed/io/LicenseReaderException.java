package info.manuelmayer.licensed.io;

public class LicenseReaderException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public LicenseReaderException() {
    }
    
    public LicenseReaderException(String msg) {
        super(msg);
    }
    
    public LicenseReaderException(Throwable cause) {
        super(cause);
    }

}
