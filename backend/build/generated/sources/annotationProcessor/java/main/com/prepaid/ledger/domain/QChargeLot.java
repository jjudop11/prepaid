package com.prepaid.ledger.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChargeLot is a Querydsl query type for ChargeLot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChargeLot extends EntityPathBase<ChargeLot> {

    private static final long serialVersionUID = -140662868L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChargeLot chargeLot = new QChargeLot("chargeLot");

    public final NumberPath<Long> amountRemaining = createNumber("amountRemaining", Long.class);

    public final NumberPath<Long> amountTotal = createNumber("amountTotal", Long.class);

    public final EnumPath<BucketType> bucketType = createEnum("bucketType", BucketType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> originalEntryId = createNumber("originalEntryId", Long.class);

    public final com.prepaid.domain.QWallet wallet;

    public QChargeLot(String variable) {
        this(ChargeLot.class, forVariable(variable), INITS);
    }

    public QChargeLot(Path<? extends ChargeLot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChargeLot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChargeLot(PathMetadata metadata, PathInits inits) {
        this(ChargeLot.class, metadata, inits);
    }

    public QChargeLot(Class<? extends ChargeLot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.wallet = inits.isInitialized("wallet") ? new com.prepaid.domain.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

