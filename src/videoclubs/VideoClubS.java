package videoclubs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class VideoClubS extends MiVentana {

    private static Connection conn;
    private static JTabbedPane windows;
    private static JComboBox filterColumn;
    private static JPanel lookingFilms;
    private static JTextField filterData;
    private static final JScrollPane scrollTabla = new JScrollPane();
    private static JTable tablaPelis;
    private static final HashMap<Integer, String[]> reparto = new HashMap<>();
    private static JTextField contadorFilas;
    private static JTextField filasTotales;
    private static int todasLasFilas;
    private static JLabel etiquetaActual;
    private static JLabel etiquetaDragged;
    private static String nameImgDragged;
    private static String[] generos = {"COMEDIA", "TERROR", "SUSPENSE", "DRAMA", "AVENTURAS", "BELICA", "CIENCIA FICCION"};
    private static final int[] anchos = {0, 160, 120, 75, 35, 35, 0, 0};

    public static void main(String[] args) {
        new VideoClubS("VideoClub Martín Rivero", 1020, 690);
    }

    public VideoClubS(String title, int ancho, int alto) {
        super(title, ancho, alto);
        conn = (new LoginBD(this, true)).getConn();

        contentFrame(this);
        //setResizable(false);
    }

    private static void contentFrame(MiVentana frame) {
        windows = new JTabbedPane();

        //Pestaña de buscar la película
        windows.addTab("Buscar Películas", paneLookFilm());

        //Pestaña de añadir películas 
        windows.addTab("Añadir Películas", paneAddFilm());

        frame.add(windows);
        frame.setVisible(true);
    }

    //-----------------------------PESTAÑA BUSCAR PELICULA-----------------------------------------------------
    private static JPanel paneLookFilm() {
        lookingFilms = new JPanel(new BorderLayout());
        GridBagConstraints gbc;
        updateComboFilters();

        //Parte Superior con los filtros........................................
        JPanel right_al = new JPanel();
        right_al.setLayout(new BoxLayout(right_al, BoxLayout.X_AXIS));
        JPanel filters = new JPanel();
        filters.setLayout(new GridBagLayout());
        filterData = new JTextField(10);

        gbc = configConstraints(0, 0, 10, 3, 15);
        filters.add(new JLabel("Columnas:"), gbc);

        gbc = configConstraints(0, 1, 3, 10, 10);
        filters.add(filterColumn, gbc);

        gbc = configConstraints(1, 0, 10, 3, 15);
        filters.add(new JLabel("Dato a Buscar:"), gbc);

        gbc = configConstraints(1, 1, 3, 10, 10);
        filters.add(filterData, gbc);

        filterData.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicaFiltrosPelis();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicaFiltrosPelis();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

        });

        right_al.add(filters);
        right_al.add(Box.createHorizontalStrut(400));

        lookingFilms.add(right_al, BorderLayout.NORTH);

        //Parte Central con la tabla, actores y caractula.......................
        //Columna Tabla....................................................
        JPanel panelCentral = new JPanel();
        tablaPelis = cargaJTable("select * from peliculas", scrollTabla, anchos);
        scrollTabla.setPreferredSize(new Dimension(440, 250));
        
        //Columna Actores.........................
        cargaActores();
        JPanel actoresCaratula = new JPanel();
        actoresCaratula.setLayout(new GridBagLayout());
        JTextArea text_actores = new JTextArea(15, 12);
        text_actores.setEditable(false);
        text_actores.setBorder(new LineBorder(Color.BLACK));

        gbc = configConstraints(0, 0, 5, 5, GridBagConstraints.SOUTH);
        actoresCaratula.add(new JLabel("Actores..."), gbc);
        gbc = configConstraints(0, 1, 5, 5, GridBagConstraints.CENTER);
        actoresCaratula.add(text_actores, gbc);

        //Columna Caratula...........................
        JLabel img_caratula = new JLabel("Carátula no Disponible");
        img_caratula.setPreferredSize(new Dimension(200, 230));
        img_caratula.setBorder(new LineBorder(Color.magenta));

        gbc = configConstraints(1, 0, 5, 5, GridBagConstraints.SOUTH);
        actoresCaratula.add(new JLabel("Carátula..."), gbc);
        gbc = configConstraints(1, 1, 5, 5, GridBagConstraints.CENTER);
        actoresCaratula.add(img_caratula, gbc);

        //Contador de filas con el filtro......................
        JPanel panelContadores = new JPanel();
        panelContadores.setLayout(new BoxLayout(panelContadores, BoxLayout.X_AXIS));

        contadorFilas = new JTextField(3);
        contadorFilas.setEditable(false);
        contadorFilas.setBackground(Color.LIGHT_GRAY);
        contadorFilas.setHorizontalAlignment(JTextField.CENTER);
        contadorFilas.setText(Integer.toString(tablaPelis.getRowCount()));

        filasTotales = new JTextField(3);
        filasTotales.setEditable(false);
        filasTotales.setBackground(Color.LIGHT_GRAY);
        filasTotales.setHorizontalAlignment(JTextField.CENTER);
        filasTotales.setText(Integer.toString(todasLasFilas));

        panelContadores.add(contadorFilas);
        panelContadores.add(new JLabel("   De   "));
        panelContadores.add(filasTotales);
        panelContadores.add(Box.createHorizontalStrut(400));

        //Mouse Listener de la tabla............................................
        tablaPelis.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                KeyEvent ke = new KeyEvent(tablaPelis, KeyEvent.KEY_RELEASED, 1, 1, 1);
                tablaPelis.dispatchEvent(ke);
                //actActoresCaratula(text_actores, img_caratula);
            }

        });

        //Key Listener de la tabla..............................................
        tablaPelis.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                actActoresCaratula(text_actores, img_caratula);
            }
        });

        //Config del JMenu Mantenimiento........................................
        borrarFila.addActionListener((e) -> {
            int rowSelec = tablaPelis.getSelectedRow();
            if (rowSelec != 1) {
                String tituloPeli = (String) tablaPelis.getValueAt(rowSelec, 1);
                if (aceptaCancela("Borrando Película", "¿Seguro/a de que quiere borrar la película " + tituloPeli + " ?", tituloPeli + " eliminado", "Borrado Cancelado")) {
                    ((DefaultTableModel) tablaPelis.getModel()).removeRow(rowSelec);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione la película que quiere borrar.", "Error Borrando Película", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelCentral.add(scrollTabla);
        panelCentral.add(actoresCaratula);
        panelCentral.add(panelContadores);

        //Películas de actor....................................................
        JPanel panelActor = new JPanel();
        panelActor.setBorder(new TitledBorder(new LineBorder(Color.BLUE), "Películas de Actor"));
        JTextField tf_nombreActor = new JTextField(10);
        JScrollPane scroll_Actores = new JScrollPane();
        scroll_Actores.setPreferredSize(new Dimension(150, 170));

        DefaultListModel dlm_ActoresTotales = devuelveListModelAct();
        ordenaJList(dlm_ActoresTotales);
        JList list_actores = new JList(dlm_ActoresTotales);

        scroll_Actores.setViewportView(list_actores);

        JTextArea pelisDeActor = new JTextArea(10, 35);
        pelisDeActor.setBorder(new TitledBorder(new LineBorder(Color.MAGENTA), "Películas: "));
        pelisDeActor.setEditable(false);

        //DocumentListener del textfield de actores para sus películas......................................
        tf_nombreActor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizaActoresFiltro(list_actores, tf_nombreActor);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizaActoresFiltro(list_actores, tf_nombreActor);
                DefaultListModel dlm = (DefaultListModel) list_actores.getModel();
                ordenaJList(dlm);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        //............Listener de la lista de actores para mostrar sus pelis.......................................
        list_actores.addListSelectionListener((e) -> {
            String pelisDeActores = "";
            for (int clave : reparto.keySet()) {
                for (String nombreActor : reparto.get(clave)) {
                    if (nombreActor.equalsIgnoreCase((String) list_actores.getSelectedValue())) {
                        pelisDeActores += "-" + devuelveStringConsulta( "select nombre from peliculas where codigo_pelicula = " + clave, "nombre") + "\n";
                    }
                }
            }    
            pelisDeActor.setText(pelisDeActores);
        });

        JPanel panelIntermedioAct = new JPanel();
        panelIntermedioAct.setLayout(new BoxLayout(panelIntermedioAct, BoxLayout.Y_AXIS));
        panelIntermedioAct.setPreferredSize(new Dimension(200, 50));
        panelIntermedioAct.add(new JLabel("✪ Películas del actor...")).setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        panelIntermedioAct.add(Box.createVerticalStrut(10));
        panelIntermedioAct.add(tf_nombreActor);
        
        panelActor.add(panelIntermedioAct);
        panelActor.add(scroll_Actores);
        panelActor.add(pelisDeActor);

        lookingFilms.add(panelCentral, BorderLayout.CENTER);
        lookingFilms.add(panelActor, BorderLayout.SOUTH);

        return lookingFilms;
    }

    //---------------------------------PESTAÑA AÑADIR PELICULA-----------------------------------------------------------------------------
    private static JPanel paneAddFilm() {
        JPanel paneAddFilm = new JPanel();
        paneAddFilm.setLayout(new GridBagLayout());
        GridBagConstraints gbc;

        //Titulo....
        JPanel p_titulo = new JPanel();
        p_titulo.setPreferredSize(new Dimension(180, 95));
        JTextField tf_titulo = new JTextField(15);
        tf_titulo.setBackground(null);
        tf_titulo.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 5), new LineBorder(Color.black)));

        p_titulo.add(new JLabel("Título...")).setForeground(Color.BLUE);
        p_titulo.add(tf_titulo);

        //Género....
        JPanel p_genero = new JPanel();
        p_genero.setPreferredSize(new Dimension(180, 95));
        String[] generosAdd = devuelveArrayConsulta("select distinct genero from peliculas", "Genero");
        JComboBox jc_generos = new JComboBox(generosAdd);
        jc_generos.setPreferredSize(new Dimension(155, jc_generos.getPreferredSize().height));
        JTextField tf_genero = new JTextField(14);
        tf_genero.setBackground(null);
        tf_genero.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 5), new LineBorder(Color.black)));
        jc_generos.addActionListener((e) -> {
            if (jc_generos.getSelectedIndex() != 0) {
                tf_genero.setText((String) jc_generos.getSelectedItem());
            } else {
                tf_genero.setText("");
            }
        });

        p_genero.add(new JLabel("Género...")).setForeground(Color.BLUE);
        p_genero.add(jc_generos);
        p_genero.add(tf_genero);

        //Director....
        JPanel p_director = new JPanel();
        p_director.setPreferredSize(new Dimension(180, 95));
        String[] directores = devuelveArrayConsulta("select distinct director from peliculas", "Director");
        JComboBox jc_director = new JComboBox(directores);
        jc_director.setPreferredSize(new Dimension(155, jc_director.getPreferredSize().height));
        JTextField tf_director = new JTextField(14);
        tf_director.setBackground(null);
        tf_director.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 5), new LineBorder(Color.black)));
        jc_director.addActionListener((e) -> {
            if (jc_director.getSelectedIndex() != 0) {
                tf_director.setText((String) jc_director.getSelectedItem());
            } else {
                tf_director.setText("");
            }
        });

        p_director.add(new JLabel("Director...")).setForeground(Color.BLUE);
        p_director.add(jc_director);
        p_director.add(tf_director);

        //Botones estreno/ publico
        JPanel rad_Buttons = new JPanel();
        rad_Buttons.setLayout(new BoxLayout(rad_Buttons, BoxLayout.Y_AXIS));
        rad_Buttons.setPreferredSize(new Dimension(155, 200));
        //Botones estrenada-no estrenada
        JRadioButton rb_estrenada = new JRadioButton("Estrenada");
        JRadioButton rb_noEstrenada = new JRadioButton("No Estrenada");
        ButtonGroup bg_estreno = new ButtonGroup();

        bg_estreno.add(rb_estrenada);
        bg_estreno.add(rb_noEstrenada);

        //Botones con-sin Publico
        JRadioButton rb_conPublico = new JRadioButton("Con Público");
        JRadioButton rb_sinPublico = new JRadioButton("Sin Público");
        ButtonGroup bg_publico = new ButtonGroup();
        bg_publico.add(rb_conPublico);
        bg_publico.add(rb_sinPublico);

        //Id Pelicula
        JPanel panel_codPeli = new JPanel();
        panel_codPeli.add(new JLabel("Código Peli:"));
        JTextField tfcod_peli = new JTextField(4);
        tfcod_peli.setHorizontalAlignment(JTextField.CENTER);
        tfcod_peli.setText(Integer.toString(devuelveNuevoId()));
        tfcod_peli.setEditable(false);
        tfcod_peli.setBackground(null);
        tfcod_peli.setBorder(new LineBorder(Color.MAGENTA));
        panel_codPeli.add(tfcod_peli);

        //Boton guardar peli
        JButton guardar_peli = new JButton("Guardar Peli");
        guardar_peli.setMaximumSize(new Dimension(110, 110));
        guardar_peli.setPreferredSize(new Dimension(110, 110));
        guardar_peli.setBorder(new LineBorder(Color.BLUE));

        rad_Buttons.add(rb_estrenada);
        rad_Buttons.add(rb_noEstrenada);
        rad_Buttons.add(Box.createVerticalStrut(20));
        rad_Buttons.add(rb_conPublico);
        rad_Buttons.add(rb_sinPublico);
        rad_Buttons.add(Box.createVerticalStrut(20));
        rad_Buttons.add(panel_codPeli);
        rad_Buttons.add(Box.createVerticalStrut(10));
        rad_Buttons.add(guardar_peli);

        //Actores DISPONIBLES
        JPanel actores_disp = new JPanel();
        actores_disp.setPreferredSize(new Dimension(180, 200));
        JScrollPane scrollListAct = new JScrollPane();
        scrollListAct.setPreferredSize(new Dimension(150, 170));
        DefaultListModel listModActDisponibles = devuelveListModelAct();

        ordenaJList(listModActDisponibles);
        JList list_actDispo = new JList(listModActDisponibles);
        scrollListAct.setViewportView(list_actDispo);

        actores_disp.add(new JLabel("Actores Disponibles")).setForeground(Color.BLUE);
        actores_disp.add(scrollListAct);

        //Actores ELEGIDOS
        JPanel actores_selec = new JPanel();
        actores_selec.setPreferredSize(new Dimension(180, 200));
        JScrollPane scrollListActS = new JScrollPane();
        scrollListActS.setPreferredSize(new Dimension(150, 170));
        DefaultListModel listModActSelec = new DefaultListModel();
        JList list_actSelect = new JList(listModActSelec);
        scrollListActS.setViewportView(list_actSelect);

        actores_selec.add(new JLabel("Actores Seleccionados")).setForeground(Color.BLUE);
        actores_selec.add(scrollListActS);

        //Añadir actor a mano:
        JPanel actor_Nuevo = new JPanel();
        actor_Nuevo.setPreferredSize(new Dimension(200, 90));
        JTextField tf_actorNuevo = new JTextField(12);
        tf_actorNuevo.setText("Actor nuevo");

        //Boton añadir actor nuevo
        JButton btn_actorNuevo = new JButton("Añadir ✔");
        btn_actorNuevo.addActionListener((e) -> {
            if (!tf_actorNuevo.getText().isEmpty()) {
                listModActSelec.addElement(tf_actorNuevo.getText());
                ordenaJList(listModActSelec);
                tf_actorNuevo.setText("");
            }
        });

        //Boton borrar actor selec
        ArrayList<String> listActoresSistema = Collections.list(listModActDisponibles.elements());
        JButton btn_borrActor = new JButton("Borrar ❌");
        btn_borrActor.addActionListener((e) -> {
            for (Object actorSelec : list_actSelect.getSelectedValuesList()) {
                String actorS = (String) actorSelec;
                if (listActoresSistema.contains(actorS)) {
                    listModActDisponibles.addElement(actorS);
                    listModActSelec.removeElement(actorS);
                } else {
                    listModActSelec.removeElement(actorS);
                }
            }
            ordenaJList(listModActDisponibles);
        });

        actor_Nuevo.add(tf_actorNuevo);
        actor_Nuevo.add(btn_actorNuevo);
        actor_Nuevo.add(btn_borrActor);

        //Botones traspaso de actores
        JPanel botones_act = new JPanel();
        botones_act.setPreferredSize(new Dimension(50, 70));

        JButton trasp_Derecha = new JButton("→");
        trasp_Derecha.setPreferredSize(new Dimension(50, 30));
        trasp_Derecha.addActionListener((e) -> {
            mueveActores(list_actDispo, listModActDisponibles, listModActSelec);
        });

        JButton trasp_Izquierda = new JButton("←");
        trasp_Izquierda.setPreferredSize(new Dimension(50, 30));
        trasp_Izquierda.addActionListener((e) -> {
            mueveActores(list_actSelect, listModActSelec, listModActDisponibles);
        });

        botones_act.add(trasp_Derecha);
        botones_act.add(trasp_Izquierda);

        //PARTE DE CARATULA
        JPanel p_caratula = new JPanel();
        p_caratula.setPreferredSize(new Dimension(250, 300));
        JButton b_buscarCaratula = new JButton("Buscar Carátula");

        JTextField tf_infoCaratula = new JTextField(15);
        tf_infoCaratula.setForeground(Color.LIGHT_GRAY);
        tf_infoCaratula.setText("Elige Carátula");
        
        JLabel label_Caratula = new JLabel("Sin Carátula");
        label_Caratula.setPreferredSize(new Dimension(200, 230));
        label_Caratula.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2, true));
        label_Caratula.setTransferHandler(new TransferHandler("icon"));
        label_Caratula.addPropertyChangeListener((evt) -> {
            //la tercera comprobacion del if es para que no se de un bucle infinito con el propertyChange 
            if (etiquetaDragged != null && evt.getPropertyName().equals("icon") && !etiquetaDragged.getName().equalsIgnoreCase(nameImgDragged)) {
                nameImgDragged = etiquetaDragged.getName();
                ImageIcon imgIcon = new ImageIcon("src/datos/img/" + nameImgDragged);
                tf_infoCaratula.setText(nameImgDragged);
                imgIcon.setImage(imgIcon.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH));
                label_Caratula.setText(null);
                label_Caratula.setIcon(imgIcon);
            }else{
                
            }
        });

        b_buscarCaratula.addActionListener((e) -> {
            File dirDefecto = new File("src/datos/img");
            JFileChooser archivoImg = new JFileChooser("src/datos/img");
            archivoImg.showOpenDialog(archivoImg);
            File rutaImg = archivoImg.getSelectedFile();
            if (rutaImg != null) {
                ImageIcon caratula = new ImageIcon(rutaImg.getAbsolutePath());
                caratula.setImage(caratula.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH));
                label_Caratula.setIcon(caratula);
                tf_infoCaratula.setText(rutaImg.getName());
            }
        });

        p_caratula.add(new JLabel("Carátula...")).setForeground(Color.BLUE);
        p_caratula.add(b_buscarCaratula);
        p_caratula.add(tf_infoCaratula);
        p_caratula.add(label_Caratula);

        //CONFIGURACION DEL BOTON GUARDAR PELI.................................................
        guardar_peli.addActionListener((e) -> {
            boolean estrenada = true, publico = true;
            boolean tituloVacio = tf_titulo.getText().isEmpty();
            boolean generoVacio = tf_genero.getText().isEmpty();
            boolean directorVacio = tf_director.getText().isEmpty();
            boolean caratulaVacia = tf_infoCaratula.getText().matches("Elige Carátula") || tf_infoCaratula.getText().isEmpty();
            boolean estrenoVacio = false, publicoVacio = false;

            //Almacena el valor que corresponda a estrenada/no estrenada
            if (rb_estrenada.isSelected()) {
                estrenada = true;
            } else if (rb_noEstrenada.isSelected()) {
                estrenada = false;
            } else {
                estrenoVacio = true;
            }

            //Almacena el valor que corresponda a con publico/sin publico
            if (rb_conPublico.isSelected()) {
                publico = true;
            } else if (rb_sinPublico.isSelected()) {
                publico = false;
            } else {
                publicoVacio = true;
            }

            //.........................................................................................
            if (!tituloVacio && !generoVacio && !directorVacio && !caratulaVacia && !estrenoVacio && !publicoVacio) {
                Pelicula nuevaPeli = new Pelicula(tf_titulo.getText(), tf_genero.getText(), tf_director.getText(),
                        tf_infoCaratula.getText(), Integer.parseInt(tfcod_peli.getText()), estrenada, publico);

                //Añadimos una nueva fila a la tabla de la pestaña buscar peli
                ((DefaultTableModel) tablaPelis.getModel()).addRow(new Object[]{
                    nuevaPeli.getCod_peli(), //Columna 1
                    nuevaPeli.getTitulo().toUpperCase(), //Columna 2
                    nuevaPeli.getDirector().toUpperCase(), //...
                    nuevaPeli.getGenero().toUpperCase(),
                    nuevaPeli.isEstrenada(),
                    nuevaPeli.isPublico(),
                    null,
                    nuevaPeli.getCaratula()
                });
                
                filasTotales.setText(Integer.toString(todasLasFilas + 1));
                insertaEnBD(nuevaPeli);
                guardaActoresFichero(listModActSelec, nuevaPeli.getCod_peli());
                ordenaJList(listModActDisponibles);
               

                //Volvemos todo a sus valores por defecto
                tf_titulo.setText("");
                tf_genero.setText("");
                jc_generos.setSelectedIndex(0);
                tf_director.setText("");
                jc_director.setSelectedIndex(0);
                label_Caratula.setIcon(null);
                tf_infoCaratula.setText("Elige Carátula");
                tfcod_peli.setText(Integer.toString(devuelveNuevoId()));
                bg_estreno.clearSelection();
                bg_publico.clearSelection();
                listModActSelec.removeAllElements();
                list_actDispo.setModel(devuelveListModelAct());

            } else {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios. ", "Se encontraron campos vacíos", JOptionPane.ERROR_MESSAGE);
            }
        });

        //Carrusel de Carátulas.................................................
        JScrollPane scrollCaratulas = new JScrollPane();
        JPanel p_carrusel = new JPanel();

        cargaCaratulas(p_carrusel, label_Caratula, tf_infoCaratula);

        scrollCaratulas.setPreferredSize(new Dimension(600, 130));
        scrollCaratulas.setViewportView(p_carrusel);

        //Añadimos todo al panel principal:
        gbc = configConstraints(0, 0, 7, 7, GridBagConstraints.CENTER);
        paneAddFilm.add(p_titulo, gbc);

        gbc = configConstraints(1, 0, 7, 7, GridBagConstraints.CENTER);
        paneAddFilm.add(p_genero, gbc);

        gbc = configConstraints(3, 0, 7, 7, GridBagConstraints.CENTER);
        paneAddFilm.add(p_director, gbc);

        gbc = configConstraints(4, 0, 7, 7, GridBagConstraints.CENTER);
        gbc.gridheight = 2;
        paneAddFilm.add(p_caratula, gbc);

        gbc = configConstraints(0, 1, 7, 7, GridBagConstraints.WEST);
        gbc.gridheight = 1;
        paneAddFilm.add(rad_Buttons, gbc);

        gbc = configConstraints(1, 1, 7, 7, GridBagConstraints.WEST);
        paneAddFilm.add(actores_disp, gbc);

        gbc = configConstraints(2, 1, 7, 7, GridBagConstraints.CENTER);
        gbc.insets = new Insets(7, 0, 7, 0);
        paneAddFilm.add(botones_act, gbc);

        gbc = configConstraints(3, 1, 7, 0, GridBagConstraints.WEST);
        paneAddFilm.add(actores_selec, gbc);

        gbc = configConstraints(3, 2, 0, 7, GridBagConstraints.WEST);
        paneAddFilm.add(actor_Nuevo, gbc);

        gbc = configConstraints(0, 3, 0, 7, GridBagConstraints.CENTER);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        paneAddFilm.add(scrollCaratulas, gbc);

        return paneAddFilm;
    }

    //----------------------------------------------------------------------------------------------------------
    private static void updateComboFilters() {
        Statement stm;
        ResultSet rst;
        String[] colNames;
        ResultSetMetaData metaData;

        try {
            stm = conn.createStatement();
            rst = stm.executeQuery("SELECT * FROM peliculas");
            metaData = rst.getMetaData();
            colNames = new String[metaData.getColumnCount() - 2];
            colNames[0] = "Elige Columna";

            for (int i = 1; i <= metaData.getColumnCount() - 3; i++) {
                colNames[i] = metaData.getColumnName(i + 1);
            }

            filterColumn = new JComboBox(colNames);

        } catch (SQLException ex) {
            System.out.println("Error de SQL -> " + ex.getMessage());
        }

    }

    //---Devuelve un objeto GridBagConstraints con las especificaciones introducidas---------------------------
    private static GridBagConstraints configConstraints(int x, int y, int top, int bottom, int direction) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(top, 7, bottom, 7);
        gbc.anchor = direction;

        return gbc;
    }

    //---------Metodo que carga la tabla con las pelis-----------------------------------------------------
    private static JTable cargaJTable(String consulta, JScrollPane scrollP, int[] anchos) {
        DefaultTableModel dtm;
        JTable tabla = null;

        try {
            ResultSet rset = conn.createStatement().executeQuery(consulta);
            ResultSetMetaData rsetMd = rset.getMetaData();

            int numCols = rsetMd.getColumnCount();
            Object[] etiquetas = new Object[numCols];

            //Muestra los nombres de las columnas...........
            for (int i = 0; i < numCols; i++) {
                etiquetas[i] = rsetMd.getColumnLabel(i + 1);
            }

            dtm = new DefaultTableModel(etiquetas, 0);
            //Añadimos los datos de los pueblos.............
            while (rset.next()) {
                for (int i = 0; i < numCols; i++) {
                    etiquetas[i] = rset.getObject(i + 1);
                }
                dtm.addRow(etiquetas);
            }

            //Creamos la tabla y ponemos que visualice las columnas por tipo y que no sean editables exc la 3
            tabla = new JTable(dtm) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return getValueAt(0, column).getClass();
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 3;
                }
            };
            
            JComboBox generosPeli = new JComboBox(generos);

            //Le ponemos a la columna 3 un editor con un combobox
            tabla.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(generosPeli) {
                @Override
                //Este metodo actua cuando seleccionamos algo en el combo al editar la celda 
                public Object getCellEditorValue() {
                    return (String) generosPeli.getSelectedItem();
                }
            });

            todasLasFilas = tabla.getRowCount();

            //Establecemos un ancho para las columnas definidos por un array
            for (int i = 0; i < anchos.length; i++) {
                tabla.getColumnModel().getColumn(i).setMinWidth(anchos[i]);
                tabla.getColumnModel().getColumn(i).setMaxWidth(anchos[i]);
            }

            tabla.setRowSorter(new TableRowSorter<>(dtm));
            scrollP.setViewportView(tabla);
            rset.close();
        } catch (SQLException ex) {
            System.out.println("ERROR DE SQL ->" + ex.getMessage());
        }

        return tabla;
    }

    //----------------------Método aceptaCancela()------------------------------------------------------------------------------------
    private static boolean aceptaCancela(String titulo, String mensaje, String mensajeAcepta, String mensajeCancela) {
        int opcion = JOptionPane.showConfirmDialog(null, mensaje, titulo, JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, mensajeAcepta);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, mensajeCancela);
            return false;
        }
    }

    //---------Carga Actores en un HashMap en memoria-----------------------------------------------------
    private static void cargaActores() {
        FileReader fr;
        BufferedReader br;
        String linea;
        String[] datosActores;

        try {
            fr = new FileReader("src/datos/actores/actores.txt");
            br = new BufferedReader(fr);

            while ((linea = br.readLine()) != null) {
                datosActores = linea.split("[;|,]");
                reparto.put(Integer.parseInt(datosActores[0]), datosActores);
            }

            fr.close();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR ENCONTRADO EL ARCHIVO ACTORES ->" + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("ERROR DE ENTRADA/SALIDA ->" + ex.getMessage());
        }
    }

    //--------Actualiza el recuadro de actores y la caratula (Pestaña Buscar Pelicula)-----------------------------------------
    private static void actActoresCaratula(JTextArea text_actores, JLabel img_caratula) {
        int rowS = tablaPelis.getSelectedRow();
        int idPelicula = (int) tablaPelis.getValueAt(rowS, 0);

        //Actualiza TextArea de los actores
        if (reparto.containsKey(idPelicula)) {
            String[] actoresArray = reparto.get(idPelicula);
            String actores = "";
            for (int i = 1; i < actoresArray.length; i++) {
                actores += actoresArray[i] + "\n";
            }
            text_actores.setText(actores);
        } else {
            text_actores.setText("Actores no disponibles.");
        }

        //Actualiza Etiqueta de la carátula
        String urlCaratula = (String) tablaPelis.getValueAt(rowS, 7);
        if (!urlCaratula.equals("")) {
            ImageIcon imgC = new ImageIcon("src/datos/img/" + urlCaratula);
            imgC.setImage(imgC.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH));
            img_caratula.setText(null);
            img_caratula.setIcon(imgC);

        } else {
            img_caratula.setIcon(null);
            img_caratula.setText("Carátula no Disponible.");
        }
    }

    //---------Actualiza la lista de actores -----------------------------------------------------------------------------
    private static void actualizaActoresFiltro(JList list_actores, JTextField tf_nombreActor) {
        DefaultListModel dfm = new DefaultListModel();
        HashSet<String> act = devuelveSetActores();
        for (String a : act) {
            if (a.contains(tf_nombreActor.getText())) {
                dfm.addElement(a);
            }
        }
        list_actores.setModel(dfm);
    }

    //----------------------------------------------------------------------------------------------------------
    private static void aplicaFiltrosPelis() {
        TableRowSorter trs = (TableRowSorter) tablaPelis.getRowSorter();
        String selectedCol = (String) filterColumn.getSelectedItem();

        if (filterColumn.getSelectedIndex() != 0) {
            int numFila = tablaPelis.getColumn(selectedCol).getModelIndex();
            trs.setRowFilter(RowFilter.regexFilter(filterData.getText(), numFila));

        } else {
            trs.setRowFilter(RowFilter.regexFilter(Pattern.quote(filterData.getText()), 1));
        }

        tablaPelis.setRowSorter(trs);
        contadorFilas.setText(Integer.toString(tablaPelis.getRowCount()));
    }

    //------------Metodo que devuelve un DefaultListModel con los actores cargados en memoria----------------------
    private static DefaultListModel devuelveListModelAct() {
        DefaultListModel listModel = new DefaultListModel();
        HashSet<String> actoresNoRep = devuelveSetActores();

        actoresNoRep.forEach((act) -> {
            listModel.addElement(act);
        });

        return listModel;
    }

    //-------Método que devuelve un array de string con los datos de una consulta-------------------------------
    private static String[] devuelveArrayConsulta(String consulta, String tipo) {
        Statement stm;
        ResultSet rst;
        String[] resultConsulta = null;
        int cont = 1;

        try {
            stm = conn.createStatement();
            rst = stm.executeQuery(consulta);

            rst.last();
            int numFilas = rst.getRow();
            resultConsulta = new String[numFilas];

            rst.first();

            resultConsulta[0] = "Elige " + tipo;

            while (rst.next()) {
                resultConsulta[cont] = (String) rst.getObject(tipo.toLowerCase());
                cont++;
            }

            stm.close();
            rst.close();
        } catch (SQLException ex) {
            System.out.println("Error de SQL -> " + ex.getMessage());
        }

        return resultConsulta;
    }

    //-----Método que devuelve un String (igual que el anterior pero para búsquedas de una sola row)------------
    private static String devuelveStringConsulta(String consulta, String tipo) {
        Statement stm;
        ResultSet rst;
        String resultConsulta = null;

        try {
            stm = conn.createStatement();
            rst = stm.executeQuery(consulta);

            rst.next();
            resultConsulta = (String) rst.getObject(tipo.toLowerCase());

            stm.close();
            rst.close();
        } catch (SQLException ex) {
            System.out.println("Error de SQL -> " + ex.getMessage());
        }

        return resultConsulta;
    }

    //----Devuelve un set con los actores cargados en memoria------------------------------------------------
    private static HashSet<String> devuelveSetActores() {
        HashSet<String> actoresNoRep = new HashSet();

        //Añadimos un hashset para que no se repitan en el model los actores
        reparto.values().forEach((actores) -> {
            for (int i = 1; i < actores.length; i++) {
                actoresNoRep.add(actores[i]);
            }
        });

        return actoresNoRep;
    }

    //----------------------------------------------------------------------------------------------------------
    private static int devuelveNuevoId() {
        Statement stm;
        ResultSet rst;
        int nuevoId = -1;

        try {
            stm = conn.createStatement();
            rst = stm.executeQuery("select max(codigo_pelicula) from peliculas");

            rst.next();
            nuevoId = (int) rst.getObject(1);

            stm.close();
            rst.close();

        } catch (SQLException ex) {
            System.out.println("Error de SQL -> " + ex.getMessage());
        }

        return nuevoId + 1;
    }

    //---------Metodo que mueve los actores seleccionados de una lista a otra------------------------------------
    private static void mueveActores(JList listSeleccionada, DefaultListModel modelSelecList, DefaultListModel modelNewList) {
        String actor;

        for (Object actorSelec : listSeleccionada.getSelectedValuesList()) {
            actor = (String) actorSelec;
            modelNewList.addElement(actor);
            modelSelecList.removeElement(actor);
        }
        
        ordenaJList(modelNewList);
        ordenaJList(modelSelecList);
    }

    //------------Ordena Alfabeticamente un JList-------------------------------------------------------------------
    private static void ordenaJList(DefaultListModel listModel) {
        ArrayList<String> listActores = Collections.list(listModel.elements());
        Collections.sort(listActores);

        listModel.removeAllElements();
        listActores.forEach((actor) -> {
            listModel.addElement(actor);
        });
    }

    //-----------------------Inserta una película en la BD----------------------------------------------------------
    private static void insertaEnBD(Pelicula nuevaPeli) {
        PreparedStatement ps;

        try {
            ps = conn.prepareStatement("insert into peliculas values (?,?,?,?,?,?,sysdate(),?)");

            ps.setInt(1, nuevaPeli.getCod_peli());
            ps.setString(2, nuevaPeli.getTitulo().toUpperCase());
            ps.setString(3, nuevaPeli.getDirector().toUpperCase());
            ps.setString(4, nuevaPeli.getGenero().toUpperCase());
            ps.setBoolean(5, nuevaPeli.isEstrenada());
            ps.setBoolean(6, nuevaPeli.isPublico());
            ps.setString(7, nuevaPeli.getCaratula());

            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(null, nuevaPeli.getTitulo() + " añadida a la BD", "Película añadida correctamente.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            System.out.println("Error de SQL -> " + ex.getMessage());
        }
    }

    //-----------------------Guarda los actores seleccionados en el fichero------------------------------------------
    private static void guardaActoresFichero(DefaultListModel listModel, int idPelicula) {
        ArrayList<String> listActores = Collections.list(listModel.elements());

        if (!listActores.isEmpty()) {
            String lineaActores = "\n" + idPelicula + ";";

            for (Object actor : listActores) {
                lineaActores += ((String) actor) + ",";
            }

            try {
                FileWriter fw = new FileWriter("src/datos/actores/actores.txt", true);
                PrintWriter pw = new PrintWriter(fw);
                pw.print(lineaActores);

                pw.flush();
                pw.close();

                cargaActores();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(VideoClubS.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(VideoClubS.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //----------------------Carga las caratulas en el carrusel------------------------------------------------------------
    private static void cargaCaratulas(JPanel p_carrusel, JLabel imgCaratula, JTextField tf_infoCaratula) {
        File dirCaratulas = new File("src/datos/img");
        String[] rutaPelis = dirCaratulas.list();
        p_carrusel.setLayout(new GridLayout(1, rutaPelis.length));
        JLabel labImg;
        ImageIcon imgIcon;

        for (int i = 0; i < rutaPelis.length; i++) {
            imgIcon = new ImageIcon("src/datos/img/" + rutaPelis[i]);
            imgIcon.setImage(imgIcon.getImage().getScaledInstance(100, 110, Image.SCALE_SMOOTH));
            labImg = new JLabel(imgIcon);

            labImg.setName(rutaPelis[i]);
            labImg.setPreferredSize(new Dimension(120, 110));
            
            //Evento del Drag.................................................
            labImg.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    JLabel c = (JLabel) e.getSource();
                    etiquetaDragged = c;
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }

            });

            labImg.setTransferHandler(new TransferHandler("icon"));
            
            //
            labImg.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e); 
                    if(e.getComponent()!= etiquetaActual){
                        ((JLabel)e.getComponent()).setBorder(new LineBorder(Color.RED, 2, true));
                        etiquetaActual.setBorder(null);
                        etiquetaActual = (JLabel)e.getComponent();
                    }
                    
                    if(e.getClickCount() == 2){
                        JLabel labelSelec = (JLabel) e.getComponent();
                        ImageIcon img = new ImageIcon("src/datos/img/" + labelSelec.getName());
                        tf_infoCaratula.setText(labelSelec.getName());
                        img.setImage(img.getImage().getScaledInstance(200, 230, Image.SCALE_SMOOTH));
                        imgCaratula.setIcon(img);
                        imgCaratula.setText(null);
                    }
                }
                
            });
            
            p_carrusel.add(labImg);
            etiquetaActual = labImg;

        }
    }

    //----------------------------------------------------------------------------------------------------------
}
