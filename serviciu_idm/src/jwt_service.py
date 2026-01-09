
#serviciu pentru gestionarea token-urilor JWT
#responsabilitati:
#1) generare token-uri JWT pentru utilizatori logati
#2) Validare token-uri JWT (verificare signature + expiration)
#3) Invalidare token-uri (logout) prin blacklist

import jwt
import uuid
from datetime import datetime, timedelta
from typing import Dict, Set
import os


class JWTService:


    def __init__(self):


        #load confing
        self.secret_key = os.getenv('JWT_SECRET_KEY', 'default-secret-key')
        self.algorithm = os.getenv('JWT_ALGORITHM', 'HS256')
        self.expiration_hours = int(os.getenv('JWT_EXPIRATION_HOURS', 1))

        #blacklist pentru token-uri invalidate
        self.blacklist: Set[str] = set()

        print(f" JWT Service init:")
        print(f"   - Algorithm: {self.algorithm}")
        print(f"   - Token expiration: {self.expiration_hours}h")

    def generate_token(self, user_id: int, role: str) -> str:

        def generate_token(self, user_id: int, role: str) -> str:
            #Genereaza token JWT
            now = datetime.utcnow()

            payload = {
                'iss': 'eventflow-idm-service',
                'sub': user_id,
                'exp': now + timedelta(hours=self.expiration_hours),
                'iat': now,
                'jti': str(uuid.uuid4()),
                'role': role
            }

            token = jwt.encode(payload, self.secret_key, algorithm=self.algorithm)
            print(f"Token generat pentru user_id={user_id}, role={role}")
            return token

        def validate_token(self, token: str) -> Dict[str, any]:
            #Valideaza token JWT
            try:
                if token in self.blacklist:
                    return {'valid': False, 'user_id': 0, 'role': '', 'message': 'Token invalidated'}

                payload = jwt.decode(token, self.secret_key, algorithms=[self.algorithm])

                return {
                    'valid': True,
                    'user_id': payload['sub'],
                    'role': payload['role'],
                    'message': 'Token is valid'
                }

            except jwt.ExpiredSignatureError:
                self.blacklist.add(token)
                return {'valid': False, 'user_id': 0, 'role': '', 'message': 'Token expired'}

            except jwt.InvalidSignatureError:
                self.blacklist.add(token)
                return {'valid': False, 'user_id': 0, 'role': '', 'message': 'Invalid signature'}

            except jwt.InvalidTokenError as e:
                self.blacklist.add(token)
                return {'valid': False, 'user_id': 0, 'role': '', 'message': f'Invalid token: {str(e)}'}

        def invalidate_token(self, token: str) -> Dict[str, any]:
            #Invalideaza token (logout)
            try:
                jwt.decode(token, self.secret_key, algorithms=[self.algorithm], options={'verify_exp': False})
                self.blacklist.add(token)
                return {'success': True, 'message': 'Token invalidated'}
            except jwt.InvalidTokenError:
                self.blacklist.add(token)
                return {'success': True, 'message': 'Invalid token added to blacklist'}