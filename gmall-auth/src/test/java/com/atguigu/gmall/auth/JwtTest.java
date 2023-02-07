package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
@SpringBootTest
public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "E:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "E:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2NzU2ODk5NTF9.ZLBlvGjrel_alOiPjgwKZUPnsN4JxJ-LLyF-Yp-WS4_Z9wMLsIYzNgdHsnJ5sWsdkVdm2UqQn6bswEMBE1EXINKutfiCwyxm_61I4Tj8JtYFG07700VsZxbvEL_PcxgrnpvbxowBHduMEEa65fbvHu3x2_goNIsF7Gf_k20w1PAeb5AxMRc9n3fW_5fvDc1c54RrNMNwTkrLjkkDoDTzwyC3J7Ulz4yKqFB3EwC3efAPPq4CgqCrLZBz91F8ZMN0x-cITY6kSu-WGyhkKZxqUJ62fRksgPXHUV3Y4Xy4fgxnsUt-Rvn16IeWPciGQPmhnZLDnMoz5aGUDYuOZ1SgpQ";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}