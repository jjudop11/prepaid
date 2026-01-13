/**
 * JWT 토큰을 localStorage에 저장
 */
export function setToken(token: string): void {
    if (typeof window !== 'undefined') {
        localStorage.setItem('auth_token', token);
    }
}

/**
 * localStorage에서 JWT 토큰 가져오기
 */
export function getToken(): string | null {
    if (typeof window !== 'undefined') {
        return localStorage.getItem('auth_token');
    }
    return null;
}

/**
 * JWT 토큰 삭제
 */
export function removeToken(): void {
    if (typeof window !== 'undefined') {
        localStorage.removeItem('auth_token');
    }
}

/**
 * 숫자를 한국 원화 포맷으로 변환
 */
export function formatKRW(amount: number): string {
    return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
    }).format(amount);
}

/**
 * 포인트 형식으로 변환
 */
export function formatPoints(amount: number): string {
    return `${amount.toLocaleString('ko-KR')} P`;
}

/**
 * 날짜를 한국어 형식으로 변환
 */
export function formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    }).format(date);
}

/**
 * 상대 시간 표시 (예: "5분 전")
 */
export function getRelativeTime(timestamp: number): string {
    const now = Date.now();
    const diff = now - timestamp;

    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    if (hours < 24) return `${hours}시간 전`;
    return `${days}일 전`;
}
