package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.resolvers.ArtifactStorageException;

import java.io.IOException;

/**
 * @author mtodorov
 */
public interface RepositoryManagementService
        extends BasicRepositoryService
{

    void createRepository(String storageId, String repositoryId)
            throws IOException;

    void mergeRepositoryIndex(String sourceStorage,
                              String sourceRepositoryId,
                              String targetStorage,
                              String targetRepositoryId)
            throws ArtifactStorageException;

    void removeRepository(String storageId,
                          String repositoryId,
                          boolean deleteContents)
            throws IOException;

}
