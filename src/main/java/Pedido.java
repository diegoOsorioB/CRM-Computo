import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Pedido implements Serializable {
    private static final AtomicInteger contador = new AtomicInteger(1); // Generador de IDs
    
    private int id;
    private List<ItemCarrito> items;
    private double total;
    private String estado;
    private String direccion;

    public Pedido(List<ItemCarrito> items, double total, String estado, String direccion) {
        this.id = contador.getAndIncrement(); // Asigna un ID único automáticamente
        this.items = items;
        this.total = total;
        this.estado = estado;
        this.direccion = direccion;
    }

    public int getId() {
        return id;
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
}
