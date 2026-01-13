'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Wallet, LayoutDashboard, History, User, LogOut } from 'lucide-react';
import { ConnectionIndicator } from './ConnectionIndicator';
import { removeToken } from '@/lib/utils';

export function Navbar() {
    const pathname = usePathname();

    const isActive = (path: string) => pathname === path;

    const handleLogout = () => {
        removeToken();
        window.location.href = '/';
    };

    return (
        <nav className="sticky top-0 z-50 w-full bg-white/80 backdrop-blur-md border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link href="/dashboard" className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold text-xl">
                                P
                            </div>
                            <span className="font-bold text-xl tracking-tight text-gray-900">PointFlow</span>
                        </Link>
                    </div>

                    <div className="hidden md:flex items-center space-x-8">
                        <NavLink href="/dashboard" icon={<LayoutDashboard size={18} />} label="대시보드" active={isActive('/dashboard')} />
                        <NavLink href="/charge" icon={<Wallet size={18} />} label="충전" active={isActive('/charge')} />
                        <NavLink href="/history" icon={<History size={18} />} label="히스토리" active={isActive('/history')} />
                        <NavLink href="/profile" icon={<User size={18} />} label="마이페이지" active={isActive('/profile')} />
                    </div>

                    <div className="flex items-center gap-4">
                        <ConnectionIndicator />
                        <button
                            onClick={handleLogout}
                            className="text-sm font-medium text-gray-500 hover:text-gray-900 flex items-center gap-1 transition-colors"
                        >
                            <LogOut size={16} />
                            <span className="hidden sm:inline">로그아웃</span>
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
}

interface NavLinkProps {
    href: string;
    icon: React.ReactNode;
    label: string;
    active: boolean;
}

function NavLink({ href, icon, label, active }: NavLinkProps) {
    return (
        <Link
            href={href}
            className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-all duration-200 ${active
                    ? 'text-blue-600 bg-blue-50'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                }`}
        >
            {icon}
            {label}
        </Link>
    );
}
