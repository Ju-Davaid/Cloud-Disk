package ju.pioneer.cloud_disk.utils;

import ju.pioneer.cloud_disk.entity.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtils {
    /**
     * 验证字符串是否符合正则表达式
     * @param regs 正则表达式
     * @param value 字符串值
     * @return 是否符合正则表达式
     */
    public static boolean verify(String regs,String value){
        if(StringTools.isEmpty(value)) return false;
        Pattern pattern = Pattern.compile(regs);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    /**
     * 验证字符串是否符合正则表达式
     * @param verifyRegexEnum 正则表达式枚举
     * @param value 字符串值
     * @return 是否符合正则表达式
     */
    public static boolean verify(VerifyRegexEnum verifyRegexEnum, String value){
       return  verify(verifyRegexEnum.getRegex(),value);
    }
}
