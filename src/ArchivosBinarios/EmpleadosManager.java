package ArchivosBinarios;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

public class EmpleadosManager {

    //Read mueve el puntero
    //Write solo lo sobreescribe
    //Seek solo es para mover el puntero
    private RandomAccessFile rcods, remps;
    String imprimir;
    
    public EmpleadosManager() {
        try {
            //1- Asegurar que el folder company exista
            File f = new File("company");
            f.mkdir();
            //2- Instanciar las RAFs dentro company 
            rcods = new RandomAccessFile("company/codigo.emp", "rw");
            remps = new RandomAccessFile("company/empleados.emp", "rw");
            //3- Inicializar el archivo de codigo si es nuevo
            initCode();

        } catch (IOException e) {
            System.out.println("No deberia de pasar esto.");
        }
    }

    private void initCode() throws IOException {
        if (rcods.length() == 0) {
            rcods.writeInt(1);
        }
    }

    private int getCode() throws IOException {
        rcods.seek(0);
        int code = rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }

    public void addEmployee(String name, double salary) throws IOException {
        /*
        Formato:
        codigo
        Nombre
        Salario
        Fecha Contratacion
        Fecha Despido
         */
        remps.seek(remps.length());
        int code = getCode();
        remps.writeInt(code);
        remps.writeUTF(name);
        remps.writeDouble(salary);
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        remps.writeLong(0);
        //Aseguramos sus archivos individuales
        createEmployeeFolders(code);
    }

    private String employeeFolder(int code) {
        return "company/empleado" + code;
    }

    private void createEmployeeFolders(int code) throws IOException {
        //Crear folder empleado+code
        File edir = new File(employeeFolder(code));
        edir.mkdir();
        //Crear los arhivos de las ventas
        createYearSalesFileFor(code);

    }

    private RandomAccessFile salesFileFor(int code) throws IOException {
        String dirPadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = dirPadre + "/ventas" + yearActual + ".emp";
        return new RandomAccessFile(path, "rw");

    }

    private void createYearSalesFileFor(int code) throws IOException {
        RandomAccessFile ryear = salesFileFor(code);
        if (ryear.length() == 0) {
            for (int m = 0; m < 12; m++) {
                ryear.writeDouble(0);
                ryear.writeBoolean(false);

            }
        }
    }


    /*
    Imprime:
    Realizar una lista de empleados NO Despedidos con la siguiente estructura
    Codigo - Nombre - Salario - Fecha Contratacion
     */
    public void imprimirEmpleadosNoDespedidos() throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() != remps.length()) {
            // Leer toda la informacion del empleado

            /*
             * Formato:
             * Codigo
             * Nombre
             * Salario
             * Fecha Contratacion
             * Fecha Despido
             */
            int code = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            Date fechaContratacion = new Date(remps.readLong());
            double millisDespido = remps.readLong();
            if (millisDespido == 0) {
                continue;
            }

            String date = new SimpleDateFormat("dd-mm-yyyy").format(fechaContratacion);
            imprimir+=code + " - " + name + " - $" + salary + " - " + date+"\n  ";
        }
    }

    private boolean isEmployeeActive(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int cod = remps.readInt();
            long pos = remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if (remps.readLong() == 0 && cod == code) {
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }

    public boolean fireEmployee(int code) throws IOException {
        if (isEmployeeActive(code)) {
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: " + name);
            return true;
        }
        return false;
    }

    public void addSalestoEmployee(int code,double sales)throws IOException{
        RandomAccessFile sYear=salesFileFor(code);
        sYear.seek(0);
        
        int pos=0;
            for(int i=0;i<Calendar.getInstance().get(Calendar.MONTH);i++){
                sYear.skipBytes(9);
                pos+=5;
            }
            
            if(sYear.readBoolean()==false){
            sYear.seek(pos);
            sYear.writeBoolean(true);
            sYear.writeDouble(sales);
            payEmployee(code);
            
            }
   }

    private RandomAccessFile billsFilefor(int code) throws IOException {
        String recibo = employeeFolder(code);
        String path = recibo + "/recibo.emp";
        return new RandomAccessFile(path, "rw");
    }

    private double salary(int code) throws IOException {
           remps.seek(0);
        double salario = 0;

        while (remps.getFilePointer() < remps.length()) {
            int codigo = remps.readInt();
            remps.readUTF();
            double salary = remps.readDouble();
            remps.skipBytes(16);
            if (codigo == code) {
                salario = salary;
            }
        }
        return salario;

    }

  
    public void payEmployee(int code) throws IOException {
        RandomAccessFile filesp = billsFilefor(code);
        RandomAccessFile ryear = salesFileFor(code);
        ryear.seek(0);
        int pos = 0;
        for (int i = 0; i < Calendar.getInstance().get(Calendar.MONTH); i++) {
            ryear.skipBytes(9);
            pos += 9;
        }
        if(ryear.readBoolean()==false){
        
        ryear.seek(pos);
        ryear.writeBoolean(true);
        double venta = ryear.readDouble();
        
        filesp.seek(filesp.length());
        double salario = salary(code);

        filesp.writeLong(new Date().getTime());
        filesp.writeDouble(salario + (venta * 0.10));
        filesp.writeDouble(salario - (salario * 0.35));
        filesp.writeInt(Calendar.getInstance().get(Calendar.YEAR));
        filesp.writeInt(Calendar.getInstance().get(Calendar.MONTH));
        JOptionPane.showMessageDialog(null, "¡Se ha pagado al empleado!");
        }else{
            
        JOptionPane.showMessageDialog(null, "¡Error: Ya se le pagó a este empleado!");
    }
    }


}
