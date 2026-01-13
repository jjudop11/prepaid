package com.prepaid.ledger.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLedgerLine is a Querydsl query type for LedgerLine
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLedgerLine extends EntityPathBase<LedgerLine> {

    private static final long serialVersionUID = -991207634L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLedgerLine ledgerLine = new QLedgerLine("ledgerLine");

    public final EnumPath<AccountCode> accountCode = createEnum("accountCode", AccountCode.class);

    public final NumberPath<Long> amountSigned = createNumber("amountSigned", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QLedgerEntry entry;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QLedgerLine(String variable) {
        this(LedgerLine.class, forVariable(variable), INITS);
    }

    public QLedgerLine(Path<? extends LedgerLine> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLedgerLine(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLedgerLine(PathMetadata metadata, PathInits inits) {
        this(LedgerLine.class, metadata, inits);
    }

    public QLedgerLine(Class<? extends LedgerLine> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.entry = inits.isInitialized("entry") ? new QLedgerEntry(forProperty("entry"), inits.get("entry")) : null;
    }

}

