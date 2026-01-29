#!/bin/bash

# 환경변수 로드 및 애플리케이션 실행 스크립트

set -e

echo "=================================================="
echo "🚀 Prepaid Platform 실행 스크립트"
echo "=================================================="

# 1. .env 파일 존재 확인
if [ ! -f "../.env" ]; then
    echo "❌ .env 파일이 없습니다!"
    echo "📝 다음 명령을 실행하세요:"
    echo "   cp ../.env.example ../.env"
    echo "   # 그 다음 .env 파일에 실제 API 키를 입력하세요"
    exit 1
fi

echo "✅ .env 파일 발견"

# 2. 환경변수 로드
echo "📦 환경변수 로드 중..."
export $(cat ../.env | grep -v '^#' | grep -v '^$' | xargs)

# Spring 프로파일 설정
export SPRING_PROFILES_ACTIVE=local

# 3. 필수 환경변수 체크
required_vars=("NAVER_CLIENT_ID" "NAVER_CLIENT_SECRET" "TOSS_SECRET_KEY" "JWT_SECRET" "JWT_REFRESH_EXPIRATION")
missing_vars=()

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ] || [ "${!var}" = "YOUR_"* ] || [ "${!var}" = "your_"* ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -gt 0 ]; then
    echo "❌ 다음 환경변수가 설정되지 않았습니다:"
    for var in "${missing_vars[@]}"; do
        echo "   - $var"
    done
    echo ""
    echo "📝 .env 파일을 열어서 실제 API 키를 입력하세요"
    echo "   vi ../.env"
    exit 1
fi

echo "✅ 모든 필수 환경변수 확인 완료"

# 4. 애플리케이션 실행
echo ""
echo "🎯 Spring Boot 애플리케이션 실행 중..."
echo "=================================================="

./gradlew bootRun
