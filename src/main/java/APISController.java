import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://b1d0-2806-104e-16-b7ce-6bac-f5a0-b133-2795.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLRYU = "https://2aff-2806-265-483-1925-9137-43e8-8ad1-5759.ngrok-free.app/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://b1d0-2806-104e-16-b7ce-6bac-f5a0-b133-2795.ngrok-free.app/DatabaseService/api/auth/login";
    private final String URLFPS = "http://192.168.100.52:8080";
    private final String API_URL_1 = "https://84d3-2806-104e-d-19f5-ec59-b82f-101d-94c2.ngrok-free.ap8p/proveedores/api/products";
    private final String API_URL_2 = "https://9458-2806-265-48d-1ca-f870-3283-ff65-c590.ngrok-free.ap8p/proveedores2/api/products";

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
