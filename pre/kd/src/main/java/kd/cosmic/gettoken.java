package kd.cosmic;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 时数调用，与myhr同步用
 * 开发者: 易佳伟
 * 创建日期: 1期完成
 * 关键客户：无
 * 已部署正式：ture
 * 备注：无
 */

public class gettoken {


    private static final String SECRET_KEY ="your-256-bit-secret" ;

    public static String generateJWT(String userid) { //时数调用
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Map<String, Object> data = new HashMap<>();
        data.put("OP_CODE", "1910180001");
        data.put("OP_COCODE", "akmmv");
        data.put("USER_ID", userid);//
        data.put("VERI_TYPE", "5");//
        Date day=new Date();
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sTARTTIME=sdf.format(day);
        data.put("STARTTIME", sTARTTIME);//
        data.put("RTN_DS_NO", "RDS_R_2104250001_101");//
        data.put("SYSTEM_NO", "100001");

        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);

        // 设置JWT的过期时间为1小时
        long expirationTimeMillis = currentTimeMillis + 9000000;
        Date expirationDate = new Date(expirationTimeMillis);
        Map header = new HashMap();
        header.put("alg","HS256");
        // header.put("type","JWT");

        Key signingKey = new SecretKeySpec(SECRET_KEY.getBytes(), signatureAlgorithm.getJcaName());


        JwtBuilder builder = Jwts.builder()
                .setClaims(data)
                .setIssuedAt(now)
                .setHeader(header)
                .setExpiration(expirationDate)
                .signWith(signatureAlgorithm, signingKey);
        return builder.compact();
        //String jtw1 = Base64.getEncoder().encodeToString(tt.getBytes(StandardCharsets.UTF_8));
        //return tt;
    }


    public static String generateJWT102(String userId) { //管控验证调用
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Map<String, Object> data = new HashMap<>();
        data.put("OP_CODE", "1910180001");
        data.put("OP_COCODE", "akmmv");
        data.put("USER_ID", userId);//
        data.put("VERI_TYPE", "5");//
        Date day=new Date();
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdTARTTIME=sdf.format(day);
        data.put("STARTTIME", sdTARTTIME);//
        data.put("RTN_DS_NO", "RDS_R_2104250001_102");//
        data.put("SYSTEM_NO", "100001");

        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);

        // 设置JWT的过期时间为1小时
        long expirationTimeMillis = currentTimeMillis + 9000000;
        Date expirationDate = new Date(expirationTimeMillis);
        Map header = new HashMap();
        header.put("alg","HS256");
        // header.put("type","JWT");

        Key signingKey = new SecretKeySpec(SECRET_KEY.getBytes(), signatureAlgorithm.getJcaName());


        JwtBuilder builder = Jwts.builder()
                .setClaims(data)
                .setIssuedAt(now)
                .setHeader(header)
                .setExpiration(expirationDate)
                .signWith(signatureAlgorithm, signingKey);
        return builder.compact();
        //String jtw1 = Base64.getEncoder().encodeToString(tt.getBytes(StandardCharsets.UTF_8));
        //return tt;

    }







}