'use client';

import { useEffect } from 'react';
import { NotificationToast } from './NotificationToast';
import { useNotifications } from '@/hooks/useNotifications';

export function NotificationProvider({ children }: { children: React.ReactNode }) {
    // Initialize SSE connection
    useNotifications();

    return (
        <>
            {children}
            <NotificationToast />
        </>
    );
}
