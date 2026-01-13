import { Transaction, ChartData } from '@/types';

// API Endpoints
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
export const SSE_ENDPOINT = `${API_BASE_URL}/api/notifications/stream`;

// SSE Configuration
export const SSE_CONFIG = {
    INITIAL_RETRY_DELAY: 1000, // 1초
    MAX_RETRY_DELAY: 30000, // 30초
    MAX_RETRIES: 10,
    HEARTBEAT_INTERVAL: 15000, // 15초
} as const;

// Toast Configuration
export const TOAST_CONFIG = {
    DEFAULT_TIMEOUT: 5000, // 5초
    MAX_TOASTS: 5,
} as const;

// Mock Data (개발용)
export const MOCK_TRANSACTIONS: Transaction[] = [
    { id: '1', type: 'charge', amount: 50000, description: '카카오페이 충전', date: '2025-12-27', status: 'completed', balance: 210000 },
    { id: '2', type: 'use', amount: -12500, description: '프리미엄 서비스 구독', date: '2025-12-26', status: 'completed', balance: 160000 },
    { id: '3', type: 'use', amount: -4500, description: '클라우드 스토리지 비용', date: '2025-12-25', status: 'completed', balance: 172500 },
    { id: '4', type: 'charge', amount: 100000, description: '신용카드 충전', date: '2025-12-24', status: 'completed', balance: 177000 },
    { id: '5', type: 'use', amount: -32000, description: 'API 사용료', date: '2025-12-23', status: 'pending', balance: 77000 },
];

export const MOCK_CHART_DATA: ChartData[] = [
    { name: '월', value: 120000 },
    { name: '화', value: 115000 },
    { name: '수', value: 165000 },
    { name: '목', value: 160500 },
    { name: '금', value: 140000 },
    { name: '토', value: 190000 },
    { name: '일', value: 210000 },
];

// Naver Login Button Colors (BI Guidelines)
export const NAVER_COLORS = {
    GREEN: '#03C75A',
    WHITE: '#FFFFFF',
    BLACK: '#000000',
} as const;
