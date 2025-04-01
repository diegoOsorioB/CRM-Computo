import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://4c6e-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLERPpro ="https://255e-2806-2f0-9020-9bac-a12e-9477-8939-d76c.ngrok-free.app/APICRM2/api/productos";
    private final String URLRYU = "https://cf84-2806-265-483-1925-b91e-df86-6bd9-ff3d.ngrok-free.app/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://4c6e-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/auth/login";
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

    public String getURLERPpro() {
        return URLERPpro;
    }
    
    
    
}
