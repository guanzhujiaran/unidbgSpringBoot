package guanzhujiaran.unidbgspringboot.Service.app.samsclub;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import guanzhujiaran.unidbgspringboot.Model.api.app.samsclub.DoEncrpytBody;

import java.io.File;
import java.io.FileNotFoundException;

public class SamClubEncrypt extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final DalvikModule dalvikModule; // Store the DalvikModule

    private String scy_iv_val = "%8LpHj&20Kz@g1MF";
    private String scy_key_val = "bQRFkTJie19qSKjerWLGkJKL9ntAoJ5ITADC1bVTVgAAgQnScWwaOTw2cA4BgI2U";
    private String scy_srd_val = "MTBfM18xNDhfOF8yMTVfNQ==";
    public void updateKey(String ivVal,String keyVal,String srdVal){
        if (!ivVal.isBlank() && !ivVal.equals(scy_iv_val)) {
            scy_iv_val = ivVal;
        }
        if (!keyVal.isBlank() && !keyVal.equals(scy_key_val)) {
            scy_key_val = keyVal;
        } if (!srdVal.isBlank() && !srdVal.equals(scy_srd_val)) {
            scy_srd_val = srdVal;
        }
    }


    public SamClubEncrypt(File apkFile, File soFile) throws FileNotFoundException {
        // 1. 创建模拟器实例
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("cn.samsclub.app").build();
        final Memory memory = emulator.getMemory();
        // 2. 设置JNI和系统属性等
        memory.setLibraryResolver(new AndroidResolver(23));
        // 3. 创建DalvikVM
        if (!apkFile.exists()) {
            throw new FileNotFoundException("APK file not found: " + apkFile.getPath());
        }
        vm = emulator.createDalvikVM(apkFile);
        vm.setJni(this);
        vm.setVerbose(false); // Crucial for detailed JNI logs
        // 4. 加载SO库并显式调用JNI_OnLoad
        if (!soFile.exists()) {
            throw new FileNotFoundException("SO file not found: " + soFile.getPath());
        }

        try {
            this.dalvikModule = vm.loadLibrary(soFile, true);
            if (this.dalvikModule == null) {
                throw new IllegalStateException("Failed to load SO: " + soFile.getName() + " - DalvikModule is null.");
            }
            if (this.dalvikModule.getModule() == null) {
                throw new IllegalStateException("Failed to load SO: " + soFile.getName() + " - underlying Module is null.");
            }
//            System.out.println("Successfully loaded SO: " + dalvikModule.getModule().name);

            // Now, explicitly call JNI_OnLoad
            // This ensures that if JNI_OnLoad has any issues (e.g., tries to call a Java method
            // that isn't hooked yet, leading to an exception), it's more apparent.
//            System.out.println("Attempting to explicitly call JNI_OnLoad for " + dalvikModule.getModule().name);
            this.dalvikModule.callJNI_OnLoad(emulator);
//            System.out.println("Explicitly called JNI_OnLoad for " + dalvikModule.getModule().name);

        } catch (Throwable e) {
            System.err.println("LibraryNotLoadedException while loading " + soFile.getName());
            e.printStackTrace();
            throw e; // Re-throw
        }


        // 5. 获取SecurityTools类 (This part should be fine if JNI_OnLoad registered it)
        DvmClass securityToolsClass = vm.resolveClass("com/srm/security/SecurityTools");
        if (securityToolsClass == null) {
            // This could happen if JNI_OnLoad is supposed to make the class known in some way,
            // or if the class just isn't in the APK. But FindClass from native is more common.
            throw new IllegalStateException("Failed to resolve class com/srm/security/SecurityTools after JNI_OnLoad.");
        }
//        System.out.println("SecurityTools class resolved: " + securityToolsClass);
    }

    public String doEncrypt(DoEncrpytBody doEncrpytBody) {
        DvmClass securityToolsClass = vm.resolveClass("com/srm/security/SecurityTools");
        if (securityToolsClass == null) {
            throw new IllegalStateException("Failed to resolve class com/srm/security/SecurityTools for encryption");
        }
        securityToolsClass.callStaticJniMethod(emulator, "update(Ljava/lang/String;)V",
                new StringObject(vm, scy_srd_val));
        DvmObject<?> result = securityToolsClass.callStaticJniMethodObject(emulator,
                "doEncrypt(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                new StringObject(vm, doEncrpytBody.getTimestampStr()),
                new StringObject(vm, doEncrpytBody.getBodyStr()),
                new StringObject(vm, doEncrpytBody.getUuidStr()),
                new StringObject(vm, doEncrpytBody.getTokenStr())
        );

        if (result == null) {
            System.err.println("doEncrypt returned null DvmObject!");
            return null;
        }
        Object value = result.getValue();
        if (value == null) {
            System.err.println("doEncrypt DvmObject has null value!");
            return null;
        }
//        System.out.println("doEncrypt returned: " + value);
        return value.toString();
    }

    // JNI Hook - 当Native层试图获取Java静态字段时被调用
    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        // System.out.println("JNI Hook: Native attempting to getStaticObjectField: " + signature);
        switch (signature) {
            case "com/srm/security/SecurityTools->scy_key:Ljava/lang/String;":
//                    System.out.println("JNI Hook: Native getting static field scy_key, returning our preset value.");
                return new StringObject(vm, scy_key_val);
            case "com/srm/security/SecurityTools->scy_iv:Ljava/lang/String;":
//                System.out.println("JNI Hook: Native getting static field scy_iv, returning our preset value.");
                return new StringObject(vm, scy_iv_val);
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    // JNI Hook - 当Native层调用Java的静态方法时被调用
    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "cn/samsclub/app/base/utils/CommonUtils->getPackageName()Ljava/lang/String;":
//                System.out.println("JNI Hook: Simulating cn.samsclub.app.base.utils.CommonUtils.getPackageName()");
                return new StringObject(vm, "cn.samsclub.app"); // 返回真实的包名
            case "cn/samsclub/app/base/utils/CommonUtils->getAppStString()Ljava/lang/String;":
//                System.out.println("JNI Hook: Simulating cn.samsclub.app.base.utils.CommonUtils.getAppStString()");
                // Let's try returning an empty string first.
                // If this doesn't work, we might need to find out what a real app returns.
                // Other options: return new StringObject(vm, "some_meaningful_placeholder_if_known");
                return new StringObject(vm, "308202e5308201cda00302010202045ec18a29300d06092a864886f70d01010b05003022310f300d060355040b13066e6577656767310f300d060355040313066e65776567673020170d3134303231393035343831395a180f33303133303632323035343831395a3022310f300d060355040b13066e6577656767310f300d060355040313066e657765676730820122300d06092a864886f70d01010105000382010f003082010a0282010100bd3015925cd713f203ac9da33970e1ff96d6c2b6d791727eac0013f4717ddc39eccd70152ef14b9627ac00ed7c1ed34cc8dd11bfa2800c2ea5f806c6a906ce3b880fad287231949297a2a4233d9ad666725957b960558bd9fa06e0aa8867d697a2820cdab940acf1fb8eb52e9a0d396d1eafe8bb497ea14af7d1129992751f3669117e63585b287c600cbee66446801ff091e8dcb700b916f3ce2c42c0f62db5fd68107c763940fb6571a3c824bd188754045f638062f4f7931ab7b43e31d0396c6909427cdf5b7db6cde3a83588fe6a376f562de6d5ae864aeaedd8d22af9162bf4ef954b752c6f08d10dd14d57bed6b5736147a50b893c9c0103ced8fafddf0203010001a321301f301d0603551d0e041604141938522ab6ae8723520649a2a8150da6619bc990300d06092a864886f70d01010b05000382010100a321057cdc9d02d10c021297f0acec18697a538f72d0a428b129eccf89119ce6195abc2309dfe1ab2928dcba7884ee4fdb98cacd3f66f1c972885e1244b286f30faadb38a8a8f8e0db3a690aaf48f207217ef9a2b43c78f59810595269468c0ce89576e7d227d71350e1591138935086a878f240ecb4505655f9d75fb563ec4e8a6c10f5aa99adca545c9c422b3bad0cfdaaea92c907a83f24c4db9e3324050962e0f7519ab7a0f5f6d0c844a57f8e72ce41c35a1040ab86f577740b7ecbe5c9d5ece46e97f6a033b1c1e0f2c1af7039a3c6d065f3f005dc0568ff97ff8acf4e057192750eba9fe25ed59198be4f9562d2e52b306ac41ec4547461fad230a010"); // Return an empty string

            // 如果有其他需要mock的静态Java方法调用，在此添加case
        }
        // System.out.println("JNI Hook: callStaticObjectMethodV calling super for: " + signature);
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    public void destroy() {
        try {
            if (emulator != null) {
                emulator.close();
                System.out.println("Emulator closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}