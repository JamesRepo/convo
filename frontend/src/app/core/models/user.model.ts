export interface User {
    id: number;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    avatarUrl?: string;
    status: 'ONLINE' | 'OFFLINE' | 'AWAY';
    lastSeen?: Date;
}

export interface AuthenticationRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

export interface AuthenticationResponse {
    token: string;
    type: string;
    userId: number;
    username: string;
    email: string;
}