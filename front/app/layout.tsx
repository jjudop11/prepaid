import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

import { NotificationProvider } from '@/components/NotificationProvider';

export const metadata: Metadata = {
  title: "PointFlow - 선불 결제 플랫폼",
  description: "실시간 알림과 함께하는 스마트한 포인트 관리",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="antialiased">
        <NotificationProvider>
          {children}
        </NotificationProvider>
      </body>
    </html>
  );
}
