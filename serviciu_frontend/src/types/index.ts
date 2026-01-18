// Tipuri pentru autentificare
export interface User {
  userId: number;
  role: 'admin' | 'owner-event' | 'client';
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  token?: string;
  userId?: number;
  role?: string;
  message: string;
}

// Tipuri pentru evenimente
export interface Eveniment {
  id: number;
  idOwner: number;
  nume: string;
  locatie: string;
  descriere: string;
  numarLocuri: number;
  _links?: HateoasLinks;
}

// Tipuri pentru pachete
export interface Pachet {
  id: number;
  idOwner: number;
  nume: string;
  locatie: string;
  descriere: string;
  numarLocuri: number;
  _links?: HateoasLinks;
}

// Tipuri pentru bilete
export interface Bilet {
  cod: string;
  evenimentId?: number;
  pachetId?: number;
  _links?: HateoasLinks;
}

// Tipuri pentru clienți
export interface Client {
  id: string;
  idmUserId: number;
  email: string;
  prenume: string;
  nume: string;
  dateSuntPublice: boolean;
  linkuriSocialMedia?: Record<string, string>;
  bileteAchizitionate?: string[];
  links?: Record<string, string>;
}

export interface ClientRequest {
  email: string;
  prenume: string;
  nume: string;
  dateSuntPublice: boolean;
  linkuriSocialMedia?: Record<string, string>;
}

// HATEOAS links
export interface HateoasLinks {
  self?: { href: string };
  [key: string]: { href: string } | undefined;
}

// Response types pentru colecții
export interface CollectionResponse<T> {
  _embedded?: { content: T[] };
  _links?: HateoasLinks;
  content?: T[];
}
