# Tables Express

Application pour apprendre les tables de multiplication de 0 à 11. L'enfant choisit son pseudo et ses tables, puis répond **à voix haute** en moins de 3 secondes :

| Temps de réponse | Points |
|---|---|
| moins d'1 seconde | 3 |
| moins de 2 secondes | 2 |
| moins de 3 secondes | 1 |
| erreur ou plus de 3 secondes | 0 |

Un mode clavier est prévu si le micro n'est pas disponible.

## Installer l'application Android

1. Ouvre l'onglet **Releases** du dépôt (colonne de droite sur la page d'accueil).
2. Télécharge `tables-express.apk` depuis le téléphone.
3. Ouvre le fichier et autorise l'installation d'applications depuis le navigateur si Android le demande.

## Version web

Elle est en ligne sur GitHub Pages : `https://etien278.github.io/tables-express/`. Dans Chrome, le bouton « Installer l'application » l'ajoute à l'écran d'accueil.

## Organisation

- `www/` : l'application (une seule page HTML, plus le manifeste, le service worker et les icônes)
- `android/` : le projet Android (Capacitor) qui emballe `www/` avec la reconnaissance vocale native du téléphone
- `.github/workflows/android.yml` : construit l'APK à chaque modification et le publie dans Releases
- `.github/workflows/pages.yml` : met la version web en ligne
