import { API_BASE_URL } from '../constants';
import { setToken } from '../utils';

export interface SignupData {
    username: string;
    password: string;
    email?: string;
}

export interface LoginData {
    username: string;
    password: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    user: {
        id: number;
        username: string;
        email: string;
        role: string;
    };
}

export async function signup(data: SignupData): Promise<{ success: boolean; message: string }> {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });

        if (response.ok) {
            const message = await response.text();
            return { success: true, message };
        } else {
            const error = await response.text();
            return { success: false, message: error };
        }
    } catch (error) {
        return { success: false, message: '서버와의 통신에 실패했습니다' };
    }
}

export async function login(data: LoginData): Promise<{ success: boolean; message?: string; data?: AuthResponse }> {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', // Cookie 포함
            body: JSON.stringify(data),
        });

        if (response.ok) {
            const authResponse: AuthResponse = await response.json();
            // 토큰 저장 (Cookie 외에 localStorage에도)
            setToken(authResponse.accessToken);
            return { success: true, data: authResponse };
        } else if (response.status === 423) {
            // 계정 잠김
            const error = await response.text();
            return { success: false, message: error };
        } else {
            // 인증 실패
            const error = await response.text();
            return { success: false, message: error };
        }
    } catch (error) {
        return { success: false, message: '서버와의 통신에 실패했습니다' };
    }
}

export async function checkUsernameAvailability(username: string): Promise<boolean> {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/check-username?username=${encodeURIComponent(username)}`);
        return response.ok;
    } catch (error) {
        return false;
    }
}
