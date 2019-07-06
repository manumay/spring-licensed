package info.manuelmayer.licensed.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.LicenseConstants;
import info.manuelmayer.licensed.model.LicenseUtil;

public class LicenseReaderImpl implements LicenseReader {
    
    private String dataAlgorithm = LicenseConstants.DEFAULT_DATA_ALGORITHM;
    private Integer dataLength = LicenseConstants.DEFAULT_DATA_LENGTH;
    private String dataTransformation = LicenseConstants.DEFAULT_DATA_TRANSFORMATION;
    private String keyAlgorithm = LicenseConstants.DEFAULT_KEY_ALGORITHM;
    private String keyDecryptKeyResource = LicenseConstants.DEFAULT_KEY_DECRYPT_KEY_RESOURCE;
    private String keyTransformation = LicenseConstants.DEFAULT_KEY_TRANSFORMATION;
    private String zipEntryData = LicenseConstants.DEFAULT_ZIP_ENTRY_DATA;
    private String zipEntryKey = LicenseConstants.DEFAULT_ZIP_ENTRY_KEY;

    @Override
    public License read(File file) {
        try (ZipFile zip = new ZipFile(file);
                InputStream keyIn = zip.getInputStream(new ZipEntry(getZipEntryKey()));
                InputStream dataIn = zip.getInputStream(new ZipEntry(getZipEntryData()));) {
            byte[] key = IOUtils.toByteArray(keyIn);
            byte[] data = IOUtils.toByteArray(dataIn);
            return read(key, data);
        } catch (IOException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    @Override
    public License read(InputStream in) {
        try (ZipInputStream zip = new ZipInputStream(in);
                ByteArrayOutputStream keyOut = new ByteArrayOutputStream();
                ByteArrayOutputStream dataOut = new ByteArrayOutputStream();) {
            
            byte[] buffer = new byte[1024];
            ZipEntry entry = null;
            while((entry = zip.getNextEntry()) != null) {
                int len = 0;
                while ((len = zip.read(buffer)) > 0) {
                    if (entry.getName().equals(getZipEntryKey())) {
                        keyOut.write(buffer, 0, len);
                    } else if (entry.getName().equals(getZipEntryData())) {
                        dataOut.write(buffer, 0, len);
                    }
                }
            }
            return read(keyOut.toByteArray(), dataOut.toByteArray());
        } catch (IOException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    public License read(byte[] key, byte[] data) {
        // decrypt the symmetric encryption key using the public key of the asymmetric encryption algorithm
        PublicKey publicKey = readPublicKey();
        byte[] encryptedKey = Base64.getDecoder().decode(key);
        byte[] encodedKey = decryptAsymmetric(encryptedKey, publicKey);
        
        // decrypt the data using the decrypted symmetric key
        byte[] encryptedData = Base64.getDecoder().decode(data);
        byte[] decryptedData = decryptSymmetric(encryptedData, encodedKey);
        
        // convert byte[] to Properties
        Properties props = loadProperties(decryptedData);
        return LicenseUtil.getLicenseDetails(props);
    }
    
    private PublicKey readPublicKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm());
            byte[] keyData = IOUtils.toByteArray(LicenseReaderImpl.class.getResourceAsStream(getKeyDecryptKeyResource()));
            keyData = Base64.getDecoder().decode(keyData);
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyData));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private byte[] decryptAsymmetric(byte[] data, PublicKey publicKey) {
        try {
            Cipher c = Cipher.getInstance(getKeyTransformation());
            c.init(Cipher.DECRYPT_MODE, publicKey);
            return c.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | 
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private byte[] decryptSymmetric(byte[] encryptedData, byte[] encodedKey) {
        try {
            Cipher cipher = Cipher.getInstance(getDataTransformation());
            byte[] iv = ArrayUtils.subarray(encodedKey, 0, cipher.getBlockSize());
            byte[] keyData = ArrayUtils.subarray(encodedKey, cipher.getBlockSize(), encodedKey.length);
            SecretKey key = getSymmetricKey(keyData);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return cipher.doFinal(encryptedData);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |  InvalidKeyException | InvalidAlgorithmParameterException |
                BadPaddingException | IllegalBlockSizeException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private SecretKey getSymmetricKey(byte[] key) {
        return new SecretKeySpec(key, getDataAlgorithm());
    }
    
    private Properties loadProperties(byte[] data) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    public String getDataAlgorithm() {
        return dataAlgorithm;
    }
    
    public void setDataAlgorithm(String dataAlgorithm) {
        this.dataAlgorithm = dataAlgorithm;
    }
    
    public Integer getDataLength() {
        return dataLength;
    }
    
    public void setDataLength(Integer dataLength) {
        this.dataLength = dataLength;
    }
    
    public String getDataTransformation() {
        return dataTransformation;
    }
    
    public void setDataTransformation(String dataTransformation) {
        this.dataTransformation = dataTransformation;
    }
    
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }
    
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }
    
    public String getKeyDecryptKeyResource() {
        return keyDecryptKeyResource;
    }
    
    public void setKeyDecryptKeyResource(String keyDecryptKeyResource) {
        this.keyDecryptKeyResource = keyDecryptKeyResource;
    }
    
    public String getKeyTransformation() {
        return keyTransformation;
    }
    
    public void setKeyTransformation(String keyTransformation) {
        this.keyTransformation = keyTransformation;
    }
    
    public String getZipEntryData() {
        return zipEntryData;
    }
    
    public void setZipEntryData(String zipEntryData) {
        this.zipEntryData = zipEntryData;
    }
    
    public String getZipEntryKey() {
        return zipEntryKey;
    }
    
    public void setZipEntryKey(String zipEntryKey) {
        this.zipEntryKey = zipEntryKey;
    }
}
