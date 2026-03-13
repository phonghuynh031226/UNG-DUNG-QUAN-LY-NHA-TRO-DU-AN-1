/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main.ChuTro;


import App.DAO.HopDongDAO;
import App.DAO.PhongDAO;
import App.Entity.HopDong;
import App.Entity.Phong;
import App.Impl.HopDongDAOImpl;
import App.Impl.PhongDAOImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;



/**
 *
 * @author PHONG
 */
public class HopDongJDialog extends javax.swing.JDialog implements HopDongController{

    /**
     * Creates new form HopDongJDialog
     */
    public HopDongJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
                afterInit();
    }


    // ==== DAO & model ====
    private final HopDongDAO hopDongDAO = new HopDongDAOImpl();
    private final PhongDAO phongDAO = new PhongDAOImpl();
    private DefaultTableModel model;

    // ==== Sau khi NetBeans build UI ====
    private void afterInit() {
        model = (DefaultTableModel) tblHopDong.getModel();
        model.setColumnIdentifiers(new Object[]{
            "Mã HĐ", "Mã phòng", "Mã người dùng", "Ngày BĐ", "Ngày KT", "Giá phòng"
        });

        // ô giá phòng chỉ hiển thị, không nhập
        txtGiaPhong.setEditable(false);

        txtTimKiem.setText("Nhập mã hợp đồng để tìm…");
        loadTable();

        // auto fill giá phòng khi nhập mã phòng
        txtmaphong.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { autoFillGiaPhong(); }
        });
        txtmaphong.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { autoFillGiaPhong(); }
        });
    }

    private void autoFillGiaPhong() {
        String ma = txtmaphong.getText().trim();
        if (ma.isEmpty()) { txtGiaPhong.setText(""); return; }
        Phong p = phongDAO.findById(ma);
        txtGiaPhong.setText(p == null || p.getGiaTien() == null ? "" : String.valueOf(p.getGiaTien()));
    }

    /* ================= Controller ================= */
    @Override
    public void loadTable() {
        model.setRowCount(0);
        List<HopDong> list = hopDongDAO.findAll();
        for (HopDong h : list) {
            // lấy giá phòng để hiển thị (không lưu trong bảng HopDong)
            Object gia = null;
            Phong p = phongDAO.findById(h.getMaPhong());
            if (p != null) gia = p.getGiaTien();

            model.addRow(new Object[]{
                h.getMaHopDong(), h.getMaPhong(), h.getMaNguoiDung(),
                h.getNgayBatDau(), h.getNgayKetThuc(), gia
            });
        }
    }

    @Override
    public void search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty() || "Nhập mã hợp đồng để tìm…".equals(kw)) { loadTable(); return; }
        model.setRowCount(0);
        HopDong h = hopDongDAO.findById(kw);
        if (h != null) {
            Object gia = null;
            Phong p = phongDAO.findById(h.getMaPhong());
            if (p != null) gia = p.getGiaTien();

            model.addRow(new Object[]{
                h.getMaHopDong(), h.getMaPhong(), h.getMaNguoiDung(),
                h.getNgayBatDau(), h.getNgayKetThuc(), gia
            });
        }
    }

    @Override
    public void clearForm() {
        lblmahopdong.setText("xx");
        txtmaphong.setText("");
        txtMaNguoiDung.setText("");
        dcsngaybatdauthue.setDate(null);
        dcsngayketthucthue.setDate(null);
        txtGiaPhong.setText("");
        tblHopDong.clearSelection();
        txtmaphong.requestFocus();
    }

    @Override
    public void setForm(HopDong h) {
        lblmahopdong.setText(h.getMaHopDong());
        txtmaphong.setText(h.getMaPhong());
        txtMaNguoiDung.setText(h.getMaNguoiDung() == null ? "" : String.valueOf(h.getMaNguoiDung()));
        dcsngaybatdauthue.setDate(h.getNgayBatDau());
        dcsngayketthucthue.setDate(h.getNgayKetThuc());

        // hiển thị giá phòng
        Phong p = phongDAO.findById(h.getMaPhong());
        txtGiaPhong.setText(p == null || p.getGiaTien() == null ? "" : String.valueOf(p.getGiaTien()));
    }

    @Override
    public HopDong getForm() {
        String maHD = "xx".equalsIgnoreCase(lblmahopdong.getText()) ? null : lblmahopdong.getText();
        String maPhong = txtmaphong.getText().trim();
        String maNguoiDungStr = txtMaNguoiDung.getText().trim();

        if (maPhong.isEmpty()) { JOptionPane.showMessageDialog(this, "Mã phòng không được trống"); return null; }

        Integer maNguoiDung = null;
        if (!maNguoiDungStr.isEmpty()) {
            try { maNguoiDung = Integer.valueOf(maNguoiDungStr); }
            catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Mã người dùng không hợp lệ"); return null; }
        }

        Date nbd = dcsngaybatdauthue.getDate();
        Date nkt = dcsngayketthucthue.getDate();
        if (nbd == null || nkt == null) { JOptionPane.showMessageDialog(this, "Chọn ngày bắt đầu/kết thúc"); return null; }
        if (nkt.before(nbd)) { JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu"); return null; }

HopDong hd = new HopDong();
hd.setMaHopDong(maHD);
hd.setMaPhong(maPhong);
hd.setMaNguoiDung(maNguoiDung);
hd.setNgayBatDau(nbd);
hd.setNgayKetThuc(nkt);

return hd;
    }

    @Override
    public void add() {
         if (!validateForm()) {
        return; // Nếu validate fail thì dừng lại
    }
        HopDong h = getForm();
        if (h == null) return;

        if (h.getMaHopDong() != null && hopDongDAO.findById(h.getMaHopDong()) != null) {
            JOptionPane.showMessageDialog(this, "Mã hợp đồng đã tồn tại!");
            return;
        }

        hopDongDAO.create(h);
        loadTable();
        JOptionPane.showMessageDialog(this, "Thêm thành công");
        clearForm();
    }

    @Override
    public void update() {
        HopDong h = getForm();
        if (h == null) return;
        if (h.getMaHopDong() == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn hợp đồng để cập nhật");
            return;
        }
        hopDongDAO.update(h);
        loadTable();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công");
    }

    @Override
    public void delete() {
        int row = tblHopDong.getSelectedRow();
        String id = null;
        if (row >= 0) id = model.getValueAt(row, 0).toString();
        else if (!"xx".equalsIgnoreCase(lblmahopdong.getText())) id = lblmahopdong.getText();

        if (id == null || id.isBlank()) {
            JOptionPane.showMessageDialog(this, "Chọn hợp đồng để xóa"); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa hợp đồng " + id + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            hopDongDAO.deleteById(id);
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã xóa");
        }
    }

    @Override
    public void tableRowClick(int row) {
        if (row < 0) return;
        String id = model.getValueAt(row, 0).toString();
        HopDong h = hopDongDAO.findById(id);
        if (h != null) setForm(h);
    }

    private boolean validateForm() {
    // Kiểm tra Mã Hợp Đồng
    if (lblmahopdong.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Mã hợp đồng không được để trống!");
        lblmahopdong.requestFocus();
        return false;
    }

    // Kiểm tra Mã Phòng
    if (txtmaphong.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Mã phòng không được để trống!");
        txtmaphong.requestFocus();
        return false;
    }

    // Kiểm tra Mã Người Dùng (phải là số)
    if (txtMaNguoiDung.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Mã người dùng không được để trống!");
        txtMaNguoiDung.requestFocus();
        return false;
    }
    try {
        Integer.parseInt(txtMaNguoiDung.getText().trim());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Mã người dùng phải là số!");
        txtMaNguoiDung.requestFocus();
        return false;
    }

    // Kiểm tra ngày
    Date ngayBD = dcsngaybatdauthue.getDate();
    Date ngayKT = dcsngayketthucthue.getDate();

    if (ngayBD == null) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu thuê!");
        dcsngaybatdauthue.requestFocus();
        return false;
    }
    if (ngayKT == null) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày kết thúc thuê!");
        dcsngayketthucthue.requestFocus();
        return false;
    }
    if (ngayKT.before(ngayBD)) {
        JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!");
        dcsngayketthucthue.requestFocus();
        return false;
    }

    return true;
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
        jPanel2 = new javax.swing.JPanel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtmaphong = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtMaNguoiDung = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtGiaPhong = new javax.swing.JTextField();
        lbl = new javax.swing.JLabel();
        lblmahopdong = new javax.swing.JLabel();
        dcsngaybatdauthue = new com.toedter.calendar.JDateChooser();
        dcsngayketthucthue = new com.toedter.calendar.JDateChooser();
        jPanel5 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHopDong = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(40, 46, 62));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(51, 204, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Tìm kiếm", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTimKiem.setForeground(new java.awt.Color(40, 46, 62));
        txtTimKiem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtTimKiemFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTimKiemFocusLost(evt);
            }
        });

        btnTimKiem.setBackground(new java.awt.Color(0, 0, 255));
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTimKiem.setForeground(new java.awt.Color(40, 46, 62));
        btnTimKiem.setText("Tìm kiếm");
        btnTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimKiemActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(197, 197, 197)
                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(btnTimKiem)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTimKiem))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 1000, -1));

        jPanel3.setBackground(new java.awt.Color(207, 243, 243));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(40, 46, 62));
        jLabel1.setText("HỢP ĐỒNG");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(40, 46, 62));
        jLabel2.setText("Mã phòng trọ:");

        txtmaphong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(40, 46, 62));
        jLabel3.setText("Mã người dùng:");

        txtMaNguoiDung.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(40, 46, 62));
        jLabel4.setText("Ngày BĐ thuê:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(40, 46, 62));
        jLabel5.setText("Ngày KT thuê:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(40, 46, 62));
        jLabel7.setText("Giá phòng:");

        txtGiaPhong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lbl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbl.setForeground(new java.awt.Color(255, 205, 31));
        lbl.setText("MÃ HĐ:");

        lblmahopdong.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblmahopdong.setForeground(new java.awt.Color(255, 205, 31));
        lblmahopdong.setText("XX");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(txtmaphong, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(dcsngaybatdauthue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(2, 2, 2)))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtGiaPhong, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 1, Short.MAX_VALUE)
                        .addComponent(txtMaNguoiDung, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dcsngayketthucthue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(42, 42, 42))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(65, 65, 65)
                .addComponent(lbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblmahopdong)
                .addGap(124, 124, 124))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl)
                    .addComponent(lblmahopdong))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtmaphong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtMaNguoiDung, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5))
                            .addComponent(dcsngaybatdauthue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(txtGiaPhong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(dcsngayketthucthue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 94, 640, 170));

        jPanel5.setBackground(new java.awt.Color(207, 243, 243));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        btnThem.setBackground(new java.awt.Color(255, 205, 31));
        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnThem.setForeground(new java.awt.Color(40, 46, 62));
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        btnSua.setBackground(new java.awt.Color(255, 205, 31));
        btnSua.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSua.setForeground(new java.awt.Color(40, 46, 62));
        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        btnXoa.setBackground(new java.awt.Color(255, 205, 31));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(40, 46, 62));
        btnXoa.setText("Xóa");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        btnReset.setBackground(new java.awt.Color(255, 205, 31));
        btnReset.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnReset.setForeground(new java.awt.Color(40, 46, 62));
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(btnSua, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 100, 340, 160));

        tblHopDong.setBackground(new java.awt.Color(207, 243, 243));
        tblHopDong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblHopDong.setForeground(new java.awt.Color(40, 46, 62));
        tblHopDong.setModel(new javax.swing.table.DefaultTableModel(
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
        tblHopDong.setGridColor(new java.awt.Color(255, 255, 255));
        tblHopDong.setSelectionBackground(new java.awt.Color(46, 56, 86));
        tblHopDong.setSelectionForeground(new java.awt.Color(255, 205, 31));
        tblHopDong.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHopDongMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHopDong);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 1047, 265));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen03.jpg"))); // NOI18N
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1090, 570));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1099, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 4, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 5, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 570, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTimKiemFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusGained
        // TODO add your handling code here:
        if ("Nhập mã hợp đồng để tìm…".equals(txtTimKiem.getText())) txtTimKiem.setText("");
    
    }//GEN-LAST:event_txtTimKiemFocusGained

    private void txtTimKiemFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusLost
        // TODO add your handling code here:
        if (txtTimKiem.getText().isBlank()) txtTimKiem.setText("Nhập mã hợp đồng để tìm…");
    
    }//GEN-LAST:event_txtTimKiemFocusLost

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimKiemActionPerformed
        // TODO add your handling code here:
        search(txtTimKiem.getText());
    }//GEN-LAST:event_btnTimKiemActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed

        add();
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        clearForm(); loadTable();
    }//GEN-LAST:event_btnResetActionPerformed

    private void tblHopDongMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHopDongMouseClicked
        // TODO add your handling code here:
        tableRowClick(tblHopDong.getSelectedRow());
    }//GEN-LAST:event_tblHopDongMouseClicked

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
            java.util.logging.Logger.getLogger(HopDongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HopDongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HopDongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HopDongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                HopDongJDialog dialog = new HopDongJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXoa;
    private com.toedter.calendar.JDateChooser dcsngaybatdauthue;
    private com.toedter.calendar.JDateChooser dcsngayketthucthue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl;
    private javax.swing.JLabel lblmahopdong;
    private javax.swing.JTable tblHopDong;
    private javax.swing.JTextField txtGiaPhong;
    private javax.swing.JTextField txtMaNguoiDung;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtmaphong;
    // End of variables declaration//GEN-END:variables
}
