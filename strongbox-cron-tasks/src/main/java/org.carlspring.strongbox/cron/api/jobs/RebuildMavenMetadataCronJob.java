package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Kate Novik
 */
public class RebuildMavenMetadataCronJob
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(RebuildMavenMetadataCronJob.class);

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private JobManager manager;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed RebuildMavenMetadataCronJob.");

        CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");
        try
        {
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");
            String basePath = config.getProperty("basePath");

            if (storageId == null)
            {
                Map<String, Storage> storages = getStorages();
                for (String storage : storages.keySet())
                {
                    rebuildRepositories(storage);
                }
            }
            else if (repositoryId == null)
            {
                rebuildRepositories(storageId);
            }
            else
            {
                artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            logger.error(e.getMessage(), e);
        }

        manager.addExecutedJob(config.getName(), true);
    }

    /**
     * To rebuild artifact's metadata in repositories
     *
     * @param storageId path of storage
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void rebuildRepositories(String storageId)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Map<String, Repository> repositories = getRepositories(storageId);

        for (String repository : repositories.keySet())
        {
            artifactMetadataService.rebuildMetadata(storageId, repository, null);
        }
    }

    private Map<String, Storage> getStorages()
    {
        return configurationManager.getConfiguration().getStorages();
    }

    private Map<String, Repository> getRepositories(String storageId)
    {
        return getStorages().get(storageId).getRepositories();
    }

}
