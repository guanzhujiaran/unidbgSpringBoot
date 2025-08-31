package guanzhujiaran.unidbgspringboot.Service.app.comm;

import guanzhujiaran.unidbgspringboot.Service.app.samsclub.SamClubEncrypt;
import lombok.Getter;
import org.springframework.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

@Getter
public class Encryptor {

    private static final Logger logger = LoggerFactory.getLogger(Encryptor.class);

    private final SamClubEncrypt samClubEncryptor;

    // 私有构造器，初始化加密对象
    private Encryptor() throws InitializationException {
        try {
            File apkFile = ResourceUtils.getFile("classpath:apk/samsclub/cn.samsclub.app_5.0.120.apk");
            File soFile = ResourceUtils.getFile("classpath:so/samsclub/libsrmscy.so");

            this.samClubEncryptor = new SamClubEncrypt(apkFile, soFile);
        } catch (FileNotFoundException e) {
            logger.error("Required resource file not found during Encryptor initialization", e);
            throw new InitializationException("Failed to initialize Encryptor due to missing resources", e);
        } catch (Exception e) {
            logger.error("Unexpected error during Encryptor initialization", e);
            throw new InitializationException("Unexpected error during Encryptor initialization", e);
        }
    }

    /**
     * 使用静态内部类实现线程安全且懒加载的单例
     */
    public static Encryptor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final Encryptor INSTANCE;

        static {
            try {
                INSTANCE = new Encryptor();
            } catch (InitializationException e) {
                throw new RuntimeException("Failed to create Encryptor instance", e);
            }
        }
    }

    /**
     * 自定义异常类，用于包装初始化错误
     */
    public static class InitializationException extends Exception {
        public InitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}