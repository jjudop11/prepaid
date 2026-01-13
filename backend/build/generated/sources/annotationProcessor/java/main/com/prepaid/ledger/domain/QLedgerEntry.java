package com.prepaid.ledger.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLedgerEntry is a Querydsl query type for LedgerEntry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLedgerEntry extends EntityPathBase<LedgerEntry> {

    private static final long serialVersionUID = -668974984L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLedgerEntry ledgerEntry = new QLedgerEntry("ledgerEntry");

    public final EnumPath<BucketType> bucketType = createEnum("bucketType", BucketType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final StringPath memo = createString("memo");

    public final NumberPath<Long> originEntryId = createNumber("originEntryId", Long.class);

    public final StringPath referenceId = createString("referenceId");

    public final NumberPath<Long> reversedEntryId = createNumber("reversedEntryId", Long.class);

    public final EnumPath<LedgerStatus> status = createEnum("status", LedgerStatus.class);

    public final EnumPath<TxType> txType = createEnum("txType", TxType.class);

    public final com.prepaid.domain.QWallet wallet;

    public QLedgerEntry(String variable) {
        this(LedgerEntry.class, forVariable(variable), INITS);
    }

    public QLedgerEntry(Path<? extends LedgerEntry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLedgerEntry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLedgerEntry(PathMetadata metadata, PathInits inits) {
        this(LedgerEntry.class, metadata, inits);
    }

    public QLedgerEntry(Class<? extends LedgerEntry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.wallet = inits.isInitialized("wallet") ? new com.prepaid.domain.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

