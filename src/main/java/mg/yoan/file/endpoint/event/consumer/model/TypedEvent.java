package mg.yoan.file.endpoint.event.consumer.model;

import mg.yoan.file.PojaGenerated;
import mg.yoan.file.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
