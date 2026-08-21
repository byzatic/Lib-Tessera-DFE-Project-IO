package io.github.cherepavel.tessera.configurator.routine.spi;

/**
 * Supplies configuration-time metadata for one Tessera workflow routine.
 *
 * <p>Implementations must have a public no-argument constructor so they can be
 * discovered through {@link java.util.ServiceLoader}.</p>
 */
public interface RoutineEditorDescriptorProvider {

    /**
     * Returns the complete editor declaration for the routine.
     *
     * @return a non-null immutable declaration
     */
    RoutineEditorDescriptor getDescriptor();
}
