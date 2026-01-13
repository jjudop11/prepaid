'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowRight, Lock, User } from 'lucide-react';
import Link from 'next/link';
import { login, type LoginData } from '@/lib/api/auth';
import { NAVER_COLORS } from '@/lib/constants';

type LoginTab = 'traditional' | 'oauth';

export default function Home() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<LoginTab>('traditional');
  const [formData, setFormData] = useState<LoginData>({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleTraditionalLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(formData);
    setLoading(false);

    if (result.success) {
      router.push('/dashboard');
    } else {
      setError(result.message || '로그인에 실패했습니다');
    }
  };

  const handleNaverLogin = () => {
    setLoading(true);
    // TODO: 실제 네이버 OAuth 플로우 구현
    setTimeout(() => {
      const mockToken = 'mock-jwt-token-' + Date.now();
      localStorage.setItem('auth_token', mockToken);
      router.push('/dashboard');
    }, 1000);
  };

  return (
    <div className="min-h-screen flex bg-white">
      {/* Left Side - Login Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-center p-8 sm:p-12 xl:p-24 relative z-10">
        <div className="max-w-md w-full mx-auto">
          {/* Header */}
          <div className="mb-10">
            <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-2xl mb-6">
              P
            </div>
            <h1 className="text-4xl font-bold text-gray-900 tracking-tight mb-3">환영합니다</h1>
            <p className="text-gray-500 text-lg">
              선불 결제 플랫폼으로 간편하게 포인트를 관리하세요.
            </p>
          </div>

          {/* Tabs */}
          <div className="flex gap-2 mb-6 border-b border-gray-200">
            <button
              onClick={() => setActiveTab('traditional')}
              className={`flex-1 py-3 font-semibold transition-all ${activeTab === 'traditional'
                  ? 'text-blue-600 border-b-2 border-blue-600'
                  : 'text-gray-500 hover:text-gray-700'
                }`}
            >
              일반 로그인
            </button>
            <button
              onClick={() => setActiveTab('oauth')}
              className={`flex-1 py-3 font-semibold transition-all ${activeTab === 'oauth'
                  ? 'text-blue-600 border-b-2 border-blue-600'
                  : 'text-gray-500 hover:text-gray-700'
                }`}
            >
              간편 로그인
            </button>
          </div>

          {/* Traditional Login Form */}
          {activeTab === 'traditional' && (
            <form onSubmit={handleTraditionalLogin} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">아이디</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <User size={20} />
                  </div>
                  <input
                    type="text"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                    placeholder="아이디를 입력하세요"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Lock size={20} />
                  </div>
                  <input
                    type="password"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                    placeholder="비밀번호를 입력하세요"
                    required
                  />
                </div>
              </div>

              {error && (
                <div className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className={`w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
              >
                {loading ? '로그인 중...' : (
                  <>
                    로그인
                    <ArrowRight size={18} className="ml-2" />
                  </>
                )}
              </button>

              <div className="text-center">
                <Link href="/signup" className="text-sm font-medium text-blue-600 hover:text-blue-500">
                  회원가입하기
                </Link>
              </div>
            </form>
          )}

          {/* OAuth Login */}
          {activeTab === 'oauth' && (
            <div className="space-y-4">
              <button
                onClick={handleNaverLogin}
                disabled={loading}
                className="w-full flex justify-center items-center py-3 px-4 rounded-lg shadow-sm text-sm font-semibold text-white transition-all"
                style={{ backgroundColor: NAVER_COLORS.GREEN }}
              >
                {loading ? '로그인 중...' : (
                  <>
                    <span className="mr-2 font-bold">N</span>
                    네이버로 로그인
                  </>
                )}
              </button>
              <p className="text-xs text-center text-gray-500">
                네이버 계정으로 간편하게 시작하세요
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Right Side - Visual */}
      <div className="hidden lg:block lg:w-1/2 relative bg-gray-50 p-6">
        <div className="absolute inset-0 z-0">
          <div className="absolute inset-0 bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px] [mask-image:radial-gradient(ellipse_50%_50%_at_50%_50%,#000_70%,transparent_100%)]"></div>
        </div>
        <div className="relative z-10 w-full h-full flex items-center justify-center">
          <div className="text-center bg-white/90 backdrop-blur-md p-8 rounded-3xl border border-white/20 shadow-lg max-w-md">
            <h3 className="font-bold text-gray-900 text-2xl mb-3">실시간 알림</h3>
            <p className="text-gray-600 text-base mb-4">
              충전, 사용, 취소 내역을 실시간으로 확인하세요.
            </p>
            <div className="flex items-center justify-center gap-2 text-blue-600 font-semibold">
              <span>시작하기</span>
              <ArrowRight size={18} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
