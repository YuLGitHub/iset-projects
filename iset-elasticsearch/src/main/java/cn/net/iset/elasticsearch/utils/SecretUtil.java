package cn.net.iset.elasticsearch.utils;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

/**
 * ClassName: SecretUtil.java
 * Description:
 *
 * @author yule1@cloudwalk.com
 * @date 2020/11/17
 */
public class SecretUtil {

    /**
     * 生成SHA3-512数据
     * @param key
     * @return
     */
    public static String generateSHA3512(String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        Digest digest = new SHA3Digest(512);
        digest.update(bytes, 0, bytes.length);
        byte[] rsData = new byte[digest.getDigestSize()];
        digest.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }
}
