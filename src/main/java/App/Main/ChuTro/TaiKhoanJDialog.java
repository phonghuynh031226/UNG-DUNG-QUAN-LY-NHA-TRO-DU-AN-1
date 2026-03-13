/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package App.Main.ChuTro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Image;
import java.util.Date;
import java.util.List;

import App.DAO.TaiKhoanDAO;          // <— SỬA: dùng đúng package
import App.Impl.TaiKhoanDAOImpl;     // <— SỬA
import App.Entity.TaiKhoan;          // <— SỬA
/**
 *
 * @author PHONG
 */
public class TaiKhoanJDialog extends javax.swing.JDialog implements TaiKhoanController{

    /**
     * Creates new form TaiKhoanJDialog
     */
    public TaiKhoanJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        afterInit();
                setLocationRelativeTo(null);
    }
    
    private final TaiKhoanDAO dao = new TaiKhoanDAOImpl();
    private DefaultTableModel model;
    
        private void afterInit() {
        model = (DefaultTableModel) tblNguoiDung.getModel();
        model.setColumnIdentifiers(new Object[]{
            "Mã ND","Tên TK","Họ tên","Giới tính","Ngày sinh","SĐT","Email","Địa chỉ","Vai trò","Trạng thái"
        });

        txtTimKiem.setText("Nhập tên tài khoản để tìm…");
        loadTable();

        // Chọn ảnh: click vào label
        lblanhnguoidung.setToolTipText("Nhấn để chọn ảnh");
        lblanhnguoidung.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { chooseImage(); }
        });
    }

    
        /* ================= Controller ================= */

    @Override
    public void loadTable() {
        model.setRowCount(0);
        List<TaiKhoan> list = dao.findAll();
        for (TaiKhoan t : list) {
            model.addRow(new Object[]{
                t.getMaNguoiDung(),
                t.getTenTaiKhoan(),
                t.getHoTen(),
                t.getGioiTinh(),
                t.getNgaySinh(),
                t.getDienThoai(),
                t.getEmail(),
                t.getDiaChi(),
                (t.getVaiTro()!=null && t.getVaiTro()==1) ? "Admin" : "Người thuê",
                (t.getTrangThai()!=null && t.getTrangThai()==1) ? "Hoạt động" : "Bị khóa"
            });
        }
    }

    @Override
    public void search(String keyword) {
        String s = keyword == null ? "" : keyword.trim();
        if (s.isEmpty() || "Nhập tên tài khoản để tìm…".equals(s)) { loadTable(); return; }
        model.setRowCount(0);
        TaiKhoan t = dao.findByTenTaiKhoan(s);
        if (t != null) {
            model.addRow(new Object[]{
                t.getMaNguoiDung(), t.getTenTaiKhoan(), t.getHoTen(),
                t.getGioiTinh(), t.getNgaySinh(), t.getDienThoai(),
                t.getEmail(), t.getDiaChi(),
                (t.getVaiTro()!=null && t.getVaiTro()==1) ? "Admin" : "Người thuê",
                (t.getTrangThai()!=null && t.getTrangThai()==1) ? "Hoạt động" : "Bị khóa"
            });
        }
    }

    @Override
    public void clearForm() {
        lblmataikhoan.setText("XX");
        txttentaikhoan.setText("");
        txtMK.setText("");
        txtHoTen.setText("");
        rbnam.setSelected(true);
        dcsNgaySinh.setDate(null);
        txtSoDT.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        txtCccd.setText("");
        rbadmin.setSelected(false);
        rbnguoithue.setSelected(true);
        rbhoatdong.setSelected(true);
        rbbikhoa.setSelected(false);
        setImageToLabel(null);
        tblNguoiDung.clearSelection();
        txttentaikhoan.requestFocus();
    }

    @Override
    public void setForm(TaiKhoan t) {
        lblmataikhoan.setText(t.getMaNguoiDung()==null?"XX":String.valueOf(t.getMaNguoiDung()));
        txttentaikhoan.setText(t.getTenTaiKhoan());
        txtMK.setText(t.getMatKhau());
        txtHoTen.setText(t.getHoTen());
        if ("Nam".equalsIgnoreCase(t.getGioiTinh())) rbnam.setSelected(true); else rbnu.setSelected(true);
        Date ns = t.getNgaySinh(); dcsNgaySinh.setDate(ns);
        txtSoDT.setText(t.getDienThoai());
        txtEmail.setText(t.getEmail());
        txtDiaChi.setText(t.getDiaChi());
        txtCccd.setText(t.getCccd());
        if (t.getVaiTro()!=null && t.getVaiTro()==1) rbadmin.setSelected(true); else rbnguoithue.setSelected(true);
        if (t.getTrangThai()!=null && t.getTrangThai()==1) rbhoatdong.setSelected(true); else rbbikhoa.setSelected(true);
        setImageToLabel(t.getHinhAnh());
    }

    @Override
    public TaiKhoan getForm() {
        String tenTK = txttentaikhoan.getText().trim();
        String mk = new String(txtMK.getPassword()).trim();
        if (tenTK.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên tài khoản không được trống"); return null; }

        Integer id = "XX".equals(lblmataikhoan.getText()) ? null : Integer.valueOf(lblmataikhoan.getText());
        String gt = rbnam.isSelected() ? "Nam" : "Nữ";
        Integer vaiTro = rbadmin.isSelected() ? 1 : 2;
        Integer trangThai = rbhoatdong.isSelected() ? 1 : 0;

TaiKhoan tk = new TaiKhoan();

tk.setMaNguoiDung(id);
tk.setTenTaiKhoan(tenTK);
tk.setMatKhau(mk.isEmpty() ? null : mk);
tk.setHoTen(txtHoTen.getText().trim());
tk.setNgaySinh(dcsNgaySinh.getDate());
tk.setGioiTinh(gt);
tk.setCccd(txtCccd.getText().trim());
tk.setDienThoai(txtSoDT.getText().trim());
tk.setEmail(txtEmail.getText().trim());
tk.setDiaChi(txtDiaChi.getText().trim());
tk.setVaiTro(vaiTro);
tk.setTrangThai(trangThai);
tk.setHinhAnh((String) lblanhnguoidung.getClientProperty("path"));

return tk;
    }

    @Override
    public void add() {
        TaiKhoan t = getForm();
        if (t == null) return;
        if (dao.isTenDangNhapTonTai(t.getTenTaiKhoan())) {
            JOptionPane.showMessageDialog(this, "Tên tài khoản đã tồn tại!"); return;
        }
        if (t.getMatKhau()==null || t.getMatKhau().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu!"); return;
        }
        dao.create(t);
        loadTable();
        JOptionPane.showMessageDialog(this, "Thêm thành công");
        clearForm();
    }

    @Override
    public void update() {
        TaiKhoan t = getForm();
        if (t == null) return;
        if (t.getMaNguoiDung()==null) { JOptionPane.showMessageDialog(this,"Chưa chọn người dùng"); return; }
        if (t.getMatKhau()==null || t.getMatKhau().isEmpty()) {
            TaiKhoan old = dao.findById(t.getMaNguoiDung());
            if (old != null) t.setMatKhau(old.getMatKhau());
        }
        dao.update(t);
        loadTable();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công");
    }

    @Override
    public void delete() {
        int row = tblNguoiDung.getSelectedRow();
        Integer id = null;
        if (row >= 0) id = Integer.valueOf(model.getValueAt(row, 0).toString());
        else if (!"XX".equals(lblmataikhoan.getText())) id = Integer.valueOf(lblmataikhoan.getText());
        if (id == null) { JOptionPane.showMessageDialog(this,"Chọn người dùng để xóa"); return; }
        if (JOptionPane.showConfirmDialog(this,"Xóa người dùng "+id+"?", "Xác nhận",
            JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            dao.deleteById(id);
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this,"Đã xóa");
        }
    }

    @Override
    public void tableRowClick(int row) {
        if (row < 0) return;
        Integer id = Integer.valueOf(model.getValueAt(row,0).toString());
        TaiKhoan t = dao.findById(id);
        if (t != null) setForm(t);
    }
    /* ================= Ảnh đại diện ================= */

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh người dùng");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setImageToLabel(fc.getSelectedFile().getAbsolutePath());
        }
    }
    private void setImageToLabel(String path) {
        if (path == null || path.isBlank()) {
            lblanhnguoidung.setIcon(null);
            lblanhnguoidung.putClientProperty("path", null);
            return;
        }
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(
            lblanhnguoidung.getWidth(), lblanhnguoidung.getHeight(), Image.SCALE_SMOOTH);
        lblanhnguoidung.setIcon(new ImageIcon(img));
        lblanhnguoidung.putClientProperty("path", path);
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        txtTimKiem = new javax.swing.JTextField();
        btnTimKiem = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtHoTen = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSoDT = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDiaChi = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        rbnam = new javax.swing.JRadioButton();
        rbnu = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        lblmataikhoan = new javax.swing.JLabel();
        txtMK = new javax.swing.JPasswordField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtCccd = new javax.swing.JTextField();
        txttentaikhoan = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        rbnguoithue = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        rbadmin = new javax.swing.JRadioButton();
        rbhoatdong = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        rbbikhoa = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        lblanhnguoidung = new javax.swing.JLabel();
        dcsNgaySinh = new com.toedter.calendar.JDateChooser();
        jPanel4 = new javax.swing.JPanel();
        btnThem = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblNguoiDung = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();

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
        btnTimKiem.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59)
                .addComponent(btnTimKiem)
                .addGap(88, 88, 88))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTimKiem))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(47, 21, 917, -1));

        jPanel3.setBackground(new java.awt.Color(207, 243, 243));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(40, 46, 62));
        jLabel1.setText("QUẢN LÝ NGƯỜI DÙNG");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(40, 46, 62));
        jLabel2.setText("Họ tên:");

        txtHoTen.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtHoTen.setForeground(new java.awt.Color(40, 46, 62));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(40, 46, 62));
        jLabel3.setText("Số điện thoại:");

        txtSoDT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSoDT.setForeground(new java.awt.Color(40, 46, 62));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(40, 46, 62));
        jLabel4.setText("Email:");

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(40, 46, 62));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(40, 46, 62));
        jLabel5.setText("Địa chỉ:");

        txtDiaChi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDiaChi.setForeground(new java.awt.Color(40, 46, 62));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(40, 46, 62));
        jLabel6.setText("Giới tính:");

        rbnam.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup1.add(rbnam);
        rbnam.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbnam.setForeground(new java.awt.Color(40, 46, 62));
        rbnam.setText("Nam");

        rbnu.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup1.add(rbnu);
        rbnu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbnu.setForeground(new java.awt.Color(40, 46, 62));
        rbnu.setText("Nữ");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(40, 46, 62));
        jLabel8.setText("Mã người dùng:");

        lblmataikhoan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblmataikhoan.setForeground(new java.awt.Color(255, 205, 31));
        lblmataikhoan.setText("XX");

        txtMK.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtMK.setForeground(new java.awt.Color(40, 46, 62));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(40, 46, 62));
        jLabel9.setText("Mật khẩu:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(40, 46, 62));
        jLabel10.setText("CCCD:");

        txtCccd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCccd.setForeground(new java.awt.Color(40, 46, 62));

        txttentaikhoan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txttentaikhoan.setForeground(new java.awt.Color(40, 46, 62));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(40, 46, 62));
        jLabel11.setText("Tên tài khoản:");

        rbnguoithue.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup2.add(rbnguoithue);
        rbnguoithue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbnguoithue.setForeground(new java.awt.Color(40, 46, 62));
        rbnguoithue.setText("Người thuê");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(40, 46, 62));
        jLabel12.setText("Vai trò:");

        rbadmin.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup2.add(rbadmin);
        rbadmin.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbadmin.setForeground(new java.awt.Color(40, 46, 62));
        rbadmin.setText("Admin");

        rbhoatdong.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup3.add(rbhoatdong);
        rbhoatdong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbhoatdong.setForeground(new java.awt.Color(40, 46, 62));
        rbhoatdong.setText("Hoạt động");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(40, 46, 62));
        jLabel13.setText("Trạng thái:");

        rbbikhoa.setBackground(new java.awt.Color(207, 243, 243));
        buttonGroup3.add(rbbikhoa);
        rbbikhoa.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        rbbikhoa.setForeground(new java.awt.Color(40, 46, 62));
        rbbikhoa.setText("Bị khóa");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(40, 46, 62));
        jLabel14.setText("Ngày sinh:");

        lblanhnguoidung.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel10)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtHoTen, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                                    .addComponent(txtSoDT)
                                    .addComponent(txtEmail)
                                    .addComponent(txtCccd)
                                    .addComponent(dcsNgaySinh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(26, 26, 26)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel11)
                                            .addComponent(jLabel9))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtMK, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txttentaikhoan, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addGap(36, 36, 36)
                                        .addComponent(rbadmin)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rbnguoithue))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(rbbikhoa)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rbhoatdong))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(63, 63, 63)
                                        .addComponent(lblanhnguoidung, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 12, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(151, 151, 151)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblmataikhoan)
                        .addGap(38, 38, 38))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(36, 36, 36)
                        .addComponent(rbnam)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbnu)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel8)
                    .addComponent(lblmataikhoan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtHoTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txttentaikhoan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSoDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtMK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(rbadmin)
                    .addComponent(rbnguoithue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(rbbikhoa)
                    .addComponent(rbhoatdong))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(dcsNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtCccd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(rbnam)
                            .addComponent(rbnu))
                        .addGap(42, 42, 42))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(lblanhnguoidung, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(47, 99, -1, -1));

        jPanel4.setBackground(new java.awt.Color(207, 243, 243));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThem, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSua, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(152, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 110, 350, 280));

        tblNguoiDung.setBackground(new java.awt.Color(207, 243, 243));
        tblNguoiDung.setModel(new javax.swing.table.DefaultTableModel(
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
        tblNguoiDung.setGridColor(new java.awt.Color(255, 255, 255));
        tblNguoiDung.setSelectionBackground(new java.awt.Color(255, 205, 31));
        tblNguoiDung.setSelectionForeground(new java.awt.Color(40, 46, 62));
        tblNguoiDung.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNguoiDungMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblNguoiDung);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 420, 917, 237));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/App/Icon/nen03.jpg"))); // NOI18N
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1010, 660));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTimKiemFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusGained
        // TODO add your handling code here:
   if ("Nhập tên tài khoản để tìm…".equals(txtTimKiem.getText())) txtTimKiem.setText("");
    }//GEN-LAST:event_txtTimKiemFocusGained

    private void txtTimKiemFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimKiemFocusLost
        // TODO add your handling code here:
    if (txtTimKiem.getText().isBlank()) txtTimKiem.setText("Nhập tên tài khoản để tìm…");
    }//GEN-LAST:event_txtTimKiemFocusLost

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimKiemActionPerformed
        // TODO add your handling code here:
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

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
    clearForm(); loadTable();
    }//GEN-LAST:event_btnResetActionPerformed

    private void tblNguoiDungMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNguoiDungMouseClicked
        // TODO add your handling code here:
    tableRowClick(tblNguoiDung.getSelectedRow());
    }//GEN-LAST:event_tblNguoiDungMouseClicked

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
            java.util.logging.Logger.getLogger(TaiKhoanJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TaiKhoanJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TaiKhoanJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TaiKhoanJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TaiKhoanJDialog dialog = new TaiKhoanJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private com.toedter.calendar.JDateChooser dcsNgaySinh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblanhnguoidung;
    private javax.swing.JLabel lblmataikhoan;
    private javax.swing.JRadioButton rbadmin;
    private javax.swing.JRadioButton rbbikhoa;
    private javax.swing.JRadioButton rbhoatdong;
    private javax.swing.JRadioButton rbnam;
    private javax.swing.JRadioButton rbnguoithue;
    private javax.swing.JRadioButton rbnu;
    private javax.swing.JTable tblNguoiDung;
    private javax.swing.JTextField txtCccd;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JPasswordField txtMK;
    private javax.swing.JTextField txtSoDT;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JTextField txttentaikhoan;
    // End of variables declaration//GEN-END:variables
}
