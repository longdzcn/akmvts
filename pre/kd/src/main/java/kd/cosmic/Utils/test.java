package kd.cosmic.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class test {
    public static void main(String[] args) {
        String input = "bos_org_duty[masterid,name,number][1, 1, 管理, [locale[YGLAABK8TQD, zh_CN, 管理], locale[YGLAABK8TQE, zh_TW, 管理]], 1]";
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher matcher = pattern.matcher(input);

        // 使用StringBuilder来存储前两个中文字符
        StringBuilder chineseCharacters = new StringBuilder();
        int count = 0;
        while (matcher.find() && count < 2) {
            chineseCharacters.append(matcher.group());
            count++;
        }

        String result = chineseCharacters.toString();
        System.out.print(result);
    }
}
