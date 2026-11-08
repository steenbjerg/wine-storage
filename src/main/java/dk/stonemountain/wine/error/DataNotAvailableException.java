package dk.stonemountain.wine.error;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Page;

public class DataNotAvailableException extends RuntimeException {
    private Class<? extends PanacheEntity> entityClass;
	private Page page;

	public DataNotAvailableException(Page page, Class<? extends PanacheEntity> entityClass) {
        this.page = page;
        this.entityClass = entityClass;
    }

    public Page getPage() {
        return page;
    }

    public Class<? extends PanacheEntity> getEntityClass() {
        return entityClass;
    }
}
