package io.github.byzatic.tessera.lib.configio.unified.spi.routine;

/**
 * Supplies configuration-time metadata for one Tessera workflow routine.
 *
 * <p>Implementations must have a public no-argument constructor and must be registered under
 * {@code META-INF/services/io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptorProvider}.</p>
 */
public interface RoutineEditorDescriptorProvider {

    /**
     * Returns the complete editor declaration for the routine.
     *
     * @return a non-null immutable declaration
     */
    RoutineEditorDescriptor getDescriptor();
}
