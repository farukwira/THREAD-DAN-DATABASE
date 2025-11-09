// Import library untuk koneksi database dan manipulasi tanggal
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// ==========================
// 1️⃣ Kelas DatabaseConnection
// ==========================
// Kelas ini bertanggung jawab untuk mengatur koneksi ke database MySQL
class DatabaseConnection {
    // Informasi koneksi ke database
    private static final String URL = "jdbc:mysql://localhost:3306/restorandb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";       // username MySQL
    private static final String PASSWORD = "";       // password MySQL (kosong jika default XAMPP)

    // Method untuk mendapatkan koneksi ke database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load driver MySQL ke dalam program
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Coba lakukan koneksi
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL tidak ditemukan!");
        } catch (SQLException e) {
            System.out.println("❌ Gagal koneksi ke database: " + e.getMessage());
        }
        return conn; // Kembalikan objek koneksi (bisa null kalau gagal)
    }
}

// ==========================
// 2️⃣ Kelas PesanMakananService
// ==========================
// Kelas ini mengatur proses penyimpanan data pesanan ke database.
// Menggunakan synchronized supaya tidak terjadi konflik antar Thread.
class PesanMakananService {

    // synchronized: hanya 1 thread yang boleh menjalankan method ini dalam satu waktu
    public synchronized void simpanPesanan(String nama, String menu, int jumlah) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("⚠️ Tidak bisa menyimpan pesanan, koneksi gagal!");
            return; // keluar jika koneksi gagal
        }

        // Query SQL untuk menyimpan data ke tabel 'pesanan'
        String sql = "INSERT INTO pesanan (nama_pelanggan, menu, jumlah, waktu_pesan) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Isi parameter SQL dengan nilai dari parameter method
            ps.setString(1, nama);
            ps.setString(2, menu);
            ps.setInt(3, jumlah);
            // Format waktu saat ini menjadi 'YYYY-MM-DD HH:mm:ss'
            ps.setString(4, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            // Jalankan perintah INSERT ke database
            ps.executeUpdate();
            System.out.println("✅ Pesanan disimpan: " + nama + " memesan " + jumlah + " " + menu);
        } catch (SQLException e) {
            System.out.println("❌ Gagal menyimpan pesanan: " + e.getMessage());
        } finally {
            // Tutup koneksi agar tidak bocor
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}

// ==========================
// 3️⃣ Kelas PelangganRestoran (Thread)
// ==========================
// Kelas ini mewakili seorang pelanggan yang memesan makanan.
// Setiap pelanggan dijalankan sebagai Thread terpisah.
class PelangganRestoran extends Thread {
    private String nama;
    private String menu;
    private int jumlah;
    private PesanMakananService service;

    // Konstruktor untuk inisialisasi data pelanggan
    public PelangganRestoran(String nama, String menu, int jumlah, PesanMakananService service) {
        this.nama = nama;
        this.menu = menu;
        this.jumlah = jumlah;
        this.service = service;
    }

    // Method run() akan otomatis dipanggil ketika Thread dijalankan
    public void run() {
        System.out.println("🍽️ " + nama + " sedang memesan " + menu + "...");
        service.simpanPesanan(nama, menu, jumlah); // Simpan ke database
    }
}

// ==========================
// 4️⃣ Kelas Utama (Main Program)
// ==========================
// Kelas ini adalah titik awal program. Di sini kita membuat beberapa Thread pelanggan.
public class ThreadRestoranDemo {
    public static void main(String[] args) {
        // Buat objek service untuk mengatur penyimpanan pesanan
        PesanMakananService service = new PesanMakananService();

        // Buat beberapa pelanggan (setiap pelanggan = 1 thread)
        Thread t1 = new PelangganRestoran("Dewi", "Nasi Goreng", 2, service);
        Thread t2 = new PelangganRestoran("Rian", "Mie Ayam", 1, service);
        Thread t3 = new PelangganRestoran("Sari", "Ayam Bakar", 3, service);
        Thread t4 = new PelangganRestoran("Andi", "Sate Ayam", 2, service);

        // Jalankan semua thread secara paralel
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
