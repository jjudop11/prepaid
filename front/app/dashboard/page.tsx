'use client';

import { Navbar } from '@/components/Navbar';
import { StatCard } from '@/components/StatCard';
import { Wallet, CreditCard, Activity, ArrowRight, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { MOCK_CHART_DATA, MOCK_TRANSACTIONS } from '@/lib/constants';
import { formatPoints } from '@/lib/utils';
import Link from 'next/link';

export default function Dashboard() {
    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Welcome Section */}
                <div className="mb-8 flex flex-col md:flex-row md:items-end justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">대시보드</h1>
                        <p className="text-gray-500 mt-1">환영합니다! 포인트 현황을 확인하세요.</p>
                    </div>
                    <div className="flex gap-3">
                        <Link href="/charge" className="inline-flex items-center px-4 py-2 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 transition-colors">
                            <Wallet className="mr-2 h-4 w-4" />
                            포인트 충전
                        </Link>
                    </div>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <StatCard
                        title="총 잔액"
                        value="210,000 P"
                        trend="+12.5%"
                        trendType="positive"
                        icon={<Wallet size={24} />}
                        highlight={true}
                    />
                    <StatCard
                        title="이번 달 사용"
                        value="45,200 P"
                        trend="-2.4%"
                        trendType="positive"
                        icon={<CreditCard size={24} />}
                    />
                    <StatCard
                        title="이번 달 충전"
                        value="150,000 P"
                        trend="+8.2%"
                        trendType="positive"
                        icon={<Activity size={24} />}
                    />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Main Chart */}
                    <div className="lg:col-span-2 bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                                <TrendingUp size={20} className="text-blue-500" />
                                잔액 히스토리
                            </h3>
                            <select className="text-sm border-gray-200 rounded-md text-gray-500 focus:ring-blue-500 focus:border-blue-500 p-1">
                                <option>최근 7일</option>
                                <option>최근 30일</option>
                                <option>올해</option>
                            </select>
                        </div>
                        <div className="h-[300px] w-full">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={MOCK_CHART_DATA}>
                                    <defs>
                                        <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#2563eb" stopOpacity={0.1} />
                                            <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                                        </linearGradient>
                                    </defs>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                                    <XAxis
                                        dataKey="name"
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fill: '#9ca3af', fontSize: 12 }}
                                        dy={10}
                                    />
                                    <YAxis
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fill: '#9ca3af', fontSize: 12 }}
                                        tickFormatter={(value) => `${value / 1000}k`}
                                    />
                                    <Tooltip
                                        contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                                    />
                                    <Area
                                        type="monotone"
                                        dataKey="value"
                                        stroke="#2563eb"
                                        strokeWidth={3}
                                        fillOpacity={1}
                                        fill="url(#colorValue)"
                                    />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Recent Transactions */}
                    <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-lg font-bold text-gray-900">최근 활동</h3>
                            <Link href="/history" className="text-sm text-blue-600 hover:text-blue-700 font-medium flex items-center">
                                전체보기 <ArrowRight size={14} className="ml-1" />
                            </Link>
                        </div>
                        <div className="space-y-4">
                            {MOCK_TRANSACTIONS.slice(0, 4).map((tx) => (
                                <div key={tx.id} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-xl transition-colors">
                                    <div className="flex items-center gap-3">
                                        <div className={`w-10 h-10 rounded-full flex items-center justify-center ${tx.type === 'charge' ? 'bg-green-100 text-green-600' : 'bg-orange-100 text-orange-600'
                                            }`}>
                                            {tx.type === 'charge' ? <ArrowRight className="rotate-45" size={18} /> : <ArrowRight className="-rotate-45" size={18} />}
                                        </div>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-900">{tx.description}</p>
                                            <p className="text-xs text-gray-500">{tx.date}</p>
                                        </div>
                                    </div>
                                    <span className={`text-sm font-bold ${tx.type === 'charge' ? 'text-green-600' : 'text-gray-900'
                                        }`}>
                                        {tx.type === 'charge' ? '+' : ''}{formatPoints(tx.amount)}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
