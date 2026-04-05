/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main;

import App.DAO.TaiKhoanDAO;
import App.Entity.TaiKhoan;
import App.Impl.TaiKhoanDAOImpl;
import java.awt.Image;
import java.io.File;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
/**
 *
 * @author PHONG
 */
public class ThongTinNguoiDungJDialog extends javax.swing.JDialog implements ThongTinNguoiDungController{

    // ==== DAO & STATE ====
    private final TaiKhoanDAO dao = new TaiKhoanDAOImpl();
    private TaiKhoan current;
    private final String loggedUsername; // tên tài khoản đang đăng nhập

    // không truyền username
    public ThongTinNguoiDungJDialog(java.awt.Frame parent, boolean modal) {
        this(parent, modal, null);
    }

    // chính truyền username để tự nạp form
    public ThongTinNguoiDungJDialog(java.awt.Frame parent, boolean modal, String username) {
        super(parent, modal);
        setUndecorated(true); 
        this.loggedUsername = username;
        initComponents(); 
        afterInit();
        setLocationRelativeTo(null);
    }

    // Sau khi build UI xong
    private void afterInit() {
        buttonGroup1.add(rdnam);
        buttonGroup1.add(rdnu);
        txtMaNguoiDung.setEditable(false);
        txttentaikhoan.setEditable(false);

        lblanh.setToolTipText("Nhấn để chọn ảnh");
        lblanh.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { chooseImage(); }
        });

        // dữ liệu nếu có username
        if (loggedUsername != null && !loggedUsername.isBlank()) {
            loadByUsername(loggedUsername);
        }
    }

    @Override
    public void loadByUsername(String username) {
        try {
            current = dao.findByTenTaiKhoan(username);
            if (current == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng: " + username);
                clearForm();
            } else {
                setForm(current);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    @Override
    public void setForm(TaiKhoan tk) {
        if (tk == null) { clearForm(); return; }
        current = tk;

        txtMaNguoiDung.setText(tk.getMaNguoiDung() == null ? "" : String.valueOf(tk.getMaNguoiDung()));
        txttentaikhoan.setText(nvl(tk.getTenTaiKhoan()));
        txtMatKhau.setText(nvl(tk.getMatKhau()));
        txtHoTen.setText(nvl(tk.getHoTen()));
        txtSoDienThoai.setText(nvl(tk.getDienThoai()));
        txtEmail.setText(nvl(tk.getEmail()));
        txtDiaChi.setText(nvl(tk.getDiaChi()));
        txtcccd.setText(nvl(tk.getCccd()));
        dcsngaysinh.setDate(tk.getNgaySinh());
        if ("Nữ".equalsIgnoreCase(nvl(tk.getGioiTinh()))) rdnu.setSelected(true); else rdnam.setSelected(true);
        setImageToLabel(tk.getHinhAnh());
    }

    @Override
    public TaiKhoan getForm() {
        if (txtHoTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được trống");
            return null;
        }
        String phone = txtSoDienThoai.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{9,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ");
            return null;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() &&
            !Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$").matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ");
            return null;
        }

        TaiKhoan tk = (current == null) ? new TaiKhoan() : current;

        // id (nếu có)
        try {
            String idStr = txtMaNguoiDung.getText().trim();
            if (!idStr.isEmpty()) tk.setMaNguoiDung(Integer.valueOf(idStr));
        } catch (Exception ignore) {}

        // Gán dữ liệu
        tk.setTenTaiKhoan(txttentaikhoan.getText().trim());
        tk.setMatKhau(new String(txtMatKhau.getPassword()));
        tk.setHoTen(txtHoTen.getText().trim());
        tk.setDienThoai(phone);
        tk.setEmail(email);
        tk.setDiaChi(txtDiaChi.getText().trim());
        tk.setCccd(txtcccd.getText().trim());
        tk.setNgaySinh(dcsngaysinh.getDate());
        tk.setGioiTinh(rdnu.isSelected() ? "Nữ" : "Nam");
        tk.setHinhAnh((String) lblanh.getClientProperty("path"));
        return tk;
    }

    @Override
    public void update() {
        TaiKhoan tk = getForm();
        if (tk == null) return;
        try {
            dao.update(tk);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công");
            setForm(dao.findByTenTaiKhoan(tk.getTenTaiKhoan())); // reload
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtMaNguoiDung.setText("");
        txttentaikhoan.setText("");
        txtMatKhau.setText("");
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        txtcccd.setText("");
        dcsngaysinh.setDate(null);
        rdnam.setSelected(true);
        setImageToLabel(null);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            setImageToLabel(f.getAbsolutePath());
        }
    }

    private void setImageToLabel(String path) {
        if (path == null || path.isBlank()) {
            lblanh.setIcon(null);
            lblanh.putClientProperty("path", null);
            return;
        }
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(lblanh.getWidth(), lblanh.getHeight(), Image.SCALE_SMOOTH);
        lblanh.setIcon(new ImageIcon(img));
        lblanh.putClientProperty("path", path);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel103 = new javax.swing.JPanel();
        jLabel112 = new javax.swing.JLabel();
        btndong102 = new javax.swing.JButton();
        jLabel113 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtMaNguoiDung = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtHoTen = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtSoDienThoai = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtDiaChi = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtMatKhau = new javax.swing.JPasswordField();
        jLabel9 = new javax.swing.JLabel();
        txttentaikhoan = new javax.swing.JTextField();
        txtcccd = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblanh = new javax.swing.JLabel();
        rdnu = new javax.swing.JRadioButton();
        rdnam = new javax.swing.JRadioButton();
        dcsngaysinh = new com.toedter.calendar.JDateChooser();
        btnCapNhat = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(40, 46, 62));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 205, 31), 2));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel103.setBackground(new java.awt.Color(102, 204, 255));
        jPanel103.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel112.setFont(new java.awt.Font("Serif", 1, 48)); // NOI18N
        jLabel112.setForeground(new java.awt.Color(255, 255, 255));
        jLabel112.setText("NHÀ TRỌ FPOLY");
        jPanel103.add(jLabel112, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, -1, -1));

        btndong102.setBackground(new java.awt.Color(0, 0, 255));
        btndong102.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        btndong102.setForeground(new java.awt.Color(255, 255, 255));
        btndong102.setText("X");
        btndong102.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndong102ActionPerformed(evt);
            }
        });
        jPanel103.add(btndong102, new org.netbeans.lib.awtextra.AbsoluteConstraints(755, 9, 60, 50));

        jLabel113.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/logo1.png"))); // NOI18N
        jPanel103.add(jLabel113, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 3, -1, 62));

        jPanel1.add(jPanel103, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 830, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setText("THÔNG TIN CÁ NHÂN");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 70, 260, -1));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 205, 31), 2));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Mã người dùng:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 13, 110, -1));

        txtMaNguoiDung.setEditable(false);
        txtMaNguoiDung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMaNguoiDung.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtMaNguoiDung, new org.netbeans.lib.awtextra.AbsoluteConstraints(447, 10, 160, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Họ tên:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 13, 106, -1));

        txtHoTen.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtHoTen.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtHoTen, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 10, 160, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Số điện thoại:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 45, 106, -1));

        txtSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSoDienThoai.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtSoDienThoai, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 42, 160, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Email:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 77, 106, -1));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 74, 160, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Địa chỉ:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 109, 106, -1));

        txtDiaChi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDiaChi.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtDiaChi, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 106, 160, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Mật khẩu:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(323, 77, 100, -1));

        txtMatKhau.setEditable(false);
        txtMatKhau.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMatKhau.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtMatKhau, new org.netbeans.lib.awtextra.AbsoluteConstraints(447, 74, 160, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Tên tài khoản:");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 45, 100, -1));

        txttentaikhoan.setEditable(false);
        txttentaikhoan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txttentaikhoan.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txttentaikhoan, new org.netbeans.lib.awtextra.AbsoluteConstraints(447, 42, 160, -1));

        txtcccd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtcccd.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtcccd, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 138, 160, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Cccd:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 141, 106, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Ngày sinh:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 170, 106, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Giới tính:");
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 214, 106, -1));

        lblanh.setBackground(new java.awt.Color(0, 0, 0));
        lblanh.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblanh.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        lblanh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblanhMouseClicked(evt);
            }
        });
        jPanel2.add(lblanh, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 110, 160, 150));

        buttonGroup1.add(rdnu);
        rdnu.setText("Nữ");
        jPanel2.add(rdnu, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 213, 98, -1));

        buttonGroup1.add(rdnam);
        rdnam.setText("Nam");
        jPanel2.add(rdnam, new org.netbeans.lib.awtextra.AbsoluteConstraints(149, 213, 98, -1));
        jPanel2.add(dcsngaysinh, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 170, 160, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 622, 268));

        btnCapNhat.setBackground(new java.awt.Color(0, 0, 255));
        btnCapNhat.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnCapNhat.setForeground(new java.awt.Color(255, 255, 255));
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCapNhatActionPerformed(evt);
            }
        });
        jPanel1.add(btnCapNhat, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 390, 180, 50));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen02.jpg"))); // NOI18N
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 830, 530));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 831, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCapNhatActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnCapNhatActionPerformed

    private void lblanhMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblanhMouseClicked
        // TODO add your handling code here:
        chooseImage();
    }//GEN-LAST:event_lblanhMouseClicked

    private void btndong102ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndong102ActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn muốn đóng cửa sổ này?",
            "Đóng",
            JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose(); // chỉ đóng JDialog hiện tại
        }
    }//GEN-LAST:event_btndong102ActionPerformed

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
            java.util.logging.Logger.getLogger(ThongTinNguoiDungJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ThongTinNguoiDungJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ThongTinNguoiDungJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ThongTinNguoiDungJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ThongTinNguoiDungJDialog dialog = new ThongTinNguoiDungJDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCapNhat;
    private javax.swing.JButton btndong102;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.toedter.calendar.JDateChooser dcsngaysinh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel103;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblanh;
    private javax.swing.JRadioButton rdnam;
    private javax.swing.JRadioButton rdnu;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JTextField txtMaNguoiDung;
    private javax.swing.JPasswordField txtMatKhau;
    private javax.swing.JTextField txtSoDienThoai;
    private javax.swing.JTextField txtcccd;
    private javax.swing.JTextField txttentaikhoan;
    // End of variables declaration//GEN-END:variables
}
