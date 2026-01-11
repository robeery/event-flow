
#Implementarea serverului gRPC pentru Identity Management

import grpc
from concurrent import futures
import os
from dotenv import load_dotenv

# Import-uri pentru modelele generate din proto
import idm_pb2
import idm_pb2_grpc

from models import DatabaseManager, User, UserRole
from jwt_service import JWTService


class IdentityServiceImpl(idm_pb2_grpc.IdentityServiceServicer):

    #Implementarea serviciului gRPC



    def __init__(self, db_manager: DatabaseManager):
        #Initializeaza serviciul
        self.db_manager = db_manager
        self.jwt_service = JWTService()

    def Authenticate(self, request, context):

        #Autentifica un utilizator si genereaza token JWT



        session = self.db_manager.get_session()

        try:
            #Cauta utilizatorul dupa email
            user = session.query(User).filter_by(email=request.username).first()

            #Verifica daca utilizatorul exista
            if not user:
                return idm_pb2.AuthResponse(
                    success=False,
                    token='',
                    message='Invalid username or password'
                )

            #Verifica parola
            if not user.check_password(request.password):
                return idm_pb2.AuthResponse(
                    success=False,
                    token='',
                    message='Invalid username or password'
                )

            #Genereaza token JWT
            token = self.jwt_service.generate_token(
                user_id=user.id,
                role=user.role.value
            )

            return idm_pb2.AuthResponse(
                success=True,
                token=token,
                message='Authentication successful'
            )

        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f'Internal server error: {str(e)}')
            return idm_pb2.AuthResponse(
                success=False,
                token='',
                message=f'Error during authentication: {str(e)}'
            )
        finally:
            session.close()

    def ValidateToken(self, request, context):

        #Valideaza un token JWT


        try:
            # Valideaza token-ul folosind JWT service
            result = self.jwt_service.validate_token(request.token)

            return idm_pb2.ValidationResponse(
                valid=result['valid'],
                user_id=result['user_id'],
                role=result['role'],
                message=result['message']
            )

        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f'Internal server error: {str(e)}')
            return idm_pb2.ValidationResponse(
                valid=False,
                user_id=0,
                role='',
                message=f'Error during validation: {str(e)}'
            )

    def InvalidateToken(self, request, context):

        #Invalideaza un token JWT (logout)
        try:
            # Invalideaza token-ul
            result = self.jwt_service.invalidate_token(request.token)

            return idm_pb2.InvalidationResponse(
                success=result['success'],
                message=result['message']
            )

        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f'Internal server error: {str(e)}')
            return idm_pb2.InvalidationResponse(
                success=False,
                message=f'Error during invalidation: {str(e)}'
            )

    def CreateUser(self, request, context):

        #Creeaza un utilizator nou (doar pentru admin)


        session = self.db_manager.get_session()

        try:
            # Verifica daca email-ul exista deja
            existing_user = session.query(User).filter_by(email=request.email).first()
            if existing_user:
                return idm_pb2.UserResponse(
                    success=False,
                    user_id=0,
                    message=f'User with email {request.email} already exists'
                )

            # Valideaza rolul
            try:
                role = UserRole(request.role)
            except ValueError:
                return idm_pb2.UserResponse(
                    success=False,
                    user_id=0,
                    message=f'Invalid role: {request.role}. Must be: admin, owner-event, client'
                )

            # Creeaza utilizatorul nou
            new_user = User(
                email=request.email,
                role=role
            )
            new_user.set_password(request.password)

            # Salveaza in baza de date
            session.add(new_user)
            session.commit()
            session.refresh(new_user)

            return idm_pb2.UserResponse(
                success=True,
                user_id=new_user.id,
                message=f'User created successfully with ID: {new_user.id}'
            )

        except Exception as e:
            session.rollback()
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f'Internal server error: {str(e)}')
            return idm_pb2.UserResponse(
                success=False,
                user_id=0,
                message=f'Error creating user: {str(e)}'
            )
        finally:
            session.close()


def serve():
    #Porneste serverul gRPC
    #Incarca variabilele de mediu
    load_dotenv()

    # Configureaza conexiunea la baza de date
    db_host = os.getenv('DB_HOST', 'localhost')
    db_port = os.getenv('DB_PORT', '3306')
    db_name = os.getenv('DB_NAME', 'eventflow_idm')
    db_user = os.getenv('DB_USER', 'root')
    db_password = os.getenv('DB_PASSWORD', '')

    connection_string = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"

    # Initializeaza database manager
    db_manager = DatabaseManager(connection_string)
    db_manager.create_tables()

    # Configureaza serverul gRPC
    grpc_port = os.getenv('GRPC_PORT', '50051')
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # Adauga serviciul la server
    idm_pb2_grpc.add_IdentityServiceServicer_to_server(
        IdentityServiceImpl(db_manager),
        server
    )

    # Porneste serverul
    server.add_insecure_port(f'[::]:{grpc_port}')
    server.start()

    print(f'gRPC server started on port {grpc_port}')
    print('Press Ctrl+C to stop')

    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        print('\nShutting down server...')
        server.stop(0)


if __name__ == '__main__':
    serve()