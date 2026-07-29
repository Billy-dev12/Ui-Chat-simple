# Memory Bank - AuraChat Android App

## 📌 Project Overview
**AuraChat** adalah aplikasi obrolan pesan instan Android modern berdesain minimalis slate dan bernuansa eksklusif. Ditulis 100% menggunakan **Kotlin** dan **Jetpack Compose (Material 3)**.

---

## 🔄 Alur Transisi Workflow (Dari Python Terminal ke Android App)
Aplikasi ini berevolusi dari workflow obrolan interaktif Python terminal sederhana (di mana sistem menanyakan nama pengguna sebelum mulai mengobrol) menjadi **Aplikasi Android Asli**.

### 💡 Konsep Utama Onboarding & Penyimpanan Lokal
1. **Welcome Screen Pertama Kali (`WelcomeNameScreen`)**:
   - Saat aplikasi pertama kali diinstal/dibuka (`is_first_run == true`), pengguna langsung disambut dengan kartu input nama tanpa perlu login atau daftar akun.
   - Tanpa kata sandi, tanpa verifikasi email, dan tanpa server registrasi rumit.
2. **Penyimpanan Lokal Hemat & Aman (`SharedPreferences`)**:
   - Nama pengguna disimpan secara permanen di penyimpanan lokal HP menggunakan file preferences `aurachat_prefs` (key: `user_name` dan `is_first_run`).
   - Setiap kali aplikasi dibuka kembali, sistem membaca nama yang sudah tersimpan dan langsung membawa pengguna ke halaman utama chat (`ChatList`).
3. **Kustomisasi Nama Profil Kapan Saja**:
   - Pengguna dapat mengubah nama profil kapan saja melalui menu **Pengaturan (`SettingsScreen`)**.
   - Perubahan nama akan otomatis memperbarui avatar inisial, handle username (`@...`), serta nama pada obrolan dan status di seluruh aplikasi secara reaktif.

---

## 🏗️ Arsitektur & Struktur Kode

```text
app/src/main/java/com/example/
├── MainActivity.kt                      # Entry point aktivitas Android
├── model/                               # Data Models (User, ChatThread, Message, Story, dll.)
│   ├── ChatModels.kt
├── viewmodel/                           # State Management & Local Storage Logic
│   └── ChatViewModel.kt                 # AndroidViewModel mengelola SharedPreferences & StateFlow
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
│   │   ├── ChatListScreen.kt            # Daftar Obrolan, Filter, & Stories
│   │   ├── ChatDetailScreen.kt          # Detail Ruang Chat & Input Pesan
│   │   ├── ContactsScreen.kt            # Daftar Kontak Pengguna
│   │   └── SettingsScreen.kt            # Pengaturan Tema & Custom Nama Profil
│   └── navigation/
│       └── AuraChatApp.kt               # Scaffold Utama, Bottom Navigation, & Transitions
```

---

## 🛠️ Stack Teknologi
- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Arsitektur**: MVVM (Model-View-ViewModel) + `StateFlow`
- **Penyimpanan Lokal**: Android `SharedPreferences` (`aurachat_prefs`)
- **Desain & Tema**: Slate Minimalist Dark Mode, Pure OLED Mode, & Clean Light Mode
