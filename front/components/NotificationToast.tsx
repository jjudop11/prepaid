'use client';

import { useEffect, useState } from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';
import { useNotificationStore } from '@/hooks/useNotifications';
import { TOAST_CONFIG } from '@/lib/constants';

export function NotificationToast() {
    const { notifications, removeNotification } = useNotificationStore();

    return (
        <div className="fixed top-4 right-4 z-50 flex flex-col gap-3 w-96 max-w-[calc(100vw-2rem)]">
            {notifications.map((notification) => (
                <ToastItem
                    key={notification.id}
                    notification={notification}
                    onClose={() => removeNotification(notification.id)}
                />
            ))}
        </div>
    );
}

interface ToastItemProps {
    notification: {
        id: string;
        type: 'success' | 'error' | 'info';
        title: string;
        message: string;
        timestamp: number;
    };
    onClose: () => void;
}

function ToastItem({ notification, onClose }: ToastItemProps) {
    const [progress, setProgress] = useState(100);
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        const startTime = Date.now();
        const duration = TOAST_CONFIG.DEFAULT_TIMEOUT;

        const interval = setInterval(() => {
            const elapsed = Date.now() - startTime;
            const remaining = Math.max(0, 100 - (elapsed / duration) * 100);
            setProgress(remaining);

            if (remaining === 0) {
                clearInterval(interval);
                handleClose();
            }
        }, 50);

        return () => clearInterval(interval);
    }, []);

    const handleClose = () => {
        setIsExiting(true);
        setTimeout(() => {
            onClose();
        }, 300);
    };

    const typeStyles = {
        success: {
            bg: 'bg-green-50 border-green-200',
            icon: 'text-green-600',
            progress: 'bg-green-500',
            IconComponent: CheckCircle,
        },
        error: {
            bg: 'bg-red-50 border-red-200',
            icon: 'text-red-600',
            progress: 'bg-red-500',
            IconComponent: AlertCircle,
        },
        info: {
            bg: 'bg-blue-50 border-blue-200',
            icon: 'text-blue-600',
            progress: 'bg-blue-500',
            IconComponent: Info,
        },
    };

    const style = typeStyles[notification.type];
    const Icon = style.IconComponent;

    return (
        <div
            className={`${style.bg} border rounded-xl shadow-lg p-4 flex items-start gap-3 transition-all duration-300 ${isExiting ? 'opacity-0 translate-x-full' : 'opacity-100 translate-x-0 animate-slide-in'
                }`}
        >
            <div className={`${style.icon} shrink-0 mt-0.5`}>
                <Icon size={20} />
            </div>
            <div className="flex-1 min-w-0">
                <h4 className="font-semibold text-gray-900 text-sm mb-0.5">{notification.title}</h4>
                <p className="text-sm text-gray-600 break-words">{notification.message}</p>
            </div>
            <button
                onClick={handleClose}
                className="shrink-0 text-gray-400 hover:text-gray-600 transition-colors"
                aria-label="닫기"
            >
                <X size={18} />
            </button>
            {/* Progress bar */}
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-200 rounded-b-xl overflow-hidden">
                <div
                    className={`h-full ${style.progress} transition-all duration-50 ease-linear`}
                    style={{ width: `${progress}%` }}
                />
            </div>
        </div>
    );
}
