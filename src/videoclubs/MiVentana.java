package videoclubs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MiVentana extends JFrame implements ItemListener {

    static JMenuBar menuPrincipal;
    static JMenuItem salir;
    static JMenuItem borrarFila;
    static JMenuItem guardarFila;

    public MiVentana(String title) {
        super(title);
        setJMenuBar(creaMenu());
        pack();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                salir();
            }
        });
        
        setVisible(true);

    }

    public MiVentana(String title, int ancho, int alto) {
        this(title);
        Dimension t_pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension t_ventana = new Dimension(ancho, alto);

        int x = (t_pantalla.width - t_ventana.width) / 2;
        int y = (t_pantalla.height - t_ventana.height) / 2;
        setBounds(x, y, ancho, alto);

    }

    //..........................................................................
    public JMenuBar creaMenu() {
        menuPrincipal = new JMenuBar();

        //JMenu archivo....
        JMenu archivo = new JMenu("Archivo");
        JMenuItem nuevo = new JMenuItem("Nuevo");
        JMenuItem abrir = new JMenuItem("Abrir");
        JMenuItem guardar = new JMenuItem("Guardar");
        JCheckBoxMenuItem flotar = new JCheckBoxMenuItem("Flotar BH");
        salir = new JMenuItem("Salir");
        salir.setMnemonic(KeyEvent.VK_S);
        salir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        //salir desde el menu bar...............................................
        salir.addActionListener((e) ->  salir() );

        archivo.add(nuevo);
        archivo.add(abrir);
        archivo.add(guardar);
        archivo.addSeparator();
        archivo.add(flotar);
        archivo.addSeparator();
        archivo.add(salir);

        //JMenu look_feel....
        JMenu look_Feel = new JMenu("Look&Feel");
        ButtonGroup opcLF = new ButtonGroup();
        JRadioButtonMenuItem metal, motif, windows, nimbus, mac;
        metal = new JRadioButtonMenuItem("Metal (java)");
            metal.addItemListener(this);
        motif = new JRadioButtonMenuItem("Motif (Unix)");
            motif.addItemListener(this);
        windows = new JRadioButtonMenuItem("Windows");
            windows.addItemListener(this);
        nimbus = new JRadioButtonMenuItem("Nimbus");
            nimbus.addItemListener(this);
        mac = new JRadioButtonMenuItem("Mac");
            mac.addItemListener(this);

        //Button Group..........
        opcLF.add(metal);
        opcLF.add(motif);
        opcLF.add(windows);
        opcLF.add(nimbus);
        opcLF.add(mac);
        
        //JMenu Mantenimiento....
        JMenu mantenimiento = new JMenu("Mantenimiento");
        borrarFila = new JMenuItem("Borrar Película");
        guardarFila = new JMenuItem("Guardar Película");
        
        mantenimiento.add(borrarFila);
        mantenimiento.add(guardarFila);
        
        
        //JMenu.................
        look_Feel.add(metal);
        look_Feel.add(motif);
        look_Feel.add(windows);
        look_Feel.add(nimbus);
        look_Feel.add(mac);
        
        
        menuPrincipal.add(archivo);
        menuPrincipal.add(look_Feel);
        menuPrincipal.add(mantenimiento);
        
        return menuPrincipal;
    }

    //..........................................................................
    public void salir() {
        int seleccion = JOptionPane.showConfirmDialog(rootPane,
                "¿Seguro de que desea salir?",
                "Saliendo...",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (seleccion == JOptionPane.OK_OPTION) {
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(rootPane, "Volviendo...", "Salida Cancelada", JOptionPane.ERROR_MESSAGE);
        }
    }

    //..........................................................................
    public void cambiarApariencia(ItemEvent e, MiVentana ventana){
        String item = ((JRadioButtonMenuItem) e.getSource()).getText();
        String apariencia = "";
        
        switch(item){
            case "Metal (java)":
                apariencia = "javax.swing.plaf.metal.MetalLookAndFeel";
                break;
                
            case "Motif (Unix)":
                apariencia = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                break;
            
            case "Windows":
                apariencia = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                break;
            
            case "Nimbus":
                apariencia = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
                break; 
            
            case "Mac":
                apariencia = "javax.swing.plaf.mac.MacLookAndFeel";
                break;
        }
        
        try {
            UIManager.setLookAndFeel(apariencia);
        } catch (Exception ex) {
            System.out.println("Error cambiando apariencia");
        }
        
        ventana.getContentPane().repaint();
        JOptionPane.showMessageDialog(rootPane, "Ha cambiado la apariencia a: " + item);
    }

    //..........................................................................
    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED){
            cambiarApariencia(e, this);
        }
    }
}
