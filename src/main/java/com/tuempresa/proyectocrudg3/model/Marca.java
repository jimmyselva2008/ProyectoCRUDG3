package com.tuempresa.proyectocrudg3.model;

public class Marca {
    private int idMarca;
    private String nombre;

    //Constructor Vacio
    public Marca() {
    }

    //Constructors
    public Marca(int idMarca, String nombre) {
        this.idMarca = idMarca;
        this.nombre = nombre;
    }

    //Getters and Setters
    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
