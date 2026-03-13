/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main.ChuTro;

import App.DAO.PhongDAO;
import App.Entity.Phong;
import App.Impl.PhongDAOImpl;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;


/**
 *
 * @author PHONG
 */
public class PhongJDialog extends javax.swing.JDialog implements PhongController{

    /**
     * Creates new form PhongJDialog
     */
    public PhongJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        afterInit();
                setLocationRelativeTo(null);
    }


    private final PhongDAO dao = new PhongDAOImpl();
    private DefaultTableModel model;

    // ===== lifecycle =====
    private void afterInit() {
        model = (DefaultTableModel) tblPhongTro.getModel();
        model.setColumnIdentifiers(new Object[]{
                "Mã phòng", "Trạng thái", "Giá tiền", "Diện tích", "Địa chỉ", "Liên hệ", "Mô tả"
        });

        txtTimKiem.setText("Nhập mã phòng trọ để tìm kiếm phòng!");
        loadTable();

        lblanhphong.setToolTipText("Nhấn để chọn ảnh phòng");
        lblanhphong.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { chooseImage(); }
        });
    }

    // ===== Controller impl =====
    @Override
    public void loadTable() {
        model.setRowCount(0);
        List<Phong> list = dao.findAll();
        for (Phong p : list) {
            model.addRow(new Object[]{
                    p.getMaPhong(),
                    p.getTrangThai(),
                    p.getGiaTien(),
                    p.getDienTich(),
                    p.getDiaChi(),
                    p.getLienHe(),
                    p.getMoTa()
            });
        }
    }

    @Override
    public void search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty() || "Nhập mã phòng trọ để tìm kiếm phòng!".equals(kw)) {
            loadTable(); return;
        }
        model.setRowCount(0);
        // dùng search(tuKhoa, giaMin, giaMax, dtMin, dtMax)
        List<Phong> list = dao.search(kw, null, null, null, null);
        for (Phong p : list) {
            model.addRow(new Object[]{
                    p.getMaPhong(), p.getTrangThai(), p.getGiaTien(), p.getDienTich(),
                    p.getDiaChi(), p.getLienHe(), p.getMoTa()
            });
        }
    }

    @Override
    public void clearForm() {
        txtMaPhong.setText("");
        txtlienhe.setText("");
        txtDienTich.setText("");
        txtGiatien.setText("");
        txtDiaChi.setText("");
        txtMoTa.setText("");
        rbtrong.setSelected(true);
        setImageToLabel(null);
        tblPhongTro.clearSelection();
        txtMaPhong.requestFocus();
    }

    @Override
    public void setForm(Phong p) {
        txtMaPhong.setText(p.getMaPhong());
        txtlienhe.setText(p.getLienHe());
        txtDienTich.setText(p.getDienTich() == null ? "" : p.getDienTich().toPlainString());
        txtGiatien.setText(p.getGiaTien() == null ? "" : p.getGiaTien().toPlainString());
        txtDiaChi.setText(p.getDiaChi());
        txtMoTa.setText(p.getMoTa());
        if ("Đang thuê".equalsIgnoreCase(p.getTrangThai())) rbDangThue.setSelected(true);
        else rbtrong.setSelected(true);
        setImageToLabel(p.getAnhPhong());
    }

    @Override
    public Phong getForm() {
        String ma = txtMaPhong.getText().trim();
        if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Mã phòng không được trống"); txtMaPhong.requestFocus(); return null; }

        BigDecimal giaTien = null, dienTich = null;
        try { if (!txtGiatien.getText().trim().isEmpty()) giaTien = new BigDecimal(txtGiatien.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Giá tiền không hợp lệ"); return null; }
        try { if (!txtDienTich.getText().trim().isEmpty()) dienTich = new BigDecimal(txtDienTich.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Diện tích không hợp lệ"); return null; }

        String trangThai = rbDangThue.isSelected() ? "Đang thuê" : "Trống";

Phong p = new Phong();
p.setMaPhong(ma);
p.setTrangThai(trangThai);
p.setGiaTien(giaTien);
p.setDienTich(dienTich);
p.setDiaChi(txtDiaChi.getText().trim());
p.setLienHe(txtlienhe.getText().trim());
p.setMoTa(txtMoTa.getText().trim());
p.setAnhPhong((String) lblanhphong.getClientProperty("path"));

return p;
    }

    @Override
    public void add() {
        Phong p = getForm();
        if (p == null) return;
        if (dao.findById(p.getMaPhong()) != null) { JOptionPane.showMessageDialog(this, "Mã phòng đã tồn tại!"); return; }
        dao.create(p);
        loadTable();
        JOptionPane.showMessageDialog(this, "Thêm thành công");
    }

    @Override
    public void update() {
        Phong p = getForm();
        if (p == null) return;
        if (dao.findById(p.getMaPhong()) == null) { JOptionPane.showMessageDialog(this, "Không tìm thấy mã phòng để cập nhật!"); return; }
        dao.update(p);
        loadTable();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công");
    }

    @Override
    public void delete() {
        int row = tblPhongTro.getSelectedRow();
        String ma = row >= 0 ? model.getValueAt(row, 0).toString() : txtMaPhong.getText().trim();
        if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn phòng để xóa"); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa phòng " + ma + " ?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.deleteById(ma);
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã xóa");
        }
    }

    @Override
    public void tableRowClick(int row) {
        if (row < 0) return;
        String ma = model.getValueAt(row, 0).toString();
        Phong p = dao.findById(ma);
        if (p != null) setForm(p);
    }

    // ===== ảnh =====
    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh phòng");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setImageToLabel(fc.getSelectedFile().getAbsolutePath());
        }
    }
    private void setImageToLabel(String path) {
        if (path == null || path.isBlank()) {
            lblanhphong.setIcon(null);
            lblanhphong.putClientProperty("path", null);
            return;
        }
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(lblanhphong.getWidth(), lblanhphong.getHeight(), Image.SCALE_SMOOTH);
        lblanhphong.setIcon(new ImageIcon(img));
        lblanhphong.putClientProperty("path", path);
    }
    
// ===== validate =====
    private boolean validatePhong() {
    String maPhong = txtMaPhong.getText().trim();
    String lienHe = txtlienhe.getText().trim();
    String giaTien = txtGiatien.getText().trim();
    String dienTich = txtDienTich.getText().trim();
    String diaChi = txtDiaChi.getText().trim();

    if (maPhong.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Mã phòng không được để trống!");
        txtMaPhong.requestFocus();
        return false;
    }
    if (lienHe.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại liên hệ!");
        txtlienhe.requestFocus();
        return false;
    }
    if (!lienHe.matches("^0\\d{9,10}$")) { // kiểm tra sđt Việt Nam
        JOptionPane.showMessageDialog(this, "Số điện thoại liên hệ không hợp lệ!");
        txtlienhe.requestFocus();
        return false;
    }
    if (!giaTien.isBlank()) {
        try {
            BigDecimal g = new BigDecimal(giaTien);
            if (g.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Giá tiền phải > 0!");
                txtGiatien.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá tiền không hợp lệ!");
            txtGiatien.requestFocus();
            return false;
        }
    }
    if (!dienTich.isBlank()) {
        try {
            BigDecimal dt = new BigDecimal(dienTich);
            if (dt.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Diện tích phải > 0!");
                txtDienTich.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Diện tích không hợp lệ!");
            txtDienTich.requestFocus();
            return false;
        }
    }
    if (diaChi.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Địa chỉ không được để trống!");
        txtDiaChi.requestFocus();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblanhphong = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMaPhong = new javax.swing.JTextField();
        txtlienhe = new javax.swing.JTextField();
        txtDienTich = new javax.swing.JTextField();
        txtGiatien = new javax.swing.JTextField();
        txtDiaChi = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMoTa = new javax.swing.JTextArea();
        rbDangThue = new javax.swing.JRadioButton();
        rbtrong = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnRest = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPhongTro = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(40, 46, 62));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(51, 204, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2), "Tìm kiếm", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N

        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTimKiem.setForeground(new java.awt.Color(40, 46, 62));
        txtTimKiem.setText("Nhập mã phòng trọ để tìm kiếm phòng!");
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
                .addGap(122, 122, 122)
                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 691, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(41, 6, 1000, -1));

        jPanel3.setBackground(new java.awt.Color(207, 243, 243));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(40, 46, 62));
        jLabel1.setText("QUẢN LÝ PHÒNG TRỌ");
        jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(263, 8, 287, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(40, 46, 62));
        jLabel2.setText("Mã phòng:");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 42, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(40, 46, 62));
        jLabel4.setText("Liên hệ:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 74, -1, -1));

        lblanhphong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblanhphong.setForeground(new java.awt.Color(40, 46, 62));
        lblanhphong.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        lblanhphong.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblanhphongMouseClicked(evt);
            }
        });
        jPanel3.add(lblanhphong, new org.netbeans.lib.awtextra.AbsoluteConstraints(356, 103, 260, 150));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(40, 46, 62));
        jLabel6.setText("Giá tiền:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 70, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(40, 46, 62));
        jLabel7.setText("Diện tích:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 106, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(40, 46, 62));
        jLabel8.setText("Địa chỉ:");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 138, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(40, 46, 62));
        jLabel9.setText("Mô tả:");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 167, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(40, 46, 62));
        jLabel10.setText("Trạng thái:");
        jPanel3.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(329, 42, -1, -1));

        txtMaPhong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMaPhong.setForeground(new java.awt.Color(40, 46, 62));
        jPanel3.add(txtMaPhong, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 39, 213, -1));

        txtlienhe.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtlienhe.setForeground(new java.awt.Color(40, 46, 62));
        txtlienhe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtlienheActionPerformed(evt);
            }
        });
        jPanel3.add(txtlienhe, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 71, 214, -1));

        txtDienTich.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDienTich.setForeground(new java.awt.Color(40, 46, 62));
        jPanel3.add(txtDienTich, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 103, 214, -1));

        txtGiatien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtGiatien.setForeground(new java.awt.Color(40, 46, 62));
        jPanel3.add(txtGiatien, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, 183, -1));

        txtDiaChi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDiaChi.setForeground(new java.awt.Color(40, 46, 62));
        jPanel3.add(txtDiaChi, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 135, 214, -1));

        txtMoTa.setColumns(20);
        txtMoTa.setRows(5);
        jScrollPane1.setViewportView(txtMoTa);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(98, 167, -1, -1));

        rbDangThue.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup1.add(rbDangThue);
        rbDangThue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbDangThue.setForeground(new java.awt.Color(40, 46, 62));
        rbDangThue.setText("Đang thuê");
        jPanel3.add(rbDangThue, new org.netbeans.lib.awtextra.AbsoluteConstraints(413, 40, -1, -1));

        rbtrong.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup1.add(rbtrong);
        rbtrong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbtrong.setForeground(new java.awt.Color(40, 46, 62));
        rbtrong.setText("Trống");
        jPanel3.add(rbtrong, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 40, -1, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(41, 96, 690, 270));

        jPanel4.setBackground(new java.awt.Color(207, 243, 243));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jPanel4.setToolTipText("");

        btnThem.setBackground(new java.awt.Color(255, 205, 31));
        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThem.setForeground(new java.awt.Color(40, 46, 62));
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        btnSua.setBackground(new java.awt.Color(255, 205, 31));
        btnSua.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSua.setForeground(new java.awt.Color(40, 46, 62));
        btnSua.setText("Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });

        btnXoa.setBackground(new java.awt.Color(255, 205, 31));
        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setForeground(new java.awt.Color(40, 46, 62));
        btnXoa.setText("Xóa");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        btnRest.setBackground(new java.awt.Color(255, 205, 31));
        btnRest.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRest.setForeground(new java.awt.Color(40, 46, 62));
        btnRest.setText("Reset");
        btnRest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnThem, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSua, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRest, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRest, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 100, 300, 260));

        tblPhongTro.setBackground(new java.awt.Color(207, 243, 243));
        tblPhongTro.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblPhongTro.setForeground(new java.awt.Color(40, 46, 62));
        tblPhongTro.setModel(new javax.swing.table.DefaultTableModel(
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
        tblPhongTro.setGridColor(new java.awt.Color(255, 255, 255));
        tblPhongTro.setSelectionBackground(new java.awt.Color(40, 46, 62));
        tblPhongTro.setSelectionForeground(new java.awt.Color(255, 205, 31));
        tblPhongTro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPhongTroMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblPhongTro);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 380, 1000, 253));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen03.jpg"))); // NOI18N
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 650));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 650, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTimKiemFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusGained
        // TODO add your handling code here:
        if ("Nhập mã phòng trọ để tìm kiếm phòng!".equals(txtTimKiem.getText())) {
            txtTimKiem.setText("");
        }
    }//GEN-LAST:event_txtTimKiemFocusGained

    private void txtTimKiemFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusLost
        // TODO add your handling code here:
        if (txtTimKiem.getText().isBlank()) {
            txtTimKiem.setText("Nhập mã phòng trọ để tìm kiếm phòng!");
        }
    }//GEN-LAST:event_txtTimKiemFocusLost

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimKiemActionPerformed
        search(txtTimKiem.getText());

    }//GEN-LAST:event_btnTimKiemActionPerformed

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // TODO add your handling code here:
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

    private void btnRestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestActionPerformed
        clearForm();
        loadTable();
    }//GEN-LAST:event_btnRestActionPerformed

    private void tblPhongTroMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPhongTroMouseClicked
        // TODO add your handling code here:
        tableRowClick(tblPhongTro.getSelectedRow());
    }//GEN-LAST:event_tblPhongTroMouseClicked

    private void txtlienheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtlienheActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtlienheActionPerformed

    private void lblanhphongMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblanhphongMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_lblanhphongMouseClicked

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
            java.util.logging.Logger.getLogger(PhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PhongJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PhongJDialog dialog = new PhongJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnRest;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXoa;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblanhphong;
    private javax.swing.JRadioButton rbDangThue;
    private javax.swing.JRadioButton rbtrong;
    private javax.swing.JTable tblPhongTro;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtDienTich;
    private javax.swing.JTextField txtGiatien;
    private javax.swing.JTextField txtMaPhong;
    private javax.swing.JTextArea txtMoTa;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txtlienhe;
    // End of variables declaration//GEN-END:variables
}
