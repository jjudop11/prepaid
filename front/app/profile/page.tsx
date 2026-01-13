'use client';

import { Navbar } from '@/components/Navbar';
import { User, Mail, Calendar, LogOut } from 'lucide-react';
import { removeToken } from '@/lib/utils';
import { useRouter } from 'next/navigation';

export default function Profile() {
    const router = useRouter();

    const handleLogout = () => {
        removeToken();
        router.push('/');
    };

    // TODO: 실제 사용자 정보 API 연동
    const user = {
        name: '홍길동',
        email: 'hong@example.com',
        joinDate: '2025-01-15',
        balance: 210000,
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900">마이페이지</h1>
                    <p className="text-gray-500 mt-1">내 정보를 확인하고 관리하세요.</p>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                    {/* Profile Header */}
                    <div className="bg-gradient-to-r from-blue-600 to-blue-700 p-8 text-white">
                        <div className="flex items-center gap-6">
                            <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center text-3xl font-bold">
                                {user.name.charAt(0)}
                            </div>
                            <div>
                                <h2 className="text-2xl font-bold">{user.name}</h2>
                                <p className="text-blue-100 mt-1">{user.email}</p>
                            </div>
                        </div>
                    </div>

                    {/* Profile Info */}
                    <div className="p-6 space-y-4">
                        <div className="flex items-center gap-4 p-4 rounded-xl bg-gray-50">
                            <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center">
                                <User size={20} />
                            </div>
                            <div className="flex-1">
                                <p className="text-sm text-gray-500">이름</p>
                                <p className="font-semibold text-gray-900">{user.name}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 p-4 rounded-xl bg-gray-50">
                            <div className="w-10 h-10 rounded-full bg-green-100 text-green-600 flex items-center justify-center">
                                <Mail size={20} />
                            </div>
                            <div className="flex-1">
                                <p className="text-sm text-gray-500">이메일</p>
                                <p className="font-semibold text-gray-900">{user.email}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 p-4 rounded-xl bg-gray-50">
                            <div className="w-10 h-10 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center">
                                <Calendar size={20} />
                            </div>
                            <div className="flex-1">
                                <p className="text-sm text-gray-500">가입일</p>
                                <p className="font-semibold text-gray-900">{user.joinDate}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 p-4 rounded-xl bg-blue-50">
                            <div className="w-10 h-10 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold">
                                P
                            </div>
                            <div className="flex-1">
                                <p className="text-sm text-blue-600">현재 잔액</p>
                                <p className="text-2xl font-bold text-blue-700">{user.balance.toLocaleString()} P</p>
                            </div>
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="p-6 border-t border-gray-100">
                        <button
                            onClick={handleLogout}
                            className="w-full flex items-center justify-center gap-2 py-3 px-4 border border-red-300 rounded-lg text-red-600 hover:bg-red-50 transition-colors font-semibold"
                        >
                            <LogOut size={18} />
                            로그아웃
                        </button>
                    </div>
                </div>
            </main>
        </div>
    );
}
