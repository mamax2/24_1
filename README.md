# 24+1 - App di Produttività e Tracking Attività

<div align="center">
  <img src="https://img.shields.io/badge/Piattaforma-Android-green.svg" alt="Piattaforma">
  <img src="https://img.shields.io/badge/Linguaggio-Kotlin-blue.svg" alt="Linguaggio">
</div>

## 📱 Descrizione

**24+1** è una moderna app Android di produttività sviluppata con Jetpack Compose che aiuta gli utenti a tracciare le attività quotidiane, mantenere streak di produttività e guadagnare achievement. L'app combina suggerimenti di attività basati sulla posizione con elementi di gamification per incoraggiare abitudini di produttività costanti.

## 📋 Requisiti

- **Android 8.0 (API level 26)** o superiore
- **Kotlin 2.0.0**
- **Target SDK 35**

## 🚀 Installazione

1. **Clona il repository**
   ```bash
   git clone https://github.com/mamax2/24-1.git
   cd 24-1
   ```

2. **Configura Firebase**
   - Crea un nuovo progetto Firebase su [Firebase Console](https://console.firebase.google.com/)
   - Abilita Authentication con Email/Password
   - Scarica `google-services.json` e inseriscilo nella directory `app/`

3. **Build e esegui**
   ```bash
   ./gradlew assembleDebug
   ```

## 📁 Struttura Progetto

```
app/src/main/java/com/example/a24/
├── data/                          # Layer dati
│   ├── entities/                  # Entità Room
│   ├── dao/                       # Data Access Objects
│   ├── database/                  # Setup database
│   └── Repository.kt              # Repository dati
├── ui/
│   ├── screens/                   # Schermate app
│   │   ├── HomeScreen.kt          # Feed attività principale
│   │   ├── LoginScreen.kt         # Autenticazione
│   │   ├── ProfileScreen.kt       # Profilo utente e badge
│   │   └── NotificationScreen.kt  # Centro notifiche
│   ├── composables/               # Componenti UI riusabili
│   │   ├── AppBar.kt              # Barra navigazione superiore
│   │   ├── BadgeCard.kt           # Badge achievement
│   │   └── NotificationCard.kt    # Elementi notifica
│   └── theme/                     # Temi app
├── managers/                      # Logica business
│   └── NotificationManager.kt     # Gestione notifiche
└── MainActivity.kt                # Punto di ingresso app
```

## 🎨 Sistema Design

- **Linee guida Material Design 3**
- **Palette colori personalizzata** con supporto tema chiaro/scuro
- **Scala tipografica** usando Google Fonts (Roboto + Anton)

## 📊 Schema Database

### **Entità Utente**
- ID utente, nome, email
- URL immagine profilo
- Streak attività e statistiche
- Collezione badge

### **Entità Attività**
- Dettagli attività e metadati
- Status completamento e timestamp
- Associazione utente e categorizzazione

### **Entità Notifica**
- Contenuto notifica e tipo
- Status lettura e timestamp
- Filtraggio specifico utente



## 👨‍💻 Autore

**Sohail Mama**
- GitHub: [@mamax2](https://github.com/mamax2)
- Corso: Programmazione di Sistemi Mobile - AA 2024/2025

## 🎓 Note Accademiche

Questo progetto è stato sviluppato come progetto finale per il corso di **Programmazione di Sistemi Mobile**. 

<div align="center">
  <p>⭐ Metti una stella a questo repo se ti è stato utile!</p>
  <p><strong>Progetto Universitario - Programmazione di Sistemi Mobile 2024/2025</strong></p>
</div>
