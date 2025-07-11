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
    private final String API_URL_1 = "https://2f21-189-138-139-117.ngrok-free.app/destinity-erp/api/products/all";
    private final String API_URL_2 = "https://f1e8-2806-2a0-1018-3fea-8d81-a91-ba00-1e7e.ngrok-free.app/Proyecto_2.0/api/productos";

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
