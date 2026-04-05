/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package App.Main.NguoiDung;

import App.Main.ThongTinNguoiDungJDialog;
import App.Main.TrangChuChung;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author WINDOWS
 */
public class MainNguoiDung extends javax.swing.JFrame implements MainNguoiDungController{
    // mặt định điện nước
    private static final java.math.BigDecimal DON_GIA_DIEN = new java.math.BigDecimal("3500");  // giá 1 kWh
    private static final java.math.BigDecimal DON_GIA_NUOC = new java.math.BigDecimal("10000"); // giá 1 m³ nước

    // ===== THÔNG TIN USER =====
    private final int currentUserId;      // ID người dùng hiện tại
    private final String currentUserName; // Tên người dùng hiện tại

    // ===== MODEL CHO JTABLE =====
    private DefaultTableModel model;

    // ===== CONSTRUCTOR =====
    public MainNguoiDung() { 
        this(0, "Họ tên"); 
    }

    // Constructor có tham số (id + tên người dùng)
    public MainNguoiDung(int userId, String hoTen) {
        initComponents(); // Khởi tạo giao diện (NetBeans sinh)
        setLocationRelativeTo(null);
        this.currentUserId  = userId;
        this.currentUserName = (hoTen != null ? hoTen : "Họ tên");
        // ===== KHỞI TẠO BẢNG PHÒNG TRỐNG =====
        model = (DefaultTableModel) tblPhongTrong.getModel();
        model.setColumnIdentifiers(new Object[]{"ID Phòng","Tên phòng","Diện tích (m²)","Giá phòng","Địa chỉ","Mô tả"});

        // ===== GÁN SỰ KIỆN =====
        // Nút Đăng ký
        btnDangKy.addActionListener(e -> dangKyPhong());

        // Click chọn dòng trong bảng -> đổ ID phòng sang ô nhập
        tblPhongTrong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPhongTrong.getSelectedRow();
                if (row >= 0) {
                    Object v = model.getValueAt(row, 0);
                    txtIDPhong.setText(v == null ? "" : v.toString());
                }
            }
        });

        // ===== NẠP DỮ LIỆU BAN ĐẦU =====
        loadPhongTrong();
        showHeaderInfo();
        init();
    }
    @Override
    public void init() {
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.awt.Rectangle usableBounds = ge.getMaximumWindowBounds();
        setBounds(usableBounds);
    }
    // ========================================================
    // ===== TRIỂN KHAI CÁC HÀM TRONG CONTROLLER ==============
    // ========================================================

    // Nạp danh sách phòng trống từ DB vào bảng
    @Override
    public void loadPhongTrong() {
        model.setRowCount(0); // xóa dữ liệu cũ

        String sql = "SELECT maPhong, dienTich, giaTien, diaChi, moTa "
                   + "FROM Phong WHERE trangThai = N'Trống' ORDER BY giaTien ASC";
        try {
            ResultSet rs = App.Utils.XJdbc.executeQuery(sql);
            while (rs.next()) {
                String mp = rs.getString("maPhong");
                java.math.BigDecimal dt = rs.getBigDecimal("dienTich");
                java.math.BigDecimal gia = rs.getBigDecimal("giaTien");

                // Định dạng số tiền theo kiểu VN
                NumberFormat nfMoney = NumberFormat.getNumberInstance(new Locale("vi","VN"));
                nfMoney.setMaximumFractionDigits(0);

                String dienTich = (dt == null) ? "" : dt.toPlainString();
                String giaTien  = (gia == null) ? "0" : nfMoney.format(gia);

                // Thêm 1 dòng vào bảng
                model.addRow(new Object[]{
                    mp, "Phòng " + mp, dienTich, giaTien, rs.getString("diaChi"), rs.getString("moTa")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải phòng trống: " + ex.getMessage());
        }
    }

    // Hiển thị thông tin header: tên, ID phòng, tháng hiện tại
    @Override
    public void showHeaderInfo() {
        // Tên người thuê
        lblTenNguoiThue.setText(currentUserName);

        // Tìm phòng đang thuê
        try {
            String sqlHD = "SELECT TOP 1 maPhong FROM HopDong "
                         + "WHERE maNguoiDung=? AND CONVERT(date,GETDATE()) "
                         + "BETWEEN CONVERT(date,ngayBatDau) AND CONVERT(date,ngayKetThuc) "
                         + "ORDER BY ngayBatDau DESC";
            ResultSet rs = App.Utils.XJdbc.executeQuery(sqlHD, currentUserId);
            if (rs.next()) lblIDPhong.setText(rs.getString("maPhong"));
            else lblIDPhong.setText("XX"); // chưa thuê phòng nào
        } catch (Exception ex) {
            lblIDPhong.setText("XX");
        }

        // Hiển thị tháng/năm hiện tại
        java.time.LocalDate now = java.time.LocalDate.now();
        lblThang.setText(String.format("THÁNG %02d/%d", now.getMonthValue(), now.getYear()));
    }

    // Khi chọn 1 dòng trong bảng -> điền ID phòng vào ô nhập
    @Override
    public void tableRowClick(int row) {
        if (row >= 0) {
            Object v = model.getValueAt(row, 0);
            txtIDPhong.setText(v == null ? "" : v.toString());
        }
    }

    // Xử lý đăng ký phòng
    @Override
    public void dangKyPhong() {
        String maPhong = txtIDPhong.getText().trim();
        if (maPhong.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập/chọn ID phòng.");
            return;
        }

        java.math.BigDecimal giaPhong = null;
        String trangThai = null;

        // Kiểm tra thông tin phòng trong DB
        try {
            ResultSet rs = App.Utils.XJdbc.executeQuery(
                    "SELECT giaTien, trangThai FROM Phong WHERE maPhong=?", maPhong);
            if (rs.next()) {
                giaPhong = rs.getBigDecimal("giaTien");
                trangThai = rs.getString("trangThai");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lấy phòng: " + ex.getMessage());
            return;
        }

        // Nếu không tìm thấy
        if (giaPhong == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phòng " + maPhong);
            return;
        }

        // Nếu không còn trống
        if (trangThai == null || !"Trống".equalsIgnoreCase(trangThai)) {
            JOptionPane.showMessageDialog(this, "Phòng không còn trống.");
            return;
        }

        // Mở dialog đăng ký phòng
        App.Main.NguoiDung.DangKyPhongJDialog dlg =
            new App.Main.NguoiDung.DangKyPhongJDialog(
                this, true, maPhong, giaPhong, 
                DON_GIA_DIEN, DON_GIA_NUOC, currentUserId, currentUserName
            );
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        // Sau khi đăng ký thành công -> refresh giao diện
        if (dlg.isAccepted()) {
            resetMiniForm();
            loadPhongTrong();
            showHeaderInfo();
        }
    }

    // Reset form mini (xóa ô nhập, bỏ chọn bảng)
    @Override
    public void resetMiniForm() {
        txtIDPhong.setText("");
        tblPhongTrong.clearSelection();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpnView = new javax.swing.JPanel();
        lblNull = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPhongTrong = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        lblThang = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblTongTien = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtIDPhong = new javax.swing.JTextField();
        btnDangKy = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblIDPhong = new javax.swing.JLabel();
        lblTenNguoiThue = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuTrangChu = new javax.swing.JMenu();
        jMenuHopDong = new javax.swing.JMenu();
        jMenuHoaDon = new javax.swing.JMenu();
        jMenuAbout = new javax.swing.JMenu();
        jMenuNguoiDung = new javax.swing.JMenu();
        jMenuThongTin = new javax.swing.JMenu();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuLichSuHD = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        btnThoat = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        btnDangXuat = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jpnView.setBackground(new java.awt.Color(40, 46, 62));
        jpnView.setPreferredSize(new java.awt.Dimension(1100, 630));
        jpnView.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jpnView.add(lblNull, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, -1, -1));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tblPhongTrong.setBackground(new java.awt.Color(207, 243, 243));
        tblPhongTrong.setModel(new javax.swing.table.DefaultTableModel(
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
        tblPhongTrong.setGridColor(new java.awt.Color(255, 255, 255));
        tblPhongTrong.setSelectionBackground(new java.awt.Color(40, 46, 62));
        tblPhongTrong.setSelectionForeground(new java.awt.Color(255, 205, 31));
        tblPhongTrong.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPhongTrongMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPhongTrong);

        jpnView.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 220, 971, 400));

        jPanel1.setBackground(new java.awt.Color(204, 255, 255));

        lblThang.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblThang.setForeground(new java.awt.Color(255, 205, 31));
        lblThang.setText("MỚI NHẤT");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("HÓA ĐƠN");

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTongTien.setForeground(new java.awt.Color(255, 205, 31));
        lblTongTien.setText("XXXXXXX");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblThang)
                .addGap(64, 64, 64))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(lblTongTien)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblThang))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTongTien)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jpnView.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 20, 300, -1));

        jPanel2.setBackground(new java.awt.Color(204, 255, 255));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("ID Phòng:");

        txtIDPhong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtIDPhong.setForeground(new java.awt.Color(40, 46, 62));

        btnDangKy.setBackground(new java.awt.Color(0, 51, 255));
        btnDangKy.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDangKy.setForeground(new java.awt.Color(40, 46, 62));
        btnDangKy.setText("Đăng ký");
        btnDangKy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDangKyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(txtIDPhong, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(btnDangKy)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtIDPhong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDangKy))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jpnView.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 20, -1, -1));

        jLabel5.setBackground(new java.awt.Color(0, 0, 0));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("ID phòng:");
        jpnView.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 100, -1, -1));

        jLabel6.setBackground(new java.awt.Color(0, 0, 0));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Tên người thuê:");
        jpnView.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 120, -1, -1));

        lblIDPhong.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblIDPhong.setForeground(new java.awt.Color(255, 51, 0));
        lblIDPhong.setText("XX");
        jpnView.add(lblIDPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 100, -1, -1));

        lblTenNguoiThue.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTenNguoiThue.setForeground(new java.awt.Color(255, 51, 0));
        lblTenNguoiThue.setText("Họ tên");
        jpnView.add(lblTenNguoiThue, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 120, -1, -1));

        jLabel7.setBackground(new java.awt.Color(0, 0, 0));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("DANH SÁCH PHÒNG TRỐNG");
        jpnView.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 180, -1, -1));

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen02.jpg"))); // NOI18N
        jpnView.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1560, 760));

        jMenuBar1.setBackground(new java.awt.Color(46, 56, 86));
        jMenuBar1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 255, 255), 2));
        jMenuBar1.setForeground(new java.awt.Color(40, 46, 62));
        jMenuBar1.setPreferredSize(new java.awt.Dimension(615, 70));

        jMenuTrangChu.setBackground(new java.awt.Color(153, 153, 153));
        jMenuTrangChu.setForeground(new java.awt.Color(255, 255, 255));
        jMenuTrangChu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Computer.png"))); // NOI18N
        jMenuTrangChu.setText("Trang chủ");
        jMenuTrangChu.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jMenuTrangChu.setMinimumSize(new java.awt.Dimension(200, 38));
        jMenuTrangChu.setOpaque(true);
        jMenuBar1.add(jMenuTrangChu);

        jMenuHopDong.setBackground(new java.awt.Color(153, 153, 153));
        jMenuHopDong.setForeground(new java.awt.Color(255, 255, 255));
        jMenuHopDong.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Accept.png"))); // NOI18N
        jMenuHopDong.setText("Hợp đồng");
        jMenuHopDong.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jMenuHopDong.setMinimumSize(new java.awt.Dimension(200, 38));
        jMenuHopDong.setOpaque(true);
        jMenuHopDong.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuHopDongMouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenuHopDong);

        jMenuHoaDon.setBackground(new java.awt.Color(153, 153, 153));
        jMenuHoaDon.setForeground(new java.awt.Color(255, 255, 255));
        jMenuHoaDon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Bell.png"))); // NOI18N
        jMenuHoaDon.setText("Hóa đơn");
        jMenuHoaDon.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jMenuHoaDon.setMinimumSize(new java.awt.Dimension(200, 38));
        jMenuHoaDon.setOpaque(true);
        jMenuHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuHoaDonMouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenuHoaDon);

        jMenuAbout.setBackground(new java.awt.Color(153, 153, 153));
        jMenuAbout.setForeground(new java.awt.Color(255, 255, 255));
        jMenuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Bricks.png"))); // NOI18N
        jMenuAbout.setText("About");
        jMenuAbout.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jMenuAbout.setMinimumSize(new java.awt.Dimension(200, 38));
        jMenuAbout.setOpaque(true);

        jMenuNguoiDung.setForeground(new java.awt.Color(40, 46, 62));
        jMenuNguoiDung.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Comment.png"))); // NOI18N
        jMenuNguoiDung.setText("Người dùng");
        jMenuNguoiDung.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        jMenuThongTin.setForeground(new java.awt.Color(40, 46, 62));
        jMenuThongTin.setText("Thông tin cá nhân");
        jMenuThongTin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuThongTin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuThongTinMouseClicked(evt);
            }
        });
        jMenuNguoiDung.add(jMenuThongTin);
        jMenuNguoiDung.add(jSeparator5);

        jMenuLichSuHD.setForeground(new java.awt.Color(40, 46, 62));
        jMenuLichSuHD.setText("Lịch sử hoạt động");
        jMenuLichSuHD.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jMenuLichSuHD.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuLichSuHDMouseClicked(evt);
            }
        });
        jMenuNguoiDung.add(jMenuLichSuHD);

        jMenuAbout.add(jMenuNguoiDung);
        jMenuAbout.add(jSeparator4);

        btnThoat.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnThoat.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnThoat.setForeground(new java.awt.Color(40, 46, 62));
        btnThoat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Computer.png"))); // NOI18N
        btnThoat.setText("Thoát");
        btnThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThoatActionPerformed(evt);
            }
        });
        jMenuAbout.add(btnThoat);
        jMenuAbout.add(jSeparator6);

        btnDangXuat.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        btnDangXuat.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnDangXuat.setForeground(new java.awt.Color(40, 46, 62));
        btnDangXuat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/Company.png"))); // NOI18N
        btnDangXuat.setText("Đăng xuất");
        btnDangXuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDangXuatActionPerformed(evt);
            }
        });
        jMenuAbout.add(btnDangXuat);

        jMenuBar1.add(jMenuAbout);

        jMenu2.setBackground(new java.awt.Color(153, 153, 153));
        jMenu2.setOpaque(true);
        jMenu2.setPreferredSize(new java.awt.Dimension(1200, 6));
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpnView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpnView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThoatActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn muốn đóng ứng dụng?",
                "Thoát",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) System.exit(0);
    }//GEN-LAST:event_btnThoatActionPerformed

    private void btnDangXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDangXuatActionPerformed
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(this,
                "Bạn muốn đăng xuất?",
                "Đăng xuất",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
        // Đóng cửa sổ hiện tại
        this.dispose();
        // Mở lại màn hình TrangChuChung
        TrangChuChung home = new TrangChuChung();
        home.setLocationRelativeTo(null); // canh giữa màn hình
        home.setVisible(true);
        }
    }//GEN-LAST:event_btnDangXuatActionPerformed

    private void tblPhongTrongMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPhongTrongMouseClicked
        // TODO add your handling code here:
        int row = tblPhongTrong.getSelectedRow();
        if (row >= 0) tableRowClick(row);
    }//GEN-LAST:event_tblPhongTrongMouseClicked

    private void btnDangKyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDangKyActionPerformed
        // TODO add your handling code here:
        dangKyPhong();
    }//GEN-LAST:event_btnDangKyActionPerformed

    private void jMenuHopDongMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuHopDongMouseClicked
        // TODO add your handling code here:
        this.showHopDongJDialog(this); 
    }//GEN-LAST:event_jMenuHopDongMouseClicked

    private void jMenuHoaDonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuHoaDonMouseClicked
        // TODO add your handling code here:
        this.showHoaDonJDialog(this); 
    }//GEN-LAST:event_jMenuHoaDonMouseClicked

    private void jMenuThongTinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuThongTinMouseClicked
        // TODO add your handling code here:
    // Lấy tên tài khoản nếu có đăng nhập
    String username = null;
    if (App.Utils.XAuth.user != null) {
        username = App.Utils.XAuth.user.getTenTaiKhoan();
    }

    // Mở dialog thông tin người dùng
    ThongTinNguoiDungJDialog dlg = new ThongTinNguoiDungJDialog(this, true, username);
    dlg.setLocationRelativeTo(this); // hiển thị giữa cửa sổ cha
    dlg.setVisible(true);

    }//GEN-LAST:event_jMenuThongTinMouseClicked

    private void jMenuLichSuHDMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuLichSuHDMouseClicked
        // TODO add your handling code here:
        this.showLichSuNguoiDungJDialog(this); 
    }//GEN-LAST:event_jMenuLichSuHDMouseClicked

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
                // có màu FlatLaf Light - ko màu Nimbus
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainNguoiDung.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainNguoiDung.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainNguoiDung.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainNguoiDung.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainNguoiDung().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDangKy;
    private javax.swing.JMenuItem btnDangXuat;
    private javax.swing.JMenuItem btnThoat;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenuAbout;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuHoaDon;
    private javax.swing.JMenu jMenuHopDong;
    private javax.swing.JMenu jMenuLichSuHD;
    private javax.swing.JMenu jMenuNguoiDung;
    private javax.swing.JMenu jMenuThongTin;
    private javax.swing.JMenu jMenuTrangChu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPanel jpnView;
    private javax.swing.JLabel lblIDPhong;
    private javax.swing.JLabel lblNull;
    private javax.swing.JLabel lblTenNguoiThue;
    private javax.swing.JLabel lblThang;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JTable tblPhongTrong;
    private javax.swing.JTextField txtIDPhong;
    // End of variables declaration//GEN-END:variables
}
