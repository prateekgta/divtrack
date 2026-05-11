package io.divtrack.dividend.infrastructure.persistence;

import io.divtrack.dividend.domain.model.BillMapping;
import io.divtrack.dividend.domain.port.BillMappingRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataBillMappingRepository extends JpaRepository<BillMapping, String> {
    List<BillMapping> findByUserId(String userId);
}

@org.springframework.stereotype.Component
class JpaBillMappingRepository implements BillMappingRepository {

    private final SpringDataBillMappingRepository repo;

    JpaBillMappingRepository(SpringDataBillMappingRepository repo) { this.repo = repo; }

    @Override
    public List<BillMapping> findByUserId(String userId) { return repo.findByUserId(userId); }

    @Override
    public Optional<BillMapping> findById(String id) { return repo.findById(id); }

    @Override
    public BillMapping save(BillMapping mapping) { return repo.save(mapping); }

    @Override
    public void delete(BillMapping mapping) { repo.delete(mapping); }
}
