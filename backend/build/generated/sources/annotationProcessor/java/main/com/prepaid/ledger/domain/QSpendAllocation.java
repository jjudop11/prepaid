package com.prepaid.ledger.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSpendAllocation is a Querydsl query type for SpendAllocation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSpendAllocation extends EntityPathBase<SpendAllocation> {

    private static final long serialVersionUID = -284893875L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSpendAllocation spendAllocation = new QSpendAllocation("spendAllocation");

    public final NumberPath<Long> amountConsumed = createNumber("amountConsumed", Long.class);

    public final QChargeLot chargeLot;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLedgerEntry spendEntry;

    public QSpendAllocation(String variable) {
        this(SpendAllocation.class, forVariable(variable), INITS);
    }

    public QSpendAllocation(Path<? extends SpendAllocation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSpendAllocation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSpendAllocation(PathMetadata metadata, PathInits inits) {
        this(SpendAllocation.class, metadata, inits);
    }

    public QSpendAllocation(Class<? extends SpendAllocation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chargeLot = inits.isInitialized("chargeLot") ? new QChargeLot(forProperty("chargeLot"), inits.get("chargeLot")) : null;
        this.spendEntry = inits.isInitialized("spendEntry") ? new QLedgerEntry(forProperty("spendEntry"), inits.get("spendEntry")) : null;
    }

}

