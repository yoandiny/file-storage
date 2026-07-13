package mg.yoan.file.service;

import java.util.List;
import lombok.AllArgsConstructor;
import mg.yoan.file.repository.StoredFileRepository;
import mg.yoan.file.repository.model.StoredFile;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoredFileService {
  private final StoredFileRepository storedFileRepository;

  public List<StoredFile> findAll() {
    return storedFileRepository.findAll();
  }
}
