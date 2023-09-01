package ArchivosBinarios;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

public class EmpleadosManager {

    //Read mueve el puntero
    //Write solo lo sobreescribe
    //Seek solo es para mover el puntero
    private RandomAccessFile rcods, remps;

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
    private void imprimirEmpleadosNoDespedidos() throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int code = rcods.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            Date fecha = new Date(remps.readLong());
            if (remps.readLong() == 0) {
                System.out.println(code + " - " + name + " - $ " + salary + " - " + fecha);
            }

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

    public void addSaleToEmployee(int code, double sale) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int cod = remps.readInt();
            long pos = remps.getFilePointer();
            if (cod == code && isEmployeeActive(code)) {
                int mes = Calendar.getInstance().get(Calendar.MONTH);
                salesFileFor(code).seek(0);
                while (salesFileFor(code).getFilePointer() < 12) {
                    if (salesFileFor(code).getFilePointer() == mes) {
                        double salarioSumado = salesFileFor(code).readDouble() + sale;
                        salesFileFor(code).writeDouble(salarioSumado);

                    }
                }
            }
        }
    }
}
