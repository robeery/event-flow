import { type ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';

interface LayoutProps {
    children: ReactNode;
    currentPage: string;
    onNavigate: (page: string) => void;
}

export function Layout({ children, currentPage, onNavigate }: LayoutProps) {
    const { user, logout } = useAuth();

    const navItems = [
        { id: 'events', label: 'Evenimente', public: true },
        { id: 'packages', label: 'Pachete', public: true },
    ];

    // Adaugă navigație specifică rolului
    if (user) {
        if (user.role === 'client') {
            navItems.push({ id: 'my-tickets', label: 'Biletele Mele', public: false });
            navItems.push({ id: 'profile', label: 'Profil', public: false });
        }
        if (user.role === 'owner-event' || user.role === 'admin') {
            navItems.push({ id: 'my-events', label: 'Evenimentele Mele', public: false });
            navItems.push({ id: 'my-packages', label: 'Pachetele Mele', public: false });
        }
        if (user.role === 'admin') {
            navItems.push({ id: 'all-tickets', label: 'Toate Biletele', public: false });
        }
    }

    return (
        <div className="layout">
            <header className="header">
                <h1 onClick={() => onNavigate('events')} style={{ cursor: 'pointer' }}>
                    EventFlow
                </h1>
                <nav className="nav">
                    {navItems.map((item) => (
                        <button
                            key={item.id}
                            className={`nav-button ${currentPage === item.id ? 'active' : ''}`}
                            onClick={() => onNavigate(item.id)}
                        >
                            {item.label}
                        </button>
                    ))}
                </nav>
                <div className="user-section">
                    {user ? (
                        <>
                            <span className="user-info">
                                {user.role} (ID: {user.userId})
                            </span>
                            <button onClick={logout} className="logout-button">
                                Deconectare
                            </button>
                        </>
                    ) : (
                        <button onClick={() => onNavigate('login')} className="login-button">
                            Autentificare
                        </button>
                    )}
                </div>
            </header>
            <main className="main-content">{children}</main>
            <footer className="footer">
                <p>© 2025 EventFlow - Platformă de gestiune evenimente</p>
            </footer>
        </div>
    );
}
