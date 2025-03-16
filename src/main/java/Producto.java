

import java.io.Serializable;
import java.util.Objects;

public class Producto implements Serializable {
    private int id;
    private String nombre;
    private String descripcion;
    private String imagen;
    private double precio;
    private int cantidad;

    public Producto(String nombre, String descripcion, String imagen, double precio, int id) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.precio = precio;
        this.cantidad = 1;
    }

    public int getId(){return id;}
    public void setId(int id){this.id = id;}
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return id == producto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, imagen);
    }

}