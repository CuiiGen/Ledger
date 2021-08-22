package tool;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA {

	/**
	 * @return 公钥和私钥组成的字符串数组
	 * @throws NoSuchAlgorithmException
	 */
	public static String[] genKeyPair() throws NoSuchAlgorithmException {
		// KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		// 初始化密钥对生成器密钥大小为96-1024位
		keyPairGen.initialize(1024, new SecureRandom());
		// 生成一个密钥对保存在keyPair中
		KeyPair keyPair = keyPairGen.generateKeyPair();
		// 得到私钥
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// 得到公钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		// BASE64编码器
		Encoder encoder = Base64.getEncoder();
		// 将秘钥和公钥编码后转为字符串
		String publicKeyString = encoder.encodeToString(publicKey.getEncoded());
		String privateKeyString = encoder.encodeToString(privateKey.getEncoded());
		String[] result = { publicKeyString, privateKeyString };
		return result;

	}

	public static String encrypt(String str, String publicKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		// base64解码公钥
		Decoder decoder = Base64.getDecoder();
		byte[] decoded = decoder.decode(publicKey);
		// 公钥
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(decoded));
		// RSA加密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] result = cipher.doFinal(str.getBytes("UTF-8"));
		// 结果用beas64编码并转为字符串
		Encoder encoder = Base64.getEncoder();
		String outStr = encoder.encodeToString(result);
		return outStr;
	}

	public static String decrypt(String str, String privateKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		// base64解码加密后的信息和秘钥
		Decoder decoder = Base64.getDecoder();
		byte[] inputByte = decoder.decode(str);
		byte[] decoded = decoder.decode(privateKey);
		RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		// RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr;
	}

}
