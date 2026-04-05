/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main.NguoiDung;

import App.Utils.XAuth;
import App.Utils.XJdbc;
import App.Utils.XMail;
import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
/**
 *
 * @author PHONG
 */
public class DangKyPhongJDialog extends javax.swing.JDialog {

    // ====== DỮ LIỆU TRUYỀN VÀO ======
    private String maPhong;
    private BigDecimal giaPhong;
    private BigDecimal donGiaDien;
    private BigDecimal donGiaNuoc;
    private int userId;
    private String userName;
    // ====== KẾT QUẢ ======
    private boolean accepted = false;
    public boolean isAccepted() { return accepted; }
    
    public DangKyPhongJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setUndecorated(true);  // Ẩn viền và nút X
        initComponents();
        afterInit();          // Sau khi khởi tạo
        setLocationRelativeTo(null);                   
    }
    
    public DangKyPhongJDialog(java.awt.Frame parent, boolean modal,
                              String maPhong, BigDecimal giaPhong,
                              BigDecimal donGiaDien, BigDecimal donGiaNuoc,
                              int userId, String userName) {
        super(parent, modal);
        this.maPhong = maPhong;
        this.giaPhong = giaPhong;
        this.donGiaDien = donGiaDien;
        this.donGiaNuoc = donGiaNuoc;
        this.userId = userId;
        this.userName = userName;
        initComponents();
        afterInit();
        bindDataToUI();
    }

    // ====== FORMAT TIỀN VIỆT NAM ======
    private static String fmtMoney(BigDecimal v) {
        if (v == null) return "0";
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(v);
    }

    // ====== SAU KHI INIT ======
    private void afterInit() {
        setLocationRelativeTo(getOwner());
        btnDatPhong.setEnabled(false); // Tắt nút khi chưa chọn
        ckbDongY.addActionListener(e -> btnDatPhong.setEnabled(ckbDongY.isSelected()));
    }

    // ====== GÁN DỮ LIỆU VÀO LABEL ======
    private void bindDataToUI() {
        lblMaPhong.setText(maPhong != null ? maPhong : "X");
        lblGiaPhong.setText(giaPhong != null ? fmtMoney(giaPhong) + " VND" : "X, XXX, XXX VND");
        lblGiaDien.setText(donGiaDien != null ? fmtMoney(donGiaDien) + " /kWh" : "X, XXX, XXX VND");
        lblGiaNuoc.setText(donGiaNuoc != null ? fmtMoney(donGiaNuoc) + " /m³"  : "X, XXX, XXX VND");
    }

    // ====== LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI ======
    private int effectiveUserId() {
        return (userId > 0) ? userId : (XAuth.user != null ? XAuth.user.getMaNguoiDung() : 0);
    }

    private String effectiveUserName() {
        return (userName != null && !userName.isBlank())
                ? userName
                : (XAuth.user != null ? XAuth.user.getHoTen() : "");
    }
    
    // ====== HÀM XỬ LÝ ĐẶT PHÒNG ======
    private void xuLyDatPhong() {
        if (!ckbDongY.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tích vào 'Tôi đồng ý' trước khi đặt.");
            return;
        }

        int uid = effectiveUserId();
        String uname = effectiveUserName();

        if (uid <= 0) {
            JOptionPane.showMessageDialog(this, "Tài khoản không hợp lệ. Vui lòng đăng nhập lại.");
            return;
        }

        Connection con = null;
        try {
            // Kiểm tra tài khoản hợp lệ
            Integer okUser = XJdbc.getValue(
                "SELECT COUNT(*) FROM TaiKhoan WHERE MaNguoiDung = ? AND TrangThai = 1", uid);
            if (okUser == null || okUser == 0) {
                JOptionPane.showMessageDialog(this, "Tài khoản không hợp lệ.");
                return;
            }

            // Kiểm tra phòng còn trống
            Integer okRoom = XJdbc.getValue(
                "SELECT COUNT(*) FROM Phong WHERE maPhong = ? AND trangThai = N'Trống'", maPhong);
            if (okRoom == null || okRoom == 0) {
                JOptionPane.showMessageDialog(this, "Phòng không còn trống.");
                return;
            }

            // Bắt đầu giao dịch
            con = XJdbc.openConnection();
            con.setAutoCommit(false);

            String maHD = "HD" + java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusMonths(6);

            // Thêm hợp đồng
            XJdbc.executeUpdate("""
                INSERT INTO HopDong(maHopDong, maNguoiDung, maPhong, ngayBatDau, ngayKetThuc)
                VALUES (?, ?, ?, ?, ?)
            """, maHD, uid, maPhong, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end));

            // Cập nhật trạng thái phòng
            XJdbc.executeUpdate("UPDATE Phong SET trangThai = N'Đang thuê' WHERE maPhong = ?", maPhong);

            con.commit();

            accepted = true;
            JOptionPane.showMessageDialog(this, "Đặt phòng thành công!");

            // Gửi email cho admin
            try {
                String subject = "[NHÀ TRỌ] Đặt phòng mới: " + lblMaPhong.getText();
                String body = "Người dùng: " + uname + " (ID " + uid + ")\n"
                        + "Phòng: " + lblMaPhong.getText() + "\n"
                        + "Giá phòng: " + lblGiaPhong.getText() + "\n"
                        + "Giá điện: " + lblGiaDien.getText() + "\n"
                        + "Giá nước: " + lblGiaNuoc.getText() + "\n"
                        + "Thời hạn hợp đồng: " + start + " → " + end;

                XMail.sendMail("phonghuynh031226@gmail.com", subject, body);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            dispose();

        } catch (Exception ex) {
            try { if (con != null) con.rollback(); } catch (Exception ignore) {}
            JOptionPane.showMessageDialog(this, "Lỗi đặt phòng: " + ex.getMessage());
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception ignore) {}
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
        jLabel4 = new javax.swing.JLabel();
        lblGiaPhong = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblGiaDien = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblGiaNuoc = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        ckbDongY = new javax.swing.JCheckBox();
        btnDatPhong = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        lblMaPhong = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        btnThoat = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(40, 46, 62));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 205, 31), 2));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("ĐIỀU KHOẢN THUÊ NHÀ");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 70, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Giá phòng:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, -1, -1));

        lblGiaPhong.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblGiaPhong.setForeground(new java.awt.Color(255, 205, 31));
        lblGiaPhong.setText("X, XXX, XXX VND");
        jPanel1.add(lblGiaPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 130, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Giá điện:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 160, -1, -1));

        lblGiaDien.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblGiaDien.setForeground(new java.awt.Color(255, 205, 31));
        lblGiaDien.setText("X, XXX, XXX VND");
        jPanel1.add(lblGiaDien, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Giá nước:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, -1, -1));

        lblGiaNuoc.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblGiaNuoc.setForeground(new java.awt.Color(255, 205, 31));
        lblGiaNuoc.setText("X, XXX, XXX VND");
        jPanel1.add(lblGiaNuoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 190, -1, -1));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Quy định đóng tiền nhà: \n- Hóa đơn sẽ được thông báo qua ứng dụng và email. \n- Thời gian đóng từ ngày 05 đến ngày 10 hàng tháng.\n- Tiền nhà đóng bằng hình thức tiền mặt hoặc chuyển khoản.\n");
        jScrollPane1.setViewportView(jTextArea1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 220, 340, 100));

        ckbDongY.setBackground(new java.awt.Color(40, 46, 62));
        ckbDongY.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        ckbDongY.setForeground(new java.awt.Color(255, 255, 255));
        ckbDongY.setText("Tôi đồng ý");
        ckbDongY.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        ckbDongY.setOpaque(true);
        jPanel1.add(ckbDongY, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 330, 120, -1));

        btnDatPhong.setBackground(new java.awt.Color(255, 205, 31));
        btnDatPhong.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDatPhong.setForeground(new java.awt.Color(40, 46, 62));
        btnDatPhong.setText("Đặt phòng");
        btnDatPhong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatPhongActionPerformed(evt);
            }
        });
        jPanel1.add(btnDatPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 330, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Mã phòng:");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 110, -1, -1));

        lblMaPhong.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblMaPhong.setForeground(new java.awt.Color(255, 205, 31));
        lblMaPhong.setText("X");
        jPanel1.add(lblMaPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 104, -1, -1));

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 205, 31)));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/vit.gif"))); // NOI18N

        btnThoat.setBackground(new java.awt.Color(0, 0, 255));
        btnThoat.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnThoat.setForeground(new java.awt.Color(40, 46, 62));
        btnThoat.setText("X");
        btnThoat.setPreferredSize(new java.awt.Dimension(38, 39));
        btnThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThoatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 430, Short.MAX_VALUE)
                .addComponent(btnThoat, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnThoat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 2, 540, -1));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen03.jpg"))); // NOI18N
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 540, 400));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDatPhongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatPhongActionPerformed
        // TODO add your handling code here:
        xuLyDatPhong(); // Gọi hàm xử lý riêng
    }//GEN-LAST:event_btnDatPhongActionPerformed

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThoatActionPerformed
        // TODO add your handling code here:
        this.dispose(); 
    }//GEN-LAST:event_btnThoatActionPerformed

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
            java.util.logging.Logger.getLogger(DangKyPhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DangKyPhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DangKyPhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DangKyPhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DangKyPhongJDialog dialog = new DangKyPhongJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnDatPhong;
    private javax.swing.JButton btnThoat;
    private javax.swing.JCheckBox ckbDongY;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lblGiaDien;
    private javax.swing.JLabel lblGiaNuoc;
    private javax.swing.JLabel lblGiaPhong;
    private javax.swing.JLabel lblMaPhong;
    // End of variables declaration//GEN-END:variables
}
