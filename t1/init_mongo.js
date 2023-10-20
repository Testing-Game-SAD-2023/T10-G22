// Script di inizializzazione del database MongoDB

// Creazione delle collezioni
db.createCollection("ClassUT");
db.createCollection("interaction");
db.createCollection("Admin");
db.createCollection("Operation");

// Creazione degli indici
db.ClassUT.createIndex({ difficulty: 1 });
db.interaction.createIndex({ name: "text", type: 1 });
db.interaction.createIndex({ name: "text" });
db.Admin.createIndex({ username: 1 });

// Esecuzione di una query per trovare un documento nell'Admin
db.Admin.find({ username: "manvsclass" });
