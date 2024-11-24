package kd.cosmic.Test.ZS;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class gettoken {



    private static final String SECRET_KEY ="2ff16168e29e11e98745ec0d9a495454" ;
    public static String generateJWT(String userId) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Map<String, Object> data = new HashMap<>();
        data.put("OP_CODE", "1910180001");
        data.put("OP_COCODE", "akmmv");
        data.put("USER_ID", userId);//
        data.put("VERI_TYPE", "5");//
        Date day=new Date();
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dSTARTTIME=sdf.format(day);
        data.put("STARTTIME", dSTARTTIME);//
        data.put("RTN_DS_NO", "RDS_R_1220617001_101");//
        data.put("SYSTEM_NO", "100001");

        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);

        // 设置JWT的过期时间为1小时
        long expirationTimeMillis = currentTimeMillis + 9000000;
        Date expirationDate = new Date(expirationTimeMillis);
        Map header = new HashMap();
        header.put("alg","HS256");
        header.put("typ","JWT");

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


    public static String generateJWT102(String userId) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Map<String, Object> data = new HashMap<>();
        data.put("OP_CODE", "1910180001");
        data.put("OP_COCODE", "akmmv");
        data.put("USER_ID", userId);//
        data.put("VERI_TYPE", "5");//
        Date day=new Date();
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String aSTARTTIME=sdf.format(day);
        data.put("STARTTIME", aSTARTTIME);//
        data.put("RTN_DS_NO", "RDS_R_1220617001_102");//
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