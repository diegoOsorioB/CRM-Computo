import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://228e-189-251-16-3.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLRYU = "https://2aff-2806-265-483-1925-9137-43e8-8ad1-5759.ngrok-free.app/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://228e-189-251-16-3.ngrok-free.app/DatabaseService/api/auth/login";
    private final String URLFPS = "http://192.168.100.52:8080";
    private final String API_URL_1 = "https://c4fe-2806-104e-d-3bcb-e57f-593d-a988-71d7.ngrok-free.app/proveedores/api/products";
    private final String API_URL_2 = "https://255e-2806-2f0-9020-9bac-a12e-9477-8939-d76c.ngrok-free.app/APICRM2/api/productos";

    public String getAPI_URL_1() {
        return API_URL_1;
    }

    public String getAPI_URL_2() {
        return API_URL_2;
    }
    
    
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
