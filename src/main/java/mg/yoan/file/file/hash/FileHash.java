package mg.yoan.file.file.hash;

import mg.yoan.file.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
