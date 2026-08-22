package io.github.byzatic.tessera.lib.configio.unified;

/** User-visible operation that failed in the unified project API. */
public enum TesseraProjectOperation {
    LOAD_PROJECT,
    SAVE_PROJECT,
    EXPORT_PROJECT,
    OPEN_RUNTIME,
    WATCH_REVISIONS,
    USE_RUNTIME,
    CLOSE_RUNTIME
}
