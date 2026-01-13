'use client';

import { ArrowUpRight, ArrowDownRight } from 'lucide-react';

interface StatCardProps {
    title: string;
    value: string;
    trend?: string;
    trendType?: 'positive' | 'negative' | 'neutral';
    icon: React.ReactNode;
    highlight?: boolean;
}

export function StatCard({ title, value, trend, trendType = 'neutral', icon, highlight = false }: StatCardProps) {
    return (
        <div className={`p-6 rounded-2xl border transition-all duration-300 hover:shadow-lg ${highlight ? 'bg-blue-600 text-white border-blue-600' : 'bg-white border-gray-100'}`}>
            <div className="flex justify-between items-start mb-4">
                <div className={`p-2 rounded-lg ${highlight ? 'bg-blue-500 text-white' : 'bg-gray-50 text-gray-600'}`}>
                    {icon}
                </div>
                {trend && (
                    <div className={`flex items-center gap-1 text-xs font-semibold px-2 py-1 rounded-full ${highlight
                            ? 'bg-blue-500 text-white'
                            : trendType === 'positive'
                                ? 'bg-green-100 text-green-700'
                                : 'bg-red-100 text-red-700'
                        }`}>
                        {trendType === 'positive' ? <ArrowUpRight size={12} /> : <ArrowDownRight size={12} />}
                        {trend}
                    </div>
                )}
            </div>
            <div>
                <p className={`text-sm font-medium mb-1 ${highlight ? 'text-blue-100' : 'text-gray-500'}`}>{title}</p>
                <h3 className="text-2xl font-bold tracking-tight">{value}</h3>
            </div>
        </div>
    );
}
