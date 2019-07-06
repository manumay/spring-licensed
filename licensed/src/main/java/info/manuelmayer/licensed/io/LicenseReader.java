package info.manuelmayer.licensed.io;

import java.io.File;
import java.io.InputStream;

import info.manuelmayer.licensed.model.License;

public interface LicenseReader {

    License read(File file) throws LicenseReaderException;
    
    License read(InputStream in) throws LicenseReaderException;
    
}
