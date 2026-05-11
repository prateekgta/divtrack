package io.divtrack.dividend.domain.port;

import io.divtrack.dividend.domain.model.BillMapping;

import java.util.List;
import java.util.Optional;

public interface BillMappingRepository {
    List<BillMapping> findByUserId(String userId);
    Optional<BillMapping> findById(String id);
    BillMapping save(BillMapping mapping);
    void delete(BillMapping mapping);
}
