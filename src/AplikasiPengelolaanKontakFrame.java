import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AplikasiPengelolaanKontakFrame extends javax.swing.JFrame {

    private DefaultTableModel tableModel;
    public AplikasiPengelolaanKontakFrame() {
        initComponents();
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Telepon", "Kategori"}, 0);
        contactTable.setModel(tableModel);
    }
    
    public class DatabaseHelper {
    public Connection connect() {
        Connection conn = null;
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:dbkontak.sqlite");
            System.out.println("Connect Berhasil");
                     
            return con;
           }catch (Exception e){
            System.out.println("Connect Gagal"+e);
            return null;
           }
    }
}
    
    public void tambahKontak(String nama, String telepon, String kategori) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "INSERT INTO kontak (nama, telepon, kategori) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nama);
        pstmt.setString(2, telepon);
        pstmt.setString(3, kategori);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    public List<String[]> getKontakList() {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM kontak";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            String[] kontak = new String[4];
            kontak[0] = String.valueOf(rs.getInt("id"));
            kontak[1] = rs.getString("nama");
            kontak[2] = rs.getString("telepon");
            kontak[3] = rs.getString("kategori");
            kontakList.add(kontak);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return kontakList;
}
    
    public void updateKontak(int id, String nama, String telepon, String kategori) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "UPDATE kontak SET nama = ?, telepon = ?, kategori = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nama);
        pstmt.setString(2, telepon);
        pstmt.setString(3, kategori);
        pstmt.setInt(4, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
     public void hapusKontak(int id) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    try (Connection conn = dbHelper.connect()) {
        String sql = "DELETE FROM kontak WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
     
     public void tampilkanKontak(DefaultTableModel model) {
    model.setRowCount(0); // Menghapus baris lama
    List<String[]> kontakList = getKontakList(); // Mendapatkan daftar kontak dari database

    for (String[] kontak : kontakList) {
        model.addRow(kontak); // Tambahkan kontak ke dalam JTable
    }
}
     
     public List<String[]> cariKontak(String keyword) {
    List<String[]> kontakList = new ArrayList<>();
    DatabaseHelper dbHelper = new DatabaseHelper();
    
    try (Connection conn = dbHelper.connect()) {
        String sql = "SELECT * FROM kontak WHERE nama LIKE ? OR telepon LIKE ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + keyword + "%");
        pstmt.setString(2, "%" + keyword + "%");
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String[] kontak = new String[4];
            kontak[0] = String.valueOf(rs.getInt("id"));
            kontak[1] = rs.getString("nama");
            kontak[2] = rs.getString("telepon");
            kontak[3] = rs.getString("kategori");
            kontakList.add(kontak);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return kontakList;
}

     
    public void tampilkanKontakBerdasarkanKategori(String kategori) {
    tableModel.setRowCount(0); // Menghapus semua baris lama di tabel
    if (kategori == null || tableModel == null) {
        System.out.println("Kategori atau tableModel tidak boleh null");
        return;
    }

    List<String[]> kontakList = getKontakList(); // Dapatkan daftar semua kontak

    // Filter kontak berdasarkan kategori
    for (String[] kontak : kontakList) {
        if (kontak[3].equals(kategori)) { // Asumsi kontak[3] adalah kolom kategori
            tableModel.addRow(kontak); // Tambahkan kontak yang sesuai ke tabel
        }
    }
}

    public boolean validasiNomorTelepon(String telepon) {
    // Cek apakah panjang nomor telepon sesuai
    if (telepon.length() < 10 || telepon.length() > 13) {
        return false;
    }
    // Cek apakah hanya berisi angka
    for (char c : telepon.toCharArray()) {
        if (!Character.isDigit(c)) {
            return false;
        }
    }
    return true;
}
    
    public void eksporKeCSV(String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
        // Tulis header kolom
        writer.write("ID,Nama,Telepon,Kategori\n");
        
        // Iterasi melalui data di tableModel
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String id = tableModel.getValueAt(i, 0).toString();
            String nama = tableModel.getValueAt(i, 1).toString();
            String telepon = tableModel.getValueAt(i, 2).toString();
            String kategori = tableModel.getValueAt(i, 3).toString();
            
            writer.write(id + "," + nama + "," + telepon + "," + kategori + "\n");
        }
        
        writer.flush();
        JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke " + filePath, "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengekspor data", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    public void imporDariCSV(String filePath) {
    DatabaseHelper dbHelper = new DatabaseHelper();
    
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
         Connection conn = dbHelper.connect()) {
         
        String line;
        
        // Lewati header
        reader.readLine();
        
        // Iterasi setiap baris
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            
            if (data.length == 4) { // Pastikan ada 4 kolom data
                String id = data[0];
                String nama = data[1];
                String telepon = data[2];
                String kategori = data[3];
                
                // Tambahkan data ke database
                String sql = "INSERT INTO kontak (id, nama, telepon, kategori) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.setString(2, nama);
                pstmt.setString(3, telepon);
                pstmt.setString(4, kategori);
                pstmt.executeUpdate();
            }
        }
        
        JOptionPane.showMessageDialog(this, "Data berhasil diimpor dari " + filePath, "Impor Berhasil", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException | SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengimpor data", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        contactTable = new javax.swing.JTable();
        exportBtn = new javax.swing.JButton();
        importBtn = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(240, 231, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aplikasi Pengelolaan Kontak", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 24), new java.awt.Color(0, 189, 212))); // NOI18N

        jLabel1.setText("Nama");

        jLabel2.setText("Nomor telepon ");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih kategori", "Kuliah", "Keluarga", "Sahabat", " " }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Change");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Delete");

        jButton4.setText("Search");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        contactTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(contactTable);

        exportBtn.setText("Export");
        exportBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportBtnActionPerformed(evt);
            }
        });

        importBtn.setText("Import");
        importBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importBtnActionPerformed(evt);
            }
        });

        jButton7.setText("Exit");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel1))
                                .addGap(72, 72, 72)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox1, 0, 203, Short.MAX_VALUE)
                                    .addComponent(jTextField1)
                                    .addComponent(jTextField2))
                                .addGap(18, 18, 18)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(114, 114, 114)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(36, 36, 36))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(123, 123, 123)
                .addComponent(exportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(importBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(122, 122, 122))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(236, 236, 236))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(16, 16, 16)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportBtn)
                    .addComponent(importBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addGap(41, 41, 41))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 5, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    jButton1.addActionListener(e -> {
    String nama = jTextField1.getText();
    String telepon = jTextField2.getText();
    String kategori = (String) jComboBox1.getSelectedItem();
    
    // Validasi nomor telepon
    if (!validasiNomorTelepon(telepon)) {
        JOptionPane.showMessageDialog(this, "Nomor telepon harus berisi angka dan memiliki panjang antara 10-13 digit.", "Error", JOptionPane.ERROR_MESSAGE);
        return; // Batalkan proses jika validasi gagal
    }

    // Jika validasi berhasil, lanjutkan dengan menambah kontak
    tambahKontak(nama, telepon, kategori);
    tampilkanKontak(tableModel); // Refresh JTable setelah tambah data
});
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    jButton2.addActionListener(e -> {
    int selectedRow = contactTable.getSelectedRow();
    if (selectedRow != -1) { // Pastikan ada baris yang dipilih
    int id = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
    String nama = jTextField1.getText();
    String telepon = jTextField2.getText();
    String kategori = (String) jComboBox1.getSelectedItem();
    updateKontak(id, nama, telepon, kategori);
    tampilkanKontak(tableModel); // Refresh JTable setelah update data
    }
});
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    jButton4.addActionListener(e -> {
    String keyword = jTextField1.getText();
    List<String[]> hasilCari = cariKontak(keyword); // cariKontak adalah metode pencarian
    tableModel.setRowCount(0); // Bersihkan tabel sebelum menampilkan hasil
    for (String[] kontak : hasilCari) {
        tableModel.addRow(kontak);
    }
});
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        jComboBox1.addItemListener(e -> {
    if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        String kategoriTerpilih = (String) e.getItem();
        // Panggil metode untuk menampilkan kontak berdasarkan kategori
        tampilkanKontakBerdasarkanKategori(kategoriTerpilih);
    }
});
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void exportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportBtnActionPerformed
        exportBtn.addActionListener((ActionEvent e) -> {
    String filePath = "contact.csv"; // Tentukan path atau gunakan dialog untuk memilih path
    eksporKeCSV(filePath);
});
    }//GEN-LAST:event_exportBtnActionPerformed

    private void importBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importBtnActionPerformed
        importBtn.addActionListener(e -> {
    String filePath = "contact.csv"; // Tentukan path atau gunakan dialog untuk memilih path
    imporDariCSV(filePath);
    tampilkanKontak(tableModel); // Refresh JTable setelah impor data
});
    }//GEN-LAST:event_importBtnActionPerformed

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiPengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AplikasiPengelolaanKontakFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable contactTable;
    private javax.swing.JButton exportBtn;
    private javax.swing.JButton importBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
