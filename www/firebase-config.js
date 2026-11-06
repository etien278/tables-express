// Configuration du projet Firebase de Tables Express (copains et classement).
// Ces valeurs ne sont pas secrètes : la sécurité est assurée par les règles Firestore (firestore.rules).
// Laisser à null tant que le projet Firebase n'est pas créé : l'appli marche alors sans la partie en ligne.
window.TX_FIREBASE = null;

// Bandeau de publicité (application Android seulement).
// Identifiant de bandeau de test fourni par Google : à remplacer par celui de ton compte AdMob,
// puis passer testing à false.
window.TX_ADS = { bannerId: "ca-app-pub-3940256099942544/6300978111", testing: true };
