import mysql.connector
from mysql.connector import Error

# Configuration de la connexion
HOST = "srv-bdens.insa-toulouse.fr"
PORT = 3306
USER = "projet_gei_039"
PASSWORD = "auteD5ro"
DATABASE = "projet_gei_039"

# Script SQL pour initialiser la base
INITIALISATION_SQL = """
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Surname VARCHAR(100) NOT NULL,
    Email VARCHAR(255) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Birthdate VARCHAR(100) NOT NULL,
    Type ENUM('Helper', 'Client') NOT NULL,
    Sex ENUM('M', 'F', 'Autre') NOT NULL
);

-- Suppression et création de la table Demande
DROP TABLE IF EXISTS Demands;

CREATE TABLE Demands (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    User_ID INT NOT NULL,
    Helper_ID INT,
    Creation_date DATETIME NOT NULL,
    Name VARCHAR(100) NOT NULL,
    Description TEXT,
    State ENUM('Waiting', 'Validated', 'Ongoing', 'Done') NOT NULL DEFAULT 'Waiting',
    Priority ENUM('Low', 'Mid', 'High') NOT NULL DEFAULT 'Mid',
    FOREIGN KEY (User_ID) REFERENCES Users(ID) ON DELETE CASCADE
);

-- Suppression et création de la table Feedbacks
DROP TABLE IF EXISTS Feedbacks;

CREATE TABLE Feedbacks (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Demand_ID INT NOT NULL,
    User_ID INT NOT NULL,
    Content TEXT,
    Note INT DEFAULT 3 CHECK (Note >= 0 AND Note <= 5),
    Creation_date DATETIME NOT NULL,
    FOREIGN KEY (Demand_ID) REFERENCES Demands(ID) ON DELETE CASCADE,
    FOREIGN KEY (User_ID) REFERENCES Users(ID) ON DELETE CASCADE
);
"""

try:
    # Connexion à la base de données
    connection = mysql.connector.connect(
        host=HOST,
        port=PORT,
        user=USER,
        password=PASSWORD,
        database=DATABASE
    )

    if connection.is_connected():
        print("Connexion réussie à la base de données")

        # Création du curseur
        cursor = connection.cursor()

        # Exécution du script d'initialisation
        cursor.execute(INITIALISATION_SQL)
        print("Table créée avec succès")

except Error as e:
    print(f"Erreur lors de la connexion à la base de données: {e}")
finally:
    if connection.is_connected():
        cursor.close()
        connection.close()
        print("Connexion fermée")
