import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class APISController {
    private final String URLBD = "https://afef-2806-104e-16-1f1-a261-a504-737d-f220.ngrok-free.app/DatabaseService/api/service";
    private final String URLERP = "https://f718-2806-104e-21-9c0b-29cc-6273-130d-c65e.ngrok-free.app/destinity/erp/api/";
    
    public String getURLBD(){
        return URLBD;
    }
    
    public String getURLERP(){
        return URLERP;
    }
}
