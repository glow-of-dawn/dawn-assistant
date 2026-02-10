package com.dawn.plugin.util;

import com.dawn.plugin.enmu.VarEnmu;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建时间：2022/4/1 21:56
 *
 * @author forest
 */
public class SensitiveUtil {

    /* [(王|李|张|刘|陈|杨|黄|赵|吴|周|徐|孙|马|朱|胡|郭|何|高|林|罗|郑|梁|谢|宋|唐|许|韩|冯|邓|曹|彭|曾|肖|田|董|袁|潘|于|蒋|蔡|余|杜
       |叶|程|苏|魏|吕|丁|任|沈|姚|卢|姜|崔|钟|谭|陆|汪|范|金|石|廖|贾|夏|韦|傅|方|白|邹|孟|熊|秦|邱|江|尹|薛|闫|段|雷|侯|龙|史|黎|贺
       |顾|毛|郝|龚|邵|万|钱|覃|武|戴|孔|汤|庞|樊|兰|殷|施|陶|洪|翟|安|颜|倪|严|牛|温|芦|季|俞|章|鲁|葛|伍|申|尤|毕|聂|柴|焦|向|柳|邢
       |岳|齐|沿|梅|莫|庄|辛|管|祝|左|涂|谷|祁|时|舒|耿|牟|卜|路|詹|关|苗|凌|费|纪|靳|盛|童|欧|甄|项|曲|成|游|阳|裴|席|卫|查|屈|鲍|位
       |覃|霍|翁|隋|植|甘|景|薄|单|包|司|柏|宁|柯|阮|桂|闵|欧阳|解|强|丛|华|车|冉|房|边|辜|吉|饶|刁|瞿|戚|丘|古|米|池|滕|晋|苑|邬|臧
       |畅|宫|来|嵺|苟|全|褚|廉|简|娄|盖|符|奚|木|穆|党|燕|郎|邸|冀|谈|姬|屠|连|郜|晏|栾|郁|商|蒙|计|喻|揭|窦|迟|宇|敖|糜|鄢|冷|卓|花
       |艾|蓝|都|巩|稽|井|练|仲|乐|虞|卞|封|竺|冼|原|官|衣|楚|佟|栗|匡|宗|应|台|巫|鞠|僧|桑|荆|谌|银|扬|明|沙|薄|伏|岑|习|胥|保|和|蔺
       |水|云|昌|凤|酆|常|皮|康|元|平|萧|湛|禹|无|贝|茅|麻|危|骆|支|咎|经|裘|缪|干|宣|贲|杭|诸|钮|嵇|滑|荣|荀|羊|於|惠|家|芮|羿|储|汲
       |邴|松|富|乌|巴|弓|牧|隗|山|宓|蓬|郗|班|仰|秋|伊|仇|暴|钭|厉|戎|祖|束|幸|韶|蓟|印|宿|怀|蒲|鄂|索|咸|籍|赖|乔|阴|能|苍|双|闻|莘
       |贡|逢|扶|堵|宰|郦|雍|却|璩|濮|寿|通|扈|郏|浦|尚|农|别|阎|充|慕|茹|宦|鱼|容|易|慎|戈|庚|终|暨|居|衡|步|满|弘|国|文|寇|广|禄|阙
       |东|殴|殳|沃|利|蔚|越|夔|隆|师|厍|晃|勾|融|訾|阚|那|空|毋|乜|养|须|丰|巢|蒯|相|后|红|权逯|盖益|桓|公|万俟|司马|上官|夏侯|诸葛
       |闻人|东方|赫连|皇甫|尉迟|公羊|澹台|公冶|宗政|濮阳|淳于|单于|太叔|申屠|公孙|仲孙|轩辕|令狐|钟离|宇文|长孙|慕容|鲜于|闾丘|司徒
       |司空|亓官|司寇|仉|督|子车|颛孙|端木|巫马|公西|漆雕|乐正|壤驷|公良|拓跋|夹谷|宰父|谷粱|法|汝|钦|段干|百里|东郭|南门|呼延|归海
       |羊舌|微生|帅|缑|亢|况|郈|琴|梁丘|左丘|东门|西门|佘|佴|伯|赏|南宫|墨|哈|谯|笪|年|爱|仝|代)[\u4e00-\u9fa5.·\u36c3\u4DAE]{2,3}] */
    /* 姓名正则(百家姓) 剔除常用字 */
    public static final String NAME_REGEX = """
        (王|李|张|刘|陈|杨|赵|吴|周|徐|孙|马|朱|胡|郭|林|罗|郑|梁|谢|宋|唐|许|韩|冯
        |邓|曹|彭|肖|董|袁|潘|蒋|蔡|杜|叶|苏|魏|吕|丁|沈|姚|卢|姜|谭|汪|范|廖|贾|韦|傅|邹|孟|熊|秦|邱|尹|薛|闫|雷
        |侯|龙|黎|贺|郝|龚|邵|覃|戴|汤|庞|樊|兰|殷|施|陶|翟|颜|倪|芦|俞|鲁|葛|伍|尤|聂|柴|柳|邢|岳|梅|莫|辛|祝|涂
        |祁|耿|牟|卜|詹|苗|靳|童|欧|甄|游|裴|席|屈|鲍|覃|霍|翁|隋|薄|柏|柯|阮|桂|闵|欧阳|丛|辜|吉|饶|刁|瞿|戚|丘
        |滕|晋|苑|邬|臧|畅|宫|嵺|苟|褚|廉|简|娄|奚|穆|党|燕|郎|邸|冀|谈|姬|屠|郜|晏|栾|郁|蒙|喻|揭|窦|迟|宇|敖|糜
        |鄢|卓|艾|巩|稽|仲|虞|卞|竺|楚|佟|栗|匡|巫|鞠|僧|桑|荆|谌|扬|沙|伏|岑|胥|蔺|酆|康|萧|湛|禹|茅|麻|骆|咎|裘
        |缪|贲|诸|钮|嵇|滑|荀|於|惠|芮|羿|汲|邴|牧|隗|宓|蓬|郗|仰|仇|钭|厉|戎|幸|韶|蓟|蒲|鄂|索|籍|赖|乔|苍|莘|宰
        |郦|雍|璩|濮|扈|郏|浦|阎|慕|茹|宦|慎|戈|庚|暨|衡|弘|寇|阙|殴|殳|蔚|夔|隆|訾|阚|毋|乜|巢|蒯|权逯|盖益|桓
        |万俟|司马|上官|夏侯|诸葛|闻人|赫连|皇甫|尉迟|公羊|澹台|公冶|宗政|濮阳|淳于|单于|太叔|申屠|公孙|仲孙|轩辕
        |令狐|钟离|宇文|长孙|慕容|鲜于|闾丘|司徒|司空|亓官|司寇|仉|子车|颛孙|端木|巫马|公西|漆雕|乐正|壤驷|公良
        |拓跋|夹谷|宰父|谷粱|汝|钦|段干|百里|东郭|南门|呼延|归海|羊舌|微生|缑|亢|况|郈|梁丘|左丘|东门|西门|佴|南宫
        |墨|谯)[\\u4e00-\\u9fa5]{1,2}(?:[\\u4e00-\\u9fa5]{1,2})*
        """;
    /* 手机号正则规则 */
    public static final String MOBILE_REGEX = "(?<!\\d)(1\\d{10})(?!\\d)";
    /* 身份证号正则规则 */
    public static final String ID_CARD_REGEX = "(?<!\\d)(\\d{6})([19,20]\\d{7})(\\d{3}[0-9Xx])(?!\\d)";
    /* 银行卡正则匹配式 */
    public static final String BANK_CARD_REGEX = "(?<!\\d)(([1-9]{1})(\\d{11}|\\d{14}|\\d{15}|\\d{16}|\\d{17}|\\d{18}))(?!\\d)";

    private SensitiveUtil() {
    }

    /**
     * 数据脱敏方法
     * 银行卡脱敏: DesensitizedUtil.bankCard()
     * 电话号脱敏: DesensitizedUtil.mobilePhone()
     * 身份证号脱敏: DesensitizedUtil.idCardNum()
     * 邮箱脱敏: DesensitizedUtil.email()
     * 姓名脱敏: DesensitizedUtil.chineseName()
     *
     * @param data 原始数据
     * @return 脱敏后的数据
     */
    public static String desensitization(String data) {
        if (Objects.isNull(data) || StringUtils.isBlank(data)) {
            return data;
        }
        var dat = SensitiveUtil.desensitization(data, SensitiveUtil.ID_CARD_REGEX,
            VarEnmu.FOUR.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
        dat = SensitiveUtil.desensitization(dat, SensitiveUtil.BANK_CARD_REGEX,
            VarEnmu.FOUR.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
        dat = SensitiveUtil.desensitization(dat, SensitiveUtil.MOBILE_REGEX,
            VarEnmu.THREE.ivalue(), VarEnmu.FOUR.ivalue(), VarEnmu.STAR.value());
        var nr = SensitiveUtil.NAME_REGEX.replace(VarEnmu.LF.value(), VarEnmu.NONE.value());
        dat = SensitiveUtil.desensitization(dat, nr, VarEnmu.ONE.ivalue(), VarEnmu.ZERO.ivalue(), VarEnmu.STAR.value());
        return dat;
    }

    /**
     * 数据脱敏方法
     *
     * @param data 原始数据
     * @param len  展示前 len 长度字符
     * @return 脱敏后的数据
     */
    public static String desensitization(String data, int len) {
        return desensitization(StringUtils.left(data, len));
    }

    /**
     * [根据正则匹配之后对数据进行脱敏]
     *
     * @param data           原始数据
     * @param showHeadLength 展示前几位
     * @param showTailLength 展示后几位
     * @param replaceChar    其他字符的替换
     * @param regular        正则规则
     * @return 脱敏后的数据
     */
    public static String desensitization(String data, String regular, int showHeadLength, int showTailLength, String replaceChar) {
        Pattern pattern = Pattern.compile(regular);
        Matcher matcher = pattern.matcher(data);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, dataDesensitization(matcher.group(), showHeadLength, showTailLength, replaceChar));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * [对数据脱敏的实际处理逻辑]
     *
     * @param data           原始数据
     * @param showHeadLength 展示前几位
     * @param showTailLength 展示后几位
     * @param replaceChar    其他字符的替换
     * @return 脱敏后的数据
     */
    public static String dataDesensitization(String data, int showHeadLength, int showTailLength, String replaceChar) {
        if (Objects.isNull(data) || StringUtils.isBlank(data)) {
            return VarEnmu.NONE.value();
        } else if ((showHeadLength + showTailLength) >= data.length()) {
            return data;
        }
        return StringUtils.left(data, showHeadLength)
            .concat(StringUtils.leftPad(StringUtils.right(data, showTailLength), data.length() - showHeadLength, replaceChar));
    }


}
