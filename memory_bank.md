# Memory Bank - AuraChat Android App

## 📌 Project Overview
**AuraChat** adalah aplikasi obrolan pesan instan Android modern berdesain minimalis slate yang terhubung secara real-time ke **Python Socket TCP Server** (`server.py` port 9090). Ditulis 100% menggunakan **Kotlin**, **Jetpack Compose (Material 3)**, dan **Room Database**.

---

## 🔄 Alur Integrasi Python Socket Server & Persistence

1. **Auto Server Connection (`ChatClient.kt`)**:
   - `savedIp` dan `savedPort` disimpan di `SharedPreferences` (`aurachat_prefs`).
   - Saat aplikasi dibuka (`init` di `ChatViewModel`), jika IP server tersimpan, aplikasi langsung berpindah ke `ChatList` dan secara otomatis menghubungkan socket ke server Python di latar belakang.
   - Apabila koneksi gagal atau terputus, muncul banner interaktif di layar utama `ChatListScreen` yang memberi tahu pengguna lengkap dengan tombol *Coba Lagi* dan *Ganti IP Server*.

2. **Sinkronisasi Pengguna Online (`/list` Polling & Events)**:
   - ViewModel menjalankan coroutine polling (setiap 8 detik) yang mengirim perintah `/list` ke server Python.
   - Parsing respons server `[Sistem] User yang online: ...` memperbarui daftar `onlineUsers` secara reaktif.
   - Notifikasi `[Sistem] ... bergabung ke dalam obrolan` dan `[Sistem] ... keluar dari obrolan` langsung memperbarui daftar pengguna online.
   - Layar *ContactsScreen* otomatis memicu `queryOnlineUsers()` begitu dibuka untuk memastikan daftar kontak online selalu segar.

3. **Format Protokol Pesan Python (`server.py`)**:
   - **Pesan Broadcast**: Ditampilkan dari format `[Nama]: pesan`.
   - **Pesan Private**: Menggunakan syntax `@nama_tujuan pesan`, dikirim server dalam format `[Private dari Nama]: pesan`.
   - **Command Sistem**: `/list` untuk daftar online.

4. **Penyimpanan Lokal Room Database**:
   - `ChatDatabase.kt`, `ChatThreadDao.kt`, `MessageDao.kt`, `ChatThreadEntity.kt`, `MessageEntity.kt`.
   - Semua pesan dan obrolan tersimpan secara permanen di database SQLite lokal sehingga riwayat chat dapat dibaca kapan saja meski dalam keadaan offline.

---

## 🏗️ Arsitektur & Struktur Kode

```text
app/src/main/java/com/example/
├── MainActivity.kt                      # Entry point aktivitas Android
├── model/                               # Data Models (User, ChatThread, Message, Story, dll.)
│   └── ChatModels.kt
├── data/                                # Room Database Engine (SQLite)
│   ├── ChatDatabase.kt                  # Room Database Instance
│   ├── ChatThreadEntity.kt              # Entitas Tabel Thread Obrolan
│   ├── MessageEntity.kt                 # Entitas Tabel Pesan
│   ├── ChatThreadDao.kt                 # Data Access Object untuk Thread
│   └── MessageDao.kt                    # Data Access Object untuk Pesan
├── network/                             # Socket Networking Engine
│   └── ChatClient.kt                    # Client Socket TCP Kotlin
├── viewmodel/                           # State Management & Controller Logic
│   └── ChatViewModel.kt                 # ViewModel utama mengelola Socket, Room DB, & StateFlow
├── ui/
│   ├── theme/                           # System Theme & Color Palette
│   │   ├── Color.kt                     # Minimal Dark Slate & OLED Theme Colors
│   │   ├── Theme.kt                     # AuraChatTheme Wrapper
│   │   └── Type.kt                      # Typography
│   ├── components/                      # Reusable Components
│   │   ├── Avatar.kt                    # User Avatar dengan Inisial & Status Online
│   │   └── ChatBubbles.kt               # Gelembung Pesan Text & Voice Note
│   ├── screens/                         # Composables Layar Utama
│   │   ├── WelcomeNameScreen.kt         # Layar Sambutan & Input Nama Pertama Kali
│   │   ├── ConnectScreen.kt             # Form Input IP & Port Server Python
│   │   ├── ChatListScreen.kt            # Daftar Obrolan, Banner Koneksi, & Stories
│   │   ├── ChatDetailScreen.kt          # Detail Ruang Chat, Voice Note, & Input Pesan
│   │   ├── ContactsScreen.kt            # Daftar Kontak Pengguna Online (Real-time `/list`)
│   │   └── SettingsScreen.kt            # Pengaturan Tema, Server IP, & Custom Nama Profil
│   └── navigation/
│       └── AuraChatApp.kt               # Scaffold Utama, Bottom Navigation, & Transitions
```

---

## 🛠️ Stack Teknologi
- **Bahasa**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3)
- **Networking**: Raw Java Socket (`java.net.Socket`)
- **Local Persistence**: Room Database (SQLite) + Android `SharedPreferences`
- **Arsitektur**: MVVM (Model-View-ViewModel) + `StateFlow` + Kotlin Coroutines
- **Desain & Tema**: Slate Minimalist Dark Mode, Pure OLED Mode, & Clean Light Mode

