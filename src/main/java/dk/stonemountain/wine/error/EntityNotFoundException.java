package dk.stonemountain.wine.error;

import dk.stonemountain.wine.domain.Wine;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

public class EntityNotFoundException extends RuntimeException {
    private final Long id;
    private final Class<? extends PanacheEntity> entityClass;
	private final String reason;

    public EntityNotFoundException(Long id, Class<? extends PanacheEntity> entityClass) {
        super(String.format("Entity with id %s of type %s not found", id, entityClass));
        this.id = id;
        this.entityClass = entityClass;
        this.reason = "";
    }

    public EntityNotFoundException(String reason, Long id, Class<? extends Wine> entityClass) {
        super(String.format("Entity with id %s of type %s (reason = %s) not found", id, entityClass, reason));
        this.id = id;
        this.entityClass = entityClass;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Class<? extends PanacheEntity> getEntityClass() {
        return entityClass;
    }

    public String getReason() {
        return reason;
    }
}
