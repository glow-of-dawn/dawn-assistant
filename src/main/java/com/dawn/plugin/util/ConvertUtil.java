package com.dawn.plugin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dawn.plugin.config.LoadParams;
import com.dawn.plugin.config.PluginConfig;
import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * [转换单元]
 * 创建时间：2021/2/2 21:33
 *
 * @author forest
 */
@Data
@Slf4j
@Component
@ConditionalOnProperty(name = {"plugin-status.util-status"}, havingValue = "enable", matchIfMissing = true)
public class ConvertUtil {

    private PluginConfig config;
    private LoadParams loadParams;
    /* 分隔符 */
    @Value("#{'${plugin-params.reg-split:\\|}'}")
    private String regSplit;
    @Value("#{'${plugin-params.split:|}'}")
    private String split;

    public ConvertUtil(PluginConfig config,
                       LoadParams loadParams) {
        this.config = config;
        this.loadParams = loadParams;
    }

    /**
     * [实体类赋值控制器]
     *
     * @param tabEntityMap [tabEntityMap]
     * @param tabEntity    [tabEntity]
     * @param loadKey      [loadKey]
     * @return boolean
     */
    public boolean editEntity(Map<String, Object> tabEntityMap, Object tabEntity, String loadKey) {
        var sha256 = DigestUtils.sha256Hex(tabEntity.toString());
        var fieldVal = loadParams.loadKey(loadKey, VarEnmu.CONSOLE_FIELD.value());
        Arrays.stream(fieldVal.split(VarEnmu.BACK_SLASH.value().concat(VarEnmu.VLINE.value())))
            .filter(field -> !VarEnmu.NONE.value().equals(field))
            .filter(field -> !VarEnmu.NONE.value().equals(tabEntityMap.getOrDefault(field, VarEnmu.NONE.value())))
            .forEach(field -> {
                var entityVal = tabEntityMap.get(field);
                Object val;
                var classField = ReflectionUtils.findField(tabEntity.getClass(), field);
                Assert.notNull(classField, "Field is null!");
                if (classField.getType().equals(LocalDateTime.class)) {
                    val = LocalDateTime.parse(String.valueOf(entityVal), DateTimeFormatter.ofPattern(VarEnmu.DATE_TIME_FORMATTER.value()));
                } else if (classField.getType().equals(LocalDate.class)) {
                    val = LocalDate.parse(String.valueOf(entityVal), DateTimeFormatter.ofPattern(VarEnmu.DATE_FORMATTER.value()));
                } else if (classField.getType().equals(Integer.class)) {
                    val = Integer.valueOf(String.valueOf(entityVal));
                } else if (classField.getType().equals(BigDecimal.class)) {
                    val = new BigDecimal(String.valueOf(entityVal));
                } else {
                    val = entityVal;
                }
                ReflectionUtils.makeAccessible(classField);
                ReflectionUtils.setField(classField, tabEntity, val);
            });
        return sha256.equals(DigestUtils.sha256Hex(tabEntity.toString()));
    }

    /**
     * [getFieldMap]
     *
     * @param transMap [transMap]
     * @param srcField [srcField]
     * @param srcMap   [srcMap]
     * @param tagField [tagField]
     * @param tagMap   [tagMap]
     * @param defValue [defValue]
     * @return Map<String, Object>
     */
    public Map<String, Object> getFieldMap(Map<String, String> transMap, String srcField, Map<String, Object> srcMap, String tagField,
                                           Map<String, Object> tagMap, Object defValue) {
        log.debug(LogEnmu.LOG7.value(), "组件预定义字段.src", srcField, srcMap, ".tar", tagField, tagMap, defValue);
        String[] reqSrcBodyFields = transMap.get(srcField).split(regSplit);
        String[] reqTagBodyFields = transMap.get(tagField).split(regSplit);
        Map<String, String> fieldMap = new LinkedHashMap<>();
        AtomicInteger idx = new AtomicInteger(VarEnmu.ZERO.ivalue());
        Arrays.stream(reqTagBodyFields)
            .forEach(field -> {
                fieldMap.put(field, idx.get() < reqSrcBodyFields.length ? reqSrcBodyFields[idx.get()] : VarEnmu.NONE.value());
                idx.getAndIncrement();
            });

        log.debug(LogEnmu.LOG2.value(), "递归二级键值填充");
        /* 特别留意仅支持二级，不支持多级，如果多个二级有重复key导致value为末尾put.value */
        Map<String, Object> srcsMap = LinkedHashMap.newLinkedHashMap(VarEnmu.SIXTEEN.ivalue());
        srcMap.entrySet().stream()
            .filter(en -> en.getValue() instanceof Map)
            .forEach(en -> srcsMap.putAll((Map<String, ?>) en.getValue()));
        srcMap.entrySet().stream()
            .filter(en -> !(en.getValue() instanceof Map))
            .forEach(en -> srcsMap.put(en.getKey(), en.getValue()));

        Map<String, Object> defMap = defValue instanceof Map ? (Map<String, Object>) defValue : new LinkedHashMap<>();

        log.debug(LogEnmu.LOG2.value(), "采用默认值");
        Arrays.stream(reqTagBodyFields)
            .filter(field -> VarEnmu.NONE.value().equals(fieldMap.get(field)))
            .forEach(field -> tagMap.put(field, defMap.getOrDefault(field, VarEnmu.NONE.value())));

        log.debug(LogEnmu.LOG2.value(), "[+]处理");
        Arrays.stream(reqTagBodyFields)
            .filter(field -> fieldMap.get(field).contains(VarEnmu.PLUS.value()))
            .forEach(field -> {
                String[] ifields = fieldMap.get(field).split(VarEnmu.BACK_SLASH.value().concat(VarEnmu.PLUS.value()));
                StringBuilder sb = new StringBuilder();
                Arrays.stream(ifields)
                    .forEach(ifield -> sb.append(srcsMap.getOrDefault(ifield, VarEnmu.NONE.value()).toString()));
                tagMap.put(field, sb.toString());
            });

        log.debug(LogEnmu.LOG2.value(), "其他处理");
        Arrays.stream(reqTagBodyFields)
            .filter(field -> !VarEnmu.NONE.value().equals(field))
            .filter(field -> VarEnmu.NONE.value().equals(tagMap.getOrDefault(field, VarEnmu.NONE.value())))
            .forEach(field -> tagMap.put(field, srcsMap.getOrDefault(fieldMap.get(field), VarEnmu.NONE.value())));
        return tagMap;
    }

    /**
     * [第二字符替换 解决 config.getMapperLowerCamel().convertValue(inczdmtqz1Map, TabTransRecord.class)]
     *
     * @param key         [key]
     * @param isUpperCase [isUpperCase]
     * @return java.lang.String
     **/
    public static String secondChar(String key, boolean isUpperCase) {
        StringBuilder sb = new StringBuilder(key);
        String seChar = isUpperCase ? sb.substring(1, 2).toUpperCase() : sb.substring(1, 2).toLowerCase();
        sb.replace(1, 2, seChar);
        return sb.toString();
    }

    /**
     * [字符串转换成十六进制值]
     *
     * @param bin String 我们看到的要转换成十六进制的字符串
     * @return String
     */
    public static String string2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        byte[] bs = bin.getBytes();
        int bit;
        for (byte b : bs) {
            bit = (b & VarEnmu.NUM_0X0F0.ivalue()) >> VarEnmu.FOUR.ivalue();
            sb.append(digital[bit]);
            bit = b & VarEnmu.NUM_0X0F.ivalue();
            sb.append(digital[bit]);
        }
        return sb.toString();
    }

    /**
     * [十六进制转换字符串]
     *
     * @param hex String 十六进制
     * @return String 转换后的字符串
     */
    public static String hex2String(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * VarEnmu.SIXTEEN.ivalue();
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & VarEnmu.NUM_0XFF.ivalue());
        }
        return new String(bytes);
    }

    /**
     * [byte[] -- String]
     *
     * @param txtString [txtString]
     * @return String 转换后的字符串
     */
    public static String string2byte(String txtString) {
        byte[] bbs = txtString.getBytes(StandardCharsets.UTF_8);
        String u = Base64.getEncoder().encodeToString(bbs);
        byte[] bs = Base64.getDecoder().decode(u);
        String ul = new String(bs, StandardCharsets.UTF_8);
        log.info(LogEnmu.LOG3.value(), txtString, u, ul);
        return ul;
    }

    /**
     * [判断是否是json]
     * hutool 判断过于宽松
     *
     * @param data [datString]
     * @return boolean
     */
    public static boolean isJson(Object data) {
        return isJsonAndHasFieldName(data, VarEnmu.NONE.value());
    }

    /**
     * [判断是否是json]
     * hutool 判断过于宽松
     *
     * @param fieldName [fieldName]
     * @param data      [datString]
     * @return boolean
     */
    public static boolean isJsonAndHasFieldName(Object data, String fieldName) {
        if (Objects.isNull(data)) {
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(data.toString());
            if (StringUtils.hasText(fieldName)) {
                return jsonNode.size() > VarEnmu.ZERO.ivalue() && StringUtils.hasText(data.toString()) && jsonNode.has(fieldName);
            } else {
                return jsonNode.size() > VarEnmu.ZERO.ivalue() && StringUtils.hasText(data.toString());
            }
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * [String 进行 gzip压缩]
     *
     * @param data [待压缩的String]
     * @return boolean
     */
    public static String gzipCompress(String data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.finish();
            byte[] compressedBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(compressedBytes);
        } catch (IOException e) {
            log.warn(LogEnmu.LOG2.value(), "gzipCompress", e.toString());
            return VarEnmu.NONE.value();
        }
    }

    /**
     * [String 进行 gzip解压]
     *
     * @param compressData [压缩后的String]
     * @return boolean
     */
    public static String gzipDecompress(String compressData) {
        byte[] compressedBytes = Base64.getDecoder().decode(compressData);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes));
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[VarEnmu.NUMBER_1024.ivalue()];
            int len;
            while ((len = gzipInputStream.read(buffer)) != VarEnmu.IIT_MINUS_ONE.ivalue()) {
                byteArrayOutputStream.write(buffer, VarEnmu.ZERO.ivalue(), len);
            }
            return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn(LogEnmu.LOG2.value(), "gzipDecompress", e.toString());
            return VarEnmu.NONE.value();
        }
    }

}
