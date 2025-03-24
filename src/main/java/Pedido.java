import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Pedido implements Serializable {
    private static final AtomicInteger contador = new AtomicInteger(1);

    private String id;
    private List<ItemCarrito> items;
    private double total;
    private String estado;
    private LocalDate fecha;
    private String direccion;
    private String correoUsuario; // Nuevo atributo

    // Constructor con la fecha actual
    public Pedido(String id,List<ItemCarrito> items, double total, String estado, String direccion, String correoUsuario) {
        this.id = id; 
        this.items = items;
        this.total = total;
        this.estado = estado;
        this.direccion = direccion;
        this.correoUsuario = correoUsuario;
        this.fecha = LocalDate.now(); // Asigna la fecha actual al pedido
    }

    public Pedido() {
        // Constructor vacío para serialización
        this.fecha = LocalDate.now(); // Asigna la fecha actual por defecto
    }

    // Getters y setters para los atributos

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }
}
