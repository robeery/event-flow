#!/usr/bin/env python3
"""
Initialization script that waits for services to be ready and populates the database.
This runs as a one-time job in Docker.
"""

import requests
import time
import sys

# Service URLs (internal Docker network)
GATEWAY_URL = "http://serviciu-gateway:8000/api"
BILETE_URL = "http://serviciu-bilete:8080/api"
CLIENTI_URL = "http://serviciu-clienti:8081/api"

MAX_RETRIES = 60
RETRY_DELAY = 2

# Store user info after registration/login
users_info = {}


def wait_for_service(url, name):
    """Wait for a service to become available"""
    print(f"Waiting for {name}...", end="", flush=True)
    
    for i in range(MAX_RETRIES):
        try:
            response = requests.get(url, timeout=2)
            if response.status_code < 500:
                print(" OK")
                return True
        except Exception:
            pass
        print(".", end="", flush=True)
        time.sleep(RETRY_DELAY)
    
    print(" FAILED")
    return False


def wait_for_all_services():
    """Wait for all services to be ready"""
    print("\n" + "=" * 60)
    print("Waiting for services to be ready...")
    print("=" * 60)
    
    services = [
        (f"{GATEWAY_URL.replace('/api', '')}/health", "API Gateway"),
        (f"{BILETE_URL}/event-manager/events", "Events Service"),
        (f"{CLIENTI_URL}/clients", "Clients Service"),
    ]
    
    for url, name in services:
        if not wait_for_service(url, name):
            print(f"\nERROR: {name} is not available. Exiting.")
            return False
    
    print("\nAll services are ready!")
    return True


def register_users():
    """Register sample users via Gateway"""
    print("\n=== Registering Users ===")
    
    users = [
        {"email": "admin@eventflow.com", "password": "admin123", "role": "admin"},
        {"email": "john.owner@eventflow.com", "password": "owner123", "role": "owner-event"},
        {"email": "jane.owner@eventflow.com", "password": "owner123", "role": "owner-event"},
        {"email": "alice.client@eventflow.com", "password": "client123", "role": "client"},
        {"email": "bob.client@eventflow.com", "password": "client123", "role": "client"},
        {"email": "charlie.client@eventflow.com", "password": "client123", "role": "client"},
    ]
    
    for user in users:
        try:
            response = requests.post(f"{GATEWAY_URL}/auth/register", json=user, timeout=10)
            if response.status_code == 200:
                data = response.json()
                user_id = data.get('userId')
                users_info[user['email']] = {
                    'id': user_id,
                    'role': user['role'],
                    'password': user['password']
                }
                print(f"  ✓ Created user: {user['email']} (ID: {user_id}, Role: {user['role']})")
            else:
                print(f"  ✗ Failed to create {user['email']}: {response.text[:100]}")
        except Exception as e:
            print(f"  ✗ Error creating {user['email']}: {e}")


def login_user(email, password):
    """Login and get token and user info"""
    try:
        response = requests.post(f"{GATEWAY_URL}/auth/login", json={
            "email": email,
            "password": password
        }, timeout=10)
        if response.status_code == 200:
            data = response.json()
            return {
                'token': data.get("token"),
                'userId': data.get("userId"),
                'role': data.get("role")
            }
    except Exception as e:
        print(f"Login failed for {email}: {e}")
    return None


def get_auth_headers(token):
    """Get authorization headers"""
    return {"Authorization": f"Bearer {token}"}


def create_events():
    """Create sample events"""
    print("\n=== Creating Events ===")
    
    # Login as john.owner
    john = login_user("john.owner@eventflow.com", "owner123")
    jane = login_user("jane.owner@eventflow.com", "owner123")
    
    if not john or not jane:
        print("  ✗ Could not login as owners")
        return
    
    print(f"  Logged in as john.owner (ID: {john['userId']})")
    print(f"  Logged in as jane.owner (ID: {jane['userId']})")
    
    events = [
        {
            "owner": john,
            "data": {
                "nume": "Summer Music Festival",
                "locatie": "Central Park, New York",
                "descriere": "A fantastic outdoor music festival featuring top artists from around the world.",
                "numarLocuri": 5000
            }
        },
        {
            "owner": john,
            "data": {
                "nume": "Tech Conference 2025",
                "locatie": "Convention Center, San Francisco",
                "descriere": "Annual technology conference with keynotes, workshops, and networking opportunities.",
                "numarLocuri": 2000
            }
        },
        {
            "owner": john,
            "data": {
                "nume": "Jazz Night",
                "locatie": "Blue Note Club, Chicago",
                "descriere": "An intimate evening of smooth jazz with renowned musicians.",
                "numarLocuri": 200
            }
        },
        {
            "owner": jane,
            "data": {
                "nume": "Art Exhibition Opening",
                "locatie": "Metropolitan Museum, Boston",
                "descriere": "Opening night of the contemporary art exhibition featuring emerging artists.",
                "numarLocuri": 500
            }
        },
        {
            "owner": jane,
            "data": {
                "nume": "Comedy Night Live",
                "locatie": "Laugh Factory, Los Angeles",
                "descriere": "Stand-up comedy show featuring top comedians from Netflix specials.",
                "numarLocuri": 300
            }
        },
        {
            "owner": john,
            "data": {
                "nume": "Rock Concert",
                "locatie": "Madison Square Garden, New York",
                "descriere": "Epic rock concert with legendary bands and special guest appearances.",
                "numarLocuri": 20000
            }
        },
        {
            "owner": jane,
            "data": {
                "nume": "Wine Tasting Gala",
                "locatie": "Napa Valley Vineyards, California",
                "descriere": "Exclusive wine tasting event with premium selections from around the world.",
                "numarLocuri": 150
            }
        },
        {
            "owner": john,
            "data": {
                "nume": "Basketball Championship Finals",
                "locatie": "Staples Center, Los Angeles",
                "descriere": "The ultimate basketball showdown - championship finals live!",
                "numarLocuri": 18000
            }
        }
    ]
    
    for event in events:
        try:
            event_data = event["data"].copy()
            event_data["idOwner"] = event["owner"]["userId"]
            
            response = requests.post(
                f"{BILETE_URL}/event-manager/events",
                json=event_data,
                headers=get_auth_headers(event["owner"]["token"]),
                timeout=10
            )
            if response.status_code in [200, 201]:
                data = response.json()
                print(f"  ✓ Created: {event_data['nume']} (ID: {data.get('id')})")
            else:
                print(f"  ✗ Failed: {event_data['nume']} - {response.status_code}: {response.text[:100]}")
        except Exception as e:
            print(f"  ✗ Error: {event['data']['nume']}: {e}")
    
    return john  # Return for creating tickets


def create_packages(owner_info):
    """Create event packages"""
    print("\n=== Creating Event Packages ===")
    
    if not owner_info:
        print("  ✗ No owner info available")
        return
    
    jane = login_user("jane.owner@eventflow.com", "owner123")
    
    packages = [
        {
            "owner": owner_info,
            "data": {
                "nume": "Summer Entertainment Bundle",
                "locatie": "Various Locations",
                "descriere": "Get access to multiple summer events at a discounted price!",
                "numarLocuri": 1000
            }
        },
        {
            "owner": jane,
            "data": {
                "nume": "Culture Pass",
                "locatie": "Art & Entertainment Venues",
                "descriere": "Experience the best of art, comedy, and fine dining events.",
                "numarLocuri": 200
            }
        },
        {
            "owner": owner_info,
            "data": {
                "nume": "Sports Fan Package",
                "locatie": "Major Sports Arenas",
                "descriere": "For the ultimate sports enthusiast - access to premium sporting events.",
                "numarLocuri": 5000
            }
        }
    ]
    
    for pkg in packages:
        try:
            pkg_data = pkg["data"].copy()
            pkg_data["idOwner"] = pkg["owner"]["userId"]
            
            response = requests.post(
                f"{BILETE_URL}/event-manager/event-packets",
                json=pkg_data,
                headers=get_auth_headers(pkg["owner"]["token"]),
                timeout=10
            )
            if response.status_code in [200, 201]:
                data = response.json()
                print(f"  ✓ Created: {pkg_data['nume']} (ID: {data.get('id')})")
            else:
                print(f"  ✗ Failed: {pkg_data['nume']} - {response.status_code}")
        except Exception as e:
            print(f"  ✗ Error: {pkg['data']['nume']}: {e}")


def create_clients():
    """Create client profiles"""
    print("\n=== Creating Client Profiles ===")
    
    clients = [
        {"email": "alice.client@eventflow.com", "prenume": "Alice", "nume": "Johnson", "dateSuntPublice": True},
        {"email": "bob.client@eventflow.com", "prenume": "Bob", "nume": "Smith", "dateSuntPublice": True},
        {"email": "charlie.client@eventflow.com", "prenume": "Charlie", "nume": "Brown", "dateSuntPublice": False},
    ]
    
    for client_data in clients:
        try:
            user_info = login_user(client_data["email"], "client123")
            
            if user_info:
                profile = {
                    "email": client_data["email"],
                    "prenume": client_data["prenume"],
                    "nume": client_data["nume"],
                    "dateSuntPublice": client_data["dateSuntPublice"]
                }
                
                response = requests.post(
                    f"{CLIENTI_URL}/clients",
                    json=profile,
                    headers=get_auth_headers(user_info["token"]),
                    timeout=10
                )
                
                if response.status_code in [200, 201]:
                    print(f"  ✓ Created profile: {client_data['prenume']} {client_data['nume']}")
                else:
                    print(f"  ✗ Failed: {client_data['email']} - {response.status_code}")
        except Exception as e:
            print(f"  ✗ Error: {client_data['email']}: {e}")


def create_tickets(owner_info):
    """Create some sample tickets"""
    print("\n=== Creating Sample Tickets ===")
    
    if not owner_info:
        print("  ✗ No owner info available")
        return
    
    for event_id in [2, 3, 4]:  # evenimentele create de noi (id-urile pot varia)
        for i in range(3):
            try:
                response = requests.post(
                    f"{BILETE_URL}/event-manager/events/{event_id}/tickets",
                    json={},
                    headers=get_auth_headers(owner_info["token"]),
                    timeout=10
                )
                if response.status_code in [200, 201]:
                    data = response.json()
                    print(f"  ✓ Ticket for event {event_id}: {data.get('cod')}")
                else:
                    print(f"  ✗ Failed ticket for event {event_id}: {response.status_code}")
            except Exception as e:
                print(f"  ✗ Error: {e}")


def add_events_to_packages(owner_info):
    """Add events to packages"""
    print("\n=== Adding Events to Packages ===")
    
    if not owner_info:
        print("  ✗ No owner info available")
        return
    
    jane = login_user("jane.owner@eventflow.com", "owner123")
    
    # asocieri: event_id -> pachet_id
    # endpoint corect: post /events/{eventid}/event-packets cu body {"pachetid": x}
    # pachetele create au id-uri 2, 3, 4
    # evenimentele create au id-uri 2-9
    associations = [
        {"eventId": 2, "packageId": 2, "owner": owner_info},  # summer music festival -> summer bundle
        {"eventId": 3, "packageId": 2, "owner": owner_info},  # tech conference -> summer bundle
        {"eventId": 7, "packageId": 2, "owner": owner_info},  # rock concert -> summer bundle
        {"eventId": 5, "packageId": 3, "owner": jane},        # art exhibition -> culture pass
        {"eventId": 6, "packageId": 3, "owner": jane},        # comedy night -> culture pass
        {"eventId": 8, "packageId": 3, "owner": jane},        # wine tasting -> culture pass
        {"eventId": 9, "packageId": 4, "owner": owner_info},  # basketball -> sports fan
    ]
    
    for assoc in associations:
        try:
            response = requests.post(
                f"{BILETE_URL}/event-manager/events/{assoc['eventId']}/event-packets",
                json={"pachetId": assoc["packageId"]},
                headers=get_auth_headers(assoc["owner"]["token"]),
                timeout=10
            )
            if response.status_code in [200, 201]:
                print(f"  ✓ Added event {assoc['eventId']} to package {assoc['packageId']}")
            else:
                print(f"  ✗ Failed event {assoc['eventId']} -> package {assoc['packageId']}: {response.status_code}")
        except Exception as e:
            print(f"  ✗ Error: {e}")


def buy_tickets_for_clients():
    """Have clients purchase some tickets"""
    print("\n=== Clients Purchasing Tickets ===")
    
    # gaseste id-urile clientilor
    try:
        response = requests.get(f"{CLIENTI_URL}/clients", timeout=10)
        clients = response.json().get("clients", [])
    except:
        print("  ✗ Could not fetch clients")
        return
    
    # fiecare client cumpara un bilet
    purchases = [
        {"email": "alice.client@eventflow.com", "evenimentId": 2},  # Summer Music Festival
        {"email": "alice.client@eventflow.com", "evenimentId": 3},  # Tech Conference
        {"email": "bob.client@eventflow.com", "evenimentId": 2},    # Summer Music Festival
        {"email": "charlie.client@eventflow.com", "evenimentId": 4}, # Jazz Night
    ]
    
    for purchase in purchases:
        try:
            # gaseste clientul
            client = next((c for c in clients if c["email"] == purchase["email"]), None)
            if not client:
                print(f"  ✗ Client not found: {purchase['email']}")
                continue
            
            # login client
            user_info = login_user(purchase["email"], "client123")
            if not user_info:
                continue
            
            # cumpara bilet
            response = requests.post(
                f"{CLIENTI_URL}/clients/{client['id']}/bilete",
                json={"evenimentId": purchase["evenimentId"]},
                headers=get_auth_headers(user_info["token"]),
                timeout=10
            )
            
            if response.status_code in [200, 201]:
                print(f"  ✓ {purchase['email']} bought ticket for event {purchase['evenimentId']}")
            else:
                print(f"  ✗ Failed: {purchase['email']} - {response.status_code}: {response.text[:100]}")
        except Exception as e:
            print(f"  ✗ Error: {e}")


def main():
    print("\n" + "=" * 60)
    print("EventFlow - Database Initialization")
    print("=" * 60)
    
    # Wait for services
    if not wait_for_all_services():
        sys.exit(1)
    
    # Small delay
    print("\nWaiting 5 more seconds for services to stabilize...")
    time.sleep(5)
    
    # 1. Register users
    register_users()
    
    # 2. Create events (returns owner info for tickets)
    owner_info = create_events()
    
    # 3. Create packages
    create_packages(owner_info)
    
    # 4. Add events to packages
    add_events_to_packages(owner_info)
    
    # 5. Create tickets for events
    create_tickets(owner_info)
    
    # 6. Create client profiles
    create_clients()
    
    # 7. Have clients buy some tickets
    buy_tickets_for_clients()
    
    print("\n" + "=" * 60)
    print("✅ Database initialization complete!")
    print("=" * 60)
    print("\n🌐 Frontend available at: http://localhost")
    print("\n📝 Test credentials:")
    print("  Owner: john.owner@eventflow.com / owner123")
    print("  Owner: jane.owner@eventflow.com / owner123")
    print("  Client: alice.client@eventflow.com / client123")
    print("  Client: bob.client@eventflow.com / client123")
    print("  Admin: admin@eventflow.com / admin123")
    print("=" * 60 + "\n")


if __name__ == "__main__":
    main()

