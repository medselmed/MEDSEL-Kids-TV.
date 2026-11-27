# MEDSEL Kids TV

Application Android TV ciblant Android 4.4 (API 19), sans Google Play Services.

## État de cette première version

- Écran TV paysage avec navigation à la télécommande.
- Catalogue initial de chaînes arabes pour enfants.
- Lecteur vidéo intégré (VideoView).
- Compilation APK debug via GitHub Actions.

**Important :** les chaînes affichées sont pour l'instant des entrées de catalogue, pas des directs opérationnels. Aucun flux n'a été intégré car je ne dispose pas encore d'adresses de diffusion gratuites, officielles et vérifiées pour ces chaînes. Ne pas présenter cette version comme un bouquet TV fonctionnel avant validation des flux.

## Cible technique

- minSdkVersion 19 (Android 4.4+)
- targetSdkVersion 28
- Android Gradle Plugin 3.6.4 / Gradle 5.6.4 / JDK 8

Le fichier APK debug est publié comme artefact du workflow `Build Android APK`.
