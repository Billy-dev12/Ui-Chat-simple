# 💬 AuraChat - Minimalist Messaging App for Android

**AuraChat** adalah aplikasi obrolan pesan instan untuk Android yang dirancang dengan estetika minimalis, cepat, dan nyaman di mata. Aplikasi ini tidak memerlukan registrasi akun atau kata sandi—cukup masukkan nama Anda saat pertama kali membuka aplikasi dan Anda siap mengobrol!

---

## 🌟 Fitur Utama

### 1. 🚀 Onboarding Langsung & Praktis (Tanpa Login)
- **Tanya Nama Pertama Kali**: Saat pertama kali aplikasi dibuka, Anda akan diminta memasukkan nama tampilan.
- **Tersimpan Otomatis di HP**: Nama Anda tersimpan secara aman di penyimpanan lokal HP (`SharedPreferences`). Saat membuka aplikasi kembali, Anda tidak perlu memasukkan nama lagi.
- **Ubah Nama Kapan Saja**: Anda bisa memperbarui nama profil kapan saja melalui menu **Pengaturan**.

### 2. 📱 Layar Utama Obrolan (`Chat List`)
- **Pencarian Cepat**: Cari obrolan berdasarkan nama kontak atau isi pesan.
- **Filter Pesan**: Kelompokkan obrolan dengan chip filter (*Semua*, *Belum Dibaca*, *Grup*).
- **Status & Stories**: Lihat pembaruan status cerita ringkas dari teman-teman Anda.
- **Indikator Pesan Baru**: Badge jumlah pesan belum dibaca yang jelas dan rapi.

### 3. 💬 Ruang Obrolan Interaktif (`Chat Detail`)
- **Gelembung Pesan Ergonomis**: Warna gelembung pesan yang membedakan pesan masuk dan keluar dengan kontras tinggi namun tidak melelahkan mata.
- **Pesan Suara (Voice Note)**: Kirim dan putar pesan suara langsung dari gelembung obrolan.
- **Kirim Balasan (Reply)**: Balas pesan tertentu untuk menjaga konteks obrolan.
- **Status Pengiriman**: Tanda centang indikator pesan terkirim, terkirim ke perangkat, dan telah dibaca (centang biru).

### 4. 👥 Daftar Kontak (`Contacts`)
- Cari teman di kontak Anda dan mulai obrolan baru dalam satu sentuhan.

### 5. 🎨 Tema Tampilan Slate & OLED
- **Dark Slate Mode**: Mode gelap elegan bernuansa batu slate yang tenang dan nyaman di mata.
- **OLED Pure Black**: Mode hitam pekat untuk efisiensi baterai pada layar AMOLED/OLED.
- **Light Mode**: Mode terang yang bersih dan tajam.

---

## 🛠️ Teknologi & Arsitektur
- **Bahasa Pemrograman**: Kotlin 100%
- **UI Framework**: Jetpack Compose dengan Material Design 3
- **Arsitektur**: MVVM (Model-View-ViewModel) + `StateFlow`
- **Penyimpanan Lokal**: Android `SharedPreferences`
