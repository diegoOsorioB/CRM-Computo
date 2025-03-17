import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.validator.FacesValidator;

@FacesValidator("telefonoValidator")
public class TelefonoValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String telefono = (String) value;
        if (telefono != null && !telefono.matches("^\\d{10}$")) {
            FacesMessage msg = new FacesMessage("Teléfono inválido", "El teléfono debe contener exactamente 10 dígitos numericos.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
