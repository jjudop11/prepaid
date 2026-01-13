'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { create } from 'zustand';
import { Notification, ConnectionStatus } from '@/types';
import { SSE_ENDPOINT, SSE_CONFIG } from '@/lib/constants';
import { getToken } from '@/lib/utils';

interface NotificationStore {
    notifications: Notification[];
    connectionStatus: ConnectionStatus;
    addNotification: (notification: Notification) => void;
    removeNotification: (id: string) => void;
    setConnectionStatus: (status: ConnectionStatus) => void;
}

// Zustand store for notifications
export const useNotificationStore = create<NotificationStore>((set) => ({
    notifications: [],
    connectionStatus: 'offline',
    addNotification: (notification) =>
        set((state) => ({
            notifications: [...state.notifications, notification].slice(-5), // Keep last 5
        })),
    removeNotification: (id) =>
        set((state) => ({
            notifications: state.notifications.filter((n) => n.id !== id),
        })),
    setConnectionStatus: (status) => set({ connectionStatus: status }),
}));

/**
 * useNotifications Hook
 * SSE 연결 관리 및 실시간 알림 수신
 */
export function useNotifications() {
    const { addNotification, setConnectionStatus, connectionStatus } = useNotificationStore();
    const eventSourceRef = useRef<EventSource | null>(null);
    const retryCountRef = useRef(0);
    const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const connect = useCallback(() => {
        const token = getToken();
        if (!token) {
            console.log('[SSE] No token available, skipping connection');
            setConnectionStatus('offline');
            return;
        }

        // Close existing connection
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
        }

        try {
            const url = `${SSE_ENDPOINT}?token=${encodeURIComponent(token)}`;
            console.log('[SSE] Connecting to:', url);

            const eventSource = new EventSource(url);
            eventSourceRef.current = eventSource;

            eventSource.onopen = () => {
                console.log('[SSE] Connection established');
                setConnectionStatus('connected');
                retryCountRef.current = 0;
            };

            eventSource.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    console.log('[SSE] Message received:', data);

                    // Heartbeat 메시지는 무시
                    if (data.type === 'heartbeat' || data.message === 'connected') {
                        return;
                    }

                    // 알림 추가
                    const notification: Notification = {
                        id: data.eventId || `notif-${Date.now()}`,
                        type: data.notificationType || 'info',
                        title: data.title || '알림',
                        message: data.message,
                        eventType: data.eventType || 'unknown',
                        timestamp: Date.now(),
                    };

                    addNotification(notification);
                } catch (error) {
                    console.error('[SSE] Error parsing message:', error);
                }
            };

            eventSource.onerror = (error) => {
                console.error('[SSE] Connection error:', error);
                eventSource.close();

                // Reconnection logic with exponential backoff
                if (retryCountRef.current < SSE_CONFIG.MAX_RETRIES) {
                    setConnectionStatus('reconnecting');
                    const delay = Math.min(
                        SSE_CONFIG.INITIAL_RETRY_DELAY * Math.pow(2, retryCountRef.current),
                        SSE_CONFIG.MAX_RETRY_DELAY
                    );

                    console.log(`[SSE] Reconnecting in ${delay}ms (attempt ${retryCountRef.current + 1}/${SSE_CONFIG.MAX_RETRIES})`);

                    retryTimeoutRef.current = setTimeout(() => {
                        retryCountRef.current++;
                        connect();
                    }, delay);
                } else {
                    console.error('[SSE] Max retries reached, giving up');
                    setConnectionStatus('offline');
                }
            };
        } catch (error) {
            console.error('[SSE] Failed to create EventSource:', error);
            setConnectionStatus('offline');
        }
    }, [addNotification, setConnectionStatus]);

    const disconnect = useCallback(() => {
        console.log('[SSE] Disconnecting');

        if (retryTimeoutRef.current) {
            clearTimeout(retryTimeoutRef.current);
            retryTimeoutRef.current = null;
        }

        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
        }

        setConnectionStatus('offline');
        retryCountRef.current = 0;
    }, [setConnectionStatus]);

    useEffect(() => {
        connect();

        return () => {
            disconnect();
        };
    }, [connect, disconnect]);

    return {
        connectionStatus,
        reconnect: connect,
        disconnect,
    };
}
