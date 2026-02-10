package com.dawn.plugin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jcraft.jsch.*;
import com.dawn.plugin.enmu.LogEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 创建时间：2021/2/1 14:22
 *
 * @author hforest-480s
 */
@Slf4j
@Data
public class SftpUtil {

    private static final Map<String, SftpLogin> SFTP_MAP = new HashMap<>();
    private static final Map<ChannelSftp, Session> CHANNEL_SFTP_MAP = new HashMap<>();

    private SftpUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * [初始化sftp]
     *
     * @param ftpName
     * @param address
     * @param port
     * @param username
     * @param password
     * @return void
     **/
    public static void createSftpLogin(String ftpName, String address, int port, String username, String password) {
        SFTP_MAP.put(ftpName, new SftpLogin(address, port, username, password));
    }

    /**
     * [初始化sftp]
     *
     * @param ftpName
     * @param username
     * @param address
     * @param port
     * @param privateKey
     **/
    public static void createSftpLogin(String ftpName, String username, String address, int port, String privateKey) {
        SFTP_MAP.put(ftpName, new SftpLogin(username, address, port, privateKey));
    }

    /**
     * [下载文件]
     *
     * @param sftpName
     * @param downloadDirectory
     * @param downloadFile
     * @param localFileName
     * @return boolean
     **/
    public static boolean downloadFile(String sftpName, String downloadDirectory, String downloadFile, String localFileName) throws SftpException {
        ChannelSftp channelSftp = login(sftpName);
        if (channelSftp == null) {
            return false;
        }
        channelSftp.cd(downloadDirectory);
        File file = new File(localFileName);
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            channelSftp.get(downloadFile, fos);
            log.info(LogEnmu.LOG3.value(), "sftp.downloadFile", sftpName, downloadDirectory.concat(downloadFile));
            return true;
        } catch (Exception ex) {
            log.error(LogEnmu.LOG6.value(), "downloadFile", sftpName, downloadDirectory, downloadFile, localFileName, ex.toString());
            return false;
        } finally {
            close(channelSftp);
        }
    }

    /**
     * [将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory]
     *
     * @param sftpName
     * @param localFile
     * @param ftpFilePath
     * @param ftpFileName
     * @return boolean
     **/
    public static boolean uploadFile(String sftpName, File localFile, String ftpFilePath, String ftpFileName) {
        if (!localFile.exists()) {
            return false;
        }
        ChannelSftp channelSftp = createSftpPath(sftpName, ftpFilePath, false);
        if (channelSftp == null) {
            return false;
        }
        try (InputStream input = new FileInputStream(localFile)) {
            /* 上传文件 */
            String ftpFilePathName = ftpFilePath.concat(ftpFileName);
            channelSftp.put(input, ftpFilePathName);
            log.info(LogEnmu.LOG3.value(), "sftp.uploadFile", sftpName, ftpFilePathName);
            return true;
        } catch (Exception ex) {
            log.error(LogEnmu.LOG5.value(), "uploadFile", sftpName, ftpFilePath, ftpFileName, ex.toString());
            return false;
        } finally {
            close(channelSftp);
        }
    }

    /**
     * [获取FTP文件列表]
     *
     * @param sftpName
     * @param directory
     * @return List<Map>
     **/
    public static List<Map<Object, Object>> listFiles(String sftpName, String directory) {
        ChannelSftp channelSftp = login(sftpName);
        if (channelSftp == null) {
            return new ArrayList<>();
        }
        try {
            log.error(LogEnmu.LOG3.value(), "sftp.listFiles", sftpName, directory);
            ObjectMapper mapperLowerCamel = new ObjectMapper();
            mapperLowerCamel.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            List<Map<Object, Object>> lsMap = new ArrayList<>();
            List<ChannelSftp.LsEntry> ls = channelSftp.ls(directory);
            ls.stream().filter(en -> (!".".equals(en.getFilename())) && !"..".equals(en.getFilename())).forEach(en -> {
                Map<Object, Object> m = mapperLowerCamel.convertValue(en, Map.class);
                lsMap.add(m);
            });
            return lsMap;
        } catch (Exception ex) {
            String fex = "2:";
            if (ex.toString().contains(fex)) {
                log.error(LogEnmu.LOG4.value(), "sftp.listFiles", sftpName, directory, "No such file");
            } else {
                log.error(LogEnmu.LOG4.value(), "listFiles", sftpName, directory, ex.toString());
            }
            return new ArrayList<>();
        } finally {
            close(channelSftp);
        }
    }

    /**
     * [只能删除单一文件]
     *
     * @param sftpName
     * @param directory
     * @param deleteFile
     * @return void
     **/
    public static boolean delete(String sftpName, String directory, String deleteFile) {
        ChannelSftp channelSftp = login(sftpName);
        if (channelSftp == null) {
            return false;
        }
        try {
            channelSftp.cd(directory);
            channelSftp.rm(deleteFile);
            log.info(LogEnmu.LOG3.value(), "sftp.delete", sftpName, directory.concat(deleteFile));
            return true;
        } catch (Exception ex) {
            log.error(LogEnmu.LOG4.value(), "delete", sftpName, directory, ex.toString());
            return false;
        } finally {
            close(channelSftp);
        }
    }

    /**
     * [建立路径]
     *
     * @param sftpName [sftpName]
     * @param path     [path]
     * @param closeFtp [closeFtp]
     * @return FTPClient
     **/
    public static ChannelSftp createSftpPath(String sftpName, String path, boolean closeFtp) {
        ChannelSftp channelSftp = login(sftpName);
        if (channelSftp == null) {
            return null;
        }
        String sftpPath = path.indexOf("/") == 0 ? "/" : "";
        String[] folders = path.split("/");
        for (String folder : folders) {
            if (folder.isEmpty()) {
                continue;
            }
            try {
                sftpPath = sftpPath.concat(folder).concat("/");
                /* 参考:[SftpATTRS attrs = channelSftp.stat(sftpPath);] */
            } catch (Exception e) {
                try {
                    channelSftp.mkdir(sftpPath);
                } catch (Exception ex) {
                    log.error(LogEnmu.LOG4.value(), "createSftpPath", sftpName, path, ex.toString());
                    return null;
                }
            }
            /* 参考:[channelSftp.cd(folder);] */
        }
        log.info(LogEnmu.LOG3.value(), "sftp.create.path", sftpName, path);
        if (closeFtp) {
            close(channelSftp);
        }
        return channelSftp;
    }

    /**
     * [连接sftp服务器]
     *
     * @param sftpName [sftpName]
     */
    private static ChannelSftp login(String sftpName) {
        try {
            SftpLogin sftpLogin = SFTP_MAP.get(sftpName);
            if (sftpLogin == null) {
                log.error(LogEnmu.LOG3.value(), "loginSftp", sftpName, "sftpMap.get(sftpName) is null");
                return null;
            }
            JSch jsch = new JSch();
            if (sftpLogin.getPrivateKey() != null) {
                /* 设置私钥 */
                jsch.addIdentity(sftpLogin.getPrivateKey());
            }
            Session session = jsch.getSession(sftpLogin.getUsername(), sftpLogin.getAddress(), sftpLogin.getPort());
            if (sftpLogin.getPassword() != null) {
                session.setPassword(sftpLogin.getPassword());
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;
            CHANNEL_SFTP_MAP.put(sftp, session);
            log.info(LogEnmu.LOG2.value(), "sftp.login", sftpName);
            return sftp;
        } catch (JSchException ex) {
            log.error(LogEnmu.LOG3.value(), "loginSftp()", sftpName, ex.toString());
            return null;
        }
    }

    /**
     * 关闭 sftp
     */
    private static void close(ChannelSftp channelSftp) {
        if (channelSftp == null || !channelSftp.isConnected()) {
            return;
        }
        try {
            channelSftp.disconnect();
            Session session = CHANNEL_SFTP_MAP.get(channelSftp);
            CHANNEL_SFTP_MAP.remove(channelSftp);
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception ex) {
            log.debug(LogEnmu.LOG2.value(), "close()", ex.toString());
        }
    }


}

/**
 * 内部类：SFTP -> ssh+ftp
 * 创建时间：2021/2/1 14:22
 *
 * @author hforest-480s * @date 2021/2/1 14:22
 */
@Data
class SftpLogin {

    private String privateKey;
    private String address;
    private int port;
    private String username;
    private String password;

    /**
     * address: ip地址 || 域名; port: 端口; username: 登陆用户; password: 密码;
     *
     * @param address  [ip地址 || 域名]
     * @param port     [端口]
     * @param username [登陆用户]
     * @param password [密码]
     */
    public SftpLogin(String address, int port, String username, String password) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * [address: username: 登陆用户; ip地址 || 域名; port: 端口; privateKey: 密钥;]
     *
     * @param username   [登陆用户]
     * @param address    [ip地址 || 域名]
     * @param port       [端口]
     * @param privateKey [密钥]
     **/
    public SftpLogin(String username, String address, int port, String privateKey) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.privateKey = privateKey;
    }

}
