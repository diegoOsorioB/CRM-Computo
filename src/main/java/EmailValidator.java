import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.validator.FacesValidator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacesValidator("emailValidator")
public class EmailValidator implements Validator {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private Pattern pattern;
    private Matcher matcher;

    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String email = (String) value;
        System.out.println("Validando email: " + email); // Depuración: verificar el valor

        if (email != null && !email.isEmpty()) {
            matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                FacesMessage msg = new FacesMessage("Correo electrónico inválido", "El correo electrónico no tiene un formato válido.");
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                System.out.println(msg); // Depuración: verificar si el mensaje se genera correctamente
                throw new ValidatorException(msg);
            }
        }
    }
}
