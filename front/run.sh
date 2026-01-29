#!/bin/bash

# 프론트엔드 실행 스크립트
# 환경변수 확인 및 Next.js 실행

set -e

echo "=================================================="
echo "🎨 프론트엔드 실행 스크립트"
echo "=================================================="

# 1. .env.local 파일 존재 확인
if [ ! -f ".env.local" ]; then
    echo "❌ .env.local 파일이 없습니다!"
    echo "📝 다음 명령을 실행하세요:"
    echo "   cp env.template .env.local"
    echo "   # 그 다음 .env.local 파일에 Toss Client Key를 입력하세요"
    exit 1
fi

echo "✅ .env.local 파일 발견"

# 2. Toss Client Key 확인
if grep -q "YOUR_CLIENT_KEY_HERE" .env.local; then
    echo "⚠️  경고: TOSS_CLIENT_KEY가 아직 설정되지 않았습니다"
    echo "📝 .env.local 파일을 열어서 실제 Toss Client Key를 입력하세요"
    echo ""
fi

# 3. 패키지 설치 확인
if [ ! -d "node_modules" ]; then
    echo "📦 패키지 설치 중..."
    npm install
fi

# 4. Next.js 실행
echo ""
echo "🚀 Next.js 개발 서버 실행 중..."
echo "=================================================="

npm run dev
