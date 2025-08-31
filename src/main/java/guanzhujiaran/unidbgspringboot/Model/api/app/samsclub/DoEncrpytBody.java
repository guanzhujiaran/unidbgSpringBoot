package guanzhujiaran.unidbgspringboot.Model.api.app.samsclub;

import lombok.Data;

@Data
public class DoEncrpytBody {
    private String timestampStr;
    private String bodyStr;
    private String uuidStr;
    private String tokenStr;
}

