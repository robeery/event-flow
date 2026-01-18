#!/usr/bin/env python3
"""
Script to populate the database with sample data in English.
Run this after all services are up and running.
"""

import requests
import sys

# Service URLs
GATEWAY_URL = "http://localhost:8000/api"
BILETE_URL = "http://localhost:8080/api"
CLIENTI_URL = "http://localhost:8081/api"

# Admin token (will be set after login)
admin_token = None


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
            response = requests.post(f"{GATEWAY_URL}/auth/register", json=user)
            if response.status_code == 200:
                data = response.json()
                print(f"  ✓ Created user: {user['email']} (ID: {data.get('userId')}, Role: {user['role']})")
            else:
                print(f"  ✗ Failed to create {user['email']}: {response.text}")
        except Exception as e:
            print(f"  ✗ Error creating {user['email']}: {e}")


def login_as_owner(email="john.owner@eventflow.com", password="owner123"):
    """Login and get token"""
    global admin_token
    try:
        response = requests.post(f"{GATEWAY_URL}/auth/login", json={
            "email": email,
            "password": password
        })
        if response.status_code == 200:
            data = response.json()
            admin_token = data.get("token")
            print(f"\n=== Logged in as {email} ===")
            return True
    except Exception as e:
        print(f"Login failed: {e}")
    return False


def get_auth_headers():
    """Get authorization headers"""
    return {"Authorization": f"Bearer {admin_token}"}


def create_events():
    """Create sample events"""
    print("\n=== Creating Events ===")
    
    events = [
        {
            "idOwner": 2,  # john.owner
            "nume": "Summer Music Festival",
            "locatie": "Central Park, New York",
            "descriere": "A fantastic outdoor music festival featuring top artists from around the world.",
            "numarLocuri": 5000
        },
        {
            "idOwner": 2,
            "nume": "Tech Conference 2025",
            "locatie": "Convention Center, San Francisco",
            "descriere": "Annual technology conference with keynotes, workshops, and networking opportunities.",
            "numarLocuri": 2000
        },
        {
            "idOwner": 2,
            "nume": "Jazz Night",
            "locatie": "Blue Note Club, Chicago",
            "descriere": "An intimate evening of smooth jazz with renowned musicians.",
            "numarLocuri": 200
        },
        {
            "idOwner": 3,  # jane.owner
            "nume": "Art Exhibition Opening",
            "locatie": "Metropolitan Museum, Boston",
            "descriere": "Opening night of the contemporary art exhibition featuring emerging artists.",
            "numarLocuri": 500
        },
        {
            "idOwner": 3,
            "nume": "Comedy Night Live",
            "locatie": "Laugh Factory, Los Angeles",
            "descriere": "Stand-up comedy show featuring top comedians from Netflix specials.",
            "numarLocuri": 300
        },
        {
            "idOwner": 2,
            "nume": "Rock Concert",
            "locatie": "Madison Square Garden, New York",
            "descriere": "Epic rock concert with legendary bands and special guest appearances.",
            "numarLocuri": 20000
        },
        {
            "idOwner": 3,
            "nume": "Wine Tasting Gala",
            "locatie": "Napa Valley Vineyards, California",
            "descriere": "Exclusive wine tasting event with premium selections from around the world.",
            "numarLocuri": 150
        },
        {
            "idOwner": 2,
            "nume": "Basketball Championship Finals",
            "locatie": "Staples Center, Los Angeles",
            "descriere": "The ultimate basketball showdown - championship finals live!",
            "numarLocuri": 18000
        }
    ]
    
    for event in events:
        try:
            response = requests.post(
                f"{BILETE_URL}/event-manager/events",
                json=event,
                headers=get_auth_headers()
            )
            if response.status_code in [200, 201]:
                data = response.json()
                print(f"  ✓ Created event: {event['nume']} (ID: {data.get('id')})")
            else:
                print(f"  ✗ Failed to create {event['nume']}: {response.text}")
        except Exception as e:
            print(f"  ✗ Error creating {event['nume']}: {e}")


def create_packages():
    """Create event packages"""
    print("\n=== Creating Event Packages ===")
    
    packages = [
        {
            "idOwner": 2,
            "nume": "Summer Entertainment Bundle",
            "locatie": "Various Locations",
            "descriere": "Get access to multiple summer events at a discounted price!",
            "numarLocuri": 1000
        },
        {
            "idOwner": 3,
            "nume": "Culture Pass",
            "locatie": "Art & Entertainment Venues",
            "descriere": "Experience the best of art, comedy, and fine dining events.",
            "numarLocuri": 200
        },
        {
            "idOwner": 2,
            "nume": "Sports Fan Package",
            "locatie": "Major Sports Arenas",
            "descriere": "For the ultimate sports enthusiast - access to premium sporting events.",
            "numarLocuri": 5000
        }
    ]
    
    for pkg in packages:
        try:
            response = requests.post(
                f"{BILETE_URL}/event-manager/event-packets",
                json=pkg,
                headers=get_auth_headers()
            )
            if response.status_code in [200, 201]:
                data = response.json()
                print(f"  ✓ Created package: {pkg['nume']} (ID: {data.get('id')})")
            else:
                print(f"  ✗ Failed to create {pkg['nume']}: {response.text}")
        except Exception as e:
            print(f"  ✗ Error creating {pkg['nume']}: {e}")


def create_clients():
    """Create client profiles"""
    print("\n=== Creating Client Profiles ===")
    
    # First login as client to create their own profile
    clients = [
        {"email": "alice.client@eventflow.com", "prenume": "Alice", "nume": "Johnson", "dateSuntPublice": True},
        {"email": "bob.client@eventflow.com", "prenume": "Bob", "nume": "Smith", "dateSuntPublice": True},
        {"email": "charlie.client@eventflow.com", "prenume": "Charlie", "nume": "Brown", "dateSuntPublice": False},
    ]
    
    for client_data in clients:
        try:
            # Login as this client
            login_resp = requests.post(f"{GATEWAY_URL}/auth/login", json={
                "email": client_data["email"],
                "password": "client123"
            })
            
            if login_resp.status_code == 200:
                client_token = login_resp.json().get("token")
                headers = {"Authorization": f"Bearer {client_token}"}
                
                # Create profile
                profile = {
                    "email": client_data["email"],
                    "prenume": client_data["prenume"],
                    "nume": client_data["nume"],
                    "dateSuntPublice": client_data["dateSuntPublice"]
                }
                
                response = requests.post(
                    f"{CLIENTI_URL}/clients",
                    json=profile,
                    headers=headers
                )
                
                if response.status_code in [200, 201]:
                    data = response.json()
                    print(f"  ✓ Created client profile: {client_data['prenume']} {client_data['nume']}")
                else:
                    print(f"  ✗ Failed to create profile for {client_data['email']}: {response.text}")
        except Exception as e:
            print(f"  ✗ Error creating profile for {client_data['email']}: {e}")


def create_tickets():
    """Create some sample tickets"""
    print("\n=== Creating Sample Tickets ===")
    
    # Login as owner first
    if not login_as_owner():
        print("  ✗ Could not login as owner to create tickets")
        return
    
    # Create tickets for events 1, 2, 3
    for event_id in [1, 2, 3]:
        for i in range(3):  # 3 tickets per event
            try:
                response = requests.post(
                    f"{BILETE_URL}/event-manager/events/{event_id}/tickets",
                    json={},
                    headers=get_auth_headers()
                )
                if response.status_code in [200, 201]:
                    data = response.json()
                    print(f"  ✓ Created ticket for event {event_id}: {data.get('cod')}")
            except Exception as e:
                print(f"  ✗ Error creating ticket for event {event_id}: {e}")


def main():
    print("=" * 60)
    print("EventFlow - Database Population Script")
    print("=" * 60)
    
    # 1. Register users
    register_users()
    
    # 2. Login as owner to create events/packages
    if login_as_owner("john.owner@eventflow.com", "owner123"):
        create_events()
        create_packages()
        create_tickets()
    
    # 3. Create client profiles
    create_clients()
    
    print("\n" + "=" * 60)
    print("Database population complete!")
    print("=" * 60)
    print("\nTest credentials:")
    print("  Owner: john.owner@eventflow.com / owner123")
    print("  Owner: jane.owner@eventflow.com / owner123")
    print("  Client: alice.client@eventflow.com / client123")
    print("  Client: bob.client@eventflow.com / client123")
    print("  Admin: admin@eventflow.com / admin123")


if __name__ == "__main__":
    main()
