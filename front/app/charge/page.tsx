'use client';

import { useState } from 'react';
import { Navbar } from '@/components/Navbar';
import { CreditCard, Smartphone, ShieldCheck } from 'lucide-react';

export default function Charge() {
    const [amount, setAmount] = useState<number>(0);
    const [paymentMethod, setPaymentMethod] = useState<'card' | 'mobile'>('card');
    const [loading, setLoading] = useState(false);

    const presets = [10000, 30000, 50000, 100000];

    const handleCharge = () => {
        if (amount <= 0) return;
        setLoading(true);
        // TODO: 실제 Toss Payments 연동
        setTimeout(() => {
            setLoading(false);
            alert(`${amount.toLocaleString()} 포인트 충전 성공!`);
        }, 1500);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
                <div className="text-center mb-10">
                    <h1 className="text-3xl font-bold text-gray-900">포인트 충전</h1>
                    <p className="text-gray-500 mt-2">충전할 금액을 선택하고 안전하게 결제하세요.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {/* Left Column: Input */}
                    <div className="md:col-span-2 space-y-6">

                        {/* Amount Selection */}
                        <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
                            <h2 className="text-lg font-semibold text-gray-900 mb-4">금액 선택</h2>
                            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
                                {presets.map((val) => (
                                    <button
                                        key={val}
                                        onClick={() => setAmount(val)}
                                        className={`py-3 px-2 rounded-xl text-sm font-bold transition-all ${amount === val
                                                ? 'bg-blue-600 text-white shadow-md transform scale-105'
                                                : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
                                            }`}
                                    >
                                        {val.toLocaleString()} P
                                    </button>
                                ))}
                            </div>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <span className="text-gray-500 font-bold">P</span>
                                </div>
                                <input
                                    type="number"
                                    value={amount === 0 ? '' : amount}
                                    onChange={(e) => setAmount(Number(e.target.value))}
                                    placeholder="직접 입력"
                                    className="block w-full pl-8 pr-12 py-4 border border-gray-200 rounded-xl text-lg font-semibold focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                />
                            </div>
                        </div>

                        {/* Payment Method */}
                        <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
                            <h2 className="text-lg font-semibold text-gray-900 mb-4">결제 수단</h2>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div
                                    onClick={() => setPaymentMethod('card')}
                                    className={`cursor-pointer p-4 rounded-xl border-2 flex items-center gap-4 transition-all ${paymentMethod === 'card' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-300'
                                        }`}
                                >
                                    <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center shadow-sm text-blue-600">
                                        <CreditCard size={20} />
                                    </div>
                                    <div>
                                        <p className="font-bold text-gray-900">신용카드</p>
                                        <p className="text-xs text-gray-500">Visa, Mastercard, Amex</p>
                                    </div>
                                </div>

                                <div
                                    onClick={() => setPaymentMethod('mobile')}
                                    className={`cursor-pointer p-4 rounded-xl border-2 flex items-center gap-4 transition-all ${paymentMethod === 'mobile' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-300'
                                        }`}
                                >
                                    <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center shadow-sm text-gray-600">
                                        <Smartphone size={20} />
                                    </div>
                                    <div>
                                        <p className="font-bold text-gray-900">모바일 결제</p>
                                        <p className="text-xs text-gray-500">카카오페이, 네이버페이</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right Column: Summary */}
                    <div className="md:col-span-1">
                        <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-lg sticky top-24">
                            <h2 className="text-lg font-semibold text-gray-900 mb-6">주문 요약</h2>

                            <div className="space-y-4 mb-6">
                                <div className="flex justify-between text-sm text-gray-600">
                                    <span>포인트 금액</span>
                                    <span className="font-medium">{amount.toLocaleString()} P</span>
                                </div>
                                <div className="flex justify-between text-sm text-gray-600">
                                    <span>수수료 (0%)</span>
                                    <span className="font-medium">0 P</span>
                                </div>
                                <div className="border-t border-gray-100 pt-4 flex justify-between items-center">
                                    <span className="font-bold text-gray-900">합계</span>
                                    <span className="text-2xl font-bold text-blue-600">{amount.toLocaleString()} P</span>
                                </div>
                            </div>

                            <div className="mb-6 bg-green-50 p-3 rounded-lg flex items-start gap-2">
                                <ShieldCheck className="text-green-600 shrink-0" size={18} />
                                <p className="text-xs text-green-700 leading-tight">
                                    256-bit SSL 암호화로 안전하게 거래가 보호됩니다.
                                </p>
                            </div>

                            <button
                                onClick={handleCharge}
                                disabled={loading || amount <= 0}
                                className={`w-full py-4 rounded-xl font-bold text-white shadow-md transition-all ${loading || amount <= 0
                                        ? 'bg-gray-300 cursor-not-allowed'
                                        : 'bg-blue-600 hover:bg-blue-700 hover:shadow-lg transform hover:-translate-y-0.5'
                                    }`}
                            >
                                {loading ? '처리 중...' : '결제하기'}
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
