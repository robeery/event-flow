
#Modele de date pentru serviciul IDM

from sqlalchemy import Column, Integer, String, Enum, create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import enum
import bcrypt


Base = declarative_base()


class UserRole(enum.Enum):
    #Enum pentru rolurile utilizatorilor
    ADMIN = "admin"
    OWNER_EVENT = "owner-event"
    CLIENT = "client"


class User(Base):

    #Entitatea User - utilizatorii aplicației


    __tablename__ = 'users'  # numele tabelului in DB

    #coloane
    id = Column(Integer, primary_key=True, autoincrement=True)
    email = Column(String(255), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    role = Column(Enum(UserRole), nullable=False)


    def set_password(self, password: str):

        #hash-uieste si salveaza parola

        #salt (valoare random pentru securitate)
        salt = bcrypt.gensalt()

        # hash-uieste parola cu bcrypt
        # bcrypt.gensalt() = genereazaa salt
        # bcrypt.hashpw() = hash-uieste cu salt
        self.password_hash = bcrypt.hashpw(
            password.encode('utf-8'),
            salt
        ).decode('utf-8')

    def check_password(self, password: str) -> bool:

        return bcrypt.checkpw(
            password.encode('utf-8'),
            self.password_hash.encode('utf-8')
        )

    def __repr__(self):
        #reprezentare string
        return f"<User(id={self.id}, email='{self.email}', role='{self.role.value}')>"


class DatabaseManager:
    def __init__(self, connection_string: str):

        #Inițializează conexiunea la baza de date
        """
        Args:
            connection_string: String-ul de conexiune
                Format: mysql+pymysql://user:password@host:port/database

        """
        #conex db
        self.engine = create_engine(
            connection_string,
            echo=False  # echo=True pentru debugging in caz de
        )


        self.SessionLocal = sessionmaker(bind=self.engine)

    def create_tables(self):
        Base.metadata.create_all(self.engine)

    def get_session(self):
        return self.SessionLocal()