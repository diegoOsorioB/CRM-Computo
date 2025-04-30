import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://0c37-2806-2f0-a160-fe13-34e0-883c-1a5e-86c6.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "http://10.250.0.116:8080/destinity-erp/api/products/all";
    private final String URLRYU = "http://10.250.2.7:8080/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://0c37-2806-2f0-a160-fe13-34e0-883c-1a5e-86c6.ngrok-free.app/DatabaseService/api/auth/login";
    private final String URLFPS = "http://10.250.3.113:8080";
    private final String API_URL_1 = "http://10.250.0.116:8080/destinity-erp/api/products/all";
    private final String API_URL_2 = "https://f183-2806-265-48d-1ca-d93b-ce27-47c-5409.ngrok-free.app/proveedores2/api/products";

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
