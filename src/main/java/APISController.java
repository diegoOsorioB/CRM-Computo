import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://9108-2806-104e-16-cde4-52c-33a3-9252-c9d8.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    private final String URLRYU = "http://10.250.2.7:8080/Roles_Usuarios_API/api/usuarios/registrar";
    private final String URLLOGIN = "https://9108-2806-104e-16-cde4-52c-33a3-9252-c9d8.ngrok-free.app/DatabaseService/api/auth/login";
    private final String URLFPS = "http://10.250.3.113:8080";
    private final String API_URL_1 = "http://localhost:8080/APICRM2/api/productos";
    private final String API_URL_2 = "http://10.250.2.194:8080/proveedores2/api/products";

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
