
#Client gRPC pentru testarea serviciului IDM

import grpc
import idm_pb2
import idm_pb2_grpc


class IDMClient:
    #Client pentru serviciul IDM

    def __init__(self, host='localhost', port=50051):
        #init client
        self.channel = grpc.insecure_channel(f'{host}:{port}')
        self.stub = idm_pb2_grpc.IdentityServiceStub(self.channel)

    def authenticate(self, username: str, password: str):
        #Autentificare un utilizator"""
        request = idm_pb2.AuthRequest(
            username=username,
            password=password
        )
        response = self.stub.Authenticate(request)
        return response

    def validate_token(self, token: str):
        #Valideaza un token JWT
        request = idm_pb2.TokenRequest(token=token)
        response = self.stub.ValidateToken(request)
        return response

    def invalidate_token(self, token: str):
        #Invalideaza un token JWT
        request = idm_pb2.TokenRequest(token=token)
        response = self.stub.InvalidateToken(request)
        return response

    def create_user(self, email: str, password: str, role: str):
        #Creeaza un utilizator nou
        request = idm_pb2.CreateUserRequest(
            email=email,
            password=password,
            role=role
        )
        response = self.stub.CreateUser(request)
        return response

    def close(self):
        #Inchide conexiunea
        self.channel.close()


def main():
    #Functie de test pentru client
    client = IDMClient()

    print("=== Test IDM Service ===\n")

    # Test 1: Creeaza un utilizator
    print("1. Creare utilizator client...")
    response = client.create_user(
        email='client1@eventflow.com',
        password='client123',
        role='client'
    )
    print(f"   Success: {response.success}")
    print(f"   User ID: {response.user_id}")
    print(f"   Message: {response.message}\n")

    # Test 2: Creeaza un owner-event
    print("2. Creare utilizator owner-event...")
    response = client.create_user(
        email='owner1@eventflow.com',
        password='owner123',
        role='owner-event'
    )
    print(f"   Success: {response.success}")
    print(f"   User ID: {response.user_id}")
    print(f"   Message: {response.message}\n")

    # Test 3: Autentificare
    print("3. Autentificare cu client1...")
    auth_response = client.authenticate(
        username='client1@eventflow.com',
        password='client123'
    )
    print(f"   Success: {auth_response.success}")
    print(f"   Message: {auth_response.message}")

    if auth_response.success:
        token = auth_response.token
        print(f"   Token: {token[:50]}...\n")
        #print(f"   Token: {token}\n")

        #with open("jwt.txt", "w") as f:
            #print(f"{token}", file=f)




        # Test 4: Validare token
        print("4. Validare token...")
        val_response = client.validate_token(token)
        print(f"   Valid: {val_response.valid}")
        print(f"   User ID: {val_response.user_id}")
        print(f"   Role: {val_response.role}")
        print(f"   Message: {val_response.message}\n")


        # Test 5: Invalidare token
        print("5. Invalidare token (logout)...")
        inv_response = client.invalidate_token(token)
        print(f"   Success: {inv_response.success}")
        print(f"   Message: {inv_response.message}\n")

        # Test 6: Validare token dupa invalidare
        print("6. Validare token dupa invalidare...")
        val_response2 = client.validate_token(token)
        print(f"   Valid: {val_response2.valid}")
        print(f"   Message: {val_response2.message}\n")

    client.close()
    print("=== Teste finalizate ===")


if __name__ == '__main__':
    main()