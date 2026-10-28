import com.android.apksig.ApkSigner;
import java.io.*;import java.security.*;import java.security.cert.X509Certificate;import java.util.*;
public class Sign{public static void main(String[] a)throws Exception{
 KeyStore ks=KeyStore.getInstance("PKCS12");ks.load(new FileInputStream(a[2]),"jzsjzs".toCharArray());
 PrivateKey k=(PrivateKey)ks.getKey("jzs","jzsjzs".toCharArray());X509Certificate c=(X509Certificate)ks.getCertificate("jzs");
 ApkSigner.SignerConfig sc=new ApkSigner.SignerConfig.Builder("JZS",k,Collections.singletonList(c)).build();
 new ApkSigner.Builder(Collections.singletonList(sc)).setInputApk(new File(a[0])).setOutputApk(new File(a[1]))
  .setV1SigningEnabled(true).setV2SigningEnabled(true).setV3SigningEnabled(true).build().sign();
 System.out.println("signed");}}
