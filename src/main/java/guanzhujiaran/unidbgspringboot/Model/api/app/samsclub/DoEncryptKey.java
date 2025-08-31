package guanzhujiaran.unidbgspringboot.Model.api.app.samsclub;

import lombok.Data;

@Data
public class DoEncryptKey {
    private String siv;
    private String ssk;
    private String srd;
}
