import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://afef-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLRYU = "https://7bb8-2806-265-401-8d89-95bb-7ef7-8189-582f.ngrok-free.app/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://afef-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/auth/login";
    
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
}
