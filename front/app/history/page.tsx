'use client';

import { useState } from 'react';
import { Navbar } from '@/components/Navbar';
import { ArrowRight, Search, Filter } from 'lucide-react';
import { MOCK_TRANSACTIONS } from '@/lib/constants';
import { formatPoints, formatDate } from '@/lib/utils';

export default function History() {
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState<'all' | 'charge' | 'use' | 'refund'>('all');

    const filteredTransactions = MOCK_TRANSACTIONS.filter((tx) => {
        const matchesSearch = tx.description.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesType = filterType === 'all' || tx.type === filterType;
        return matchesSearch && matchesType;
    });

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900">거래 내역</h1>
                    <p className="text-gray-500 mt-1">모든 충전 및 사용 내역을 확인하세요.</p>
                </div>

                {/* Filters */}
                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6">
                    <div className="flex flex-col sm:flex-row gap-4">
                        {/* Search */}
                        <div className="flex-1 relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <Search size={18} className="text-gray-400" />
                            </div>
                            <input
                                type="text"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                placeholder="거래 내역 검색..."
                                className="block w-full pl-10 pr-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            />
                        </div>

                        {/* Type Filter */}
                        <div className="flex items-center gap-2">
                            <Filter size={18} className="text-gray-500" />
                            <select
                                value={filterType}
                                onChange={(e) => setFilterType(e.target.value as any)}
                                className="border-gray-200 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 py-2"
                            >
                                <option value="all">전체</option>
                                <option value="charge">충전</option>
                                <option value="use">사용</option>
                                <option value="refund">환불</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* Transactions List */}
                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                    {filteredTransactions.length === 0 ? (
                        <div className="p-12 text-center text-gray-500">
                            거래 내역이 없습니다.
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-100">
                            {filteredTransactions.map((tx) => (
                                <div key={tx.id} className="p-4 hover:bg-gray-50 transition-colors">
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-4">
                                            <div className={`w-12 h-12 rounded-full flex items-center justify-center ${tx.type === 'charge' ? 'bg-green-100 text-green-600' :
                                                    tx.type === 'refund' ? 'bg-blue-100 text-blue-600' :
                                                        'bg-orange-100 text-orange-600'
                                                }`}>
                                                {tx.type === 'charge' ? (
                                                    <ArrowRight className="rotate-45" size={20} />
                                                ) : tx.type === 'refund' ? (
                                                    <ArrowRight className="rotate-180" size={20} />
                                                ) : (
                                                    <ArrowRight className="-rotate-45" size={20} />
                                                )}
                                            </div>
                                            <div>
                                                <p className="font-semibold text-gray-900">{tx.description}</p>
                                                <p className="text-sm text-gray-500">{formatDate(tx.date)}</p>
                                            </div>
                                        </div>
                                        <div className="text-right">
                                            <p className={`font-bold text-lg ${tx.type === 'charge' ? 'text-green-600' :
                                                    tx.type === 'refund' ? 'text-blue-600' :
                                                        'text-gray-900'
                                                }`}>
                                                {tx.type === 'charge' || tx.type === 'refund' ? '+' : ''}{formatPoints(tx.amount)}
                                            </p>
                                            {tx.balance !== undefined && (
                                                <p className="text-sm text-gray-500">잔액: {formatPoints(tx.balance)}</p>
                                            )}
                                            <span className={`inline-block mt-1 text-xs px-2 py-1 rounded-full ${tx.status === 'completed' ? 'bg-green-100 text-green-700' :
                                                    tx.status === 'pending' ? 'bg-yellow-100 text-yellow-700' :
                                                        'bg-red-100 text-red-700'
                                                }`}>
                                                {tx.status === 'completed' ? '완료' :
                                                    tx.status === 'pending' ? '대기' : '실패'}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}
