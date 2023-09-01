package ArchivosBinarios;

public class MainEmpleado {
    
    public static void main(String[] args) {
        EmpleadosManager em = new EmpleadosManager();
        MenuPrincipal mp = new MenuPrincipal(em);
            mp.setVisible(true);
        }
    }
    

