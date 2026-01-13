'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { CheckCircle2, XCircle } from 'lucide-react';
import { signup, type SignupData } from '@/lib/api/auth';

export default function SignupPage() {
    const router = useRouter();
    const [formData, setFormData] = useState<SignupData>({
        username: '',
        password: '',
        email: '',
    });
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState<{ [key: string]: string }>({});

    // 비밀번호 요구사항 체크
    const passwordChecks = {
        length: formData.password.length >= 8,
        hasLetter: /[a-zA-Z]/.test(formData.password),
        hasDigit: /\d/.test(formData.password),
        hasSpecial: /[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(formData.password),
        noUsername: !formData.password.toLowerCase().includes(formData.username.toLowerCase().substring(0, 3)) || formData.username.length < 3,
        match: formData.password === passwordConfirm && formData.password.length > 0,
    };

    const allValid = Object.values(passwordChecks).every(Boolean) && /^[a-z0-9]{4,20}$/.test(formData.username);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrors({});

        if (!allValid) {
            setErrors({ general: '모든 요구사항을 충족해야 합니다' });
            return;
        }

        setLoading(true);
        const result = await signup({
            username: formData.username,
            password: formData.password,
            email: formData.email || undefined,
        });

        if (result.success) {
            alert(result.message);
            router.push('/');
        } else {
            setErrors({ general: result.message });
        }
        setLoading(false);
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-lg p-8">
                {/* Header */}
                <div className="mb-8">
                    <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-2xl mb-4">
                        P
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900">회원가입</h1>
                    <p className="text-gray-500 mt-2">새 계정을 만들어 시작하세요</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    {/* Username */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">아이디</label>
                        <input
                            type="text"
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value.toLowerCase() })}
                            className="block w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="영문 소문자와 숫자 (4-20자)"
                            required
                        />
                        {formData.username && !/^[a-z0-9]{4,20}$/.test(formData.username) && (
                            <p className="text-xs text-red-600 mt-1">영문 소문자와 숫자만 사용 가능 (4-20자)</p>
                        )}
                    </div>

                    {/* Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
                        <input
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            className="block w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="최소 8자 / 영문+숫자+특수기호"
                            required
                        />
                    </div>

                    {/* Password Confirm */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">비밀번호 확인</label>
                        <input
                            type="password"
                            value={passwordConfirm}
                            onChange={(e) => setPasswordConfirm(e.target.value)}
                            className="block w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="비밀번호를 다시 입력하세요"
                            required
                        />
                    </div>

                    {/* Password Requirements */}
                    <div className="bg-gray-50 p-4 rounded-lg space-y-2 text-sm">
                        <p className="font-medium text-gray-700 mb-2">비밀번호 요구사항:</p>
                        <CheckItem checked={passwordChecks.length} text="최소 8자 이상" />
                        <CheckItem checked={passwordChecks.hasLetter} text="영문자 포함" />
                        <CheckItem checked={passwordChecks.hasDigit} text="숫자 포함" />
                        <CheckItem checked={passwordChecks.hasSpecial} text="특수기호 포함 (!@#$%^&* 등)" />
                        <CheckItem checked={passwordChecks.noUsername} text="아이디 미포함" />
                        <CheckItem checked={passwordChecks.match} text="비밀번호 일치" />
                    </div>

                    {/* Email (Optional) */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">이메일 (선택사항)</label>
                        <input
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="block w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                            placeholder="email@example.com"
                        />
                    </div>

                    {/* Error Message */}
                    {errors.general && (
                        <div className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">
                            {errors.general}
                        </div>
                    )}

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading || !allValid}
                        className={`w-full py-3 rounded-lg font-semibold text-white transition-all ${loading || !allValid
                                ? 'bg-gray-300 cursor-not-allowed'
                                : 'bg-blue-600 hover:bg-blue-700 hover:shadow-lg'
                            }`}
                    >
                        {loading ? '처리 중...' : '회원가입'}
                    </button>
                </form>

                {/* Login Link */}
                <div className="mt-6 text-center text-sm text-gray-600">
                    이미 계정이 있으신가요?{' '}
                    <Link href="/" className="text-blue-600 hover:text-blue-700 font-medium">
                        로그인하기
                    </Link>
                </div>
            </div>
        </div>
    );
}

function CheckItem({ checked, text }: { checked: boolean; text: string }) {
    return (
        <div className="flex items-center gap-2">
            {checked ? (
                <CheckCircle2 size={16} className="text-green-600 shrink-0" />
            ) : (
                <XCircle size={16} className="text-gray-300 shrink-0" />
            )}
            <span className={checked ? 'text-green-700' : 'text-gray-500'}>{text}</span>
        </div>
    );
}
