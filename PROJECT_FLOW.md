# SambungKata Plugin - Project Flow Doc

## Ringkasan
Plugin ini adalah minigame sambung kata di Minecraft (Paper API). Moderator mendaftarkan pemain, memilih mode kata, lalu memulai permainan. Sistem membangun arena, melakukan gacha urutan pemain, lalu menjalankan ronde berbasis giliran. Jawaban dinilai dari daftar kata di `words.yml`.

## Struktur Utama
- Entry point: `src/main/java/yt/corazonid/sambungKata/SambungKata.java`
- Command handler: `src/main/java/yt/corazonid/sambungKata/command/ModeratorCommand.java`
- Core logic: `src/main/java/yt/corazonid/sambungKata/manager/GameManager.java`
- Arena builder: `src/main/java/yt/corazonid/sambungKata/manager/BuildManager.java`
- Word dictionary: `src/main/java/yt/corazonid/sambungKata/manager/WordManager.java`
- Player model: `src/main/java/yt/corazonid/sambungKata/model/GamePlayer.java`
- Listeners:
  - Chat: `src/main/java/yt/corazonid/sambungKata/listener/ChatListener.java`
  - Protection: `src/main/java/yt/corazonid/sambungKata/listener/ProtectionListener.java`
- Utils: `src/main/java/yt/corazonid/sambungKata/util/*`
- Config:
  - Commands: `src/main/resources/plugin.yml`
  - Words: `src/main/resources/words.yml`

## Game State
Enum `GameState`:
- `IDLE`: belum ada game
- `PLAYING`: sedang berjalan
- `ROUND_END`: ronde selesai, tunggu `/nextround` atau `/endgame`

## Game Mode
Enum `GameMode`:
- `SINGLE`: hanya satu kata
- `MULTI`: boleh lebih dari satu kata

## Flow High Level
1) Moderator daftarkan pemain
2) Moderator pilih mode
3) Moderator jalankan `/start`
4) Build arena + gacha urutan
5) Ronde berjalan (giliran per pemain)
6) Jawaban benar -> lanjut giliran berikut
7) Jawaban salah / timeout -> kehilangan heart
8) Jika tersisa 1 pemain hidup -> ronde selesai
9) Moderator pilih `/nextround` atau `/endgame`

## Flow Detail - Startup
Entry: `SambungKata#onEnable`
- Inisialisasi `WordManager`, `BuildManager`, `GameManager`
- Register command:
  - `regis`, `unregis`, `listplayer`, `listscore`, `mode`, `start`, `nextround`, `endgame`, `resetgame`, `skip`, `commandinfo`
- Register listener:
  - `ChatListener` untuk jawaban
  - `ProtectionListener` untuk proteksi entity/gerak/inventory

## Flow Detail - Registrasi
Lokasi: `GameManager#registerPlayerByName`
- Cek state bukan `PLAYING`
- Validasi nama
- Maks 8 pemain
- Cek online
- Simpan ke `registeredPlayers` sebagai `GamePlayer`

Unregister: `GameManager#unregisterPlayer`
- Hapus dari `registeredPlayers`

## Flow Detail - Start Game
Entry: `GameManager#startGame`
- Hanya boleh saat `IDLE`
- Minimal 2 pemain, maksimal 8
- Mode harus sudah dipilih
- Lanjut ke `beginRoundSession`

`beginRoundSession`:
- Reset state, queue, usedWords, counter
- Apply efek ke player (survival, invis, health sesuai heart)
- Build arena `BuildManager#buildArena`
- Play sound start
- Mulai gacha `startGacha`

## Flow Detail - Gacha Urutan
Lokasi: `GameManager#startGacha`
- Random urutan 1..N untuk `registeredPlayers`
- Animasi title selama 20 frame
- Setelah selesai:
  - `setupPlayOrder` (urut berdasarkan order)
  - `spawnMannequins` (ArmorStand + Mannequin)
  - `setSpawnpoints`
  - `startRound`

## Flow Detail - Start Round
Lokasi: `GameManager#startRound`
- `roundNumber++`
- Reset hint dan answeredThisRound
- Isi `answerQueue` dengan pemain hidup
- `nextTurn`

## Flow Detail - Next Turn
Lokasi: `GameManager#nextTurn`
- Stop timer, action bar, kamera
- Ambil player hidup berikut dari `answerQueue`
- Jika hidup tinggal 1 -> `endRound`
- Jika queue habis -> `startRound` ulang
- Set `currentPlayer` dan `currentChances = 5`
- Hitung hint `determineHint`
- Show hint dengan `TextDisplay`
- Broadcast giliran
- Beri wool indikator kesempatan
- Kamera snap/animate ke viewer
- Start timer 20 detik

## Flow Detail - Chat Answer
Listener: `ChatListener#onChat`
- Hanya saat `PLAYING`
- Hanya player terdaftar dan hidup
- Hanya jika `currentPlayer`
- Ambil teks plain
- Cancel chat
- Jalankan sync task -> `GameManager#handleChatAnswer`

`handleChatAnswer`:
- Validasi kata via `WordManager#getRejectReason`
- Jika reject:
  - `currentChances--`
  - Update wool
  - Broadcast salah
  - Jika `currentChances <= 0` -> `handleOutOfChances`
- Jika accept:
  - Simpan `usedWords`
  - `lastCorrectAnswer` + `chainFromLastCorrectAnswer = true`
  - Broadcast benar dan nama giliran berikut
  - Delay 40 tick -> `nextTurn`

## Flow Detail - Timeout
`startAnswerTimer`:
- 20 detik hitung mundur, update action bar
- Detik 5..1 bunyi countdown
- Saat 0 -> `handleTimeout` -> `eliminateOneHeart`

## Flow Detail - Kehilangan Heart
`eliminateOneHeart`:
- `hearts--`
- Jika `hearts <= 0`:
  - Player eliminated
  - Remove mannequin dan text display
  - Restore state pemain
  - Jika tinggal 1 -> `endRound`
  - Else -> delay 40 tick -> `nextTurn`
- Jika masih hidup -> delay 40 tick -> `nextTurn`

## Flow Detail - End Round
Lokasi: `GameManager#endRound`
- Stop timer/camera/action bar
- Cari winner (pemain hidup terakhir)
- Update skor berdasarkan urutan mati
- Broadcast winner
- Clear mannequin dan entity arena
- Restore state semua player
- `state = ROUND_END`

## Flow Detail - Next Round
Lokasi: `GameManager#nextRound`
- Boleh saat `ROUND_END`
- Validasi mode dan arena
- Reset data round
- Rebuild arena di lokasi start sebelumnya
- Apply efek ulang
- `state = PLAYING` dan `startGacha`

## Flow Detail - End Game
Lokasi: `GameManager#endGame`
- Stop semua task
- Broadcast skor final
- Restore block arena
- Remove entity arena
- Restore state player
- Reset semua skor dan data
- `state = IDLE`

## Flow Detail - Reset Game
Lokasi: `GameManager#resetGame`
- Hanya saat `PLAYING` dan bukan saat gacha
- Stop semua task
- Reset skor dan data round
- Remove mannequin dan entity arena
- Restore state player
- `state = ROUND_END`

## Arena Build Flow
Lokasi: `BuildManager`
- `buildArena`: simpan blok lama, clear area, set lantai, set meja, pasang kursi (slab)
- Chair location ditentukan oleh jumlah pemain (2..8)
- `restoreBlocks`: kembalikan blok lama dari snapshot

Catatan penting:
- Build menggunakan `setBlock` yang menyimpan snapshot sebelum mengganti blok
- `clearExtendedArea` juga menghapus block dekorasi agar arena tidak pecah oleh gravity

## Camera Flow
Lokasi: `GameManager`
- `snapCameraTo` untuk perpindahan pertama
- `animateCameraTo` untuk perpindahan berikutnya
- `startCameraLock` akan teleport semua pemain ke viewer tiap 2 tick dan lock slot item 0

## Proteksi Interaksi
Listener: `ProtectionListener`
- Cegah damage ke mannequin
- Cegah player bergerak saat `PLAYING`
- Cegah drop/interact/pindah inventory untuk wool
- Cegah player mati saat `PLAYING`

## Word System
Lokasi: `WordManager`
- Load dari `plugins/SambungKata/words.yml`
- Auto replace jika format rusak / terlalu sedikit kata
- Normalisasi:
  - lowercase
  - trim spasi
  - collapse spasi ganda
- Validasi jawaban:
  - Tidak boleh kosong
  - Tidak boleh sudah dipakai
  - Prefix harus cocok dengan hint
  - Jika mode `SINGLE`, jawaban tidak boleh ada spasi

Hint:
- Jika sebelumnya ada jawaban benar, hint pakai huruf akhir kata terakhir
- Jika tidak ada kata yang cocok -> pilih prefix random dari kata yang tersedia

## Data dan Parameter Penting
- `currentChances` default 5
- Timer per giliran: 20 detik
- Heart per pemain: 2 (di `GamePlayer#resetForNewRound`)
- Maks player: 8

## File Konfigurasi
`plugin.yml`
- Daftar command untuk moderator
- Permission default: OP

`words.yml`
- `single`: daftar kata single
- `multi`: daftar kata multi

## Titik Perubahan yang Sering Diedit
- Aturan win/score: `GameManager#endRound`
- Durasi timer: `GameManager#startAnswerTimer`
- Jumlah kesempatan: `GameManager#nextTurn` (currentChances)
- Heart per pemain: `GamePlayer#resetForNewRound`
- Logika hint: `WordManager#getHintFromAnswer` dan `getRandomStartingLetter`
- Layout arena: `BuildManager#getChairOffsets`

## Diagram Alur (ASCII)
```
[IDLE]
  | /start + mode + >=2 player
  v
[PLAYING]
  | build arena + gacha
  v
[ROUND LOOP]
  | nextTurn -> player answer
  | correct -> nextTurn
  | wrong/timeout -> heart--
  | if alive <= 1 -> endRound
  v
[ROUND_END]
  | /nextround -> PLAYING
  | /endgame -> IDLE
```

## Checklist Saat Debug Flow
- Pastikan `state` berubah sesuai fase
- Cek `currentPlayer` tidak null saat `PLAYING`
- Pastikan `answerQueue` terisi setelah `startRound`
- Lihat `usedWords` tidak kosong setelah jawaban benar
- Pastikan `TextDisplay` dibersihkan saat ronde selesai
- Cek `BuildManager#restoreBlocks` dipanggil saat end
