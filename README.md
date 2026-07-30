# 💬 AuraChat - Minimalist Messaging App for Android & Python Socket Server

**AuraChat** adalah aplikasi obrolan pesan instan untuk Android yang dirancang dengan estetika minimalis, cepat, dan terhubung langsung secara **real-time ke Python Socket TCP Server** (`server.py`). 

Aplikasi ini tidak memerlukan registrasi akun atau kata sandi—cukup masukkan nama Anda saat pertama kali membuka aplikasi, masukkan IP Server Python Anda, dan Anda langsung siap berkirim pesan privat maupun broadcast!

---

## 🌟 Fitur Utama

### 1. 🔌 Konektivitas Real-time ke Python Socket Server
- **Socket TCP Connection**: Berkomunikasi langsung dengan script Python (`server.py` di port 9090) menggunakan koneksi socket murni.
- **Auto-Connect On Startup**: Mengingat IP & Port server terakhir yang digunakan di `SharedPreferences`. Saat aplikasi dibuka kembali, AuraChat otomatis mencoba terhubung tanpa perlu menginput ulang IP.
- **Status Banner & Auto-Retry**: Banner status koneksi interaktif di bagian atas daftar obrolan (`Terkoneksi`, `Menghubungkan...`, `Tidak Terhubung`). Pengguna dapat melakukan *Coba Lagi* atau *Ganti IP Server* dalam satu sentuhan.
- **Protokol Pesan Python**:
  - `[Private dari <nama>]`: Pesan pribadi secara spesifik (`@nama_tujuan pesan`).
  - `[<nama>]:`: Pesan broadcast umum ke seluruh pengguna online.
  - `/list`: Perintah otomatis untuk mengambil daftar pengguna yang sedang online.

### 2. 👥 Sinkronisasi Pengguna Online Otomatis (`/list` Polling)
- **Automatic Polling**: Sistem ViewModel mengirimkan command `/list` secara berkala (tiap 8 detik) dan saat pengguna membuka layar *Kontak* untuk memperbarui status pengguna yang sedang aktif secara real-time.
- **Notifikasi Bergabung/Keluar**: Tangkapan pesan sistem saat ada pengguna lain yang masuk (`[Sistem] <nama> bergabung`) atau keluar dari server (`[Sistem] <nama> keluar`).

### 3. 🚀 Onboarding Langsung & Praktis (Tanpa Login)
- **Tanya Nama Pertama Kali**: Saat pertama kali aplikasi dibuka, Anda akan diminta memasukkan nama tampilan.
- **Tersimpan Otomatis di HP**: Nama Anda tersimpan secara aman di penyimpanan lokal HP (`SharedPreferences`).
- **Ubah Nama Kapan Saja**: Anda bisa memperbarui nama profil kapan saja melalui menu **Pengaturan**.

### 4. 🗄️ Penyimpanan Lokal Room Database (SQLite)
- **Persistensi Riwayat Pesan**: Semua pesan masuk dan keluar disimpan secara permanen di database lokal **Room** (`ChatDatabase`).
- **Offline History**: Pengguna tetap dapat membaca riwayat obrolan lama meskipun koneksi ke server Python terputus.

### 5. 💬 Ruang Obrolan Interaktif (`Chat Detail`)
- **Gelembung Pesan Ergonomis**: Warna gelembung pesan yang membedakan pesan masuk, keluar, dan privat.
- **Pesan Suara (Voice Note)**: Kirim dan putar pesan suara langsung dari gelembung obrolan.
- **Kirim Balasan (Reply)**: Balas pesan tertentu untuk menjaga konteks obrolan.
- **Status Pengiriman**: Indikator pesan terkirim dan dibaca.

### 6. 🎨 Tema Tampilan Slate & OLED
- **Dark Slate Mode**: Mode gelap elegan bernuansa batu slate yang tenang dan nyaman di mata.
- **OLED Pure Black**: Mode hitam pekat untuk efisiensi baterai pada layar AMOLED/OLED.
- **Light Mode**: Mode terang yang bersih dan tajam.

---

## 🛠️ Teknologi & Arsitektur
- **Bahasa Pemrograman**: Kotlin 100%
- **UI Framework**: Jetpack Compose dengan Material Design 3
- **Networking**: Raw Java/Kotlin Socket TCP Client (`ChatClient.kt`)
- **Database**: Room Database (SQLite) + KSP
- **Arsitektur**: MVVM (Model-View-ViewModel) + `StateFlow` + Coroutines
- **Penyimpanan Lokal**: Android `SharedPreferences` + Room DB

