# MEDSEL Kids TV

Application Android TV pour Android 4.4 (API 19), sans dépendance à Google Play Services.

## Version du projet

- versionName : 0.2.0
- versionCode : 2
- Interface paysage pilotable à la télécommande.
- Catalogue de chaînes arabes pour enfants.
- Lecteur intégré ; aucun flux n'est déclaré opérationnel sans validation.
- Vérification manuelle des mises à jour depuis le bouton de l'application.
- Téléchargement d'un APK de GitHub Releases puis ouverture de l'installateur Android.

## Chaînes et disponibilité

Le catalogue contient Spacetoon, MBC 3, Majid, Al Majd Kids, Baraem, Jeem, Toyor Al Janah, Toyor Baby, Cartoon Network Arabic, Ajial, Karameesh, Rotana Kids et BabyBus Arabic.

Ces noms ne signifient pas que les directs sont disponibles dans l'application. Les flux doivent être officiels, gratuits, accessibles depuis l'Algérie et compatibles avec Android 4.4 avant d'être activés. Les pages officielles peuvent exiger un abonnement, une application propriétaire ou un lecteur DRM non pris en charge par VideoView.

## Publication des mises à jour

L'application consulte la dernière GitHub Release et cherche un fichier APK joint. Le tag de publication doit être un numéro de versionCode simple : v3, v4, v5, etc. (la version installée actuelle est 2).

Le workflow `.github/workflows/release.yml` construit et publie une APK signée lorsqu'un tag `v*` est poussé. Avant de l'utiliser, configurer ces secrets dans Settings > Secrets and variables > Actions :

- `ANDROID_KEYSTORE_BASE64` : keystore Android encodé en Base64.
- `ANDROID_KEYSTORE_PASSWORD` : mot de passe du keystore.
- `ANDROID_KEY_ALIAS` : alias de la clé.
- `ANDROID_KEY_PASSWORD` : mot de passe de la clé.

**La même clé de signature doit être conservée pour toutes les versions.** Ne jamais committer le keystore ni les mots de passe dans le dépôt. Android affiche encore sa confirmation système d'installation.

## Compilation de test

Le workflow `Build Android APK` produit une APK debug comme artefact GitHub Actions. Une APK debug de CI n'est pas destinée à servir de clé de signature stable pour les mises à jour.
