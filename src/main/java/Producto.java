import java.util.Objects;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private String imagen;
    private double precio;
    private int stock;
<<<<<<< HEAD
    private String status;
    private String category;

    public Producto() {}
=======


>>>>>>> Francisco

    public Producto(String nombre, String descripcion, String imagen, double precio, String id, int stock, String category, String status) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.precio = precio;
        this.stock = stock;
<<<<<<< HEAD
        this.category = category;
        this.status = status;
=======
>>>>>>> Francisco
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

<<<<<<< HEAD
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
=======
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
    

    public void setId(int id) {
        this.id = id;
    }
>>>>>>> Francisco

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
