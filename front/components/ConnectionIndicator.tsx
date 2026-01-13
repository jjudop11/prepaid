'use client';

import { useNotificationStore } from '@/hooks/useNotifications';

export function ConnectionIndicator() {
    const { connectionStatus } = useNotificationStore();

    const statusConfig = {
        connected: {
            color: 'bg-green-500',
            label: '연결됨',
            pulse: false,
        },
        reconnecting: {
            color: 'bg-yellow-500',
            label: '재연결 중...',
            pulse: true,
        },
        offline: {
            color: 'bg-red-500',
            label: '오프라인',
            pulse: false,
        },
    };

    const config = statusConfig[connectionStatus];

    return (
        <div className="flex items-center gap-2 group relative">
            <div className="relative">
                <div className={`w-2 h-2 rounded-full ${config.color} ${config.pulse ? 'animate-pulse' : ''}`} />
                {config.pulse && (
                    <div className={`absolute inset-0 w-2 h-2 rounded-full ${config.color} animate-ping opacity-75`} />
                )}
            </div>
            {/* Tooltip */}
            <div className="hidden group-hover:block absolute right-0 top-8 bg-gray-900 text-white text-xs px-2 py-1 rounded whitespace-nowrap z-10">
                {config.label}
            </div>
        </div>
    );
}
