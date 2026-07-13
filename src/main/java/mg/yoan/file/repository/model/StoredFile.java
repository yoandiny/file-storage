package mg.yoan.file.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file")
@Getter
@Setter
public class StoredFile {
  @Id private UUID id;
  private String name;
  private String userEmail;
  private Instant creationDatetime;
}
