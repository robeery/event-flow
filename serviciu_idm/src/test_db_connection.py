"""
Script de test pentru conexiunea la baza de date
"""
from dotenv import load_dotenv
import os
import sys


load_dotenv()

print("=" * 60)
print("TEST CONEXIUNE BAZA DE DATE")
print("=" * 60)


print("\n📋 Configurare:")
print(f"   Host: {os.getenv('DB_HOST')}")
print(f"   Port: {os.getenv('DB_PORT')}")
print(f"   Database: {os.getenv('DB_NAME')}")
print(f"   User: {os.getenv('DB_USER')}")
print(f"   Password: {'*' * len(os.getenv('DB_PASSWORD', ''))}")


print("\nTestare conexiune...")

try:
    from models import DatabaseManager

    #construieste  connection string
    db_host = os.getenv('DB_HOST')
    db_port = os.getenv('DB_PORT')
    db_name = os.getenv('DB_NAME')
    db_user = os.getenv('DB_USER')
    db_password = os.getenv('DB_PASSWORD')

    connection_string = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"

    # creeaza db manager
    db = DatabaseManager(connection_string)
    print("Conexiune la MySQL reușită!")

    # Creează tabelele
    print("\nCreare tabele...")
    db.create_tables()
    print("Tabelul 'users' creat cu succes!")

    # Verifica tabelul
    print("\n📊 Verificare structură tabel...")
    session = db.get_session()

    # Import pentru SQL raw (necesar în SQLAlchemy 2.0)
    from sqlalchemy import text

    # Query pentru a vedea structura tabelului
    result = session.execute(text("DESCRIBE users"))
    print("\nStructura tabelului 'users':")
    print("-" * 60)
    for row in result:
        print(f"   {row[0]:20} {row[1]:20} {row[2]}")

    session.close()
    print("\n" + "=" * 60)
    print("✅ TOATE TESTELE AU TRECUT CU SUCCES!")
    print("=" * 60)

except Exception as e:
    print(f"\nEROARE: {e}")
    print("\nVerifica:")
    print("   1. MySQL server-ul ruleaza")
    print("   2. Credentialele din .env sunt corecte")
    print("   3. Baza de date 'eventflow_idm' exista")
    print("   4. Utilizatorul 'idm_user' are permisiuni")
    sys.exit(1)