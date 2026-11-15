# LEOVANISLAND — Dynamic Island Pintar untuk Android

**LEOVANISLAND** adalah aplikasi Android native yang menghadirkan pengalaman interaksi *Dynamic Island* bergaya iOS dengan desain **Full Black (`#000000`)** yang minimalis, elegan, dan responsif. Dikembangkan menggunakan **Kotlin** dan **Jetpack Compose** tanpa memerlukan akses root.

---

## Fitur Utama

1. **Dynamic Island Utama & Mode Split Pill**:
   - Mendukung mode **Compact (Ringkas)**, **Split Pill (2 aktivitas berdampingan seperti Musik + Timer)**, dan **Expanded (Kartu Interaktif Penuh)**.
   - Animasi **Spring (Pegas)**, **Morph (Morfosis)**, **Fade (Pudar)**, dan **Slide (Geser)** dengan durasi yang dapat diatur.
   - Interaksi sentuh lengkap: **Tap** untuk membuka/memperluas, **Long Press** untuk kontrol penuh, dan **Swipe Horizontal** untuk menyembunyikan kapsul secara manual (*Manual Hide*) di samping fitur *Auto-Hide*.
   - Berjalan di atas aplikasi lain melalui layanan resmi `WindowManager` (`TYPE_APPLICATION_OVERLAY`) tanpa menghalangi sentuhan layar di bawahnya (`FLAG_NOT_FOCUSABLE`).

2. **Notifikasi Pintar (`NotificationListenerService`)**:
   - Menampilkan ikon aplikasi, nama aplikasi, pengirim, isi pesan, waktu notifikasi, dan badge pengelompokan notifikasi.
   - Mendukung **Balasan Cepat (`RemoteInput`)** langsung dari dalam Dynamic Island untuk aplikasi pesan seperti WhatsApp, Telegram, Instagram, dan Pesan Android.
   - Filter per aplikasi, penyembunyian kata kunci tertentu, privasi layar kunci, dan riwayat notifikasi lokal (`Room Database`).

3. **Pemutar Musik Universal (`MediaSessionManager`)**:
   - Terhubung ke sesi media aktif (Spotify, YouTube Music, dan pemutar musik lainnya).
   - Menampilkan sampul album asli, judul lagu, nama artis, visualizer gelombang suara animasi, bilah progres interaktif (*seek bar*), serta tombol Play, Pause, Next, dan Previous.

4. **Fitur Sistem Android Real-Time**:
   - Pemantau status baterai, persentase, suhu, pengisian daya cepat, dan peringatan baterai penuh.
   - Utilitas **Timer** dan **Stopwatch** interaktif yang langsung berjalan di dalam Dynamic Island.
   - Indikator perubahan **Volume Media**, **Bluetooth**, **Wi-Fi**, **Hotspot Pribadi**, **Papan Klip (Clipboard)**, dan **Perekaman Layar (Screen Recording)**.

5. **Kompatibilitas Semua Jenis Layar**:
   - Menggunakan warna hitam solid `#000000` yang tampak konsisten di panel **AMOLED, OLED, IPS LCD, TFT LCD, PLS LCD, dan LTPS LCD**.
   - Mendukung preset posisi untuk layar **Punch-Hole Tengah**, **Punch-Hole Kiri/Kanan**, **Notch Lebar**, maupun **Layar Tanpa Notch**.

---

## Panduan Membuka Project di Android Studio

1. Buka **Android Studio** (disarankan versi Ladybug / Meerkat atau yang lebih baru).
2. Pilih **File > Open**, lalu arahkan ke direktori root project `LEOVANISLAND`.
3. Tunggu proses **Gradle Sync** hingga seluruh dependensi selesai diunduh secara otomatis.
4. Pastikan SDK Android (minimal API 24 hingga API 36) dan JDK 11+ telah terkonfigurasi di pengaturan Gradle.

---

## Panduan Build APK

### 1. Otomatis di GitHub (GitHub Actions — Tanpa Perlu Android Studio Lokal)
Project ini telah dilengkapi workflow otomatis **`.github/workflows/build-apk.yml`**:
1. Push atau ekspor project ini ke repositori **GitHub** Anda.
2. Buka tab **Actions** di repositori GitHub Anda.
3. Workflow **Build LEOVANISLAND APK** akan berjalan secara otomatis setiap kali ada *push*, atau Anda dapat menekan tombol **Run workflow** secara manual.
4. Setelah proses selesai (centang hijau), gulir ke bagian **Artifacts** di halaman ringkasan workflow dan unduh:
   - **`LEOVANISLAND-Debug-APK`** (`LEOVANISLAND-v1.0.0-debug.apk`)
   - **`LEOVANISLAND-Release-APK`** (`LEOVANISLAND-v1.0.0-release.apk`)

### 2. Melalui Menu Android Studio:
1. Klik menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Setelah proses kompilasi selesai, klik tautan **locate** pada notifikasi kanan bawah untuk menemukan file `app-debug.apk` di dalam folder `app/build/outputs/apk/debug/`.

### 3. Melalui Terminal / Command Line:
```bash
gradle assembleDebug
```
File APK siap pasang akan tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

---

## Panduan Instalasi

1. Salin file `app-debug.apk` ke perangkat Android Anda atau hubungkan perangkat menggunakan kabel USB dengan fitur *USB Debugging* aktif.
2. Buka file APK pada perangkat dan ketuk **Instal** (*Install*).
3. Aplikasi **LEOVANISLAND** tidak memerlukan akses root dan aman dipasang pada seluruh perangkat Android 7.0 (Nougat) ke atas.

---

## Panduan Mengaktifkan Izin

Agar Dynamic Island dapat muncul di atas aplikasi lain dan membaca notifikasi serta musik secara real-time, aktifkan dua izin resmi berikut melalui halaman **Beranda** atau **Privasi** di dalam aplikasi:

1. **Tampilkan di Atas Aplikasi Lain (`SYSTEM_ALERT_WINDOW`)**:
   - Ketuk tombol **Aktifkan** pada baris *Tampilkan di Atas Aplikasi Lain (Overlay)*.
   - Cari **LEOVANISLAND** pada daftar sistem Android, lalu nyalakan sakelar izinnya.
2. **Akses Notifikasi Pintar & Sesi Media (`NotificationListenerService`)**:
   - Ketuk tombol **Aktifkan** pada baris *Akses Notifikasi Pintar & Sesi Media*.
   - Pilih **LEOVANISLAND** dan ketuk **Izinkan** (*Allow*).

Anda dapat menonaktifkan layanan atau mencabut izin kapan saja melalui halaman **Privasi**.

---

## Panduan Menguji Fitur

1. **Menguji Melalui Simulator Interaktif Langsung**:
   - Buka halaman **Beranda**, lalu gunakan tombol pada bagian **Simulator & Uji Interaktif Langsung** (*Notifikasi WhatsApp*, *Putar Lagu Berikutnya*, *Split Pill Musik + Timer*, *Pengisian Daya 88%*, *Timer 3 Menit*, *Bluetooth Audio*, *Indikator Papan Klip*).
   - Ketuk kapsul Dynamic Island di bagian atas untuk memperluas ke mode **Expanded**, tekan tombol **Balas Cepat** pada notifikasi untuk mengetik balasan langsung, atau geser kapsul ke samping (*Swipe*) untuk menyembunyikannya.
2. **Menguji dengan Aplikasi Musik & Pesan Nyata**:
   - Pastikan kedua izin di atas telah aktif dan sakelar utama **LEOVANISLAND** menyala.
   - Putar lagu di **Spotify** atau **YouTube Music** — kapsul Dynamic Island akan otomatis menampilkan sampul album, visualizer gelombang suara, dan kontrol musik aktif.
   - Saat ada pesan masuk dari **WhatsApp**, **Telegram**, atau **Instagram**, kapsul akan beranimasi menampilkan pesan tersebut secara real-time.
