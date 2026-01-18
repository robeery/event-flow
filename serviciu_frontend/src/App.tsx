import { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout';
import { Login } from './components/Login';
import { EventList } from './components/EventList';
import { EventForm } from './components/EventForm';
import { EventDetails } from './components/EventDetails';
import { PackageList } from './components/PackageList';
import { PackageForm } from './components/PackageForm';
import { MyTickets } from './components/MyTickets';
import { ClientProfile } from './components/ClientProfile';
import type { Eveniment } from './types';
import './App.css';

function AppContent() {
  const { user } = useAuth();
  const [currentPage, setCurrentPage] = useState('events');
  const [selectedEvent, setSelectedEvent] = useState<Eveniment | null>(null);
  const [showEventForm, setShowEventForm] = useState(false);
  const [showPackageForm, setShowPackageForm] = useState(false);

  const handleNavigate = (page: string) => {
    setCurrentPage(page);
    setSelectedEvent(null);
    setShowEventForm(false);
    setShowPackageForm(false);
  };

  const handleSelectEvent = (event: Eveniment) => {
    setSelectedEvent(event);
  };

  const handleEventFormSuccess = () => {
    setShowEventForm(false);
    setCurrentPage('my-events');
  };

  const handlePackageFormSuccess = () => {
    setShowPackageForm(false);
    setCurrentPage('my-packages');
  };

  const renderPage = () => {
    // Dacă nu e autentificat și încearcă să acceseze pagini protejate
    if (!user && ['my-tickets', 'profile', 'my-events', 'my-packages', 'all-tickets'].includes(currentPage)) {
      return <Login />;
    }

    // Formular eveniment nou
    if (showEventForm) {
      return (
        <EventForm
          onSuccess={handleEventFormSuccess}
          onCancel={() => setShowEventForm(false)}
        />
      );
    }

    // Formular pachet nou
    if (showPackageForm) {
      return (
        <PackageForm
          onSuccess={handlePackageFormSuccess}
          onCancel={() => setShowPackageForm(false)}
        />
      );
    }

    // Detalii eveniment
    if (selectedEvent) {
      return (
        <EventDetails
          event={selectedEvent}
          onBack={() => setSelectedEvent(null)}
        />
      );
    }

    switch (currentPage) {
      case 'login':
        return <Login />;

      case 'events':
        return (
          <div>
            <EventList onSelectEvent={handleSelectEvent} />
          </div>
        );

      case 'packages':
        return <PackageList />;

      case 'my-events':
        return (
          <div>
            <div className="page-header">
              <h2>Evenimentele Mele</h2>
              <button onClick={() => setShowEventForm(true)} className="add-button">
                + Adaugă Eveniment
              </button>
            </div>
            <EventList onSelectEvent={handleSelectEvent} ownerOnly={true} />
          </div>
        );

      case 'my-packages':
        return (
          <div>
            <div className="page-header">
              <h2>Pachetele Mele</h2>
              <button onClick={() => setShowPackageForm(true)} className="add-button">
                + Adauga Pachet
              </button>
            </div>
            <PackageList ownerOnly={true} />
          </div>
        );

      case 'my-tickets':
        return <MyTickets />;

      case 'profile':
        return <ClientProfile />;

      case 'all-tickets':
        return (
          <div>
            <h2>Toate Biletele</h2>
            <p>Funcționalitate disponibilă pentru admin.</p>
          </div>
        );

      default:
        return <EventList onSelectEvent={handleSelectEvent} />;
    }
  };

  return (
    <Layout currentPage={currentPage} onNavigate={handleNavigate}>
      {renderPage()}
    </Layout>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
