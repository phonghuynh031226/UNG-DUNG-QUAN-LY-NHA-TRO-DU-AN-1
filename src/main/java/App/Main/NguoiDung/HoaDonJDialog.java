/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main.NguoiDung;

import App.Utils.XAuth;
import App.Utils.XJdbc;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
//hoadon
/**
 *
 * @author PHONG
 */
public class HoaDonJDialog extends javax.swing.JDialog {

    /**
     * Creates new form HoaDonJDialog
     */
//    public HoaDonJDialog(java.awt.Frame parent, boolean modal) {
//        super(parent, modal);
//        initComponents();
//    }

    
    // ====== table model & format ======
    private DefaultTableModel model;
    private final NumberFormat moneyFmt;

    public HoaDonJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        moneyFmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        moneyFmt.setMaximumFractionDigits(0);
        afterInit();
                setLocationRelativeTo(null);
    }

    /* ================= lifecycle ================= */

    private void afterInit() {
        // header bảng
        model = (DefaultTableModel) tblHoaDon.getModel();
        model.setColumnIdentifiers(new Object[]{
                "Mã HĐ", "Mã HĐồng", "Ngày tạo", "Tổng cộng", "Tình trạng"
        });

        // click để xem chi tiết
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onRowClick(); }
        });

        // load dữ liệu theo user đang đăng nhập
        loadHoaDonCuaToi();
        // clear chi tiết
        clearDetail();
    }

    /* ================ data ================== */

    private void loadHoaDonCuaToi() {
        model.setRowCount(0);

        if (XAuth.user == null || XAuth.user.getMaNguoiDung() == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được tài khoản đang đăng nhập.");
            return;
        }

        final Integer userId = XAuth.user.getMaNguoiDung();

        // Tính tổng ở SQL cho gọn (coalesce để tránh null)
        final String sql = """
            SELECT h.maHoaDon,
                   h.maHopDong,
                   h.ngayTaoHoaDon,
                   h.ngayThanhToan,
                   h.trangThai,
                   COALESCE(h.tienPhong,0) + COALESCE(h.tienDien,0) + COALESCE(h.tienNuoc,0) AS tongCong
            FROM HoaDon h
            JOIN HopDong d ON d.maHopDong = h.maHopDong
            WHERE d.maNguoiDung = ?
            ORDER BY h.ngayTaoHoaDon DESC
        """;

        try (ResultSet rs = XJdbc.executeQuery(sql, userId)) {
            while (rs.next()) {
                String maHD   = rs.getString("maHoaDon");
                String maHDg  = rs.getString("maHopDong");
                Date ngayTao  = rs.getDate("ngayTaoHoaDon");
                String trang  = rs.getString("trangThai");
                BigDecimal tong = (BigDecimal) rs.getObject("tongCong");

                model.addRow(new Object[]{
                        maHD,
                        maHDg,
                        ngayTao,                       // để Date, JTable sẽ hiển thị theo toString
                        moneyFmt.format(tong),        // tổng cộng định dạng tiền
                        (trang == null ? "Chưa thanh toán" : trang)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải hóa đơn: " + ex.getMessage());
        }
    }

    private void onRowClick() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0) return;

        String maHD = String.valueOf(model.getValueAt(row, 0));

        // Lấy đầy đủ chi tiết của HĐ được chọn
        final String sql = """
            SELECT h.maHoaDon, h.maHopDong, h.ngayTaoHoaDon, h.ngayThanhToan, h.trangThai,
                   h.tienPhong, h.tienDien, h.tienNuoc
            FROM HoaDon h
            JOIN HopDong d ON d.maHopDong = h.maHopDong
            WHERE d.maNguoiDung = ? AND h.maHoaDon = ?
        """;

        try (ResultSet rs = XJdbc.executeQuery(sql, XAuth.user.getMaNguoiDung(), maHD)) {
            if (rs.next()) {
                // header
                lblIdHoaDon.setText(nvl(rs.getString("maHoaDon")));
                lblIdHopDong.setText(nvl(rs.getString("maHopDong")));
                jdcNgayTao.setDate(rs.getDate("ngayTaoHoaDon"));
                jdcNgaytt.setDate(rs.getDate("ngayThanhToan"));
                lblNguoiTao.setText(nvl(XAuth.user.getHoTen()));
                lblStatus.setText(nvl(rs.getString("trangThai")));

                // tiền & số đo
                BigDecimal tienPhong = (BigDecimal) rs.getObject("tienPhong");
                BigDecimal tienDien  = (BigDecimal) rs.getObject("tienDien");
                BigDecimal tienNuoc  = (BigDecimal) rs.getObject("tienNuoc");

                txtTienPhong.setText(fmtMoney(tienPhong));
                txtTienDien.setText(fmtMoney(tienDien));
                txtTienNuoc.setText(fmtMoney(tienNuoc));


                BigDecimal tong = nvlBD(tienPhong).add(nvlBD(tienDien)).add(nvlBD(tienNuoc));
                lblTongCong.setText(fmtMoney(tong));
            } else {
                clearDetail();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi load chi tiết HĐ: " + ex.getMessage());
        }
    }

    private void clearDetail() {
        jdcNgayTao.setDate(null);
        jdcNgaytt.setDate(null);
        lblNguoiTao.setText(nvl(XAuth.user == null ? "" : XAuth.user.getHoTen()));
        lblIdHoaDon.setText("ID hóa đơn");
        lblIdHopDong.setText("ID hợp đồng");
        lblStatus.setText("Chưa/Đã thanh toán");



        txtTienDien.setText("");
        txtTienNuoc.setText("");
        txtTienPhong.setText("");
        lblTongCong.setText("x xxx xxx");
    }

    /* =============== helpers =============== */

    private String fmtMoney(BigDecimal v) {
        if (v == null) return "0";
        return moneyFmt.format(v);
    }

    private BigDecimal nvlBD(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String nvl(String s) { return (s == null ? "" : s); }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jdcNgaytt = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        jdcNgayTao = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        lblNguoiTao = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lblIdHoaDon = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblIdHopDong = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        txtTienDien = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtTienNuoc = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtTienPhong = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel24 = new javax.swing.JLabel();
        lblTongCong = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHoaDon = new javax.swing.JTable();
        btnThanhToan = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(46, 56, 86));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("PHIẾU HÓA ĐƠN");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 10, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Price list.png"))); // NOI18N
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Ngày thanh toán:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 60, -1, -1));

        jdcNgaytt.setBackground(new java.awt.Color(255, 255, 255));
        jdcNgaytt.setForeground(new java.awt.Color(40, 46, 62));
        jdcNgaytt.setDateFormatString("dd-MM-yyyy");
        jdcNgaytt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel2.add(jdcNgaytt, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 60, 115, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Ngày tạo:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        jdcNgayTao.setBackground(new java.awt.Color(255, 255, 255));
        jdcNgayTao.setForeground(new java.awt.Color(40, 46, 62));
        jdcNgayTao.setDateFormatString("dd-MM-yyyy");
        jdcNgayTao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel2.add(jdcNgayTao, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 115, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Người tạo:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 100, -1, -1));

        lblNguoiTao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNguoiTao.setForeground(new java.awt.Color(255, 205, 31));
        lblNguoiTao.setText("Họ tên");
        jPanel2.add(lblNguoiTao, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 100, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Mã hóa đơn:");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 140, -1, -1));

        lblIdHoaDon.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblIdHoaDon.setForeground(new java.awt.Color(255, 205, 31));
        lblIdHoaDon.setText("ID hóa đơn");
        jPanel2.add(lblIdHoaDon, new org.netbeans.lib.awtextra.AbsoluteConstraints(114, 140, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Mã hợp đồng:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 140, -1, -1));

        lblIdHopDong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblIdHopDong.setForeground(new java.awt.Color(255, 205, 31));
        lblIdHopDong.setText("ID hợp đồng");
        jPanel2.add(lblIdHopDong, new org.netbeans.lib.awtextra.AbsoluteConstraints(359, 140, -1, -1));

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 198, 520, 10));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Tiền điện:");
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(228, 217, -1, -1));

        txtTienDien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTienDien.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtTienDien, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 210, 100, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Tiền nước:");
        jPanel2.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 217, -1, -1));

        txtTienNuoc.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTienNuoc.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtTienNuoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(97, 214, 100, -1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel21.setText("Tiền phòng:");
        jPanel2.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 249, -1, -1));

        txtTienPhong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTienPhong.setForeground(new java.awt.Color(40, 46, 62));
        jPanel2.add(txtTienPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(97, 246, 100, -1));

        jSeparator3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 286, 520, 10));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setText("Tổng cộng:");
        jPanel2.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 250, -1, -1));

        lblTongCong.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTongCong.setForeground(new java.awt.Color(255, 205, 31));
        lblTongCong.setText("x xxx xxx");
        jPanel2.add(lblTongCong, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 250, -1, -1));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setText("Tình trạng:");
        jPanel2.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(255, 205, 31));
        lblStatus.setText("Chưa/Đã thanh toán");
        jPanel2.add(lblStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, -1, -1));

        tblHoaDon.setBackground(new java.awt.Color(207, 243, 243));
        tblHoaDon.setForeground(new java.awt.Color(40, 46, 62));
        tblHoaDon.setModel(new javax.swing.table.DefaultTableModel(
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
        tblHoaDon.setGridColor(new java.awt.Color(255, 255, 255));
        tblHoaDon.setSelectionBackground(new java.awt.Color(40, 46, 62));
        tblHoaDon.setSelectionForeground(new java.awt.Color(255, 205, 31));
        tblHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHoaDonMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHoaDon);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 60, 560, 280));

        btnThanhToan.setBackground(new java.awt.Color(255, 205, 31));
        btnThanhToan.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnThanhToan.setForeground(new java.awt.Color(40, 46, 62));
        btnThanhToan.setText("Thanh toán");
        btnThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThanhToanActionPerformed(evt);
            }
        });
        jPanel2.add(btnThanhToan, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 300, -1, -1));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen02.jpg"))); // NOI18N
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 380));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThanhToanActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(null, "Khách thuê trọ thanh toán tiền vui lòng chuyển khoản qua số tài khoản sau: \n"
            + "Ngân hàng: Vietcombank\n Số tài khoản: 0123456789 \n Tên: CHU TRO\n"
            + "Nội dung: MaHoaDon_MaHopDong_TienNhaThangXX\n"
            + "Xin cám ơn!");
    }//GEN-LAST:event_btnThanhToanActionPerformed

    private void tblHoaDonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHoaDonMouseClicked
        // TODO add your handling code here:
        //        try {
            //            long selectedId = getSelectedID();
            //            if (selectedId < 0) {
                //                JOptionPane.showMessageDialog(null, "Chọn hóa đơn cần chọn!");
                //            }
            //            String query = "select hdon.Id, hdong.Id, hdon.NgayBatDau, hdon.NgayKetThuc, hdon.NgayTao, hdon.NguoiTao,\n"
            //                    + "hdon.SoDienMoi, hdon.SoDienCu, \n"
            //                    + "((hdon.SoDienMoi - hdon.SoDienCu)*hdong.GiaDien) as 'Tien dien', \n"
            //                    + "hdon.SoNuocMoi, hdon.SoNuocCu,\n"
            //                    + "((hdon.SoNuocMoi - hdon.SoNuocCu)*hdong.GiaNuoc) as 'Tien nuoc', \n"
            //                    + "(hdong.GiaInternet), (hdong.GiaRac), (hdong.GiaPhong), (hdon.KhauTru), (hdon.TienNo),\n"
            //                    + "( ((hdon.SoDienMoi - hdon.SoDienCu)*hdong.GiaDien) + ((hdon.SoNuocMoi - hdon.SoNuocCu)*hdong.GiaNuoc) +\n"
            //                    + "(hdong.GiaInternet) + (hdong.GiaRac) + (hdong.GiaPhong) + (hdon.TienNo) - (hdon.KhauTru)) as 'Tong cong', hdon.Status\n"
            //                    + "from HoaDon hdon join HopDong hdong on hdon.IdHopDong = hdong.Id join NguoiDung nd on hdong.IdNguoiDung = nd.Id\n"
            //                    + "where nd.Id =? and hdon.Id=?;";
            //            PreparedStatement ps = con.prepareStatement(query);
            //            ps.setLong(1, session.getId());
            //            ps.setLong(2, selectedId);
            //            ResultSet rs = ps.executeQuery();
            //            while (rs.next()) {
                //                jdcNgayBD.setDate(rs.getDate(3));
                //                jdcNgayKT.setDate(rs.getDate(4));
                //                jdcNgayTao.setDate(rs.getDate(5));
                //                lblNguoiTao.setText(rs.getString(6));
                //                lblIdHoaDon.setText(rs.getString(1));
                //                lblIdHopDong.setText(rs.getString(2));
                //                if(rs.getInt(19) == 1) {
                    //                    lblStatus.setText("Đã thanh toán");
                    //                } else {
                    //                    lblStatus.setText("Chưa thanh toán");
                    //                }
                //
                //                txtSoDiencu.setText(rs.getFloat(8)+"");
                //                txtSoDienMoi.setText(rs.getFloat(7)+"");
                //                txtTienDien.setText(rs.getFloat(9)+"");
                //                txtSoNuocCu.setText(rs.getFloat(11)+"");
                //                txtSoNuocMoi.setText(rs.getFloat(10)+"");
                //                txtTienNuoc.setText(rs.getFloat(12)+"");
                //                txtTienInternet.setText(rs.getFloat(13)+"");
                //                txtTienRac.setText(rs.getFloat(14)+"");
                //                txtTienPhong.setText(rs.getFloat(15)+"");
                //                txtKhauTru.setText(rs.getFloat(16)+"");
                //                txtNo.setText(rs.getFloat(17)+"");
                //                lblTongCong.setText(rs.getFloat(18)+"");
                //            }
            //        } catch (Exception e) {
            //            e.printStackTrace();
            //        }
    }//GEN-LAST:event_tblHoaDonMouseClicked

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
            java.util.logging.Logger.getLogger(HoaDonJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HoaDonJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HoaDonJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HoaDonJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                HoaDonJDialog dialog = new HoaDonJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnThanhToan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private com.toedter.calendar.JDateChooser jdcNgayTao;
    private com.toedter.calendar.JDateChooser jdcNgaytt;
    private javax.swing.JLabel lblIdHoaDon;
    private javax.swing.JLabel lblIdHopDong;
    private javax.swing.JLabel lblNguoiTao;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTongCong;
    private javax.swing.JTable tblHoaDon;
    private javax.swing.JTextField txtTienDien;
    private javax.swing.JTextField txtTienNuoc;
    private javax.swing.JTextField txtTienPhong;
    // End of variables declaration//GEN-END:variables
}
