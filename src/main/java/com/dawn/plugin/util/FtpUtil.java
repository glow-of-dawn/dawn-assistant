package com.dawn.plugin.util;

import com.dawn.plugin.enmu.LogEnmu;
import com.dawn.plugin.enmu.VarEnmu;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.*;
import org.springframework.util.NumberUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2021/2/1 15:05
 *
 * @author hforest-480s
 */
@Data
@Slf4j
public class FtpUtil {

    private static final Map<String, FtpLogin> FTP_MAP = new HashMap<>();

    private FtpUtil() {
        throw new UnsupportedOperationException();
    }


    /**
     * [获取路径名，惊醒编码转义]
     *
     * @param ftpName     [ftpName]
     * @param ftpFileName [ftpFileName]
     * @return String
     */
    public static String getFtpFileName(String ftpName, String ftpFileName) {
        FtpLogin ftpLogin = FTP_MAP.get(ftpName);
        try {
            return new String(ftpFileName.getBytes(ftpLogin.getLocalCharest()), ftpLogin.getServerCharest());
        } catch (UnsupportedEncodingException ex) {
            return ftpFileName;
        }
    }

    /**
     * [建立ftp]
     *
     * @param ftpName []
     * @param map     map [address, port, username, password, serverCharest("ISO-8859-1"), localCharest("GBK / UTF-8)]
     */
    public static void createFtpLogin(String ftpName,
                                      Map<String, String> map) {
        FTP_MAP.put(ftpName,
                new FtpLogin(map.get("address"),
                        NumberUtils.parseNumber(map.get("port"), Integer.class),
                        map.get("username"),
                        map.get("password"),
                        map.get("serverCharest"),
                        map.get("localCharest")));
    }

    /**
     * [建立ftp]
     *
     * @param ftpName  [ftpName]
     * @param address  [address]
     * @param port     [port]
     * @param username [username]
     * @param password [password]
     */
    public static void createFtpLogin(String ftpName, String address, int port, String username, String password) {
        FTP_MAP.put(ftpName, new FtpLogin(address, port, username, password, "ISO-8859-1", VarEnmu.GBK.value()));
    }

    /**
     * [上传文件]
     *
     * @param ftpName       []
     * @param localFileName []
     * @param ftpFilePath   []
     * @param ftpFileName   []
     * @return boolean
     */
    public static boolean uploadFile(String ftpName, String localFileName, String ftpFilePath, String ftpFileName) {
        return uploadFile(ftpName, new File(localFileName), ftpFilePath, ftpFileName);
    }

    /**
     * [上传文件]
     *
     * @param ftpName       []
     * @param localFileName []
     * @param ftpFileName   []
     * @return boolean
     */
    public static boolean uploadFile(String ftpName, String localFileName, String ftpFileName) {
        return uploadFile(ftpName, new File(localFileName),
                ftpFileName.substring(0, ftpFileName.lastIndexOf("/")),
                ftpFileName.substring(ftpFileName.lastIndexOf("/") + 1));
    }

    /**
     * [上传文件]
     *
     * @param ftpName     []
     * @param localFile   []
     * @param ftpFilePath []
     * @param ftpFileName []
     * @return boolean
     */
    public static boolean uploadFile(String ftpName, File localFile, String ftpFilePath, String ftpFileName) {
        if (!localFile.exists()) {
            return false;
        }
        FTPClient ftpClient = createFtpPath(ftpName, ftpFilePath, false);
        if (ftpClient == null) {
            return false;
        }
        FtpLogin ftpLogin = FTP_MAP.get(ftpName);
        try {
            String fileName = new String(ftpFileName.getBytes(ftpLogin.getLocalCharest()), ftpLogin.getServerCharest());
            try (FileInputStream fis = new FileInputStream(localFile)) {
                if (ftpClient.storeFile(ftpFilePath.concat(VarEnmu.SLASH.value()).concat(fileName), fis)) {
                    return true;
                }
            }
            return false;
        } catch (IOException ex) {
            log.error(LogEnmu.LOG5.value(), "uploadFile", ftpName, ftpFilePath, ftpFileName, ex.toString());
            return false;
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [下载 ftp文件]
     *
     * @param ftpName       []
     * @param localFileName []
     * @param ftpFileName   []
     * @return boolean
     */
    public static boolean downloadFile(String ftpName, String localFileName, String ftpFileName) {
        FTPClient ftpClient = login(ftpName);
        if (ftpClient == null) {
            return false;
        }
        File file = new File(localFileName);
        if (!file.exists()) {
            new File(file.getParent()).mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(localFileName)) {
            ftpClient.retrieveFile(getFtpFileName(ftpName, ftpFileName), fos);
            return true;
        } catch (IOException ex) {
            log.error(LogEnmu.LOG5.value(), "downloadFile", ftpName, localFileName, ftpFileName, ex.toString());
            return false;
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [下载 ftp文件]
     *
     * @param ftpName       []
     * @param localFileName []
     * @param ftpFileNames  []
     * @return boolean
     */
    public static boolean downloadFiles(String ftpName, String localFileName, List<String> ftpFileNames) {
        FTPClient ftpClient = login(ftpName);
        if (ftpClient == null) {
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(localFileName)) {
            File file = new File(localFileName);
            if (!file.exists()) {
                new File(file.getParent()).mkdirs();
            }
            for (String ftpFileName : ftpFileNames) {
                ftpClient.retrieveFile(getFtpFileName(ftpName, ftpFileName), fos);
            }
            return true;
        } catch (Exception ex) {
            log.error(LogEnmu.LOG4.value(), "downloadFiles", ftpName, localFileName, ex.toString());
            return false;
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [下载 ftp文件]
     *
     * @param ftpName     []
     * @param ftpFileName []
     * @return String
     */
    public static String downloadFile(String ftpName, String ftpFileName) {
        FTPClient ftpClient = login(ftpName);
        FtpLogin ftpLogin = FTP_MAP.get(ftpName);
        if (ftpClient == null) {
            return null;
        }
        try (InputStream is = ftpClient.retrieveFileStream(getFtpFileName(ftpName, ftpFileName))) {
            return IOUtils.toString(is, ftpLogin.getLocalCharest());
        } catch (Exception ex) {
            log.error(LogEnmu.LOG4.value(), "downloadFile", ftpName, ftpFileName, ex.toString());
            return null;
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [获取FTP文件列表]
     *
     * @param ftpName     []
     * @param ftpFileName []
     * @return FTPFile[]
     */
    public static FTPFile[] getFiles(String ftpName, String ftpFileName) {
        FTPClient ftpClient = login(ftpName);
        if (ftpClient == null) {
            return new FTPFile[0];
        }
        try {
            return ftpClient.listFiles(ftpFileName);
        } catch (Exception ex) {
            log.error(LogEnmu.LOG4.value(), "getFiles", ftpName, ftpFileName, ex.toString());
            return new FTPFile[0];
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [删除FTP文件]
     *
     * @param ftpName     []
     * @param ftpFileName []
     * @return boolean
     */
    public static boolean remove(String ftpName, String ftpFileName) {
        FTPClient ftpClient = login(ftpName);
        if (ftpClient == null) {
            return false;
        }
        try {
            FTPFile ftpFile = ftpClient.mlistFile(getFtpFileName(ftpName, ftpFileName));
            if (ftpFile == null) {
                return false;
            } else if (ftpFile.isDirectory()) {
                return removeFiles(ftpName, ftpClient, ftpFileName);
            } else {
                return ftpClient.deleteFile(getFtpFileName(ftpName, ftpFileName));
            }
        } catch (Exception e) {
            log.error(LogEnmu.LOG4.value(), "remove", ftpName, ftpFileName, e.toString());
            return false;
        } finally {
            close(ftpClient);
        }
    }

    /**
     * [批量删除]
     *
     * @param ftpClient   []
     * @param ftpFileName []
     * @return boolean
     */
    public static boolean removeFiles(String ftpName, FTPClient ftpClient, String ftpFileName) {
        try {
            FTPFile[] subFiles = ftpClient.listFiles(getFtpFileName(ftpName, ftpFileName));
            for (FTPFile ftpFile : subFiles) {
                /* remove the sub directory */
                if (ftpFile.isDirectory()) {
                    removeFiles(ftpName, ftpClient, ftpFileName + ftpFile.getName());
                    ftpClient.removeDirectory(getFtpFileName(ftpName, ftpFileName.concat("/").concat(ftpFile.getName())));
                } else {
                    ftpClient.deleteFile(getFtpFileName(ftpName, ftpFileName.concat("/").concat(ftpFile.getName())));
                }
            }
            ftpClient.removeDirectory(getFtpFileName(ftpName, ftpFileName));
            return true;
        } catch (Exception e) {
            log.error(LogEnmu.LOG3.value(), "removeFiles", ftpName, e.toString());
            return false;
        }
    }

    /**
     * [建立路径]
     *
     * @param ftpName  []
     * @param path     []
     * @param closeFtp []
     * @return FTPClient
     */
    public static FTPClient createFtpPath(String ftpName, String path, boolean closeFtp) {
        FTPClient ftpClient = login(ftpName);
        FtpLogin ftpLogin = FTP_MAP.get(ftpName);
        if (ftpClient == null) {
            return null;
        }
        try {
            String ftppath = new String(path.getBytes(ftpLogin.getLocalCharest()), ftpLogin.getServerCharest());
            if (ftpClient.changeWorkingDirectory(ftppath)) {
                return ftpClient;
            }
            String[] arr = ftppath.split("/");
            StringBuilder sbdir = new StringBuilder();
            for (String s : arr) {
                if ("".equals(s) || ftpClient.changeWorkingDirectory(s)) {
                    continue;
                }
                sbdir.append("/").append(s);
                if (!ftpClient.makeDirectory(sbdir.toString())) {
                    log.error(LogEnmu.LOG4.value(), "createFtpPath", ftpName, path, sbdir);
                    return null;
                }
            }
            return ftpClient.changeWorkingDirectory(ftppath) ? ftpClient : null;
        } catch (IOException ex) {
            log.error(LogEnmu.LOG4.value(), "createFtpPath", ftpName, path, ex.toString());
            return null;
        } finally {
            if (closeFtp) {
                close(ftpClient);
            }
        }
    }

    /**
     * [登陆FTP]
     * 如果服务器支持UTF-8编码 ? 开启服务器对UTF-8的支持 : 使用本地编码 GBK
     * [String opts = "OPTS UTF8";]
     * [if(FTPReply.isPositiveCompletion(ftpClient.sendCommand(opts, VarEnmu.ON.value()))) { localCharest = VarEnmu.UTF8.value(); }]
     *
     * @param ftpName []
     * @return FTPClient
     */
    private static FTPClient login(String ftpName) {
        try {
            FTPClient ftpClient = new FTPClient();
            FtpLogin ftpLogin = FTP_MAP.get(ftpName);
            if (ftpLogin == null) {
                log.error(LogEnmu.LOG3.value(), "loginFtp", ftpName, "ftpMap.get(ftpName) is null");
                return null;
            }
            ftpClient.connect(ftpLogin.getAddress(), ftpLogin.getPort());
            if (!ftpClient.login(ftpLogin.getUsername(), ftpLogin.getPassword())) {
                log.error(LogEnmu.LOG4.value(), "ftp.login", ftpLogin.getUsername(), ftpLogin.getPassword(), "ftpMap.get(ftpName) login is false");
                return null;
            } else if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                /* 设置被动模式 */
                ftpClient.enterLocalPassiveMode();
                /* 设置主动模式 [ftpClient.enterLocalActiveMode();] */
            }
            ftpClient.setControlEncoding(ftpLogin.getLocalCharest());
            /* 设置传输模式(FTP.ASCII_FILE_TYPE); 默认传输模式; 大多服务端默认(FTP.BINARY_FILE_TYPE) */
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(VarEnmu.NUMBER_1024.ivalue());
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
            conf.setServerLanguageCode("zh");
            /* 开启服务器对UTF-8的支持 */
            return ftpClient;
        } catch (Exception e) {
            log.error(LogEnmu.LOG3.value(), "loginFtp()", ftpName, e.toString());
            return null;
        }
    }

    /**
     * [关闭ftp]
     */
    private static void close(FTPClient ftp) {
        if (ftp == null) {
            return;
        }
        if (!ftp.isConnected()) {
            return;
        }
        try {
            ftp.logout();
            ftp.disconnect();
        } catch (Exception ex) {
            log.error(LogEnmu.LOG2.value(), "close()", ex.toString());
        }
    }

}

/**
 * [内部类]
 * 创建时间：2021/02/20 16:08
 *
 * @author hforest-480s
 */
@Data
class FtpLogin {

    private String address;
    private int port;
    private String username;
    private String password;
    /* FTP协议里面，规定文件名编码为 iso-8859-1 ISO-8859-1 */
    private String serverCharest;
    /* 本地字符编码 */
    private String localCharest;

    /**
     * [address: ip地址 || 域名; port: 端口; username: 登陆用户; password: 密码;]
     *
     * @param address       []
     * @param port          []
     * @param username      []
     * @param password      []
     * @param serverCharest []
     * @param localCharest  []
     */
    public FtpLogin(String address, int port, String username, String password, String serverCharest, String localCharest) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.serverCharest = serverCharest;
        this.localCharest = localCharest;
    }

}
