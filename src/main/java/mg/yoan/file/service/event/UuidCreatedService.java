package mg.yoan.file.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mg.yoan.file.PojaGenerated;
import mg.yoan.file.endpoint.event.model.UuidCreated;
import mg.yoan.file.repository.DummyUuidRepository;
import mg.yoan.file.repository.model.DummyUuid;
import org.springframework.stereotype.Service;

@PojaGenerated
@Service
@AllArgsConstructor
@Slf4j
public class UuidCreatedService implements Consumer<UuidCreated> {

  private final DummyUuidRepository dummyUuidRepository;

  @Override
  public void accept(UuidCreated uuidCreated) {
    var dummyUuid = new DummyUuid();
    dummyUuid.setId(uuidCreated.getUuid());
    dummyUuidRepository.save(dummyUuid);
  }
}
