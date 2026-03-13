package App.Entity;

import java.math.BigDecimal;
import java.util.Date;

public class HoaDon {

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public BigDecimal getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(BigDecimal tienPhong) {
        this.tienPhong = tienPhong;
    }

    public BigDecimal getTienDien() {
        return tienDien;
    }

    public void setTienDien(BigDecimal tienDien) {
        this.tienDien = tienDien;
    }

    public BigDecimal getTienNuoc() {
        return tienNuoc;
    }

    public void setTienNuoc(BigDecimal tienNuoc) {
        this.tienNuoc = tienNuoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(Date ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public Date getNgayTaoHoaDon() {
        return ngayTaoHoaDon;
    }

    public void setNgayTaoHoaDon(Date ngayTaoHoaDon) {
        this.ngayTaoHoaDon = ngayTaoHoaDon;
    }
    private String maHoaDon; // ma hoa dơn
    private String maHopDong;
    private BigDecimal tienPhong;
    private BigDecimal tienDien;
    private BigDecimal tienNuoc;
    private String trangThai;      // Chưa thanh toán / Đã thanh toán
    private Date ngayThanhToan;    // nullable
    private Date ngayTaoHoaDon;
}
