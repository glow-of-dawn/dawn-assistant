package com.dawn.plugin.util;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * [二进制处理]
 * 创建时间：2021/5/10 22:53
 *
 * @author hforest-480s
 */
@Slf4j
public class BinUtil {

    private BinUtil() {
    }

    /**
     * [切割文件 转 流 转 二进制字符串，拆入文件目录]
     *
     * @param sourceFile [源文件]
     * @param targetPath [目的路径]
     * @param capacity   [每组文件字节大小]
     * @param key        [口令]
     * @param fileList   [文件名组]
     * @return boolean   是否成功
     **/
    public static boolean fileChannelSplit(File sourceFile, Path targetPath, int capacity, String key, List<String> fileList) {
        try (var readFile = new RandomAccessFile(sourceFile, "r")) {
            /* 清理路径 */
            FileUtils.deleteDirectory(targetPath.toFile());
            /* 建立路径 */
            if (!targetPath.toFile().mkdirs()) {
                log.warn(LogEnmu.LOG2.value(), "目录创建失败", targetPath);
                return false;
            }
            /* 文件序号 */
            int hashNo = VarEnmu.ZERO.ivalue();
            /* 获取只读文件通道 */
            FileChannel readChannel = readFile.getChannel();

            /* 分配容积为 1024 字节的一块Buffer，用来读取数据 */
            ByteBuffer readBuf = ByteBuffer.allocate(capacity);
            /* 从通道里读取数据到Buffer内（最大不超过Buffer容积） */
            int bytesRead = readChannel.read(readBuf);
            /* 当读不到任何东西时返回-1 */
            while (bytesRead != VarEnmu.IIT_MINUS_ONE.ivalue()) {
                /* 切换到Buffer读模式，读模式下可以读取到之前写入Buffer的数据 */
                readBuf.flip();
                /* 循环输出Buffer中的数据 */
                byte[] bts = new byte[capacity];
                while (readBuf.hasRemaining()) {
                    bts[readBuf.position()] = readBuf.get();
                }
                /* java中通过 byte[] 转换为 String 时，可能因为一些编码规则，造成部分被替换，反向转换为 byte[] 后和之前不同；在转换时，可以通过指定 StandardCharsets.ISO_8859_1 等单字节编码来解决问题 */
                String btStr = new String(bts, StandardCharsets.ISO_8859_1);
                /* Base64 bt内容 作为文件体 */
                var btStr64 = Base64.encodeBase64String(btStr.getBytes(StandardCharsets.UTF_8));
                /* sha256(key + hashNo) 作为文件名 */
                var fileNameHashNo = DigestUtils.sha256Hex(key.concat(String.valueOf(hashNo)));
                FileUtils.writeStringToFile(targetPath.resolve(fileNameHashNo).toFile(), btStr64, StandardCharsets.UTF_8);
                /* 切换回Buffer的写模式 */
                readBuf.compact();
                /* 跟上面一样，再次从通道读取数据到Buffer中 */
                bytesRead = readChannel.read(readBuf);
                /* 文件序号递增 */
                hashNo++;
                fileList.add(fileNameHashNo);
            }
            return true;
        } catch (IOException ex) {
            log.warn(LogEnmu.LOG2.value(), "BinUtil.fileChannelSplit", ex.toString());
            return false;
        }
    }

    /**
     * [将切割文件 还原为文件]
     *
     * @param targetFile    [目的文件]
     * @param targetPath    [目的路径]
     * @param capacity      [每组文件字节大小]
     * @param key           [口令]
     * @param fileList      [文件名组]
     * @param delTargetPath [还原后是否清理目的路径]
     * @return boolean   是否成功
     */
    public static boolean fileChannelMerge(File targetFile, Path targetPath, int capacity, String key, List<String> fileList, boolean delTargetPath) {
        try (var writeFile = new RandomAccessFile(targetFile, "rw")) {
            /* 文件序号 */
            int hashNo = VarEnmu.ZERO.ivalue();
            FileUtils.deleteQuietly(targetFile);
            /* 获取写文件通道 */
            FileChannel writeChannel = writeFile.getChannel();
            for (File file : Objects.requireNonNull(targetPath.toFile().listFiles())) {
                fileList.add(file.getName());
            }
            int fsize = fileList.size();
            while (hashNo < fsize) {
                /* sha256(key + hashNo) 作为文件名 */
                var fileNameHashNo = DigestUtils.sha256Hex(key.concat(String.valueOf(hashNo)));
                File noFile = targetPath.resolve(fileNameHashNo).toFile();
                var btStr64 = FileUtils.readFileToString(noFile, StandardCharsets.UTF_8);
                ByteBuffer buffer = ByteBuffer.allocate(capacity);
                buffer.compact();
                var bt64 = Base64.decodeBase64(btStr64);
                String btStr = new String(bt64, StandardCharsets.UTF_8);
                byte[] bts = btStr.getBytes(StandardCharsets.ISO_8859_1);
                int i = VarEnmu.NONE.ivalue();
                for (byte bt : bts) {
                    buffer.put(i, bt);
                    i++;
                }
                buffer.flip();
                /* 将现在的buffer里的数据写到文件haha.txt里 */
                writeChannel.write(buffer);
                /* 文件序号递增 */
                hashNo++;
            }
            if (delTargetPath) {
                /* 清理路径 */
                FileUtils.deleteDirectory(targetPath.toFile());
            }
            return true;
        } catch (IOException ex) {
            log.warn(LogEnmu.LOG2.value(), "BinUtil.fileChannelSplit", ex.toString());
            return false;
        }
    }

}
