# Memory Bank - AuraChat Android App

## 📌 Project Overview
**AuraChat** adalah aplikasi obrolan pesan instan Android modern berdesain minimalis slate yang terhubung secara real-time ke **Python Socket TCP Server** (`server.py` port 9090). Ditulis 100% menggunakan **Kotlin**, **Jetpack Compose (Material 3)**, dan **Room Database**.

---

## 🔄 Alur Integrasi Python Socket Server & Persistence

1. **Koneksi ke Server (`ChatClient.kt`)**:
   - User masukkan IP server, port, dan nama di `ConnectScreen`.
   - IP dan port disimpan di `SharedPreferences` (`aurachat_prefs`).
   - `ChatClient.kt` buka `java.net.Socket` ke Python server, kirim nama user.
   - Setelah terkoneksi, server otomatis kirim daftar online ke client baru.

2. **Synchronisasi User Online**:
   - Server **otomatis kirim** `[Sistem] User yang online: ...` saat:
     - User baru join (setelah welcome message)
     - User lain join/leave (broadcast ke semua client)
   - Android app **parse** format tersebut dan update `_onlineUsers` StateFlow.
   - User bisa manual refresh via tombol `/list` di UI.

3. **Format Protokol Server → Android**:
   | Server Output | Android Action |
   |---------------|----------------|
   | `[Sistem] Selamat datang, Nama!` | Welcome message, tidak perlu action |
   | `[Sistem] User yang online: A, B, C` | Update `_onlineUsers` StateFlow |
   | `[Sistem] Nama bergabung.` | Tambah ke onlineUsers, query `/list` |
   | `[Sistem] Nama keluar.` | Hapus dari onlineUsers, query `/list` |
   | `[Private dari Nama]: pesan` | Buat/update ChatThread + Message |
   | `[Nama]: pesan` | Buat/update ChatThread + Message |

4. **Kirim Pesan dari Android**:
   - Private: `@NamaPenerima pesan`
   - Broadcast: `pesan langsung`
   - Query online: `/list`

5. **Penyimpanan Lokal Room Database**:
   - `ChatDatabase.kt`, `ChatThreadDao.kt`, `MessageDao.kt`
   - Semua pesan tersimpan di SQLite lokal.

---

## 🏗️ Arsitektur & Struktur Kode

```text
app/src/main/java/com/example/
├── MainActivity.kt
├── model/
│   └── ChatModels.kt              # User, Message, ChatThread, etc.
├── data/
│   ├── ChatDatabase.kt            # Room Database Singleton
│   ├── ChatThreadEntity.kt        # Entity Thread
│   ├── MessageEntity.kt           # Entity Pesan
│   ├── ChatThreadDao.kt           # DAO Thread
│   └── MessageDao.kt              # DAO Pesan
├── network/
│   └── ChatClient.kt              # TCP Socket Client
├── viewmodel/
│   └── ChatViewModel.kt           # MVVM ViewModel
├── ui/
│   ├── theme/
│   ├── components/
│   │   ├── Avatar.kt
│   │   └── ChatBubbles.kt
│   ├── screens/
│   │   ├── WelcomeNameScreen.kt
│   │   ├── ConnectScreen.kt       # Input IP + Port + Nama
│   │   ├── ChatListScreen.kt      # Daftar Chat + Online Users
│   │   ├── ChatDetailScreen.kt    # Room Chat
│   │   ├── ContactsScreen.kt      # User Online
│   │   └── SettingsScreen.kt      # Tema + Disconnect
│   └── navigation/
│       └── AuraChatApp.kt
```

---

## 🛠️ Stack Teknologi
- **Bahasa**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3)
- **Networking**: Raw Java Socket (`java.net.Socket`)
- **Local Persistence**: Room Database (SQLite) + SharedPreferences
- **Arsitektur**: MVVM + StateFlow + Kotlin Coroutines
