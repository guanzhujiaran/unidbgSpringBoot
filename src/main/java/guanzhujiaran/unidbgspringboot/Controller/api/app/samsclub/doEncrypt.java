package guanzhujiaran.unidbgspringboot.Controller.api.app.samsclub;

import guanzhujiaran.unidbgspringboot.Model.api.app.samsclub.DoEncrpytBody;
import guanzhujiaran.unidbgspringboot.Model.api.app.samsclub.DoEncryptKey;
import guanzhujiaran.unidbgspringboot.Service.app.comm.Encryptor;
import guanzhujiaran.unidbgspringboot.Service.app.samsclub.SamClubEncrypt;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "山姆会员商店app加密")
@RestController
@RequestMapping(value = "/api/app/samsclub")
public class doEncrypt {
    private final SamClubEncrypt samClubDoEncrypt = Encryptor.getInstance().getSamClubEncryptor();

    @PostMapping("/doEncrypt")
    public String StringDoEncrypt(
            @RequestBody DoEncrpytBody doEncrpytBody
    ) {
        return samClubDoEncrypt.doEncrypt(doEncrpytBody);
    }

    @PostMapping("/updateKey")
    public Boolean updateKey(
            @RequestBody DoEncryptKey doEncryptKey
    ) {
        samClubDoEncrypt.updateKey(
                doEncryptKey.getSiv(),
                doEncryptKey.getSsk(),
                doEncryptKey.getSrd()
        );
        return true;
    }
}