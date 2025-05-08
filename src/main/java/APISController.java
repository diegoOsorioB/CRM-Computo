import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://40b4-2806-2f0-a160-fe13-d6d-46b9-379d-81be.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLRYU = "https://bdff-2806-265-483-1f4-50d7-e36e-edce-d893.ngrok-free.app/Roles_Usuarios_API/api/clientes/registrar";
    private final String URLLOGIN = "https://40b4-2806-2f0-a160-fe13-d6d-46b9-379d-81be.ngrok-free.app/DatabaseService/api/auth/login";
    private final String URLFPS = "http://192.168.100.52:8080";
    
    
    public String getURLBD(){
        return URLBD;
    }
    
    public String getURLERP(){
        return URLERP;
    }
    
    public String getURLRYU(){
        return URLRYU;
    }
    
    public String getURLLOGIN(){
        return URLLOGIN;
    }
     public String getURLFPS(){
        return URLFPS;
    }
}
