export interface Notification {
  id: string;
  type: 'success' | 'error' | 'info';
  title: string;
  message: string;
  eventType: string;
  timestamp: number;
}

export interface Transaction {
  id: string;
  type: 'charge' | 'use' | 'refund';
  amount: number;
  description: string;
  date: string;
  status: 'completed' | 'pending' | 'failed';
  balance?: number;
}

export interface User {
  id: string;
  name: string;
  email: string;
  balance: number;
}

export interface ChartData {
  name: string;
  value: number;
}

export interface WalletStats {
  totalBalance: number;
  monthlySpending: number;
  monthlyCharged: number;
}

export type ConnectionStatus = 'connected' | 'reconnecting' | 'offline';
