package com.prepaid.wallet.service;

import com.prepaid.common.exception.specific.WalletNotFoundException;
import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.repository.WalletRepository;
import com.prepaid.wallet.dto.WalletBalanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.prepaid.common.logging.LoggingUtils.*;

/**
 * 지갑 서비스
 * - 잔액 조회
 * - 지갑 정보 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    /**
     * 사용자 잔액 조회
     */
    @Transactional(readOnly = true)
    public WalletBalanceDTO getBalance(User user) {
        setUserContext(user.getId());
        
        try {
            Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new WalletNotFoundException());
            
            return new WalletBalanceDTO(
                wallet.getBalance(),
                wallet.getBalancePaid(),
                wallet.getBalanceFree(),
                wallet.getUpdatedAt()
            );
        } finally {
            clearContext();
        }
    }
}
