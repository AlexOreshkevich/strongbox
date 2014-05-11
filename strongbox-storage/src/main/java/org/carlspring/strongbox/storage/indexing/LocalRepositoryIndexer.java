package org.carlspring.strongbox.storage.indexing;

import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.MUST_NOT;

public class LocalRepositoryIndexer
{

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LocalRepositoryIndexer.class);

    final PlexusContainer plexus;

    final Indexer indexer;

    final Scanner scanner;

    final List<IndexCreator> indexers;

    final IndexingContext context;


    public LocalRepositoryIndexer(final String repoId,
                                  final File localRepo,
                                  final File indexDir)
            throws PlexusContainerException,
                   ComponentLookupException,
                   IOException
    {
        plexus = new DefaultPlexusContainer();
        indexer = plexus.lookup(Indexer.class);
        scanner = plexus.lookup(Scanner.class);

        // @TODO: make a list of indexers configurable
        indexers = asList(plexus.lookup(IndexCreator.class, "min"),
                          plexus.lookup(IndexCreator.class, "jarContent"),
                          plexus.lookup(IndexCreator.class, "maven-plugin"));

        // @TODO: remove once no longer needed
        plexus.lookup(LoggerManager.class).setThresholds(Logger.LEVEL_DEBUG);

        context = indexer.createIndexingContext(repoId + "/ctx",
                                            repoId,
                                            localRepo,
                                            indexDir,
                                            null,
                                            null,
                                            true, // if context should be searched in non-targeted mode.
                                            true, // if indexDirectory is known to contain (or should contain)
                                                  // valid Maven Indexer lucene index, and no checks needed to be
                                                  // performed, or, if we want to "stomp" over existing index
                                                  // (unsafe to do!).
                                            indexers);
    }

    void close(boolean deleteFiles)
            throws IOException
    {
        context.close(deleteFiles);
    }

    public void delete(final Collection<ArtifactInfo> artifacts)
            throws IOException
    {
        final List<ArtifactContext> delete = new ArrayList<ArtifactContext>();
        for (final ArtifactInfo artifact : artifacts)
        {
            delete.add(new ArtifactContext(null, null, null, artifact, null));
        }

        indexer.deleteArtifactsFromIndex(delete, context);
    }

    public Set<ArtifactInfo> search(final String groupId,
                                    final String artifactId,
                                    final String version)
            throws IOException
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(indexer.constructQuery(MAVEN.GROUP_ID, new SourcedSearchExpression(groupId)), MUST);
        query.add(indexer.constructQuery(MAVEN.ARTIFACT_ID, new SourcedSearchExpression(artifactId)), MUST);
        query.add(indexer.constructQuery(MAVEN.PACKAGING, new SourcedSearchExpression("jar")), MUST);
        query.add(indexer.constructQuery(MAVEN.CLASSIFIER, new SourcedSearchExpression(Field.NOT_PRESENT)), MUST_NOT);

        if (version != null)
        {
            query.add(indexer.constructQuery(MAVEN.VERSION, new SourcedSearchExpression(version)), MUST);
        }

        final FlatSearchResponse response = indexer.searchFlat(new FlatSearchRequest(query, context));

        return response.getResults();
    }

    @Async("indexPool")
    public int index(final File startingPath)
    {
        final ScanningResult scan = scanner.scan(new ScanningRequest(context,
                                                                     new ReindexArtifactScanningListener(),
                                                                     startingPath == null ? "." :
                                                                     startingPath.getPath()));
        return scan.getTotalFiles();
    }

    private class ReindexArtifactScanningListener
            implements ArtifactScanningListener
    {

        int totalFiles = 0;
        private IndexingContext ctx;

        @Override
        public void scanningStarted(final IndexingContext ctx)
        {
            this.ctx = ctx;
        }

        @Override
        public void scanningFinished(final IndexingContext ctx,
                                     final ScanningResult result)
        {
            result.setTotalFiles(totalFiles);
            logger.debug("scanning finished; total files: {}; has exception: {}", result.getTotalFiles(),
                         result.hasExceptions());
        }

        @Override
        public void artifactError(final ArtifactContext ac,
                                  final Exception ex)
        {
            logger.error("artifact error", ex);
        }

        @Override
        public void artifactDiscovered(final ArtifactContext ac)
        {
            try
            {
                indexer.addArtifactsToIndex(asList(ac), ctx);
                totalFiles++;
            }
            catch (IOException ex)
            {
                logger.error("artifact index error", ex);
            }
        }
    }

}
