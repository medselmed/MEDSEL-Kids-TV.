# MEDSEL Kids TV

Application Android TV de chaînes jeunesse arabes en direct (HLS), conçue pour un téléviseur
**Iris sous Android 4.4.2 (API 19)**. Interface pilotée à la télécommande, sans écran tactile ni
Google Play Services.

## Fonctions

- Liste en deux sections : chaînes **en direct** et chaînes **sans direct public** (grisées, avec explication).
- Lecteur ExoPlayer 2.19.1 (HLS), reconnexion automatique (2 essais) puis **serveur de secours** si la chaîne en a un.
- Reprise automatique de la dernière chaîne regardée.
- Télécommande : `OK` lit la chaîne, un second `OK` passe en plein écran ; `CH+` / `CH−` (ou ▲ / ▼ en plein écran) changent de chaîne ; `RETOUR` revient à la liste.
- Compatibilité Android 4.4 : TLS 1.1/1.2 activés pour HTTPS, MultiDex, icône et bannière du lanceur Leanback.

## Catalogue

Règle : une URL n'est ajoutée que si elle est publiée par le diffuseur lui-même (son domaine ou son CDN).
Les relais non autorisés ne sont pas acceptés.

| Chaîne | État |
|---|---|
| Spacetoon, Atfal & Mawaheb, Koogi TV, Roya Kids, Roya Kids Originals, Amou Yazid, SAT-7 Kids, God Stands Kids Arabic | Flux de direct déclaré |
| Toyor Al Janah, Toyor Baby, MBC 3, Majid, Baraem, Jeem, Cartoon Network Arabic, Karameesh, Ajial, Al Majd Kids, Rotana Kids | Aucun flux public vérifié (non lisible) |

Aucune URL n'a pu être testée depuis un serveur : **chaque flux doit être validé sur le téléviseur**
(réseau algérien, Android 4.4.2). Un flux peut être géo-restreint ou changer d'adresse.
L'appartenance de chaque chaîne à Nilesat n'est pas encodée dans l'application.

Pour ajouter ou corriger une chaîne : modifier `app/src/main/java/com/medsel/kidstv/ChannelCatalog.java`.

## Limites connues sur Android 4.4

- TLS 1.2 est activé par `Tls12SocketFactory`, mais aucun certificat racine n'est ajouté : un serveur dont la
  chaîne dépend d'une autorité absente d'Android 4.4 (ex. certificats Let's Encrypt récents) peut échouer.
- Le flux Amou Yazid est en HTTP simple sur un serveur à DNS dynamique.
- Les mises à jour intégrées ont été retirées au commit `00c8d3f` (récupérables depuis `98f11f1`).
  L'installation d'une nouvelle version se fait à la main depuis les Releases GitHub.

## Compilation

- Prérequis : JDK 8, Android SDK (`platforms;android-31`, `build-tools;30.0.3`).
- Debug : `gradle assembleDebug`. Le workflow `android.yml` le fait à chaque push et publie l'APK en artefact.
- Release signée : pousser un tag `v*` ; le workflow `release.yml` signe l'APK (secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) et le publie.
- Ne jamais committer le keystore (`.gitignore` l'exclut).
