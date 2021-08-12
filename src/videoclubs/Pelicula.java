
package videoclubs;

public class Pelicula {
    private String titulo;
    private String genero;
    private String director;
    private String caratula;
    private int cod_peli;
    private boolean estrenada;
    private boolean publico;

    //Constructores.............................................................
    public Pelicula() {
    }
    
    public Pelicula(String titulo, String genero, String director, String caratula, int cod_peli, boolean estrenada, boolean publico) {
        this.titulo = titulo;
        this.genero = genero;
        this.director = director;
        this.caratula = caratula;
        this.cod_peli = cod_peli;
        this.estrenada = estrenada;
        this.publico = publico;
    }
    
    //Getters y Setters............................................................
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCaratula() {
        return caratula;
    }

    public void setCaratula(String caratula) {
        this.caratula = caratula;
    }

    public int getCod_peli() {
        return cod_peli;
    }

    public void setCod_peli(int cod_peli) {
        this.cod_peli = cod_peli;
    }

    public boolean isEstrenada() {
        return estrenada;
    }

    public void setEstrenada(boolean estrenada) {
        this.estrenada = estrenada;
    }

    public boolean isPublico() {
        return publico;
    }

    public void setPublico(boolean publico) {
        this.publico = publico;
    }
    
    
    
}
