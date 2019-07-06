package info.manuelmayer.licensed.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Base64;

public final class LicenseKeyPairGenerator {
    
    private LicenseKeyPairGenerator() {
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096); // 4096 / 8 - 11 = 501 Bytes max length of license
        
        KeyPair keyPair = keyGen.generateKeyPair();
        
        File publicKeyFile = new File("src/main/resources/decrypt.key");
        File privateKeyFile = new File("src/main/resources/encrypt.key");
        if (privateKeyFile.exists() || publicKeyFile.exists()) {
            throw new RuntimeException("private or public key already exists, you should not replace them unless you exactly know what your're doing");
        }
        
        try (FileOutputStream out = new FileOutputStream(privateKeyFile)){
            PrivateKey privateKey = keyPair.getPrivate();
            out.write(Base64.getEncoder().encode(privateKey.getEncoded()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try (FileOutputStream out = new FileOutputStream(publicKeyFile)){
            PublicKey publicKey = keyPair.getPublic();
            out.write(Base64.getEncoder().encode(publicKey.getEncoded()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
