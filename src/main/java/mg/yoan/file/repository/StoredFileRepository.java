package mg.yoan.file.repository;

import java.util.UUID;
import mg.yoan.file.repository.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}
