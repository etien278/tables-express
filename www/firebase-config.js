// Configuration du projet Firebase de Tables Express (copains et classement).
// Ces valeurs ne sont pas secrètes : la sécurité est assurée par les règles Firestore (firestore.rules).
window.TX_FIREBASE = {
  apiKey: "AIzaSyAMQj_77CKhlDiAdMiKhwic_BWWO_dh5no",
  authDomain: "tables-express.firebaseapp.com",
  projectId: "tables-express",
  storageBucket: "tables-express.firebasestorage.app",
  messagingSenderId: "622612332959",
  appId: "1:622612332959:web:59f90a1b05b82c7beefdaa"
};

// Bandeau de publicité (application Android seulement).
// Bloc d'annonces « bandeau-bas » du compte AdMob.
// Pub coupée pendant le test fermé. Pour la remettre, enlever « null && » ci-dessous.
window.TX_ADS = null && { bannerId: "ca-app-pub-5171289248104054/2354951500", testing: false };
