package info.manuelmayer.licensed.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import info.manuelmayer.licensed.io.LicenseReaderException;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.LicenseConstants;
import info.manuelmayer.licensed.model.LicenseUtil;

public final class LicenseWriterImpl {
    
    private String dataAlgorithm = LicenseConstants.DEFAULT_DATA_ALGORITHM;
    private Integer dataLength = LicenseConstants.DEFAULT_DATA_LENGTH;
    private String dataTransformation = LicenseConstants.DEFAULT_DATA_TRANSFORMATION;
    private String keyAlgorithm = LicenseConstants.DEFAULT_KEY_ALGORITHM;
    private String keyEncryptKey = LicenseConstants.DEFAULT_KEY_ENCRYPT_KEY_RESOURCE;
    private String keyTransformation = LicenseConstants.DEFAULT_KEY_TRANSFORMATION;
    private String zipEntryData = LicenseConstants.DEFAULT_ZIP_ENTRY_DATA;
    private String zipEntryKey = LicenseConstants.DEFAULT_ZIP_ENTRY_KEY;

    public void write(License license, File file) {
        // generate symmetric key (default: AES)
        SecretKey symmetricKey = generateSymmetricKey();
        byte[] encodedKey = symmetricKey.getEncoded();
        
        // encrypt properties with symmetric key
        Properties properties = LicenseUtil.getProperties(license);
        Pair<byte[],byte[]> encrypted = encryptSymmetric(properties, symmetricKey);
        byte[] encryptedData = Base64.getEncoder().encode(encrypted.getRight());
                
        // encrypt symmetric key with asymmetric key
        PrivateKey privateKey = readPrivateKey();
        byte[] key = ArrayUtils.addAll(encrypted.getLeft(), encodedKey);
        byte[] encryptedKey = Base64.getEncoder().encode(encryptAsymmetric(key, privateKey));
        
        // write a zip file containing the symmetrically encrypted properties and
        // the asymmetrically encrypted keys
        writeZip(file, encryptedKey, encryptedData);
    }
    
    private SecretKey generateSymmetricKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(getDataAlgorithm());
            keygen.init(getDataLength());
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private Pair<byte[],byte[]> encryptSymmetric(Properties properties, SecretKey key) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            properties.store(out, "");
            Cipher cipher = Cipher.getInstance(getDataTransformation());
            byte[] iv = new SecureRandom().generateSeed(cipher.getBlockSize());
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            return Pair.of(iv, cipher.doFinal(out.toByteArray()));
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException |
                InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException |
                IllegalBlockSizeException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private PrivateKey readPrivateKey() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyAlgorithm());
            byte[] keyData = IOUtils.toByteArray(LicenseWriterImpl.class.getResourceAsStream(getKeyEncryptKey()));
            keyData = Base64.getDecoder().decode(keyData);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyData));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private byte[] encryptAsymmetric(byte[] key, PrivateKey privateKey) {
        try {
            Cipher c = Cipher.getInstance(getKeyTransformation());
            c.init(Cipher.ENCRYPT_MODE, privateKey);
            return c.doFinal(key);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | 
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new LicenseReaderException(e);
        }
    }
    
    private void writeZip(File file, byte[] encryptedKey, byte[] encryptedData) {
        try (FileOutputStream out = new FileOutputStream(file);
                ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(getZipEntryKey()));
            IOUtils.write(encryptedKey, zip);
            zip.putNextEntry(new ZipEntry(getZipEntryData()));
            IOUtils.write(encryptedData, zip);
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
    
    public String getKeyEncryptKey() {
        return keyEncryptKey;
    }
    
    public void setKeyEncryptKey(String keyEncryptKey) {
        this.keyEncryptKey = keyEncryptKey;
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
